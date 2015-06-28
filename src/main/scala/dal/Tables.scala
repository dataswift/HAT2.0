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
  lazy val schema = Array(DataDebit.schema, DataField.schema, DataRecord.schema, DataTable.schema, DataTabletotablecrossref.schema, DataValue.schema, EventsEvent.schema, EventsEventlocationcrossref.schema, EventsEventorganisationcrossref.schema, EventsEventpersoncrossref.schema, EventsEventthingcrossref.schema, EventsEventtoeventcrossref.schema, EventsSystempropertydynamiccrossref.schema, EventsSystempropertystaticcrossref.schema, EventsSystemtypecrossref.schema, LocationsLocation.schema, LocationsLocationthingcrossref.schema, LocationsLocationtolocationcrossref.schema, LocationsSystempropertydynamiccrossref.schema, LocationsSystempropertystaticcrossref.schema, LocationSystemtypecrossref.schema, OrganisationOrganisationtoorganisationcrossref.schema, OrganisationsOrganisation.schema, OrganisationsOrganisationlocationcrossref.schema, OrganisationsSystempropertydynamiccrossref.schema, OrganisationsSystempropertystaticcrossref.schema, OrganisationSystemtypecrossref.schema, PeoplePerson.schema, PeoplePersonlocationcrossref.schema, PeoplePersonorganisationcrossref.schema, PeoplePersontopersoncrossref.schema, PeoplePersontopersonrelationshiptype.schema, PeopleSystempropertydynamiccrossref.schema, PeopleSystempropertystaticcrossref.schema, PeopleSystemtypecrossref.schema, SystemEventlog.schema, SystemProperty.schema, SystemPropertytypecrossref.schema, SystemPropertyuomcrossref.schema, SystemType.schema, SystemTypetotypecrossref.schema, SystemUnitofmeasurement.schema, ThingsSystempropertydynamiccrossref.schema, ThingsSystempropertystaticcrossref.schema, ThingsSystemtypecrossref.schema, ThingsThing.schema, ThingsThingpersoncrossref.schema, ThingsThingtothingcrossref.schema).reduceLeft(_ ++ _)
  @deprecated("Use .schema instead of .ddl", "3.0")
  def ddl = schema

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
   *  @param tableId Database column table_id SqlType(int4)
   *  @param senderId Database column sender_id SqlType(varchar), Length(36,true)
   *  @param recipientId Database column recipient_id SqlType(varchar), Length(36,true) */
  case class DataDebitRow(id: Int, dateCreated: org.joda.time.LocalDateTime, lastUpdated: org.joda.time.LocalDateTime, name: String, startDate: org.joda.time.LocalDateTime, endDate: org.joda.time.LocalDateTime, rolling: Boolean, sellRent: Boolean, price: Float, dataDebitKey: String, tableId: Int, senderId: String, recipientId: String)
  /** GetResult implicit for fetching DataDebitRow objects using plain SQL queries */
  implicit def GetResultDataDebitRow(implicit e0: GR[Int], e1: GR[org.joda.time.LocalDateTime], e2: GR[String], e3: GR[Boolean], e4: GR[Float]): GR[DataDebitRow] = GR{
    prs => import prs._
    DataDebitRow.tupled((<<[Int], <<[org.joda.time.LocalDateTime], <<[org.joda.time.LocalDateTime], <<[String], <<[org.joda.time.LocalDateTime], <<[org.joda.time.LocalDateTime], <<[Boolean], <<[Boolean], <<[Float], <<[String], <<[Int], <<[String], <<[String]))
  }
  /** Table description of table data_debit. Objects of this class serve as prototypes for rows in queries. */
  class DataDebit(_tableTag: Tag) extends Table[DataDebitRow](_tableTag, "data_debit") {
    def * = (id, dateCreated, lastUpdated, name, startDate, endDate, rolling, sellRent, price, dataDebitKey, tableId, senderId, recipientId) <> (DataDebitRow.tupled, DataDebitRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(dateCreated), Rep.Some(lastUpdated), Rep.Some(name), Rep.Some(startDate), Rep.Some(endDate), Rep.Some(rolling), Rep.Some(sellRent), Rep.Some(price), Rep.Some(dataDebitKey), Rep.Some(tableId), Rep.Some(senderId), Rep.Some(recipientId)).shaped.<>({r=>import r._; _1.map(_=> DataDebitRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get, _7.get, _8.get, _9.get, _10.get, _11.get, _12.get, _13.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

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
    /** Database column table_id SqlType(int4) */
    val tableId: Rep[Int] = column[Int]("table_id")
    /** Database column sender_id SqlType(varchar), Length(36,true) */
    val senderId: Rep[String] = column[String]("sender_id", O.Length(36,varying=true))
    /** Database column recipient_id SqlType(varchar), Length(36,true) */
    val recipientId: Rep[String] = column[String]("recipient_id", O.Length(36,varying=true))

    /** Foreign key referencing DataTable (database name data_table_data_debit_fk) */
    lazy val dataTableFk = foreignKey("data_table_data_debit_fk", tableId, DataTable)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
  }
  /** Collection-like TableQuery object for table DataDebit */
  lazy val DataDebit = new TableQuery(tag => new DataDebit(tag))

  /** Entity class storing rows of table DataField
   *  @param id Database column id SqlType(serial), AutoInc, PrimaryKey
   *  @param dateCreated Database column date_created SqlType(timestamp)
   *  @param lastUpdated Database column last_updated SqlType(timestamp)
   *  @param dataTableFk Database column data_table_fk SqlType(int4)
   *  @param name Database column name SqlType(varchar) */
  case class DataFieldRow(id: Int, dateCreated: org.joda.time.LocalDateTime, lastUpdated: org.joda.time.LocalDateTime, dataTableFk: Int, name: String)
  /** GetResult implicit for fetching DataFieldRow objects using plain SQL queries */
  implicit def GetResultDataFieldRow(implicit e0: GR[Int], e1: GR[org.joda.time.LocalDateTime], e2: GR[String]): GR[DataFieldRow] = GR{
    prs => import prs._
    DataFieldRow.tupled((<<[Int], <<[org.joda.time.LocalDateTime], <<[org.joda.time.LocalDateTime], <<[Int], <<[String]))
  }
  /** Table description of table data_field. Objects of this class serve as prototypes for rows in queries. */
  class DataField(_tableTag: Tag) extends Table[DataFieldRow](_tableTag, "data_field") {
    def * = (id, dateCreated, lastUpdated, dataTableFk, name) <> (DataFieldRow.tupled, DataFieldRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(dateCreated), Rep.Some(lastUpdated), Rep.Some(dataTableFk), Rep.Some(name)).shaped.<>({r=>import r._; _1.map(_=> DataFieldRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(serial), AutoInc, PrimaryKey */
    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    /** Database column date_created SqlType(timestamp) */
    val dateCreated: Rep[org.joda.time.LocalDateTime] = column[org.joda.time.LocalDateTime]("date_created")
    /** Database column last_updated SqlType(timestamp) */
    val lastUpdated: Rep[org.joda.time.LocalDateTime] = column[org.joda.time.LocalDateTime]("last_updated")
    /** Database column data_table_fk SqlType(int4) */
    val dataTableFk: Rep[Int] = column[Int]("data_table_fk")
    /** Database column name SqlType(varchar) */
    val name: Rep[String] = column[String]("name")

    /** Foreign key referencing DataTable (database name data_table_fk) */
    lazy val dataTableFkX = foreignKey("data_table_fk", dataTableFk, DataTable)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
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
   *  @param isBundle Database column is_bundle SqlType(bool)
   *  @param sourceName Database column source_name SqlType(varchar) */
  case class DataTableRow(id: Int, dateCreated: org.joda.time.LocalDateTime, lastUpdated: org.joda.time.LocalDateTime, name: String, isBundle: Boolean, sourceName: String)
  /** GetResult implicit for fetching DataTableRow objects using plain SQL queries */
  implicit def GetResultDataTableRow(implicit e0: GR[Int], e1: GR[org.joda.time.LocalDateTime], e2: GR[String], e3: GR[Boolean]): GR[DataTableRow] = GR{
    prs => import prs._
    DataTableRow.tupled((<<[Int], <<[org.joda.time.LocalDateTime], <<[org.joda.time.LocalDateTime], <<[String], <<[Boolean], <<[String]))
  }
  /** Table description of table data_table. Objects of this class serve as prototypes for rows in queries. */
  class DataTable(_tableTag: Tag) extends Table[DataTableRow](_tableTag, "data_table") {
    def * = (id, dateCreated, lastUpdated, name, isBundle, sourceName) <> (DataTableRow.tupled, DataTableRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(dateCreated), Rep.Some(lastUpdated), Rep.Some(name), Rep.Some(isBundle), Rep.Some(sourceName)).shaped.<>({r=>import r._; _1.map(_=> DataTableRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(serial), AutoInc, PrimaryKey */
    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    /** Database column date_created SqlType(timestamp) */
    val dateCreated: Rep[org.joda.time.LocalDateTime] = column[org.joda.time.LocalDateTime]("date_created")
    /** Database column last_updated SqlType(timestamp) */
    val lastUpdated: Rep[org.joda.time.LocalDateTime] = column[org.joda.time.LocalDateTime]("last_updated")
    /** Database column name SqlType(varchar) */
    val name: Rep[String] = column[String]("name")
    /** Database column is_bundle SqlType(bool) */
    val isBundle: Rep[Boolean] = column[Boolean]("is_bundle")
    /** Database column source_name SqlType(varchar) */
    val sourceName: Rep[String] = column[String]("source_name")
  }
  /** Collection-like TableQuery object for table DataTable */
  lazy val DataTable = new TableQuery(tag => new DataTable(tag))

  /** Entity class storing rows of table DataTabletotablecrossref
   *  @param id Database column id SqlType(serial), AutoInc, PrimaryKey
   *  @param dateCreated Database column date_created SqlType(timestamp)
   *  @param lastUpdated1 Database column last_updated_1 SqlType(timestamp)
   *  @param relationshipType Database column relationship_type SqlType(varchar)
   *  @param table1 Database column table1 SqlType(int4)
   *  @param table2 Database column table2 SqlType(int4) */
  case class DataTabletotablecrossrefRow(id: Int, dateCreated: org.joda.time.LocalDateTime, lastUpdated1: org.joda.time.LocalDateTime, relationshipType: String, table1: Int, table2: Int)
  /** GetResult implicit for fetching DataTabletotablecrossrefRow objects using plain SQL queries */
  implicit def GetResultDataTabletotablecrossrefRow(implicit e0: GR[Int], e1: GR[org.joda.time.LocalDateTime], e2: GR[String]): GR[DataTabletotablecrossrefRow] = GR{
    prs => import prs._
    DataTabletotablecrossrefRow.tupled((<<[Int], <<[org.joda.time.LocalDateTime], <<[org.joda.time.LocalDateTime], <<[String], <<[Int], <<[Int]))
  }
  /** Table description of table data_tabletotablecrossref. Objects of this class serve as prototypes for rows in queries. */
  class DataTabletotablecrossref(_tableTag: Tag) extends Table[DataTabletotablecrossrefRow](_tableTag, "data_tabletotablecrossref") {
    def * = (id, dateCreated, lastUpdated1, relationshipType, table1, table2) <> (DataTabletotablecrossrefRow.tupled, DataTabletotablecrossrefRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(dateCreated), Rep.Some(lastUpdated1), Rep.Some(relationshipType), Rep.Some(table1), Rep.Some(table2)).shaped.<>({r=>import r._; _1.map(_=> DataTabletotablecrossrefRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(serial), AutoInc, PrimaryKey */
    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    /** Database column date_created SqlType(timestamp) */
    val dateCreated: Rep[org.joda.time.LocalDateTime] = column[org.joda.time.LocalDateTime]("date_created")
    /** Database column last_updated_1 SqlType(timestamp) */
    val lastUpdated1: Rep[org.joda.time.LocalDateTime] = column[org.joda.time.LocalDateTime]("last_updated_1")
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
   *  @param relationshipDescription Database column relationship_description SqlType(text)
   *  @param isCurrent Database column is_current SqlType(bool) */
  case class EventsEventlocationcrossrefRow(id: Int, dateCreated: org.joda.time.LocalDateTime, lastUpdated: org.joda.time.LocalDateTime, locationId: Int, eventId: Int, relationshipDescription: String, isCurrent: Boolean)
  /** GetResult implicit for fetching EventsEventlocationcrossrefRow objects using plain SQL queries */
  implicit def GetResultEventsEventlocationcrossrefRow(implicit e0: GR[Int], e1: GR[org.joda.time.LocalDateTime], e2: GR[String], e3: GR[Boolean]): GR[EventsEventlocationcrossrefRow] = GR{
    prs => import prs._
    EventsEventlocationcrossrefRow.tupled((<<[Int], <<[org.joda.time.LocalDateTime], <<[org.joda.time.LocalDateTime], <<[Int], <<[Int], <<[String], <<[Boolean]))
  }
  /** Table description of table events_eventlocationcrossref. Objects of this class serve as prototypes for rows in queries. */
  class EventsEventlocationcrossref(_tableTag: Tag) extends Table[EventsEventlocationcrossrefRow](_tableTag, "events_eventlocationcrossref") {
    def * = (id, dateCreated, lastUpdated, locationId, eventId, relationshipDescription, isCurrent) <> (EventsEventlocationcrossrefRow.tupled, EventsEventlocationcrossrefRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(dateCreated), Rep.Some(lastUpdated), Rep.Some(locationId), Rep.Some(eventId), Rep.Some(relationshipDescription), Rep.Some(isCurrent)).shaped.<>({r=>import r._; _1.map(_=> EventsEventlocationcrossrefRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get, _7.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

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
    /** Database column relationship_description SqlType(text) */
    val relationshipDescription: Rep[String] = column[String]("relationship_description")
    /** Database column is_current SqlType(bool) */
    val isCurrent: Rep[Boolean] = column[Boolean]("is_current")

    /** Foreign key referencing EventsEvent (database name events_eventlocationcrossref_fk) */
    lazy val eventsEventFk = foreignKey("events_eventlocationcrossref_fk", eventId, EventsEvent)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
    /** Foreign key referencing LocationsLocation (database name locations_location_events_eventlocationcrossref_fk) */
    lazy val locationsLocationFk = foreignKey("locations_location_events_eventlocationcrossref_fk", locationId, LocationsLocation)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
  }
  /** Collection-like TableQuery object for table EventsEventlocationcrossref */
  lazy val EventsEventlocationcrossref = new TableQuery(tag => new EventsEventlocationcrossref(tag))

  /** Entity class storing rows of table EventsEventorganisationcrossref
   *  @param id Database column id SqlType(serial), AutoInc, PrimaryKey
   *  @param dateCreated Database column date_created SqlType(timestamp)
   *  @param lastUpdated Database column last_updated SqlType(timestamp)
   *  @param organisationId Database column organisation_id SqlType(int4)
   *  @param eventOd Database column event_od SqlType(int4)
   *  @param relationshipDescription Database column relationship_description SqlType(text), Default(None)
   *  @param isCurrent Database column is_current SqlType(bool) */
  case class EventsEventorganisationcrossrefRow(id: Int, dateCreated: org.joda.time.LocalDateTime, lastUpdated: org.joda.time.LocalDateTime, organisationId: Int, eventOd: Int, relationshipDescription: Option[String] = None, isCurrent: Boolean)
  /** GetResult implicit for fetching EventsEventorganisationcrossrefRow objects using plain SQL queries */
  implicit def GetResultEventsEventorganisationcrossrefRow(implicit e0: GR[Int], e1: GR[org.joda.time.LocalDateTime], e2: GR[Option[String]], e3: GR[Boolean]): GR[EventsEventorganisationcrossrefRow] = GR{
    prs => import prs._
    EventsEventorganisationcrossrefRow.tupled((<<[Int], <<[org.joda.time.LocalDateTime], <<[org.joda.time.LocalDateTime], <<[Int], <<[Int], <<?[String], <<[Boolean]))
  }
  /** Table description of table events_eventorganisationcrossref. Objects of this class serve as prototypes for rows in queries. */
  class EventsEventorganisationcrossref(_tableTag: Tag) extends Table[EventsEventorganisationcrossrefRow](_tableTag, "events_eventorganisationcrossref") {
    def * = (id, dateCreated, lastUpdated, organisationId, eventOd, relationshipDescription, isCurrent) <> (EventsEventorganisationcrossrefRow.tupled, EventsEventorganisationcrossrefRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(dateCreated), Rep.Some(lastUpdated), Rep.Some(organisationId), Rep.Some(eventOd), relationshipDescription, Rep.Some(isCurrent)).shaped.<>({r=>import r._; _1.map(_=> EventsEventorganisationcrossrefRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6, _7.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

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
    /** Database column relationship_description SqlType(text), Default(None) */
    val relationshipDescription: Rep[Option[String]] = column[Option[String]]("relationship_description", O.Default(None))
    /** Database column is_current SqlType(bool) */
    val isCurrent: Rep[Boolean] = column[Boolean]("is_current")

    /** Foreign key referencing EventsEvent (database name events_eventorganisationcrossref_fk) */
    lazy val eventsEventFk = foreignKey("events_eventorganisationcrossref_fk", eventOd, EventsEvent)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
    /** Foreign key referencing OrganisationsOrganisation (database name organisations_organisation_events_eventorganisationcrossref_fk) */
    lazy val organisationsOrganisationFk = foreignKey("organisations_organisation_events_eventorganisationcrossref_fk", organisationId, OrganisationsOrganisation)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
  }
  /** Collection-like TableQuery object for table EventsEventorganisationcrossref */
  lazy val EventsEventorganisationcrossref = new TableQuery(tag => new EventsEventorganisationcrossref(tag))

  /** Entity class storing rows of table EventsEventpersoncrossref
   *  @param id Database column id SqlType(serial), AutoInc, PrimaryKey
   *  @param dateCreated Database column date_created SqlType(timestamp)
   *  @param lastUpdated Database column last_updated SqlType(timestamp)
   *  @param personId Database column person_id SqlType(int4)
   *  @param eventOd Database column event_od SqlType(int4)
   *  @param relationshipDescription Database column relationship_description SqlType(text), Default(None)
   *  @param isCurrent Database column is_current SqlType(bool) */
  case class EventsEventpersoncrossrefRow(id: Int, dateCreated: org.joda.time.LocalDateTime, lastUpdated: org.joda.time.LocalDateTime, personId: Int, eventOd: Int, relationshipDescription: Option[String] = None, isCurrent: Boolean)
  /** GetResult implicit for fetching EventsEventpersoncrossrefRow objects using plain SQL queries */
  implicit def GetResultEventsEventpersoncrossrefRow(implicit e0: GR[Int], e1: GR[org.joda.time.LocalDateTime], e2: GR[Option[String]], e3: GR[Boolean]): GR[EventsEventpersoncrossrefRow] = GR{
    prs => import prs._
    EventsEventpersoncrossrefRow.tupled((<<[Int], <<[org.joda.time.LocalDateTime], <<[org.joda.time.LocalDateTime], <<[Int], <<[Int], <<?[String], <<[Boolean]))
  }
  /** Table description of table events_eventpersoncrossref. Objects of this class serve as prototypes for rows in queries. */
  class EventsEventpersoncrossref(_tableTag: Tag) extends Table[EventsEventpersoncrossrefRow](_tableTag, "events_eventpersoncrossref") {
    def * = (id, dateCreated, lastUpdated, personId, eventOd, relationshipDescription, isCurrent) <> (EventsEventpersoncrossrefRow.tupled, EventsEventpersoncrossrefRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(dateCreated), Rep.Some(lastUpdated), Rep.Some(personId), Rep.Some(eventOd), relationshipDescription, Rep.Some(isCurrent)).shaped.<>({r=>import r._; _1.map(_=> EventsEventpersoncrossrefRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6, _7.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

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
    /** Database column relationship_description SqlType(text), Default(None) */
    val relationshipDescription: Rep[Option[String]] = column[Option[String]]("relationship_description", O.Default(None))
    /** Database column is_current SqlType(bool) */
    val isCurrent: Rep[Boolean] = column[Boolean]("is_current")

    /** Foreign key referencing EventsEvent (database name events_eventpersoncrossref_thing_id_fkey) */
    lazy val eventsEventFk = foreignKey("events_eventpersoncrossref_thing_id_fkey", eventOd, EventsEvent)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
    /** Foreign key referencing PeoplePerson (database name people_person_people_eventpersoncrossref_fk) */
    lazy val peoplePersonFk = foreignKey("people_person_people_eventpersoncrossref_fk", personId, PeoplePerson)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
  }
  /** Collection-like TableQuery object for table EventsEventpersoncrossref */
  lazy val EventsEventpersoncrossref = new TableQuery(tag => new EventsEventpersoncrossref(tag))

  /** Entity class storing rows of table EventsEventthingcrossref
   *  @param id Database column id SqlType(serial), AutoInc, PrimaryKey
   *  @param dateCreated Database column date_created SqlType(timestamp)
   *  @param lastUpdated Database column last_updated SqlType(timestamp)
   *  @param thingId Database column thing_id SqlType(int4)
   *  @param eventId Database column event_id SqlType(int4)
   *  @param relationshipDescription Database column relationship_description SqlType(text)
   *  @param isCurrent Database column is_current SqlType(bool) */
  case class EventsEventthingcrossrefRow(id: Int, dateCreated: org.joda.time.LocalDateTime, lastUpdated: org.joda.time.LocalDateTime, thingId: Int, eventId: Int, relationshipDescription: String, isCurrent: Boolean)
  /** GetResult implicit for fetching EventsEventthingcrossrefRow objects using plain SQL queries */
  implicit def GetResultEventsEventthingcrossrefRow(implicit e0: GR[Int], e1: GR[org.joda.time.LocalDateTime], e2: GR[String], e3: GR[Boolean]): GR[EventsEventthingcrossrefRow] = GR{
    prs => import prs._
    EventsEventthingcrossrefRow.tupled((<<[Int], <<[org.joda.time.LocalDateTime], <<[org.joda.time.LocalDateTime], <<[Int], <<[Int], <<[String], <<[Boolean]))
  }
  /** Table description of table events_eventthingcrossref. Objects of this class serve as prototypes for rows in queries. */
  class EventsEventthingcrossref(_tableTag: Tag) extends Table[EventsEventthingcrossrefRow](_tableTag, "events_eventthingcrossref") {
    def * = (id, dateCreated, lastUpdated, thingId, eventId, relationshipDescription, isCurrent) <> (EventsEventthingcrossrefRow.tupled, EventsEventthingcrossrefRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(dateCreated), Rep.Some(lastUpdated), Rep.Some(thingId), Rep.Some(eventId), Rep.Some(relationshipDescription), Rep.Some(isCurrent)).shaped.<>({r=>import r._; _1.map(_=> EventsEventthingcrossrefRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get, _7.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

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
    /** Database column relationship_description SqlType(text) */
    val relationshipDescription: Rep[String] = column[String]("relationship_description")
    /** Database column is_current SqlType(bool) */
    val isCurrent: Rep[Boolean] = column[Boolean]("is_current")

    /** Foreign key referencing EventsEvent (database name events_eventthingcrossref_fk) */
    lazy val eventsEventFk = foreignKey("events_eventthingcrossref_fk", eventId, EventsEvent)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
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
   *  @param relationshipType Database column relationship_type SqlType(varchar), Length(100,true), Default(None) */
  case class EventsEventtoeventcrossrefRow(id: Int, dateCreated: org.joda.time.LocalDateTime, lastUpdated: org.joda.time.LocalDateTime, eventOneId: Int, eventTwoId: Int, relationshipType: Option[String] = None)
  /** GetResult implicit for fetching EventsEventtoeventcrossrefRow objects using plain SQL queries */
  implicit def GetResultEventsEventtoeventcrossrefRow(implicit e0: GR[Int], e1: GR[org.joda.time.LocalDateTime], e2: GR[Option[String]]): GR[EventsEventtoeventcrossrefRow] = GR{
    prs => import prs._
    EventsEventtoeventcrossrefRow.tupled((<<[Int], <<[org.joda.time.LocalDateTime], <<[org.joda.time.LocalDateTime], <<[Int], <<[Int], <<?[String]))
  }
  /** Table description of table events_eventtoeventcrossref. Objects of this class serve as prototypes for rows in queries. */
  class EventsEventtoeventcrossref(_tableTag: Tag) extends Table[EventsEventtoeventcrossrefRow](_tableTag, "events_eventtoeventcrossref") {
    def * = (id, dateCreated, lastUpdated, eventOneId, eventTwoId, relationshipType) <> (EventsEventtoeventcrossrefRow.tupled, EventsEventtoeventcrossrefRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(dateCreated), Rep.Some(lastUpdated), Rep.Some(eventOneId), Rep.Some(eventTwoId), relationshipType).shaped.<>({r=>import r._; _1.map(_=> EventsEventtoeventcrossrefRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

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
    /** Database column relationship_type SqlType(varchar), Length(100,true), Default(None) */
    val relationshipType: Rep[Option[String]] = column[Option[String]]("relationship_type", O.Length(100,varying=true), O.Default(None))

    /** Foreign key referencing EventsEvent (database name event_one_id_refs_id_fk) */
    lazy val eventsEventFk1 = foreignKey("event_one_id_refs_id_fk", eventOneId, EventsEvent)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
    /** Foreign key referencing EventsEvent (database name event_two_id_refs_id_fk) */
    lazy val eventsEventFk2 = foreignKey("event_two_id_refs_id_fk", eventTwoId, EventsEvent)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
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
   *  @param relationshipType Database column relationship_type SqlType(varchar), Length(100,true), Default(None)
   *  @param isCurrent Database column is_current SqlType(bool) */
  case class EventsSystempropertydynamiccrossrefRow(id: Int, dateCreated: org.joda.time.LocalDateTime, lastUpdated: org.joda.time.LocalDateTime, eventId: Int, systemPropertyId: Int, fieldId: Int, relationshipType: Option[String] = None, isCurrent: Boolean)
  /** GetResult implicit for fetching EventsSystempropertydynamiccrossrefRow objects using plain SQL queries */
  implicit def GetResultEventsSystempropertydynamiccrossrefRow(implicit e0: GR[Int], e1: GR[org.joda.time.LocalDateTime], e2: GR[Option[String]], e3: GR[Boolean]): GR[EventsSystempropertydynamiccrossrefRow] = GR{
    prs => import prs._
    EventsSystempropertydynamiccrossrefRow.tupled((<<[Int], <<[org.joda.time.LocalDateTime], <<[org.joda.time.LocalDateTime], <<[Int], <<[Int], <<[Int], <<?[String], <<[Boolean]))
  }
  /** Table description of table events_systempropertydynamiccrossref. Objects of this class serve as prototypes for rows in queries. */
  class EventsSystempropertydynamiccrossref(_tableTag: Tag) extends Table[EventsSystempropertydynamiccrossrefRow](_tableTag, "events_systempropertydynamiccrossref") {
    def * = (id, dateCreated, lastUpdated, eventId, systemPropertyId, fieldId, relationshipType, isCurrent) <> (EventsSystempropertydynamiccrossrefRow.tupled, EventsSystempropertydynamiccrossrefRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(dateCreated), Rep.Some(lastUpdated), Rep.Some(eventId), Rep.Some(systemPropertyId), Rep.Some(fieldId), relationshipType, Rep.Some(isCurrent)).shaped.<>({r=>import r._; _1.map(_=> EventsSystempropertydynamiccrossrefRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get, _7, _8.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

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
    /** Database column relationship_type SqlType(varchar), Length(100,true), Default(None) */
    val relationshipType: Rep[Option[String]] = column[Option[String]]("relationship_type", O.Length(100,varying=true), O.Default(None))
    /** Database column is_current SqlType(bool) */
    val isCurrent: Rep[Boolean] = column[Boolean]("is_current")

    /** Foreign key referencing DataField (database name data_field_events_systempropertydynamiccrossref_fk) */
    lazy val dataFieldFk = foreignKey("data_field_events_systempropertydynamiccrossref_fk", fieldId, DataField)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
    /** Foreign key referencing EventsEvent (database name events_systempropertydynamiccrossref_fk) */
    lazy val eventsEventFk = foreignKey("events_systempropertydynamiccrossref_fk", eventId, EventsEvent)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
    /** Foreign key referencing SystemProperty (database name system_property_events_systempropertydynamiccrossref_fk) */
    lazy val systemPropertyFk = foreignKey("system_property_events_systempropertydynamiccrossref_fk", systemPropertyId, SystemProperty)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
  }
  /** Collection-like TableQuery object for table EventsSystempropertydynamiccrossref */
  lazy val EventsSystempropertydynamiccrossref = new TableQuery(tag => new EventsSystempropertydynamiccrossref(tag))

  /** Entity class storing rows of table EventsSystempropertystaticcrossref
   *  @param id Database column id SqlType(int4), PrimaryKey
   *  @param dateCreated Database column date_created SqlType(timestamp)
   *  @param lastUpdated Database column last_updated SqlType(timestamp)
   *  @param eventId Database column event_id SqlType(int4)
   *  @param systemPropertyId Database column system_property_id SqlType(int4)
   *  @param recordId Database column record_id SqlType(int4)
   *  @param fieldId Database column field_id SqlType(int4)
   *  @param relationshipType Database column relationship_type SqlType(varchar), Length(100,true), Default(None)
   *  @param isCurrent Database column is_current SqlType(bool) */
  case class EventsSystempropertystaticcrossrefRow(id: Int, dateCreated: org.joda.time.LocalDateTime, lastUpdated: org.joda.time.LocalDateTime, eventId: Int, systemPropertyId: Int, recordId: Int, fieldId: Int, relationshipType: Option[String] = None, isCurrent: Boolean)
  /** GetResult implicit for fetching EventsSystempropertystaticcrossrefRow objects using plain SQL queries */
  implicit def GetResultEventsSystempropertystaticcrossrefRow(implicit e0: GR[Int], e1: GR[org.joda.time.LocalDateTime], e2: GR[Option[String]], e3: GR[Boolean]): GR[EventsSystempropertystaticcrossrefRow] = GR{
    prs => import prs._
    EventsSystempropertystaticcrossrefRow.tupled((<<[Int], <<[org.joda.time.LocalDateTime], <<[org.joda.time.LocalDateTime], <<[Int], <<[Int], <<[Int], <<[Int], <<?[String], <<[Boolean]))
  }
  /** Table description of table events_systempropertystaticcrossref. Objects of this class serve as prototypes for rows in queries. */
  class EventsSystempropertystaticcrossref(_tableTag: Tag) extends Table[EventsSystempropertystaticcrossrefRow](_tableTag, "events_systempropertystaticcrossref") {
    def * = (id, dateCreated, lastUpdated, eventId, systemPropertyId, recordId, fieldId, relationshipType, isCurrent) <> (EventsSystempropertystaticcrossrefRow.tupled, EventsSystempropertystaticcrossrefRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(dateCreated), Rep.Some(lastUpdated), Rep.Some(eventId), Rep.Some(systemPropertyId), Rep.Some(recordId), Rep.Some(fieldId), relationshipType, Rep.Some(isCurrent)).shaped.<>({r=>import r._; _1.map(_=> EventsSystempropertystaticcrossrefRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get, _7.get, _8, _9.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(int4), PrimaryKey */
    val id: Rep[Int] = column[Int]("id", O.PrimaryKey)
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
    /** Database column relationship_type SqlType(varchar), Length(100,true), Default(None) */
    val relationshipType: Rep[Option[String]] = column[Option[String]]("relationship_type", O.Length(100,varying=true), O.Default(None))
    /** Database column is_current SqlType(bool) */
    val isCurrent: Rep[Boolean] = column[Boolean]("is_current")

    /** Foreign key referencing DataField (database name data_field_events_systempropertystaticcrossref_fk) */
    lazy val dataFieldFk = foreignKey("data_field_events_systempropertystaticcrossref_fk", fieldId, DataField)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
    /** Foreign key referencing DataRecord (database name data_record_events_systempropertystaticcrossref_fk) */
    lazy val dataRecordFk = foreignKey("data_record_events_systempropertystaticcrossref_fk", recordId, DataRecord)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
    /** Foreign key referencing EventsEvent (database name events_systempropertycrossref_fk) */
    lazy val eventsEventFk = foreignKey("events_systempropertycrossref_fk", eventId, EventsEvent)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
    /** Foreign key referencing SystemProperty (database name system_property_events_systempropertystaticcrossref_fk) */
    lazy val systemPropertyFk = foreignKey("system_property_events_systempropertystaticcrossref_fk", systemPropertyId, SystemProperty)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
  }
  /** Collection-like TableQuery object for table EventsSystempropertystaticcrossref */
  lazy val EventsSystempropertystaticcrossref = new TableQuery(tag => new EventsSystempropertystaticcrossref(tag))

  /** Entity class storing rows of table EventsSystemtypecrossref
   *  @param id Database column id SqlType(serial), AutoInc, PrimaryKey
   *  @param dateCreated Database column date_created SqlType(timestamp)
   *  @param lastUpdated Database column last_updated SqlType(timestamp)
   *  @param eventId Database column event_id SqlType(int4)
   *  @param systemTypeId Database column system_type_id SqlType(int4)
   *  @param relationshipType Database column relationship_type SqlType(varchar), Length(100,true), Default(None)
   *  @param isCurrent Database column is_current SqlType(bool) */
  case class EventsSystemtypecrossrefRow(id: Int, dateCreated: org.joda.time.LocalDateTime, lastUpdated: org.joda.time.LocalDateTime, eventId: Int, systemTypeId: Int, relationshipType: Option[String] = None, isCurrent: Boolean)
  /** GetResult implicit for fetching EventsSystemtypecrossrefRow objects using plain SQL queries */
  implicit def GetResultEventsSystemtypecrossrefRow(implicit e0: GR[Int], e1: GR[org.joda.time.LocalDateTime], e2: GR[Option[String]], e3: GR[Boolean]): GR[EventsSystemtypecrossrefRow] = GR{
    prs => import prs._
    EventsSystemtypecrossrefRow.tupled((<<[Int], <<[org.joda.time.LocalDateTime], <<[org.joda.time.LocalDateTime], <<[Int], <<[Int], <<?[String], <<[Boolean]))
  }
  /** Table description of table events_systemtypecrossref. Objects of this class serve as prototypes for rows in queries. */
  class EventsSystemtypecrossref(_tableTag: Tag) extends Table[EventsSystemtypecrossrefRow](_tableTag, "events_systemtypecrossref") {
    def * = (id, dateCreated, lastUpdated, eventId, systemTypeId, relationshipType, isCurrent) <> (EventsSystemtypecrossrefRow.tupled, EventsSystemtypecrossrefRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(dateCreated), Rep.Some(lastUpdated), Rep.Some(eventId), Rep.Some(systemTypeId), relationshipType, Rep.Some(isCurrent)).shaped.<>({r=>import r._; _1.map(_=> EventsSystemtypecrossrefRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6, _7.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

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
    /** Database column relationship_type SqlType(varchar), Length(100,true), Default(None) */
    val relationshipType: Rep[Option[String]] = column[Option[String]]("relationship_type", O.Length(100,varying=true), O.Default(None))
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
   *  @param relationshipType Database column relationship_type SqlType(varchar), Length(100,true), Default(None)
   *  @param isCurrent Database column is_current SqlType(bool) */
  case class LocationsLocationthingcrossrefRow(id: Int, dateCreated: org.joda.time.LocalDateTime, lastUpdated: org.joda.time.LocalDateTime, locationId: Int, thingId: Int, relationshipType: Option[String] = None, isCurrent: Boolean)
  /** GetResult implicit for fetching LocationsLocationthingcrossrefRow objects using plain SQL queries */
  implicit def GetResultLocationsLocationthingcrossrefRow(implicit e0: GR[Int], e1: GR[org.joda.time.LocalDateTime], e2: GR[Option[String]], e3: GR[Boolean]): GR[LocationsLocationthingcrossrefRow] = GR{
    prs => import prs._
    LocationsLocationthingcrossrefRow.tupled((<<[Int], <<[org.joda.time.LocalDateTime], <<[org.joda.time.LocalDateTime], <<[Int], <<[Int], <<?[String], <<[Boolean]))
  }
  /** Table description of table locations_locationthingcrossref. Objects of this class serve as prototypes for rows in queries. */
  class LocationsLocationthingcrossref(_tableTag: Tag) extends Table[LocationsLocationthingcrossrefRow](_tableTag, "locations_locationthingcrossref") {
    def * = (id, dateCreated, lastUpdated, locationId, thingId, relationshipType, isCurrent) <> (LocationsLocationthingcrossrefRow.tupled, LocationsLocationthingcrossrefRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(dateCreated), Rep.Some(lastUpdated), Rep.Some(locationId), Rep.Some(thingId), relationshipType, Rep.Some(isCurrent)).shaped.<>({r=>import r._; _1.map(_=> LocationsLocationthingcrossrefRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6, _7.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

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
    /** Database column relationship_type SqlType(varchar), Length(100,true), Default(None) */
    val relationshipType: Rep[Option[String]] = column[Option[String]]("relationship_type", O.Length(100,varying=true), O.Default(None))
    /** Database column is_current SqlType(bool) */
    val isCurrent: Rep[Boolean] = column[Boolean]("is_current")

    /** Foreign key referencing LocationsLocation (database name locations_locationthingcrossref_location_id_fkey) */
    lazy val locationsLocationFk = foreignKey("locations_locationthingcrossref_location_id_fkey", locationId, LocationsLocation)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
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
   *  @param relationshipType Database column relationship_type SqlType(varchar), Length(100,true), Default(None) */
  case class LocationsLocationtolocationcrossrefRow(id: Int, dateCreated: org.joda.time.LocalDateTime, lastUpdated: org.joda.time.LocalDateTime, locOneId: Int, locTwoId: Int, relationshipType: Option[String] = None)
  /** GetResult implicit for fetching LocationsLocationtolocationcrossrefRow objects using plain SQL queries */
  implicit def GetResultLocationsLocationtolocationcrossrefRow(implicit e0: GR[Int], e1: GR[org.joda.time.LocalDateTime], e2: GR[Option[String]]): GR[LocationsLocationtolocationcrossrefRow] = GR{
    prs => import prs._
    LocationsLocationtolocationcrossrefRow.tupled((<<[Int], <<[org.joda.time.LocalDateTime], <<[org.joda.time.LocalDateTime], <<[Int], <<[Int], <<?[String]))
  }
  /** Table description of table locations_locationtolocationcrossref. Objects of this class serve as prototypes for rows in queries. */
  class LocationsLocationtolocationcrossref(_tableTag: Tag) extends Table[LocationsLocationtolocationcrossrefRow](_tableTag, "locations_locationtolocationcrossref") {
    def * = (id, dateCreated, lastUpdated, locOneId, locTwoId, relationshipType) <> (LocationsLocationtolocationcrossrefRow.tupled, LocationsLocationtolocationcrossrefRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(dateCreated), Rep.Some(lastUpdated), Rep.Some(locOneId), Rep.Some(locTwoId), relationshipType).shaped.<>({r=>import r._; _1.map(_=> LocationsLocationtolocationcrossrefRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

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
    /** Database column relationship_type SqlType(varchar), Length(100,true), Default(None) */
    val relationshipType: Rep[Option[String]] = column[Option[String]]("relationship_type", O.Length(100,varying=true), O.Default(None))

    /** Foreign key referencing LocationsLocation (database name locations_locationtolocationcrossref_loc_one_id_fkey) */
    lazy val locationsLocationFk1 = foreignKey("locations_locationtolocationcrossref_loc_one_id_fkey", locOneId, LocationsLocation)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
    /** Foreign key referencing LocationsLocation (database name locations_locationtolocationcrossref_loc_two_id_fkey) */
    lazy val locationsLocationFk2 = foreignKey("locations_locationtolocationcrossref_loc_two_id_fkey", locTwoId, LocationsLocation)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
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
   *  @param relationshipType Database column relationship_type SqlType(varchar), Length(100,true), Default(None)
   *  @param isCurrent Database column is_current SqlType(bool) */
  case class LocationsSystempropertydynamiccrossrefRow(id: Int, dateCreated: org.joda.time.LocalDateTime, lastUpdated: org.joda.time.LocalDateTime, locationId: Int, systemPropertyId: Int, fieldId: Int, relationshipType: Option[String] = None, isCurrent: Boolean)
  /** GetResult implicit for fetching LocationsSystempropertydynamiccrossrefRow objects using plain SQL queries */
  implicit def GetResultLocationsSystempropertydynamiccrossrefRow(implicit e0: GR[Int], e1: GR[org.joda.time.LocalDateTime], e2: GR[Option[String]], e3: GR[Boolean]): GR[LocationsSystempropertydynamiccrossrefRow] = GR{
    prs => import prs._
    LocationsSystempropertydynamiccrossrefRow.tupled((<<[Int], <<[org.joda.time.LocalDateTime], <<[org.joda.time.LocalDateTime], <<[Int], <<[Int], <<[Int], <<?[String], <<[Boolean]))
  }
  /** Table description of table locations_systempropertydynamiccrossref. Objects of this class serve as prototypes for rows in queries. */
  class LocationsSystempropertydynamiccrossref(_tableTag: Tag) extends Table[LocationsSystempropertydynamiccrossrefRow](_tableTag, "locations_systempropertydynamiccrossref") {
    def * = (id, dateCreated, lastUpdated, locationId, systemPropertyId, fieldId, relationshipType, isCurrent) <> (LocationsSystempropertydynamiccrossrefRow.tupled, LocationsSystempropertydynamiccrossrefRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(dateCreated), Rep.Some(lastUpdated), Rep.Some(locationId), Rep.Some(systemPropertyId), Rep.Some(fieldId), relationshipType, Rep.Some(isCurrent)).shaped.<>({r=>import r._; _1.map(_=> LocationsSystempropertydynamiccrossrefRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get, _7, _8.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

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
    /** Database column relationship_type SqlType(varchar), Length(100,true), Default(None) */
    val relationshipType: Rep[Option[String]] = column[Option[String]]("relationship_type", O.Length(100,varying=true), O.Default(None))
    /** Database column is_current SqlType(bool) */
    val isCurrent: Rep[Boolean] = column[Boolean]("is_current")

    /** Foreign key referencing DataField (database name data_field_locations_systempropertydynamiccrossref_fk) */
    lazy val dataFieldFk = foreignKey("data_field_locations_systempropertydynamiccrossref_fk", fieldId, DataField)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
    /** Foreign key referencing LocationsLocation (database name locations_location_locations_systempropertydynamiccrossref_fk) */
    lazy val locationsLocationFk = foreignKey("locations_location_locations_systempropertydynamiccrossref_fk", locationId, LocationsLocation)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
    /** Foreign key referencing SystemProperty (database name system_property_locations_systempropertydynamiccrossref_fk) */
    lazy val systemPropertyFk = foreignKey("system_property_locations_systempropertydynamiccrossref_fk", systemPropertyId, SystemProperty)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
  }
  /** Collection-like TableQuery object for table LocationsSystempropertydynamiccrossref */
  lazy val LocationsSystempropertydynamiccrossref = new TableQuery(tag => new LocationsSystempropertydynamiccrossref(tag))

  /** Entity class storing rows of table LocationsSystempropertystaticcrossref
   *  @param id Database column id SqlType(int4), PrimaryKey
   *  @param dateCreated Database column date_created SqlType(timestamp)
   *  @param lastUpdated Database column last_updated SqlType(timestamp)
   *  @param locationId Database column location_id SqlType(int4)
   *  @param systemPropertyId Database column system_property_id SqlType(int4)
   *  @param recordId Database column record_id SqlType(int4)
   *  @param fieldId Database column field_id SqlType(int4)
   *  @param relationshipType Database column relationship_type SqlType(varchar), Length(100,true), Default(None)
   *  @param isCurrent Database column is_current SqlType(bool) */
  case class LocationsSystempropertystaticcrossrefRow(id: Int, dateCreated: org.joda.time.LocalDateTime, lastUpdated: org.joda.time.LocalDateTime, locationId: Int, systemPropertyId: Int, recordId: Int, fieldId: Int, relationshipType: Option[String] = None, isCurrent: Boolean)
  /** GetResult implicit for fetching LocationsSystempropertystaticcrossrefRow objects using plain SQL queries */
  implicit def GetResultLocationsSystempropertystaticcrossrefRow(implicit e0: GR[Int], e1: GR[org.joda.time.LocalDateTime], e2: GR[Option[String]], e3: GR[Boolean]): GR[LocationsSystempropertystaticcrossrefRow] = GR{
    prs => import prs._
    LocationsSystempropertystaticcrossrefRow.tupled((<<[Int], <<[org.joda.time.LocalDateTime], <<[org.joda.time.LocalDateTime], <<[Int], <<[Int], <<[Int], <<[Int], <<?[String], <<[Boolean]))
  }
  /** Table description of table locations_systempropertystaticcrossref. Objects of this class serve as prototypes for rows in queries. */
  class LocationsSystempropertystaticcrossref(_tableTag: Tag) extends Table[LocationsSystempropertystaticcrossrefRow](_tableTag, "locations_systempropertystaticcrossref") {
    def * = (id, dateCreated, lastUpdated, locationId, systemPropertyId, recordId, fieldId, relationshipType, isCurrent) <> (LocationsSystempropertystaticcrossrefRow.tupled, LocationsSystempropertystaticcrossrefRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(dateCreated), Rep.Some(lastUpdated), Rep.Some(locationId), Rep.Some(systemPropertyId), Rep.Some(recordId), Rep.Some(fieldId), relationshipType, Rep.Some(isCurrent)).shaped.<>({r=>import r._; _1.map(_=> LocationsSystempropertystaticcrossrefRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get, _7.get, _8, _9.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(int4), PrimaryKey */
    val id: Rep[Int] = column[Int]("id", O.PrimaryKey)
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
    /** Database column relationship_type SqlType(varchar), Length(100,true), Default(None) */
    val relationshipType: Rep[Option[String]] = column[Option[String]]("relationship_type", O.Length(100,varying=true), O.Default(None))
    /** Database column is_current SqlType(bool) */
    val isCurrent: Rep[Boolean] = column[Boolean]("is_current")

    /** Foreign key referencing DataField (database name data_field_locations_systempropertystaticcrossref_fk) */
    lazy val dataFieldFk = foreignKey("data_field_locations_systempropertystaticcrossref_fk", fieldId, DataField)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
    /** Foreign key referencing DataRecord (database name data_record_locations_systempropertystaticcrossref_fk) */
    lazy val dataRecordFk = foreignKey("data_record_locations_systempropertystaticcrossref_fk", recordId, DataRecord)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
    /** Foreign key referencing LocationsLocation (database name locations_location_locations_systempropertystaticcrossref_fk) */
    lazy val locationsLocationFk = foreignKey("locations_location_locations_systempropertystaticcrossref_fk", locationId, LocationsLocation)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
    /** Foreign key referencing SystemProperty (database name system_property_locations_systempropertystaticcrossref_fk) */
    lazy val systemPropertyFk = foreignKey("system_property_locations_systempropertystaticcrossref_fk", systemPropertyId, SystemProperty)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
  }
  /** Collection-like TableQuery object for table LocationsSystempropertystaticcrossref */
  lazy val LocationsSystempropertystaticcrossref = new TableQuery(tag => new LocationsSystempropertystaticcrossref(tag))

  /** Entity class storing rows of table LocationSystemtypecrossref
   *  @param id Database column id SqlType(serial), AutoInc, PrimaryKey
   *  @param dateCreated Database column date_created SqlType(timestamp)
   *  @param lastUpdated Database column last_updated SqlType(timestamp)
   *  @param locationId Database column location_id SqlType(int4)
   *  @param systemTypeId Database column system_type_id SqlType(int4)
   *  @param relationshipType Database column relationship_type SqlType(varchar), Length(100,true), Default(None)
   *  @param isCurrent Database column is_current SqlType(bool) */
  case class LocationSystemtypecrossrefRow(id: Int, dateCreated: org.joda.time.LocalDateTime, lastUpdated: org.joda.time.LocalDateTime, locationId: Int, systemTypeId: Int, relationshipType: Option[String] = None, isCurrent: Boolean)
  /** GetResult implicit for fetching LocationSystemtypecrossrefRow objects using plain SQL queries */
  implicit def GetResultLocationSystemtypecrossrefRow(implicit e0: GR[Int], e1: GR[org.joda.time.LocalDateTime], e2: GR[Option[String]], e3: GR[Boolean]): GR[LocationSystemtypecrossrefRow] = GR{
    prs => import prs._
    LocationSystemtypecrossrefRow.tupled((<<[Int], <<[org.joda.time.LocalDateTime], <<[org.joda.time.LocalDateTime], <<[Int], <<[Int], <<?[String], <<[Boolean]))
  }
  /** Table description of table location_systemtypecrossref. Objects of this class serve as prototypes for rows in queries. */
  class LocationSystemtypecrossref(_tableTag: Tag) extends Table[LocationSystemtypecrossrefRow](_tableTag, "location_systemtypecrossref") {
    def * = (id, dateCreated, lastUpdated, locationId, systemTypeId, relationshipType, isCurrent) <> (LocationSystemtypecrossrefRow.tupled, LocationSystemtypecrossrefRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(dateCreated), Rep.Some(lastUpdated), Rep.Some(locationId), Rep.Some(systemTypeId), relationshipType, Rep.Some(isCurrent)).shaped.<>({r=>import r._; _1.map(_=> LocationSystemtypecrossrefRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6, _7.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

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
    /** Database column relationship_type SqlType(varchar), Length(100,true), Default(None) */
    val relationshipType: Rep[Option[String]] = column[Option[String]]("relationship_type", O.Length(100,varying=true), O.Default(None))
    /** Database column is_current SqlType(bool) */
    val isCurrent: Rep[Boolean] = column[Boolean]("is_current")

    /** Foreign key referencing LocationsLocation (database name locations_location_location_systemtypecrossref_fk) */
    lazy val locationsLocationFk = foreignKey("locations_location_location_systemtypecrossref_fk", locationId, LocationsLocation)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
    /** Foreign key referencing SystemType (database name system_type_location_systemtypecrossref_fk) */
    lazy val systemTypeFk = foreignKey("system_type_location_systemtypecrossref_fk", systemTypeId, SystemType)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
  }
  /** Collection-like TableQuery object for table LocationSystemtypecrossref */
  lazy val LocationSystemtypecrossref = new TableQuery(tag => new LocationSystemtypecrossref(tag))

  /** Entity class storing rows of table OrganisationOrganisationtoorganisationcrossref
   *  @param id Database column id SqlType(int4), PrimaryKey
   *  @param dateCreated Database column date_created SqlType(timestamp)
   *  @param lastUpdated Database column last_updated SqlType(timestamp)
   *  @param organisationOneId Database column organisation_one_id SqlType(int4)
   *  @param organisationTwoId Database column organisation_two_id SqlType(int4)
   *  @param relationshipType Database column relationship_type SqlType(varchar), Length(100,true)
   *  @param isCurrent Database column is_current SqlType(bool) */
  case class OrganisationOrganisationtoorganisationcrossrefRow(id: Int, dateCreated: org.joda.time.LocalDateTime, lastUpdated: org.joda.time.LocalDateTime, organisationOneId: Int, organisationTwoId: Int, relationshipType: String, isCurrent: Boolean)
  /** GetResult implicit for fetching OrganisationOrganisationtoorganisationcrossrefRow objects using plain SQL queries */
  implicit def GetResultOrganisationOrganisationtoorganisationcrossrefRow(implicit e0: GR[Int], e1: GR[org.joda.time.LocalDateTime], e2: GR[String], e3: GR[Boolean]): GR[OrganisationOrganisationtoorganisationcrossrefRow] = GR{
    prs => import prs._
    OrganisationOrganisationtoorganisationcrossrefRow.tupled((<<[Int], <<[org.joda.time.LocalDateTime], <<[org.joda.time.LocalDateTime], <<[Int], <<[Int], <<[String], <<[Boolean]))
  }
  /** Table description of table organisation_organisationtoorganisationcrossref. Objects of this class serve as prototypes for rows in queries. */
  class OrganisationOrganisationtoorganisationcrossref(_tableTag: Tag) extends Table[OrganisationOrganisationtoorganisationcrossrefRow](_tableTag, "organisation_organisationtoorganisationcrossref") {
    def * = (id, dateCreated, lastUpdated, organisationOneId, organisationTwoId, relationshipType, isCurrent) <> (OrganisationOrganisationtoorganisationcrossrefRow.tupled, OrganisationOrganisationtoorganisationcrossrefRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(dateCreated), Rep.Some(lastUpdated), Rep.Some(organisationOneId), Rep.Some(organisationTwoId), Rep.Some(relationshipType), Rep.Some(isCurrent)).shaped.<>({r=>import r._; _1.map(_=> OrganisationOrganisationtoorganisationcrossrefRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get, _7.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(int4), PrimaryKey */
    val id: Rep[Int] = column[Int]("id", O.PrimaryKey)
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

    /** Foreign key referencing OrganisationsOrganisation (database name organisations_organisation_organisation_organisationtoorgani645) */
    lazy val organisationsOrganisationFk1 = foreignKey("organisations_organisation_organisation_organisationtoorgani645", organisationTwoId, OrganisationsOrganisation)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
    /** Foreign key referencing OrganisationsOrganisation (database name organisations_organisation_organisation_organisationtoorgani876) */
    lazy val organisationsOrganisationFk2 = foreignKey("organisations_organisation_organisation_organisationtoorgani876", organisationOneId, OrganisationsOrganisation)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
  }
  /** Collection-like TableQuery object for table OrganisationOrganisationtoorganisationcrossref */
  lazy val OrganisationOrganisationtoorganisationcrossref = new TableQuery(tag => new OrganisationOrganisationtoorganisationcrossref(tag))

  /** Entity class storing rows of table OrganisationsOrganisation
   *  @param id Database column id SqlType(serial), AutoInc, PrimaryKey
   *  @param name Database column name SqlType(varchar), Length(100,true)
   *  @param dateCreated Database column date_created SqlType(timestamp)
   *  @param lastyUpdated Database column lasty_updated SqlType(timestamp) */
  case class OrganisationsOrganisationRow(id: Int, name: String, dateCreated: org.joda.time.LocalDateTime, lastyUpdated: org.joda.time.LocalDateTime)
  /** GetResult implicit for fetching OrganisationsOrganisationRow objects using plain SQL queries */
  implicit def GetResultOrganisationsOrganisationRow(implicit e0: GR[Int], e1: GR[String], e2: GR[org.joda.time.LocalDateTime]): GR[OrganisationsOrganisationRow] = GR{
    prs => import prs._
    OrganisationsOrganisationRow.tupled((<<[Int], <<[String], <<[org.joda.time.LocalDateTime], <<[org.joda.time.LocalDateTime]))
  }
  /** Table description of table organisations_organisation. Objects of this class serve as prototypes for rows in queries. */
  class OrganisationsOrganisation(_tableTag: Tag) extends Table[OrganisationsOrganisationRow](_tableTag, "organisations_organisation") {
    def * = (id, name, dateCreated, lastyUpdated) <> (OrganisationsOrganisationRow.tupled, OrganisationsOrganisationRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(name), Rep.Some(dateCreated), Rep.Some(lastyUpdated)).shaped.<>({r=>import r._; _1.map(_=> OrganisationsOrganisationRow.tupled((_1.get, _2.get, _3.get, _4.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(serial), AutoInc, PrimaryKey */
    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    /** Database column name SqlType(varchar), Length(100,true) */
    val name: Rep[String] = column[String]("name", O.Length(100,varying=true))
    /** Database column date_created SqlType(timestamp) */
    val dateCreated: Rep[org.joda.time.LocalDateTime] = column[org.joda.time.LocalDateTime]("date_created")
    /** Database column lasty_updated SqlType(timestamp) */
    val lastyUpdated: Rep[org.joda.time.LocalDateTime] = column[org.joda.time.LocalDateTime]("lasty_updated")
  }
  /** Collection-like TableQuery object for table OrganisationsOrganisation */
  lazy val OrganisationsOrganisation = new TableQuery(tag => new OrganisationsOrganisation(tag))

  /** Entity class storing rows of table OrganisationsOrganisationlocationcrossref
   *  @param id Database column id SqlType(serial), AutoInc, PrimaryKey
   *  @param dateCreated Database column date_created SqlType(timestamp)
   *  @param lastUpdated Database column last_updated SqlType(timestamp)
   *  @param locationId Database column location_id SqlType(int4)
   *  @param organisationId Database column organisation_id SqlType(int4)
   *  @param relationshipType Database column relationship_type SqlType(varchar), Length(100,true), Default(None)
   *  @param isCurrent Database column is_current SqlType(bool) */
  case class OrganisationsOrganisationlocationcrossrefRow(id: Int, dateCreated: org.joda.time.LocalDateTime, lastUpdated: org.joda.time.LocalDateTime, locationId: Int, organisationId: Int, relationshipType: Option[String] = None, isCurrent: Boolean)
  /** GetResult implicit for fetching OrganisationsOrganisationlocationcrossrefRow objects using plain SQL queries */
  implicit def GetResultOrganisationsOrganisationlocationcrossrefRow(implicit e0: GR[Int], e1: GR[org.joda.time.LocalDateTime], e2: GR[Option[String]], e3: GR[Boolean]): GR[OrganisationsOrganisationlocationcrossrefRow] = GR{
    prs => import prs._
    OrganisationsOrganisationlocationcrossrefRow.tupled((<<[Int], <<[org.joda.time.LocalDateTime], <<[org.joda.time.LocalDateTime], <<[Int], <<[Int], <<?[String], <<[Boolean]))
  }
  /** Table description of table organisations_organisationlocationcrossref. Objects of this class serve as prototypes for rows in queries. */
  class OrganisationsOrganisationlocationcrossref(_tableTag: Tag) extends Table[OrganisationsOrganisationlocationcrossrefRow](_tableTag, "organisations_organisationlocationcrossref") {
    def * = (id, dateCreated, lastUpdated, locationId, organisationId, relationshipType, isCurrent) <> (OrganisationsOrganisationlocationcrossrefRow.tupled, OrganisationsOrganisationlocationcrossrefRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(dateCreated), Rep.Some(lastUpdated), Rep.Some(locationId), Rep.Some(organisationId), relationshipType, Rep.Some(isCurrent)).shaped.<>({r=>import r._; _1.map(_=> OrganisationsOrganisationlocationcrossrefRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6, _7.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

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
    /** Database column relationship_type SqlType(varchar), Length(100,true), Default(None) */
    val relationshipType: Rep[Option[String]] = column[Option[String]]("relationship_type", O.Length(100,varying=true), O.Default(None))
    /** Database column is_current SqlType(bool) */
    val isCurrent: Rep[Boolean] = column[Boolean]("is_current")

    /** Foreign key referencing LocationsLocation (database name locations_location_organisations_organisationlocationcrossre499) */
    lazy val locationsLocationFk = foreignKey("locations_location_organisations_organisationlocationcrossre499", locationId, LocationsLocation)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
    /** Foreign key referencing OrganisationsOrganisation (database name organisations_organisationlocationcrossref_organisation_id_fkey) */
    lazy val organisationsOrganisationFk = foreignKey("organisations_organisationlocationcrossref_organisation_id_fkey", organisationId, OrganisationsOrganisation)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
  }
  /** Collection-like TableQuery object for table OrganisationsOrganisationlocationcrossref */
  lazy val OrganisationsOrganisationlocationcrossref = new TableQuery(tag => new OrganisationsOrganisationlocationcrossref(tag))

  /** Entity class storing rows of table OrganisationsSystempropertydynamiccrossref
   *  @param id Database column id SqlType(serial), AutoInc, PrimaryKey
   *  @param dateCreated Database column date_created SqlType(timestamp)
   *  @param lastUpdated Database column last_updated SqlType(timestamp)
   *  @param organisationId Database column organisation_id SqlType(int4)
   *  @param systemPropertyId Database column system_property_id SqlType(int4)
   *  @param fieldId Database column field_id SqlType(int4)
   *  @param relationshipType Database column relationship_type SqlType(varchar), Length(100,true), Default(None)
   *  @param isCurrent Database column is_current SqlType(bool) */
  case class OrganisationsSystempropertydynamiccrossrefRow(id: Int, dateCreated: org.joda.time.LocalDateTime, lastUpdated: org.joda.time.LocalDateTime, organisationId: Int, systemPropertyId: Int, fieldId: Int, relationshipType: Option[String] = None, isCurrent: Boolean)
  /** GetResult implicit for fetching OrganisationsSystempropertydynamiccrossrefRow objects using plain SQL queries */
  implicit def GetResultOrganisationsSystempropertydynamiccrossrefRow(implicit e0: GR[Int], e1: GR[org.joda.time.LocalDateTime], e2: GR[Option[String]], e3: GR[Boolean]): GR[OrganisationsSystempropertydynamiccrossrefRow] = GR{
    prs => import prs._
    OrganisationsSystempropertydynamiccrossrefRow.tupled((<<[Int], <<[org.joda.time.LocalDateTime], <<[org.joda.time.LocalDateTime], <<[Int], <<[Int], <<[Int], <<?[String], <<[Boolean]))
  }
  /** Table description of table organisations_systempropertydynamiccrossref. Objects of this class serve as prototypes for rows in queries. */
  class OrganisationsSystempropertydynamiccrossref(_tableTag: Tag) extends Table[OrganisationsSystempropertydynamiccrossrefRow](_tableTag, "organisations_systempropertydynamiccrossref") {
    def * = (id, dateCreated, lastUpdated, organisationId, systemPropertyId, fieldId, relationshipType, isCurrent) <> (OrganisationsSystempropertydynamiccrossrefRow.tupled, OrganisationsSystempropertydynamiccrossrefRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(dateCreated), Rep.Some(lastUpdated), Rep.Some(organisationId), Rep.Some(systemPropertyId), Rep.Some(fieldId), relationshipType, Rep.Some(isCurrent)).shaped.<>({r=>import r._; _1.map(_=> OrganisationsSystempropertydynamiccrossrefRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get, _7, _8.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

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
    /** Database column relationship_type SqlType(varchar), Length(100,true), Default(None) */
    val relationshipType: Rep[Option[String]] = column[Option[String]]("relationship_type", O.Length(100,varying=true), O.Default(None))
    /** Database column is_current SqlType(bool) */
    val isCurrent: Rep[Boolean] = column[Boolean]("is_current")

    /** Foreign key referencing DataField (database name data_field_organisations_systempropertydynamiccrossref_fk) */
    lazy val dataFieldFk = foreignKey("data_field_organisations_systempropertydynamiccrossref_fk", fieldId, DataField)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
    /** Foreign key referencing OrganisationsOrganisation (database name organisations_organisation_organisations_systempropertydynam75) */
    lazy val organisationsOrganisationFk = foreignKey("organisations_organisation_organisations_systempropertydynam75", organisationId, OrganisationsOrganisation)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
    /** Foreign key referencing SystemProperty (database name system_property_organisations_systempropertydynamiccrossref_fk) */
    lazy val systemPropertyFk = foreignKey("system_property_organisations_systempropertydynamiccrossref_fk", systemPropertyId, SystemProperty)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
  }
  /** Collection-like TableQuery object for table OrganisationsSystempropertydynamiccrossref */
  lazy val OrganisationsSystempropertydynamiccrossref = new TableQuery(tag => new OrganisationsSystempropertydynamiccrossref(tag))

  /** Entity class storing rows of table OrganisationsSystempropertystaticcrossref
   *  @param id Database column id SqlType(int4), PrimaryKey
   *  @param dateCreated Database column date_created SqlType(timestamp)
   *  @param lastUpdated Database column last_updated SqlType(timestamp)
   *  @param organisationId Database column organisation_id SqlType(int4)
   *  @param systemPropertyId Database column system_property_id SqlType(int4)
   *  @param recordId Database column record_id SqlType(int4)
   *  @param fieldId Database column field_id SqlType(int4)
   *  @param relationshipType Database column relationship_type SqlType(varchar), Length(100,true), Default(None)
   *  @param isCurrent Database column is_current SqlType(bool) */
  case class OrganisationsSystempropertystaticcrossrefRow(id: Int, dateCreated: org.joda.time.LocalDateTime, lastUpdated: org.joda.time.LocalDateTime, organisationId: Int, systemPropertyId: Int, recordId: Int, fieldId: Int, relationshipType: Option[String] = None, isCurrent: Boolean)
  /** GetResult implicit for fetching OrganisationsSystempropertystaticcrossrefRow objects using plain SQL queries */
  implicit def GetResultOrganisationsSystempropertystaticcrossrefRow(implicit e0: GR[Int], e1: GR[org.joda.time.LocalDateTime], e2: GR[Option[String]], e3: GR[Boolean]): GR[OrganisationsSystempropertystaticcrossrefRow] = GR{
    prs => import prs._
    OrganisationsSystempropertystaticcrossrefRow.tupled((<<[Int], <<[org.joda.time.LocalDateTime], <<[org.joda.time.LocalDateTime], <<[Int], <<[Int], <<[Int], <<[Int], <<?[String], <<[Boolean]))
  }
  /** Table description of table organisations_systempropertystaticcrossref. Objects of this class serve as prototypes for rows in queries. */
  class OrganisationsSystempropertystaticcrossref(_tableTag: Tag) extends Table[OrganisationsSystempropertystaticcrossrefRow](_tableTag, "organisations_systempropertystaticcrossref") {
    def * = (id, dateCreated, lastUpdated, organisationId, systemPropertyId, recordId, fieldId, relationshipType, isCurrent) <> (OrganisationsSystempropertystaticcrossrefRow.tupled, OrganisationsSystempropertystaticcrossrefRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(dateCreated), Rep.Some(lastUpdated), Rep.Some(organisationId), Rep.Some(systemPropertyId), Rep.Some(recordId), Rep.Some(fieldId), relationshipType, Rep.Some(isCurrent)).shaped.<>({r=>import r._; _1.map(_=> OrganisationsSystempropertystaticcrossrefRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get, _7.get, _8, _9.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(int4), PrimaryKey */
    val id: Rep[Int] = column[Int]("id", O.PrimaryKey)
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
    /** Database column relationship_type SqlType(varchar), Length(100,true), Default(None) */
    val relationshipType: Rep[Option[String]] = column[Option[String]]("relationship_type", O.Length(100,varying=true), O.Default(None))
    /** Database column is_current SqlType(bool) */
    val isCurrent: Rep[Boolean] = column[Boolean]("is_current")

    /** Foreign key referencing DataField (database name data_field_organisations_systempropertystaticcrossref_fk) */
    lazy val dataFieldFk = foreignKey("data_field_organisations_systempropertystaticcrossref_fk", fieldId, DataField)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
    /** Foreign key referencing DataRecord (database name data_record_organisations_systempropertystaticcrossref_fk) */
    lazy val dataRecordFk = foreignKey("data_record_organisations_systempropertystaticcrossref_fk", recordId, DataRecord)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
    /** Foreign key referencing OrganisationsOrganisation (database name organisations_organisation_organisations_systempropertystati434) */
    lazy val organisationsOrganisationFk = foreignKey("organisations_organisation_organisations_systempropertystati434", organisationId, OrganisationsOrganisation)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
    /** Foreign key referencing SystemProperty (database name system_property_organisations_systempropertystaticcrossref_fk) */
    lazy val systemPropertyFk = foreignKey("system_property_organisations_systempropertystaticcrossref_fk", systemPropertyId, SystemProperty)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
  }
  /** Collection-like TableQuery object for table OrganisationsSystempropertystaticcrossref */
  lazy val OrganisationsSystempropertystaticcrossref = new TableQuery(tag => new OrganisationsSystempropertystaticcrossref(tag))

  /** Entity class storing rows of table OrganisationSystemtypecrossref
   *  @param id Database column id SqlType(serial), AutoInc, PrimaryKey
   *  @param dateCreated Database column date_created SqlType(timestamp)
   *  @param lastUpdated Database column last_updated SqlType(timestamp)
   *  @param organisationId Database column organisation_id SqlType(int4)
   *  @param systemTypeId Database column system_type_id SqlType(int4)
   *  @param relationshipType Database column relationship_type SqlType(varchar), Length(100,true), Default(None)
   *  @param isCurrent Database column is_current SqlType(bool) */
  case class OrganisationSystemtypecrossrefRow(id: Int, dateCreated: org.joda.time.LocalDateTime, lastUpdated: org.joda.time.LocalDateTime, organisationId: Int, systemTypeId: Int, relationshipType: Option[String] = None, isCurrent: Boolean)
  /** GetResult implicit for fetching OrganisationSystemtypecrossrefRow objects using plain SQL queries */
  implicit def GetResultOrganisationSystemtypecrossrefRow(implicit e0: GR[Int], e1: GR[org.joda.time.LocalDateTime], e2: GR[Option[String]], e3: GR[Boolean]): GR[OrganisationSystemtypecrossrefRow] = GR{
    prs => import prs._
    OrganisationSystemtypecrossrefRow.tupled((<<[Int], <<[org.joda.time.LocalDateTime], <<[org.joda.time.LocalDateTime], <<[Int], <<[Int], <<?[String], <<[Boolean]))
  }
  /** Table description of table organisation_systemtypecrossref. Objects of this class serve as prototypes for rows in queries. */
  class OrganisationSystemtypecrossref(_tableTag: Tag) extends Table[OrganisationSystemtypecrossrefRow](_tableTag, "organisation_systemtypecrossref") {
    def * = (id, dateCreated, lastUpdated, organisationId, systemTypeId, relationshipType, isCurrent) <> (OrganisationSystemtypecrossrefRow.tupled, OrganisationSystemtypecrossrefRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(dateCreated), Rep.Some(lastUpdated), Rep.Some(organisationId), Rep.Some(systemTypeId), relationshipType, Rep.Some(isCurrent)).shaped.<>({r=>import r._; _1.map(_=> OrganisationSystemtypecrossrefRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6, _7.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

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
    /** Database column relationship_type SqlType(varchar), Length(100,true), Default(None) */
    val relationshipType: Rep[Option[String]] = column[Option[String]]("relationship_type", O.Length(100,varying=true), O.Default(None))
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
   *  @param name Database column name SqlType(varchar)
   *  @param dateCreated Database column date_created SqlType(timestamp)
   *  @param lastUpdated Database column last_updated SqlType(timestamp)
   *  @param personId Database column person_id SqlType(varchar), Length(36,true) */
  case class PeoplePersonRow(id: Int, name: String, dateCreated: org.joda.time.LocalDateTime, lastUpdated: org.joda.time.LocalDateTime, personId: String)
  /** GetResult implicit for fetching PeoplePersonRow objects using plain SQL queries */
  implicit def GetResultPeoplePersonRow(implicit e0: GR[Int], e1: GR[String], e2: GR[org.joda.time.LocalDateTime]): GR[PeoplePersonRow] = GR{
    prs => import prs._
    PeoplePersonRow.tupled((<<[Int], <<[String], <<[org.joda.time.LocalDateTime], <<[org.joda.time.LocalDateTime], <<[String]))
  }
  /** Table description of table people_person. Objects of this class serve as prototypes for rows in queries. */
  class PeoplePerson(_tableTag: Tag) extends Table[PeoplePersonRow](_tableTag, "people_person") {
    def * = (id, name, dateCreated, lastUpdated, personId) <> (PeoplePersonRow.tupled, PeoplePersonRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(name), Rep.Some(dateCreated), Rep.Some(lastUpdated), Rep.Some(personId)).shaped.<>({r=>import r._; _1.map(_=> PeoplePersonRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(serial), AutoInc, PrimaryKey */
    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    /** Database column name SqlType(varchar) */
    val name: Rep[String] = column[String]("name")
    /** Database column date_created SqlType(timestamp) */
    val dateCreated: Rep[org.joda.time.LocalDateTime] = column[org.joda.time.LocalDateTime]("date_created")
    /** Database column last_updated SqlType(timestamp) */
    val lastUpdated: Rep[org.joda.time.LocalDateTime] = column[org.joda.time.LocalDateTime]("last_updated")
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
   *  @param relationshipType Database column relationship_type SqlType(varchar), Length(100,true), Default(None)
   *  @param isCurrent Database column is_current SqlType(bool) */
  case class PeoplePersonlocationcrossrefRow(id: Int, dateCreated: org.joda.time.LocalDateTime, lastUpdated: org.joda.time.LocalDateTime, locationId: Int, personId: Int, relationshipType: Option[String] = None, isCurrent: Boolean)
  /** GetResult implicit for fetching PeoplePersonlocationcrossrefRow objects using plain SQL queries */
  implicit def GetResultPeoplePersonlocationcrossrefRow(implicit e0: GR[Int], e1: GR[org.joda.time.LocalDateTime], e2: GR[Option[String]], e3: GR[Boolean]): GR[PeoplePersonlocationcrossrefRow] = GR{
    prs => import prs._
    PeoplePersonlocationcrossrefRow.tupled((<<[Int], <<[org.joda.time.LocalDateTime], <<[org.joda.time.LocalDateTime], <<[Int], <<[Int], <<?[String], <<[Boolean]))
  }
  /** Table description of table people_personlocationcrossref. Objects of this class serve as prototypes for rows in queries. */
  class PeoplePersonlocationcrossref(_tableTag: Tag) extends Table[PeoplePersonlocationcrossrefRow](_tableTag, "people_personlocationcrossref") {
    def * = (id, dateCreated, lastUpdated, locationId, personId, relationshipType, isCurrent) <> (PeoplePersonlocationcrossrefRow.tupled, PeoplePersonlocationcrossrefRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(dateCreated), Rep.Some(lastUpdated), Rep.Some(locationId), Rep.Some(personId), relationshipType, Rep.Some(isCurrent)).shaped.<>({r=>import r._; _1.map(_=> PeoplePersonlocationcrossrefRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6, _7.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

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
    /** Database column relationship_type SqlType(varchar), Length(100,true), Default(None) */
    val relationshipType: Rep[Option[String]] = column[Option[String]]("relationship_type", O.Length(100,varying=true), O.Default(None))
    /** Database column is_current SqlType(bool) */
    val isCurrent: Rep[Boolean] = column[Boolean]("is_current")

    /** Foreign key referencing LocationsLocation (database name locations_locationpersoncrossref_location_id_fkey) */
    lazy val locationsLocationFk = foreignKey("locations_locationpersoncrossref_location_id_fkey", locationId, LocationsLocation)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
    /** Foreign key referencing PeoplePerson (database name person_id_refs_id) */
    lazy val peoplePersonFk = foreignKey("person_id_refs_id", personId, PeoplePerson)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
  }
  /** Collection-like TableQuery object for table PeoplePersonlocationcrossref */
  lazy val PeoplePersonlocationcrossref = new TableQuery(tag => new PeoplePersonlocationcrossref(tag))

  /** Entity class storing rows of table PeoplePersonorganisationcrossref
   *  @param id Database column id SqlType(serial), AutoInc, PrimaryKey
   *  @param dateCreated Database column date_created SqlType(timestamp)
   *  @param lastUpdated Database column last_updated SqlType(timestamp)
   *  @param personId Database column person_id SqlType(int4)
   *  @param organisationId Database column organisation_id SqlType(int4)
   *  @param relationshipType Database column relationship_type SqlType(varchar)
   *  @param isCurrent Database column is_current SqlType(bool) */
  case class PeoplePersonorganisationcrossrefRow(id: Int, dateCreated: org.joda.time.LocalDateTime, lastUpdated: org.joda.time.LocalDateTime, personId: Int, organisationId: Int, relationshipType: String, isCurrent: Boolean)
  /** GetResult implicit for fetching PeoplePersonorganisationcrossrefRow objects using plain SQL queries */
  implicit def GetResultPeoplePersonorganisationcrossrefRow(implicit e0: GR[Int], e1: GR[org.joda.time.LocalDateTime], e2: GR[String], e3: GR[Boolean]): GR[PeoplePersonorganisationcrossrefRow] = GR{
    prs => import prs._
    PeoplePersonorganisationcrossrefRow.tupled((<<[Int], <<[org.joda.time.LocalDateTime], <<[org.joda.time.LocalDateTime], <<[Int], <<[Int], <<[String], <<[Boolean]))
  }
  /** Table description of table people_personorganisationcrossref. Objects of this class serve as prototypes for rows in queries. */
  class PeoplePersonorganisationcrossref(_tableTag: Tag) extends Table[PeoplePersonorganisationcrossrefRow](_tableTag, "people_personorganisationcrossref") {
    def * = (id, dateCreated, lastUpdated, personId, organisationId, relationshipType, isCurrent) <> (PeoplePersonorganisationcrossrefRow.tupled, PeoplePersonorganisationcrossrefRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(dateCreated), Rep.Some(lastUpdated), Rep.Some(personId), Rep.Some(organisationId), Rep.Some(relationshipType), Rep.Some(isCurrent)).shaped.<>({r=>import r._; _1.map(_=> PeoplePersonorganisationcrossrefRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get, _7.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

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
    /** Database column relationship_type SqlType(varchar) */
    val relationshipType: Rep[String] = column[String]("relationship_type")
    /** Database column is_current SqlType(bool) */
    val isCurrent: Rep[Boolean] = column[Boolean]("is_current")

    /** Foreign key referencing OrganisationsOrganisation (database name organisation_id_refs_id_fk) */
    lazy val organisationsOrganisationFk = foreignKey("organisation_id_refs_id_fk", organisationId, OrganisationsOrganisation)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
    /** Foreign key referencing PeoplePerson (database name person_id_refs_id_fk) */
    lazy val peoplePersonFk = foreignKey("person_id_refs_id_fk", personId, PeoplePerson)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
  }
  /** Collection-like TableQuery object for table PeoplePersonorganisationcrossref */
  lazy val PeoplePersonorganisationcrossref = new TableQuery(tag => new PeoplePersonorganisationcrossref(tag))

  /** Entity class storing rows of table PeoplePersontopersoncrossref
   *  @param id Database column id SqlType(serial), AutoInc, PrimaryKey
   *  @param dateCreated Database column date_created SqlType(timestamp)
   *  @param lastUpdated Database column last_updated SqlType(timestamp)
   *  @param personOneId Database column person_one_id SqlType(int4)
   *  @param personTwoId Database column person_two_id SqlType(int4)
   *  @param relationshipTypeId Database column relationship_type_id SqlType(int4) */
  case class PeoplePersontopersoncrossrefRow(id: Int, dateCreated: org.joda.time.LocalDateTime, lastUpdated: org.joda.time.LocalDateTime, personOneId: Int, personTwoId: Int, relationshipTypeId: Int)
  /** GetResult implicit for fetching PeoplePersontopersoncrossrefRow objects using plain SQL queries */
  implicit def GetResultPeoplePersontopersoncrossrefRow(implicit e0: GR[Int], e1: GR[org.joda.time.LocalDateTime]): GR[PeoplePersontopersoncrossrefRow] = GR{
    prs => import prs._
    PeoplePersontopersoncrossrefRow.tupled((<<[Int], <<[org.joda.time.LocalDateTime], <<[org.joda.time.LocalDateTime], <<[Int], <<[Int], <<[Int]))
  }
  /** Table description of table people_persontopersoncrossref. Objects of this class serve as prototypes for rows in queries. */
  class PeoplePersontopersoncrossref(_tableTag: Tag) extends Table[PeoplePersontopersoncrossrefRow](_tableTag, "people_persontopersoncrossref") {
    def * = (id, dateCreated, lastUpdated, personOneId, personTwoId, relationshipTypeId) <> (PeoplePersontopersoncrossrefRow.tupled, PeoplePersontopersoncrossrefRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(dateCreated), Rep.Some(lastUpdated), Rep.Some(personOneId), Rep.Some(personTwoId), Rep.Some(relationshipTypeId)).shaped.<>({r=>import r._; _1.map(_=> PeoplePersontopersoncrossrefRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

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

    /** Foreign key referencing PeoplePerson (database name people_persontopersoncrossref_person_one_id_fkey) */
    lazy val peoplePersonFk1 = foreignKey("people_persontopersoncrossref_person_one_id_fkey", personOneId, PeoplePerson)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
    /** Foreign key referencing PeoplePerson (database name people_persontopersoncrossref_person_two_id_fkey) */
    lazy val peoplePersonFk2 = foreignKey("people_persontopersoncrossref_person_two_id_fkey", personTwoId, PeoplePerson)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
    /** Foreign key referencing PeoplePersontopersonrelationshiptype (database name relationship_type_id_refs_id_fk) */
    lazy val peoplePersontopersonrelationshiptypeFk = foreignKey("relationship_type_id_refs_id_fk", relationshipTypeId, PeoplePersontopersonrelationshiptype)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
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
   *  @param relationshipType Database column relationship_type SqlType(varchar), Length(100,true), Default(None)
   *  @param isCurrent Database column is_current SqlType(bool) */
  case class PeopleSystempropertydynamiccrossrefRow(id: Int, dateCreated: org.joda.time.LocalDateTime, lastUpdated: org.joda.time.LocalDateTime, personId: Int, systemPropertyId: Int, fieldId: Int, relationshipType: Option[String] = None, isCurrent: Boolean)
  /** GetResult implicit for fetching PeopleSystempropertydynamiccrossrefRow objects using plain SQL queries */
  implicit def GetResultPeopleSystempropertydynamiccrossrefRow(implicit e0: GR[Int], e1: GR[org.joda.time.LocalDateTime], e2: GR[Option[String]], e3: GR[Boolean]): GR[PeopleSystempropertydynamiccrossrefRow] = GR{
    prs => import prs._
    PeopleSystempropertydynamiccrossrefRow.tupled((<<[Int], <<[org.joda.time.LocalDateTime], <<[org.joda.time.LocalDateTime], <<[Int], <<[Int], <<[Int], <<?[String], <<[Boolean]))
  }
  /** Table description of table people_systempropertydynamiccrossref. Objects of this class serve as prototypes for rows in queries. */
  class PeopleSystempropertydynamiccrossref(_tableTag: Tag) extends Table[PeopleSystempropertydynamiccrossrefRow](_tableTag, "people_systempropertydynamiccrossref") {
    def * = (id, dateCreated, lastUpdated, personId, systemPropertyId, fieldId, relationshipType, isCurrent) <> (PeopleSystempropertydynamiccrossrefRow.tupled, PeopleSystempropertydynamiccrossrefRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(dateCreated), Rep.Some(lastUpdated), Rep.Some(personId), Rep.Some(systemPropertyId), Rep.Some(fieldId), relationshipType, Rep.Some(isCurrent)).shaped.<>({r=>import r._; _1.map(_=> PeopleSystempropertydynamiccrossrefRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get, _7, _8.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

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
    /** Database column relationship_type SqlType(varchar), Length(100,true), Default(None) */
    val relationshipType: Rep[Option[String]] = column[Option[String]]("relationship_type", O.Length(100,varying=true), O.Default(None))
    /** Database column is_current SqlType(bool) */
    val isCurrent: Rep[Boolean] = column[Boolean]("is_current")

    /** Foreign key referencing DataField (database name data_field_people_systempropertydynamiccrossref_fk) */
    lazy val dataFieldFk = foreignKey("data_field_people_systempropertydynamiccrossref_fk", fieldId, DataField)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
    /** Foreign key referencing PeoplePerson (database name people_person_people_systempropertydynamiccrossref_fk) */
    lazy val peoplePersonFk = foreignKey("people_person_people_systempropertydynamiccrossref_fk", personId, PeoplePerson)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
    /** Foreign key referencing SystemProperty (database name system_property_people_systempropertydynamiccrossref_fk) */
    lazy val systemPropertyFk = foreignKey("system_property_people_systempropertydynamiccrossref_fk", systemPropertyId, SystemProperty)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
  }
  /** Collection-like TableQuery object for table PeopleSystempropertydynamiccrossref */
  lazy val PeopleSystempropertydynamiccrossref = new TableQuery(tag => new PeopleSystempropertydynamiccrossref(tag))

  /** Entity class storing rows of table PeopleSystempropertystaticcrossref
   *  @param id Database column id SqlType(int4), PrimaryKey
   *  @param dateCreated Database column date_created SqlType(timestamp)
   *  @param lastUpdated Database column last_updated SqlType(timestamp)
   *  @param personId Database column person_id SqlType(int4)
   *  @param systemPropertyId Database column system_property_id SqlType(int4)
   *  @param recordId Database column record_id SqlType(int4)
   *  @param fieldId Database column field_id SqlType(int4)
   *  @param relationshipType Database column relationship_type SqlType(varchar), Length(100,true), Default(None)
   *  @param isCurrent Database column is_current SqlType(bool) */
  case class PeopleSystempropertystaticcrossrefRow(id: Int, dateCreated: org.joda.time.LocalDateTime, lastUpdated: org.joda.time.LocalDateTime, personId: Int, systemPropertyId: Int, recordId: Int, fieldId: Int, relationshipType: Option[String] = None, isCurrent: Boolean)
  /** GetResult implicit for fetching PeopleSystempropertystaticcrossrefRow objects using plain SQL queries */
  implicit def GetResultPeopleSystempropertystaticcrossrefRow(implicit e0: GR[Int], e1: GR[org.joda.time.LocalDateTime], e2: GR[Option[String]], e3: GR[Boolean]): GR[PeopleSystempropertystaticcrossrefRow] = GR{
    prs => import prs._
    PeopleSystempropertystaticcrossrefRow.tupled((<<[Int], <<[org.joda.time.LocalDateTime], <<[org.joda.time.LocalDateTime], <<[Int], <<[Int], <<[Int], <<[Int], <<?[String], <<[Boolean]))
  }
  /** Table description of table people_systempropertystaticcrossref. Objects of this class serve as prototypes for rows in queries. */
  class PeopleSystempropertystaticcrossref(_tableTag: Tag) extends Table[PeopleSystempropertystaticcrossrefRow](_tableTag, "people_systempropertystaticcrossref") {
    def * = (id, dateCreated, lastUpdated, personId, systemPropertyId, recordId, fieldId, relationshipType, isCurrent) <> (PeopleSystempropertystaticcrossrefRow.tupled, PeopleSystempropertystaticcrossrefRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(dateCreated), Rep.Some(lastUpdated), Rep.Some(personId), Rep.Some(systemPropertyId), Rep.Some(recordId), Rep.Some(fieldId), relationshipType, Rep.Some(isCurrent)).shaped.<>({r=>import r._; _1.map(_=> PeopleSystempropertystaticcrossrefRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get, _7.get, _8, _9.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(int4), PrimaryKey */
    val id: Rep[Int] = column[Int]("id", O.PrimaryKey)
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
    /** Database column relationship_type SqlType(varchar), Length(100,true), Default(None) */
    val relationshipType: Rep[Option[String]] = column[Option[String]]("relationship_type", O.Length(100,varying=true), O.Default(None))
    /** Database column is_current SqlType(bool) */
    val isCurrent: Rep[Boolean] = column[Boolean]("is_current")

    /** Foreign key referencing DataField (database name data_field_people_systempropertystaticcrossref_fk) */
    lazy val dataFieldFk = foreignKey("data_field_people_systempropertystaticcrossref_fk", fieldId, DataField)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
    /** Foreign key referencing DataRecord (database name data_record_people_systempropertystaticcrossref_fk) */
    lazy val dataRecordFk = foreignKey("data_record_people_systempropertystaticcrossref_fk", recordId, DataRecord)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
    /** Foreign key referencing PeoplePerson (database name people_person_people_systempropertystaticcrossref_fk) */
    lazy val peoplePersonFk = foreignKey("people_person_people_systempropertystaticcrossref_fk", personId, PeoplePerson)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
    /** Foreign key referencing SystemProperty (database name system_property_people_systempropertystaticcrossref_fk) */
    lazy val systemPropertyFk = foreignKey("system_property_people_systempropertystaticcrossref_fk", systemPropertyId, SystemProperty)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
  }
  /** Collection-like TableQuery object for table PeopleSystempropertystaticcrossref */
  lazy val PeopleSystempropertystaticcrossref = new TableQuery(tag => new PeopleSystempropertystaticcrossref(tag))

  /** Entity class storing rows of table PeopleSystemtypecrossref
   *  @param id Database column id SqlType(serial), AutoInc, PrimaryKey
   *  @param dateCreated Database column date_created SqlType(timestamp)
   *  @param lastUpdated Database column last_updated SqlType(timestamp)
   *  @param usersId Database column users_id SqlType(int4)
   *  @param systemTypeId Database column system_type_id SqlType(int4)
   *  @param relationshipType Database column relationship_type SqlType(varchar), Length(100,true), Default(None)
   *  @param isCurrent Database column is_current SqlType(bool) */
  case class PeopleSystemtypecrossrefRow(id: Int, dateCreated: org.joda.time.LocalDateTime, lastUpdated: org.joda.time.LocalDateTime, usersId: Int, systemTypeId: Int, relationshipType: Option[String] = None, isCurrent: Boolean)
  /** GetResult implicit for fetching PeopleSystemtypecrossrefRow objects using plain SQL queries */
  implicit def GetResultPeopleSystemtypecrossrefRow(implicit e0: GR[Int], e1: GR[org.joda.time.LocalDateTime], e2: GR[Option[String]], e3: GR[Boolean]): GR[PeopleSystemtypecrossrefRow] = GR{
    prs => import prs._
    PeopleSystemtypecrossrefRow.tupled((<<[Int], <<[org.joda.time.LocalDateTime], <<[org.joda.time.LocalDateTime], <<[Int], <<[Int], <<?[String], <<[Boolean]))
  }
  /** Table description of table people_systemtypecrossref. Objects of this class serve as prototypes for rows in queries. */
  class PeopleSystemtypecrossref(_tableTag: Tag) extends Table[PeopleSystemtypecrossrefRow](_tableTag, "people_systemtypecrossref") {
    def * = (id, dateCreated, lastUpdated, usersId, systemTypeId, relationshipType, isCurrent) <> (PeopleSystemtypecrossrefRow.tupled, PeopleSystemtypecrossrefRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(dateCreated), Rep.Some(lastUpdated), Rep.Some(usersId), Rep.Some(systemTypeId), relationshipType, Rep.Some(isCurrent)).shaped.<>({r=>import r._; _1.map(_=> PeopleSystemtypecrossrefRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6, _7.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

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
    /** Database column relationship_type SqlType(varchar), Length(100,true), Default(None) */
    val relationshipType: Rep[Option[String]] = column[Option[String]]("relationship_type", O.Length(100,varying=true), O.Default(None))
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
   *  @param description Database column description SqlType(text) */
  case class SystemPropertyRow(id: Int, dateCreated: org.joda.time.LocalDateTime, lastUpdated: org.joda.time.LocalDateTime, name: String, description: String)
  /** GetResult implicit for fetching SystemPropertyRow objects using plain SQL queries */
  implicit def GetResultSystemPropertyRow(implicit e0: GR[Int], e1: GR[org.joda.time.LocalDateTime], e2: GR[String]): GR[SystemPropertyRow] = GR{
    prs => import prs._
    SystemPropertyRow.tupled((<<[Int], <<[org.joda.time.LocalDateTime], <<[org.joda.time.LocalDateTime], <<[String], <<[String]))
  }
  /** Table description of table system_property. Objects of this class serve as prototypes for rows in queries. */
  class SystemProperty(_tableTag: Tag) extends Table[SystemPropertyRow](_tableTag, "system_property") {
    def * = (id, dateCreated, lastUpdated, name, description) <> (SystemPropertyRow.tupled, SystemPropertyRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(dateCreated), Rep.Some(lastUpdated), Rep.Some(name), Rep.Some(description)).shaped.<>({r=>import r._; _1.map(_=> SystemPropertyRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(serial), AutoInc, PrimaryKey */
    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    /** Database column date_created SqlType(timestamp) */
    val dateCreated: Rep[org.joda.time.LocalDateTime] = column[org.joda.time.LocalDateTime]("date_created")
    /** Database column last_updated SqlType(timestamp) */
    val lastUpdated: Rep[org.joda.time.LocalDateTime] = column[org.joda.time.LocalDateTime]("last_updated")
    /** Database column name SqlType(varchar) */
    val name: Rep[String] = column[String]("name")
    /** Database column description SqlType(text) */
    val description: Rep[String] = column[String]("description")
  }
  /** Collection-like TableQuery object for table SystemProperty */
  lazy val SystemProperty = new TableQuery(tag => new SystemProperty(tag))

  /** Entity class storing rows of table SystemPropertytypecrossref
   *  @param id Database column id SqlType(serial), AutoInc, PrimaryKey
   *  @param dateCreated Database column date_created SqlType(timestamp)
   *  @param lastUpdated Database column last_updated SqlType(timestamp)
   *  @param typeId Database column type_id SqlType(int4)
   *  @param propertyId Database column property_id SqlType(int4)
   *  @param relationshipType Database column relationship_type SqlType(varchar), Length(100,true), Default(None)
   *  @param isCurrent Database column is_current SqlType(bool) */
  case class SystemPropertytypecrossrefRow(id: Int, dateCreated: org.joda.time.LocalDateTime, lastUpdated: org.joda.time.LocalDateTime, typeId: Int, propertyId: Int, relationshipType: Option[String] = None, isCurrent: Boolean)
  /** GetResult implicit for fetching SystemPropertytypecrossrefRow objects using plain SQL queries */
  implicit def GetResultSystemPropertytypecrossrefRow(implicit e0: GR[Int], e1: GR[org.joda.time.LocalDateTime], e2: GR[Option[String]], e3: GR[Boolean]): GR[SystemPropertytypecrossrefRow] = GR{
    prs => import prs._
    SystemPropertytypecrossrefRow.tupled((<<[Int], <<[org.joda.time.LocalDateTime], <<[org.joda.time.LocalDateTime], <<[Int], <<[Int], <<?[String], <<[Boolean]))
  }
  /** Table description of table system_propertytypecrossref. Objects of this class serve as prototypes for rows in queries. */
  class SystemPropertytypecrossref(_tableTag: Tag) extends Table[SystemPropertytypecrossrefRow](_tableTag, "system_propertytypecrossref") {
    def * = (id, dateCreated, lastUpdated, typeId, propertyId, relationshipType, isCurrent) <> (SystemPropertytypecrossrefRow.tupled, SystemPropertytypecrossrefRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(dateCreated), Rep.Some(lastUpdated), Rep.Some(typeId), Rep.Some(propertyId), relationshipType, Rep.Some(isCurrent)).shaped.<>({r=>import r._; _1.map(_=> SystemPropertytypecrossrefRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6, _7.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(serial), AutoInc, PrimaryKey */
    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    /** Database column date_created SqlType(timestamp) */
    val dateCreated: Rep[org.joda.time.LocalDateTime] = column[org.joda.time.LocalDateTime]("date_created")
    /** Database column last_updated SqlType(timestamp) */
    val lastUpdated: Rep[org.joda.time.LocalDateTime] = column[org.joda.time.LocalDateTime]("last_updated")
    /** Database column type_id SqlType(int4) */
    val typeId: Rep[Int] = column[Int]("type_id")
    /** Database column property_id SqlType(int4) */
    val propertyId: Rep[Int] = column[Int]("property_id")
    /** Database column relationship_type SqlType(varchar), Length(100,true), Default(None) */
    val relationshipType: Rep[Option[String]] = column[Option[String]]("relationship_type", O.Length(100,varying=true), O.Default(None))
    /** Database column is_current SqlType(bool) */
    val isCurrent: Rep[Boolean] = column[Boolean]("is_current")

    /** Foreign key referencing SystemProperty (database name system_propertytotypecrossref_fk) */
    lazy val systemPropertyFk = foreignKey("system_propertytotypecrossref_fk", propertyId, SystemProperty)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
    /** Foreign key referencing SystemType (database name system_typetopropertycrossref_fk) */
    lazy val systemTypeFk = foreignKey("system_typetopropertycrossref_fk", typeId, SystemType)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
  }
  /** Collection-like TableQuery object for table SystemPropertytypecrossref */
  lazy val SystemPropertytypecrossref = new TableQuery(tag => new SystemPropertytypecrossref(tag))

  /** Entity class storing rows of table SystemPropertyuomcrossref
   *  @param id Database column id SqlType(int4), PrimaryKey
   *  @param dateCreated Database column date_created SqlType(timestamp)
   *  @param lastUpdated Database column last_updated SqlType(timestamp)
   *  @param unitofmeasurementId Database column unitofmeasurement_id SqlType(int4)
   *  @param propertyId Database column property_id SqlType(int4)
   *  @param relationshipType Database column relationship_type SqlType(varchar)
   *  @param isCurrent Database column is_current SqlType(bool) */
  case class SystemPropertyuomcrossrefRow(id: Int, dateCreated: org.joda.time.LocalDateTime, lastUpdated: org.joda.time.LocalDateTime, unitofmeasurementId: Int, propertyId: Int, relationshipType: String, isCurrent: Boolean)
  /** GetResult implicit for fetching SystemPropertyuomcrossrefRow objects using plain SQL queries */
  implicit def GetResultSystemPropertyuomcrossrefRow(implicit e0: GR[Int], e1: GR[org.joda.time.LocalDateTime], e2: GR[String], e3: GR[Boolean]): GR[SystemPropertyuomcrossrefRow] = GR{
    prs => import prs._
    SystemPropertyuomcrossrefRow.tupled((<<[Int], <<[org.joda.time.LocalDateTime], <<[org.joda.time.LocalDateTime], <<[Int], <<[Int], <<[String], <<[Boolean]))
  }
  /** Table description of table system_propertyuomcrossref. Objects of this class serve as prototypes for rows in queries. */
  class SystemPropertyuomcrossref(_tableTag: Tag) extends Table[SystemPropertyuomcrossrefRow](_tableTag, "system_propertyuomcrossref") {
    def * = (id, dateCreated, lastUpdated, unitofmeasurementId, propertyId, relationshipType, isCurrent) <> (SystemPropertyuomcrossrefRow.tupled, SystemPropertyuomcrossrefRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(dateCreated), Rep.Some(lastUpdated), Rep.Some(unitofmeasurementId), Rep.Some(propertyId), Rep.Some(relationshipType), Rep.Some(isCurrent)).shaped.<>({r=>import r._; _1.map(_=> SystemPropertyuomcrossrefRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get, _7.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(int4), PrimaryKey */
    val id: Rep[Int] = column[Int]("id", O.PrimaryKey)
    /** Database column date_created SqlType(timestamp) */
    val dateCreated: Rep[org.joda.time.LocalDateTime] = column[org.joda.time.LocalDateTime]("date_created")
    /** Database column last_updated SqlType(timestamp) */
    val lastUpdated: Rep[org.joda.time.LocalDateTime] = column[org.joda.time.LocalDateTime]("last_updated")
    /** Database column unitofmeasurement_id SqlType(int4) */
    val unitofmeasurementId: Rep[Int] = column[Int]("unitofmeasurement_id")
    /** Database column property_id SqlType(int4) */
    val propertyId: Rep[Int] = column[Int]("property_id")
    /** Database column relationship_type SqlType(varchar) */
    val relationshipType: Rep[String] = column[String]("relationship_type")
    /** Database column is_current SqlType(bool) */
    val isCurrent: Rep[Boolean] = column[Boolean]("is_current")

    /** Foreign key referencing SystemProperty (database name system_property_system_propertyuomcrossref_fk) */
    lazy val systemPropertyFk = foreignKey("system_property_system_propertyuomcrossref_fk", propertyId, SystemProperty)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
    /** Foreign key referencing SystemUnitofmeasurement (database name system_unitofmeasurement_system_propertyuomcrossref_fk) */
    lazy val systemUnitofmeasurementFk = foreignKey("system_unitofmeasurement_system_propertyuomcrossref_fk", unitofmeasurementId, SystemUnitofmeasurement)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
  }
  /** Collection-like TableQuery object for table SystemPropertyuomcrossref */
  lazy val SystemPropertyuomcrossref = new TableQuery(tag => new SystemPropertyuomcrossref(tag))

  /** Entity class storing rows of table SystemType
   *  @param id Database column id SqlType(serial), AutoInc, PrimaryKey
   *  @param dateCreated Database column date_created SqlType(timestamp)
   *  @param lastUpdated Database column last_updated SqlType(timestamp)
   *  @param name Database column name SqlType(varchar)
   *  @param description Database column description SqlType(text) */
  case class SystemTypeRow(id: Int, dateCreated: org.joda.time.LocalDateTime, lastUpdated: org.joda.time.LocalDateTime, name: String, description: String)
  /** GetResult implicit for fetching SystemTypeRow objects using plain SQL queries */
  implicit def GetResultSystemTypeRow(implicit e0: GR[Int], e1: GR[org.joda.time.LocalDateTime], e2: GR[String]): GR[SystemTypeRow] = GR{
    prs => import prs._
    SystemTypeRow.tupled((<<[Int], <<[org.joda.time.LocalDateTime], <<[org.joda.time.LocalDateTime], <<[String], <<[String]))
  }
  /** Table description of table system_type. Objects of this class serve as prototypes for rows in queries. */
  class SystemType(_tableTag: Tag) extends Table[SystemTypeRow](_tableTag, "system_type") {
    def * = (id, dateCreated, lastUpdated, name, description) <> (SystemTypeRow.tupled, SystemTypeRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(dateCreated), Rep.Some(lastUpdated), Rep.Some(name), Rep.Some(description)).shaped.<>({r=>import r._; _1.map(_=> SystemTypeRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(serial), AutoInc, PrimaryKey */
    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    /** Database column date_created SqlType(timestamp) */
    val dateCreated: Rep[org.joda.time.LocalDateTime] = column[org.joda.time.LocalDateTime]("date_created")
    /** Database column last_updated SqlType(timestamp) */
    val lastUpdated: Rep[org.joda.time.LocalDateTime] = column[org.joda.time.LocalDateTime]("last_updated")
    /** Database column name SqlType(varchar) */
    val name: Rep[String] = column[String]("name")
    /** Database column description SqlType(text) */
    val description: Rep[String] = column[String]("description")
  }
  /** Collection-like TableQuery object for table SystemType */
  lazy val SystemType = new TableQuery(tag => new SystemType(tag))

  /** Entity class storing rows of table SystemTypetotypecrossref
   *  @param id Database column id SqlType(serial), AutoInc, PrimaryKey
   *  @param dateCreated Database column date_created SqlType(timestamp)
   *  @param lastUpdated Database column last_updated SqlType(timestamp)
   *  @param typeOneId Database column type_one_id SqlType(int4)
   *  @param typeTwoId Database column type_two_id SqlType(int4)
   *  @param relationshipDescription Database column relationship_description SqlType(varchar), Length(100,true), Default(None) */
  case class SystemTypetotypecrossrefRow(id: Int, dateCreated: org.joda.time.LocalDateTime, lastUpdated: org.joda.time.LocalDateTime, typeOneId: Int, typeTwoId: Int, relationshipDescription: Option[String] = None)
  /** GetResult implicit for fetching SystemTypetotypecrossrefRow objects using plain SQL queries */
  implicit def GetResultSystemTypetotypecrossrefRow(implicit e0: GR[Int], e1: GR[org.joda.time.LocalDateTime], e2: GR[Option[String]]): GR[SystemTypetotypecrossrefRow] = GR{
    prs => import prs._
    SystemTypetotypecrossrefRow.tupled((<<[Int], <<[org.joda.time.LocalDateTime], <<[org.joda.time.LocalDateTime], <<[Int], <<[Int], <<?[String]))
  }
  /** Table description of table system_typetotypecrossref. Objects of this class serve as prototypes for rows in queries. */
  class SystemTypetotypecrossref(_tableTag: Tag) extends Table[SystemTypetotypecrossrefRow](_tableTag, "system_typetotypecrossref") {
    def * = (id, dateCreated, lastUpdated, typeOneId, typeTwoId, relationshipDescription) <> (SystemTypetotypecrossrefRow.tupled, SystemTypetotypecrossrefRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(dateCreated), Rep.Some(lastUpdated), Rep.Some(typeOneId), Rep.Some(typeTwoId), relationshipDescription).shaped.<>({r=>import r._; _1.map(_=> SystemTypetotypecrossrefRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

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
    /** Database column relationship_description SqlType(varchar), Length(100,true), Default(None) */
    val relationshipDescription: Rep[Option[String]] = column[Option[String]]("relationship_description", O.Length(100,varying=true), O.Default(None))

    /** Foreign key referencing SystemType (database name system_type_system_typetotypecrossref_fk) */
    lazy val systemTypeFk1 = foreignKey("system_type_system_typetotypecrossref_fk", typeOneId, SystemType)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
    /** Foreign key referencing SystemType (database name system_type_system_typetotypecrossref_fk1) */
    lazy val systemTypeFk2 = foreignKey("system_type_system_typetotypecrossref_fk1", typeTwoId, SystemType)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
  }
  /** Collection-like TableQuery object for table SystemTypetotypecrossref */
  lazy val SystemTypetotypecrossref = new TableQuery(tag => new SystemTypetotypecrossref(tag))

  /** Entity class storing rows of table SystemUnitofmeasurement
   *  @param id Database column id SqlType(int4), PrimaryKey
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

    /** Database column id SqlType(int4), PrimaryKey */
    val id: Rep[Int] = column[Int]("id", O.PrimaryKey)
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
   *  @param relationshipType Database column relationship_type SqlType(varchar), Length(100,true), Default(None)
   *  @param isCurrent Database column is_current SqlType(bool) */
  case class ThingsSystempropertydynamiccrossrefRow(id: Int, dateCreated: org.joda.time.LocalDateTime, lastUpdated: org.joda.time.LocalDateTime, thingId: Int, systemPropertyId: Int, fieldId: Int, relationshipType: Option[String] = None, isCurrent: Boolean)
  /** GetResult implicit for fetching ThingsSystempropertydynamiccrossrefRow objects using plain SQL queries */
  implicit def GetResultThingsSystempropertydynamiccrossrefRow(implicit e0: GR[Int], e1: GR[org.joda.time.LocalDateTime], e2: GR[Option[String]], e3: GR[Boolean]): GR[ThingsSystempropertydynamiccrossrefRow] = GR{
    prs => import prs._
    ThingsSystempropertydynamiccrossrefRow.tupled((<<[Int], <<[org.joda.time.LocalDateTime], <<[org.joda.time.LocalDateTime], <<[Int], <<[Int], <<[Int], <<?[String], <<[Boolean]))
  }
  /** Table description of table things_systempropertydynamiccrossref. Objects of this class serve as prototypes for rows in queries. */
  class ThingsSystempropertydynamiccrossref(_tableTag: Tag) extends Table[ThingsSystempropertydynamiccrossrefRow](_tableTag, "things_systempropertydynamiccrossref") {
    def * = (id, dateCreated, lastUpdated, thingId, systemPropertyId, fieldId, relationshipType, isCurrent) <> (ThingsSystempropertydynamiccrossrefRow.tupled, ThingsSystempropertydynamiccrossrefRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(dateCreated), Rep.Some(lastUpdated), Rep.Some(thingId), Rep.Some(systemPropertyId), Rep.Some(fieldId), relationshipType, Rep.Some(isCurrent)).shaped.<>({r=>import r._; _1.map(_=> ThingsSystempropertydynamiccrossrefRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get, _7, _8.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

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
    /** Database column relationship_type SqlType(varchar), Length(100,true), Default(None) */
    val relationshipType: Rep[Option[String]] = column[Option[String]]("relationship_type", O.Length(100,varying=true), O.Default(None))
    /** Database column is_current SqlType(bool) */
    val isCurrent: Rep[Boolean] = column[Boolean]("is_current")

    /** Foreign key referencing DataField (database name data_field_things_systempropertydynamiccrossref_fk) */
    lazy val dataFieldFk = foreignKey("data_field_things_systempropertydynamiccrossref_fk", fieldId, DataField)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
    /** Foreign key referencing SystemProperty (database name system_property_things_systempropertydynamiccrossref_fk) */
    lazy val systemPropertyFk = foreignKey("system_property_things_systempropertydynamiccrossref_fk", systemPropertyId, SystemProperty)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
    /** Foreign key referencing ThingsThing (database name things_systempropertydynamiccrossref_fk) */
    lazy val thingsThingFk = foreignKey("things_systempropertydynamiccrossref_fk", thingId, ThingsThing)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
  }
  /** Collection-like TableQuery object for table ThingsSystempropertydynamiccrossref */
  lazy val ThingsSystempropertydynamiccrossref = new TableQuery(tag => new ThingsSystempropertydynamiccrossref(tag))

  /** Entity class storing rows of table ThingsSystempropertystaticcrossref
   *  @param id Database column id SqlType(int4), PrimaryKey
   *  @param dateCreated Database column date_created SqlType(timestamp)
   *  @param lastUpdated Database column last_updated SqlType(timestamp)
   *  @param thingId Database column thing_id SqlType(int4)
   *  @param systemPropertyId Database column system_property_id SqlType(int4)
   *  @param fieldId Database column field_id SqlType(int4)
   *  @param recordId Database column record_id SqlType(int4)
   *  @param relationshipType Database column relationship_type SqlType(varchar), Length(100,true), Default(None)
   *  @param isCurrent Database column is_current SqlType(bool) */
  case class ThingsSystempropertystaticcrossrefRow(id: Int, dateCreated: org.joda.time.LocalDateTime, lastUpdated: org.joda.time.LocalDateTime, thingId: Int, systemPropertyId: Int, fieldId: Int, recordId: Int, relationshipType: Option[String] = None, isCurrent: Boolean)
  /** GetResult implicit for fetching ThingsSystempropertystaticcrossrefRow objects using plain SQL queries */
  implicit def GetResultThingsSystempropertystaticcrossrefRow(implicit e0: GR[Int], e1: GR[org.joda.time.LocalDateTime], e2: GR[Option[String]], e3: GR[Boolean]): GR[ThingsSystempropertystaticcrossrefRow] = GR{
    prs => import prs._
    ThingsSystempropertystaticcrossrefRow.tupled((<<[Int], <<[org.joda.time.LocalDateTime], <<[org.joda.time.LocalDateTime], <<[Int], <<[Int], <<[Int], <<[Int], <<?[String], <<[Boolean]))
  }
  /** Table description of table things_systempropertystaticcrossref. Objects of this class serve as prototypes for rows in queries. */
  class ThingsSystempropertystaticcrossref(_tableTag: Tag) extends Table[ThingsSystempropertystaticcrossrefRow](_tableTag, "things_systempropertystaticcrossref") {
    def * = (id, dateCreated, lastUpdated, thingId, systemPropertyId, fieldId, recordId, relationshipType, isCurrent) <> (ThingsSystempropertystaticcrossrefRow.tupled, ThingsSystempropertystaticcrossrefRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(dateCreated), Rep.Some(lastUpdated), Rep.Some(thingId), Rep.Some(systemPropertyId), Rep.Some(fieldId), Rep.Some(recordId), relationshipType, Rep.Some(isCurrent)).shaped.<>({r=>import r._; _1.map(_=> ThingsSystempropertystaticcrossrefRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get, _7.get, _8, _9.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(int4), PrimaryKey */
    val id: Rep[Int] = column[Int]("id", O.PrimaryKey)
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
    /** Database column relationship_type SqlType(varchar), Length(100,true), Default(None) */
    val relationshipType: Rep[Option[String]] = column[Option[String]]("relationship_type", O.Length(100,varying=true), O.Default(None))
    /** Database column is_current SqlType(bool) */
    val isCurrent: Rep[Boolean] = column[Boolean]("is_current")

    /** Foreign key referencing DataField (database name data_field_things_systempropertystaticcrossref_fk) */
    lazy val dataFieldFk = foreignKey("data_field_things_systempropertystaticcrossref_fk", fieldId, DataField)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
    /** Foreign key referencing DataRecord (database name data_record_things_systempropertycrossref_fk) */
    lazy val dataRecordFk = foreignKey("data_record_things_systempropertycrossref_fk", recordId, DataRecord)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
    /** Foreign key referencing SystemProperty (database name thing_property_id_refs_id_fk) */
    lazy val systemPropertyFk = foreignKey("thing_property_id_refs_id_fk", systemPropertyId, SystemProperty)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
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
   *  @param relationshipType Database column relationship_type SqlType(varchar), Length(100,true), Default(None)
   *  @param isCurrent Database column is_current SqlType(bool) */
  case class ThingsSystemtypecrossrefRow(id: Int, dateCreated: org.joda.time.LocalDateTime, lastUpdated: org.joda.time.LocalDateTime, thingId: Int, systemTypeId: Int, relationshipType: Option[String] = None, isCurrent: Boolean)
  /** GetResult implicit for fetching ThingsSystemtypecrossrefRow objects using plain SQL queries */
  implicit def GetResultThingsSystemtypecrossrefRow(implicit e0: GR[Int], e1: GR[org.joda.time.LocalDateTime], e2: GR[Option[String]], e3: GR[Boolean]): GR[ThingsSystemtypecrossrefRow] = GR{
    prs => import prs._
    ThingsSystemtypecrossrefRow.tupled((<<[Int], <<[org.joda.time.LocalDateTime], <<[org.joda.time.LocalDateTime], <<[Int], <<[Int], <<?[String], <<[Boolean]))
  }
  /** Table description of table things_systemtypecrossref. Objects of this class serve as prototypes for rows in queries. */
  class ThingsSystemtypecrossref(_tableTag: Tag) extends Table[ThingsSystemtypecrossrefRow](_tableTag, "things_systemtypecrossref") {
    def * = (id, dateCreated, lastUpdated, thingId, systemTypeId, relationshipType, isCurrent) <> (ThingsSystemtypecrossrefRow.tupled, ThingsSystemtypecrossrefRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(dateCreated), Rep.Some(lastUpdated), Rep.Some(thingId), Rep.Some(systemTypeId), relationshipType, Rep.Some(isCurrent)).shaped.<>({r=>import r._; _1.map(_=> ThingsSystemtypecrossrefRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6, _7.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

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
    /** Database column relationship_type SqlType(varchar), Length(100,true), Default(None) */
    val relationshipType: Rep[Option[String]] = column[Option[String]]("relationship_type", O.Length(100,varying=true), O.Default(None))
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
   *  @param description Database column description SqlType(text), Default(None)
   *  @param personId Database column person_id SqlType(int4)
   *  @param thingId Database column thing_id SqlType(int4)
   *  @param relationshipType Database column relationship_type SqlType(varchar), Length(100,true), Default(None)
   *  @param isCurrent Database column is_current SqlType(bool) */
  case class ThingsThingpersoncrossrefRow(id: Int, dateCreated: org.joda.time.LocalDateTime, lastUpdated: org.joda.time.LocalDateTime, description: Option[String] = None, personId: Int, thingId: Int, relationshipType: Option[String] = None, isCurrent: Boolean)
  /** GetResult implicit for fetching ThingsThingpersoncrossrefRow objects using plain SQL queries */
  implicit def GetResultThingsThingpersoncrossrefRow(implicit e0: GR[Int], e1: GR[org.joda.time.LocalDateTime], e2: GR[Option[String]], e3: GR[Boolean]): GR[ThingsThingpersoncrossrefRow] = GR{
    prs => import prs._
    ThingsThingpersoncrossrefRow.tupled((<<[Int], <<[org.joda.time.LocalDateTime], <<[org.joda.time.LocalDateTime], <<?[String], <<[Int], <<[Int], <<?[String], <<[Boolean]))
  }
  /** Table description of table things_thingpersoncrossref. Objects of this class serve as prototypes for rows in queries. */
  class ThingsThingpersoncrossref(_tableTag: Tag) extends Table[ThingsThingpersoncrossrefRow](_tableTag, "things_thingpersoncrossref") {
    def * = (id, dateCreated, lastUpdated, description, personId, thingId, relationshipType, isCurrent) <> (ThingsThingpersoncrossrefRow.tupled, ThingsThingpersoncrossrefRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(dateCreated), Rep.Some(lastUpdated), description, Rep.Some(personId), Rep.Some(thingId), relationshipType, Rep.Some(isCurrent)).shaped.<>({r=>import r._; _1.map(_=> ThingsThingpersoncrossrefRow.tupled((_1.get, _2.get, _3.get, _4, _5.get, _6.get, _7, _8.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(serial), AutoInc, PrimaryKey */
    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    /** Database column date_created SqlType(timestamp) */
    val dateCreated: Rep[org.joda.time.LocalDateTime] = column[org.joda.time.LocalDateTime]("date_created")
    /** Database column last_updated SqlType(timestamp) */
    val lastUpdated: Rep[org.joda.time.LocalDateTime] = column[org.joda.time.LocalDateTime]("last_updated")
    /** Database column description SqlType(text), Default(None) */
    val description: Rep[Option[String]] = column[Option[String]]("description", O.Default(None))
    /** Database column person_id SqlType(int4) */
    val personId: Rep[Int] = column[Int]("person_id")
    /** Database column thing_id SqlType(int4) */
    val thingId: Rep[Int] = column[Int]("thing_id")
    /** Database column relationship_type SqlType(varchar), Length(100,true), Default(None) */
    val relationshipType: Rep[Option[String]] = column[Option[String]]("relationship_type", O.Length(100,varying=true), O.Default(None))
    /** Database column is_current SqlType(bool) */
    val isCurrent: Rep[Boolean] = column[Boolean]("is_current")

    /** Foreign key referencing PeoplePerson (database name owner_id_refs_id) */
    lazy val peoplePersonFk = foreignKey("owner_id_refs_id", personId, PeoplePerson)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
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
   *  @param relationshipType Database column relationship_type SqlType(varchar), Length(100,true), Default(None) */
  case class ThingsThingtothingcrossrefRow(id: Int, dateCreated: org.joda.time.LocalDateTime, lastUpdated: org.joda.time.LocalDateTime, thingOneId: Int, thingTwoId: Int, relationshipType: Option[String] = None)
  /** GetResult implicit for fetching ThingsThingtothingcrossrefRow objects using plain SQL queries */
  implicit def GetResultThingsThingtothingcrossrefRow(implicit e0: GR[Int], e1: GR[org.joda.time.LocalDateTime], e2: GR[Option[String]]): GR[ThingsThingtothingcrossrefRow] = GR{
    prs => import prs._
    ThingsThingtothingcrossrefRow.tupled((<<[Int], <<[org.joda.time.LocalDateTime], <<[org.joda.time.LocalDateTime], <<[Int], <<[Int], <<?[String]))
  }
  /** Table description of table things_thingtothingcrossref. Objects of this class serve as prototypes for rows in queries. */
  class ThingsThingtothingcrossref(_tableTag: Tag) extends Table[ThingsThingtothingcrossrefRow](_tableTag, "things_thingtothingcrossref") {
    def * = (id, dateCreated, lastUpdated, thingOneId, thingTwoId, relationshipType) <> (ThingsThingtothingcrossrefRow.tupled, ThingsThingtothingcrossrefRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(dateCreated), Rep.Some(lastUpdated), Rep.Some(thingOneId), Rep.Some(thingTwoId), relationshipType).shaped.<>({r=>import r._; _1.map(_=> ThingsThingtothingcrossrefRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

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
    /** Database column relationship_type SqlType(varchar), Length(100,true), Default(None) */
    val relationshipType: Rep[Option[String]] = column[Option[String]]("relationship_type", O.Length(100,varying=true), O.Default(None))

    /** Foreign key referencing ThingsThing (database name thing_one_id_refs_id_fk) */
    lazy val thingsThingFk1 = foreignKey("thing_one_id_refs_id_fk", thingOneId, ThingsThing)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
    /** Foreign key referencing ThingsThing (database name thing_two_id_refs_id_fk) */
    lazy val thingsThingFk2 = foreignKey("thing_two_id_refs_id_fk", thingTwoId, ThingsThing)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
  }
  /** Collection-like TableQuery object for table ThingsThingtothingcrossref */
  lazy val ThingsThingtothingcrossref = new TableQuery(tag => new ThingsThingtothingcrossref(tag))
}
