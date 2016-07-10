/*
 * Copyright (C) 2016 Andrius Aucinas <andrius.aucinas@hatdex.org>
 * SPDX-License-Identifier: AGPL-3.0
 *
 * This file is part of the Hub of All Things project (HAT).
 *
 * HAT is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation, version 3 of
 * the License.
 *
 * HAT is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See
 * the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General
 * Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 */

package hatdex.hat.api.service

import akka.actor.ActorRefFactory
import akka.event.LoggingAdapter
import hatdex.hat.api.DatabaseInfo
import hatdex.hat.api.json.JsonProtocol
import hatdex.hat.api.models._
import hatdex.hat.dal.SlickPostgresDriver.api._
import hatdex.hat.dal.Tables._
import org.joda.time.LocalDateTime
import spray.json.{ JsonParser, _ }

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

// this trait defines our service behavior independently from the service actor
trait BundleService extends DataService {

  val logger: LoggingAdapter
  def actorRefFactory: ActorRefFactory
  import JsonProtocol._

  protected[api] def storeBundleContextless(bundle: ApiBundleContextless): Future[ApiBundleContextless] = {
    val bundleContextlessRow = BundleContextlessRow(0, bundle.name, LocalDateTime.now(), LocalDateTime.now())

    val maybeInsertedBundle = DatabaseInfo.db.run {
      (BundleContextless returning BundleContextless) += bundleContextlessRow
    }

    val apiBundle = bundle.sources map { sources =>
      val insertDatasets = for {
        insertedBundle <- maybeInsertedBundle
        dataFieldsWithTables <- getSourceFields(sources)
      } yield {
        // Empty Contextless Bundle
        val bundleDatasetRows = dataFieldsWithTables.map {
          case (sourceStructure, dataset, datasetTable, datasetFields) =>
            BundleContextlessDataSourceDatasetRow(0, insertedBundle.id, sourceStructure.source, dataset.name,
              datasetTable.id.get, dataset.description, dataset.fields.toJson.toString, datasetFields.flatMap(_.id).toList)
        }
        bundleDatasetRows.map { dataset =>
          BundleContextlessDataSourceDataset += dataset
        }
      }

      val insertedDatasets = insertDatasets.flatMap { case datasets =>
        Future.sequence(
          datasets.map { dataset =>
            DatabaseInfo.db.run(dataset).recover { case e =>
              logger.error(s"Error inserting dataset $dataset: ${e.getMessage}")
              logger.error(s"Executed statemetns: ${dataset.statements}")
                throw e
            }
          }
        )
      }

      for {
        insertedBundle <- maybeInsertedBundle
        _ <- insertedDatasets
      } yield {
        val bundleApi = ApiBundleContextless.fromBundleContextless(insertedBundle)
        bundleApi.copy(sources = Some(sources))
      }

    } getOrElse {
      Future.failed(new RuntimeException("Bundle has no sources defined"))
    }

    apiBundle recover {
      case e =>
        logger.error(s"Error creating Contextless Data Bundle: ${e.getMessage}")
        throw new RuntimeException(s"Error creating Contextless Data Bundle ${e.getMessage}")
    }
  }

  case class FieldRequested(sourceName: String, tableName: String, fieldName: String)

  private def getFieldsRequested(sourceName: String, tableName: String, sourceDataset: ApiBundleDataSourceField): Seq[FieldRequested] = {
    if (sourceDataset.fields.isEmpty || sourceDataset.fields.get.isEmpty) {
      // source name, table name, field name
      Seq(FieldRequested(sourceName, tableName, sourceDataset.name))
    }
    else {
      sourceDataset.fields.get.flatMap { subfield =>
        getFieldsRequested(sourceName, sourceDataset.name, subfield)
      }
    }
  }

  private def sourceStructureFieldsRequested(sources: Seq[ApiBundleDataSourceStructure]): Seq[FieldRequested] = {
    sources.flatMap { sourceStructure =>
      sourceStructure.datasets.flatMap { dataset =>
        dataset.fields.flatMap { field =>
          getFieldsRequested(sourceStructure.source, dataset.name, field)
        }
      }
    }
  }

