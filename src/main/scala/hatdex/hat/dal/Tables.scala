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
package hatdex.hat.dal
// AUTO-GENERATED Slick data model
/** Stand-alone Slick data model for immediate use */
object Tables extends {
  val profile = hatdex.hat.dal.SlickPostgresDriver
} with Tables
/** Slick data model trait for extension, choice of backend or usage in the cake pattern. (Make sure to initialize this late.) */
trait Tables {
  val profile: hatdex.hat.dal.SlickPostgresDriver
  import profile.simple._
  import slick.model.ForeignKeyAction
  // NOTE: GetResult mappers for plain SQL are only generated for tables where Slick knows how to map the types of all columns.
  import slick.jdbc.{GetResult => GR}

  /** DDL for all tables. Call .create to execute. */
  lazy val schema = Array(BundleContext.schema, BundleContextEntitySelection.schema, BundleContextless.schema, BundleContextlessDataSourceDataset.schema, BundleContextPropertySelection.schema, BundleContextToBundleCrossref.schema, BundleContextTree.schema, DataDebit.schema, DataField.schema, DataRecord.schema, DataTable.schema, DataTabletotablecrossref.schema, DataTableTree.schema, DataValue.schema, Entity.schema, EventsEvent.schema, EventsEventlocationcrossref.schema, EventsEventorganisationcrossref.schema, EventsEventpersoncrossref.schema, EventsEventthingcrossref.schema, EventsEventtoeventcrossref.schema, EventsSystempropertydynamiccrossref.schema, EventsSystempropertystaticcrossref.schema, EventsSystemtypecrossref.schema, LocationsLocation.schema, LocationsLocationthingcrossref.schema, LocationsLocationtolocationcrossref.schema, LocationsSystempropertydynamiccrossref.schema, LocationsSystempropertystaticcrossref.schema, LocationsSystemtypecrossref.schema, OrganisationsOrganisation.schema, OrganisationsOrganisationlocationcrossref.schema, OrganisationsOrganisationthingcrossref.schema, OrganisationsOrganisationtoorganisationcrossref.schema, OrganisationsSystempropertydynamiccrossref.schema, OrganisationsSystempropertystaticcrossref.schema, OrganisationsSystemtypecrossref.schema, PeoplePerson.schema, PeoplePersonlocationcrossref.schema, PeoplePersonorganisationcrossref.schema, PeoplePersontopersoncrossref.schema, PeoplePersontopersonrelationshiptype.schema, PeopleSystempropertydynamiccrossref.schema, PeopleSystempropertystaticcrossref.schema, PeopleSystemtypecrossref.schema, StatsDataDebitClessBundleRecords.schema, StatsDataDebitDataFieldAccess.schema, StatsDataDebitDataTableAccess.schema, StatsDataDebitOperation.schema, StatsDataDebitRecordCount.schema, SystemEventlog.schema, SystemProperty.schema, SystemPropertyrecord.schema, SystemRelationshiprecord.schema, SystemRelationshiprecordtorecordcrossref.schema, SystemType.schema, SystemTypetotypecrossref.schema, SystemUnitofmeasurement.schema, ThingsSystempropertydynamiccrossref.schema, ThingsSystempropertystaticcrossref.schema, ThingsSystemtypecrossref.schema, ThingsThing.schema, ThingsThingpersoncrossref.schema, ThingsThingtothingcrossref.schema, UserAccessToken.schema, UserUser.schema).reduceLeft(_ ++ _)
  @deprecated("Use .schema instead of .ddl", "3.0")
  def ddl = schema

