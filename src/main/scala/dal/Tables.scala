package dal
// AUTO-GENERATED Slick data model
/** Stand-alone Slick data model for immediate use */
object Tables extends {
  val profile = dal.SlickPostgresDriver
} with Tables
/** Slick data model trait for extension, choice of backend or usage in the cake pattern. (Make sure to initialize this late.) */
trait Tables {
  val profile: dal.SlickPostgresDriver
  import profile.simple._
  import slick.model.ForeignKeyAction
  // NOTE: GetResult mappers for plain SQL are only generated for tables where Slick knows how to map the types of all columns.
  import slick.jdbc.{GetResult => GR}

  /** DDL for all tables. Call .create to execute. */
  lazy val schema = Array(BundleContext.schema, BundleContextless.schema, BundleJoin.schema, BundlePropertylice.schema, BundlePropertylicecondition.schema, BundlePropertyrecordCrossref.schema, BundleTable.schema, BundleTableslice.schema, BundleTableslicecondition.schema, DataDebit.schema, DataField.schema, DataRecord.schema, DataTable.schema, DataTabletotablecrossref.schema, DataValue.schema, Entity.schema, EntitySelection.schema, EventsEvent.schema, EventsEventlocationcrossref.schema, EventsEventorganisationcrossref.schema, EventsEventpersoncrossref.schema, EventsEventthingcrossref.schema, EventsEventtoeventcrossref.schema, EventsSystempropertydynamiccrossref.schema, EventsSystempropertystaticcrossref.schema, EventsSystemtypecrossref.schema, LocationsLocation.schema, LocationsLocationthingcrossref.schema, LocationsLocationtolocationcrossref.schema, LocationsSystempropertydynamiccrossref.schema, LocationsSystempropertystaticcrossref.schema, LocationsSystemtypecrossref.schema, OrganisationsOrganisation.schema, OrganisationsOrganisationlocationcrossref.schema, OrganisationsOrganisationthingcrossref.schema, OrganisationsOrganisationtoorganisationcrossref.schema, OrganisationsSystempropertydynamiccrossref.schema, OrganisationsSystempropertystaticcrossref.schema, OrganisationSystemtypecrossref.schema, PeoplePerson.schema, PeoplePersonlocationcrossref.schema, PeoplePersonorganisationcrossref.schema, PeoplePersontopersoncrossref.schema, PeoplePersontopersonrelationshiptype.schema, PeopleSystempropertydynamiccrossref.schema, PeopleSystempropertystaticcrossref.schema, PeopleSystemtypecrossref.schema, SystemEventlog.schema, SystemProperty.schema, SystemPropertyrecord.schema, SystemRelationshiprecord.schema, SystemRelationshiprecordtorecordcrossref.schema, SystemType.schema, SystemTypetotypecrossref.schema, SystemUnitofmeasurement.schema, ThingsSystempropertydynamiccrossref.schema, ThingsSystempropertystaticcrossref.schema, ThingsSystemtypecrossref.schema, ThingsThing.schema, ThingsThingpersoncrossref.schema, ThingsThingtothingcrossref.schema).reduceLeft(_ ++ _)
  @deprecated("Use .schema instead of .ddl", "3.0")
  def ddl = schema