  private def getSourceFields(sources: Seq[ApiBundleDataSourceStructure]): Future[Seq[(ApiBundleDataSourceStructure, ApiBundleDataSourceDataset, ApiDataTable, Seq[ApiDataField])]] = {
    val sourceDatasetFieldEventuals = for {
      source <- sources
      dataset <- source.datasets
    } yield {
      val fieldsRequested = dataset.fields.flatMap { field =>
        getFieldsRequested(source.source, dataset.name, field)
      }

      val requestedTableFieldsQuery = fieldsRequested.map { fReq =>
        for {
          sourceTable <- DataTable.filter(table => table.sourceName === source.source && table.name === dataset.name)
          t <- DataTable.filter(table => table.sourceName === fReq.sourceName && table.name === fReq.tableName)
          f <- DataField.filter(field => field.name === fReq.fieldName && field.tableIdFk === t.id)
        } yield (sourceTable, f)
      } reduceLeft { (q, field) =>
        q ++ field
      }

      DatabaseInfo.db.run {
        requestedTableFieldsQuery.result
      } map { tableFields =>
        // logger.info(s"Found table fields: $tableFields")
        val sourceDatasetTable = tableFields
          .headOption.map(_._1)
          .map(t => ApiDataTable.fromDataTable(t)(None)(None))
          .getOrElse(throw new IllegalArgumentException("None of the selected fields exist"))
        val dataFields = tableFields.map(_._2).map(f => ApiDataField.fromDataField(f))
        (source, dataset, sourceDatasetTable, dataFields)
      }
    }

    val eventualSourceDatasetField = Future.sequence(sourceDatasetFieldEventuals)
    eventualSourceDatasetField
  }

  // FIXME: reimplementation of one in DataService during Slick access migration
  //  private def getTablesRecursively(tableId: Int*): Future[Seq[(ApiDataTable, Option[Int])]] = {
  //    import hatdex.hat.dal.SlickPostgresDriver.api._
  //    val eventualTables = DatabaseInfo.db.run {
  //      DataTableTree.filter(_.rootTable === tableId).result
  //    }
  //    eventualTables map { tables =>
  //      tables.map { table =>
  //        (ApiDataTable.fromNestedTable(table)(None)(None), table.table1)
  //      }
  //    }
  //  }

  protected[api] def getBundleContextlessById(bundleId: Int): Future[Option[ApiBundleContextless]] = {
    getBundleContextlessWithDatasets(bundleId).map(_.map(_._1))
  }

  private def getBundleContextlessWithDatasets(bundleId: Int): Future[Option[(ApiBundleContextless, Set[Int])]] = {
    val bundleDatasetQuery = for {
      bundle <- BundleContextless.filter(_.id === bundleId)
      bundleDataset <- BundleContextlessDataSourceDataset.filter(_.bundleId === bundleId)
    } yield (bundle, bundleDataset)

    val eventualBundleDataset = DatabaseInfo.db.run {
      bundleDatasetQuery.result
    }
    eventualBundleDataset map { bundleWithDataset =>
      // Only one (or none) bundle mathcing specific ID
      val maybeBundle = bundleWithDataset.headOption.map(_._1)
      maybeBundle.map { bundle =>
        val sources = bundleWithDataset map {
          case (_, bundleDataset) =>
            val dataSourceDataset = ApiBundleDataSourceDataset(bundleDataset.datasetName, bundleDataset.description,
              bundleDataset.fieldStructure.parseJson.convertTo[List[ApiBundleDataSourceField]])

            val dataSourceStructure = ApiBundleDataSourceStructure(bundleDataset.sourceName, List(dataSourceDataset))

            // Return both the structure and the set of field ids separately
            (dataSourceStructure, bundleDataset.fieldIds)
        }
        // Flatten the set of fields of interest across all sources and datasets
        val fieldsOfInterest = sources.unzip._2.flatten.toSet
        val collatedSources = sources.unzip._1.groupBy(_.source).map {
          case (sourceName, structures) =>
            structures.reduceLeft { (sourceStructure, structure) =>
              sourceStructure.copy(datasets = sourceStructure.datasets ++ structure.datasets)
            }
        }
        val retrievedBundle = ApiBundleContextless(Some(bundle.id),
          Some(bundle.dateCreated), Some(bundle.lastUpdated),
          bundle.name, Some(collatedSources.toSeq))

        (retrievedBundle, fieldsOfInterest)
      }
    }

  }

