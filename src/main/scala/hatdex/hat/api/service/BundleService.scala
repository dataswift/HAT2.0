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

      val insertedDatasets = insertDatasets.flatMap {
        case datasets =>
          Future.sequence(
            datasets.map { dataset =>
              DatabaseInfo.db.run(dataset).recover {
                case e =>
                  logger.error(s"Error inserting dataset $dataset: ${e.getMessage}")
                  logger.error(s"Executed statemetns: ${dataset.statements}")
                  throw e
              }
            })
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

  protected[api] def getBundleContextlessValues(bundleId: Int, maybeLimit: Option[Int],
                                                startTime: LocalDateTime, endTime: LocalDateTime): Future[ApiBundleContextlessData] = {
    for {
      (bundle, fieldset) <- getBundleContextlessWithDatasets(bundleId).map(_.get)
      sourceTables <- getSourceTables(bundle.sources.get)
      values <- fieldsetValues(fieldset, startTime, endTime)
    } yield {
      val valueRecords = getValueRecords(values, sourceTables)
      val dataGroups = recordsDatasetGrouped(sourceTables, valueRecords)
      ApiBundleContextlessData(bundle.id.get, bundle.name, dataGroups)
    }
  }

  protected[api] def recordsDatasetGrouped(
    tables: Seq[ApiDataTable],
    valueRecords: Iterable[ApiDataRecord]): Map[String, Seq[ApiBundleContextlessDatasetData]] = {

    val datasets = tables.map { table =>
      val recordsWithTable = valueRecords.filter { record =>
        record.tables
          .map(recordTable => recordTable.map(_.id))
          .getOrElse(Seq())
          .contains(table.id)
      }

      (table.source, ApiBundleContextlessDatasetData(table.name, table, Some(recordsWithTable.toSeq)))
    }
    datasets.groupBy(_._1).map { case (k, v) => (k, v.unzip._2) }

  }

  private def getSourceTables(sources: Seq[ApiBundleDataSourceStructure]): Future[Seq[ApiDataTable]] = {
    val fieldsRequested = sourceStructureFieldsRequested(sources)
    val sourceDatasets = sources flatMap { source =>
      source.datasets.map { dataset =>
        (source.source, dataset.name)
      }
    }
    sourceDatasetTables(sourceDatasets, Some(fieldsRequested))
  }

  protected[api] def sourceDatasetTables(sourceDatasets: Seq[(String, String)], maybeFieldsRequested: Option[Seq[FieldRequested]]): Future[Seq[ApiDataTable]] = {
    // Get All Data Table trees matchinf source and name
    val dataTableTrees = sourceDatasets.map {
      case (source, dataset) =>
        for {
          rootTable <- DataTableTree.filter(_.sourceName === source).filter(_.name === dataset)
          tree <- DataTableTree.filter(_.rootTable === rootTable.id)
        } yield (rootTable, tree)
    } reduceLeft { (q, tree) =>
      q ++ tree
    }

    val eventualRoots = DatabaseInfo.db.run(dataTableTrees.map(_._1.id).result) map { roots => roots.flatten.toSet }
    eventualRoots flatMap { roots =>
      buildDataTreeStructures(dataTableTrees.map(_._2), roots)
    }
  }
}