  /** Entity class storing rows of table BundleContext
   *  @param id Database column id SqlType(serial), AutoInc, PrimaryKey
   *  @param dateCreated Database column date_created SqlType(timestamp)
   *  @param lastUpdated Database column last_updated SqlType(timestamp)
   *  @param name Database column name SqlType(varchar) */
  case class BundleContextRow(id: Int, dateCreated: org.joda.time.LocalDateTime, lastUpdated: org.joda.time.LocalDateTime, name: String)
  /** GetResult implicit for fetching BundleContextRow objects using plain SQL queries */
  implicit def GetResultBundleContextRow(implicit e0: GR[Int], e1: GR[org.joda.time.LocalDateTime], e2: GR[String]): GR[BundleContextRow] = GR{
    prs => import prs._
    BundleContextRow.tupled((<<[Int], <<[org.joda.time.LocalDateTime], <<[org.joda.time.LocalDateTime], <<[String]))
  }
  /** Table description of table bundle_context. Objects of this class serve as prototypes for rows in queries. */
  class BundleContext(_tableTag: Tag) extends Table[BundleContextRow](_tableTag, Some("hat"), "bundle_context") {
    def * = (id, dateCreated, lastUpdated, name) <> (BundleContextRow.tupled, BundleContextRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(dateCreated), Rep.Some(lastUpdated), Rep.Some(name)).shaped.<>({r=>import r._; _1.map(_=> BundleContextRow.tupled((_1.get, _2.get, _3.get, _4.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(serial), AutoInc, PrimaryKey */
    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    /** Database column date_created SqlType(timestamp) */
    val dateCreated: Rep[org.joda.time.LocalDateTime] = column[org.joda.time.LocalDateTime]("date_created")
    /** Database column last_updated SqlType(timestamp) */
    val lastUpdated: Rep[org.joda.time.LocalDateTime] = column[org.joda.time.LocalDateTime]("last_updated")
    /** Database column name SqlType(varchar) */
    val name: Rep[String] = column[String]("name")
  }
  /** Collection-like TableQuery object for table BundleContext */
  lazy val BundleContext = new TableQuery(tag => new BundleContext(tag))

  /** Entity class storing rows of table BundleContextEntitySelection
   *  @param id Database column id SqlType(serial), AutoInc, PrimaryKey
   *  @param bundleContextId Database column bundle_context_id SqlType(int4)
   *  @param dateCreated Database column date_created SqlType(timestamp)
   *  @param lastUpdated Database column last_updated SqlType(timestamp)
   *  @param entityName Database column entity_name SqlType(varchar), Length(100,true), Default(None)
   *  @param entityId Database column entity_id SqlType(int4), Default(None)
   *  @param entityKind Database column entity_kind SqlType(varchar), Length(100,true), Default(None) */
  case class BundleContextEntitySelectionRow(id: Int, bundleContextId: Int, dateCreated: org.joda.time.LocalDateTime, lastUpdated: org.joda.time.LocalDateTime, entityName: Option[String] = None, entityId: Option[Int] = None, entityKind: Option[String] = None)
  /** GetResult implicit for fetching BundleContextEntitySelectionRow objects using plain SQL queries */
  implicit def GetResultBundleContextEntitySelectionRow(implicit e0: GR[Int], e1: GR[org.joda.time.LocalDateTime], e2: GR[Option[String]], e3: GR[Option[Int]]): GR[BundleContextEntitySelectionRow] = GR{
    prs => import prs._
    BundleContextEntitySelectionRow.tupled((<<[Int], <<[Int], <<[org.joda.time.LocalDateTime], <<[org.joda.time.LocalDateTime], <<?[String], <<?[Int], <<?[String]))
  }
  /** Table description of table bundle_context_entity_selection. Objects of this class serve as prototypes for rows in queries. */
  class BundleContextEntitySelection(_tableTag: Tag) extends Table[BundleContextEntitySelectionRow](_tableTag, Some("hat"), "bundle_context_entity_selection") {
    def * = (id, bundleContextId, dateCreated, lastUpdated, entityName, entityId, entityKind) <> (BundleContextEntitySelectionRow.tupled, BundleContextEntitySelectionRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(bundleContextId), Rep.Some(dateCreated), Rep.Some(lastUpdated), entityName, entityId, entityKind).shaped.<>({r=>import r._; _1.map(_=> BundleContextEntitySelectionRow.tupled((_1.get, _2.get, _3.get, _4.get, _5, _6, _7)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(serial), AutoInc, PrimaryKey */
    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    /** Database column bundle_context_id SqlType(int4) */
    val bundleContextId: Rep[Int] = column[Int]("bundle_context_id")
    /** Database column date_created SqlType(timestamp) */
    val dateCreated: Rep[org.joda.time.LocalDateTime] = column[org.joda.time.LocalDateTime]("date_created")
    /** Database column last_updated SqlType(timestamp) */
    val lastUpdated: Rep[org.joda.time.LocalDateTime] = column[org.joda.time.LocalDateTime]("last_updated")
    /** Database column entity_name SqlType(varchar), Length(100,true), Default(None) */
    val entityName: Rep[Option[String]] = column[Option[String]]("entity_name", O.Length(100,varying=true), O.Default(None))
    /** Database column entity_id SqlType(int4), Default(None) */
    val entityId: Rep[Option[Int]] = column[Option[Int]]("entity_id", O.Default(None))
    /** Database column entity_kind SqlType(varchar), Length(100,true), Default(None) */
    val entityKind: Rep[Option[String]] = column[Option[String]]("entity_kind", O.Length(100,varying=true), O.Default(None))

    /** Foreign key referencing BundleContext (database name entity_selection_bundle_context_fk) */
    lazy val bundleContextFk = foreignKey("entity_selection_bundle_context_fk", bundleContextId, BundleContext)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
    /** Foreign key referencing Entity (database name entity_entity_selection_fk) */
    lazy val entityFk = foreignKey("entity_entity_selection_fk", entityId, Entity)(r => Rep.Some(r.id), onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
  }
  /** Collection-like TableQuery object for table BundleContextEntitySelection */
  lazy val BundleContextEntitySelection = new TableQuery(tag => new BundleContextEntitySelection(tag))

  /** Entity class storing rows of table BundleContextless
   *  @param id Database column id SqlType(serial), AutoInc, PrimaryKey
   *  @param name Database column name SqlType(varchar)
   *  @param dateCreated Database column date_created SqlType(timestamp)
   *  @param lastUpdated Database column last_updated SqlType(timestamp) */
  case class BundleContextlessRow(id: Int, name: String, dateCreated: org.joda.time.LocalDateTime, lastUpdated: org.joda.time.LocalDateTime)
  /** GetResult implicit for fetching BundleContextlessRow objects using plain SQL queries */
  implicit def GetResultBundleContextlessRow(implicit e0: GR[Int], e1: GR[String], e2: GR[org.joda.time.LocalDateTime]): GR[BundleContextlessRow] = GR{
    prs => import prs._
    BundleContextlessRow.tupled((<<[Int], <<[String], <<[org.joda.time.LocalDateTime], <<[org.joda.time.LocalDateTime]))
  }
  /** Table description of table bundle_contextless. Objects of this class serve as prototypes for rows in queries. */
  class BundleContextless(_tableTag: Tag) extends Table[BundleContextlessRow](_tableTag, Some("hat"), "bundle_contextless") {
    def * = (id, name, dateCreated, lastUpdated) <> (BundleContextlessRow.tupled, BundleContextlessRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(name), Rep.Some(dateCreated), Rep.Some(lastUpdated)).shaped.<>({r=>import r._; _1.map(_=> BundleContextlessRow.tupled((_1.get, _2.get, _3.get, _4.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(serial), AutoInc, PrimaryKey */
    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    /** Database column name SqlType(varchar) */
    val name: Rep[String] = column[String]("name")
    /** Database column date_created SqlType(timestamp) */
    val dateCreated: Rep[org.joda.time.LocalDateTime] = column[org.joda.time.LocalDateTime]("date_created")
    /** Database column last_updated SqlType(timestamp) */
    val lastUpdated: Rep[org.joda.time.LocalDateTime] = column[org.joda.time.LocalDateTime]("last_updated")
  }
  /** Collection-like TableQuery object for table BundleContextless */
  lazy val BundleContextless = new TableQuery(tag => new BundleContextless(tag))

  /** Entity class storing rows of table BundleContextlessDataSourceDataset
   *  @param id Database column id SqlType(serial), AutoInc, PrimaryKey
   *  @param bundleId Database column bundle_id SqlType(int4)
   *  @param sourceName Database column source_name SqlType(varchar)
   *  @param datasetName Database column dataset_name SqlType(varchar)
   *  @param datasetTableId Database column dataset_table_id SqlType(int4)
   *  @param description Database column description SqlType(varchar)
   *  @param fieldStructure Database column field_structure SqlType(varchar)
   *  @param fieldIds Database column field_ids SqlType(_int4), Length(10,false) */
  case class BundleContextlessDataSourceDatasetRow(id: Int, bundleId: Int, sourceName: String, datasetName: String, datasetTableId: Int, description: String, fieldStructure: String, fieldIds: List[Int])
  /** GetResult implicit for fetching BundleContextlessDataSourceDatasetRow objects using plain SQL queries */
  implicit def GetResultBundleContextlessDataSourceDatasetRow(implicit e0: GR[Int], e1: GR[String], e2: GR[List[Int]]): GR[BundleContextlessDataSourceDatasetRow] = GR{
    prs => import prs._
    BundleContextlessDataSourceDatasetRow.tupled((<<[Int], <<[Int], <<[String], <<[String], <<[Int], <<[String], <<[String], <<[List[Int]]))
  }
  /** Table description of table bundle_contextless_data_source_dataset. Objects of this class serve as prototypes for rows in queries. */
  class BundleContextlessDataSourceDataset(_tableTag: Tag) extends Table[BundleContextlessDataSourceDatasetRow](_tableTag, Some("hat"), "bundle_contextless_data_source_dataset") {
    def * = (id, bundleId, sourceName, datasetName, datasetTableId, description, fieldStructure, fieldIds) <> (BundleContextlessDataSourceDatasetRow.tupled, BundleContextlessDataSourceDatasetRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(bundleId), Rep.Some(sourceName), Rep.Some(datasetName), Rep.Some(datasetTableId), Rep.Some(description), Rep.Some(fieldStructure), Rep.Some(fieldIds)).shaped.<>({r=>import r._; _1.map(_=> BundleContextlessDataSourceDatasetRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get, _7.get, _8.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(serial), AutoInc, PrimaryKey */
    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    /** Database column bundle_id SqlType(int4) */
    val bundleId: Rep[Int] = column[Int]("bundle_id")
    /** Database column source_name SqlType(varchar) */
    val sourceName: Rep[String] = column[String]("source_name")
    /** Database column dataset_name SqlType(varchar) */
    val datasetName: Rep[String] = column[String]("dataset_name")
    /** Database column dataset_table_id SqlType(int4) */
    val datasetTableId: Rep[Int] = column[Int]("dataset_table_id")
    /** Database column description SqlType(varchar) */
    val description: Rep[String] = column[String]("description")
    /** Database column field_structure SqlType(varchar) */
    val fieldStructure: Rep[String] = column[String]("field_structure")
    /** Database column field_ids SqlType(_int4), Length(10,false) */
    val fieldIds: Rep[List[Int]] = column[List[Int]]("field_ids", O.Length(10,varying=false))

    /** Foreign key referencing BundleContextless (database name bundle_contextless_data_source_dataset_bundle_id_fkey) */
    lazy val bundleContextlessFk = foreignKey("bundle_contextless_data_source_dataset_bundle_id_fkey", bundleId, BundleContextless)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
    /** Foreign key referencing DataTable (database name bundle_contextless_data_source_dataset_dataset_table_id_fkey) */
    lazy val dataTableFk = foreignKey("bundle_contextless_data_source_dataset_dataset_table_id_fkey", datasetTableId, DataTable)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
  }
  /** Collection-like TableQuery object for table BundleContextlessDataSourceDataset */
  lazy val BundleContextlessDataSourceDataset = new TableQuery(tag => new BundleContextlessDataSourceDataset(tag))

  /** Entity class storing rows of table BundleContextPropertySelection
   *  @param propertySelectionId Database column property_selection_id SqlType(serial), AutoInc, PrimaryKey
   *  @param bundleContextEntitySelectionId Database column bundle_context_entity_selection_id SqlType(int4)
   *  @param dateCreated Database column date_created SqlType(timestamp)
   *  @param lastUpdated Database column last_updated SqlType(timestamp)
   *  @param propertyRelationshipKind Database column property_relationship_kind SqlType(varchar), Length(7,true), Default(None)
   *  @param propertyRelationshipId Database column property_relationship_id SqlType(int4), Default(None)
   *  @param propertyName Database column property_name SqlType(varchar), Default(None)
   *  @param propertyType Database column property_type SqlType(varchar), Default(None)
   *  @param propertyUnitofmeasurement Database column property_unitofmeasurement SqlType(varchar), Default(None) */
  case class BundleContextPropertySelectionRow(propertySelectionId: Int, bundleContextEntitySelectionId: Int, dateCreated: org.joda.time.LocalDateTime, lastUpdated: org.joda.time.LocalDateTime, propertyRelationshipKind: Option[String] = None, propertyRelationshipId: Option[Int] = None, propertyName: Option[String] = None, propertyType: Option[String] = None, propertyUnitofmeasurement: Option[String] = None)
  /** GetResult implicit for fetching BundleContextPropertySelectionRow objects using plain SQL queries */
  implicit def GetResultBundleContextPropertySelectionRow(implicit e0: GR[Int], e1: GR[org.joda.time.LocalDateTime], e2: GR[Option[String]], e3: GR[Option[Int]]): GR[BundleContextPropertySelectionRow] = GR{
    prs => import prs._
    BundleContextPropertySelectionRow.tupled((<<[Int], <<[Int], <<[org.joda.time.LocalDateTime], <<[org.joda.time.LocalDateTime], <<?[String], <<?[Int], <<?[String], <<?[String], <<?[String]))
  }
  /** Table description of table bundle_context_property_selection. Objects of this class serve as prototypes for rows in queries. */
  class BundleContextPropertySelection(_tableTag: Tag) extends Table[BundleContextPropertySelectionRow](_tableTag, Some("hat"), "bundle_context_property_selection") {
    def * = (propertySelectionId, bundleContextEntitySelectionId, dateCreated, lastUpdated, propertyRelationshipKind, propertyRelationshipId, propertyName, propertyType, propertyUnitofmeasurement) <> (BundleContextPropertySelectionRow.tupled, BundleContextPropertySelectionRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(propertySelectionId), Rep.Some(bundleContextEntitySelectionId), Rep.Some(dateCreated), Rep.Some(lastUpdated), propertyRelationshipKind, propertyRelationshipId, propertyName, propertyType, propertyUnitofmeasurement).shaped.<>({r=>import r._; _1.map(_=> BundleContextPropertySelectionRow.tupled((_1.get, _2.get, _3.get, _4.get, _5, _6, _7, _8, _9)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column property_selection_id SqlType(serial), AutoInc, PrimaryKey */
    val propertySelectionId: Rep[Int] = column[Int]("property_selection_id", O.AutoInc, O.PrimaryKey)
    /** Database column bundle_context_entity_selection_id SqlType(int4) */
    val bundleContextEntitySelectionId: Rep[Int] = column[Int]("bundle_context_entity_selection_id")
    /** Database column date_created SqlType(timestamp) */
    val dateCreated: Rep[org.joda.time.LocalDateTime] = column[org.joda.time.LocalDateTime]("date_created")
    /** Database column last_updated SqlType(timestamp) */
    val lastUpdated: Rep[org.joda.time.LocalDateTime] = column[org.joda.time.LocalDateTime]("last_updated")
    /** Database column property_relationship_kind SqlType(varchar), Length(7,true), Default(None) */
    val propertyRelationshipKind: Rep[Option[String]] = column[Option[String]]("property_relationship_kind", O.Length(7,varying=true), O.Default(None))
    /** Database column property_relationship_id SqlType(int4), Default(None) */
    val propertyRelationshipId: Rep[Option[Int]] = column[Option[Int]]("property_relationship_id", O.Default(None))
    /** Database column property_name SqlType(varchar), Default(None) */
    val propertyName: Rep[Option[String]] = column[Option[String]]("property_name", O.Default(None))
    /** Database column property_type SqlType(varchar), Default(None) */
    val propertyType: Rep[Option[String]] = column[Option[String]]("property_type", O.Default(None))
    /** Database column property_unitofmeasurement SqlType(varchar), Default(None) */
    val propertyUnitofmeasurement: Rep[Option[String]] = column[Option[String]]("property_unitofmeasurement", O.Default(None))

    /** Foreign key referencing BundleContextEntitySelection (database name property_selection_entity_selection__fk) */
    lazy val bundleContextEntitySelectionFk = foreignKey("property_selection_entity_selection__fk", bundleContextEntitySelectionId, BundleContextEntitySelection)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
  }
  /** Collection-like TableQuery object for table BundleContextPropertySelection */
  lazy val BundleContextPropertySelection = new TableQuery(tag => new BundleContextPropertySelection(tag))

  /** Entity class storing rows of table BundleContextToBundleCrossref
   *  @param id Database column id SqlType(serial), AutoInc, PrimaryKey
   *  @param dateCreated Database column date_created SqlType(timestamp)
   *  @param lastUpdated Database column last_updated SqlType(timestamp)
   *  @param bundleParent Database column bundle_parent SqlType(int4)
   *  @param bundleChild Database column bundle_child SqlType(int4) */
  case class BundleContextToBundleCrossrefRow(id: Int, dateCreated: org.joda.time.LocalDateTime, lastUpdated: org.joda.time.LocalDateTime, bundleParent: Int, bundleChild: Int)
  /** GetResult implicit for fetching BundleContextToBundleCrossrefRow objects using plain SQL queries */
  implicit def GetResultBundleContextToBundleCrossrefRow(implicit e0: GR[Int], e1: GR[org.joda.time.LocalDateTime]): GR[BundleContextToBundleCrossrefRow] = GR{
    prs => import prs._
    BundleContextToBundleCrossrefRow.tupled((<<[Int], <<[org.joda.time.LocalDateTime], <<[org.joda.time.LocalDateTime], <<[Int], <<[Int]))
  }
  /** Table description of table bundle_context_to_bundle_crossref. Objects of this class serve as prototypes for rows in queries. */
  class BundleContextToBundleCrossref(_tableTag: Tag) extends Table[BundleContextToBundleCrossrefRow](_tableTag, Some("hat"), "bundle_context_to_bundle_crossref") {
    def * = (id, dateCreated, lastUpdated, bundleParent, bundleChild) <> (BundleContextToBundleCrossrefRow.tupled, BundleContextToBundleCrossrefRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(dateCreated), Rep.Some(lastUpdated), Rep.Some(bundleParent), Rep.Some(bundleChild)).shaped.<>({r=>import r._; _1.map(_=> BundleContextToBundleCrossrefRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(serial), AutoInc, PrimaryKey */
    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    /** Database column date_created SqlType(timestamp) */
    val dateCreated: Rep[org.joda.time.LocalDateTime] = column[org.joda.time.LocalDateTime]("date_created")
    /** Database column last_updated SqlType(timestamp) */
    val lastUpdated: Rep[org.joda.time.LocalDateTime] = column[org.joda.time.LocalDateTime]("last_updated")
    /** Database column bundle_parent SqlType(int4) */
    val bundleParent: Rep[Int] = column[Int]("bundle_parent")
    /** Database column bundle_child SqlType(int4) */
    val bundleChild: Rep[Int] = column[Int]("bundle_child")

    /** Foreign key referencing BundleContext (database name bundle_context_bundle_bundletobundlecrossref_fk) */
    lazy val bundleContextFk1 = foreignKey("bundle_context_bundle_bundletobundlecrossref_fk", bundleParent, BundleContext)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
    /** Foreign key referencing BundleContext (database name bundle_context_bundle_bundletobundlecrossref_fk1) */
    lazy val bundleContextFk2 = foreignKey("bundle_context_bundle_bundletobundlecrossref_fk1", bundleChild, BundleContext)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
  }
  /** Collection-like TableQuery object for table BundleContextToBundleCrossref */
  lazy val BundleContextToBundleCrossref = new TableQuery(tag => new BundleContextToBundleCrossref(tag))

  /** Entity class storing rows of table BundleContextTree
   *  @param id Database column id SqlType(int4), Default(None)
   *  @param dateCreated Database column date_created SqlType(timestamp), Default(None)
   *  @param lastUpdated Database column last_updated SqlType(timestamp), Default(None)
   *  @param name Database column name SqlType(varchar), Default(None)
   *  @param bundleParent Database column bundle_parent SqlType(int4), Default(None)
   *  @param path Database column path SqlType(_int4), Length(10,false), Default(None)
   *  @param rootBundle Database column root_bundle SqlType(int4), Default(None) */
  case class BundleContextTreeRow(id: Option[Int] = None, dateCreated: Option[org.joda.time.LocalDateTime] = None, lastUpdated: Option[org.joda.time.LocalDateTime] = None, name: Option[String] = None, bundleParent: Option[Int] = None, path: Option[List[Int]] = None, rootBundle: Option[Int] = None)
  /** GetResult implicit for fetching BundleContextTreeRow objects using plain SQL queries */
  implicit def GetResultBundleContextTreeRow(implicit e0: GR[Option[Int]], e1: GR[Option[org.joda.time.LocalDateTime]], e2: GR[Option[String]], e3: GR[Option[List[Int]]]): GR[BundleContextTreeRow] = GR{
    prs => import prs._
    BundleContextTreeRow.tupled((<<?[Int], <<?[org.joda.time.LocalDateTime], <<?[org.joda.time.LocalDateTime], <<?[String], <<?[Int], <<?[List[Int]], <<?[Int]))
  }
  /** Table description of table bundle_context_tree. Objects of this class serve as prototypes for rows in queries. */
  class BundleContextTree(_tableTag: Tag) extends Table[BundleContextTreeRow](_tableTag, Some("hat"), "bundle_context_tree") {
    def * = (id, dateCreated, lastUpdated, name, bundleParent, path, rootBundle) <> (BundleContextTreeRow.tupled, BundleContextTreeRow.unapply)

    /** Database column id SqlType(int4), Default(None) */
    val id: Rep[Option[Int]] = column[Option[Int]]("id", O.Default(None))
    /** Database column date_created SqlType(timestamp), Default(None) */
    val dateCreated: Rep[Option[org.joda.time.LocalDateTime]] = column[Option[org.joda.time.LocalDateTime]]("date_created", O.Default(None))
    /** Database column last_updated SqlType(timestamp), Default(None) */
    val lastUpdated: Rep[Option[org.joda.time.LocalDateTime]] = column[Option[org.joda.time.LocalDateTime]]("last_updated", O.Default(None))
    /** Database column name SqlType(varchar), Default(None) */
    val name: Rep[Option[String]] = column[Option[String]]("name", O.Default(None))
    /** Database column bundle_parent SqlType(int4), Default(None) */
    val bundleParent: Rep[Option[Int]] = column[Option[Int]]("bundle_parent", O.Default(None))
    /** Database column path SqlType(_int4), Length(10,false), Default(None) */
    val path: Rep[Option[List[Int]]] = column[Option[List[Int]]]("path", O.Length(10,varying=false), O.Default(None))
    /** Database column root_bundle SqlType(int4), Default(None) */
    val rootBundle: Rep[Option[Int]] = column[Option[Int]]("root_bundle", O.Default(None))
  }
  /** Collection-like TableQuery object for table BundleContextTree */
  lazy val BundleContextTree = new TableQuery(tag => new BundleContextTree(tag))

  /** Entity class storing rows of table DataDebit
   *  @param dataDebitKey Database column data_debit_key SqlType(uuid), PrimaryKey
   *  @param dateCreated Database column date_created SqlType(timestamp)
   *  @param lastUpdated Database column last_updated SqlType(timestamp)
   *  @param name Database column name SqlType(varchar)
   *  @param startDate Database column start_date SqlType(timestamp)
   *  @param endDate Database column end_date SqlType(timestamp)
   *  @param rolling Database column rolling SqlType(bool)
   *  @param sellRent Database column sell_rent SqlType(bool)
   *  @param price Database column price SqlType(float4)
   *  @param enabled Database column enabled SqlType(bool)
   *  @param senderId Database column sender_id SqlType(varchar)
   *  @param recipientId Database column recipient_id SqlType(varchar)
   *  @param bundleContextlessId Database column bundle_contextless_id SqlType(int4), Default(None)
   *  @param bundleContextId Database column bundle_context_id SqlType(int4), Default(None)
   *  @param kind Database column kind SqlType(varchar) */
  case class DataDebitRow(dataDebitKey: java.util.UUID, dateCreated: org.joda.time.LocalDateTime, lastUpdated: org.joda.time.LocalDateTime, name: String, startDate: org.joda.time.LocalDateTime, endDate: org.joda.time.LocalDateTime, rolling: Boolean, sellRent: Boolean, price: Float, enabled: Boolean, senderId: String, recipientId: String, bundleContextlessId: Option[Int] = None, bundleContextId: Option[Int] = None, kind: String)
  /** GetResult implicit for fetching DataDebitRow objects using plain SQL queries */
  implicit def GetResultDataDebitRow(implicit e0: GR[java.util.UUID], e1: GR[org.joda.time.LocalDateTime], e2: GR[String], e3: GR[Boolean], e4: GR[Float], e5: GR[Option[Int]]): GR[DataDebitRow] = GR{
    prs => import prs._
    DataDebitRow.tupled((<<[java.util.UUID], <<[org.joda.time.LocalDateTime], <<[org.joda.time.LocalDateTime], <<[String], <<[org.joda.time.LocalDateTime], <<[org.joda.time.LocalDateTime], <<[Boolean], <<[Boolean], <<[Float], <<[Boolean], <<[String], <<[String], <<?[Int], <<?[Int], <<[String]))
  }
  /** Table description of table data_debit. Objects of this class serve as prototypes for rows in queries. */
  class DataDebit(_tableTag: Tag) extends Table[DataDebitRow](_tableTag, Some("hat"), "data_debit") {
    def * = (dataDebitKey, dateCreated, lastUpdated, name, startDate, endDate, rolling, sellRent, price, enabled, senderId, recipientId, bundleContextlessId, bundleContextId, kind) <> (DataDebitRow.tupled, DataDebitRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(dataDebitKey), Rep.Some(dateCreated), Rep.Some(lastUpdated), Rep.Some(name), Rep.Some(startDate), Rep.Some(endDate), Rep.Some(rolling), Rep.Some(sellRent), Rep.Some(price), Rep.Some(enabled), Rep.Some(senderId), Rep.Some(recipientId), bundleContextlessId, bundleContextId, Rep.Some(kind)).shaped.<>({r=>import r._; _1.map(_=> DataDebitRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get, _7.get, _8.get, _9.get, _10.get, _11.get, _12.get, _13, _14, _15.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column data_debit_key SqlType(uuid), PrimaryKey */
    val dataDebitKey: Rep[java.util.UUID] = column[java.util.UUID]("data_debit_key", O.PrimaryKey)
    /** Database column date_created SqlType(timestamp) */
    val dateCreated: Rep[org.joda.time.LocalDateTime] = column[org.joda.time.LocalDateTime]("date_created")
    /** Database column last_updated SqlType(timestamp) */
    val lastUpdated: Rep[org.joda.time.LocalDateTime] = column[org.joda.time.LocalDateTime]("last_updated")
    /** Database column name SqlType(varchar) */
    val name: Rep[String] = column[String]("name")
    /** Database column start_date SqlType(timestamp) */
    val startDate: Rep[org.joda.time.LocalDateTime] = column[org.joda.time.LocalDateTime]("start_date")
    /** Database column end_date SqlType(timestamp) */
    val endDate: Rep[org.joda.time.LocalDateTime] = column[org.joda.time.LocalDateTime]("end_date")
    /** Database column rolling SqlType(bool) */
    val rolling: Rep[Boolean] = column[Boolean]("rolling")
    /** Database column sell_rent SqlType(bool) */
    val sellRent: Rep[Boolean] = column[Boolean]("sell_rent")
    /** Database column price SqlType(float4) */
    val price: Rep[Float] = column[Float]("price")
    /** Database column enabled SqlType(bool) */
    val enabled: Rep[Boolean] = column[Boolean]("enabled")
    /** Database column sender_id SqlType(varchar) */
    val senderId: Rep[String] = column[String]("sender_id")
    /** Database column recipient_id SqlType(varchar) */
    val recipientId: Rep[String] = column[String]("recipient_id")
    /** Database column bundle_contextless_id SqlType(int4), Default(None) */
    val bundleContextlessId: Rep[Option[Int]] = column[Option[Int]]("bundle_contextless_id", O.Default(None))
    /** Database column bundle_context_id SqlType(int4), Default(None) */
    val bundleContextId: Rep[Option[Int]] = column[Option[Int]]("bundle_context_id", O.Default(None))
    /** Database column kind SqlType(varchar) */
    val kind: Rep[String] = column[String]("kind")

    /** Foreign key referencing BundleContext (database name bundle_context_data_debit_fk) */
    lazy val bundleContextFk = foreignKey("bundle_context_data_debit_fk", bundleContextId, BundleContext)(r => Rep.Some(r.id), onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
    /** Foreign key referencing BundleContextless (database name bundle_contextless_data_debit_fk) */
    lazy val bundleContextlessFk = foreignKey("bundle_contextless_data_debit_fk", bundleContextlessId, BundleContextless)(r => Rep.Some(r.id), onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
  }
  /** Collection-like TableQuery object for table DataDebit */
  lazy val DataDebit = new TableQuery(tag => new DataDebit(tag))

  /** Entity class storing rows of table DataField
   *  @param id Database column id SqlType(serial), AutoInc, PrimaryKey
   *  @param dateCreated Database column date_created SqlType(timestamp)
   *  @param lastUpdated Database column last_updated SqlType(timestamp)
   *  @param name Database column name SqlType(varchar)
   *  @param tableIdFk Database column table_id_fk SqlType(int4) */
  case class DataFieldRow(id: Int, dateCreated: org.joda.time.LocalDateTime, lastUpdated: org.joda.time.LocalDateTime, name: String, tableIdFk: Int)
  /** GetResult implicit for fetching DataFieldRow objects using plain SQL queries */
  implicit def GetResultDataFieldRow(implicit e0: GR[Int], e1: GR[org.joda.time.LocalDateTime], e2: GR[String]): GR[DataFieldRow] = GR{
    prs => import prs._
    DataFieldRow.tupled((<<[Int], <<[org.joda.time.LocalDateTime], <<[org.joda.time.LocalDateTime], <<[String], <<[Int]))
  }
  /** Table description of table data_field. Objects of this class serve as prototypes for rows in queries. */
  class DataField(_tableTag: Tag) extends Table[DataFieldRow](_tableTag, Some("hat"), "data_field") {
    def * = (id, dateCreated, lastUpdated, name, tableIdFk) <> (DataFieldRow.tupled, DataFieldRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(dateCreated), Rep.Some(lastUpdated), Rep.Some(name), Rep.Some(tableIdFk)).shaped.<>({r=>import r._; _1.map(_=> DataFieldRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(serial), AutoInc, PrimaryKey */
    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    /** Database column date_created SqlType(timestamp) */
    val dateCreated: Rep[org.joda.time.LocalDateTime] = column[org.joda.time.LocalDateTime]("date_created")
    /** Database column last_updated SqlType(timestamp) */
    val lastUpdated: Rep[org.joda.time.LocalDateTime] = column[org.joda.time.LocalDateTime]("last_updated")
    /** Database column name SqlType(varchar) */
    val name: Rep[String] = column[String]("name")
    /** Database column table_id_fk SqlType(int4) */
    val tableIdFk: Rep[Int] = column[Int]("table_id_fk")

    /** Foreign key referencing DataTable (database name data_table_fk) */
    lazy val dataTableFk = foreignKey("data_table_fk", tableIdFk, DataTable)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
  }
  /** Collection-like TableQuery object for table DataField */
  lazy val DataField = new TableQuery(tag => new DataField(tag))

  /** Entity class storing rows of table DataRecord
   *  @param id Database column id SqlType(serial), AutoInc, PrimaryKey
   *  @param dateCreated Database column date_created SqlType(timestamp)
   *  @param lastUpdated Database column last_updated SqlType(timestamp)
   *  @param name Database column name SqlType(varchar) */
  case class DataRecordRow(id: Int, dateCreated: org.joda.time.LocalDateTime, lastUpdated: org.joda.time.LocalDateTime, name: String)
  /** GetResult implicit for fetching DataRecordRow objects using plain SQL queries */
  implicit def GetResultDataRecordRow(implicit e0: GR[Int], e1: GR[org.joda.time.LocalDateTime], e2: GR[String]): GR[DataRecordRow] = GR{
    prs => import prs._
    DataRecordRow.tupled((<<[Int], <<[org.joda.time.LocalDateTime], <<[org.joda.time.LocalDateTime], <<[String]))
  }
  /** Table description of table data_record. Objects of this class serve as prototypes for rows in queries. */
  class DataRecord(_tableTag: Tag) extends Table[DataRecordRow](_tableTag, Some("hat"), "data_record") {
    def * = (id, dateCreated, lastUpdated, name) <> (DataRecordRow.tupled, DataRecordRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(dateCreated), Rep.Some(lastUpdated), Rep.Some(name)).shaped.<>({r=>import r._; _1.map(_=> DataRecordRow.tupled((_1.get, _2.get, _3.get, _4.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(serial), AutoInc, PrimaryKey */
    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    /** Database column date_created SqlType(timestamp) */
    val dateCreated: Rep[org.joda.time.LocalDateTime] = column[org.joda.time.LocalDateTime]("date_created")
    /** Database column last_updated SqlType(timestamp) */
    val lastUpdated: Rep[org.joda.time.LocalDateTime] = column[org.joda.time.LocalDateTime]("last_updated")
    /** Database column name SqlType(varchar) */
    val name: Rep[String] = column[String]("name")
  }
  /** Collection-like TableQuery object for table DataRecord */
  lazy val DataRecord = new TableQuery(tag => new DataRecord(tag))

  /** Entity class storing rows of table DataTable
   *  @param id Database column id SqlType(serial), AutoInc, PrimaryKey
   *  @param dateCreated Database column date_created SqlType(timestamp)
   *  @param lastUpdated Database column last_updated SqlType(timestamp)
   *  @param name Database column name SqlType(varchar)
   *  @param sourceName Database column source_name SqlType(varchar) */
  case class DataTableRow(id: Int, dateCreated: org.joda.time.LocalDateTime, lastUpdated: org.joda.time.LocalDateTime, name: String, sourceName: String)
  /** GetResult implicit for fetching DataTableRow objects using plain SQL queries */
  implicit def GetResultDataTableRow(implicit e0: GR[Int], e1: GR[org.joda.time.LocalDateTime], e2: GR[String]): GR[DataTableRow] = GR{
    prs => import prs._
    DataTableRow.tupled((<<[Int], <<[org.joda.time.LocalDateTime], <<[org.joda.time.LocalDateTime], <<[String], <<[String]))
  }
  /** Table description of table data_table. Objects of this class serve as prototypes for rows in queries. */
  class DataTable(_tableTag: Tag) extends Table[DataTableRow](_tableTag, Some("hat"), "data_table") {
    def * = (id, dateCreated, lastUpdated, name, sourceName) <> (DataTableRow.tupled, DataTableRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(dateCreated), Rep.Some(lastUpdated), Rep.Some(name), Rep.Some(sourceName)).shaped.<>({r=>import r._; _1.map(_=> DataTableRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(serial), AutoInc, PrimaryKey */
    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    /** Database column date_created SqlType(timestamp) */
    val dateCreated: Rep[org.joda.time.LocalDateTime] = column[org.joda.time.LocalDateTime]("date_created")
    /** Database column last_updated SqlType(timestamp) */
    val lastUpdated: Rep[org.joda.time.LocalDateTime] = column[org.joda.time.LocalDateTime]("last_updated")
    /** Database column name SqlType(varchar) */
    val name: Rep[String] = column[String]("name")
    /** Database column source_name SqlType(varchar) */
    val sourceName: Rep[String] = column[String]("source_name")

    /** Uniqueness Index over (name,sourceName) (database name data_table_name_source) */
    val index1 = index("data_table_name_source", (name, sourceName), unique=true)
  }
  /** Collection-like TableQuery object for table DataTable */
  lazy val DataTable = new TableQuery(tag => new DataTable(tag))

  /** Entity class storing rows of table DataTabletotablecrossref
   *  @param id Database column id SqlType(serial), AutoInc, PrimaryKey
   *  @param dateCreated Database column date_created SqlType(timestamp)
   *  @param lastUpdated Database column last_updated SqlType(timestamp)
   *  @param relationshipType Database column relationship_type SqlType(varchar)
   *  @param table1 Database column table1 SqlType(int4)
   *  @param table2 Database column table2 SqlType(int4) */
  case class DataTabletotablecrossrefRow(id: Int, dateCreated: org.joda.time.LocalDateTime, lastUpdated: org.joda.time.LocalDateTime, relationshipType: String, table1: Int, table2: Int)
  /** GetResult implicit for fetching DataTabletotablecrossrefRow objects using plain SQL queries */
  implicit def GetResultDataTabletotablecrossrefRow(implicit e0: GR[Int], e1: GR[org.joda.time.LocalDateTime], e2: GR[String]): GR[DataTabletotablecrossrefRow] = GR{
    prs => import prs._
    DataTabletotablecrossrefRow.tupled((<<[Int], <<[org.joda.time.LocalDateTime], <<[org.joda.time.LocalDateTime], <<[String], <<[Int], <<[Int]))
  }
  /** Table description of table data_tabletotablecrossref. Objects of this class serve as prototypes for rows in queries. */
  class DataTabletotablecrossref(_tableTag: Tag) extends Table[DataTabletotablecrossrefRow](_tableTag, Some("hat"), "data_tabletotablecrossref") {
    def * = (id, dateCreated, lastUpdated, relationshipType, table1, table2) <> (DataTabletotablecrossrefRow.tupled, DataTabletotablecrossrefRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(dateCreated), Rep.Some(lastUpdated), Rep.Some(relationshipType), Rep.Some(table1), Rep.Some(table2)).shaped.<>({r=>import r._; _1.map(_=> DataTabletotablecrossrefRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(serial), AutoInc, PrimaryKey */
    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    /** Database column date_created SqlType(timestamp) */
    val dateCreated: Rep[org.joda.time.LocalDateTime] = column[org.joda.time.LocalDateTime]("date_created")
    /** Database column last_updated SqlType(timestamp) */
    val lastUpdated: Rep[org.joda.time.LocalDateTime] = column[org.joda.time.LocalDateTime]("last_updated")
    /** Database column relationship_type SqlType(varchar) */
    val relationshipType: Rep[String] = column[String]("relationship_type")
    /** Database column table1 SqlType(int4) */
    val table1: Rep[Int] = column[Int]("table1")
    /** Database column table2 SqlType(int4) */
    val table2: Rep[Int] = column[Int]("table2")

    /** Foreign key referencing DataTable (database name data_table_data_tabletotablecrossref_fk) */
    lazy val dataTableFk1 = foreignKey("data_table_data_tabletotablecrossref_fk", table2, DataTable)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
    /** Foreign key referencing DataTable (database name data_table_data_tabletotablecrossref_fk1) */
    lazy val dataTableFk2 = foreignKey("data_table_data_tabletotablecrossref_fk1", table1, DataTable)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
  }
  /** Collection-like TableQuery object for table DataTabletotablecrossref */
  lazy val DataTabletotablecrossref = new TableQuery(tag => new DataTabletotablecrossref(tag))

  /** Entity class storing rows of table DataTableTree
   *  @param id Database column id SqlType(int4), Default(None)
   *  @param dateCreated Database column date_created SqlType(timestamp), Default(None)
   *  @param lastUpdated Database column last_updated SqlType(timestamp), Default(None)
   *  @param name Database column name SqlType(varchar), Default(None)
   *  @param sourceName Database column source_name SqlType(varchar), Default(None)
   *  @param table1 Database column table1 SqlType(int4), Default(None)
   *  @param path Database column path SqlType(_int4), Length(10,false), Default(None)
   *  @param rootTable Database column root_table SqlType(int4), Default(None) */
  case class DataTableTreeRow(id: Option[Int] = None, dateCreated: Option[org.joda.time.LocalDateTime] = None, lastUpdated: Option[org.joda.time.LocalDateTime] = None, name: Option[String] = None, sourceName: Option[String] = None, table1: Option[Int] = None, path: Option[List[Int]] = None, rootTable: Option[Int] = None)
  /** GetResult implicit for fetching DataTableTreeRow objects using plain SQL queries */
  implicit def GetResultDataTableTreeRow(implicit e0: GR[Option[Int]], e1: GR[Option[org.joda.time.LocalDateTime]], e2: GR[Option[String]], e3: GR[Option[List[Int]]]): GR[DataTableTreeRow] = GR{
    prs => import prs._
    DataTableTreeRow.tupled((<<?[Int], <<?[org.joda.time.LocalDateTime], <<?[org.joda.time.LocalDateTime], <<?[String], <<?[String], <<?[Int], <<?[List[Int]], <<?[Int]))
  }
  /** Table description of table data_table_tree. Objects of this class serve as prototypes for rows in queries. */
  class DataTableTree(_tableTag: Tag) extends Table[DataTableTreeRow](_tableTag, Some("hat"), "data_table_tree") {
    def * = (id, dateCreated, lastUpdated, name, sourceName, table1, path, rootTable) <> (DataTableTreeRow.tupled, DataTableTreeRow.unapply)

    /** Database column id SqlType(int4), Default(None) */
    val id: Rep[Option[Int]] = column[Option[Int]]("id", O.Default(None))
    /** Database column date_created SqlType(timestamp), Default(None) */
    val dateCreated: Rep[Option[org.joda.time.LocalDateTime]] = column[Option[org.joda.time.LocalDateTime]]("date_created", O.Default(None))
    /** Database column last_updated SqlType(timestamp), Default(None) */
    val lastUpdated: Rep[Option[org.joda.time.LocalDateTime]] = column[Option[org.joda.time.LocalDateTime]]("last_updated", O.Default(None))
    /** Database column name SqlType(varchar), Default(None) */
    val name: Rep[Option[String]] = column[Option[String]]("name", O.Default(None))
    /** Database column source_name SqlType(varchar), Default(None) */
    val sourceName: Rep[Option[String]] = column[Option[String]]("source_name", O.Default(None))
    /** Database column table1 SqlType(int4), Default(None) */
    val table1: Rep[Option[Int]] = column[Option[Int]]("table1", O.Default(None))
    /** Database column path SqlType(_int4), Length(10,false), Default(None) */
    val path: Rep[Option[List[Int]]] = column[Option[List[Int]]]("path", O.Length(10,varying=false), O.Default(None))
    /** Database column root_table SqlType(int4), Default(None) */
    val rootTable: Rep[Option[Int]] = column[Option[Int]]("root_table", O.Default(None))
  }
  /** Collection-like TableQuery object for table DataTableTree */
  lazy val DataTableTree = new TableQuery(tag => new DataTableTree(tag))

  /** Entity class storing rows of table DataValue
   *  @param id Database column id SqlType(serial), AutoInc, PrimaryKey
   *  @param dateCreated Database column date_created SqlType(timestamp)
   *  @param lastUpdated Database column last_updated SqlType(timestamp)
   *  @param value Database column value SqlType(text)
   *  @param fieldId Database column field_id SqlType(int4)
   *  @param recordId Database column record_id SqlType(int4) */
  case class DataValueRow(id: Int, dateCreated: org.joda.time.LocalDateTime, lastUpdated: org.joda.time.LocalDateTime, value: String, fieldId: Int, recordId: Int)
  /** GetResult implicit for fetching DataValueRow objects using plain SQL queries */
  implicit def GetResultDataValueRow(implicit e0: GR[Int], e1: GR[org.joda.time.LocalDateTime], e2: GR[String]): GR[DataValueRow] = GR{
    prs => import prs._
    DataValueRow.tupled((<<[Int], <<[org.joda.time.LocalDateTime], <<[org.joda.time.LocalDateTime], <<[String], <<[Int], <<[Int]))
  }
  /** Table description of table data_value. Objects of this class serve as prototypes for rows in queries. */
  class DataValue(_tableTag: Tag) extends Table[DataValueRow](_tableTag, Some("hat"), "data_value") {
    def * = (id, dateCreated, lastUpdated, value, fieldId, recordId) <> (DataValueRow.tupled, DataValueRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(dateCreated), Rep.Some(lastUpdated), Rep.Some(value), Rep.Some(fieldId), Rep.Some(recordId)).shaped.<>({r=>import r._; _1.map(_=> DataValueRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(serial), AutoInc, PrimaryKey */
    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    /** Database column date_created SqlType(timestamp) */
    val dateCreated: Rep[org.joda.time.LocalDateTime] = column[org.joda.time.LocalDateTime]("date_created")
    /** Database column last_updated SqlType(timestamp) */
    val lastUpdated: Rep[org.joda.time.LocalDateTime] = column[org.joda.time.LocalDateTime]("last_updated")
    /** Database column value SqlType(text) */
    val value: Rep[String] = column[String]("value")
    /** Database column field_id SqlType(int4) */
    val fieldId: Rep[Int] = column[Int]("field_id")
    /** Database column record_id SqlType(int4) */
    val recordId: Rep[Int] = column[Int]("record_id")

    /** Foreign key referencing DataField (database name data_field_data_value_fk) */
    lazy val dataFieldFk = foreignKey("data_field_data_value_fk", fieldId, DataField)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
    /** Foreign key referencing DataRecord (database name data_record_data_value_fk) */
    lazy val dataRecordFk = foreignKey("data_record_data_value_fk", recordId, DataRecord)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
  }
  /** Collection-like TableQuery object for table DataValue */
  lazy val DataValue = new TableQuery(tag => new DataValue(tag))

  /** Entity class storing rows of table Entity
   *  @param id Database column id SqlType(int4), PrimaryKey
   *  @param dateCreated Database column date_created SqlType(timestamp)
   *  @param lastUpdated Database column last_updated SqlType(timestamp)
   *  @param name Database column name SqlType(varchar), Length(100,true)
   *  @param kind Database column kind SqlType(varchar), Length(100,true)
   *  @param locationId Database column location_id SqlType(int4), Default(None)
   *  @param thingId Database column thing_id SqlType(int4), Default(None)
   *  @param eventId Database column event_id SqlType(int4), Default(None)
   *  @param organisationId Database column organisation_id SqlType(int4), Default(None)
   *  @param personId Database column person_id SqlType(int4), Default(None) */
  case class EntityRow(id: Int, dateCreated: org.joda.time.LocalDateTime, lastUpdated: org.joda.time.LocalDateTime, name: String, kind: String, locationId: Option[Int] = None, thingId: Option[Int] = None, eventId: Option[Int] = None, organisationId: Option[Int] = None, personId: Option[Int] = None)
  /** GetResult implicit for fetching EntityRow objects using plain SQL queries */
  implicit def GetResultEntityRow(implicit e0: GR[Int], e1: GR[org.joda.time.LocalDateTime], e2: GR[String], e3: GR[Option[Int]]): GR[EntityRow] = GR{
    prs => import prs._
    EntityRow.tupled((<<[Int], <<[org.joda.time.LocalDateTime], <<[org.joda.time.LocalDateTime], <<[String], <<[String], <<?[Int], <<?[Int], <<?[Int], <<?[Int], <<?[Int]))
  }
  /** Table description of table entity. Objects of this class serve as prototypes for rows in queries. */
  class Entity(_tableTag: Tag) extends Table[EntityRow](_tableTag, Some("hat"), "entity") {
    def * = (id, dateCreated, lastUpdated, name, kind, locationId, thingId, eventId, organisationId, personId) <> (EntityRow.tupled, EntityRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(dateCreated), Rep.Some(lastUpdated), Rep.Some(name), Rep.Some(kind), locationId, thingId, eventId, organisationId, personId).shaped.<>({r=>import r._; _1.map(_=> EntityRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6, _7, _8, _9, _10)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(int4), PrimaryKey */
    val id: Rep[Int] = column[Int]("id", O.PrimaryKey)
    /** Database column date_created SqlType(timestamp) */
    val dateCreated: Rep[org.joda.time.LocalDateTime] = column[org.joda.time.LocalDateTime]("date_created")
    /** Database column last_updated SqlType(timestamp) */
    val lastUpdated: Rep[org.joda.time.LocalDateTime] = column[org.joda.time.LocalDateTime]("last_updated")
    /** Database column name SqlType(varchar), Length(100,true) */
    val name: Rep[String] = column[String]("name", O.Length(100,varying=true))
    /** Database column kind SqlType(varchar), Length(100,true) */
    val kind: Rep[String] = column[String]("kind", O.Length(100,varying=true))
    /** Database column location_id SqlType(int4), Default(None) */
    val locationId: Rep[Option[Int]] = column[Option[Int]]("location_id", O.Default(None))
    /** Database column thing_id SqlType(int4), Default(None) */
    val thingId: Rep[Option[Int]] = column[Option[Int]]("thing_id", O.Default(None))
    /** Database column event_id SqlType(int4), Default(None) */
    val eventId: Rep[Option[Int]] = column[Option[Int]]("event_id", O.Default(None))
    /** Database column organisation_id SqlType(int4), Default(None) */
    val organisationId: Rep[Option[Int]] = column[Option[Int]]("organisation_id", O.Default(None))
    /** Database column person_id SqlType(int4), Default(None) */
    val personId: Rep[Option[Int]] = column[Option[Int]]("person_id", O.Default(None))

    /** Foreign key referencing EventsEvent (database name events_event_entity_fk) */
    lazy val eventsEventFk = foreignKey("events_event_entity_fk", eventId, EventsEvent)(r => Rep.Some(r.id), onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
    /** Foreign key referencing LocationsLocation (database name locations_location_entity_fk) */
    lazy val locationsLocationFk = foreignKey("locations_location_entity_fk", locationId, LocationsLocation)(r => Rep.Some(r.id), onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
    /** Foreign key referencing OrganisationsOrganisation (database name organisations_organisation_entity_fk) */
    lazy val organisationsOrganisationFk = foreignKey("organisations_organisation_entity_fk", organisationId, OrganisationsOrganisation)(r => Rep.Some(r.id), onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
    /** Foreign key referencing PeoplePerson (database name people_person_entity_fk) */
    lazy val peoplePersonFk = foreignKey("people_person_entity_fk", personId, PeoplePerson)(r => Rep.Some(r.id), onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
    /** Foreign key referencing ThingsThing (database name things_thing_entity_fk) */
    lazy val thingsThingFk = foreignKey("things_thing_entity_fk", thingId, ThingsThing)(r => Rep.Some(r.id), onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
  }
  /** Collection-like TableQuery object for table Entity */
  lazy val Entity = new TableQuery(tag => new Entity(tag))

  /** Entity class storing rows of table EventsEvent
   *  @param id Database column id SqlType(serial), AutoInc, PrimaryKey
   *  @param dateCreated Database column date_created SqlType(timestamp)
   *  @param lastUpdated Database column last_updated SqlType(timestamp)
   *  @param name Database column name SqlType(varchar), Length(100,true) */
  case class EventsEventRow(id: Int, dateCreated: org.joda.time.LocalDateTime, lastUpdated: org.joda.time.LocalDateTime, name: String)
  /** GetResult implicit for fetching EventsEventRow objects using plain SQL queries */
  implicit def GetResultEventsEventRow(implicit e0: GR[Int], e1: GR[org.joda.time.LocalDateTime], e2: GR[String]): GR[EventsEventRow] = GR{
    prs => import prs._
    EventsEventRow.tupled((<<[Int], <<[org.joda.time.LocalDateTime], <<[org.joda.time.LocalDateTime], <<[String]))
  }
  /** Table description of table events_event. Objects of this class serve as prototypes for rows in queries. */
  class EventsEvent(_tableTag: Tag) extends Table[EventsEventRow](_tableTag, Some("hat"), "events_event") {
    def * = (id, dateCreated, lastUpdated, name) <> (EventsEventRow.tupled, EventsEventRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(dateCreated), Rep.Some(lastUpdated), Rep.Some(name)).shaped.<>({r=>import r._; _1.map(_=> EventsEventRow.tupled((_1.get, _2.get, _3.get, _4.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(serial), AutoInc, PrimaryKey */
    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    /** Database column date_created SqlType(timestamp) */
    val dateCreated: Rep[org.joda.time.LocalDateTime] = column[org.joda.time.LocalDateTime]("date_created")
    /** Database column last_updated SqlType(timestamp) */
    val lastUpdated: Rep[org.joda.time.LocalDateTime] = column[org.joda.time.LocalDateTime]("last_updated")
    /** Database column name SqlType(varchar), Length(100,true) */
    val name: Rep[String] = column[String]("name", O.Length(100,varying=true))
  }
  /** Collection-like TableQuery object for table EventsEvent */
  lazy val EventsEvent = new TableQuery(tag => new EventsEvent(tag))

  /** Entity class storing rows of table EventsEventlocationcrossref
   *  @param id Database column id SqlType(serial), AutoInc, PrimaryKey
   *  @param dateCreated Database column date_created SqlType(timestamp)
   *  @param lastUpdated Database column last_updated SqlType(timestamp)
   *  @param locationId Database column location_id SqlType(int4)
   *  @param eventId Database column event_id SqlType(int4)
   *  @param relationshipType Database column relationship_type SqlType(varchar), Length(100,true)
   *  @param isCurrent Database column is_current SqlType(bool)
   *  @param relationshiprecordId Database column relationshiprecord_id SqlType(int4) */
  case class EventsEventlocationcrossrefRow(id: Int, dateCreated: org.joda.time.LocalDateTime, lastUpdated: org.joda.time.LocalDateTime, locationId: Int, eventId: Int, relationshipType: String, isCurrent: Boolean, relationshiprecordId: Int)
  /** GetResult implicit for fetching EventsEventlocationcrossrefRow objects using plain SQL queries */
  implicit def GetResultEventsEventlocationcrossrefRow(implicit e0: GR[Int], e1: GR[org.joda.time.LocalDateTime], e2: GR[String], e3: GR[Boolean]): GR[EventsEventlocationcrossrefRow] = GR{
    prs => import prs._
    EventsEventlocationcrossrefRow.tupled((<<[Int], <<[org.joda.time.LocalDateTime], <<[org.joda.time.LocalDateTime], <<[Int], <<[Int], <<[String], <<[Boolean], <<[Int]))
  }
  /** Table description of table events_eventlocationcrossref. Objects of this class serve as prototypes for rows in queries. */
  class EventsEventlocationcrossref(_tableTag: Tag) extends Table[EventsEventlocationcrossrefRow](_tableTag, Some("hat"), "events_eventlocationcrossref") {
    def * = (id, dateCreated, lastUpdated, locationId, eventId, relationshipType, isCurrent, relationshiprecordId) <> (EventsEventlocationcrossrefRow.tupled, EventsEventlocationcrossrefRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(dateCreated), Rep.Some(lastUpdated), Rep.Some(locationId), Rep.Some(eventId), Rep.Some(relationshipType), Rep.Some(isCurrent), Rep.Some(relationshiprecordId)).shaped.<>({r=>import r._; _1.map(_=> EventsEventlocationcrossrefRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get, _7.get, _8.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(serial), AutoInc, PrimaryKey */
    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    /** Database column date_created SqlType(timestamp) */
    val dateCreated: Rep[org.joda.time.LocalDateTime] = column[org.joda.time.LocalDateTime]("date_created")
    /** Database column last_updated SqlType(timestamp) */
    val lastUpdated: Rep[org.joda.time.LocalDateTime] = column[org.joda.time.LocalDateTime]("last_updated")
    /** Database column location_id SqlType(int4) */
    val locationId: Rep[Int] = column[Int]("location_id")
    /** Database column event_id SqlType(int4) */
    val eventId: Rep[Int] = column[Int]("event_id")
    /** Database column relationship_type SqlType(varchar), Length(100,true) */
    val relationshipType: Rep[String] = column[String]("relationship_type", O.Length(100,varying=true))
    /** Database column is_current SqlType(bool) */
    val isCurrent: Rep[Boolean] = column[Boolean]("is_current")
    /** Database column relationshiprecord_id SqlType(int4) */
    val relationshiprecordId: Rep[Int] = column[Int]("relationshiprecord_id")

    /** Foreign key referencing EventsEvent (database name events_eventlocationcrossref_fk) */
    lazy val eventsEventFk = foreignKey("events_eventlocationcrossref_fk", eventId, EventsEvent)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
    /** Foreign key referencing LocationsLocation (database name locations_location_events_eventlocationcrossref_fk) */
    lazy val locationsLocationFk = foreignKey("locations_location_events_eventlocationcrossref_fk", locationId, LocationsLocation)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
    /** Foreign key referencing SystemRelationshiprecord (database name system_relationshiprecord_events_eventlocationcrossref_fk) */
    lazy val systemRelationshiprecordFk = foreignKey("system_relationshiprecord_events_eventlocationcrossref_fk", relationshiprecordId, SystemRelationshiprecord)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
  }
  /** Collection-like TableQuery object for table EventsEventlocationcrossref */
  lazy val EventsEventlocationcrossref = new TableQuery(tag => new EventsEventlocationcrossref(tag))

  /** Entity class storing rows of table EventsEventorganisationcrossref
   *  @param id Database column id SqlType(serial), AutoInc, PrimaryKey
   *  @param dateCreated Database column date_created SqlType(timestamp)
   *  @param lastUpdated Database column last_updated SqlType(timestamp)
   *  @param organisationId Database column organisation_id SqlType(int4)
   *  @param eventId Database column event_id SqlType(int4)
   *  @param relationshipType Database column relationship_type SqlType(varchar), Length(100,true)
   *  @param isCurrent Database column is_current SqlType(bool)
   *  @param relationshiprecordId Database column relationshiprecord_id SqlType(int4) */
  case class EventsEventorganisationcrossrefRow(id: Int, dateCreated: org.joda.time.LocalDateTime, lastUpdated: org.joda.time.LocalDateTime, organisationId: Int, eventId: Int, relationshipType: String, isCurrent: Boolean, relationshiprecordId: Int)
  /** GetResult implicit for fetching EventsEventorganisationcrossrefRow objects using plain SQL queries */
  implicit def GetResultEventsEventorganisationcrossrefRow(implicit e0: GR[Int], e1: GR[org.joda.time.LocalDateTime], e2: GR[String], e3: GR[Boolean]): GR[EventsEventorganisationcrossrefRow] = GR{
    prs => import prs._
    EventsEventorganisationcrossrefRow.tupled((<<[Int], <<[org.joda.time.LocalDateTime], <<[org.joda.time.LocalDateTime], <<[Int], <<[Int], <<[String], <<[Boolean], <<[Int]))
  }
  /** Table description of table events_eventorganisationcrossref. Objects of this class serve as prototypes for rows in queries. */
  class EventsEventorganisationcrossref(_tableTag: Tag) extends Table[EventsEventorganisationcrossrefRow](_tableTag, Some("hat"), "events_eventorganisationcrossref") {
    def * = (id, dateCreated, lastUpdated, organisationId, eventId, relationshipType, isCurrent, relationshiprecordId) <> (EventsEventorganisationcrossrefRow.tupled, EventsEventorganisationcrossrefRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(dateCreated), Rep.Some(lastUpdated), Rep.Some(organisationId), Rep.Some(eventId), Rep.Some(relationshipType), Rep.Some(isCurrent), Rep.Some(relationshiprecordId)).shaped.<>({r=>import r._; _1.map(_=> EventsEventorganisationcrossrefRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get, _7.get, _8.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(serial), AutoInc, PrimaryKey */
    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    /** Database column date_created SqlType(timestamp) */
    val dateCreated: Rep[org.joda.time.LocalDateTime] = column[org.joda.time.LocalDateTime]("date_created")
    /** Database column last_updated SqlType(timestamp) */
    val lastUpdated: Rep[org.joda.time.LocalDateTime] = column[org.joda.time.LocalDateTime]("last_updated")
    /** Database column organisation_id SqlType(int4) */
    val organisationId: Rep[Int] = column[Int]("organisation_id")
    /** Database column event_id SqlType(int4) */
    val eventId: Rep[Int] = column[Int]("event_id")
    /** Database column relationship_type SqlType(varchar), Length(100,true) */
    val relationshipType: Rep[String] = column[String]("relationship_type", O.Length(100,varying=true))
    /** Database column is_current SqlType(bool) */
    val isCurrent: Rep[Boolean] = column[Boolean]("is_current")
    /** Database column relationshiprecord_id SqlType(int4) */
    val relationshiprecordId: Rep[Int] = column[Int]("relationshiprecord_id")

    /** Foreign key referencing EventsEvent (database name events_eventorganisationcrossref_fk) */
    lazy val eventsEventFk = foreignKey("events_eventorganisationcrossref_fk", eventId, EventsEvent)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
    /** Foreign key referencing OrganisationsOrganisation (database name organisations_organisation_events_eventorganisationcrossref_fk) */
    lazy val organisationsOrganisationFk = foreignKey("organisations_organisation_events_eventorganisationcrossref_fk", organisationId, OrganisationsOrganisation)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
    /** Foreign key referencing SystemRelationshiprecord (database name system_relationshiprecord_events_eventorganisationcrossref_fk) */
    lazy val systemRelationshiprecordFk = foreignKey("system_relationshiprecord_events_eventorganisationcrossref_fk", relationshiprecordId, SystemRelationshiprecord)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
  }
  /** Collection-like TableQuery object for table EventsEventorganisationcrossref */
  lazy val EventsEventorganisationcrossref = new TableQuery(tag => new EventsEventorganisationcrossref(tag))

  /** Entity class storing rows of table EventsEventpersoncrossref
   *  @param id Database column id SqlType(serial), AutoInc, PrimaryKey
   *  @param dateCreated Database column date_created SqlType(timestamp)
   *  @param lastUpdated Database column last_updated SqlType(timestamp)
   *  @param personId Database column person_id SqlType(int4)
   *  @param eventId Database column event_id SqlType(int4)
   *  @param relationshipType Database column relationship_type SqlType(varchar), Length(100,true)
   *  @param isCurrent Database column is_current SqlType(bool)
   *  @param relationshiprecordId Database column relationshiprecord_id SqlType(int4) */
  case class EventsEventpersoncrossrefRow(id: Int, dateCreated: org.joda.time.LocalDateTime, lastUpdated: org.joda.time.LocalDateTime, personId: Int, eventId: Int, relationshipType: String, isCurrent: Boolean, relationshiprecordId: Int)
  /** GetResult implicit for fetching EventsEventpersoncrossrefRow objects using plain SQL queries */
  implicit def GetResultEventsEventpersoncrossrefRow(implicit e0: GR[Int], e1: GR[org.joda.time.LocalDateTime], e2: GR[String], e3: GR[Boolean]): GR[EventsEventpersoncrossrefRow] = GR{
    prs => import prs._
    EventsEventpersoncrossrefRow.tupled((<<[Int], <<[org.joda.time.LocalDateTime], <<[org.joda.time.LocalDateTime], <<[Int], <<[Int], <<[String], <<[Boolean], <<[Int]))
  }
  /** Table description of table events_eventpersoncrossref. Objects of this class serve as prototypes for rows in queries. */
  class EventsEventpersoncrossref(_tableTag: Tag) extends Table[EventsEventpersoncrossrefRow](_tableTag, Some("hat"), "events_eventpersoncrossref") {
    def * = (id, dateCreated, lastUpdated, personId, eventId, relationshipType, isCurrent, relationshiprecordId) <> (EventsEventpersoncrossrefRow.tupled, EventsEventpersoncrossrefRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(dateCreated), Rep.Some(lastUpdated), Rep.Some(personId), Rep.Some(eventId), Rep.Some(relationshipType), Rep.Some(isCurrent), Rep.Some(relationshiprecordId)).shaped.<>({r=>import r._; _1.map(_=> EventsEventpersoncrossrefRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get, _7.get, _8.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(serial), AutoInc, PrimaryKey */
    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    /** Database column date_created SqlType(timestamp) */
    val dateCreated: Rep[org.joda.time.LocalDateTime] = column[org.joda.time.LocalDateTime]("date_created")
    /** Database column last_updated SqlType(timestamp) */
    val lastUpdated: Rep[org.joda.time.LocalDateTime] = column[org.joda.time.LocalDateTime]("last_updated")
    /** Database column person_id SqlType(int4) */
    val personId: Rep[Int] = column[Int]("person_id")
    /** Database column event_id SqlType(int4) */
    val eventId: Rep[Int] = column[Int]("event_id")
    /** Database column relationship_type SqlType(varchar), Length(100,true) */
    val relationshipType: Rep[String] = column[String]("relationship_type", O.Length(100,varying=true))
    /** Database column is_current SqlType(bool) */
    val isCurrent: Rep[Boolean] = column[Boolean]("is_current")
    /** Database column relationshiprecord_id SqlType(int4) */
    val relationshiprecordId: Rep[Int] = column[Int]("relationshiprecord_id")

    /** Foreign key referencing EventsEvent (database name events_eventpersoncrossref_thing_id_fkey) */
    lazy val eventsEventFk = foreignKey("events_eventpersoncrossref_thing_id_fkey", eventId, EventsEvent)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
    /** Foreign key referencing PeoplePerson (database name people_person_people_eventpersoncrossref_fk) */
    lazy val peoplePersonFk = foreignKey("people_person_people_eventpersoncrossref_fk", personId, PeoplePerson)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
    /** Foreign key referencing SystemRelationshiprecord (database name system_relationshiprecord_events_eventpersoncrossref_fk) */
    lazy val systemRelationshiprecordFk = foreignKey("system_relationshiprecord_events_eventpersoncrossref_fk", relationshiprecordId, SystemRelationshiprecord)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
  }
  /** Collection-like TableQuery object for table EventsEventpersoncrossref */
  lazy val EventsEventpersoncrossref = new TableQuery(tag => new EventsEventpersoncrossref(tag))

  /** Entity class storing rows of table EventsEventthingcrossref
   *  @param id Database column id SqlType(serial), AutoInc, PrimaryKey
   *  @param dateCreated Database column date_created SqlType(timestamp)
   *  @param lastUpdated Database column last_updated SqlType(timestamp)
   *  @param thingId Database column thing_id SqlType(int4)
   *  @param eventId Database column event_id SqlType(int4)
   *  @param relationshipType Database column relationship_type SqlType(varchar), Length(100,true)
   *  @param isCurrent Database column is_current SqlType(bool)
   *  @param relationshiprecordId Database column relationshiprecord_id SqlType(int4) */
  case class EventsEventthingcrossrefRow(id: Int, dateCreated: org.joda.time.LocalDateTime, lastUpdated: org.joda.time.LocalDateTime, thingId: Int, eventId: Int, relationshipType: String, isCurrent: Boolean, relationshiprecordId: Int)
  /** GetResult implicit for fetching EventsEventthingcrossrefRow objects using plain SQL queries */
  implicit def GetResultEventsEventthingcrossrefRow(implicit e0: GR[Int], e1: GR[org.joda.time.LocalDateTime], e2: GR[String], e3: GR[Boolean]): GR[EventsEventthingcrossrefRow] = GR{
    prs => import prs._
    EventsEventthingcrossrefRow.tupled((<<[Int], <<[org.joda.time.LocalDateTime], <<[org.joda.time.LocalDateTime], <<[Int], <<[Int], <<[String], <<[Boolean], <<[Int]))
  }
  /** Table description of table events_eventthingcrossref. Objects of this class serve as prototypes for rows in queries. */
  class EventsEventthingcrossref(_tableTag: Tag) extends Table[EventsEventthingcrossrefRow](_tableTag, Some("hat"), "events_eventthingcrossref") {
    def * = (id, dateCreated, lastUpdated, thingId, eventId, relationshipType, isCurrent, relationshiprecordId) <> (EventsEventthingcrossrefRow.tupled, EventsEventthingcrossrefRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(dateCreated), Rep.Some(lastUpdated), Rep.Some(thingId), Rep.Some(eventId), Rep.Some(relationshipType), Rep.Some(isCurrent), Rep.Some(relationshiprecordId)).shaped.<>({r=>import r._; _1.map(_=> EventsEventthingcrossrefRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get, _7.get, _8.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(serial), AutoInc, PrimaryKey */
    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    /** Database column date_created SqlType(timestamp) */
    val dateCreated: Rep[org.joda.time.LocalDateTime] = column[org.joda.time.LocalDateTime]("date_created")
    /** Database column last_updated SqlType(timestamp) */
    val lastUpdated: Rep[org.joda.time.LocalDateTime] = column[org.joda.time.LocalDateTime]("last_updated")
    /** Database column thing_id SqlType(int4) */
    val thingId: Rep[Int] = column[Int]("thing_id")
    /** Database column event_id SqlType(int4) */
    val eventId: Rep[Int] = column[Int]("event_id")
    /** Database column relationship_type SqlType(varchar), Length(100,true) */
    val relationshipType: Rep[String] = column[String]("relationship_type", O.Length(100,varying=true))
    /** Database column is_current SqlType(bool) */
    val isCurrent: Rep[Boolean] = column[Boolean]("is_current")
    /** Database column relationshiprecord_id SqlType(int4) */
    val relationshiprecordId: Rep[Int] = column[Int]("relationshiprecord_id")

    /** Foreign key referencing EventsEvent (database name events_eventthingcrossref_fk) */
    lazy val eventsEventFk = foreignKey("events_eventthingcrossref_fk", eventId, EventsEvent)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
    /** Foreign key referencing SystemRelationshiprecord (database name system_relationshiprecord_events_eventthingcrossref_fk) */
    lazy val systemRelationshiprecordFk = foreignKey("system_relationshiprecord_events_eventthingcrossref_fk", relationshiprecordId, SystemRelationshiprecord)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
    /** Foreign key referencing ThingsThing (database name events_thingeventcrossref_fk) */
    lazy val thingsThingFk = foreignKey("events_thingeventcrossref_fk", thingId, ThingsThing)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
  }
  /** Collection-like TableQuery object for table EventsEventthingcrossref */
  lazy val EventsEventthingcrossref = new TableQuery(tag => new EventsEventthingcrossref(tag))

  /** Entity class storing rows of table EventsEventtoeventcrossref
   *  @param id Database column id SqlType(serial), AutoInc, PrimaryKey
   *  @param dateCreated Database column date_created SqlType(timestamp)
   *  @param lastUpdated Database column last_updated SqlType(timestamp)
   *  @param eventOneId Database column event_one_id SqlType(int4)
   *  @param eventTwoId Database column event_two_id SqlType(int4)
   *  @param relationshipType Database column relationship_type SqlType(varchar), Length(100,true)
   *  @param isCurrent Database column is_current SqlType(bool)
   *  @param relationshiprecordId Database column relationshiprecord_id SqlType(int4) */
  case class EventsEventtoeventcrossrefRow(id: Int, dateCreated: org.joda.time.LocalDateTime, lastUpdated: org.joda.time.LocalDateTime, eventOneId: Int, eventTwoId: Int, relationshipType: String, isCurrent: Boolean, relationshiprecordId: Int)
  /** GetResult implicit for fetching EventsEventtoeventcrossrefRow objects using plain SQL queries */
  implicit def GetResultEventsEventtoeventcrossrefRow(implicit e0: GR[Int], e1: GR[org.joda.time.LocalDateTime], e2: GR[String], e3: GR[Boolean]): GR[EventsEventtoeventcrossrefRow] = GR{
    prs => import prs._
    EventsEventtoeventcrossrefRow.tupled((<<[Int], <<[org.joda.time.LocalDateTime], <<[org.joda.time.LocalDateTime], <<[Int], <<[Int], <<[String], <<[Boolean], <<[Int]))
  }
  /** Table description of table events_eventtoeventcrossref. Objects of this class serve as prototypes for rows in queries. */
  class EventsEventtoeventcrossref(_tableTag: Tag) extends Table[EventsEventtoeventcrossrefRow](_tableTag, Some("hat"), "events_eventtoeventcrossref") {
    def * = (id, dateCreated, lastUpdated, eventOneId, eventTwoId, relationshipType, isCurrent, relationshiprecordId) <> (EventsEventtoeventcrossrefRow.tupled, EventsEventtoeventcrossrefRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(dateCreated), Rep.Some(lastUpdated), Rep.Some(eventOneId), Rep.Some(eventTwoId), Rep.Some(relationshipType), Rep.Some(isCurrent), Rep.Some(relationshiprecordId)).shaped.<>({r=>import r._; _1.map(_=> EventsEventtoeventcrossrefRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get, _7.get, _8.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(serial), AutoInc, PrimaryKey */
    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    /** Database column date_created SqlType(timestamp) */
    val dateCreated: Rep[org.joda.time.LocalDateTime] = column[org.joda.time.LocalDateTime]("date_created")
    /** Database column last_updated SqlType(timestamp) */
    val lastUpdated: Rep[org.joda.time.LocalDateTime] = column[org.joda.time.LocalDateTime]("last_updated")
    /** Database column event_one_id SqlType(int4) */
    val eventOneId: Rep[Int] = column[Int]("event_one_id")
    /** Database column event_two_id SqlType(int4) */
    val eventTwoId: Rep[Int] = column[Int]("event_two_id")
    /** Database column relationship_type SqlType(varchar), Length(100,true) */
    val relationshipType: Rep[String] = column[String]("relationship_type", O.Length(100,varying=true))
    /** Database column is_current SqlType(bool) */
    val isCurrent: Rep[Boolean] = column[Boolean]("is_current")
    /** Database column relationshiprecord_id SqlType(int4) */
    val relationshiprecordId: Rep[Int] = column[Int]("relationshiprecord_id")

    /** Foreign key referencing EventsEvent (database name event_one_id_refs_id_fk) */
    lazy val eventsEventFk1 = foreignKey("event_one_id_refs_id_fk", eventOneId, EventsEvent)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
    /** Foreign key referencing EventsEvent (database name event_two_id_refs_id_fk) */
    lazy val eventsEventFk2 = foreignKey("event_two_id_refs_id_fk", eventTwoId, EventsEvent)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
    /** Foreign key referencing SystemRelationshiprecord (database name system_relationshiprecord_events_eventtoeventcrossref_fk) */
    lazy val systemRelationshiprecordFk = foreignKey("system_relationshiprecord_events_eventtoeventcrossref_fk", relationshiprecordId, SystemRelationshiprecord)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
  }
  /** Collection-like TableQuery object for table EventsEventtoeventcrossref */
  lazy val EventsEventtoeventcrossref = new TableQuery(tag => new EventsEventtoeventcrossref(tag))

  /** Entity class storing rows of table EventsSystempropertydynamiccrossref
   *  @param id Database column id SqlType(serial), AutoInc, PrimaryKey
   *  @param dateCreated Database column date_created SqlType(timestamp)
   *  @param lastUpdated Database column last_updated SqlType(timestamp)
   *  @param eventId Database column event_id SqlType(int4)
   *  @param systemPropertyId Database column system_property_id SqlType(int4)
   *  @param fieldId Database column field_id SqlType(int4)
   *  @param relationshipType Database column relationship_type SqlType(varchar), Length(100,true)
   *  @param isCurrent Database column is_current SqlType(bool)
   *  @param propertyrecordId Database column propertyrecord_id SqlType(int4) */
  case class EventsSystempropertydynamiccrossrefRow(id: Int, dateCreated: org.joda.time.LocalDateTime, lastUpdated: org.joda.time.LocalDateTime, eventId: Int, systemPropertyId: Int, fieldId: Int, relationshipType: String, isCurrent: Boolean, propertyrecordId: Int)
  /** GetResult implicit for fetching EventsSystempropertydynamiccrossrefRow objects using plain SQL queries */
  implicit def GetResultEventsSystempropertydynamiccrossrefRow(implicit e0: GR[Int], e1: GR[org.joda.time.LocalDateTime], e2: GR[String], e3: GR[Boolean]): GR[EventsSystempropertydynamiccrossrefRow] = GR{
    prs => import prs._
    EventsSystempropertydynamiccrossrefRow.tupled((<<[Int], <<[org.joda.time.LocalDateTime], <<[org.joda.time.LocalDateTime], <<[Int], <<[Int], <<[Int], <<[String], <<[Boolean], <<[Int]))
  }
  /** Table description of table events_systempropertydynamiccrossref. Objects of this class serve as prototypes for rows in queries. */
  class EventsSystempropertydynamiccrossref(_tableTag: Tag) extends Table[EventsSystempropertydynamiccrossrefRow](_tableTag, Some("hat"), "events_systempropertydynamiccrossref") {
    def * = (id, dateCreated, lastUpdated, eventId, systemPropertyId, fieldId, relationshipType, isCurrent, propertyrecordId) <> (EventsSystempropertydynamiccrossrefRow.tupled, EventsSystempropertydynamiccrossrefRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(dateCreated), Rep.Some(lastUpdated), Rep.Some(eventId), Rep.Some(systemPropertyId), Rep.Some(fieldId), Rep.Some(relationshipType), Rep.Some(isCurrent), Rep.Some(propertyrecordId)).shaped.<>({r=>import r._; _1.map(_=> EventsSystempropertydynamiccrossrefRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get, _7.get, _8.get, _9.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(serial), AutoInc, PrimaryKey */
    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    /** Database column date_created SqlType(timestamp) */
    val dateCreated: Rep[org.joda.time.LocalDateTime] = column[org.joda.time.LocalDateTime]("date_created")
    /** Database column last_updated SqlType(timestamp) */
    val lastUpdated: Rep[org.joda.time.LocalDateTime] = column[org.joda.time.LocalDateTime]("last_updated")
    /** Database column event_id SqlType(int4) */
    val eventId: Rep[Int] = column[Int]("event_id")
    /** Database column system_property_id SqlType(int4) */
    val systemPropertyId: Rep[Int] = column[Int]("system_property_id")
    /** Database column field_id SqlType(int4) */
    val fieldId: Rep[Int] = column[Int]("field_id")
    /** Database column relationship_type SqlType(varchar), Length(100,true) */
    val relationshipType: Rep[String] = column[String]("relationship_type", O.Length(100,varying=true))
    /** Database column is_current SqlType(bool) */
    val isCurrent: Rep[Boolean] = column[Boolean]("is_current")
    /** Database column propertyrecord_id SqlType(int4) */
    val propertyrecordId: Rep[Int] = column[Int]("propertyrecord_id")

    /** Foreign key referencing DataField (database name data_field_events_systempropertydynamiccrossref_fk) */
    lazy val dataFieldFk = foreignKey("data_field_events_systempropertydynamiccrossref_fk", fieldId, DataField)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
    /** Foreign key referencing EventsEvent (database name events_systempropertydynamiccrossref_fk) */
    lazy val eventsEventFk = foreignKey("events_systempropertydynamiccrossref_fk", eventId, EventsEvent)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
    /** Foreign key referencing SystemProperty (database name system_property_events_systempropertydynamiccrossref_fk) */
    lazy val systemPropertyFk = foreignKey("system_property_events_systempropertydynamiccrossref_fk", systemPropertyId, SystemProperty)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
    /** Foreign key referencing SystemPropertyrecord (database name property_record_events_systempropertydynamiccrossref_fk) */
    lazy val systemPropertyrecordFk = foreignKey("property_record_events_systempropertydynamiccrossref_fk", propertyrecordId, SystemPropertyrecord)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
  }
  /** Collection-like TableQuery object for table EventsSystempropertydynamiccrossref */
  lazy val EventsSystempropertydynamiccrossref = new TableQuery(tag => new EventsSystempropertydynamiccrossref(tag))

  /** Entity class storing rows of table EventsSystempropertystaticcrossref
   *  @param id Database column id SqlType(serial), AutoInc, PrimaryKey
   *  @param dateCreated Database column date_created SqlType(timestamp)
   *  @param lastUpdated Database column last_updated SqlType(timestamp)
   *  @param eventId Database column event_id SqlType(int4)
   *  @param systemPropertyId Database column system_property_id SqlType(int4)
   *  @param recordId Database column record_id SqlType(int4)
   *  @param fieldId Database column field_id SqlType(int4)
   *  @param relationshipType Database column relationship_type SqlType(varchar), Length(100,true)
   *  @param isCurrent Database column is_current SqlType(bool)
   *  @param propertyrecordId Database column propertyrecord_id SqlType(int4) */
  case class EventsSystempropertystaticcrossrefRow(id: Int, dateCreated: org.joda.time.LocalDateTime, lastUpdated: org.joda.time.LocalDateTime, eventId: Int, systemPropertyId: Int, recordId: Int, fieldId: Int, relationshipType: String, isCurrent: Boolean, propertyrecordId: Int)
  /** GetResult implicit for fetching EventsSystempropertystaticcrossrefRow objects using plain SQL queries */
  implicit def GetResultEventsSystempropertystaticcrossrefRow(implicit e0: GR[Int], e1: GR[org.joda.time.LocalDateTime], e2: GR[String], e3: GR[Boolean]): GR[EventsSystempropertystaticcrossrefRow] = GR{
    prs => import prs._
    EventsSystempropertystaticcrossrefRow.tupled((<<[Int], <<[org.joda.time.LocalDateTime], <<[org.joda.time.LocalDateTime], <<[Int], <<[Int], <<[Int], <<[Int], <<[String], <<[Boolean], <<[Int]))
  }
  /** Table description of table events_systempropertystaticcrossref. Objects of this class serve as prototypes for rows in queries. */
  class EventsSystempropertystaticcrossref(_tableTag: Tag) extends Table[EventsSystempropertystaticcrossrefRow](_tableTag, Some("hat"), "events_systempropertystaticcrossref") {
    def * = (id, dateCreated, lastUpdated, eventId, systemPropertyId, recordId, fieldId, relationshipType, isCurrent, propertyrecordId) <> (EventsSystempropertystaticcrossrefRow.tupled, EventsSystempropertystaticcrossrefRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(dateCreated), Rep.Some(lastUpdated), Rep.Some(eventId), Rep.Some(systemPropertyId), Rep.Some(recordId), Rep.Some(fieldId), Rep.Some(relationshipType), Rep.Some(isCurrent), Rep.Some(propertyrecordId)).shaped.<>({r=>import r._; _1.map(_=> EventsSystempropertystaticcrossrefRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get, _7.get, _8.get, _9.get, _10.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(serial), AutoInc, PrimaryKey */
    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    /** Database column date_created SqlType(timestamp) */
    val dateCreated: Rep[org.joda.time.LocalDateTime] = column[org.joda.time.LocalDateTime]("date_created")
    /** Database column last_updated SqlType(timestamp) */
    val lastUpdated: Rep[org.joda.time.LocalDateTime] = column[org.joda.time.LocalDateTime]("last_updated")
    /** Database column event_id SqlType(int4) */
    val eventId: Rep[Int] = column[Int]("event_id")
    /** Database column system_property_id SqlType(int4) */
    val systemPropertyId: Rep[Int] = column[Int]("system_property_id")
    /** Database column record_id SqlType(int4) */
    val recordId: Rep[Int] = column[Int]("record_id")
    /** Database column field_id SqlType(int4) */
    val fieldId: Rep[Int] = column[Int]("field_id")
    /** Database column relationship_type SqlType(varchar), Length(100,true) */
    val relationshipType: Rep[String] = column[String]("relationship_type", O.Length(100,varying=true))
    /** Database column is_current SqlType(bool) */
    val isCurrent: Rep[Boolean] = column[Boolean]("is_current")
    /** Database column propertyrecord_id SqlType(int4) */
    val propertyrecordId: Rep[Int] = column[Int]("propertyrecord_id")

    /** Foreign key referencing DataField (database name data_field_events_systempropertystaticcrossref_fk) */
    lazy val dataFieldFk = foreignKey("data_field_events_systempropertystaticcrossref_fk", fieldId, DataField)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
    /** Foreign key referencing DataRecord (database name data_record_events_systempropertystaticcrossref_fk) */
    lazy val dataRecordFk = foreignKey("data_record_events_systempropertystaticcrossref_fk", recordId, DataRecord)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
    /** Foreign key referencing EventsEvent (database name events_systempropertycrossref_fk) */
    lazy val eventsEventFk = foreignKey("events_systempropertycrossref_fk", eventId, EventsEvent)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
    /** Foreign key referencing SystemProperty (database name system_property_events_systempropertystaticcrossref_fk) */
    lazy val systemPropertyFk = foreignKey("system_property_events_systempropertystaticcrossref_fk", systemPropertyId, SystemProperty)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
    /** Foreign key referencing SystemPropertyrecord (database name property_record_events_systempropertystaticcrossref_fk) */
    lazy val systemPropertyrecordFk = foreignKey("property_record_events_systempropertystaticcrossref_fk", propertyrecordId, SystemPropertyrecord)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
  }
  /** Collection-like TableQuery object for table EventsSystempropertystaticcrossref */
  lazy val EventsSystempropertystaticcrossref = new TableQuery(tag => new EventsSystempropertystaticcrossref(tag))

  /** Entity class storing rows of table EventsSystemtypecrossref
   *  @param id Database column id SqlType(serial), AutoInc, PrimaryKey
   *  @param dateCreated Database column date_created SqlType(timestamp)
   *  @param lastUpdated Database column last_updated SqlType(timestamp)
   *  @param eventId Database column event_id SqlType(int4)
   *  @param systemTypeId Database column system_type_id SqlType(int4)
   *  @param relationshipType Database column relationship_type SqlType(varchar), Length(100,true)
   *  @param isCurrent Database column is_current SqlType(bool) */
  case class EventsSystemtypecrossrefRow(id: Int, dateCreated: org.joda.time.LocalDateTime, lastUpdated: org.joda.time.LocalDateTime, eventId: Int, systemTypeId: Int, relationshipType: String, isCurrent: Boolean)
  /** GetResult implicit for fetching EventsSystemtypecrossrefRow objects using plain SQL queries */
  implicit def GetResultEventsSystemtypecrossrefRow(implicit e0: GR[Int], e1: GR[org.joda.time.LocalDateTime], e2: GR[String], e3: GR[Boolean]): GR[EventsSystemtypecrossrefRow] = GR{
    prs => import prs._
    EventsSystemtypecrossrefRow.tupled((<<[Int], <<[org.joda.time.LocalDateTime], <<[org.joda.time.LocalDateTime], <<[Int], <<[Int], <<[String], <<[Boolean]))
  }
  /** Table description of table events_systemtypecrossref. Objects of this class serve as prototypes for rows in queries. */
  class EventsSystemtypecrossref(_tableTag: Tag) extends Table[EventsSystemtypecrossrefRow](_tableTag, Some("hat"), "events_systemtypecrossref") {
    def * = (id, dateCreated, lastUpdated, eventId, systemTypeId, relationshipType, isCurrent) <> (EventsSystemtypecrossrefRow.tupled, EventsSystemtypecrossrefRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(dateCreated), Rep.Some(lastUpdated), Rep.Some(eventId), Rep.Some(systemTypeId), Rep.Some(relationshipType), Rep.Some(isCurrent)).shaped.<>({r=>import r._; _1.map(_=> EventsSystemtypecrossrefRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get, _7.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(serial), AutoInc, PrimaryKey */
    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    /** Database column date_created SqlType(timestamp) */
    val dateCreated: Rep[org.joda.time.LocalDateTime] = column[org.joda.time.LocalDateTime]("date_created")
    /** Database column last_updated SqlType(timestamp) */
    val lastUpdated: Rep[org.joda.time.LocalDateTime] = column[org.joda.time.LocalDateTime]("last_updated")
    /** Database column event_id SqlType(int4) */
    val eventId: Rep[Int] = column[Int]("event_id")
    /** Database column system_type_id SqlType(int4) */
    val systemTypeId: Rep[Int] = column[Int]("system_type_id")
    /** Database column relationship_type SqlType(varchar), Length(100,true) */
    val relationshipType: Rep[String] = column[String]("relationship_type", O.Length(100,varying=true))
    /** Database column is_current SqlType(bool) */
    val isCurrent: Rep[Boolean] = column[Boolean]("is_current")

    /** Foreign key referencing EventsEvent (database name events_systemtypecrossref_fk) */
    lazy val eventsEventFk = foreignKey("events_systemtypecrossref_fk", eventId, EventsEvent)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
    /** Foreign key referencing SystemType (database name system_type_events_systemtypecrossref_fk) */
    lazy val systemTypeFk = foreignKey("system_type_events_systemtypecrossref_fk", systemTypeId, SystemType)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
  }
  /** Collection-like TableQuery object for table EventsSystemtypecrossref */
  lazy val EventsSystemtypecrossref = new TableQuery(tag => new EventsSystemtypecrossref(tag))

  /** Entity class storing rows of table LocationsLocation
   *  @param id Database column id SqlType(serial), AutoInc, PrimaryKey
   *  @param dateCreated Database column date_created SqlType(timestamp)
   *  @param lastUpdated Database column last_updated SqlType(timestamp)
   *  @param name Database column name SqlType(varchar), Length(512,true) */
  case class LocationsLocationRow(id: Int, dateCreated: org.joda.time.LocalDateTime, lastUpdated: org.joda.time.LocalDateTime, name: String)
  /** GetResult implicit for fetching LocationsLocationRow objects using plain SQL queries */
  implicit def GetResultLocationsLocationRow(implicit e0: GR[Int], e1: GR[org.joda.time.LocalDateTime], e2: GR[String]): GR[LocationsLocationRow] = GR{
    prs => import prs._
    LocationsLocationRow.tupled((<<[Int], <<[org.joda.time.LocalDateTime], <<[org.joda.time.LocalDateTime], <<[String]))
  }
  /** Table description of table locations_location. Objects of this class serve as prototypes for rows in queries. */
  class LocationsLocation(_tableTag: Tag) extends Table[LocationsLocationRow](_tableTag, Some("hat"), "locations_location") {
    def * = (id, dateCreated, lastUpdated, name) <> (LocationsLocationRow.tupled, LocationsLocationRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(dateCreated), Rep.Some(lastUpdated), Rep.Some(name)).shaped.<>({r=>import r._; _1.map(_=> LocationsLocationRow.tupled((_1.get, _2.get, _3.get, _4.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(serial), AutoInc, PrimaryKey */
    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    /** Database column date_created SqlType(timestamp) */
    val dateCreated: Rep[org.joda.time.LocalDateTime] = column[org.joda.time.LocalDateTime]("date_created")
    /** Database column last_updated SqlType(timestamp) */
    val lastUpdated: Rep[org.joda.time.LocalDateTime] = column[org.joda.time.LocalDateTime]("last_updated")
    /** Database column name SqlType(varchar), Length(512,true) */
    val name: Rep[String] = column[String]("name", O.Length(512,varying=true))
  }
  /** Collection-like TableQuery object for table LocationsLocation */
  lazy val LocationsLocation = new TableQuery(tag => new LocationsLocation(tag))

  /** Entity class storing rows of table LocationsLocationthingcrossref
   *  @param id Database column id SqlType(serial), AutoInc, PrimaryKey
   *  @param dateCreated Database column date_created SqlType(timestamp)
   *  @param lastUpdated Database column last_updated SqlType(timestamp)
   *  @param thingId Database column thing_id SqlType(int4)
   *  @param locationId Database column location_id SqlType(int4)
   *  @param relationshipType Database column relationship_type SqlType(varchar), Length(100,true)
   *  @param isCurrent Database column is_current SqlType(bool)
   *  @param relationshiprecordId Database column relationshiprecord_id SqlType(int4) */
  case class LocationsLocationthingcrossrefRow(id: Int, dateCreated: org.joda.time.LocalDateTime, lastUpdated: org.joda.time.LocalDateTime, thingId: Int, locationId: Int, relationshipType: String, isCurrent: Boolean, relationshiprecordId: Int)
  /** GetResult implicit for fetching LocationsLocationthingcrossrefRow objects using plain SQL queries */
  implicit def GetResultLocationsLocationthingcrossrefRow(implicit e0: GR[Int], e1: GR[org.joda.time.LocalDateTime], e2: GR[String], e3: GR[Boolean]): GR[LocationsLocationthingcrossrefRow] = GR{
    prs => import prs._
    LocationsLocationthingcrossrefRow.tupled((<<[Int], <<[org.joda.time.LocalDateTime], <<[org.joda.time.LocalDateTime], <<[Int], <<[Int], <<[String], <<[Boolean], <<[Int]))
  }
  /** Table description of table locations_locationthingcrossref. Objects of this class serve as prototypes for rows in queries. */
  class LocationsLocationthingcrossref(_tableTag: Tag) extends Table[LocationsLocationthingcrossrefRow](_tableTag, Some("hat"), "locations_locationthingcrossref") {
    def * = (id, dateCreated, lastUpdated, thingId, locationId, relationshipType, isCurrent, relationshiprecordId) <> (LocationsLocationthingcrossrefRow.tupled, LocationsLocationthingcrossrefRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(dateCreated), Rep.Some(lastUpdated), Rep.Some(thingId), Rep.Some(locationId), Rep.Some(relationshipType), Rep.Some(isCurrent), Rep.Some(relationshiprecordId)).shaped.<>({r=>import r._; _1.map(_=> LocationsLocationthingcrossrefRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get, _7.get, _8.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(serial), AutoInc, PrimaryKey */
    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    /** Database column date_created SqlType(timestamp) */
    val dateCreated: Rep[org.joda.time.LocalDateTime] = column[org.joda.time.LocalDateTime]("date_created")
    /** Database column last_updated SqlType(timestamp) */
    val lastUpdated: Rep[org.joda.time.LocalDateTime] = column[org.joda.time.LocalDateTime]("last_updated")
    /** Database column thing_id SqlType(int4) */
    val thingId: Rep[Int] = column[Int]("thing_id")
    /** Database column location_id SqlType(int4) */
    val locationId: Rep[Int] = column[Int]("location_id")
    /** Database column relationship_type SqlType(varchar), Length(100,true) */
    val relationshipType: Rep[String] = column[String]("relationship_type", O.Length(100,varying=true))
    /** Database column is_current SqlType(bool) */
    val isCurrent: Rep[Boolean] = column[Boolean]("is_current")
    /** Database column relationshiprecord_id SqlType(int4) */
    val relationshiprecordId: Rep[Int] = column[Int]("relationshiprecord_id")

    /** Foreign key referencing LocationsLocation (database name locations_locationthingcrossref_location_id_fkey) */
    lazy val locationsLocationFk = foreignKey("locations_locationthingcrossref_location_id_fkey", locationId, LocationsLocation)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
    /** Foreign key referencing SystemRelationshiprecord (database name system_relationshiprecord_locations_locationthingcrossref_fk) */
    lazy val systemRelationshiprecordFk = foreignKey("system_relationshiprecord_locations_locationthingcrossref_fk", relationshiprecordId, SystemRelationshiprecord)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
    /** Foreign key referencing ThingsThing (database name thing_id_refs_id_fk) */
    lazy val thingsThingFk = foreignKey("thing_id_refs_id_fk", thingId, ThingsThing)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
  }
  /** Collection-like TableQuery object for table LocationsLocationthingcrossref */
  lazy val LocationsLocationthingcrossref = new TableQuery(tag => new LocationsLocationthingcrossref(tag))

  /** Entity class storing rows of table LocationsLocationtolocationcrossref
   *  @param id Database column id SqlType(serial), AutoInc, PrimaryKey
   *  @param dateCreated Database column date_created SqlType(timestamp)
   *  @param lastUpdated Database column last_updated SqlType(timestamp)
   *  @param locOneId Database column loc_one_id SqlType(int4)
   *  @param locTwoId Database column loc_two_id SqlType(int4)
   *  @param relationshipType Database column relationship_type SqlType(varchar), Length(100,true)
   *  @param isCurrent Database column is_current SqlType(bool)
   *  @param relationshiprecordId Database column relationshiprecord_id SqlType(int4) */
  case class LocationsLocationtolocationcrossrefRow(id: Int, dateCreated: org.joda.time.LocalDateTime, lastUpdated: org.joda.time.LocalDateTime, locOneId: Int, locTwoId: Int, relationshipType: String, isCurrent: Boolean, relationshiprecordId: Int)
  /** GetResult implicit for fetching LocationsLocationtolocationcrossrefRow objects using plain SQL queries */
  implicit def GetResultLocationsLocationtolocationcrossrefRow(implicit e0: GR[Int], e1: GR[org.joda.time.LocalDateTime], e2: GR[String], e3: GR[Boolean]): GR[LocationsLocationtolocationcrossrefRow] = GR{
    prs => import prs._
    LocationsLocationtolocationcrossrefRow.tupled((<<[Int], <<[org.joda.time.LocalDateTime], <<[org.joda.time.LocalDateTime], <<[Int], <<[Int], <<[String], <<[Boolean], <<[Int]))
  }
  /** Table description of table locations_locationtolocationcrossref. Objects of this class serve as prototypes for rows in queries. */
  class LocationsLocationtolocationcrossref(_tableTag: Tag) extends Table[LocationsLocationtolocationcrossrefRow](_tableTag, Some("hat"), "locations_locationtolocationcrossref") {
    def * = (id, dateCreated, lastUpdated, locOneId, locTwoId, relationshipType, isCurrent, relationshiprecordId) <> (LocationsLocationtolocationcrossrefRow.tupled, LocationsLocationtolocationcrossrefRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(dateCreated), Rep.Some(lastUpdated), Rep.Some(locOneId), Rep.Some(locTwoId), Rep.Some(relationshipType), Rep.Some(isCurrent), Rep.Some(relationshiprecordId)).shaped.<>({r=>import r._; _1.map(_=> LocationsLocationtolocationcrossrefRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get, _7.get, _8.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(serial), AutoInc, PrimaryKey */
    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    /** Database column date_created SqlType(timestamp) */
    val dateCreated: Rep[org.joda.time.LocalDateTime] = column[org.joda.time.LocalDateTime]("date_created")
    /** Database column last_updated SqlType(timestamp) */
    val lastUpdated: Rep[org.joda.time.LocalDateTime] = column[org.joda.time.LocalDateTime]("last_updated")
    /** Database column loc_one_id SqlType(int4) */
    val locOneId: Rep[Int] = column[Int]("loc_one_id")
    /** Database column loc_two_id SqlType(int4) */
    val locTwoId: Rep[Int] = column[Int]("loc_two_id")
    /** Database column relationship_type SqlType(varchar), Length(100,true) */
    val relationshipType: Rep[String] = column[String]("relationship_type", O.Length(100,varying=true))
    /** Database column is_current SqlType(bool) */
    val isCurrent: Rep[Boolean] = column[Boolean]("is_current")
    /** Database column relationshiprecord_id SqlType(int4) */
    val relationshiprecordId: Rep[Int] = column[Int]("relationshiprecord_id")

    /** Foreign key referencing LocationsLocation (database name locations_locationtolocationcrossref_loc_one_id_fkey) */
    lazy val locationsLocationFk1 = foreignKey("locations_locationtolocationcrossref_loc_one_id_fkey", locOneId, LocationsLocation)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
    /** Foreign key referencing LocationsLocation (database name locations_locationtolocationcrossref_loc_two_id_fkey) */
    lazy val locationsLocationFk2 = foreignKey("locations_locationtolocationcrossref_loc_two_id_fkey", locTwoId, LocationsLocation)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
    /** Foreign key referencing SystemRelationshiprecord (database name system_relationshiprecord_locations_locationtolocationcrossr309) */
    lazy val systemRelationshiprecordFk = foreignKey("system_relationshiprecord_locations_locationtolocationcrossr309", relationshiprecordId, SystemRelationshiprecord)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
  }
  /** Collection-like TableQuery object for table LocationsLocationtolocationcrossref */
  lazy val LocationsLocationtolocationcrossref = new TableQuery(tag => new LocationsLocationtolocationcrossref(tag))

  /** Entity class storing rows of table LocationsSystempropertydynamiccrossref
   *  @param id Database column id SqlType(serial), AutoInc, PrimaryKey
   *  @param dateCreated Database column date_created SqlType(timestamp)
   *  @param lastUpdated Database column last_updated SqlType(timestamp)
   *  @param locationId Database column location_id SqlType(int4)
   *  @param systemPropertyId Database column system_property_id SqlType(int4)
   *  @param fieldId Database column field_id SqlType(int4)
   *  @param relationshipType Database column relationship_type SqlType(varchar), Length(100,true)
   *  @param isCurrent Database column is_current SqlType(bool)
   *  @param propertyrecordId Database column propertyrecord_id SqlType(int4) */
  case class LocationsSystempropertydynamiccrossrefRow(id: Int, dateCreated: org.joda.time.LocalDateTime, lastUpdated: org.joda.time.LocalDateTime, locationId: Int, systemPropertyId: Int, fieldId: Int, relationshipType: String, isCurrent: Boolean, propertyrecordId: Int)
  /** GetResult implicit for fetching LocationsSystempropertydynamiccrossrefRow objects using plain SQL queries */
  implicit def GetResultLocationsSystempropertydynamiccrossrefRow(implicit e0: GR[Int], e1: GR[org.joda.time.LocalDateTime], e2: GR[String], e3: GR[Boolean]): GR[LocationsSystempropertydynamiccrossrefRow] = GR{
    prs => import prs._
    LocationsSystempropertydynamiccrossrefRow.tupled((<<[Int], <<[org.joda.time.LocalDateTime], <<[org.joda.time.LocalDateTime], <<[Int], <<[Int], <<[Int], <<[String], <<[Boolean], <<[Int]))
  }
  /** Table description of table locations_systempropertydynamiccrossref. Objects of this class serve as prototypes for rows in queries. */
  class LocationsSystempropertydynamiccrossref(_tableTag: Tag) extends Table[LocationsSystempropertydynamiccrossrefRow](_tableTag, Some("hat"), "locations_systempropertydynamiccrossref") {
    def * = (id, dateCreated, lastUpdated, locationId, systemPropertyId, fieldId, relationshipType, isCurrent, propertyrecordId) <> (LocationsSystempropertydynamiccrossrefRow.tupled, LocationsSystempropertydynamiccrossrefRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(dateCreated), Rep.Some(lastUpdated), Rep.Some(locationId), Rep.Some(systemPropertyId), Rep.Some(fieldId), Rep.Some(relationshipType), Rep.Some(isCurrent), Rep.Some(propertyrecordId)).shaped.<>({r=>import r._; _1.map(_=> LocationsSystempropertydynamiccrossrefRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get, _7.get, _8.get, _9.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(serial), AutoInc, PrimaryKey */
    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    /** Database column date_created SqlType(timestamp) */
    val dateCreated: Rep[org.joda.time.LocalDateTime] = column[org.joda.time.LocalDateTime]("date_created")
    /** Database column last_updated SqlType(timestamp) */
    val lastUpdated: Rep[org.joda.time.LocalDateTime] = column[org.joda.time.LocalDateTime]("last_updated")
    /** Database column location_id SqlType(int4) */
    val locationId: Rep[Int] = column[Int]("location_id")
    /** Database column system_property_id SqlType(int4) */
    val systemPropertyId: Rep[Int] = column[Int]("system_property_id")
    /** Database column field_id SqlType(int4) */
    val fieldId: Rep[Int] = column[Int]("field_id")
    /** Database column relationship_type SqlType(varchar), Length(100,true) */
    val relationshipType: Rep[String] = column[String]("relationship_type", O.Length(100,varying=true))
    /** Database column is_current SqlType(bool) */
    val isCurrent: Rep[Boolean] = column[Boolean]("is_current")
    /** Database column propertyrecord_id SqlType(int4) */
    val propertyrecordId: Rep[Int] = column[Int]("propertyrecord_id")

    /** Foreign key referencing DataField (database name data_field_locations_systempropertydynamiccrossref_fk) */
    lazy val dataFieldFk = foreignKey("data_field_locations_systempropertydynamiccrossref_fk", fieldId, DataField)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
    /** Foreign key referencing LocationsLocation (database name locations_location_locations_systempropertydynamiccrossref_fk) */
    lazy val locationsLocationFk = foreignKey("locations_location_locations_systempropertydynamiccrossref_fk", locationId, LocationsLocation)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
    /** Foreign key referencing SystemProperty (database name system_property_locations_systempropertydynamiccrossref_fk) */
    lazy val systemPropertyFk = foreignKey("system_property_locations_systempropertydynamiccrossref_fk", systemPropertyId, SystemProperty)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
    /** Foreign key referencing SystemPropertyrecord (database name property_record_locations_systempropertydynamiccrossref_fk) */
    lazy val systemPropertyrecordFk = foreignKey("property_record_locations_systempropertydynamiccrossref_fk", propertyrecordId, SystemPropertyrecord)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
  }
  /** Collection-like TableQuery object for table LocationsSystempropertydynamiccrossref */
  lazy val LocationsSystempropertydynamiccrossref = new TableQuery(tag => new LocationsSystempropertydynamiccrossref(tag))

  /** Entity class storing rows of table LocationsSystempropertystaticcrossref
   *  @param id Database column id SqlType(serial), AutoInc, PrimaryKey
   *  @param dateCreated Database column date_created SqlType(timestamp)
   *  @param lastUpdated Database column last_updated SqlType(timestamp)
   *  @param locationId Database column location_id SqlType(int4)
   *  @param systemPropertyId Database column system_property_id SqlType(int4)
   *  @param recordId Database column record_id SqlType(int4)
   *  @param fieldId Database column field_id SqlType(int4)
   *  @param relationshipType Database column relationship_type SqlType(varchar), Length(100,true)
   *  @param isCurrent Database column is_current SqlType(bool)
   *  @param propertyrecordId Database column propertyrecord_id SqlType(int4) */
  case class LocationsSystempropertystaticcrossrefRow(id: Int, dateCreated: org.joda.time.LocalDateTime, lastUpdated: org.joda.time.LocalDateTime, locationId: Int, systemPropertyId: Int, recordId: Int, fieldId: Int, relationshipType: String, isCurrent: Boolean, propertyrecordId: Int)
  /** GetResult implicit for fetching LocationsSystempropertystaticcrossrefRow objects using plain SQL queries */
  implicit def GetResultLocationsSystempropertystaticcrossrefRow(implicit e0: GR[Int], e1: GR[org.joda.time.LocalDateTime], e2: GR[String], e3: GR[Boolean]): GR[LocationsSystempropertystaticcrossrefRow] = GR{
    prs => import prs._
    LocationsSystempropertystaticcrossrefRow.tupled((<<[Int], <<[org.joda.time.LocalDateTime], <<[org.joda.time.LocalDateTime], <<[Int], <<[Int], <<[Int], <<[Int], <<[String], <<[Boolean], <<[Int]))
  }
  /** Table description of table locations_systempropertystaticcrossref. Objects of this class serve as prototypes for rows in queries. */
  class LocationsSystempropertystaticcrossref(_tableTag: Tag) extends Table[LocationsSystempropertystaticcrossrefRow](_tableTag, Some("hat"), "locations_systempropertystaticcrossref") {
    def * = (id, dateCreated, lastUpdated, locationId, systemPropertyId, recordId, fieldId, relationshipType, isCurrent, propertyrecordId) <> (LocationsSystempropertystaticcrossrefRow.tupled, LocationsSystempropertystaticcrossrefRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(dateCreated), Rep.Some(lastUpdated), Rep.Some(locationId), Rep.Some(systemPropertyId), Rep.Some(recordId), Rep.Some(fieldId), Rep.Some(relationshipType), Rep.Some(isCurrent), Rep.Some(propertyrecordId)).shaped.<>({r=>import r._; _1.map(_=> LocationsSystempropertystaticcrossrefRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get, _7.get, _8.get, _9.get, _10.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(serial), AutoInc, PrimaryKey */
    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    /** Database column date_created SqlType(timestamp) */
    val dateCreated: Rep[org.joda.time.LocalDateTime] = column[org.joda.time.LocalDateTime]("date_created")
    /** Database column last_updated SqlType(timestamp) */
    val lastUpdated: Rep[org.joda.time.LocalDateTime] = column[org.joda.time.LocalDateTime]("last_updated")
    /** Database column location_id SqlType(int4) */
    val locationId: Rep[Int] = column[Int]("location_id")
    /** Database column system_property_id SqlType(int4) */
    val systemPropertyId: Rep[Int] = column[Int]("system_property_id")
    /** Database column record_id SqlType(int4) */
    val recordId: Rep[Int] = column[Int]("record_id")
    /** Database column field_id SqlType(int4) */
    val fieldId: Rep[Int] = column[Int]("field_id")
    /** Database column relationship_type SqlType(varchar), Length(100,true) */
    val relationshipType: Rep[String] = column[String]("relationship_type", O.Length(100,varying=true))
    /** Database column is_current SqlType(bool) */
    val isCurrent: Rep[Boolean] = column[Boolean]("is_current")
    /** Database column propertyrecord_id SqlType(int4) */
    val propertyrecordId: Rep[Int] = column[Int]("propertyrecord_id")

    /** Foreign key referencing DataField (database name data_field_locations_systempropertystaticcrossref_fk) */
    lazy val dataFieldFk = foreignKey("data_field_locations_systempropertystaticcrossref_fk", fieldId, DataField)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
    /** Foreign key referencing DataRecord (database name data_record_locations_systempropertystaticcrossref_fk) */
    lazy val dataRecordFk = foreignKey("data_record_locations_systempropertystaticcrossref_fk", recordId, DataRecord)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
    /** Foreign key referencing LocationsLocation (database name locations_location_locations_systempropertystaticcrossref_fk) */
    lazy val locationsLocationFk = foreignKey("locations_location_locations_systempropertystaticcrossref_fk", locationId, LocationsLocation)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
    /** Foreign key referencing SystemProperty (database name system_property_locations_systempropertystaticcrossref_fk) */
    lazy val systemPropertyFk = foreignKey("system_property_locations_systempropertystaticcrossref_fk", systemPropertyId, SystemProperty)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
    /** Foreign key referencing SystemPropertyrecord (database name property_record_locations_systempropertystaticcrossref_fk) */
    lazy val systemPropertyrecordFk = foreignKey("property_record_locations_systempropertystaticcrossref_fk", propertyrecordId, SystemPropertyrecord)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
  }
  /** Collection-like TableQuery object for table LocationsSystempropertystaticcrossref */
  lazy val LocationsSystempropertystaticcrossref = new TableQuery(tag => new LocationsSystempropertystaticcrossref(tag))

  /** Entity class storing rows of table LocationsSystemtypecrossref
   *  @param id Database column id SqlType(serial), AutoInc, PrimaryKey
   *  @param dateCreated Database column date_created SqlType(timestamp)
   *  @param lastUpdated Database column last_updated SqlType(timestamp)
   *  @param locationId Database column location_id SqlType(int4)
   *  @param systemTypeId Database column system_type_id SqlType(int4)
   *  @param relationshipType Database column relationship_type SqlType(varchar), Length(100,true)
   *  @param isCurrent Database column is_current SqlType(bool) */
  case class LocationsSystemtypecrossrefRow(id: Int, dateCreated: org.joda.time.LocalDateTime, lastUpdated: org.joda.time.LocalDateTime, locationId: Int, systemTypeId: Int, relationshipType: String, isCurrent: Boolean)
  /** GetResult implicit for fetching LocationsSystemtypecrossrefRow objects using plain SQL queries */
  implicit def GetResultLocationsSystemtypecrossrefRow(implicit e0: GR[Int], e1: GR[org.joda.time.LocalDateTime], e2: GR[String], e3: GR[Boolean]): GR[LocationsSystemtypecrossrefRow] = GR{
    prs => import prs._
    LocationsSystemtypecrossrefRow.tupled((<<[Int], <<[org.joda.time.LocalDateTime], <<[org.joda.time.LocalDateTime], <<[Int], <<[Int], <<[String], <<[Boolean]))
  }
  /** Table description of table locations_systemtypecrossref. Objects of this class serve as prototypes for rows in queries. */
  class LocationsSystemtypecrossref(_tableTag: Tag) extends Table[LocationsSystemtypecrossrefRow](_tableTag, Some("hat"), "locations_systemtypecrossref") {
    def * = (id, dateCreated, lastUpdated, locationId, systemTypeId, relationshipType, isCurrent) <> (LocationsSystemtypecrossrefRow.tupled, LocationsSystemtypecrossrefRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(dateCreated), Rep.Some(lastUpdated), Rep.Some(locationId), Rep.Some(systemTypeId), Rep.Some(relationshipType), Rep.Some(isCurrent)).shaped.<>({r=>import r._; _1.map(_=> LocationsSystemtypecrossrefRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get, _7.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(serial), AutoInc, PrimaryKey */
    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    /** Database column date_created SqlType(timestamp) */
    val dateCreated: Rep[org.joda.time.LocalDateTime] = column[org.joda.time.LocalDateTime]("date_created")
    /** Database column last_updated SqlType(timestamp) */
    val lastUpdated: Rep[org.joda.time.LocalDateTime] = column[org.joda.time.LocalDateTime]("last_updated")
    /** Database column location_id SqlType(int4) */
    val locationId: Rep[Int] = column[Int]("location_id")
    /** Database column system_type_id SqlType(int4) */
    val systemTypeId: Rep[Int] = column[Int]("system_type_id")
    /** Database column relationship_type SqlType(varchar), Length(100,true) */
    val relationshipType: Rep[String] = column[String]("relationship_type", O.Length(100,varying=true))
    /** Database column is_current SqlType(bool) */
    val isCurrent: Rep[Boolean] = column[Boolean]("is_current")

    /** Foreign key referencing LocationsLocation (database name locations_location_location_systemtypecrossref_fk) */
    lazy val locationsLocationFk = foreignKey("locations_location_location_systemtypecrossref_fk", locationId, LocationsLocation)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
    /** Foreign key referencing SystemType (database name system_type_location_systemtypecrossref_fk) */
    lazy val systemTypeFk = foreignKey("system_type_location_systemtypecrossref_fk", systemTypeId, SystemType)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
  }
  /** Collection-like TableQuery object for table LocationsSystemtypecrossref */
  lazy val LocationsSystemtypecrossref = new TableQuery(tag => new LocationsSystemtypecrossref(tag))

  /** Entity class storing rows of table OrganisationsOrganisation
   *  @param id Database column id SqlType(serial), AutoInc, PrimaryKey
   *  @param dateCreated Database column date_created SqlType(timestamp)
   *  @param lastyUpdated Database column lasty_updated SqlType(timestamp)
   *  @param name Database column name SqlType(varchar), Length(100,true) */
  case class OrganisationsOrganisationRow(id: Int, dateCreated: org.joda.time.LocalDateTime, lastyUpdated: org.joda.time.LocalDateTime, name: String)
  /** GetResult implicit for fetching OrganisationsOrganisationRow objects using plain SQL queries */
  implicit def GetResultOrganisationsOrganisationRow(implicit e0: GR[Int], e1: GR[org.joda.time.LocalDateTime], e2: GR[String]): GR[OrganisationsOrganisationRow] = GR{
    prs => import prs._
    OrganisationsOrganisationRow.tupled((<<[Int], <<[org.joda.time.LocalDateTime], <<[org.joda.time.LocalDateTime], <<[String]))
  }
  /** Table description of table organisations_organisation. Objects of this class serve as prototypes for rows in queries. */
  class OrganisationsOrganisation(_tableTag: Tag) extends Table[OrganisationsOrganisationRow](_tableTag, Some("hat"), "organisations_organisation") {
    def * = (id, dateCreated, lastyUpdated, name) <> (OrganisationsOrganisationRow.tupled, OrganisationsOrganisationRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(dateCreated), Rep.Some(lastyUpdated), Rep.Some(name)).shaped.<>({r=>import r._; _1.map(_=> OrganisationsOrganisationRow.tupled((_1.get, _2.get, _3.get, _4.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(serial), AutoInc, PrimaryKey */
    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    /** Database column date_created SqlType(timestamp) */
    val dateCreated: Rep[org.joda.time.LocalDateTime] = column[org.joda.time.LocalDateTime]("date_created")
    /** Database column lasty_updated SqlType(timestamp) */
    val lastyUpdated: Rep[org.joda.time.LocalDateTime] = column[org.joda.time.LocalDateTime]("lasty_updated")
    /** Database column name SqlType(varchar), Length(100,true) */
    val name: Rep[String] = column[String]("name", O.Length(100,varying=true))
  }
  /** Collection-like TableQuery object for table OrganisationsOrganisation */
  lazy val OrganisationsOrganisation = new TableQuery(tag => new OrganisationsOrganisation(tag))

  /** Entity class storing rows of table OrganisationsOrganisationlocationcrossref
   *  @param id Database column id SqlType(serial), AutoInc, PrimaryKey
   *  @param dateCreated Database column date_created SqlType(timestamp)
   *  @param lastUpdated Database column last_updated SqlType(timestamp)
   *  @param locationId Database column location_id SqlType(int4)
   *  @param organisationId Database column organisation_id SqlType(int4)
   *  @param relationshipType Database column relationship_type SqlType(varchar), Length(100,true)
   *  @param isCurrent Database column is_current SqlType(bool)
   *  @param relationshiprecordId Database column relationshiprecord_id SqlType(int4) */
  case class OrganisationsOrganisationlocationcrossrefRow(id: Int, dateCreated: org.joda.time.LocalDateTime, lastUpdated: org.joda.time.LocalDateTime, locationId: Int, organisationId: Int, relationshipType: String, isCurrent: Boolean, relationshiprecordId: Int)
  /** GetResult implicit for fetching OrganisationsOrganisationlocationcrossrefRow objects using plain SQL queries */
  implicit def GetResultOrganisationsOrganisationlocationcrossrefRow(implicit e0: GR[Int], e1: GR[org.joda.time.LocalDateTime], e2: GR[String], e3: GR[Boolean]): GR[OrganisationsOrganisationlocationcrossrefRow] = GR{
    prs => import prs._
    OrganisationsOrganisationlocationcrossrefRow.tupled((<<[Int], <<[org.joda.time.LocalDateTime], <<[org.joda.time.LocalDateTime], <<[Int], <<[Int], <<[String], <<[Boolean], <<[Int]))
  }
  /** Table description of table organisations_organisationlocationcrossref. Objects of this class serve as prototypes for rows in queries. */
  class OrganisationsOrganisationlocationcrossref(_tableTag: Tag) extends Table[OrganisationsOrganisationlocationcrossrefRow](_tableTag, Some("hat"), "organisations_organisationlocationcrossref") {
    def * = (id, dateCreated, lastUpdated, locationId, organisationId, relationshipType, isCurrent, relationshiprecordId) <> (OrganisationsOrganisationlocationcrossrefRow.tupled, OrganisationsOrganisationlocationcrossrefRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(dateCreated), Rep.Some(lastUpdated), Rep.Some(locationId), Rep.Some(organisationId), Rep.Some(relationshipType), Rep.Some(isCurrent), Rep.Some(relationshiprecordId)).shaped.<>({r=>import r._; _1.map(_=> OrganisationsOrganisationlocationcrossrefRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get, _7.get, _8.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(serial), AutoInc, PrimaryKey */
    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    /** Database column date_created SqlType(timestamp) */
    val dateCreated: Rep[org.joda.time.LocalDateTime] = column[org.joda.time.LocalDateTime]("date_created")
    /** Database column last_updated SqlType(timestamp) */
    val lastUpdated: Rep[org.joda.time.LocalDateTime] = column[org.joda.time.LocalDateTime]("last_updated")
    /** Database column location_id SqlType(int4) */
    val locationId: Rep[Int] = column[Int]("location_id")
    /** Database column organisation_id SqlType(int4) */
    val organisationId: Rep[Int] = column[Int]("organisation_id")
    /** Database column relationship_type SqlType(varchar), Length(100,true) */
    val relationshipType: Rep[String] = column[String]("relationship_type", O.Length(100,varying=true))
    /** Database column is_current SqlType(bool) */
    val isCurrent: Rep[Boolean] = column[Boolean]("is_current")
    /** Database column relationshiprecord_id SqlType(int4) */
    val relationshiprecordId: Rep[Int] = column[Int]("relationshiprecord_id")

    /** Foreign key referencing LocationsLocation (database name locations_location_organisations_organisationlocationcrossre499) */
    lazy val locationsLocationFk = foreignKey("locations_location_organisations_organisationlocationcrossre499", locationId, LocationsLocation)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
    /** Foreign key referencing OrganisationsOrganisation (database name organisations_organisationlocationcrossref_organisation_id_fkey) */
    lazy val organisationsOrganisationFk = foreignKey("organisations_organisationlocationcrossref_organisation_id_fkey", organisationId, OrganisationsOrganisation)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
    /** Foreign key referencing SystemRelationshiprecord (database name system_relationshiprecord_organisations_organisationlocation278) */
    lazy val systemRelationshiprecordFk = foreignKey("system_relationshiprecord_organisations_organisationlocation278", relationshiprecordId, SystemRelationshiprecord)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
  }
  /** Collection-like TableQuery object for table OrganisationsOrganisationlocationcrossref */
  lazy val OrganisationsOrganisationlocationcrossref = new TableQuery(tag => new OrganisationsOrganisationlocationcrossref(tag))

  /** Entity class storing rows of table OrganisationsOrganisationthingcrossref
   *  @param id Database column id SqlType(serial), AutoInc, PrimaryKey
   *  @param dateCreated Database column date_created SqlType(timestamp)
   *  @param lastUpdated Database column last_updated SqlType(timestamp)
   *  @param thingId Database column thing_id SqlType(int4)
   *  @param organisationId Database column organisation_id SqlType(int4)
   *  @param relationshipType Database column relationship_type SqlType(varchar), Length(100,true)
   *  @param isCurrent Database column is_current SqlType(bool)
   *  @param relationshiprecordId Database column relationshiprecord_id SqlType(int4) */
  case class OrganisationsOrganisationthingcrossrefRow(id: Int, dateCreated: org.joda.time.LocalDateTime, lastUpdated: org.joda.time.LocalDateTime, thingId: Int, organisationId: Int, relationshipType: String, isCurrent: Boolean, relationshiprecordId: Int)
  /** GetResult implicit for fetching OrganisationsOrganisationthingcrossrefRow objects using plain SQL queries */
  implicit def GetResultOrganisationsOrganisationthingcrossrefRow(implicit e0: GR[Int], e1: GR[org.joda.time.LocalDateTime], e2: GR[String], e3: GR[Boolean]): GR[OrganisationsOrganisationthingcrossrefRow] = GR{
    prs => import prs._
    OrganisationsOrganisationthingcrossrefRow.tupled((<<[Int], <<[org.joda.time.LocalDateTime], <<[org.joda.time.LocalDateTime], <<[Int], <<[Int], <<[String], <<[Boolean], <<[Int]))
  }
  /** Table description of table organisations_organisationthingcrossref. Objects of this class serve as prototypes for rows in queries. */
  class OrganisationsOrganisationthingcrossref(_tableTag: Tag) extends Table[OrganisationsOrganisationthingcrossrefRow](_tableTag, Some("hat"), "organisations_organisationthingcrossref") {
    def * = (id, dateCreated, lastUpdated, thingId, organisationId, relationshipType, isCurrent, relationshiprecordId) <> (OrganisationsOrganisationthingcrossrefRow.tupled, OrganisationsOrganisationthingcrossrefRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(dateCreated), Rep.Some(lastUpdated), Rep.Some(thingId), Rep.Some(organisationId), Rep.Some(relationshipType), Rep.Some(isCurrent), Rep.Some(relationshiprecordId)).shaped.<>({r=>import r._; _1.map(_=> OrganisationsOrganisationthingcrossrefRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get, _7.get, _8.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(serial), AutoInc, PrimaryKey */
    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    /** Database column date_created SqlType(timestamp) */
    val dateCreated: Rep[org.joda.time.LocalDateTime] = column[org.joda.time.LocalDateTime]("date_created")
    /** Database column last_updated SqlType(timestamp) */
    val lastUpdated: Rep[org.joda.time.LocalDateTime] = column[org.joda.time.LocalDateTime]("last_updated")
    /** Database column thing_id SqlType(int4) */
    val thingId: Rep[Int] = column[Int]("thing_id")
    /** Database column organisation_id SqlType(int4) */
    val organisationId: Rep[Int] = column[Int]("organisation_id")
    /** Database column relationship_type SqlType(varchar), Length(100,true) */
    val relationshipType: Rep[String] = column[String]("relationship_type", O.Length(100,varying=true))
    /** Database column is_current SqlType(bool) */
    val isCurrent: Rep[Boolean] = column[Boolean]("is_current")
    /** Database column relationshiprecord_id SqlType(int4) */
    val relationshiprecordId: Rep[Int] = column[Int]("relationshiprecord_id")

    /** Foreign key referencing OrganisationsOrganisation (database name organisations_organisation_organisations_organisationthingcr474) */
    lazy val organisationsOrganisationFk = foreignKey("organisations_organisation_organisations_organisationthingcr474", organisationId, OrganisationsOrganisation)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
    /** Foreign key referencing SystemRelationshiprecord (database name system_relationshiprecord_organisations_organisationthingcro825) */
    lazy val systemRelationshiprecordFk = foreignKey("system_relationshiprecord_organisations_organisationthingcro825", relationshiprecordId, SystemRelationshiprecord)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
    /** Foreign key referencing ThingsThing (database name things_thing_organisations_organisationthingcrossref_fk) */
    lazy val thingsThingFk = foreignKey("things_thing_organisations_organisationthingcrossref_fk", thingId, ThingsThing)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
  }
  /** Collection-like TableQuery object for table OrganisationsOrganisationthingcrossref */
  lazy val OrganisationsOrganisationthingcrossref = new TableQuery(tag => new OrganisationsOrganisationthingcrossref(tag))

  /** Entity class storing rows of table OrganisationsOrganisationtoorganisationcrossref
   *  @param id Database column id SqlType(serial), AutoInc, PrimaryKey
   *  @param dateCreated Database column date_created SqlType(timestamp)
   *  @param lastUpdated Database column last_updated SqlType(timestamp)
   *  @param organisationOneId Database column organisation_one_id SqlType(int4)
   *  @param organisationTwoId Database column organisation_two_id SqlType(int4)
   *  @param relationshipType Database column relationship_type SqlType(varchar), Length(100,true)
   *  @param isCurrent Database column is_current SqlType(bool)
   *  @param relationshiprecordId Database column relationshiprecord_id SqlType(int4) */
  case class OrganisationsOrganisationtoorganisationcrossrefRow(id: Int, dateCreated: org.joda.time.LocalDateTime, lastUpdated: org.joda.time.LocalDateTime, organisationOneId: Int, organisationTwoId: Int, relationshipType: String, isCurrent: Boolean, relationshiprecordId: Int)
  /** GetResult implicit for fetching OrganisationsOrganisationtoorganisationcrossrefRow objects using plain SQL queries */
  implicit def GetResultOrganisationsOrganisationtoorganisationcrossrefRow(implicit e0: GR[Int], e1: GR[org.joda.time.LocalDateTime], e2: GR[String], e3: GR[Boolean]): GR[OrganisationsOrganisationtoorganisationcrossrefRow] = GR{
    prs => import prs._
    OrganisationsOrganisationtoorganisationcrossrefRow.tupled((<<[Int], <<[org.joda.time.LocalDateTime], <<[org.joda.time.LocalDateTime], <<[Int], <<[Int], <<[String], <<[Boolean], <<[Int]))
  }
  /** Table description of table organisations_organisationtoorganisationcrossref. Objects of this class serve as prototypes for rows in queries. */
  class OrganisationsOrganisationtoorganisationcrossref(_tableTag: Tag) extends Table[OrganisationsOrganisationtoorganisationcrossrefRow](_tableTag, Some("hat"), "organisations_organisationtoorganisationcrossref") {
    def * = (id, dateCreated, lastUpdated, organisationOneId, organisationTwoId, relationshipType, isCurrent, relationshiprecordId) <> (OrganisationsOrganisationtoorganisationcrossrefRow.tupled, OrganisationsOrganisationtoorganisationcrossrefRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(dateCreated), Rep.Some(lastUpdated), Rep.Some(organisationOneId), Rep.Some(organisationTwoId), Rep.Some(relationshipType), Rep.Some(isCurrent), Rep.Some(relationshiprecordId)).shaped.<>({r=>import r._; _1.map(_=> OrganisationsOrganisationtoorganisationcrossrefRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get, _7.get, _8.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(serial), AutoInc, PrimaryKey */
    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    /** Database column date_created SqlType(timestamp) */
    val dateCreated: Rep[org.joda.time.LocalDateTime] = column[org.joda.time.LocalDateTime]("date_created")
    /** Database column last_updated SqlType(timestamp) */
    val lastUpdated: Rep[org.joda.time.LocalDateTime] = column[org.joda.time.LocalDateTime]("last_updated")
    /** Database column organisation_one_id SqlType(int4) */
    val organisationOneId: Rep[Int] = column[Int]("organisation_one_id")
    /** Database column organisation_two_id SqlType(int4) */
    val organisationTwoId: Rep[Int] = column[Int]("organisation_two_id")
    /** Database column relationship_type SqlType(varchar), Length(100,true) */
    val relationshipType: Rep[String] = column[String]("relationship_type", O.Length(100,varying=true))
    /** Database column is_current SqlType(bool) */
    val isCurrent: Rep[Boolean] = column[Boolean]("is_current")
    /** Database column relationshiprecord_id SqlType(int4) */
    val relationshiprecordId: Rep[Int] = column[Int]("relationshiprecord_id")

    /** Foreign key referencing OrganisationsOrganisation (database name organisations_organisation_organisation_organisationtoorgani645) */
    lazy val organisationsOrganisationFk1 = foreignKey("organisations_organisation_organisation_organisationtoorgani645", organisationTwoId, OrganisationsOrganisation)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
    /** Foreign key referencing OrganisationsOrganisation (database name organisations_organisation_organisation_organisationtoorgani876) */
    lazy val organisationsOrganisationFk2 = foreignKey("organisations_organisation_organisation_organisationtoorgani876", organisationOneId, OrganisationsOrganisation)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
    /** Foreign key referencing SystemRelationshiprecord (database name system_relationshiprecord_organisations_organisationtoorgani310) */
    lazy val systemRelationshiprecordFk = foreignKey("system_relationshiprecord_organisations_organisationtoorgani310", relationshiprecordId, SystemRelationshiprecord)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
  }
  /** Collection-like TableQuery object for table OrganisationsOrganisationtoorganisationcrossref */
  lazy val OrganisationsOrganisationtoorganisationcrossref = new TableQuery(tag => new OrganisationsOrganisationtoorganisationcrossref(tag))

  /** Entity class storing rows of table OrganisationsSystempropertydynamiccrossref
   *  @param id Database column id SqlType(serial), AutoInc, PrimaryKey
   *  @param dateCreated Database column date_created SqlType(timestamp)
   *  @param lastUpdated Database column last_updated SqlType(timestamp)
   *  @param organisationId Database column organisation_id SqlType(int4)
   *  @param systemPropertyId Database column system_property_id SqlType(int4)
   *  @param fieldId Database column field_id SqlType(int4)
   *  @param relationshipType Database column relationship_type SqlType(varchar), Length(100,true)
   *  @param isCurrent Database column is_current SqlType(bool)
   *  @param propertyrecordId Database column propertyrecord_id SqlType(int4) */
  case class OrganisationsSystempropertydynamiccrossrefRow(id: Int, dateCreated: org.joda.time.LocalDateTime, lastUpdated: org.joda.time.LocalDateTime, organisationId: Int, systemPropertyId: Int, fieldId: Int, relationshipType: String, isCurrent: Boolean, propertyrecordId: Int)
  /** GetResult implicit for fetching OrganisationsSystempropertydynamiccrossrefRow objects using plain SQL queries */
  implicit def GetResultOrganisationsSystempropertydynamiccrossrefRow(implicit e0: GR[Int], e1: GR[org.joda.time.LocalDateTime], e2: GR[String], e3: GR[Boolean]): GR[OrganisationsSystempropertydynamiccrossrefRow] = GR{
    prs => import prs._
    OrganisationsSystempropertydynamiccrossrefRow.tupled((<<[Int], <<[org.joda.time.LocalDateTime], <<[org.joda.time.LocalDateTime], <<[Int], <<[Int], <<[Int], <<[String], <<[Boolean], <<[Int]))
  }
  /** Table description of table organisations_systempropertydynamiccrossref. Objects of this class serve as prototypes for rows in queries. */
  class OrganisationsSystempropertydynamiccrossref(_tableTag: Tag) extends Table[OrganisationsSystempropertydynamiccrossrefRow](_tableTag, Some("hat"), "organisations_systempropertydynamiccrossref") {
    def * = (id, dateCreated, lastUpdated, organisationId, systemPropertyId, fieldId, relationshipType, isCurrent, propertyrecordId) <> (OrganisationsSystempropertydynamiccrossrefRow.tupled, OrganisationsSystempropertydynamiccrossrefRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(dateCreated), Rep.Some(lastUpdated), Rep.Some(organisationId), Rep.Some(systemPropertyId), Rep.Some(fieldId), Rep.Some(relationshipType), Rep.Some(isCurrent), Rep.Some(propertyrecordId)).shaped.<>({r=>import r._; _1.map(_=> OrganisationsSystempropertydynamiccrossrefRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get, _7.get, _8.get, _9.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(serial), AutoInc, PrimaryKey */
    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    /** Database column date_created SqlType(timestamp) */
    val dateCreated: Rep[org.joda.time.LocalDateTime] = column[org.joda.time.LocalDateTime]("date_created")
    /** Database column last_updated SqlType(timestamp) */
    val lastUpdated: Rep[org.joda.time.LocalDateTime] = column[org.joda.time.LocalDateTime]("last_updated")
    /** Database column organisation_id SqlType(int4) */
    val organisationId: Rep[Int] = column[Int]("organisation_id")
    /** Database column system_property_id SqlType(int4) */
    val systemPropertyId: Rep[Int] = column[Int]("system_property_id")
    /** Database column field_id SqlType(int4) */
    val fieldId: Rep[Int] = column[Int]("field_id")
    /** Database column relationship_type SqlType(varchar), Length(100,true) */
    val relationshipType: Rep[String] = column[String]("relationship_type", O.Length(100,varying=true))
    /** Database column is_current SqlType(bool) */
    val isCurrent: Rep[Boolean] = column[Boolean]("is_current")
    /** Database column propertyrecord_id SqlType(int4) */
    val propertyrecordId: Rep[Int] = column[Int]("propertyrecord_id")

    /** Foreign key referencing DataField (database name data_field_organisations_systempropertydynamiccrossref_fk) */
    lazy val dataFieldFk = foreignKey("data_field_organisations_systempropertydynamiccrossref_fk", fieldId, DataField)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
    /** Foreign key referencing OrganisationsOrganisation (database name organisations_organisation_organisations_systempropertydynam75) */
    lazy val organisationsOrganisationFk = foreignKey("organisations_organisation_organisations_systempropertydynam75", organisationId, OrganisationsOrganisation)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
    /** Foreign key referencing SystemProperty (database name system_property_organisations_systempropertydynamiccrossref_fk) */
    lazy val systemPropertyFk = foreignKey("system_property_organisations_systempropertydynamiccrossref_fk", systemPropertyId, SystemProperty)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
    /** Foreign key referencing SystemPropertyrecord (database name property_record_organisations_systempropertydynamiccrossref_fk) */
    lazy val systemPropertyrecordFk = foreignKey("property_record_organisations_systempropertydynamiccrossref_fk", propertyrecordId, SystemPropertyrecord)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
  }
  /** Collection-like TableQuery object for table OrganisationsSystempropertydynamiccrossref */
  lazy val OrganisationsSystempropertydynamiccrossref = new TableQuery(tag => new OrganisationsSystempropertydynamiccrossref(tag))

  /** Entity class storing rows of table OrganisationsSystempropertystaticcrossref
   *  @param id Database column id SqlType(serial), AutoInc, PrimaryKey
   *  @param dateCreated Database column date_created SqlType(timestamp)
   *  @param lastUpdated Database column last_updated SqlType(timestamp)
   *  @param organisationId Database column organisation_id SqlType(int4)
   *  @param systemPropertyId Database column system_property_id SqlType(int4)
   *  @param recordId Database column record_id SqlType(int4)
   *  @param fieldId Database column field_id SqlType(int4)
   *  @param relationshipType Database column relationship_type SqlType(varchar), Length(100,true)
   *  @param isCurrent Database column is_current SqlType(bool)
   *  @param propertyrecordId Database column propertyrecord_id SqlType(int4) */
  case class OrganisationsSystempropertystaticcrossrefRow(id: Int, dateCreated: org.joda.time.LocalDateTime, lastUpdated: org.joda.time.LocalDateTime, organisationId: Int, systemPropertyId: Int, recordId: Int, fieldId: Int, relationshipType: String, isCurrent: Boolean, propertyrecordId: Int)
  /** GetResult implicit for fetching OrganisationsSystempropertystaticcrossrefRow objects using plain SQL queries */
  implicit def GetResultOrganisationsSystempropertystaticcrossrefRow(implicit e0: GR[Int], e1: GR[org.joda.time.LocalDateTime], e2: GR[String], e3: GR[Boolean]): GR[OrganisationsSystempropertystaticcrossrefRow] = GR{
    prs => import prs._
    OrganisationsSystempropertystaticcrossrefRow.tupled((<<[Int], <<[org.joda.time.LocalDateTime], <<[org.joda.time.LocalDateTime], <<[Int], <<[Int], <<[Int], <<[Int], <<[String], <<[Boolean], <<[Int]))
  }
  /** Table description of table organisations_systempropertystaticcrossref. Objects of this class serve as prototypes for rows in queries. */
  class OrganisationsSystempropertystaticcrossref(_tableTag: Tag) extends Table[OrganisationsSystempropertystaticcrossrefRow](_tableTag, Some("hat"), "organisations_systempropertystaticcrossref") {
    def * = (id, dateCreated, lastUpdated, organisationId, systemPropertyId, recordId, fieldId, relationshipType, isCurrent, propertyrecordId) <> (OrganisationsSystempropertystaticcrossrefRow.tupled, OrganisationsSystempropertystaticcrossrefRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(dateCreated), Rep.Some(lastUpdated), Rep.Some(organisationId), Rep.Some(systemPropertyId), Rep.Some(recordId), Rep.Some(fieldId), Rep.Some(relationshipType), Rep.Some(isCurrent), Rep.Some(propertyrecordId)).shaped.<>({r=>import r._; _1.map(_=> OrganisationsSystempropertystaticcrossrefRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get, _7.get, _8.get, _9.get, _10.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(serial), AutoInc, PrimaryKey */
    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    /** Database column date_created SqlType(timestamp) */
    val dateCreated: Rep[org.joda.time.LocalDateTime] = column[org.joda.time.LocalDateTime]("date_created")
    /** Database column last_updated SqlType(timestamp) */
    val lastUpdated: Rep[org.joda.time.LocalDateTime] = column[org.joda.time.LocalDateTime]("last_updated")
    /** Database column organisation_id SqlType(int4) */
    val organisationId: Rep[Int] = column[Int]("organisation_id")
    /** Database column system_property_id SqlType(int4) */
    val systemPropertyId: Rep[Int] = column[Int]("system_property_id")
    /** Database column record_id SqlType(int4) */
    val recordId: Rep[Int] = column[Int]("record_id")
    /** Database column field_id SqlType(int4) */
    val fieldId: Rep[Int] = column[Int]("field_id")
    /** Database column relationship_type SqlType(varchar), Length(100,true) */
    val relationshipType: Rep[String] = column[String]("relationship_type", O.Length(100,varying=true))
    /** Database column is_current SqlType(bool) */
    val isCurrent: Rep[Boolean] = column[Boolean]("is_current")
    /** Database column propertyrecord_id SqlType(int4) */
    val propertyrecordId: Rep[Int] = column[Int]("propertyrecord_id")

    /** Foreign key referencing DataField (database name data_field_organisations_systempropertystaticcrossref_fk) */
    lazy val dataFieldFk = foreignKey("data_field_organisations_systempropertystaticcrossref_fk", fieldId, DataField)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
    /** Foreign key referencing DataRecord (database name data_record_organisations_systempropertystaticcrossref_fk) */
    lazy val dataRecordFk = foreignKey("data_record_organisations_systempropertystaticcrossref_fk", recordId, DataRecord)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
    /** Foreign key referencing OrganisationsOrganisation (database name organisations_organisation_organisations_systempropertystati434) */
    lazy val organisationsOrganisationFk = foreignKey("organisations_organisation_organisations_systempropertystati434", organisationId, OrganisationsOrganisation)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
    /** Foreign key referencing SystemProperty (database name system_property_organisations_systempropertystaticcrossref_fk) */
    lazy val systemPropertyFk = foreignKey("system_property_organisations_systempropertystaticcrossref_fk", systemPropertyId, SystemProperty)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
    /** Foreign key referencing SystemPropertyrecord (database name property_record_organisations_systempropertystaticcrossref_fk) */
    lazy val systemPropertyrecordFk = foreignKey("property_record_organisations_systempropertystaticcrossref_fk", propertyrecordId, SystemPropertyrecord)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
  }
  /** Collection-like TableQuery object for table OrganisationsSystempropertystaticcrossref */
  lazy val OrganisationsSystempropertystaticcrossref = new TableQuery(tag => new OrganisationsSystempropertystaticcrossref(tag))

  /** Entity class storing rows of table OrganisationsSystemtypecrossref
   *  @param id Database column id SqlType(serial), AutoInc, PrimaryKey
   *  @param dateCreated Database column date_created SqlType(timestamp)
   *  @param lastUpdated Database column last_updated SqlType(timestamp)
   *  @param organisationId Database column organisation_id SqlType(int4)
   *  @param systemTypeId Database column system_type_id SqlType(int4)
   *  @param relationshipType Database column relationship_type SqlType(varchar), Length(100,true)
   *  @param isCurrent Database column is_current SqlType(bool) */
  case class OrganisationsSystemtypecrossrefRow(id: Int, dateCreated: org.joda.time.LocalDateTime, lastUpdated: org.joda.time.LocalDateTime, organisationId: Int, systemTypeId: Int, relationshipType: String, isCurrent: Boolean)
  /** GetResult implicit for fetching OrganisationsSystemtypecrossrefRow objects using plain SQL queries */
  implicit def GetResultOrganisationsSystemtypecrossrefRow(implicit e0: GR[Int], e1: GR[org.joda.time.LocalDateTime], e2: GR[String], e3: GR[Boolean]): GR[OrganisationsSystemtypecrossrefRow] = GR{
    prs => import prs._
    OrganisationsSystemtypecrossrefRow.tupled((<<[Int], <<[org.joda.time.LocalDateTime], <<[org.joda.time.LocalDateTime], <<[Int], <<[Int], <<[String], <<[Boolean]))
  }
  /** Table description of table organisations_systemtypecrossref. Objects of this class serve as prototypes for rows in queries. */
  class OrganisationsSystemtypecrossref(_tableTag: Tag) extends Table[OrganisationsSystemtypecrossrefRow](_tableTag, Some("hat"), "organisations_systemtypecrossref") {
    def * = (id, dateCreated, lastUpdated, organisationId, systemTypeId, relationshipType, isCurrent) <> (OrganisationsSystemtypecrossrefRow.tupled, OrganisationsSystemtypecrossrefRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(dateCreated), Rep.Some(lastUpdated), Rep.Some(organisationId), Rep.Some(systemTypeId), Rep.Some(relationshipType), Rep.Some(isCurrent)).shaped.<>({r=>import r._; _1.map(_=> OrganisationsSystemtypecrossrefRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get, _7.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(serial), AutoInc, PrimaryKey */
    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    /** Database column date_created SqlType(timestamp) */
    val dateCreated: Rep[org.joda.time.LocalDateTime] = column[org.joda.time.LocalDateTime]("date_created")
    /** Database column last_updated SqlType(timestamp) */
    val lastUpdated: Rep[org.joda.time.LocalDateTime] = column[org.joda.time.LocalDateTime]("last_updated")
    /** Database column organisation_id SqlType(int4) */
    val organisationId: Rep[Int] = column[Int]("organisation_id")
    /** Database column system_type_id SqlType(int4) */
    val systemTypeId: Rep[Int] = column[Int]("system_type_id")
    /** Database column relationship_type SqlType(varchar), Length(100,true) */
    val relationshipType: Rep[String] = column[String]("relationship_type", O.Length(100,varying=true))
    /** Database column is_current SqlType(bool) */
    val isCurrent: Rep[Boolean] = column[Boolean]("is_current")

    /** Foreign key referencing OrganisationsOrganisation (database name organisations_organisation_organisations_systemtypecrossref_fk) */
    lazy val organisationsOrganisationFk = foreignKey("organisations_organisation_organisations_systemtypecrossref_fk", organisationId, OrganisationsOrganisation)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
    /** Foreign key referencing SystemType (database name system_type_organisations_systemtypecrossref_fk) */
    lazy val systemTypeFk = foreignKey("system_type_organisations_systemtypecrossref_fk", systemTypeId, SystemType)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
  }
  /** Collection-like TableQuery object for table OrganisationsSystemtypecrossref */
  lazy val OrganisationsSystemtypecrossref = new TableQuery(tag => new OrganisationsSystemtypecrossref(tag))

  /** Entity class storing rows of table PeoplePerson
   *  @param id Database column id SqlType(serial), AutoInc, PrimaryKey
   *  @param dateCreated Database column date_created SqlType(timestamp)
   *  @param lastUpdated Database column last_updated SqlType(timestamp)
   *  @param name Database column name SqlType(varchar)
   *  @param personId Database column person_id SqlType(varchar), Length(36,true) */
  case class PeoplePersonRow(id: Int, dateCreated: org.joda.time.LocalDateTime, lastUpdated: org.joda.time.LocalDateTime, name: String, personId: String)
  /** GetResult implicit for fetching PeoplePersonRow objects using plain SQL queries */
  implicit def GetResultPeoplePersonRow(implicit e0: GR[Int], e1: GR[org.joda.time.LocalDateTime], e2: GR[String]): GR[PeoplePersonRow] = GR{
    prs => import prs._
    PeoplePersonRow.tupled((<<[Int], <<[org.joda.time.LocalDateTime], <<[org.joda.time.LocalDateTime], <<[String], <<[String]))
  }
  /** Table description of table people_person. Objects of this class serve as prototypes for rows in queries. */
  class PeoplePerson(_tableTag: Tag) extends Table[PeoplePersonRow](_tableTag, Some("hat"), "people_person") {
    def * = (id, dateCreated, lastUpdated, name, personId) <> (PeoplePersonRow.tupled, PeoplePersonRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(dateCreated), Rep.Some(lastUpdated), Rep.Some(name), Rep.Some(personId)).shaped.<>({r=>import r._; _1.map(_=> PeoplePersonRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(serial), AutoInc, PrimaryKey */
    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    /** Database column date_created SqlType(timestamp) */
    val dateCreated: Rep[org.joda.time.LocalDateTime] = column[org.joda.time.LocalDateTime]("date_created")
    /** Database column last_updated SqlType(timestamp) */
    val lastUpdated: Rep[org.joda.time.LocalDateTime] = column[org.joda.time.LocalDateTime]("last_updated")
    /** Database column name SqlType(varchar) */
    val name: Rep[String] = column[String]("name")
    /** Database column person_id SqlType(varchar), Length(36,true) */
    val personId: Rep[String] = column[String]("person_id", O.Length(36,varying=true))
  }
  /** Collection-like TableQuery object for table PeoplePerson */
  lazy val PeoplePerson = new TableQuery(tag => new PeoplePerson(tag))

  /** Entity class storing rows of table PeoplePersonlocationcrossref
   *  @param id Database column id SqlType(serial), AutoInc, PrimaryKey
   *  @param dateCreated Database column date_created SqlType(timestamp)
   *  @param lastUpdated Database column last_updated SqlType(timestamp)
   *  @param locationId Database column location_id SqlType(int4)
   *  @param personId Database column person_id SqlType(int4)
   *  @param relationshipType Database column relationship_type SqlType(varchar), Length(100,true)
   *  @param isCurrent Database column is_current SqlType(bool)
   *  @param relationshiprecordId Database column relationshiprecord_id SqlType(int4) */
  case class PeoplePersonlocationcrossrefRow(id: Int, dateCreated: org.joda.time.LocalDateTime, lastUpdated: org.joda.time.LocalDateTime, locationId: Int, personId: Int, relationshipType: String, isCurrent: Boolean, relationshiprecordId: Int)
  /** GetResult implicit for fetching PeoplePersonlocationcrossrefRow objects using plain SQL queries */
  implicit def GetResultPeoplePersonlocationcrossrefRow(implicit e0: GR[Int], e1: GR[org.joda.time.LocalDateTime], e2: GR[String], e3: GR[Boolean]): GR[PeoplePersonlocationcrossrefRow] = GR{
    prs => import prs._
    PeoplePersonlocationcrossrefRow.tupled((<<[Int], <<[org.joda.time.LocalDateTime], <<[org.joda.time.LocalDateTime], <<[Int], <<[Int], <<[String], <<[Boolean], <<[Int]))
  }
  /** Table description of table people_personlocationcrossref. Objects of this class serve as prototypes for rows in queries. */
  class PeoplePersonlocationcrossref(_tableTag: Tag) extends Table[PeoplePersonlocationcrossrefRow](_tableTag, Some("hat"), "people_personlocationcrossref") {
    def * = (id, dateCreated, lastUpdated, locationId, personId, relationshipType, isCurrent, relationshiprecordId) <> (PeoplePersonlocationcrossrefRow.tupled, PeoplePersonlocationcrossrefRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(dateCreated), Rep.Some(lastUpdated), Rep.Some(locationId), Rep.Some(personId), Rep.Some(relationshipType), Rep.Some(isCurrent), Rep.Some(relationshiprecordId)).shaped.<>({r=>import r._; _1.map(_=> PeoplePersonlocationcrossrefRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get, _7.get, _8.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(serial), AutoInc, PrimaryKey */
    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    /** Database column date_created SqlType(timestamp) */
    val dateCreated: Rep[org.joda.time.LocalDateTime] = column[org.joda.time.LocalDateTime]("date_created")
    /** Database column last_updated SqlType(timestamp) */
    val lastUpdated: Rep[org.joda.time.LocalDateTime] = column[org.joda.time.LocalDateTime]("last_updated")
    /** Database column location_id SqlType(int4) */
    val locationId: Rep[Int] = column[Int]("location_id")
    /** Database column person_id SqlType(int4) */
    val personId: Rep[Int] = column[Int]("person_id")
    /** Database column relationship_type SqlType(varchar), Length(100,true) */
    val relationshipType: Rep[String] = column[String]("relationship_type", O.Length(100,varying=true))
    /** Database column is_current SqlType(bool) */
    val isCurrent: Rep[Boolean] = column[Boolean]("is_current")
    /** Database column relationshiprecord_id SqlType(int4) */
    val relationshiprecordId: Rep[Int] = column[Int]("relationshiprecord_id")

    /** Foreign key referencing LocationsLocation (database name locations_locationpersoncrossref_location_id_fkey) */
    lazy val locationsLocationFk = foreignKey("locations_locationpersoncrossref_location_id_fkey", locationId, LocationsLocation)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
    /** Foreign key referencing PeoplePerson (database name person_id_refs_id) */
    lazy val peoplePersonFk = foreignKey("person_id_refs_id", personId, PeoplePerson)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
    /** Foreign key referencing SystemRelationshiprecord (database name system_relationshiprecord_people_personlocationcrossref_fk) */
    lazy val systemRelationshiprecordFk = foreignKey("system_relationshiprecord_people_personlocationcrossref_fk", relationshiprecordId, SystemRelationshiprecord)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
  }
  /** Collection-like TableQuery object for table PeoplePersonlocationcrossref */
  lazy val PeoplePersonlocationcrossref = new TableQuery(tag => new PeoplePersonlocationcrossref(tag))

  /** Entity class storing rows of table PeoplePersonorganisationcrossref
   *  @param id Database column id SqlType(serial), AutoInc, PrimaryKey
   *  @param dateCreated Database column date_created SqlType(timestamp)
   *  @param lastUpdated Database column last_updated SqlType(timestamp)
   *  @param organisationId Database column organisation_id SqlType(int4)
   *  @param personId Database column person_id SqlType(int4)
   *  @param relationshipType Database column relationship_type SqlType(varchar), Length(100,true)
   *  @param isCurrent Database column is_current SqlType(bool)
   *  @param relationshiprecordId Database column relationshiprecord_id SqlType(int4) */
  case class PeoplePersonorganisationcrossrefRow(id: Int, dateCreated: org.joda.time.LocalDateTime, lastUpdated: org.joda.time.LocalDateTime, organisationId: Int, personId: Int, relationshipType: String, isCurrent: Boolean, relationshiprecordId: Int)
  /** GetResult implicit for fetching PeoplePersonorganisationcrossrefRow objects using plain SQL queries */
  implicit def GetResultPeoplePersonorganisationcrossrefRow(implicit e0: GR[Int], e1: GR[org.joda.time.LocalDateTime], e2: GR[String], e3: GR[Boolean]): GR[PeoplePersonorganisationcrossrefRow] = GR{
    prs => import prs._
    PeoplePersonorganisationcrossrefRow.tupled((<<[Int], <<[org.joda.time.LocalDateTime], <<[org.joda.time.LocalDateTime], <<[Int], <<[Int], <<[String], <<[Boolean], <<[Int]))
  }
  /** Table description of table people_personorganisationcrossref. Objects of this class serve as prototypes for rows in queries. */
  class PeoplePersonorganisationcrossref(_tableTag: Tag) extends Table[PeoplePersonorganisationcrossrefRow](_tableTag, Some("hat"), "people_personorganisationcrossref") {
    def * = (id, dateCreated, lastUpdated, organisationId, personId, relationshipType, isCurrent, relationshiprecordId) <> (PeoplePersonorganisationcrossrefRow.tupled, PeoplePersonorganisationcrossrefRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(dateCreated), Rep.Some(lastUpdated), Rep.Some(organisationId), Rep.Some(personId), Rep.Some(relationshipType), Rep.Some(isCurrent), Rep.Some(relationshiprecordId)).shaped.<>({r=>import r._; _1.map(_=> PeoplePersonorganisationcrossrefRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get, _7.get, _8.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(serial), AutoInc, PrimaryKey */
    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    /** Database column date_created SqlType(timestamp) */
    val dateCreated: Rep[org.joda.time.LocalDateTime] = column[org.joda.time.LocalDateTime]("date_created")
    /** Database column last_updated SqlType(timestamp) */
    val lastUpdated: Rep[org.joda.time.LocalDateTime] = column[org.joda.time.LocalDateTime]("last_updated")
    /** Database column organisation_id SqlType(int4) */
    val organisationId: Rep[Int] = column[Int]("organisation_id")
    /** Database column person_id SqlType(int4) */
    val personId: Rep[Int] = column[Int]("person_id")
    /** Database column relationship_type SqlType(varchar), Length(100,true) */
    val relationshipType: Rep[String] = column[String]("relationship_type", O.Length(100,varying=true))
    /** Database column is_current SqlType(bool) */
    val isCurrent: Rep[Boolean] = column[Boolean]("is_current")
    /** Database column relationshiprecord_id SqlType(int4) */
    val relationshiprecordId: Rep[Int] = column[Int]("relationshiprecord_id")

    /** Foreign key referencing OrganisationsOrganisation (database name organisation_id_refs_id_fk) */
    lazy val organisationsOrganisationFk = foreignKey("organisation_id_refs_id_fk", organisationId, OrganisationsOrganisation)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
    /** Foreign key referencing PeoplePerson (database name person_id_refs_id_fk) */
    lazy val peoplePersonFk = foreignKey("person_id_refs_id_fk", personId, PeoplePerson)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
    /** Foreign key referencing SystemRelationshiprecord (database name system_relationshiprecord_people_personorganisationcrossref_fk) */
    lazy val systemRelationshiprecordFk = foreignKey("system_relationshiprecord_people_personorganisationcrossref_fk", relationshiprecordId, SystemRelationshiprecord)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
  }
  /** Collection-like TableQuery object for table PeoplePersonorganisationcrossref */
  lazy val PeoplePersonorganisationcrossref = new TableQuery(tag => new PeoplePersonorganisationcrossref(tag))

  /** Entity class storing rows of table PeoplePersontopersoncrossref
   *  @param id Database column id SqlType(serial), AutoInc, PrimaryKey
   *  @param dateCreated Database column date_created SqlType(timestamp)
   *  @param lastUpdated Database column last_updated SqlType(timestamp)
   *  @param personOneId Database column person_one_id SqlType(int4)
   *  @param personTwoId Database column person_two_id SqlType(int4)
   *  @param relationshipTypeId Database column relationship_type_id SqlType(int4)
   *  @param isCurrent Database column is_current SqlType(bool)
   *  @param relationshiprecordId Database column relationshiprecord_id SqlType(int4) */
  case class PeoplePersontopersoncrossrefRow(id: Int, dateCreated: org.joda.time.LocalDateTime, lastUpdated: org.joda.time.LocalDateTime, personOneId: Int, personTwoId: Int, relationshipTypeId: Int, isCurrent: Boolean, relationshiprecordId: Int)
  /** GetResult implicit for fetching PeoplePersontopersoncrossrefRow objects using plain SQL queries */
  implicit def GetResultPeoplePersontopersoncrossrefRow(implicit e0: GR[Int], e1: GR[org.joda.time.LocalDateTime], e2: GR[Boolean]): GR[PeoplePersontopersoncrossrefRow] = GR{
    prs => import prs._
    PeoplePersontopersoncrossrefRow.tupled((<<[Int], <<[org.joda.time.LocalDateTime], <<[org.joda.time.LocalDateTime], <<[Int], <<[Int], <<[Int], <<[Boolean], <<[Int]))
  }
  /** Table description of table people_persontopersoncrossref. Objects of this class serve as prototypes for rows in queries. */
  class PeoplePersontopersoncrossref(_tableTag: Tag) extends Table[PeoplePersontopersoncrossrefRow](_tableTag, Some("hat"), "people_persontopersoncrossref") {
    def * = (id, dateCreated, lastUpdated, personOneId, personTwoId, relationshipTypeId, isCurrent, relationshiprecordId) <> (PeoplePersontopersoncrossrefRow.tupled, PeoplePersontopersoncrossrefRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(dateCreated), Rep.Some(lastUpdated), Rep.Some(personOneId), Rep.Some(personTwoId), Rep.Some(relationshipTypeId), Rep.Some(isCurrent), Rep.Some(relationshiprecordId)).shaped.<>({r=>import r._; _1.map(_=> PeoplePersontopersoncrossrefRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get, _7.get, _8.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(serial), AutoInc, PrimaryKey */
    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    /** Database column date_created SqlType(timestamp) */
    val dateCreated: Rep[org.joda.time.LocalDateTime] = column[org.joda.time.LocalDateTime]("date_created")
    /** Database column last_updated SqlType(timestamp) */
    val lastUpdated: Rep[org.joda.time.LocalDateTime] = column[org.joda.time.LocalDateTime]("last_updated")
    /** Database column person_one_id SqlType(int4) */
    val personOneId: Rep[Int] = column[Int]("person_one_id")
    /** Database column person_two_id SqlType(int4) */
    val personTwoId: Rep[Int] = column[Int]("person_two_id")
    /** Database column relationship_type_id SqlType(int4) */
    val relationshipTypeId: Rep[Int] = column[Int]("relationship_type_id")
    /** Database column is_current SqlType(bool) */
    val isCurrent: Rep[Boolean] = column[Boolean]("is_current")
    /** Database column relationshiprecord_id SqlType(int4) */
    val relationshiprecordId: Rep[Int] = column[Int]("relationshiprecord_id")

    /** Foreign key referencing PeoplePerson (database name people_persontopersoncrossref_person_one_id_fkey) */
    lazy val peoplePersonFk1 = foreignKey("people_persontopersoncrossref_person_one_id_fkey", personOneId, PeoplePerson)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
    /** Foreign key referencing PeoplePerson (database name people_persontopersoncrossref_person_two_id_fkey) */
    lazy val peoplePersonFk2 = foreignKey("people_persontopersoncrossref_person_two_id_fkey", personTwoId, PeoplePerson)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
    /** Foreign key referencing PeoplePersontopersonrelationshiptype (database name relationship_type_id_refs_id_fk) */
    lazy val peoplePersontopersonrelationshiptypeFk = foreignKey("relationship_type_id_refs_id_fk", relationshipTypeId, PeoplePersontopersonrelationshiptype)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
    /** Foreign key referencing SystemRelationshiprecord (database name system_relationshiprecord_people_persontopersoncrossref_fk) */
    lazy val systemRelationshiprecordFk = foreignKey("system_relationshiprecord_people_persontopersoncrossref_fk", relationshiprecordId, SystemRelationshiprecord)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
  }
  /** Collection-like TableQuery object for table PeoplePersontopersoncrossref */
  lazy val PeoplePersontopersoncrossref = new TableQuery(tag => new PeoplePersontopersoncrossref(tag))

  /** Entity class storing rows of table PeoplePersontopersonrelationshiptype
   *  @param id Database column id SqlType(serial), AutoInc, PrimaryKey
   *  @param dateCreated Database column date_created SqlType(timestamp)
   *  @param lastUpdated Database column last_updated SqlType(timestamp)
   *  @param name Database column name SqlType(varchar), Length(100,true)
   *  @param description Database column description SqlType(text), Default(None) */
  case class PeoplePersontopersonrelationshiptypeRow(id: Int, dateCreated: org.joda.time.LocalDateTime, lastUpdated: org.joda.time.LocalDateTime, name: String, description: Option[String] = None)
  /** GetResult implicit for fetching PeoplePersontopersonrelationshiptypeRow objects using plain SQL queries */
  implicit def GetResultPeoplePersontopersonrelationshiptypeRow(implicit e0: GR[Int], e1: GR[org.joda.time.LocalDateTime], e2: GR[String], e3: GR[Option[String]]): GR[PeoplePersontopersonrelationshiptypeRow] = GR{
    prs => import prs._
    PeoplePersontopersonrelationshiptypeRow.tupled((<<[Int], <<[org.joda.time.LocalDateTime], <<[org.joda.time.LocalDateTime], <<[String], <<?[String]))
  }
  /** Table description of table people_persontopersonrelationshiptype. Objects of this class serve as prototypes for rows in queries. */
  class PeoplePersontopersonrelationshiptype(_tableTag: Tag) extends Table[PeoplePersontopersonrelationshiptypeRow](_tableTag, Some("hat"), "people_persontopersonrelationshiptype") {
    def * = (id, dateCreated, lastUpdated, name, description) <> (PeoplePersontopersonrelationshiptypeRow.tupled, PeoplePersontopersonrelationshiptypeRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(dateCreated), Rep.Some(lastUpdated), Rep.Some(name), description).shaped.<>({r=>import r._; _1.map(_=> PeoplePersontopersonrelationshiptypeRow.tupled((_1.get, _2.get, _3.get, _4.get, _5)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(serial), AutoInc, PrimaryKey */
    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    /** Database column date_created SqlType(timestamp) */
    val dateCreated: Rep[org.joda.time.LocalDateTime] = column[org.joda.time.LocalDateTime]("date_created")
    /** Database column last_updated SqlType(timestamp) */
    val lastUpdated: Rep[org.joda.time.LocalDateTime] = column[org.joda.time.LocalDateTime]("last_updated")
    /** Database column name SqlType(varchar), Length(100,true) */
    val name: Rep[String] = column[String]("name", O.Length(100,varying=true))
    /** Database column description SqlType(text), Default(None) */
    val description: Rep[Option[String]] = column[Option[String]]("description", O.Default(None))
  }
  /** Collection-like TableQuery object for table PeoplePersontopersonrelationshiptype */
  lazy val PeoplePersontopersonrelationshiptype = new TableQuery(tag => new PeoplePersontopersonrelationshiptype(tag))

  /** Entity class storing rows of table PeopleSystempropertydynamiccrossref
   *  @param id Database column id SqlType(serial), AutoInc, PrimaryKey
   *  @param dateCreated Database column date_created SqlType(timestamp)
   *  @param lastUpdated Database column last_updated SqlType(timestamp)
   *  @param personId Database column person_id SqlType(int4)
   *  @param systemPropertyId Database column system_property_id SqlType(int4)
   *  @param fieldId Database column field_id SqlType(int4)
   *  @param relationshipType Database column relationship_type SqlType(varchar), Length(100,true)
   *  @param isCurrent Database column is_current SqlType(bool)
   *  @param propertyrecordId Database column propertyrecord_id SqlType(int4) */
  case class PeopleSystempropertydynamiccrossrefRow(id: Int, dateCreated: org.joda.time.LocalDateTime, lastUpdated: org.joda.time.LocalDateTime, personId: Int, systemPropertyId: Int, fieldId: Int, relationshipType: String, isCurrent: Boolean, propertyrecordId: Int)
  /** GetResult implicit for fetching PeopleSystempropertydynamiccrossrefRow objects using plain SQL queries */
  implicit def GetResultPeopleSystempropertydynamiccrossrefRow(implicit e0: GR[Int], e1: GR[org.joda.time.LocalDateTime], e2: GR[String], e3: GR[Boolean]): GR[PeopleSystempropertydynamiccrossrefRow] = GR{
    prs => import prs._
    PeopleSystempropertydynamiccrossrefRow.tupled((<<[Int], <<[org.joda.time.LocalDateTime], <<[org.joda.time.LocalDateTime], <<[Int], <<[Int], <<[Int], <<[String], <<[Boolean], <<[Int]))
  }
  /** Table description of table people_systempropertydynamiccrossref. Objects of this class serve as prototypes for rows in queries. */
  class PeopleSystempropertydynamiccrossref(_tableTag: Tag) extends Table[PeopleSystempropertydynamiccrossrefRow](_tableTag, Some("hat"), "people_systempropertydynamiccrossref") {
    def * = (id, dateCreated, lastUpdated, personId, systemPropertyId, fieldId, relationshipType, isCurrent, propertyrecordId) <> (PeopleSystempropertydynamiccrossrefRow.tupled, PeopleSystempropertydynamiccrossrefRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(dateCreated), Rep.Some(lastUpdated), Rep.Some(personId), Rep.Some(systemPropertyId), Rep.Some(fieldId), Rep.Some(relationshipType), Rep.Some(isCurrent), Rep.Some(propertyrecordId)).shaped.<>({r=>import r._; _1.map(_=> PeopleSystempropertydynamiccrossrefRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get, _7.get, _8.get, _9.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(serial), AutoInc, PrimaryKey */
    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    /** Database column date_created SqlType(timestamp) */
    val dateCreated: Rep[org.joda.time.LocalDateTime] = column[org.joda.time.LocalDateTime]("date_created")
    /** Database column last_updated SqlType(timestamp) */
    val lastUpdated: Rep[org.joda.time.LocalDateTime] = column[org.joda.time.LocalDateTime]("last_updated")
    /** Database column person_id SqlType(int4) */
    val personId: Rep[Int] = column[Int]("person_id")
    /** Database column system_property_id SqlType(int4) */
    val systemPropertyId: Rep[Int] = column[Int]("system_property_id")
    /** Database column field_id SqlType(int4) */
    val fieldId: Rep[Int] = column[Int]("field_id")
    /** Database column relationship_type SqlType(varchar), Length(100,true) */
    val relationshipType: Rep[String] = column[String]("relationship_type", O.Length(100,varying=true))
    /** Database column is_current SqlType(bool) */
    val isCurrent: Rep[Boolean] = column[Boolean]("is_current")
    /** Database column propertyrecord_id SqlType(int4) */
    val propertyrecordId: Rep[Int] = column[Int]("propertyrecord_id")

    /** Foreign key referencing DataField (database name data_field_people_systempropertydynamiccrossref_fk) */
    lazy val dataFieldFk = foreignKey("data_field_people_systempropertydynamiccrossref_fk", fieldId, DataField)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
    /** Foreign key referencing PeoplePerson (database name people_person_people_systempropertydynamiccrossref_fk) */
    lazy val peoplePersonFk = foreignKey("people_person_people_systempropertydynamiccrossref_fk", personId, PeoplePerson)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
    /** Foreign key referencing SystemProperty (database name system_property_people_systempropertydynamiccrossref_fk) */
    lazy val systemPropertyFk = foreignKey("system_property_people_systempropertydynamiccrossref_fk", systemPropertyId, SystemProperty)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
    /** Foreign key referencing SystemPropertyrecord (database name property_record_people_systempropertydynamiccrossref_fk) */
    lazy val systemPropertyrecordFk = foreignKey("property_record_people_systempropertydynamiccrossref_fk", propertyrecordId, SystemPropertyrecord)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
  }
  /** Collection-like TableQuery object for table PeopleSystempropertydynamiccrossref */
  lazy val PeopleSystempropertydynamiccrossref = new TableQuery(tag => new PeopleSystempropertydynamiccrossref(tag))

  /** Entity class storing rows of table PeopleSystempropertystaticcrossref
   *  @param id Database column id SqlType(serial), AutoInc, PrimaryKey
   *  @param dateCreated Database column date_created SqlType(timestamp)
   *  @param lastUpdated Database column last_updated SqlType(timestamp)
   *  @param personId Database column person_id SqlType(int4)
   *  @param systemPropertyId Database column system_property_id SqlType(int4)
   *  @param recordId Database column record_id SqlType(int4)
   *  @param fieldId Database column field_id SqlType(int4)
   *  @param relationshipType Database column relationship_type SqlType(varchar), Length(100,true)
   *  @param isCurrent Database column is_current SqlType(bool)
   *  @param propertyrecordId Database column propertyrecord_id SqlType(int4) */
  case class PeopleSystempropertystaticcrossrefRow(id: Int, dateCreated: org.joda.time.LocalDateTime, lastUpdated: org.joda.time.LocalDateTime, personId: Int, systemPropertyId: Int, recordId: Int, fieldId: Int, relationshipType: String, isCurrent: Boolean, propertyrecordId: Int)
  /** GetResult implicit for fetching PeopleSystempropertystaticcrossrefRow objects using plain SQL queries */
  implicit def GetResultPeopleSystempropertystaticcrossrefRow(implicit e0: GR[Int], e1: GR[org.joda.time.LocalDateTime], e2: GR[String], e3: GR[Boolean]): GR[PeopleSystempropertystaticcrossrefRow] = GR{
    prs => import prs._
    PeopleSystempropertystaticcrossrefRow.tupled((<<[Int], <<[org.joda.time.LocalDateTime], <<[org.joda.time.LocalDateTime], <<[Int], <<[Int], <<[Int], <<[Int], <<[String], <<[Boolean], <<[Int]))
  }
  /** Table description of table people_systempropertystaticcrossref. Objects of this class serve as prototypes for rows in queries. */
  class PeopleSystempropertystaticcrossref(_tableTag: Tag) extends Table[PeopleSystempropertystaticcrossrefRow](_tableTag, Some("hat"), "people_systempropertystaticcrossref") {
    def * = (id, dateCreated, lastUpdated, personId, systemPropertyId, recordId, fieldId, relationshipType, isCurrent, propertyrecordId) <> (PeopleSystempropertystaticcrossrefRow.tupled, PeopleSystempropertystaticcrossrefRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(dateCreated), Rep.Some(lastUpdated), Rep.Some(personId), Rep.Some(systemPropertyId), Rep.Some(recordId), Rep.Some(fieldId), Rep.Some(relationshipType), Rep.Some(isCurrent), Rep.Some(propertyrecordId)).shaped.<>({r=>import r._; _1.map(_=> PeopleSystempropertystaticcrossrefRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get, _7.get, _8.get, _9.get, _10.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(serial), AutoInc, PrimaryKey */
    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    /** Database column date_created SqlType(timestamp) */
    val dateCreated: Rep[org.joda.time.LocalDateTime] = column[org.joda.time.LocalDateTime]("date_created")
    /** Database column last_updated SqlType(timestamp) */
    val lastUpdated: Rep[org.joda.time.LocalDateTime] = column[org.joda.time.LocalDateTime]("last_updated")
    /** Database column person_id SqlType(int4) */
    val personId: Rep[Int] = column[Int]("person_id")
    /** Database column system_property_id SqlType(int4) */
    val systemPropertyId: Rep[Int] = column[Int]("system_property_id")
    /** Database column record_id SqlType(int4) */
    val recordId: Rep[Int] = column[Int]("record_id")
    /** Database column field_id SqlType(int4) */
    val fieldId: Rep[Int] = column[Int]("field_id")
    /** Database column relationship_type SqlType(varchar), Length(100,true) */
    val relationshipType: Rep[String] = column[String]("relationship_type", O.Length(100,varying=true))
    /** Database column is_current SqlType(bool) */
    val isCurrent: Rep[Boolean] = column[Boolean]("is_current")
    /** Database column propertyrecord_id SqlType(int4) */
    val propertyrecordId: Rep[Int] = column[Int]("propertyrecord_id")

    /** Foreign key referencing DataField (database name data_field_people_systempropertystaticcrossref_fk) */
    lazy val dataFieldFk = foreignKey("data_field_people_systempropertystaticcrossref_fk", fieldId, DataField)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
    /** Foreign key referencing DataRecord (database name data_record_people_systempropertystaticcrossref_fk) */
    lazy val dataRecordFk = foreignKey("data_record_people_systempropertystaticcrossref_fk", recordId, DataRecord)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
    /** Foreign key referencing PeoplePerson (database name people_person_people_systempropertystaticcrossref_fk) */
    lazy val peoplePersonFk = foreignKey("people_person_people_systempropertystaticcrossref_fk", personId, PeoplePerson)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
    /** Foreign key referencing SystemProperty (database name system_property_people_systempropertystaticcrossref_fk) */
    lazy val systemPropertyFk = foreignKey("system_property_people_systempropertystaticcrossref_fk", systemPropertyId, SystemProperty)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
    /** Foreign key referencing SystemPropertyrecord (database name property_record_people_systempropertystaticcrossref_fk) */
    lazy val systemPropertyrecordFk = foreignKey("property_record_people_systempropertystaticcrossref_fk", propertyrecordId, SystemPropertyrecord)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
  }
  /** Collection-like TableQuery object for table PeopleSystempropertystaticcrossref */
  lazy val PeopleSystempropertystaticcrossref = new TableQuery(tag => new PeopleSystempropertystaticcrossref(tag))

  /** Entity class storing rows of table PeopleSystemtypecrossref
   *  @param id Database column id SqlType(serial), AutoInc, PrimaryKey
   *  @param dateCreated Database column date_created SqlType(timestamp)
   *  @param lastUpdated Database column last_updated SqlType(timestamp)
   *  @param usersId Database column users_id SqlType(int4)
   *  @param systemTypeId Database column system_type_id SqlType(int4)
   *  @param relationshipType Database column relationship_type SqlType(varchar), Length(100,true)
   *  @param isCurrent Database column is_current SqlType(bool) */
  case class PeopleSystemtypecrossrefRow(id: Int, dateCreated: org.joda.time.LocalDateTime, lastUpdated: org.joda.time.LocalDateTime, usersId: Int, systemTypeId: Int, relationshipType: String, isCurrent: Boolean)
  /** GetResult implicit for fetching PeopleSystemtypecrossrefRow objects using plain SQL queries */
  implicit def GetResultPeopleSystemtypecrossrefRow(implicit e0: GR[Int], e1: GR[org.joda.time.LocalDateTime], e2: GR[String], e3: GR[Boolean]): GR[PeopleSystemtypecrossrefRow] = GR{
    prs => import prs._
    PeopleSystemtypecrossrefRow.tupled((<<[Int], <<[org.joda.time.LocalDateTime], <<[org.joda.time.LocalDateTime], <<[Int], <<[Int], <<[String], <<[Boolean]))
  }
  /** Table description of table people_systemtypecrossref. Objects of this class serve as prototypes for rows in queries. */
  class PeopleSystemtypecrossref(_tableTag: Tag) extends Table[PeopleSystemtypecrossrefRow](_tableTag, Some("hat"), "people_systemtypecrossref") {
    def * = (id, dateCreated, lastUpdated, usersId, systemTypeId, relationshipType, isCurrent) <> (PeopleSystemtypecrossrefRow.tupled, PeopleSystemtypecrossrefRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(dateCreated), Rep.Some(lastUpdated), Rep.Some(usersId), Rep.Some(systemTypeId), Rep.Some(relationshipType), Rep.Some(isCurrent)).shaped.<>({r=>import r._; _1.map(_=> PeopleSystemtypecrossrefRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get, _7.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(serial), AutoInc, PrimaryKey */
    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    /** Database column date_created SqlType(timestamp) */
    val dateCreated: Rep[org.joda.time.LocalDateTime] = column[org.joda.time.LocalDateTime]("date_created")
    /** Database column last_updated SqlType(timestamp) */
    val lastUpdated: Rep[org.joda.time.LocalDateTime] = column[org.joda.time.LocalDateTime]("last_updated")
    /** Database column users_id SqlType(int4) */
    val usersId: Rep[Int] = column[Int]("users_id")
    /** Database column system_type_id SqlType(int4) */
    val systemTypeId: Rep[Int] = column[Int]("system_type_id")
    /** Database column relationship_type SqlType(varchar), Length(100,true) */
    val relationshipType: Rep[String] = column[String]("relationship_type", O.Length(100,varying=true))
    /** Database column is_current SqlType(bool) */
    val isCurrent: Rep[Boolean] = column[Boolean]("is_current")

    /** Foreign key referencing PeoplePerson (database name people_person_people_systemtypecrossref_fk) */
    lazy val peoplePersonFk = foreignKey("people_person_people_systemtypecrossref_fk", usersId, PeoplePerson)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
    /** Foreign key referencing SystemType (database name system_type_people_systemtypecrossref_fk) */
    lazy val systemTypeFk = foreignKey("system_type_people_systemtypecrossref_fk", systemTypeId, SystemType)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
  }
  /** Collection-like TableQuery object for table PeopleSystemtypecrossref */
  lazy val PeopleSystemtypecrossref = new TableQuery(tag => new PeopleSystemtypecrossref(tag))

  /** Entity class storing rows of table StatsDataDebitClessBundleRecords
   *  @param recordId Database column record_id SqlType(serial), AutoInc, PrimaryKey
   *  @param dateCreated Database column date_created SqlType(timestamp)
   *  @param bundleContextlessId Database column bundle_contextless_id SqlType(int4)
   *  @param dataDebitOperation Database column data_debit_operation SqlType(int4)
   *  @param recordCount Database column record_count SqlType(int4), Default(0) */
  case class StatsDataDebitClessBundleRecordsRow(recordId: Int, dateCreated: org.joda.time.LocalDateTime, bundleContextlessId: Int, dataDebitOperation: Int, recordCount: Int = 0)
  /** GetResult implicit for fetching StatsDataDebitClessBundleRecordsRow objects using plain SQL queries */
  implicit def GetResultStatsDataDebitClessBundleRecordsRow(implicit e0: GR[Int], e1: GR[org.joda.time.LocalDateTime]): GR[StatsDataDebitClessBundleRecordsRow] = GR{
    prs => import prs._
    StatsDataDebitClessBundleRecordsRow.tupled((<<[Int], <<[org.joda.time.LocalDateTime], <<[Int], <<[Int], <<[Int]))
  }
  /** Table description of table stats_data_debit_cless_bundle_records. Objects of this class serve as prototypes for rows in queries. */
  class StatsDataDebitClessBundleRecords(_tableTag: Tag) extends Table[StatsDataDebitClessBundleRecordsRow](_tableTag, Some("hat"), "stats_data_debit_cless_bundle_records") {
    def * = (recordId, dateCreated, bundleContextlessId, dataDebitOperation, recordCount) <> (StatsDataDebitClessBundleRecordsRow.tupled, StatsDataDebitClessBundleRecordsRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(recordId), Rep.Some(dateCreated), Rep.Some(bundleContextlessId), Rep.Some(dataDebitOperation), Rep.Some(recordCount)).shaped.<>({r=>import r._; _1.map(_=> StatsDataDebitClessBundleRecordsRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column record_id SqlType(serial), AutoInc, PrimaryKey */
    val recordId: Rep[Int] = column[Int]("record_id", O.AutoInc, O.PrimaryKey)
    /** Database column date_created SqlType(timestamp) */
    val dateCreated: Rep[org.joda.time.LocalDateTime] = column[org.joda.time.LocalDateTime]("date_created")
    /** Database column bundle_contextless_id SqlType(int4) */
    val bundleContextlessId: Rep[Int] = column[Int]("bundle_contextless_id")
    /** Database column data_debit_operation SqlType(int4) */
    val dataDebitOperation: Rep[Int] = column[Int]("data_debit_operation")
    /** Database column record_count SqlType(int4), Default(0) */
    val recordCount: Rep[Int] = column[Int]("record_count", O.Default(0))

    /** Foreign key referencing StatsDataDebitOperation (database name stats_data_debit_cless_bundle_records_data_debit_operation_fkey) */
    lazy val statsDataDebitOperationFk = foreignKey("stats_data_debit_cless_bundle_records_data_debit_operation_fkey", dataDebitOperation, StatsDataDebitOperation)(r => r.recordId, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
  }
  /** Collection-like TableQuery object for table StatsDataDebitClessBundleRecords */
  lazy val StatsDataDebitClessBundleRecords = new TableQuery(tag => new StatsDataDebitClessBundleRecords(tag))

  /** Entity class storing rows of table StatsDataDebitDataFieldAccess
   *  @param recordId Database column record_id SqlType(serial), AutoInc, PrimaryKey
   *  @param dateCreated Database column date_created SqlType(timestamp)
   *  @param fieldId Database column field_id SqlType(int4)
   *  @param dataDebitOperation Database column data_debit_operation SqlType(int4)
   *  @param valueCount Database column value_count SqlType(int4), Default(0) */
  case class StatsDataDebitDataFieldAccessRow(recordId: Int, dateCreated: org.joda.time.LocalDateTime, fieldId: Int, dataDebitOperation: Int, valueCount: Int = 0)
  /** GetResult implicit for fetching StatsDataDebitDataFieldAccessRow objects using plain SQL queries */
  implicit def GetResultStatsDataDebitDataFieldAccessRow(implicit e0: GR[Int], e1: GR[org.joda.time.LocalDateTime]): GR[StatsDataDebitDataFieldAccessRow] = GR{
    prs => import prs._
    StatsDataDebitDataFieldAccessRow.tupled((<<[Int], <<[org.joda.time.LocalDateTime], <<[Int], <<[Int], <<[Int]))
  }
  /** Table description of table stats_data_debit_data_field_access. Objects of this class serve as prototypes for rows in queries. */
  class StatsDataDebitDataFieldAccess(_tableTag: Tag) extends Table[StatsDataDebitDataFieldAccessRow](_tableTag, Some("hat"), "stats_data_debit_data_field_access") {
    def * = (recordId, dateCreated, fieldId, dataDebitOperation, valueCount) <> (StatsDataDebitDataFieldAccessRow.tupled, StatsDataDebitDataFieldAccessRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(recordId), Rep.Some(dateCreated), Rep.Some(fieldId), Rep.Some(dataDebitOperation), Rep.Some(valueCount)).shaped.<>({r=>import r._; _1.map(_=> StatsDataDebitDataFieldAccessRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column record_id SqlType(serial), AutoInc, PrimaryKey */
    val recordId: Rep[Int] = column[Int]("record_id", O.AutoInc, O.PrimaryKey)
    /** Database column date_created SqlType(timestamp) */
    val dateCreated: Rep[org.joda.time.LocalDateTime] = column[org.joda.time.LocalDateTime]("date_created")
    /** Database column field_id SqlType(int4) */
    val fieldId: Rep[Int] = column[Int]("field_id")
    /** Database column data_debit_operation SqlType(int4) */
    val dataDebitOperation: Rep[Int] = column[Int]("data_debit_operation")
    /** Database column value_count SqlType(int4), Default(0) */
    val valueCount: Rep[Int] = column[Int]("value_count", O.Default(0))

    /** Foreign key referencing DataField (database name stats_data_debit_data_field_access_field_id_fkey) */
    lazy val dataFieldFk = foreignKey("stats_data_debit_data_field_access_field_id_fkey", fieldId, DataField)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
    /** Foreign key referencing StatsDataDebitOperation (database name stats_data_debit_data_field_access_data_debit_operation_fkey) */
    lazy val statsDataDebitOperationFk = foreignKey("stats_data_debit_data_field_access_data_debit_operation_fkey", dataDebitOperation, StatsDataDebitOperation)(r => r.recordId, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
  }
  /** Collection-like TableQuery object for table StatsDataDebitDataFieldAccess */
  lazy val StatsDataDebitDataFieldAccess = new TableQuery(tag => new StatsDataDebitDataFieldAccess(tag))

  /** Entity class storing rows of table StatsDataDebitDataTableAccess
   *  @param recordId Database column record_id SqlType(serial), AutoInc, PrimaryKey
   *  @param dateCreated Database column date_created SqlType(timestamp)
   *  @param tableId Database column table_id SqlType(int4)
   *  @param dataDebitOperation Database column data_debit_operation SqlType(int4)
   *  @param valueCount Database column value_count SqlType(int4), Default(0) */
  case class StatsDataDebitDataTableAccessRow(recordId: Int, dateCreated: org.joda.time.LocalDateTime, tableId: Int, dataDebitOperation: Int, valueCount: Int = 0)
  /** GetResult implicit for fetching StatsDataDebitDataTableAccessRow objects using plain SQL queries */
  implicit def GetResultStatsDataDebitDataTableAccessRow(implicit e0: GR[Int], e1: GR[org.joda.time.LocalDateTime]): GR[StatsDataDebitDataTableAccessRow] = GR{
    prs => import prs._
    StatsDataDebitDataTableAccessRow.tupled((<<[Int], <<[org.joda.time.LocalDateTime], <<[Int], <<[Int], <<[Int]))
  }
  /** Table description of table stats_data_debit_data_table_access. Objects of this class serve as prototypes for rows in queries. */
  class StatsDataDebitDataTableAccess(_tableTag: Tag) extends Table[StatsDataDebitDataTableAccessRow](_tableTag, Some("hat"), "stats_data_debit_data_table_access") {
    def * = (recordId, dateCreated, tableId, dataDebitOperation, valueCount) <> (StatsDataDebitDataTableAccessRow.tupled, StatsDataDebitDataTableAccessRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(recordId), Rep.Some(dateCreated), Rep.Some(tableId), Rep.Some(dataDebitOperation), Rep.Some(valueCount)).shaped.<>({r=>import r._; _1.map(_=> StatsDataDebitDataTableAccessRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column record_id SqlType(serial), AutoInc, PrimaryKey */
    val recordId: Rep[Int] = column[Int]("record_id", O.AutoInc, O.PrimaryKey)
    /** Database column date_created SqlType(timestamp) */
    val dateCreated: Rep[org.joda.time.LocalDateTime] = column[org.joda.time.LocalDateTime]("date_created")
    /** Database column table_id SqlType(int4) */
    val tableId: Rep[Int] = column[Int]("table_id")
    /** Database column data_debit_operation SqlType(int4) */
    val dataDebitOperation: Rep[Int] = column[Int]("data_debit_operation")
    /** Database column value_count SqlType(int4), Default(0) */
    val valueCount: Rep[Int] = column[Int]("value_count", O.Default(0))

    /** Foreign key referencing DataTable (database name stats_data_debit_data_table_access_table_id_fkey) */
    lazy val dataTableFk = foreignKey("stats_data_debit_data_table_access_table_id_fkey", tableId, DataTable)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
    /** Foreign key referencing StatsDataDebitOperation (database name stats_data_debit_data_table_access_data_debit_operation_fkey) */
    lazy val statsDataDebitOperationFk = foreignKey("stats_data_debit_data_table_access_data_debit_operation_fkey", dataDebitOperation, StatsDataDebitOperation)(r => r.recordId, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
  }
  /** Collection-like TableQuery object for table StatsDataDebitDataTableAccess */
  lazy val StatsDataDebitDataTableAccess = new TableQuery(tag => new StatsDataDebitDataTableAccess(tag))

  /** Entity class storing rows of table StatsDataDebitOperation
   *  @param recordId Database column record_id SqlType(serial), AutoInc, PrimaryKey
   *  @param dateCreated Database column date_created SqlType(timestamp)
   *  @param dataDebit Database column data_debit SqlType(uuid)
   *  @param hatUser Database column hat_user SqlType(uuid)
   *  @param operation Database column operation SqlType(varchar) */
  case class StatsDataDebitOperationRow(recordId: Int, dateCreated: org.joda.time.LocalDateTime, dataDebit: java.util.UUID, hatUser: java.util.UUID, operation: String)
  /** GetResult implicit for fetching StatsDataDebitOperationRow objects using plain SQL queries */
  implicit def GetResultStatsDataDebitOperationRow(implicit e0: GR[Int], e1: GR[org.joda.time.LocalDateTime], e2: GR[java.util.UUID], e3: GR[String]): GR[StatsDataDebitOperationRow] = GR{
    prs => import prs._
    StatsDataDebitOperationRow.tupled((<<[Int], <<[org.joda.time.LocalDateTime], <<[java.util.UUID], <<[java.util.UUID], <<[String]))
  }
  /** Table description of table stats_data_debit_operation. Objects of this class serve as prototypes for rows in queries. */
  class StatsDataDebitOperation(_tableTag: Tag) extends Table[StatsDataDebitOperationRow](_tableTag, Some("hat"), "stats_data_debit_operation") {
    def * = (recordId, dateCreated, dataDebit, hatUser, operation) <> (StatsDataDebitOperationRow.tupled, StatsDataDebitOperationRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(recordId), Rep.Some(dateCreated), Rep.Some(dataDebit), Rep.Some(hatUser), Rep.Some(operation)).shaped.<>({r=>import r._; _1.map(_=> StatsDataDebitOperationRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column record_id SqlType(serial), AutoInc, PrimaryKey */
    val recordId: Rep[Int] = column[Int]("record_id", O.AutoInc, O.PrimaryKey)
    /** Database column date_created SqlType(timestamp) */
    val dateCreated: Rep[org.joda.time.LocalDateTime] = column[org.joda.time.LocalDateTime]("date_created")
    /** Database column data_debit SqlType(uuid) */
    val dataDebit: Rep[java.util.UUID] = column[java.util.UUID]("data_debit")
    /** Database column hat_user SqlType(uuid) */
    val hatUser: Rep[java.util.UUID] = column[java.util.UUID]("hat_user")
    /** Database column operation SqlType(varchar) */
    val operation: Rep[String] = column[String]("operation")

    /** Foreign key referencing DataDebit (database name stats_data_debit_operation_data_debit_fkey) */
    lazy val dataDebitFk = foreignKey("stats_data_debit_operation_data_debit_fkey", dataDebit, DataDebit)(r => r.dataDebitKey, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
    /** Foreign key referencing UserUser (database name stats_data_debit_operation_hat_user_fkey) */
    lazy val userUserFk = foreignKey("stats_data_debit_operation_hat_user_fkey", hatUser, UserUser)(r => r.userId, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
  }
  /** Collection-like TableQuery object for table StatsDataDebitOperation */
  lazy val StatsDataDebitOperation = new TableQuery(tag => new StatsDataDebitOperation(tag))

  /** Entity class storing rows of table StatsDataDebitRecordCount
   *  @param recordId Database column record_id SqlType(serial), AutoInc, PrimaryKey
   *  @param dateCreated Database column date_created SqlType(timestamp)
   *  @param dataDebitOperation Database column data_debit_operation SqlType(int4)
   *  @param recordCount Database column record_count SqlType(int4), Default(0) */
  case class StatsDataDebitRecordCountRow(recordId: Int, dateCreated: org.joda.time.LocalDateTime, dataDebitOperation: Int, recordCount: Int = 0)
  /** GetResult implicit for fetching StatsDataDebitRecordCountRow objects using plain SQL queries */
  implicit def GetResultStatsDataDebitRecordCountRow(implicit e0: GR[Int], e1: GR[org.joda.time.LocalDateTime]): GR[StatsDataDebitRecordCountRow] = GR{
    prs => import prs._
    StatsDataDebitRecordCountRow.tupled((<<[Int], <<[org.joda.time.LocalDateTime], <<[Int], <<[Int]))
  }
  /** Table description of table stats_data_debit_record_count. Objects of this class serve as prototypes for rows in queries. */
  class StatsDataDebitRecordCount(_tableTag: Tag) extends Table[StatsDataDebitRecordCountRow](_tableTag, Some("hat"), "stats_data_debit_record_count") {
    def * = (recordId, dateCreated, dataDebitOperation, recordCount) <> (StatsDataDebitRecordCountRow.tupled, StatsDataDebitRecordCountRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(recordId), Rep.Some(dateCreated), Rep.Some(dataDebitOperation), Rep.Some(recordCount)).shaped.<>({r=>import r._; _1.map(_=> StatsDataDebitRecordCountRow.tupled((_1.get, _2.get, _3.get, _4.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column record_id SqlType(serial), AutoInc, PrimaryKey */
    val recordId: Rep[Int] = column[Int]("record_id", O.AutoInc, O.PrimaryKey)
    /** Database column date_created SqlType(timestamp) */
    val dateCreated: Rep[org.joda.time.LocalDateTime] = column[org.joda.time.LocalDateTime]("date_created")
    /** Database column data_debit_operation SqlType(int4) */
    val dataDebitOperation: Rep[Int] = column[Int]("data_debit_operation")
    /** Database column record_count SqlType(int4), Default(0) */
    val recordCount: Rep[Int] = column[Int]("record_count", O.Default(0))

    /** Foreign key referencing StatsDataDebitOperation (database name stats_data_debit_record_count_data_debit_operation_fkey) */
    lazy val statsDataDebitOperationFk = foreignKey("stats_data_debit_record_count_data_debit_operation_fkey", dataDebitOperation, StatsDataDebitOperation)(r => r.recordId, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
  }
  /** Collection-like TableQuery object for table StatsDataDebitRecordCount */
  lazy val StatsDataDebitRecordCount = new TableQuery(tag => new StatsDataDebitRecordCount(tag))

  /** Entity class storing rows of table SystemEventlog
   *  @param id Database column id SqlType(serial), AutoInc, PrimaryKey
   *  @param eventType Database column event_type SqlType(varchar), Length(45,true)
   *  @param date Database column date SqlType(date)
   *  @param time Database column time SqlType(time)
   *  @param creator Database column creator SqlType(varchar), Length(100,true)
   *  @param command Database column command SqlType(varchar), Length(100,true)
   *  @param result Database column result SqlType(varchar), Length(45,true) */
  case class SystemEventlogRow(id: Int, eventType: String, date: org.joda.time.LocalDate, time: org.joda.time.LocalTime, creator: String, command: String, result: String)
  /** GetResult implicit for fetching SystemEventlogRow objects using plain SQL queries */
  implicit def GetResultSystemEventlogRow(implicit e0: GR[Int], e1: GR[String], e2: GR[org.joda.time.LocalDate], e3: GR[org.joda.time.LocalTime]): GR[SystemEventlogRow] = GR{
    prs => import prs._
    SystemEventlogRow.tupled((<<[Int], <<[String], <<[org.joda.time.LocalDate], <<[org.joda.time.LocalTime], <<[String], <<[String], <<[String]))
  }
  /** Table description of table system_eventlog. Objects of this class serve as prototypes for rows in queries. */
  class SystemEventlog(_tableTag: Tag) extends Table[SystemEventlogRow](_tableTag, Some("hat"), "system_eventlog") {
    def * = (id, eventType, date, time, creator, command, result) <> (SystemEventlogRow.tupled, SystemEventlogRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(eventType), Rep.Some(date), Rep.Some(time), Rep.Some(creator), Rep.Some(command), Rep.Some(result)).shaped.<>({r=>import r._; _1.map(_=> SystemEventlogRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get, _7.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(serial), AutoInc, PrimaryKey */
    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    /** Database column event_type SqlType(varchar), Length(45,true) */
    val eventType: Rep[String] = column[String]("event_type", O.Length(45,varying=true))
    /** Database column date SqlType(date) */
    val date: Rep[org.joda.time.LocalDate] = column[org.joda.time.LocalDate]("date")
    /** Database column time SqlType(time) */
    val time: Rep[org.joda.time.LocalTime] = column[org.joda.time.LocalTime]("time")
    /** Database column creator SqlType(varchar), Length(100,true) */
    val creator: Rep[String] = column[String]("creator", O.Length(100,varying=true))
    /** Database column command SqlType(varchar), Length(100,true) */
    val command: Rep[String] = column[String]("command", O.Length(100,varying=true))
    /** Database column result SqlType(varchar), Length(45,true) */
    val result: Rep[String] = column[String]("result", O.Length(45,varying=true))
  }
  /** Collection-like TableQuery object for table SystemEventlog */
  lazy val SystemEventlog = new TableQuery(tag => new SystemEventlog(tag))

  /** Entity class storing rows of table SystemProperty
   *  @param id Database column id SqlType(serial), AutoInc, PrimaryKey
   *  @param dateCreated Database column date_created SqlType(timestamp)
   *  @param lastUpdated Database column last_updated SqlType(timestamp)
   *  @param name Database column name SqlType(varchar)
   *  @param description Database column description SqlType(text), Default(None)
   *  @param typeId Database column type_id SqlType(int4)
   *  @param unitofmeasurementId Database column unitofmeasurement_id SqlType(int4) */
  case class SystemPropertyRow(id: Int, dateCreated: org.joda.time.LocalDateTime, lastUpdated: org.joda.time.LocalDateTime, name: String, description: Option[String] = None, typeId: Int, unitofmeasurementId: Int)
  /** GetResult implicit for fetching SystemPropertyRow objects using plain SQL queries */
  implicit def GetResultSystemPropertyRow(implicit e0: GR[Int], e1: GR[org.joda.time.LocalDateTime], e2: GR[String], e3: GR[Option[String]]): GR[SystemPropertyRow] = GR{
    prs => import prs._
    SystemPropertyRow.tupled((<<[Int], <<[org.joda.time.LocalDateTime], <<[org.joda.time.LocalDateTime], <<[String], <<?[String], <<[Int], <<[Int]))
  }
  /** Table description of table system_property. Objects of this class serve as prototypes for rows in queries. */
  class SystemProperty(_tableTag: Tag) extends Table[SystemPropertyRow](_tableTag, Some("hat"), "system_property") {
    def * = (id, dateCreated, lastUpdated, name, description, typeId, unitofmeasurementId) <> (SystemPropertyRow.tupled, SystemPropertyRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(dateCreated), Rep.Some(lastUpdated), Rep.Some(name), description, Rep.Some(typeId), Rep.Some(unitofmeasurementId)).shaped.<>({r=>import r._; _1.map(_=> SystemPropertyRow.tupled((_1.get, _2.get, _3.get, _4.get, _5, _6.get, _7.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(serial), AutoInc, PrimaryKey */
    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    /** Database column date_created SqlType(timestamp) */
    val dateCreated: Rep[org.joda.time.LocalDateTime] = column[org.joda.time.LocalDateTime]("date_created")
    /** Database column last_updated SqlType(timestamp) */
    val lastUpdated: Rep[org.joda.time.LocalDateTime] = column[org.joda.time.LocalDateTime]("last_updated")
    /** Database column name SqlType(varchar) */
    val name: Rep[String] = column[String]("name")
    /** Database column description SqlType(text), Default(None) */
    val description: Rep[Option[String]] = column[Option[String]]("description", O.Default(None))
    /** Database column type_id SqlType(int4) */
    val typeId: Rep[Int] = column[Int]("type_id")
    /** Database column unitofmeasurement_id SqlType(int4) */
    val unitofmeasurementId: Rep[Int] = column[Int]("unitofmeasurement_id")

    /** Foreign key referencing SystemType (database name system_type_system_property_fk) */
    lazy val systemTypeFk = foreignKey("system_type_system_property_fk", typeId, SystemType)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
    /** Foreign key referencing SystemUnitofmeasurement (database name system_unitofmeasurement_system_property_fk) */
    lazy val systemUnitofmeasurementFk = foreignKey("system_unitofmeasurement_system_property_fk", unitofmeasurementId, SystemUnitofmeasurement)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
  }
  /** Collection-like TableQuery object for table SystemProperty */
  lazy val SystemProperty = new TableQuery(tag => new SystemProperty(tag))

  /** Entity class storing rows of table SystemPropertyrecord
   *  @param id Database column id SqlType(serial), AutoInc, PrimaryKey
   *  @param dateCreated Database column date_created SqlType(timestamp)
   *  @param lastUpdated Database column last_updated SqlType(timestamp)
   *  @param name Database column name SqlType(varchar) */
  case class SystemPropertyrecordRow(id: Int, dateCreated: org.joda.time.LocalDateTime, lastUpdated: org.joda.time.LocalDateTime, name: String)
  /** GetResult implicit for fetching SystemPropertyrecordRow objects using plain SQL queries */
  implicit def GetResultSystemPropertyrecordRow(implicit e0: GR[Int], e1: GR[org.joda.time.LocalDateTime], e2: GR[String]): GR[SystemPropertyrecordRow] = GR{
    prs => import prs._
    SystemPropertyrecordRow.tupled((<<[Int], <<[org.joda.time.LocalDateTime], <<[org.joda.time.LocalDateTime], <<[String]))
  }
  /** Table description of table system_propertyrecord. Objects of this class serve as prototypes for rows in queries. */
  class SystemPropertyrecord(_tableTag: Tag) extends Table[SystemPropertyrecordRow](_tableTag, Some("hat"), "system_propertyrecord") {
    def * = (id, dateCreated, lastUpdated, name) <> (SystemPropertyrecordRow.tupled, SystemPropertyrecordRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(dateCreated), Rep.Some(lastUpdated), Rep.Some(name)).shaped.<>({r=>import r._; _1.map(_=> SystemPropertyrecordRow.tupled((_1.get, _2.get, _3.get, _4.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(serial), AutoInc, PrimaryKey */
    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    /** Database column date_created SqlType(timestamp) */
    val dateCreated: Rep[org.joda.time.LocalDateTime] = column[org.joda.time.LocalDateTime]("date_created")
    /** Database column last_updated SqlType(timestamp) */
    val lastUpdated: Rep[org.joda.time.LocalDateTime] = column[org.joda.time.LocalDateTime]("last_updated")
    /** Database column name SqlType(varchar) */
    val name: Rep[String] = column[String]("name")
  }
  /** Collection-like TableQuery object for table SystemPropertyrecord */
  lazy val SystemPropertyrecord = new TableQuery(tag => new SystemPropertyrecord(tag))

  /** Entity class storing rows of table SystemRelationshiprecord
   *  @param id Database column id SqlType(serial), AutoInc, PrimaryKey
   *  @param dateCreated Database column date_created SqlType(timestamp)
   *  @param lastUpdated Database column last_updated SqlType(timestamp)
   *  @param name Database column name SqlType(varchar) */
  case class SystemRelationshiprecordRow(id: Int, dateCreated: org.joda.time.LocalDateTime, lastUpdated: org.joda.time.LocalDateTime, name: String)
  /** GetResult implicit for fetching SystemRelationshiprecordRow objects using plain SQL queries */
  implicit def GetResultSystemRelationshiprecordRow(implicit e0: GR[Int], e1: GR[org.joda.time.LocalDateTime], e2: GR[String]): GR[SystemRelationshiprecordRow] = GR{
    prs => import prs._
    SystemRelationshiprecordRow.tupled((<<[Int], <<[org.joda.time.LocalDateTime], <<[org.joda.time.LocalDateTime], <<[String]))
  }
  /** Table description of table system_relationshiprecord. Objects of this class serve as prototypes for rows in queries. */
  class SystemRelationshiprecord(_tableTag: Tag) extends Table[SystemRelationshiprecordRow](_tableTag, Some("hat"), "system_relationshiprecord") {
    def * = (id, dateCreated, lastUpdated, name) <> (SystemRelationshiprecordRow.tupled, SystemRelationshiprecordRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(dateCreated), Rep.Some(lastUpdated), Rep.Some(name)).shaped.<>({r=>import r._; _1.map(_=> SystemRelationshiprecordRow.tupled((_1.get, _2.get, _3.get, _4.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(serial), AutoInc, PrimaryKey */
    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    /** Database column date_created SqlType(timestamp) */
    val dateCreated: Rep[org.joda.time.LocalDateTime] = column[org.joda.time.LocalDateTime]("date_created")
    /** Database column last_updated SqlType(timestamp) */
    val lastUpdated: Rep[org.joda.time.LocalDateTime] = column[org.joda.time.LocalDateTime]("last_updated")
    /** Database column name SqlType(varchar) */
    val name: Rep[String] = column[String]("name")
  }
  /** Collection-like TableQuery object for table SystemRelationshiprecord */
  lazy val SystemRelationshiprecord = new TableQuery(tag => new SystemRelationshiprecord(tag))

  /** Entity class storing rows of table SystemRelationshiprecordtorecordcrossref
   *  @param id Database column id SqlType(serial), AutoInc, PrimaryKey
   *  @param dateCreated Database column date_created SqlType(timestamp)
   *  @param lastUpdated Database column last_updated SqlType(timestamp)
   *  @param relationshiprecordId1 Database column relationshiprecord_id1 SqlType(int4)
   *  @param relationshiprecordId2 Database column relationshiprecord_id2 SqlType(int4)
   *  @param relationshipType Database column relationship_type SqlType(varchar), Length(100,true) */
  case class SystemRelationshiprecordtorecordcrossrefRow(id: Int, dateCreated: org.joda.time.LocalDateTime, lastUpdated: org.joda.time.LocalDateTime, relationshiprecordId1: Int, relationshiprecordId2: Int, relationshipType: String)
  /** GetResult implicit for fetching SystemRelationshiprecordtorecordcrossrefRow objects using plain SQL queries */
  implicit def GetResultSystemRelationshiprecordtorecordcrossrefRow(implicit e0: GR[Int], e1: GR[org.joda.time.LocalDateTime], e2: GR[String]): GR[SystemRelationshiprecordtorecordcrossrefRow] = GR{
    prs => import prs._
    SystemRelationshiprecordtorecordcrossrefRow.tupled((<<[Int], <<[org.joda.time.LocalDateTime], <<[org.joda.time.LocalDateTime], <<[Int], <<[Int], <<[String]))
  }
  /** Table description of table system_relationshiprecordtorecordcrossref. Objects of this class serve as prototypes for rows in queries. */
  class SystemRelationshiprecordtorecordcrossref(_tableTag: Tag) extends Table[SystemRelationshiprecordtorecordcrossrefRow](_tableTag, Some("hat"), "system_relationshiprecordtorecordcrossref") {
    def * = (id, dateCreated, lastUpdated, relationshiprecordId1, relationshiprecordId2, relationshipType) <> (SystemRelationshiprecordtorecordcrossrefRow.tupled, SystemRelationshiprecordtorecordcrossrefRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(dateCreated), Rep.Some(lastUpdated), Rep.Some(relationshiprecordId1), Rep.Some(relationshiprecordId2), Rep.Some(relationshipType)).shaped.<>({r=>import r._; _1.map(_=> SystemRelationshiprecordtorecordcrossrefRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(serial), AutoInc, PrimaryKey */
    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    /** Database column date_created SqlType(timestamp) */
    val dateCreated: Rep[org.joda.time.LocalDateTime] = column[org.joda.time.LocalDateTime]("date_created")
    /** Database column last_updated SqlType(timestamp) */
    val lastUpdated: Rep[org.joda.time.LocalDateTime] = column[org.joda.time.LocalDateTime]("last_updated")
    /** Database column relationshiprecord_id1 SqlType(int4) */
    val relationshiprecordId1: Rep[Int] = column[Int]("relationshiprecord_id1")
    /** Database column relationshiprecord_id2 SqlType(int4) */
    val relationshiprecordId2: Rep[Int] = column[Int]("relationshiprecord_id2")
    /** Database column relationship_type SqlType(varchar), Length(100,true) */
    val relationshipType: Rep[String] = column[String]("relationship_type", O.Length(100,varying=true))

    /** Foreign key referencing SystemRelationshiprecord (database name system_relationshiprecord_system_relationshiprecordtorecordc18) */
    lazy val systemRelationshiprecordFk1 = foreignKey("system_relationshiprecord_system_relationshiprecordtorecordc18", relationshiprecordId2, SystemRelationshiprecord)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
    /** Foreign key referencing SystemRelationshiprecord (database name system_relationshiprecord_system_relationshiprecordtorecordc567) */
    lazy val systemRelationshiprecordFk2 = foreignKey("system_relationshiprecord_system_relationshiprecordtorecordc567", relationshiprecordId1, SystemRelationshiprecord)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
  }
  /** Collection-like TableQuery object for table SystemRelationshiprecordtorecordcrossref */
  lazy val SystemRelationshiprecordtorecordcrossref = new TableQuery(tag => new SystemRelationshiprecordtorecordcrossref(tag))

  /** Entity class storing rows of table SystemType
   *  @param id Database column id SqlType(serial), AutoInc, PrimaryKey
   *  @param dateCreated Database column date_created SqlType(timestamp)
   *  @param lastUpdated Database column last_updated SqlType(timestamp)
   *  @param name Database column name SqlType(varchar)
   *  @param description Database column description SqlType(text), Default(None) */
  case class SystemTypeRow(id: Int, dateCreated: org.joda.time.LocalDateTime, lastUpdated: org.joda.time.LocalDateTime, name: String, description: Option[String] = None)
  /** GetResult implicit for fetching SystemTypeRow objects using plain SQL queries */
  implicit def GetResultSystemTypeRow(implicit e0: GR[Int], e1: GR[org.joda.time.LocalDateTime], e2: GR[String], e3: GR[Option[String]]): GR[SystemTypeRow] = GR{
    prs => import prs._
    SystemTypeRow.tupled((<<[Int], <<[org.joda.time.LocalDateTime], <<[org.joda.time.LocalDateTime], <<[String], <<?[String]))
  }
  /** Table description of table system_type. Objects of this class serve as prototypes for rows in queries. */
  class SystemType(_tableTag: Tag) extends Table[SystemTypeRow](_tableTag, Some("hat"), "system_type") {
    def * = (id, dateCreated, lastUpdated, name, description) <> (SystemTypeRow.tupled, SystemTypeRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(dateCreated), Rep.Some(lastUpdated), Rep.Some(name), description).shaped.<>({r=>import r._; _1.map(_=> SystemTypeRow.tupled((_1.get, _2.get, _3.get, _4.get, _5)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(serial), AutoInc, PrimaryKey */
    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    /** Database column date_created SqlType(timestamp) */
    val dateCreated: Rep[org.joda.time.LocalDateTime] = column[org.joda.time.LocalDateTime]("date_created")
    /** Database column last_updated SqlType(timestamp) */
    val lastUpdated: Rep[org.joda.time.LocalDateTime] = column[org.joda.time.LocalDateTime]("last_updated")
    /** Database column name SqlType(varchar) */
    val name: Rep[String] = column[String]("name")
    /** Database column description SqlType(text), Default(None) */
    val description: Rep[Option[String]] = column[Option[String]]("description", O.Default(None))

    /** Uniqueness Index over (name) (database name system_type_name_key) */
    val index1 = index("system_type_name_key", name, unique=true)
  }
  /** Collection-like TableQuery object for table SystemType */
  lazy val SystemType = new TableQuery(tag => new SystemType(tag))

  /** Entity class storing rows of table SystemTypetotypecrossref
   *  @param id Database column id SqlType(serial), AutoInc, PrimaryKey
   *  @param dateCreated Database column date_created SqlType(timestamp)
   *  @param lastUpdated Database column last_updated SqlType(timestamp)
   *  @param typeOneId Database column type_one_id SqlType(int4)
   *  @param typeTwoId Database column type_two_id SqlType(int4)
   *  @param relationshipType Database column relationship_type SqlType(varchar), Length(100,true) */
  case class SystemTypetotypecrossrefRow(id: Int, dateCreated: org.joda.time.LocalDateTime, lastUpdated: org.joda.time.LocalDateTime, typeOneId: Int, typeTwoId: Int, relationshipType: String)
  /** GetResult implicit for fetching SystemTypetotypecrossrefRow objects using plain SQL queries */
  implicit def GetResultSystemTypetotypecrossrefRow(implicit e0: GR[Int], e1: GR[org.joda.time.LocalDateTime], e2: GR[String]): GR[SystemTypetotypecrossrefRow] = GR{
    prs => import prs._
    SystemTypetotypecrossrefRow.tupled((<<[Int], <<[org.joda.time.LocalDateTime], <<[org.joda.time.LocalDateTime], <<[Int], <<[Int], <<[String]))
  }
  /** Table description of table system_typetotypecrossref. Objects of this class serve as prototypes for rows in queries. */
  class SystemTypetotypecrossref(_tableTag: Tag) extends Table[SystemTypetotypecrossrefRow](_tableTag, Some("hat"), "system_typetotypecrossref") {
    def * = (id, dateCreated, lastUpdated, typeOneId, typeTwoId, relationshipType) <> (SystemTypetotypecrossrefRow.tupled, SystemTypetotypecrossrefRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(dateCreated), Rep.Some(lastUpdated), Rep.Some(typeOneId), Rep.Some(typeTwoId), Rep.Some(relationshipType)).shaped.<>({r=>import r._; _1.map(_=> SystemTypetotypecrossrefRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(serial), AutoInc, PrimaryKey */
    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    /** Database column date_created SqlType(timestamp) */
    val dateCreated: Rep[org.joda.time.LocalDateTime] = column[org.joda.time.LocalDateTime]("date_created")
    /** Database column last_updated SqlType(timestamp) */
    val lastUpdated: Rep[org.joda.time.LocalDateTime] = column[org.joda.time.LocalDateTime]("last_updated")
    /** Database column type_one_id SqlType(int4) */
    val typeOneId: Rep[Int] = column[Int]("type_one_id")
    /** Database column type_two_id SqlType(int4) */
    val typeTwoId: Rep[Int] = column[Int]("type_two_id")
    /** Database column relationship_type SqlType(varchar), Length(100,true) */
    val relationshipType: Rep[String] = column[String]("relationship_type", O.Length(100,varying=true))

    /** Foreign key referencing SystemType (database name system_type_system_typetotypecrossref_fk) */
    lazy val systemTypeFk1 = foreignKey("system_type_system_typetotypecrossref_fk", typeOneId, SystemType)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
    /** Foreign key referencing SystemType (database name system_type_system_typetotypecrossref_fk1) */
    lazy val systemTypeFk2 = foreignKey("system_type_system_typetotypecrossref_fk1", typeTwoId, SystemType)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
  }
  /** Collection-like TableQuery object for table SystemTypetotypecrossref */
  lazy val SystemTypetotypecrossref = new TableQuery(tag => new SystemTypetotypecrossref(tag))

  /** Entity class storing rows of table SystemUnitofmeasurement
   *  @param id Database column id SqlType(serial), AutoInc, PrimaryKey
   *  @param dateCreated Database column date_created SqlType(timestamp)
   *  @param lastUpdated Database column last_updated SqlType(timestamp)
   *  @param name Database column name SqlType(varchar)
   *  @param description Database column description SqlType(text), Default(None)
   *  @param symbol Database column symbol SqlType(varchar), Default(None) */
  case class SystemUnitofmeasurementRow(id: Int, dateCreated: org.joda.time.LocalDateTime, lastUpdated: org.joda.time.LocalDateTime, name: String, description: Option[String] = None, symbol: Option[String] = None)
  /** GetResult implicit for fetching SystemUnitofmeasurementRow objects using plain SQL queries */
  implicit def GetResultSystemUnitofmeasurementRow(implicit e0: GR[Int], e1: GR[org.joda.time.LocalDateTime], e2: GR[String], e3: GR[Option[String]]): GR[SystemUnitofmeasurementRow] = GR{
    prs => import prs._
    SystemUnitofmeasurementRow.tupled((<<[Int], <<[org.joda.time.LocalDateTime], <<[org.joda.time.LocalDateTime], <<[String], <<?[String], <<?[String]))
  }
  /** Table description of table system_unitofmeasurement. Objects of this class serve as prototypes for rows in queries. */
  class SystemUnitofmeasurement(_tableTag: Tag) extends Table[SystemUnitofmeasurementRow](_tableTag, Some("hat"), "system_unitofmeasurement") {
    def * = (id, dateCreated, lastUpdated, name, description, symbol) <> (SystemUnitofmeasurementRow.tupled, SystemUnitofmeasurementRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(dateCreated), Rep.Some(lastUpdated), Rep.Some(name), description, symbol).shaped.<>({r=>import r._; _1.map(_=> SystemUnitofmeasurementRow.tupled((_1.get, _2.get, _3.get, _4.get, _5, _6)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(serial), AutoInc, PrimaryKey */
    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    /** Database column date_created SqlType(timestamp) */
    val dateCreated: Rep[org.joda.time.LocalDateTime] = column[org.joda.time.LocalDateTime]("date_created")
    /** Database column last_updated SqlType(timestamp) */
    val lastUpdated: Rep[org.joda.time.LocalDateTime] = column[org.joda.time.LocalDateTime]("last_updated")
    /** Database column name SqlType(varchar) */
    val name: Rep[String] = column[String]("name")
    /** Database column description SqlType(text), Default(None) */
    val description: Rep[Option[String]] = column[Option[String]]("description", O.Default(None))
    /** Database column symbol SqlType(varchar), Default(None) */
    val symbol: Rep[Option[String]] = column[Option[String]]("symbol", O.Default(None))

    /** Uniqueness Index over (name) (database name system_unitofmeasurement_name_key) */
    val index1 = index("system_unitofmeasurement_name_key", name, unique=true)
  }
  /** Collection-like TableQuery object for table SystemUnitofmeasurement */
  lazy val SystemUnitofmeasurement = new TableQuery(tag => new SystemUnitofmeasurement(tag))

  /** Entity class storing rows of table ThingsSystempropertydynamiccrossref
   *  @param id Database column id SqlType(serial), AutoInc, PrimaryKey
   *  @param dateCreated Database column date_created SqlType(timestamp)
   *  @param lastUpdated Database column last_updated SqlType(timestamp)
   *  @param thingId Database column thing_id SqlType(int4)
   *  @param systemPropertyId Database column system_property_id SqlType(int4)
   *  @param fieldId Database column field_id SqlType(int4)
   *  @param relationshipType Database column relationship_type SqlType(varchar), Length(100,true)
   *  @param isCurrent Database column is_current SqlType(bool)
   *  @param propertyrecordId Database column propertyrecord_id SqlType(int4) */
  case class ThingsSystempropertydynamiccrossrefRow(id: Int, dateCreated: org.joda.time.LocalDateTime, lastUpdated: org.joda.time.LocalDateTime, thingId: Int, systemPropertyId: Int, fieldId: Int, relationshipType: String, isCurrent: Boolean, propertyrecordId: Int)
  /** GetResult implicit for fetching ThingsSystempropertydynamiccrossrefRow objects using plain SQL queries */
  implicit def GetResultThingsSystempropertydynamiccrossrefRow(implicit e0: GR[Int], e1: GR[org.joda.time.LocalDateTime], e2: GR[String], e3: GR[Boolean]): GR[ThingsSystempropertydynamiccrossrefRow] = GR{
    prs => import prs._
    ThingsSystempropertydynamiccrossrefRow.tupled((<<[Int], <<[org.joda.time.LocalDateTime], <<[org.joda.time.LocalDateTime], <<[Int], <<[Int], <<[Int], <<[String], <<[Boolean], <<[Int]))
  }
  /** Table description of table things_systempropertydynamiccrossref. Objects of this class serve as prototypes for rows in queries. */
  class ThingsSystempropertydynamiccrossref(_tableTag: Tag) extends Table[ThingsSystempropertydynamiccrossrefRow](_tableTag, Some("hat"), "things_systempropertydynamiccrossref") {
    def * = (id, dateCreated, lastUpdated, thingId, systemPropertyId, fieldId, relationshipType, isCurrent, propertyrecordId) <> (ThingsSystempropertydynamiccrossrefRow.tupled, ThingsSystempropertydynamiccrossrefRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(dateCreated), Rep.Some(lastUpdated), Rep.Some(thingId), Rep.Some(systemPropertyId), Rep.Some(fieldId), Rep.Some(relationshipType), Rep.Some(isCurrent), Rep.Some(propertyrecordId)).shaped.<>({r=>import r._; _1.map(_=> ThingsSystempropertydynamiccrossrefRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get, _7.get, _8.get, _9.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(serial), AutoInc, PrimaryKey */
    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    /** Database column date_created SqlType(timestamp) */
    val dateCreated: Rep[org.joda.time.LocalDateTime] = column[org.joda.time.LocalDateTime]("date_created")
    /** Database column last_updated SqlType(timestamp) */
    val lastUpdated: Rep[org.joda.time.LocalDateTime] = column[org.joda.time.LocalDateTime]("last_updated")
    /** Database column thing_id SqlType(int4) */
    val thingId: Rep[Int] = column[Int]("thing_id")
    /** Database column system_property_id SqlType(int4) */
    val systemPropertyId: Rep[Int] = column[Int]("system_property_id")
    /** Database column field_id SqlType(int4) */
    val fieldId: Rep[Int] = column[Int]("field_id")
    /** Database column relationship_type SqlType(varchar), Length(100,true) */
    val relationshipType: Rep[String] = column[String]("relationship_type", O.Length(100,varying=true))
    /** Database column is_current SqlType(bool) */
    val isCurrent: Rep[Boolean] = column[Boolean]("is_current")
    /** Database column propertyrecord_id SqlType(int4) */
    val propertyrecordId: Rep[Int] = column[Int]("propertyrecord_id")

    /** Foreign key referencing DataField (database name data_field_things_systempropertydynamiccrossref_fk) */
    lazy val dataFieldFk = foreignKey("data_field_things_systempropertydynamiccrossref_fk", fieldId, DataField)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
    /** Foreign key referencing SystemProperty (database name system_property_things_systempropertydynamiccrossref_fk) */
    lazy val systemPropertyFk = foreignKey("system_property_things_systempropertydynamiccrossref_fk", systemPropertyId, SystemProperty)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
    /** Foreign key referencing SystemPropertyrecord (database name property_record_things_systempropertydynamiccrossref_fk) */
    lazy val systemPropertyrecordFk = foreignKey("property_record_things_systempropertydynamiccrossref_fk", propertyrecordId, SystemPropertyrecord)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
    /** Foreign key referencing ThingsThing (database name things_systempropertydynamiccrossref_fk) */
    lazy val thingsThingFk = foreignKey("things_systempropertydynamiccrossref_fk", thingId, ThingsThing)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
  }
  /** Collection-like TableQuery object for table ThingsSystempropertydynamiccrossref */
  lazy val ThingsSystempropertydynamiccrossref = new TableQuery(tag => new ThingsSystempropertydynamiccrossref(tag))

  /** Entity class storing rows of table ThingsSystempropertystaticcrossref
   *  @param id Database column id SqlType(serial), AutoInc, PrimaryKey
   *  @param dateCreated Database column date_created SqlType(timestamp)
   *  @param lastUpdated Database column last_updated SqlType(timestamp)
   *  @param thingId Database column thing_id SqlType(int4)
   *  @param systemPropertyId Database column system_property_id SqlType(int4)
   *  @param recordId Database column record_id SqlType(int4)
   *  @param fieldId Database column field_id SqlType(int4)
   *  @param relationshipType Database column relationship_type SqlType(varchar), Length(100,true)
   *  @param isCurrent Database column is_current SqlType(bool)
   *  @param propertyrecordId Database column propertyrecord_id SqlType(int4) */
  case class ThingsSystempropertystaticcrossrefRow(id: Int, dateCreated: org.joda.time.LocalDateTime, lastUpdated: org.joda.time.LocalDateTime, thingId: Int, systemPropertyId: Int, recordId: Int, fieldId: Int, relationshipType: String, isCurrent: Boolean, propertyrecordId: Int)
  /** GetResult implicit for fetching ThingsSystempropertystaticcrossrefRow objects using plain SQL queries */
  implicit def GetResultThingsSystempropertystaticcrossrefRow(implicit e0: GR[Int], e1: GR[org.joda.time.LocalDateTime], e2: GR[String], e3: GR[Boolean]): GR[ThingsSystempropertystaticcrossrefRow] = GR{
    prs => import prs._
    ThingsSystempropertystaticcrossrefRow.tupled((<<[Int], <<[org.joda.time.LocalDateTime], <<[org.joda.time.LocalDateTime], <<[Int], <<[Int], <<[Int], <<[Int], <<[String], <<[Boolean], <<[Int]))
  }
  /** Table description of table things_systempropertystaticcrossref. Objects of this class serve as prototypes for rows in queries. */
  class ThingsSystempropertystaticcrossref(_tableTag: Tag) extends Table[ThingsSystempropertystaticcrossrefRow](_tableTag, Some("hat"), "things_systempropertystaticcrossref") {
    def * = (id, dateCreated, lastUpdated, thingId, systemPropertyId, recordId, fieldId, relationshipType, isCurrent, propertyrecordId) <> (ThingsSystempropertystaticcrossrefRow.tupled, ThingsSystempropertystaticcrossrefRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(dateCreated), Rep.Some(lastUpdated), Rep.Some(thingId), Rep.Some(systemPropertyId), Rep.Some(recordId), Rep.Some(fieldId), Rep.Some(relationshipType), Rep.Some(isCurrent), Rep.Some(propertyrecordId)).shaped.<>({r=>import r._; _1.map(_=> ThingsSystempropertystaticcrossrefRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get, _7.get, _8.get, _9.get, _10.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(serial), AutoInc, PrimaryKey */
    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    /** Database column date_created SqlType(timestamp) */
    val dateCreated: Rep[org.joda.time.LocalDateTime] = column[org.joda.time.LocalDateTime]("date_created")
    /** Database column last_updated SqlType(timestamp) */
    val lastUpdated: Rep[org.joda.time.LocalDateTime] = column[org.joda.time.LocalDateTime]("last_updated")
    /** Database column thing_id SqlType(int4) */
    val thingId: Rep[Int] = column[Int]("thing_id")
    /** Database column system_property_id SqlType(int4) */
    val systemPropertyId: Rep[Int] = column[Int]("system_property_id")
    /** Database column record_id SqlType(int4) */
    val recordId: Rep[Int] = column[Int]("record_id")
    /** Database column field_id SqlType(int4) */
    val fieldId: Rep[Int] = column[Int]("field_id")
    /** Database column relationship_type SqlType(varchar), Length(100,true) */
    val relationshipType: Rep[String] = column[String]("relationship_type", O.Length(100,varying=true))
    /** Database column is_current SqlType(bool) */
    val isCurrent: Rep[Boolean] = column[Boolean]("is_current")
    /** Database column propertyrecord_id SqlType(int4) */
    val propertyrecordId: Rep[Int] = column[Int]("propertyrecord_id")

    /** Foreign key referencing DataField (database name data_field_things_systempropertystaticcrossref_fk) */
    lazy val dataFieldFk = foreignKey("data_field_things_systempropertystaticcrossref_fk", fieldId, DataField)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
    /** Foreign key referencing DataRecord (database name data_record_things_systempropertycrossref_fk) */
    lazy val dataRecordFk = foreignKey("data_record_things_systempropertycrossref_fk", recordId, DataRecord)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
    /** Foreign key referencing SystemProperty (database name thing_property_id_refs_id_fk) */
    lazy val systemPropertyFk = foreignKey("thing_property_id_refs_id_fk", systemPropertyId, SystemProperty)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
    /** Foreign key referencing SystemPropertyrecord (database name property_record_things_systempropertystaticcrossref_fk) */
    lazy val systemPropertyrecordFk = foreignKey("property_record_things_systempropertystaticcrossref_fk", propertyrecordId, SystemPropertyrecord)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
    /** Foreign key referencing ThingsThing (database name things_thingstaticpropertycrossref_thing_id_fkey) */
    lazy val thingsThingFk = foreignKey("things_thingstaticpropertycrossref_thing_id_fkey", thingId, ThingsThing)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
  }
  /** Collection-like TableQuery object for table ThingsSystempropertystaticcrossref */
  lazy val ThingsSystempropertystaticcrossref = new TableQuery(tag => new ThingsSystempropertystaticcrossref(tag))

  /** Entity class storing rows of table ThingsSystemtypecrossref
   *  @param id Database column id SqlType(serial), AutoInc, PrimaryKey
   *  @param dateCreated Database column date_created SqlType(timestamp)
   *  @param lastUpdated Database column last_updated SqlType(timestamp)
   *  @param thingId Database column thing_id SqlType(int4)
   *  @param systemTypeId Database column system_type_id SqlType(int4)
   *  @param relationshipType Database column relationship_type SqlType(varchar), Length(100,true)
   *  @param isCurrent Database column is_current SqlType(bool) */
  case class ThingsSystemtypecrossrefRow(id: Int, dateCreated: org.joda.time.LocalDateTime, lastUpdated: org.joda.time.LocalDateTime, thingId: Int, systemTypeId: Int, relationshipType: String, isCurrent: Boolean)
  /** GetResult implicit for fetching ThingsSystemtypecrossrefRow objects using plain SQL queries */
  implicit def GetResultThingsSystemtypecrossrefRow(implicit e0: GR[Int], e1: GR[org.joda.time.LocalDateTime], e2: GR[String], e3: GR[Boolean]): GR[ThingsSystemtypecrossrefRow] = GR{
    prs => import prs._
    ThingsSystemtypecrossrefRow.tupled((<<[Int], <<[org.joda.time.LocalDateTime], <<[org.joda.time.LocalDateTime], <<[Int], <<[Int], <<[String], <<[Boolean]))
  }
  /** Table description of table things_systemtypecrossref. Objects of this class serve as prototypes for rows in queries. */
  class ThingsSystemtypecrossref(_tableTag: Tag) extends Table[ThingsSystemtypecrossrefRow](_tableTag, Some("hat"), "things_systemtypecrossref") {
    def * = (id, dateCreated, lastUpdated, thingId, systemTypeId, relationshipType, isCurrent) <> (ThingsSystemtypecrossrefRow.tupled, ThingsSystemtypecrossrefRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(dateCreated), Rep.Some(lastUpdated), Rep.Some(thingId), Rep.Some(systemTypeId), Rep.Some(relationshipType), Rep.Some(isCurrent)).shaped.<>({r=>import r._; _1.map(_=> ThingsSystemtypecrossrefRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get, _7.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(serial), AutoInc, PrimaryKey */
    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    /** Database column date_created SqlType(timestamp) */
    val dateCreated: Rep[org.joda.time.LocalDateTime] = column[org.joda.time.LocalDateTime]("date_created")
    /** Database column last_updated SqlType(timestamp) */
    val lastUpdated: Rep[org.joda.time.LocalDateTime] = column[org.joda.time.LocalDateTime]("last_updated")
    /** Database column thing_id SqlType(int4) */
    val thingId: Rep[Int] = column[Int]("thing_id")
    /** Database column system_type_id SqlType(int4) */
    val systemTypeId: Rep[Int] = column[Int]("system_type_id")
    /** Database column relationship_type SqlType(varchar), Length(100,true) */
    val relationshipType: Rep[String] = column[String]("relationship_type", O.Length(100,varying=true))
    /** Database column is_current SqlType(bool) */
    val isCurrent: Rep[Boolean] = column[Boolean]("is_current")

    /** Foreign key referencing SystemType (database name system_type_things_systemtypecrossref_fk) */
    lazy val systemTypeFk = foreignKey("system_type_things_systemtypecrossref_fk", systemTypeId, SystemType)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
    /** Foreign key referencing ThingsThing (database name things_systemtypecrossref_fk) */
    lazy val thingsThingFk = foreignKey("things_systemtypecrossref_fk", thingId, ThingsThing)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
  }
  /** Collection-like TableQuery object for table ThingsSystemtypecrossref */
  lazy val ThingsSystemtypecrossref = new TableQuery(tag => new ThingsSystemtypecrossref(tag))

  /** Entity class storing rows of table ThingsThing
   *  @param id Database column id SqlType(serial), AutoInc, PrimaryKey
   *  @param dateCreated Database column date_created SqlType(timestamp)
   *  @param lastUpdated Database column last_updated SqlType(timestamp)
   *  @param name Database column name SqlType(varchar) */
  case class ThingsThingRow(id: Int, dateCreated: org.joda.time.LocalDateTime, lastUpdated: org.joda.time.LocalDateTime, name: String)
  /** GetResult implicit for fetching ThingsThingRow objects using plain SQL queries */
  implicit def GetResultThingsThingRow(implicit e0: GR[Int], e1: GR[org.joda.time.LocalDateTime], e2: GR[String]): GR[ThingsThingRow] = GR{
    prs => import prs._
    ThingsThingRow.tupled((<<[Int], <<[org.joda.time.LocalDateTime], <<[org.joda.time.LocalDateTime], <<[String]))
  }
  /** Table description of table things_thing. Objects of this class serve as prototypes for rows in queries. */
  class ThingsThing(_tableTag: Tag) extends Table[ThingsThingRow](_tableTag, Some("hat"), "things_thing") {
    def * = (id, dateCreated, lastUpdated, name) <> (ThingsThingRow.tupled, ThingsThingRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(dateCreated), Rep.Some(lastUpdated), Rep.Some(name)).shaped.<>({r=>import r._; _1.map(_=> ThingsThingRow.tupled((_1.get, _2.get, _3.get, _4.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(serial), AutoInc, PrimaryKey */
    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    /** Database column date_created SqlType(timestamp) */
    val dateCreated: Rep[org.joda.time.LocalDateTime] = column[org.joda.time.LocalDateTime]("date_created")
    /** Database column last_updated SqlType(timestamp) */
    val lastUpdated: Rep[org.joda.time.LocalDateTime] = column[org.joda.time.LocalDateTime]("last_updated")
    /** Database column name SqlType(varchar) */
    val name: Rep[String] = column[String]("name")
  }
  /** Collection-like TableQuery object for table ThingsThing */
  lazy val ThingsThing = new TableQuery(tag => new ThingsThing(tag))

  /** Entity class storing rows of table ThingsThingpersoncrossref
   *  @param id Database column id SqlType(serial), AutoInc, PrimaryKey
   *  @param dateCreated Database column date_created SqlType(timestamp)
   *  @param lastUpdated Database column last_updated SqlType(timestamp)
   *  @param personId Database column person_id SqlType(int4)
   *  @param thingId Database column thing_id SqlType(int4)
   *  @param relationshipType Database column relationship_type SqlType(varchar), Length(100,true)
   *  @param isCurrent Database column is_current SqlType(bool)
   *  @param relationshiprecordId Database column relationshiprecord_id SqlType(int4) */
  case class ThingsThingpersoncrossrefRow(id: Int, dateCreated: org.joda.time.LocalDateTime, lastUpdated: org.joda.time.LocalDateTime, personId: Int, thingId: Int, relationshipType: String, isCurrent: Boolean, relationshiprecordId: Int)
  /** GetResult implicit for fetching ThingsThingpersoncrossrefRow objects using plain SQL queries */
  implicit def GetResultThingsThingpersoncrossrefRow(implicit e0: GR[Int], e1: GR[org.joda.time.LocalDateTime], e2: GR[String], e3: GR[Boolean]): GR[ThingsThingpersoncrossrefRow] = GR{
    prs => import prs._
    ThingsThingpersoncrossrefRow.tupled((<<[Int], <<[org.joda.time.LocalDateTime], <<[org.joda.time.LocalDateTime], <<[Int], <<[Int], <<[String], <<[Boolean], <<[Int]))
  }
  /** Table description of table things_thingpersoncrossref. Objects of this class serve as prototypes for rows in queries. */
  class ThingsThingpersoncrossref(_tableTag: Tag) extends Table[ThingsThingpersoncrossrefRow](_tableTag, Some("hat"), "things_thingpersoncrossref") {
    def * = (id, dateCreated, lastUpdated, personId, thingId, relationshipType, isCurrent, relationshiprecordId) <> (ThingsThingpersoncrossrefRow.tupled, ThingsThingpersoncrossrefRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(dateCreated), Rep.Some(lastUpdated), Rep.Some(personId), Rep.Some(thingId), Rep.Some(relationshipType), Rep.Some(isCurrent), Rep.Some(relationshiprecordId)).shaped.<>({r=>import r._; _1.map(_=> ThingsThingpersoncrossrefRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get, _7.get, _8.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(serial), AutoInc, PrimaryKey */
    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    /** Database column date_created SqlType(timestamp) */
    val dateCreated: Rep[org.joda.time.LocalDateTime] = column[org.joda.time.LocalDateTime]("date_created")
    /** Database column last_updated SqlType(timestamp) */
    val lastUpdated: Rep[org.joda.time.LocalDateTime] = column[org.joda.time.LocalDateTime]("last_updated")
    /** Database column person_id SqlType(int4) */
    val personId: Rep[Int] = column[Int]("person_id")
    /** Database column thing_id SqlType(int4) */
    val thingId: Rep[Int] = column[Int]("thing_id")
    /** Database column relationship_type SqlType(varchar), Length(100,true) */
    val relationshipType: Rep[String] = column[String]("relationship_type", O.Length(100,varying=true))
    /** Database column is_current SqlType(bool) */
    val isCurrent: Rep[Boolean] = column[Boolean]("is_current")
    /** Database column relationshiprecord_id SqlType(int4) */
    val relationshiprecordId: Rep[Int] = column[Int]("relationshiprecord_id")

    /** Foreign key referencing PeoplePerson (database name owner_id_refs_id) */
    lazy val peoplePersonFk = foreignKey("owner_id_refs_id", personId, PeoplePerson)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
    /** Foreign key referencing SystemRelationshiprecord (database name system_relationshiprecord_things_thingpersoncrossref_fk) */
    lazy val systemRelationshiprecordFk = foreignKey("system_relationshiprecord_things_thingpersoncrossref_fk", relationshiprecordId, SystemRelationshiprecord)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
    /** Foreign key referencing ThingsThing (database name things_thingpersoncrossref_thing_id_fkey) */
    lazy val thingsThingFk = foreignKey("things_thingpersoncrossref_thing_id_fkey", thingId, ThingsThing)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
  }
  /** Collection-like TableQuery object for table ThingsThingpersoncrossref */
  lazy val ThingsThingpersoncrossref = new TableQuery(tag => new ThingsThingpersoncrossref(tag))

  /** Entity class storing rows of table ThingsThingtothingcrossref
   *  @param id Database column id SqlType(serial), AutoInc, PrimaryKey
   *  @param dateCreated Database column date_created SqlType(timestamp)
   *  @param lastUpdated Database column last_updated SqlType(timestamp)
   *  @param thingOneId Database column thing_one_id SqlType(int4)
   *  @param thingTwoId Database column thing_two_id SqlType(int4)
   *  @param relationshipType Database column relationship_type SqlType(varchar), Length(100,true)
   *  @param isCurrent Database column is_current SqlType(bool)
   *  @param relationshiprecordId Database column relationshiprecord_id SqlType(int4) */
  case class ThingsThingtothingcrossrefRow(id: Int, dateCreated: org.joda.time.LocalDateTime, lastUpdated: org.joda.time.LocalDateTime, thingOneId: Int, thingTwoId: Int, relationshipType: String, isCurrent: Boolean, relationshiprecordId: Int)
  /** GetResult implicit for fetching ThingsThingtothingcrossrefRow objects using plain SQL queries */
  implicit def GetResultThingsThingtothingcrossrefRow(implicit e0: GR[Int], e1: GR[org.joda.time.LocalDateTime], e2: GR[String], e3: GR[Boolean]): GR[ThingsThingtothingcrossrefRow] = GR{
    prs => import prs._
    ThingsThingtothingcrossrefRow.tupled((<<[Int], <<[org.joda.time.LocalDateTime], <<[org.joda.time.LocalDateTime], <<[Int], <<[Int], <<[String], <<[Boolean], <<[Int]))
  }
  /** Table description of table things_thingtothingcrossref. Objects of this class serve as prototypes for rows in queries. */
  class ThingsThingtothingcrossref(_tableTag: Tag) extends Table[ThingsThingtothingcrossrefRow](_tableTag, Some("hat"), "things_thingtothingcrossref") {
    def * = (id, dateCreated, lastUpdated, thingOneId, thingTwoId, relationshipType, isCurrent, relationshiprecordId) <> (ThingsThingtothingcrossrefRow.tupled, ThingsThingtothingcrossrefRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(dateCreated), Rep.Some(lastUpdated), Rep.Some(thingOneId), Rep.Some(thingTwoId), Rep.Some(relationshipType), Rep.Some(isCurrent), Rep.Some(relationshiprecordId)).shaped.<>({r=>import r._; _1.map(_=> ThingsThingtothingcrossrefRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get, _7.get, _8.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(serial), AutoInc, PrimaryKey */
    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    /** Database column date_created SqlType(timestamp) */
    val dateCreated: Rep[org.joda.time.LocalDateTime] = column[org.joda.time.LocalDateTime]("date_created")
    /** Database column last_updated SqlType(timestamp) */
    val lastUpdated: Rep[org.joda.time.LocalDateTime] = column[org.joda.time.LocalDateTime]("last_updated")
    /** Database column thing_one_id SqlType(int4) */
    val thingOneId: Rep[Int] = column[Int]("thing_one_id")
    /** Database column thing_two_id SqlType(int4) */
    val thingTwoId: Rep[Int] = column[Int]("thing_two_id")
    /** Database column relationship_type SqlType(varchar), Length(100,true) */
    val relationshipType: Rep[String] = column[String]("relationship_type", O.Length(100,varying=true))
    /** Database column is_current SqlType(bool) */
    val isCurrent: Rep[Boolean] = column[Boolean]("is_current")
    /** Database column relationshiprecord_id SqlType(int4) */
    val relationshiprecordId: Rep[Int] = column[Int]("relationshiprecord_id")

    /** Foreign key referencing SystemRelationshiprecord (database name system_relationshiprecord_things_thingtothingcrossref_fk) */
    lazy val systemRelationshiprecordFk = foreignKey("system_relationshiprecord_things_thingtothingcrossref_fk", relationshiprecordId, SystemRelationshiprecord)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
    /** Foreign key referencing ThingsThing (database name thing_one_id_refs_id_fk) */
    lazy val thingsThingFk2 = foreignKey("thing_one_id_refs_id_fk", thingOneId, ThingsThing)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
    /** Foreign key referencing ThingsThing (database name thing_two_id_refs_id_fk) */
    lazy val thingsThingFk3 = foreignKey("thing_two_id_refs_id_fk", thingTwoId, ThingsThing)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
  }
  /** Collection-like TableQuery object for table ThingsThingtothingcrossref */
  lazy val ThingsThingtothingcrossref = new TableQuery(tag => new ThingsThingtothingcrossref(tag))

  /** Entity class storing rows of table UserAccessToken
   *  @param accessToken Database column access_token SqlType(varchar), PrimaryKey
   *  @param userId Database column user_id SqlType(uuid)
   *  @param scope Database column scope SqlType(varchar), Default()
   *  @param resource Database column resource SqlType(varchar), Default() */
  case class UserAccessTokenRow(accessToken: String, userId: java.util.UUID, scope: String = "", resource: String = "")
  /** GetResult implicit for fetching UserAccessTokenRow objects using plain SQL queries */
  implicit def GetResultUserAccessTokenRow(implicit e0: GR[String], e1: GR[java.util.UUID]): GR[UserAccessTokenRow] = GR{
    prs => import prs._
    UserAccessTokenRow.tupled((<<[String], <<[java.util.UUID], <<[String], <<[String]))
  }
  /** Table description of table user_access_token. Objects of this class serve as prototypes for rows in queries. */
  class UserAccessToken(_tableTag: Tag) extends Table[UserAccessTokenRow](_tableTag, Some("hat"), "user_access_token") {
    def * = (accessToken, userId, scope, resource) <> (UserAccessTokenRow.tupled, UserAccessTokenRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(accessToken), Rep.Some(userId), Rep.Some(scope), Rep.Some(resource)).shaped.<>({r=>import r._; _1.map(_=> UserAccessTokenRow.tupled((_1.get, _2.get, _3.get, _4.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column access_token SqlType(varchar), PrimaryKey */
    val accessToken: Rep[String] = column[String]("access_token", O.PrimaryKey)
    /** Database column user_id SqlType(uuid) */
    val userId: Rep[java.util.UUID] = column[java.util.UUID]("user_id")
    /** Database column scope SqlType(varchar), Default() */
    val scope: Rep[String] = column[String]("scope", O.Default(""))
    /** Database column resource SqlType(varchar), Default() */
    val resource: Rep[String] = column[String]("resource", O.Default(""))

    /** Foreign key referencing UserUser (database name user_fk) */
    lazy val userUserFk = foreignKey("user_fk", userId, UserUser)(r => r.userId, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
  }
  /** Collection-like TableQuery object for table UserAccessToken */
  lazy val UserAccessToken = new TableQuery(tag => new UserAccessToken(tag))

  /** Entity class storing rows of table UserUser
   *  @param userId Database column user_id SqlType(uuid), PrimaryKey
   *  @param dateCreated Database column date_created SqlType(timestamp)
   *  @param lastUpdated Database column last_updated SqlType(timestamp)
   *  @param email Database column email SqlType(varchar)
   *  @param pass Database column pass SqlType(varchar), Default(None)
   *  @param name Database column name SqlType(varchar)
   *  @param role Database column role SqlType(varchar)
   *  @param enabled Database column enabled SqlType(bool), Default(false) */
  case class UserUserRow(userId: java.util.UUID, dateCreated: org.joda.time.LocalDateTime, lastUpdated: org.joda.time.LocalDateTime, email: String, pass: Option[String] = None, name: String, role: String, enabled: Boolean = false)
  /** GetResult implicit for fetching UserUserRow objects using plain SQL queries */
  implicit def GetResultUserUserRow(implicit e0: GR[java.util.UUID], e1: GR[org.joda.time.LocalDateTime], e2: GR[String], e3: GR[Option[String]], e4: GR[Boolean]): GR[UserUserRow] = GR{
    prs => import prs._
    UserUserRow.tupled((<<[java.util.UUID], <<[org.joda.time.LocalDateTime], <<[org.joda.time.LocalDateTime], <<[String], <<?[String], <<[String], <<[String], <<[Boolean]))
  }
  /** Table description of table user_user. Objects of this class serve as prototypes for rows in queries. */
  class UserUser(_tableTag: Tag) extends Table[UserUserRow](_tableTag, Some("hat"), "user_user") {
    def * = (userId, dateCreated, lastUpdated, email, pass, name, role, enabled) <> (UserUserRow.tupled, UserUserRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(userId), Rep.Some(dateCreated), Rep.Some(lastUpdated), Rep.Some(email), pass, Rep.Some(name), Rep.Some(role), Rep.Some(enabled)).shaped.<>({r=>import r._; _1.map(_=> UserUserRow.tupled((_1.get, _2.get, _3.get, _4.get, _5, _6.get, _7.get, _8.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column user_id SqlType(uuid), PrimaryKey */
    val userId: Rep[java.util.UUID] = column[java.util.UUID]("user_id", O.PrimaryKey)
    /** Database column date_created SqlType(timestamp) */
    val dateCreated: Rep[org.joda.time.LocalDateTime] = column[org.joda.time.LocalDateTime]("date_created")
    /** Database column last_updated SqlType(timestamp) */
    val lastUpdated: Rep[org.joda.time.LocalDateTime] = column[org.joda.time.LocalDateTime]("last_updated")
    /** Database column email SqlType(varchar) */
    val email: Rep[String] = column[String]("email")
    /** Database column pass SqlType(varchar), Default(None) */
    val pass: Rep[Option[String]] = column[Option[String]]("pass", O.Default(None))
    /** Database column name SqlType(varchar) */
    val name: Rep[String] = column[String]("name")
    /** Database column role SqlType(varchar) */
    val role: Rep[String] = column[String]("role")
    /** Database column enabled SqlType(bool), Default(false) */
    val enabled: Rep[Boolean] = column[Boolean]("enabled", O.Default(false))
  }
  /** Collection-like TableQuery object for table UserUser */
  lazy val UserUser = new TableQuery(tag => new UserUser(tag))
}