  protected[api] def getBundleContextlessValues(
    bundleId: Int,
    maybeLimit: Option[Int],
    startTime: LocalDateTime,
    endTime: LocalDateTime): Future[ApiBundleContextlessData] = {

    val eventualSourceTables = for {
      bundle <- getBundleContextlessWithDatasets(bundleId).map(_.get)
      sourceTables <- getSourceTables(bundle._1.sources.get)
    } yield (bundle._1, bundle._2, sourceTables)

    val eventualValues = eventualSourceTables.flatMap { case (_, fieldset, _) =>
      val valueQuery = for {
        value <- DataValue.filter(_.fieldId inSet fieldset)
          .filter(v => v.dateCreated <= endTime && v.dateCreated >= startTime)
        record <- value.dataRecordFk//.sortBy(_.id.desc).take(maybeLimit.getOrElse(1000))
        field <- value.dataFieldFk
      } yield (record, field, value)

      DatabaseInfo.db.run(valueQuery.result)
    }

    val eventualValueRecords = for {
      dbValues <- eventualValues
      (bundle, _, tables) <- eventualSourceTables
    } yield {
      // Group values by record
      val byRecord = dbValues.groupBy(_._1)

      byRecord flatMap {
        case (record, recordValues: Seq[(DataRecordRow, DataFieldRow, DataValueRow)]) =>
          val fieldValues = recordValues.map { case (r, f, v) => (f, v) }
            .groupBy(_._1.id) // Group values by field
            .map { case (k, v) => (k, v.unzip._2.map(ApiDataValue.fromDataValue)) }

          val filledRecords = tables.flatMap {
            case table => // if recordValueTables.contains(table.id.get) =>
              val filledValues = fillStructure(table)(fieldValues)
              if (filledValues.fields.isDefined && filledValues.fields.get.nonEmpty ||
                filledValues.subTables.isDefined && filledValues.subTables.get.nonEmpty) {
                // Keep records separate for each root table
                Some(ApiDataRecord.fromDataRecord(record)(Some(Seq(filledValues))))
              } else {
                None
              }
          }
          filledRecords
      }
    }

    for {
      (bundle, _, tables) <- eventualSourceTables
      valueRecords <- eventualValueRecords
    } yield {
      val datasets = tables.map { table =>
        val recordsWithTable = valueRecords.filter { record =>
          record.tables
            .map(recordTable => recordTable.map(_.id))
            .getOrElse(Seq())
            .contains(table.id)
        }

        (table.source, ApiBundleContextlessDatasetData(table.name, table, Some(recordsWithTable.toSeq)))
      }
      val dataGroups = datasets.groupBy(_._1).map { case (k, v) => (k, v.unzip._2) }
      ApiBundleContextlessData(bundle.id.get, bundle.name, dataGroups)
    }
  }

  def fillStructure(table: ApiDataTable)(values: Map[Int, Seq[ApiDataValue]]): ApiDataTable = {
    val filledFields = table.fields map { fields =>
      // For each field, insert values
      fields flatMap {
        case ApiDataField(Some(fieldId), dateCreated, lastUpdated, tableId, fieldName, maybeValues) =>
          val fieldValues = values.get(fieldId)
          fieldValues.map { fValues =>
            // Create a new field with only the values updated
            ApiDataField(Some(fieldId), dateCreated, lastUpdated, tableId, fieldName, values.get(fieldId))
          }
      }
    }

    val filledSubtables = table.subTables map { subtables =>
      val filled = subtables map { subtable: ApiDataTable =>
        fillStructure(subtable)(values)
      }
      val nonEmpty = filled.filter { t =>
        (t.fields.nonEmpty && t.fields.get.nonEmpty) || (t.subTables.nonEmpty && t.subTables.get.nonEmpty)
      }
      nonEmpty
    }

    table.copy(fields = filledFields, subTables = filledSubtables)
  }

  private def getSourceTables(sources: Seq[ApiBundleDataSourceStructure]): Future[Seq[ApiDataTable]] = {
    val fieldsRequested = sourceStructureFieldsRequested(sources)
    // Get All Data Table trees matchinf source and name
    val dataTableTrees = sources.flatMap { sourceStructure =>
      sourceStructure.datasets.map { dataset =>
        for {
          rootTable <- DataTableTree.filter(_.sourceName === sourceStructure.source).filter(_.name === dataset.name)
          tree <- DataTableTree.filter(_.rootTable === rootTable.id)
        } yield tree
      }
    } reduceLeft { (q, tree) =>
      q ++ tree
    }

    // For fields extracted from Data Source Structure, find all matching fields in the database
    // Each table is uniquely identified by (sourceName, tableName), and fields are within them
    val requestedTableFieldsQuery = fieldsRequested.map { fReq =>
      for {
        t <- DataTable.filter(table => table.sourceName === fReq.sourceName && table.name === fReq.tableName)
        f <- DataField.filter(field => field.name === fReq.fieldName && field.tableIdFk === t.id)
      } yield f
    } reduceLeft { (q, field) =>
      q ++ field
    }

    // Another round of field filtering to only get those within the returned trees
    val treeFieldQuery = for {
      tree <- dataTableTrees
      field <- requestedTableFieldsQuery
    } yield (tree, field)

    DatabaseInfo.db.run(treeFieldQuery.result).map { treeFields =>
      val tablesWithParents = treeFields.map(_._1)                                    // Get the trees
        .map(tree => (ApiDataTable.fromNestedTable(tree)(None)(None), tree.table1))   // Use the API data model for each tree
        .distinct                                                                     // Take only distinct trees (duplicates returned with each field
      val fields = treeFields.map(_._2)
        .map(ApiDataField.fromDataField)
        .distinct
      val rootTables = tablesWithParents.filter(_._2.isEmpty).map(_._1)

      rootTables map { table =>
        buildTableStructure(table, fields, tablesWithParents)
      }
    }
  }
}