  /** Entity class storing rows of table BundleContext
   *  @param id Database column id SqlType(serial), AutoInc, PrimaryKey
   *  @param parentBundleId Database column parent_bundle_id SqlType(int4)
   *  @param dateCreated Database column date_created SqlType(timestamp)
   *  @param lastUpdated Database column last_updated SqlType(timestamp)
   *  @param name Database column name SqlType(varchar)
   *  @param entitySelectionId Database column entity_selection_id SqlType(int4) */
  case class BundleContextRow(id: Int, parentBundleId: Int, dateCreated: org.joda.time.LocalDateTime, lastUpdated: org.joda.time.LocalDateTime, name: String, entitySelectionId: Int)
  /** GetResult implicit for fetching BundleContextRow objects using plain SQL queries */
  implicit def GetResultBundleContextRow(implicit e0: GR[Int], e1: GR[org.joda.time.LocalDateTime], e2: GR[String]): GR[BundleContextRow] = GR{
    prs => import prs._
    BundleContextRow.tupled((<<[Int], <<[Int], <<[org.joda.time.LocalDateTime], <<[org.joda.time.LocalDateTime], <<[String], <<[Int]))
  }
  /** Table description of table bundle_context. Objects of this class serve as prototypes for rows in queries. */
  class BundleContext(_tableTag: Tag) extends Table[BundleContextRow](_tableTag, "bundle_context") {
    def * = (id, parentBundleId, dateCreated, lastUpdated, name, entitySelectionId) <> (BundleContextRow.tupled, BundleContextRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(parentBundleId), Rep.Some(dateCreated), Rep.Some(lastUpdated), Rep.Some(name), Rep.Some(entitySelectionId)).shaped.<>({r=>import r._; _1.map(_=> BundleContextRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(serial), AutoInc, PrimaryKey */
    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    /** Database column parent_bundle_id SqlType(int4) */
    val parentBundleId: Rep[Int] = column[Int]("parent_bundle_id")
    /** Database column date_created SqlType(timestamp) */
    val dateCreated: Rep[org.joda.time.LocalDateTime] = column[org.joda.time.LocalDateTime]("date_created")
    /** Database column last_updated SqlType(timestamp) */
    val lastUpdated: Rep[org.joda.time.LocalDateTime] = column[org.joda.time.LocalDateTime]("last_updated")
    /** Database column name SqlType(varchar) */
    val name: Rep[String] = column[String]("name")
    /** Database column entity_selection_id SqlType(int4) */
    val entitySelectionId: Rep[Int] = column[Int]("entity_selection_id")

    /** Foreign key referencing BundleContext (database name bundle_context_bundle_context_fk) */
    lazy val bundleContextFk = foreignKey("bundle_context_bundle_context_fk", parentBundleId, BundleContext)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
    /** Foreign key referencing EntitySelection (database name entity_selection_bundle_context_fk) */
    lazy val entitySelectionFk = foreignKey("entity_selection_bundle_context_fk", entitySelectionId, EntitySelection)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
  }
  /** Collection-like TableQuery object for table BundleContext */
  lazy val BundleContext = new TableQuery(tag => new BundleContext(tag))

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
  class BundleContextless(_tableTag: Tag) extends Table[BundleContextlessRow](_tableTag, "bundle_contextless") {
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

  /** Entity class storing rows of table BundleJoin
   *  @param id Database column id SqlType(serial), AutoInc, PrimaryKey
   *  @param dateCreated Database column date_created SqlType(timestamp)
   *  @param lastUpdated Database column last_updated SqlType(timestamp)
   *  @param bundleTableId Database column bundle_table_id SqlType(int4)
   *  @param bundleId Database column bundle_id SqlType(int4)
   *  @param bundleJoinField Database column bundle_join_field SqlType(int4)
   *  @param bundleTableField Database column bundle_table_field SqlType(int4)
   *  @param operator Database column operator SqlType(varchar), Default(None) */
  case class BundleJoinRow(id: Int, dateCreated: org.joda.time.LocalDateTime, lastUpdated: org.joda.time.LocalDateTime, bundleTableId: Int, bundleId: Int, bundleJoinField: Int, bundleTableField: Int, operator: Option[String] = None)
  /** GetResult implicit for fetching BundleJoinRow objects using plain SQL queries */
  implicit def GetResultBundleJoinRow(implicit e0: GR[Int], e1: GR[org.joda.time.LocalDateTime], e2: GR[Option[String]]): GR[BundleJoinRow] = GR{
    prs => import prs._
    BundleJoinRow.tupled((<<[Int], <<[org.joda.time.LocalDateTime], <<[org.joda.time.LocalDateTime], <<[Int], <<[Int], <<[Int], <<[Int], <<?[String]))
  }
  /** Table description of table bundle_join. Objects of this class serve as prototypes for rows in queries. */
  class BundleJoin(_tableTag: Tag) extends Table[BundleJoinRow](_tableTag, "bundle_join") {
    def * = (id, dateCreated, lastUpdated, bundleTableId, bundleId, bundleJoinField, bundleTableField, operator) <> (BundleJoinRow.tupled, BundleJoinRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(dateCreated), Rep.Some(lastUpdated), Rep.Some(bundleTableId), Rep.Some(bundleId), Rep.Some(bundleJoinField), Rep.Some(bundleTableField), operator).shaped.<>({r=>import r._; _1.map(_=> BundleJoinRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get, _7.get, _8)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(serial), AutoInc, PrimaryKey */
    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    /** Database column date_created SqlType(timestamp) */
    val dateCreated: Rep[org.joda.time.LocalDateTime] = column[org.joda.time.LocalDateTime]("date_created")
    /** Database column last_updated SqlType(timestamp) */
    val lastUpdated: Rep[org.joda.time.LocalDateTime] = column[org.joda.time.LocalDateTime]("last_updated")
    /** Database column bundle_table_id SqlType(int4) */
    val bundleTableId: Rep[Int] = column[Int]("bundle_table_id")
    /** Database column bundle_id SqlType(int4) */
    val bundleId: Rep[Int] = column[Int]("bundle_id")
    /** Database column bundle_join_field SqlType(int4) */
    val bundleJoinField: Rep[Int] = column[Int]("bundle_join_field")
    /** Database column bundle_table_field SqlType(int4) */
    val bundleTableField: Rep[Int] = column[Int]("bundle_table_field")
    /** Database column operator SqlType(varchar), Default(None) */
    val operator: Rep[Option[String]] = column[Option[String]]("operator", O.Default(None))

    /** Foreign key referencing BundleContextless (database name acontextual_bundle_bundle_join_fk) */
    lazy val bundleContextlessFk = foreignKey("acontextual_bundle_bundle_join_fk", bundleId, BundleContextless)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
    /** Foreign key referencing BundleTable (database name bundle_table_bundle_join_fk) */
    lazy val bundleTableFk = foreignKey("bundle_table_bundle_join_fk", bundleTableId, BundleTable)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
    /** Foreign key referencing DataField (database name data_field_bundle_join_fk) */
    lazy val dataFieldFk3 = foreignKey("data_field_bundle_join_fk", bundleJoinField, DataField)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
    /** Foreign key referencing DataField (database name data_field_bundle_join_fk1) */
    lazy val dataFieldFk4 = foreignKey("data_field_bundle_join_fk1", bundleTableField, DataField)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
  }
  /** Collection-like TableQuery object for table BundleJoin */
  lazy val BundleJoin = new TableQuery(tag => new BundleJoin(tag))

  /** Entity class storing rows of table BundlePropertylice
   *  @param id Database column id SqlType(serial), AutoInc, PrimaryKey
   *  @param name Database column name SqlType(varchar)
   *  @param bundlePropertyrecordCrossrefId Database column bundle_propertyrecord_crossref_id SqlType(int4) */
  case class BundlePropertyliceRow(id: Int, name: String, bundlePropertyrecordCrossrefId: Int)
  /** GetResult implicit for fetching BundlePropertyliceRow objects using plain SQL queries */
  implicit def GetResultBundlePropertyliceRow(implicit e0: GR[Int], e1: GR[String]): GR[BundlePropertyliceRow] = GR{
    prs => import prs._
    BundlePropertyliceRow.tupled((<<[Int], <<[String], <<[Int]))
  }
  /** Table description of table bundle_propertylice. Objects of this class serve as prototypes for rows in queries. */
  class BundlePropertylice(_tableTag: Tag) extends Table[BundlePropertyliceRow](_tableTag, "bundle_propertylice") {
    def * = (id, name, bundlePropertyrecordCrossrefId) <> (BundlePropertyliceRow.tupled, BundlePropertyliceRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(name), Rep.Some(bundlePropertyrecordCrossrefId)).shaped.<>({r=>import r._; _1.map(_=> BundlePropertyliceRow.tupled((_1.get, _2.get, _3.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(serial), AutoInc, PrimaryKey */
    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    /** Database column name SqlType(varchar) */
    val name: Rep[String] = column[String]("name")
    /** Database column bundle_propertyrecord_crossref_id SqlType(int4) */
    val bundlePropertyrecordCrossrefId: Rep[Int] = column[Int]("bundle_propertyrecord_crossref_id")

    /** Foreign key referencing BundlePropertyrecordCrossref (database name bundle_propertyrecord_crossref_contextual_bundlepropertyslic823) */
    lazy val bundlePropertyrecordCrossrefFk = foreignKey("bundle_propertyrecord_crossref_contextual_bundlepropertyslic823", bundlePropertyrecordCrossrefId, BundlePropertyrecordCrossref)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
  }
  /** Collection-like TableQuery object for table BundlePropertylice */
  lazy val BundlePropertylice = new TableQuery(tag => new BundlePropertylice(tag))

  /** Entity class storing rows of table BundlePropertylicecondition
   *  @param id Database column id SqlType(serial), AutoInc, PrimaryKey
   *  @param propertysliceId Database column propertyslice_id SqlType(int4)
   *  @param operator Database column operator SqlType(varchar)
   *  @param value Database column value SqlType(varchar) */
  case class BundlePropertyliceconditionRow(id: Int, propertysliceId: Int, operator: String, value: String)
  /** GetResult implicit for fetching BundlePropertyliceconditionRow objects using plain SQL queries */
  implicit def GetResultBundlePropertyliceconditionRow(implicit e0: GR[Int], e1: GR[String]): GR[BundlePropertyliceconditionRow] = GR{
    prs => import prs._
    BundlePropertyliceconditionRow.tupled((<<[Int], <<[Int], <<[String], <<[String]))
  }
  /** Table description of table bundle_propertylicecondition. Objects of this class serve as prototypes for rows in queries. */
  class BundlePropertylicecondition(_tableTag: Tag) extends Table[BundlePropertyliceconditionRow](_tableTag, "bundle_propertylicecondition") {
    def * = (id, propertysliceId, operator, value) <> (BundlePropertyliceconditionRow.tupled, BundlePropertyliceconditionRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(propertysliceId), Rep.Some(operator), Rep.Some(value)).shaped.<>({r=>import r._; _1.map(_=> BundlePropertyliceconditionRow.tupled((_1.get, _2.get, _3.get, _4.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(serial), AutoInc, PrimaryKey */
    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    /** Database column propertyslice_id SqlType(int4) */
    val propertysliceId: Rep[Int] = column[Int]("propertyslice_id")
    /** Database column operator SqlType(varchar) */
    val operator: Rep[String] = column[String]("operator")
    /** Database column value SqlType(varchar) */
    val value: Rep[String] = column[String]("value")

    /** Foreign key referencing BundlePropertylice (database name bundle_propertyslice_bundle_propertyslicecondition_fk) */
    lazy val bundlePropertyliceFk = foreignKey("bundle_propertyslice_bundle_propertyslicecondition_fk", propertysliceId, BundlePropertylice)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
  }
  /** Collection-like TableQuery object for table BundlePropertylicecondition */
  lazy val BundlePropertylicecondition = new TableQuery(tag => new BundlePropertylicecondition(tag))

  /** Entity class storing rows of table BundlePropertyrecordCrossref
   *  @param id Database column id SqlType(serial), AutoInc, PrimaryKey
   *  @param dateCreated Database column date_created SqlType(timestamp)
   *  @param lastUpdated Database column last_updated SqlType(timestamp)
   *  @param propertyrecordId Database column propertyrecord_id SqlType(int4)
   *  @param bundleContextId Database column bundle_context_id SqlType(int4) */
  case class BundlePropertyrecordCrossrefRow(id: Int, dateCreated: org.joda.time.LocalDateTime, lastUpdated: org.joda.time.LocalDateTime, propertyrecordId: Int, bundleContextId: Int)
  /** GetResult implicit for fetching BundlePropertyrecordCrossrefRow objects using plain SQL queries */
  implicit def GetResultBundlePropertyrecordCrossrefRow(implicit e0: GR[Int], e1: GR[org.joda.time.LocalDateTime]): GR[BundlePropertyrecordCrossrefRow] = GR{
    prs => import prs._
    BundlePropertyrecordCrossrefRow.tupled((<<[Int], <<[org.joda.time.LocalDateTime], <<[org.joda.time.LocalDateTime], <<[Int], <<[Int]))
  }
  /** Table description of table bundle_propertyrecord_crossref. Objects of this class serve as prototypes for rows in queries. */
  class BundlePropertyrecordCrossref(_tableTag: Tag) extends Table[BundlePropertyrecordCrossrefRow](_tableTag, "bundle_propertyrecord_crossref") {
    def * = (id, dateCreated, lastUpdated, propertyrecordId, bundleContextId) <> (BundlePropertyrecordCrossrefRow.tupled, BundlePropertyrecordCrossrefRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(dateCreated), Rep.Some(lastUpdated), Rep.Some(propertyrecordId), Rep.Some(bundleContextId)).shaped.<>({r=>import r._; _1.map(_=> BundlePropertyrecordCrossrefRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(serial), AutoInc, PrimaryKey */
    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    /** Database column date_created SqlType(timestamp) */
    val dateCreated: Rep[org.joda.time.LocalDateTime] = column[org.joda.time.LocalDateTime]("date_created")
    /** Database column last_updated SqlType(timestamp) */
    val lastUpdated: Rep[org.joda.time.LocalDateTime] = column[org.joda.time.LocalDateTime]("last_updated")
    /** Database column propertyrecord_id SqlType(int4) */
    val propertyrecordId: Rep[Int] = column[Int]("propertyrecord_id")
    /** Database column bundle_context_id SqlType(int4) */
    val bundleContextId: Rep[Int] = column[Int]("bundle_context_id")

    /** Foreign key referencing BundleContext (database name bundle_context_system_propertyrecordtobundlecrrossref_fk) */
    lazy val bundleContextFk = foreignKey("bundle_context_system_propertyrecordtobundlecrrossref_fk", bundleContextId, BundleContext)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
    /** Foreign key referencing SystemPropertyrecord (database name system_propertyrecord_system_propertyrecordtobundlecrrossref_fk) */
    lazy val systemPropertyrecordFk = foreignKey("system_propertyrecord_system_propertyrecordtobundlecrrossref_fk", propertyrecordId, SystemPropertyrecord)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
  }
  /** Collection-like TableQuery object for table BundlePropertyrecordCrossref */
  lazy val BundlePropertyrecordCrossref = new TableQuery(tag => new BundlePropertyrecordCrossref(tag))

  /** Entity class storing rows of table BundleTable
   *  @param id Database column id SqlType(serial), AutoInc, PrimaryKey
   *  @param lastUpdated Database column last_updated SqlType(timestamp)
   *  @param dateCreated Database column date_created SqlType(timestamp)
   *  @param name Database column name SqlType(varchar)
   *  @param dataTable Database column data_table SqlType(int4) */
  case class BundleTableRow(id: Int, lastUpdated: org.joda.time.LocalDateTime, dateCreated: org.joda.time.LocalDateTime, name: String, dataTable: Int)
  /** GetResult implicit for fetching BundleTableRow objects using plain SQL queries */
  implicit def GetResultBundleTableRow(implicit e0: GR[Int], e1: GR[org.joda.time.LocalDateTime], e2: GR[String]): GR[BundleTableRow] = GR{
    prs => import prs._
    BundleTableRow.tupled((<<[Int], <<[org.joda.time.LocalDateTime], <<[org.joda.time.LocalDateTime], <<[String], <<[Int]))
  }
  /** Table description of table bundle_table. Objects of this class serve as prototypes for rows in queries. */
  class BundleTable(_tableTag: Tag) extends Table[BundleTableRow](_tableTag, "bundle_table") {
    def * = (id, lastUpdated, dateCreated, name, dataTable) <> (BundleTableRow.tupled, BundleTableRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(lastUpdated), Rep.Some(dateCreated), Rep.Some(name), Rep.Some(dataTable)).shaped.<>({r=>import r._; _1.map(_=> BundleTableRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(serial), AutoInc, PrimaryKey */
    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    /** Database column last_updated SqlType(timestamp) */
    val lastUpdated: Rep[org.joda.time.LocalDateTime] = column[org.joda.time.LocalDateTime]("last_updated")
    /** Database column date_created SqlType(timestamp) */
    val dateCreated: Rep[org.joda.time.LocalDateTime] = column[org.joda.time.LocalDateTime]("date_created")
    /** Database column name SqlType(varchar) */
    val name: Rep[String] = column[String]("name")
    /** Database column data_table SqlType(int4) */
    val dataTable: Rep[Int] = column[Int]("data_table")

    /** Foreign key referencing DataTable (database name data_table_bundle_table_fk) */
    lazy val dataTableFk = foreignKey("data_table_bundle_table_fk", dataTable, DataTable)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
  }
  /** Collection-like TableQuery object for table BundleTable */
  lazy val BundleTable = new TableQuery(tag => new BundleTable(tag))

  /** Entity class storing rows of table BundleTableslice
   *  @param id Database column id SqlType(serial), AutoInc, PrimaryKey
   *  @param dateCreated Database column date_created SqlType(timestamp)
   *  @param lastUpdated Database column last_updated SqlType(timestamp)
   *  @param name Database column name SqlType(varchar)
   *  @param bundleTableId Database column bundle_table_id SqlType(int4)
   *  @param dataTableId Database column data_table_id SqlType(int4) */
  case class BundleTablesliceRow(id: Int, dateCreated: org.joda.time.LocalDateTime, lastUpdated: org.joda.time.LocalDateTime, name: String, bundleTableId: Int, dataTableId: Int)
  /** GetResult implicit for fetching BundleTablesliceRow objects using plain SQL queries */
  implicit def GetResultBundleTablesliceRow(implicit e0: GR[Int], e1: GR[org.joda.time.LocalDateTime], e2: GR[String]): GR[BundleTablesliceRow] = GR{
    prs => import prs._
    BundleTablesliceRow.tupled((<<[Int], <<[org.joda.time.LocalDateTime], <<[org.joda.time.LocalDateTime], <<[String], <<[Int], <<[Int]))
  }
  /** Table description of table bundle_tableslice. Objects of this class serve as prototypes for rows in queries. */
  class BundleTableslice(_tableTag: Tag) extends Table[BundleTablesliceRow](_tableTag, "bundle_tableslice") {
    def * = (id, dateCreated, lastUpdated, name, bundleTableId, dataTableId) <> (BundleTablesliceRow.tupled, BundleTablesliceRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(dateCreated), Rep.Some(lastUpdated), Rep.Some(name), Rep.Some(bundleTableId), Rep.Some(dataTableId)).shaped.<>({r=>import r._; _1.map(_=> BundleTablesliceRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(serial), AutoInc, PrimaryKey */
    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    /** Database column date_created SqlType(timestamp) */
    val dateCreated: Rep[org.joda.time.LocalDateTime] = column[org.joda.time.LocalDateTime]("date_created")
    /** Database column last_updated SqlType(timestamp) */
    val lastUpdated: Rep[org.joda.time.LocalDateTime] = column[org.joda.time.LocalDateTime]("last_updated")
    /** Database column name SqlType(varchar) */
    val name: Rep[String] = column[String]("name")
    /** Database column bundle_table_id SqlType(int4) */
    val bundleTableId: Rep[Int] = column[Int]("bundle_table_id")
    /** Database column data_table_id SqlType(int4) */
    val dataTableId: Rep[Int] = column[Int]("data_table_id")

    /** Foreign key referencing BundleTable (database name bundle_table_bundle_tableslice_fk) */
    lazy val bundleTableFk = foreignKey("bundle_table_bundle_tableslice_fk", bundleTableId, BundleTable)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
    /** Foreign key referencing DataTable (database name data_table_bundle_tableslice_fk) */
    lazy val dataTableFk = foreignKey("data_table_bundle_tableslice_fk", dataTableId, DataTable)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
  }
  /** Collection-like TableQuery object for table BundleTableslice */
  lazy val BundleTableslice = new TableQuery(tag => new BundleTableslice(tag))

  /** Entity class storing rows of table BundleTableslicecondition
   *  @param id Database column id SqlType(serial), AutoInc, PrimaryKey
   *  @param dateCreated Database column date_created SqlType(timestamp)
   *  @param lastUpdated Database column last_updated SqlType(timestamp)
   *  @param fieldId Database column field_id SqlType(int4)
   *  @param tablesliceId Database column tableslice_id SqlType(int4)
   *  @param operator Database column operator SqlType(varchar)
   *  @param value Database column value SqlType(varchar) */
  case class BundleTablesliceconditionRow(id: Int, dateCreated: org.joda.time.LocalDateTime, lastUpdated: org.joda.time.LocalDateTime, fieldId: Int, tablesliceId: Int, operator: String, value: String)
  /** GetResult implicit for fetching BundleTablesliceconditionRow objects using plain SQL queries */
  implicit def GetResultBundleTablesliceconditionRow(implicit e0: GR[Int], e1: GR[org.joda.time.LocalDateTime], e2: GR[String]): GR[BundleTablesliceconditionRow] = GR{
    prs => import prs._
    BundleTablesliceconditionRow.tupled((<<[Int], <<[org.joda.time.LocalDateTime], <<[org.joda.time.LocalDateTime], <<[Int], <<[Int], <<[String], <<[String]))
  }
  /** Table description of table bundle_tableslicecondition. Objects of this class serve as prototypes for rows in queries. */
  class BundleTableslicecondition(_tableTag: Tag) extends Table[BundleTablesliceconditionRow](_tableTag, "bundle_tableslicecondition") {
    def * = (id, dateCreated, lastUpdated, fieldId, tablesliceId, operator, value) <> (BundleTablesliceconditionRow.tupled, BundleTablesliceconditionRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(dateCreated), Rep.Some(lastUpdated), Rep.Some(fieldId), Rep.Some(tablesliceId), Rep.Some(operator), Rep.Some(value)).shaped.<>({r=>import r._; _1.map(_=> BundleTablesliceconditionRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get, _7.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(serial), AutoInc, PrimaryKey */
    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    /** Database column date_created SqlType(timestamp) */
    val dateCreated: Rep[org.joda.time.LocalDateTime] = column[org.joda.time.LocalDateTime]("date_created")
    /** Database column last_updated SqlType(timestamp) */
    val lastUpdated: Rep[org.joda.time.LocalDateTime] = column[org.joda.time.LocalDateTime]("last_updated")
    /** Database column field_id SqlType(int4) */
    val fieldId: Rep[Int] = column[Int]("field_id")
    /** Database column tableslice_id SqlType(int4) */
    val tablesliceId: Rep[Int] = column[Int]("tableslice_id")
    /** Database column operator SqlType(varchar) */
    val operator: Rep[String] = column[String]("operator")
    /** Database column value SqlType(varchar) */
    val value: Rep[String] = column[String]("value")

    /** Foreign key referencing BundleTableslice (database name bundle_tableslice_bundle_tableslicecondition_fk) */
    lazy val bundleTablesliceFk = foreignKey("bundle_tableslice_bundle_tableslicecondition_fk", tablesliceId, BundleTableslice)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
    /** Foreign key referencing DataField (database name data_field_bundle_tableslicecondition_fk) */
    lazy val dataFieldFk = foreignKey("data_field_bundle_tableslicecondition_fk", fieldId, DataField)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
  }
  /** Collection-like TableQuery object for table BundleTableslicecondition */
  lazy val BundleTableslicecondition = new TableQuery(tag => new BundleTableslicecondition(tag))

  /** Entity class storing rows of table DataDebit
   *  @param id Database column id SqlType(serial), AutoInc, PrimaryKey
   *  @param dateCreated Database column date_created SqlType(timestamp)
   *  @param lastUpdated Database column last_updated SqlType(timestamp)
   *  @param name Database column name SqlType(varchar)
   *  @param startDate Database column start_date SqlType(timestamp)
   *  @param endDate Database column end_date SqlType(timestamp)
   *  @param rolling Database column rolling SqlType(bool)
   *  @param sellRent Database column sell_rent SqlType(bool)
   *  @param price Database column price SqlType(float4)
   *  @param dataDebitKey Database column data_debit_key SqlType(varchar)
   *  @param senderId Database column sender_id SqlType(varchar), Length(36,true)
   *  @param recipientId Database column recipient_id SqlType(varchar), Length(36,true) */
  case class DataDebitRow(id: Int, dateCreated: org.joda.time.LocalDateTime, lastUpdated: org.joda.time.LocalDateTime, name: String, startDate: org.joda.time.LocalDateTime, endDate: org.joda.time.LocalDateTime, rolling: Boolean, sellRent: Boolean, price: Float, dataDebitKey: String, senderId: String, recipientId: String)
  /** GetResult implicit for fetching DataDebitRow objects using plain SQL queries */
  implicit def GetResultDataDebitRow(implicit e0: GR[Int], e1: GR[org.joda.time.LocalDateTime], e2: GR[String], e3: GR[Boolean], e4: GR[Float]): GR[DataDebitRow] = GR{
    prs => import prs._
    DataDebitRow.tupled((<<[Int], <<[org.joda.time.LocalDateTime], <<[org.joda.time.LocalDateTime], <<[String], <<[org.joda.time.LocalDateTime], <<[org.joda.time.LocalDateTime], <<[Boolean], <<[Boolean], <<[Float], <<[String], <<[String], <<[String]))
  }
  /** Table description of table data_debit. Objects of this class serve as prototypes for rows in queries. */
  class DataDebit(_tableTag: Tag) extends Table[DataDebitRow](_tableTag, "data_debit") {
    def * = (id, dateCreated, lastUpdated, name, startDate, endDate, rolling, sellRent, price, dataDebitKey, senderId, recipientId) <> (DataDebitRow.tupled, DataDebitRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(dateCreated), Rep.Some(lastUpdated), Rep.Some(name), Rep.Some(startDate), Rep.Some(endDate), Rep.Some(rolling), Rep.Some(sellRent), Rep.Some(price), Rep.Some(dataDebitKey), Rep.Some(senderId), Rep.Some(recipientId)).shaped.<>({r=>import r._; _1.map(_=> DataDebitRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get, _7.get, _8.get, _9.get, _10.get, _11.get, _12.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(serial), AutoInc, PrimaryKey */
    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
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
    /** Database column data_debit_key SqlType(varchar) */
    val dataDebitKey: Rep[String] = column[String]("data_debit_key")
    /** Database column sender_id SqlType(varchar), Length(36,true) */
    val senderId: Rep[String] = column[String]("sender_id", O.Length(36,varying=true))
    /** Database column recipient_id SqlType(varchar), Length(36,true) */
    val recipientId: Rep[String] = column[String]("recipient_id", O.Length(36,varying=true))
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
  class DataField(_tableTag: Tag) extends Table[DataFieldRow](_tableTag, "data_field") {
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
  class DataRecord(_tableTag: Tag) extends Table[DataRecordRow](_tableTag, "data_record") {
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
  class DataTable(_tableTag: Tag) extends Table[DataTableRow](_tableTag, "data_table") {
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
  class DataTabletotablecrossref(_tableTag: Tag) extends Table[DataTabletotablecrossrefRow](_tableTag, "data_tabletotablecrossref") {
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
  class DataValue(_tableTag: Tag) extends Table[DataValueRow](_tableTag, "data_value") {
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
   *  @param id Database column id SqlType(serial), AutoInc, PrimaryKey
   *  @param dateCreated Database column date_created SqlType(timestamp)
   *  @param lastUpdated Database column last_updated SqlType(timestamp)
   *  @param name Database column name SqlType(varchar), Length(100,true)
   *  @param kind Database column kind SqlType(varchar), Length(100,true)
   *  @param locationId Database column location_id SqlType(int4)
   *  @param thingId Database column thing_id SqlType(int4)
   *  @param eventId Database column event_id SqlType(int4)
   *  @param organisationId Database column organisation_id SqlType(int4)
   *  @param personId Database column person_id SqlType(int4) */
  case class EntityRow(id: Int, dateCreated: org.joda.time.LocalDateTime, lastUpdated: org.joda.time.LocalDateTime, name: String, kind: String, locationId: Int, thingId: Int, eventId: Int, organisationId: Int, personId: Int)
  /** GetResult implicit for fetching EntityRow objects using plain SQL queries */
  implicit def GetResultEntityRow(implicit e0: GR[Int], e1: GR[org.joda.time.LocalDateTime], e2: GR[String]): GR[EntityRow] = GR{
    prs => import prs._
    EntityRow.tupled((<<[Int], <<[org.joda.time.LocalDateTime], <<[org.joda.time.LocalDateTime], <<[String], <<[String], <<[Int], <<[Int], <<[Int], <<[Int], <<[Int]))
  }
  /** Table description of table entity. Objects of this class serve as prototypes for rows in queries. */
  class Entity(_tableTag: Tag) extends Table[EntityRow](_tableTag, "entity") {
    def * = (id, dateCreated, lastUpdated, name, kind, locationId, thingId, eventId, organisationId, personId) <> (EntityRow.tupled, EntityRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(dateCreated), Rep.Some(lastUpdated), Rep.Some(name), Rep.Some(kind), Rep.Some(locationId), Rep.Some(thingId), Rep.Some(eventId), Rep.Some(organisationId), Rep.Some(personId)).shaped.<>({r=>import r._; _1.map(_=> EntityRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get, _7.get, _8.get, _9.get, _10.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(serial), AutoInc, PrimaryKey */
    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    /** Database column date_created SqlType(timestamp) */
    val dateCreated: Rep[org.joda.time.LocalDateTime] = column[org.joda.time.LocalDateTime]("date_created")
    /** Database column last_updated SqlType(timestamp) */
    val lastUpdated: Rep[org.joda.time.LocalDateTime] = column[org.joda.time.LocalDateTime]("last_updated")
    /** Database column name SqlType(varchar), Length(100,true) */
    val name: Rep[String] = column[String]("name", O.Length(100,varying=true))
    /** Database column kind SqlType(varchar), Length(100,true) */
    val kind: Rep[String] = column[String]("kind", O.Length(100,varying=true))
    /** Database column location_id SqlType(int4) */
    val locationId: Rep[Int] = column[Int]("location_id")
    /** Database column thing_id SqlType(int4) */
    val thingId: Rep[Int] = column[Int]("thing_id")
    /** Database column event_id SqlType(int4) */
    val eventId: Rep[Int] = column[Int]("event_id")
    /** Database column organisation_id SqlType(int4) */
    val organisationId: Rep[Int] = column[Int]("organisation_id")
    /** Database column person_id SqlType(int4) */
    val personId: Rep[Int] = column[Int]("person_id")

    /** Foreign key referencing EventsEvent (database name events_event_entity_fk) */
    lazy val eventsEventFk = foreignKey("events_event_entity_fk", eventId, EventsEvent)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
    /** Foreign key referencing LocationsLocation (database name locations_location_entity_fk) */
    lazy val locationsLocationFk = foreignKey("locations_location_entity_fk", locationId, LocationsLocation)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
    /** Foreign key referencing OrganisationsOrganisation (database name organisations_organisation_entity_fk) */
    lazy val organisationsOrganisationFk = foreignKey("organisations_organisation_entity_fk", organisationId, OrganisationsOrganisation)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
    /** Foreign key referencing PeoplePerson (database name people_person_entity_fk) */
    lazy val peoplePersonFk = foreignKey("people_person_entity_fk", personId, PeoplePerson)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
    /** Foreign key referencing ThingsThing (database name things_thing_entity_fk) */
    lazy val thingsThingFk = foreignKey("things_thing_entity_fk", thingId, ThingsThing)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
  }
  /** Collection-like TableQuery object for table Entity */
  lazy val Entity = new TableQuery(tag => new Entity(tag))

  /** Entity class storing rows of table EntitySelection
   *  @param id Database column id SqlType(serial), AutoInc, PrimaryKey
   *  @param dateCreated Database column date_created SqlType(timestamp)
   *  @param lastUpdated Database column last_updated SqlType(timestamp)
   *  @param entityName Database column entity_name SqlType(varchar), Length(100,true)
   *  @param entityId Database column entity_id SqlType(int4)
   *  @param entityKind Database column entity_kind SqlType(varchar), Length(100,true) */
  case class EntitySelectionRow(id: Int, dateCreated: org.joda.time.LocalDateTime, lastUpdated: org.joda.time.LocalDateTime, entityName: String, entityId: Int, entityKind: String)
  /** GetResult implicit for fetching EntitySelectionRow objects using plain SQL queries */
  implicit def GetResultEntitySelectionRow(implicit e0: GR[Int], e1: GR[org.joda.time.LocalDateTime], e2: GR[String]): GR[EntitySelectionRow] = GR{
    prs => import prs._
    EntitySelectionRow.tupled((<<[Int], <<[org.joda.time.LocalDateTime], <<[org.joda.time.LocalDateTime], <<[String], <<[Int], <<[String]))
  }
  /** Table description of table entity_selection. Objects of this class serve as prototypes for rows in queries. */
  class EntitySelection(_tableTag: Tag) extends Table[EntitySelectionRow](_tableTag, "entity_selection") {
    def * = (id, dateCreated, lastUpdated, entityName, entityId, entityKind) <> (EntitySelectionRow.tupled, EntitySelectionRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(dateCreated), Rep.Some(lastUpdated), Rep.Some(entityName), Rep.Some(entityId), Rep.Some(entityKind)).shaped.<>({r=>import r._; _1.map(_=> EntitySelectionRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(serial), AutoInc, PrimaryKey */
    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    /** Database column date_created SqlType(timestamp) */
    val dateCreated: Rep[org.joda.time.LocalDateTime] = column[org.joda.time.LocalDateTime]("date_created")
    /** Database column last_updated SqlType(timestamp) */
    val lastUpdated: Rep[org.joda.time.LocalDateTime] = column[org.joda.time.LocalDateTime]("last_updated")
    /** Database column entity_name SqlType(varchar), Length(100,true) */
    val entityName: Rep[String] = column[String]("entity_name", O.Length(100,varying=true))
    /** Database column entity_id SqlType(int4) */
    val entityId: Rep[Int] = column[Int]("entity_id")
    /** Database column entity_kind SqlType(varchar), Length(100,true) */
    val entityKind: Rep[String] = column[String]("entity_kind", O.Length(100,varying=true))

    /** Foreign key referencing Entity (database name entity_entity_selection_fk) */
    lazy val entityFk = foreignKey("entity_entity_selection_fk", entityId, Entity)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
  }
  /** Collection-like TableQuery object for table EntitySelection */
  lazy val EntitySelection = new TableQuery(tag => new EntitySelection(tag))

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
  class EventsEvent(_tableTag: Tag) extends Table[EventsEventRow](_tableTag, "events_event") {
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
   *  @param relationshipType Database column relationship_type SqlType(varchar)
   *  @param isCurrent Database column is_current SqlType(bool)
   *  @param relationshiprecordId Database column relationshiprecord_id SqlType(int4) */
  case class EventsEventlocationcrossrefRow(id: Int, dateCreated: org.joda.time.LocalDateTime, lastUpdated: org.joda.time.LocalDateTime, locationId: Int, eventId: Int, relationshipType: String, isCurrent: Boolean, relationshiprecordId: Int)
  /** GetResult implicit for fetching EventsEventlocationcrossrefRow objects using plain SQL queries */
  implicit def GetResultEventsEventlocationcrossrefRow(implicit e0: GR[Int], e1: GR[org.joda.time.LocalDateTime], e2: GR[String], e3: GR[Boolean]): GR[EventsEventlocationcrossrefRow] = GR{
    prs => import prs._
    EventsEventlocationcrossrefRow.tupled((<<[Int], <<[org.joda.time.LocalDateTime], <<[org.joda.time.LocalDateTime], <<[Int], <<[Int], <<[String], <<[Boolean], <<[Int]))
  }
  /** Table description of table events_eventlocationcrossref. Objects of this class serve as prototypes for rows in queries. */
  class EventsEventlocationcrossref(_tableTag: Tag) extends Table[EventsEventlocationcrossrefRow](_tableTag, "events_eventlocationcrossref") {
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
    /** Database column relationship_type SqlType(varchar) */
    val relationshipType: Rep[String] = column[String]("relationship_type")
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
   *  @param eventOd Database column event_od SqlType(int4)
   *  @param relationshipType Database column relationship_type SqlType(varchar), Length(100,true)
   *  @param isCurrent Database column is_current SqlType(bool)
   *  @param relationshiprecordId Database column relationshiprecord_id SqlType(int4) */
  case class EventsEventorganisationcrossrefRow(id: Int, dateCreated: org.joda.time.LocalDateTime, lastUpdated: org.joda.time.LocalDateTime, organisationId: Int, eventOd: Int, relationshipType: String, isCurrent: Boolean, relationshiprecordId: Int)
  /** GetResult implicit for fetching EventsEventorganisationcrossrefRow objects using plain SQL queries */
  implicit def GetResultEventsEventorganisationcrossrefRow(implicit e0: GR[Int], e1: GR[org.joda.time.LocalDateTime], e2: GR[String], e3: GR[Boolean]): GR[EventsEventorganisationcrossrefRow] = GR{
    prs => import prs._
    EventsEventorganisationcrossrefRow.tupled((<<[Int], <<[org.joda.time.LocalDateTime], <<[org.joda.time.LocalDateTime], <<[Int], <<[Int], <<[String], <<[Boolean], <<[Int]))
  }
  /** Table description of table events_eventorganisationcrossref. Objects of this class serve as prototypes for rows in queries. */
  class EventsEventorganisationcrossref(_tableTag: Tag) extends Table[EventsEventorganisationcrossrefRow](_tableTag, "events_eventorganisationcrossref") {
    def * = (id, dateCreated, lastUpdated, organisationId, eventOd, relationshipType, isCurrent, relationshiprecordId) <> (EventsEventorganisationcrossrefRow.tupled, EventsEventorganisationcrossrefRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(dateCreated), Rep.Some(lastUpdated), Rep.Some(organisationId), Rep.Some(eventOd), Rep.Some(relationshipType), Rep.Some(isCurrent), Rep.Some(relationshiprecordId)).shaped.<>({r=>import r._; _1.map(_=> EventsEventorganisationcrossrefRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get, _7.get, _8.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(serial), AutoInc, PrimaryKey */
    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    /** Database column date_created SqlType(timestamp) */
    val dateCreated: Rep[org.joda.time.LocalDateTime] = column[org.joda.time.LocalDateTime]("date_created")
    /** Database column last_updated SqlType(timestamp) */
    val lastUpdated: Rep[org.joda.time.LocalDateTime] = column[org.joda.time.LocalDateTime]("last_updated")
    /** Database column organisation_id SqlType(int4) */
    val organisationId: Rep[Int] = column[Int]("organisation_id")
    /** Database column event_od SqlType(int4) */
    val eventOd: Rep[Int] = column[Int]("event_od")
    /** Database column relationship_type SqlType(varchar), Length(100,true) */
    val relationshipType: Rep[String] = column[String]("relationship_type", O.Length(100,varying=true))
    /** Database column is_current SqlType(bool) */
    val isCurrent: Rep[Boolean] = column[Boolean]("is_current")
    /** Database column relationshiprecord_id SqlType(int4) */
    val relationshiprecordId: Rep[Int] = column[Int]("relationshiprecord_id")

    /** Foreign key referencing EventsEvent (database name events_eventorganisationcrossref_fk) */
    lazy val eventsEventFk = foreignKey("events_eventorganisationcrossref_fk", eventOd, EventsEvent)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
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
   *  @param eventOd Database column event_od SqlType(int4)
   *  @param relationshipType Database column relationship_type SqlType(varchar), Length(100,true)
   *  @param isCurrent Database column is_current SqlType(bool)
   *  @param relationshiprecordId Database column relationshiprecord_id SqlType(int4) */
  case class EventsEventpersoncrossrefRow(id: Int, dateCreated: org.joda.time.LocalDateTime, lastUpdated: org.joda.time.LocalDateTime, personId: Int, eventOd: Int, relationshipType: String, isCurrent: Boolean, relationshiprecordId: Int)
  /** GetResult implicit for fetching EventsEventpersoncrossrefRow objects using plain SQL queries */
  implicit def GetResultEventsEventpersoncrossrefRow(implicit e0: GR[Int], e1: GR[org.joda.time.LocalDateTime], e2: GR[String], e3: GR[Boolean]): GR[EventsEventpersoncrossrefRow] = GR{
    prs => import prs._
    EventsEventpersoncrossrefRow.tupled((<<[Int], <<[org.joda.time.LocalDateTime], <<[org.joda.time.LocalDateTime], <<[Int], <<[Int], <<[String], <<[Boolean], <<[Int]))
  }
  /** Table description of table events_eventpersoncrossref. Objects of this class serve as prototypes for rows in queries. */
  class EventsEventpersoncrossref(_tableTag: Tag) extends Table[EventsEventpersoncrossrefRow](_tableTag, "events_eventpersoncrossref") {
    def * = (id, dateCreated, lastUpdated, personId, eventOd, relationshipType, isCurrent, relationshiprecordId) <> (EventsEventpersoncrossrefRow.tupled, EventsEventpersoncrossrefRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(dateCreated), Rep.Some(lastUpdated), Rep.Some(personId), Rep.Some(eventOd), Rep.Some(relationshipType), Rep.Some(isCurrent), Rep.Some(relationshiprecordId)).shaped.<>({r=>import r._; _1.map(_=> EventsEventpersoncrossrefRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get, _7.get, _8.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(serial), AutoInc, PrimaryKey */
    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    /** Database column date_created SqlType(timestamp) */
    val dateCreated: Rep[org.joda.time.LocalDateTime] = column[org.joda.time.LocalDateTime]("date_created")
    /** Database column last_updated SqlType(timestamp) */
    val lastUpdated: Rep[org.joda.time.LocalDateTime] = column[org.joda.time.LocalDateTime]("last_updated")
    /** Database column person_id SqlType(int4) */
    val personId: Rep[Int] = column[Int]("person_id")
    /** Database column event_od SqlType(int4) */
    val eventOd: Rep[Int] = column[Int]("event_od")
    /** Database column relationship_type SqlType(varchar), Length(100,true) */
    val relationshipType: Rep[String] = column[String]("relationship_type", O.Length(100,varying=true))
    /** Database column is_current SqlType(bool) */
    val isCurrent: Rep[Boolean] = column[Boolean]("is_current")
    /** Database column relationshiprecord_id SqlType(int4) */
    val relationshiprecordId: Rep[Int] = column[Int]("relationshiprecord_id")

    /** Foreign key referencing EventsEvent (database name events_eventpersoncrossref_thing_id_fkey) */
    lazy val eventsEventFk = foreignKey("events_eventpersoncrossref_thing_id_fkey", eventOd, EventsEvent)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
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
  class EventsEventthingcrossref(_tableTag: Tag) extends Table[EventsEventthingcrossrefRow](_tableTag, "events_eventthingcrossref") {
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
  class EventsEventtoeventcrossref(_tableTag: Tag) extends Table[EventsEventtoeventcrossrefRow](_tableTag, "events_eventtoeventcrossref") {
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
  class EventsSystempropertydynamiccrossref(_tableTag: Tag) extends Table[EventsSystempropertydynamiccrossrefRow](_tableTag, "events_systempropertydynamiccrossref") {
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
  class EventsSystempropertystaticcrossref(_tableTag: Tag) extends Table[EventsSystempropertystaticcrossrefRow](_tableTag, "events_systempropertystaticcrossref") {
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
  class EventsSystemtypecrossref(_tableTag: Tag) extends Table[EventsSystemtypecrossrefRow](_tableTag, "events_systemtypecrossref") {
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
  class LocationsLocation(_tableTag: Tag) extends Table[LocationsLocationRow](_tableTag, "locations_location") {
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
   *  @param locationId Database column location_id SqlType(int4)
   *  @param thingId Database column thing_id SqlType(int4)
   *  @param relationshipType Database column relationship_type SqlType(varchar), Length(100,true)
   *  @param isCurrent Database column is_current SqlType(bool)
   *  @param relationshiprecordId Database column relationshiprecord_id SqlType(int4) */
  case class LocationsLocationthingcrossrefRow(id: Int, dateCreated: org.joda.time.LocalDateTime, lastUpdated: org.joda.time.LocalDateTime, locationId: Int, thingId: Int, relationshipType: String, isCurrent: Boolean, relationshiprecordId: Int)
  /** GetResult implicit for fetching LocationsLocationthingcrossrefRow objects using plain SQL queries */
  implicit def GetResultLocationsLocationthingcrossrefRow(implicit e0: GR[Int], e1: GR[org.joda.time.LocalDateTime], e2: GR[String], e3: GR[Boolean]): GR[LocationsLocationthingcrossrefRow] = GR{
    prs => import prs._
    LocationsLocationthingcrossrefRow.tupled((<<[Int], <<[org.joda.time.LocalDateTime], <<[org.joda.time.LocalDateTime], <<[Int], <<[Int], <<[String], <<[Boolean], <<[Int]))
  }
  /** Table description of table locations_locationthingcrossref. Objects of this class serve as prototypes for rows in queries. */
  class LocationsLocationthingcrossref(_tableTag: Tag) extends Table[LocationsLocationthingcrossrefRow](_tableTag, "locations_locationthingcrossref") {
    def * = (id, dateCreated, lastUpdated, locationId, thingId, relationshipType, isCurrent, relationshiprecordId) <> (LocationsLocationthingcrossrefRow.tupled, LocationsLocationthingcrossrefRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(dateCreated), Rep.Some(lastUpdated), Rep.Some(locationId), Rep.Some(thingId), Rep.Some(relationshipType), Rep.Some(isCurrent), Rep.Some(relationshiprecordId)).shaped.<>({r=>import r._; _1.map(_=> LocationsLocationthingcrossrefRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get, _7.get, _8.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(serial), AutoInc, PrimaryKey */
    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    /** Database column date_created SqlType(timestamp) */
    val dateCreated: Rep[org.joda.time.LocalDateTime] = column[org.joda.time.LocalDateTime]("date_created")
    /** Database column last_updated SqlType(timestamp) */
    val lastUpdated: Rep[org.joda.time.LocalDateTime] = column[org.joda.time.LocalDateTime]("last_updated")
    /** Database column location_id SqlType(int4) */
    val locationId: Rep[Int] = column[Int]("location_id")
    /** Database column thing_id SqlType(int4) */
    val thingId: Rep[Int] = column[Int]("thing_id")
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
  class LocationsLocationtolocationcrossref(_tableTag: Tag) extends Table[LocationsLocationtolocationcrossrefRow](_tableTag, "locations_locationtolocationcrossref") {
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
  class LocationsSystempropertydynamiccrossref(_tableTag: Tag) extends Table[LocationsSystempropertydynamiccrossrefRow](_tableTag, "locations_systempropertydynamiccrossref") {
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
  class LocationsSystempropertystaticcrossref(_tableTag: Tag) extends Table[LocationsSystempropertystaticcrossrefRow](_tableTag, "locations_systempropertystaticcrossref") {
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
  class LocationsSystemtypecrossref(_tableTag: Tag) extends Table[LocationsSystemtypecrossrefRow](_tableTag, "locations_systemtypecrossref") {
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
  class OrganisationsOrganisation(_tableTag: Tag) extends Table[OrganisationsOrganisationRow](_tableTag, "organisations_organisation") {
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
  class OrganisationsOrganisationlocationcrossref(_tableTag: Tag) extends Table[OrganisationsOrganisationlocationcrossrefRow](_tableTag, "organisations_organisationlocationcrossref") {
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
   *  @param id Database column id SqlType(varchar), AutoInc, PrimaryKey
   *  @param dateCreated Database column date_created SqlType(timestamp)
   *  @param lastUpdated Database column last_updated SqlType(timestamp)
   *  @param thingId Database column thing_id SqlType(int4)
   *  @param organisationId Database column organisation_id SqlType(int4)
   *  @param relationshipType Database column relationship_type SqlType(varchar), Length(100,true)
   *  @param isCurrent Database column is_current SqlType(bool)
   *  @param relationshiprecordId Database column relationshiprecord_id SqlType(int4) */
  case class OrganisationsOrganisationthingcrossrefRow(id: String, dateCreated: org.joda.time.LocalDateTime, lastUpdated: org.joda.time.LocalDateTime, thingId: Int, organisationId: Int, relationshipType: String, isCurrent: Boolean, relationshiprecordId: Int)
  /** GetResult implicit for fetching OrganisationsOrganisationthingcrossrefRow objects using plain SQL queries */
  implicit def GetResultOrganisationsOrganisationthingcrossrefRow(implicit e0: GR[String], e1: GR[org.joda.time.LocalDateTime], e2: GR[Int], e3: GR[Boolean]): GR[OrganisationsOrganisationthingcrossrefRow] = GR{
    prs => import prs._
    OrganisationsOrganisationthingcrossrefRow.tupled((<<[String], <<[org.joda.time.LocalDateTime], <<[org.joda.time.LocalDateTime], <<[Int], <<[Int], <<[String], <<[Boolean], <<[Int]))
  }
  /** Table description of table organisations_organisationthingcrossref. Objects of this class serve as prototypes for rows in queries. */
  class OrganisationsOrganisationthingcrossref(_tableTag: Tag) extends Table[OrganisationsOrganisationthingcrossrefRow](_tableTag, "organisations_organisationthingcrossref") {
    def * = (id, dateCreated, lastUpdated, thingId, organisationId, relationshipType, isCurrent, relationshiprecordId) <> (OrganisationsOrganisationthingcrossrefRow.tupled, OrganisationsOrganisationthingcrossrefRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(dateCreated), Rep.Some(lastUpdated), Rep.Some(thingId), Rep.Some(organisationId), Rep.Some(relationshipType), Rep.Some(isCurrent), Rep.Some(relationshiprecordId)).shaped.<>({r=>import r._; _1.map(_=> OrganisationsOrganisationthingcrossrefRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get, _7.get, _8.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(varchar), AutoInc, PrimaryKey */
    val id: Rep[String] = column[String]("id", O.AutoInc, O.PrimaryKey)
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
  class OrganisationsOrganisationtoorganisationcrossref(_tableTag: Tag) extends Table[OrganisationsOrganisationtoorganisationcrossrefRow](_tableTag, "organisations_organisationtoorganisationcrossref") {
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
  class OrganisationsSystempropertydynamiccrossref(_tableTag: Tag) extends Table[OrganisationsSystempropertydynamiccrossrefRow](_tableTag, "organisations_systempropertydynamiccrossref") {
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
  class OrganisationsSystempropertystaticcrossref(_tableTag: Tag) extends Table[OrganisationsSystempropertystaticcrossrefRow](_tableTag, "organisations_systempropertystaticcrossref") {
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

  /** Entity class storing rows of table OrganisationSystemtypecrossref
   *  @param id Database column id SqlType(serial), AutoInc, PrimaryKey
   *  @param dateCreated Database column date_created SqlType(timestamp)
   *  @param lastUpdated Database column last_updated SqlType(timestamp)
   *  @param organisationId Database column organisation_id SqlType(int4)
   *  @param systemTypeId Database column system_type_id SqlType(int4)
   *  @param relationshipType Database column relationship_type SqlType(varchar), Length(100,true)
   *  @param isCurrent Database column is_current SqlType(bool) */
  case class OrganisationSystemtypecrossrefRow(id: Int, dateCreated: org.joda.time.LocalDateTime, lastUpdated: org.joda.time.LocalDateTime, organisationId: Int, systemTypeId: Int, relationshipType: String, isCurrent: Boolean)
  /** GetResult implicit for fetching OrganisationSystemtypecrossrefRow objects using plain SQL queries */
  implicit def GetResultOrganisationSystemtypecrossrefRow(implicit e0: GR[Int], e1: GR[org.joda.time.LocalDateTime], e2: GR[String], e3: GR[Boolean]): GR[OrganisationSystemtypecrossrefRow] = GR{
    prs => import prs._
    OrganisationSystemtypecrossrefRow.tupled((<<[Int], <<[org.joda.time.LocalDateTime], <<[org.joda.time.LocalDateTime], <<[Int], <<[Int], <<[String], <<[Boolean]))
  }
  /** Table description of table organisation_systemtypecrossref. Objects of this class serve as prototypes for rows in queries. */
  class OrganisationSystemtypecrossref(_tableTag: Tag) extends Table[OrganisationSystemtypecrossrefRow](_tableTag, "organisation_systemtypecrossref") {
    def * = (id, dateCreated, lastUpdated, organisationId, systemTypeId, relationshipType, isCurrent) <> (OrganisationSystemtypecrossrefRow.tupled, OrganisationSystemtypecrossrefRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(dateCreated), Rep.Some(lastUpdated), Rep.Some(organisationId), Rep.Some(systemTypeId), Rep.Some(relationshipType), Rep.Some(isCurrent)).shaped.<>({r=>import r._; _1.map(_=> OrganisationSystemtypecrossrefRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get, _7.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

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

    /** Foreign key referencing OrganisationsOrganisation (database name organisations_organisation_organisation_systemtypecrossref_fk) */
    lazy val organisationsOrganisationFk = foreignKey("organisations_organisation_organisation_systemtypecrossref_fk", organisationId, OrganisationsOrganisation)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
    /** Foreign key referencing SystemType (database name system_type_organisation_systemtypecrossref_fk) */
    lazy val systemTypeFk = foreignKey("system_type_organisation_systemtypecrossref_fk", systemTypeId, SystemType)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
  }
  /** Collection-like TableQuery object for table OrganisationSystemtypecrossref */
  lazy val OrganisationSystemtypecrossref = new TableQuery(tag => new OrganisationSystemtypecrossref(tag))

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
  class PeoplePerson(_tableTag: Tag) extends Table[PeoplePersonRow](_tableTag, "people_person") {
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
  class PeoplePersonlocationcrossref(_tableTag: Tag) extends Table[PeoplePersonlocationcrossrefRow](_tableTag, "people_personlocationcrossref") {
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
   *  @param personId Database column person_id SqlType(int4)
   *  @param organisationId Database column organisation_id SqlType(int4)
   *  @param relationshipType Database column relationship_type SqlType(varchar), Length(100,true)
   *  @param isCurrent Database column is_current SqlType(bool)
   *  @param relationshiprecordId Database column relationshiprecord_id SqlType(int4) */
  case class PeoplePersonorganisationcrossrefRow(id: Int, dateCreated: org.joda.time.LocalDateTime, lastUpdated: org.joda.time.LocalDateTime, personId: Int, organisationId: Int, relationshipType: String, isCurrent: Boolean, relationshiprecordId: Int)
  /** GetResult implicit for fetching PeoplePersonorganisationcrossrefRow objects using plain SQL queries */
  implicit def GetResultPeoplePersonorganisationcrossrefRow(implicit e0: GR[Int], e1: GR[org.joda.time.LocalDateTime], e2: GR[String], e3: GR[Boolean]): GR[PeoplePersonorganisationcrossrefRow] = GR{
    prs => import prs._
    PeoplePersonorganisationcrossrefRow.tupled((<<[Int], <<[org.joda.time.LocalDateTime], <<[org.joda.time.LocalDateTime], <<[Int], <<[Int], <<[String], <<[Boolean], <<[Int]))
  }
  /** Table description of table people_personorganisationcrossref. Objects of this class serve as prototypes for rows in queries. */
  class PeoplePersonorganisationcrossref(_tableTag: Tag) extends Table[PeoplePersonorganisationcrossrefRow](_tableTag, "people_personorganisationcrossref") {
    def * = (id, dateCreated, lastUpdated, personId, organisationId, relationshipType, isCurrent, relationshiprecordId) <> (PeoplePersonorganisationcrossrefRow.tupled, PeoplePersonorganisationcrossrefRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(dateCreated), Rep.Some(lastUpdated), Rep.Some(personId), Rep.Some(organisationId), Rep.Some(relationshipType), Rep.Some(isCurrent), Rep.Some(relationshiprecordId)).shaped.<>({r=>import r._; _1.map(_=> PeoplePersonorganisationcrossrefRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get, _7.get, _8.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(serial), AutoInc, PrimaryKey */
    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    /** Database column date_created SqlType(timestamp) */
    val dateCreated: Rep[org.joda.time.LocalDateTime] = column[org.joda.time.LocalDateTime]("date_created")
    /** Database column last_updated SqlType(timestamp) */
    val lastUpdated: Rep[org.joda.time.LocalDateTime] = column[org.joda.time.LocalDateTime]("last_updated")
    /** Database column person_id SqlType(int4) */
    val personId: Rep[Int] = column[Int]("person_id")
    /** Database column organisation_id SqlType(int4) */
    val organisationId: Rep[Int] = column[Int]("organisation_id")
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
  class PeoplePersontopersoncrossref(_tableTag: Tag) extends Table[PeoplePersontopersoncrossrefRow](_tableTag, "people_persontopersoncrossref") {
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
  class PeoplePersontopersonrelationshiptype(_tableTag: Tag) extends Table[PeoplePersontopersonrelationshiptypeRow](_tableTag, "people_persontopersonrelationshiptype") {
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
  class PeopleSystempropertydynamiccrossref(_tableTag: Tag) extends Table[PeopleSystempropertydynamiccrossrefRow](_tableTag, "people_systempropertydynamiccrossref") {
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
  class PeopleSystempropertystaticcrossref(_tableTag: Tag) extends Table[PeopleSystempropertystaticcrossrefRow](_tableTag, "people_systempropertystaticcrossref") {
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
  class PeopleSystemtypecrossref(_tableTag: Tag) extends Table[PeopleSystemtypecrossrefRow](_tableTag, "people_systemtypecrossref") {
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
  class SystemEventlog(_tableTag: Tag) extends Table[SystemEventlogRow](_tableTag, "system_eventlog") {
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
  class SystemProperty(_tableTag: Tag) extends Table[SystemPropertyRow](_tableTag, "system_property") {
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
  class SystemPropertyrecord(_tableTag: Tag) extends Table[SystemPropertyrecordRow](_tableTag, "system_propertyrecord") {
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
  class SystemRelationshiprecord(_tableTag: Tag) extends Table[SystemRelationshiprecordRow](_tableTag, "system_relationshiprecord") {
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
  class SystemRelationshiprecordtorecordcrossref(_tableTag: Tag) extends Table[SystemRelationshiprecordtorecordcrossrefRow](_tableTag, "system_relationshiprecordtorecordcrossref") {
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
  class SystemType(_tableTag: Tag) extends Table[SystemTypeRow](_tableTag, "system_type") {
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
  class SystemTypetotypecrossref(_tableTag: Tag) extends Table[SystemTypetotypecrossrefRow](_tableTag, "system_typetotypecrossref") {
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
   *  @param name Database column name SqlType(varchar), Length(100,true)
   *  @param description Database column description SqlType(text), Default(None)
   *  @param symbol Database column symbol SqlType(varchar), Length(16,true), Default(None) */
  case class SystemUnitofmeasurementRow(id: Int, dateCreated: org.joda.time.LocalDateTime, lastUpdated: org.joda.time.LocalDateTime, name: String, description: Option[String] = None, symbol: Option[String] = None)
  /** GetResult implicit for fetching SystemUnitofmeasurementRow objects using plain SQL queries */
  implicit def GetResultSystemUnitofmeasurementRow(implicit e0: GR[Int], e1: GR[org.joda.time.LocalDateTime], e2: GR[String], e3: GR[Option[String]]): GR[SystemUnitofmeasurementRow] = GR{
    prs => import prs._
    SystemUnitofmeasurementRow.tupled((<<[Int], <<[org.joda.time.LocalDateTime], <<[org.joda.time.LocalDateTime], <<[String], <<?[String], <<?[String]))
  }
  /** Table description of table system_unitofmeasurement. Objects of this class serve as prototypes for rows in queries. */
  class SystemUnitofmeasurement(_tableTag: Tag) extends Table[SystemUnitofmeasurementRow](_tableTag, "system_unitofmeasurement") {
    def * = (id, dateCreated, lastUpdated, name, description, symbol) <> (SystemUnitofmeasurementRow.tupled, SystemUnitofmeasurementRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(dateCreated), Rep.Some(lastUpdated), Rep.Some(name), description, symbol).shaped.<>({r=>import r._; _1.map(_=> SystemUnitofmeasurementRow.tupled((_1.get, _2.get, _3.get, _4.get, _5, _6)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

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
    /** Database column symbol SqlType(varchar), Length(16,true), Default(None) */
    val symbol: Rep[Option[String]] = column[Option[String]]("symbol", O.Length(16,varying=true), O.Default(None))
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
  class ThingsSystempropertydynamiccrossref(_tableTag: Tag) extends Table[ThingsSystempropertydynamiccrossrefRow](_tableTag, "things_systempropertydynamiccrossref") {
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
   *  @param fieldId Database column field_id SqlType(int4)
   *  @param recordId Database column record_id SqlType(int4)
   *  @param relationshipType Database column relationship_type SqlType(varchar), Length(100,true)
   *  @param isCurrent Database column is_current SqlType(bool)
   *  @param propertyrecordId Database column propertyrecord_id SqlType(int4) */
  case class ThingsSystempropertystaticcrossrefRow(id: Int, dateCreated: org.joda.time.LocalDateTime, lastUpdated: org.joda.time.LocalDateTime, thingId: Int, systemPropertyId: Int, fieldId: Int, recordId: Int, relationshipType: String, isCurrent: Boolean, propertyrecordId: Int)
  /** GetResult implicit for fetching ThingsSystempropertystaticcrossrefRow objects using plain SQL queries */
  implicit def GetResultThingsSystempropertystaticcrossrefRow(implicit e0: GR[Int], e1: GR[org.joda.time.LocalDateTime], e2: GR[String], e3: GR[Boolean]): GR[ThingsSystempropertystaticcrossrefRow] = GR{
    prs => import prs._
    ThingsSystempropertystaticcrossrefRow.tupled((<<[Int], <<[org.joda.time.LocalDateTime], <<[org.joda.time.LocalDateTime], <<[Int], <<[Int], <<[Int], <<[Int], <<[String], <<[Boolean], <<[Int]))
  }
  /** Table description of table things_systempropertystaticcrossref. Objects of this class serve as prototypes for rows in queries. */
  class ThingsSystempropertystaticcrossref(_tableTag: Tag) extends Table[ThingsSystempropertystaticcrossrefRow](_tableTag, "things_systempropertystaticcrossref") {
    def * = (id, dateCreated, lastUpdated, thingId, systemPropertyId, fieldId, recordId, relationshipType, isCurrent, propertyrecordId) <> (ThingsSystempropertystaticcrossrefRow.tupled, ThingsSystempropertystaticcrossrefRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(dateCreated), Rep.Some(lastUpdated), Rep.Some(thingId), Rep.Some(systemPropertyId), Rep.Some(fieldId), Rep.Some(recordId), Rep.Some(relationshipType), Rep.Some(isCurrent), Rep.Some(propertyrecordId)).shaped.<>({r=>import r._; _1.map(_=> ThingsSystempropertystaticcrossrefRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get, _7.get, _8.get, _9.get, _10.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

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
    /** Database column record_id SqlType(int4) */
    val recordId: Rep[Int] = column[Int]("record_id")
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
  class ThingsSystemtypecrossref(_tableTag: Tag) extends Table[ThingsSystemtypecrossrefRow](_tableTag, "things_systemtypecrossref") {
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
   *  @param name Database column name SqlType(varchar), Length(100,true) */
  case class ThingsThingRow(id: Int, dateCreated: org.joda.time.LocalDateTime, lastUpdated: org.joda.time.LocalDateTime, name: String)
  /** GetResult implicit for fetching ThingsThingRow objects using plain SQL queries */
  implicit def GetResultThingsThingRow(implicit e0: GR[Int], e1: GR[org.joda.time.LocalDateTime], e2: GR[String]): GR[ThingsThingRow] = GR{
    prs => import prs._
    ThingsThingRow.tupled((<<[Int], <<[org.joda.time.LocalDateTime], <<[org.joda.time.LocalDateTime], <<[String]))
  }
  /** Table description of table things_thing. Objects of this class serve as prototypes for rows in queries. */
  class ThingsThing(_tableTag: Tag) extends Table[ThingsThingRow](_tableTag, "things_thing") {
    def * = (id, dateCreated, lastUpdated, name) <> (ThingsThingRow.tupled, ThingsThingRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(dateCreated), Rep.Some(lastUpdated), Rep.Some(name)).shaped.<>({r=>import r._; _1.map(_=> ThingsThingRow.tupled((_1.get, _2.get, _3.get, _4.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(serial), AutoInc, PrimaryKey */
    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    /** Database column date_created SqlType(timestamp) */
    val dateCreated: Rep[org.joda.time.LocalDateTime] = column[org.joda.time.LocalDateTime]("date_created")
    /** Database column last_updated SqlType(timestamp) */
    val lastUpdated: Rep[org.joda.time.LocalDateTime] = column[org.joda.time.LocalDateTime]("last_updated")
    /** Database column name SqlType(varchar), Length(100,true) */
    val name: Rep[String] = column[String]("name", O.Length(100,varying=true))
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
  class ThingsThingpersoncrossref(_tableTag: Tag) extends Table[ThingsThingpersoncrossrefRow](_tableTag, "things_thingpersoncrossref") {
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
  class ThingsThingtothingcrossref(_tableTag: Tag) extends Table[ThingsThingtothingcrossrefRow](_tableTag, "things_thingtothingcrossref") {
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
}
