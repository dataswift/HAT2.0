package org.hatdex.hat.dal
// AUTO-GENERATED Slick data model
/** Stand-alone Slick data model for immediate use */
object Tables extends {
  val profile = org.hatdex.libs.dal.HATPostgresProfile
} with Tables
/** Slick data model trait for extension, choice of backend or usage in the cake pattern. (Make sure to initialize this late.) */
trait Tables {
  val profile: org.hatdex.libs.dal.HATPostgresProfile
  import profile.api._
  import slick.model.ForeignKeyAction
  // NOTE: GetResult mappers for plain SQL are only generated for tables where Slick knows how to map the types of all columns.
  import slick.jdbc.{ GetResult => GR }

  /** DDL for all tables. Call .create to execute. */
  lazy val schema: profile.SchemaDescription = Array(Applications.schema, ApplicationStatus.schema, BundleContextless.schema, BundleContextlessDataSourceDataset.schema, DataBundles.schema, DataCombinators.schema, DataDebit.schema, DataDebitBundle.schema, DataDebitContract.schema, DataDebitPermissions.schema, DataField.schema, DataJson.schema, DataJsonGroupRecords.schema, DataJsonGroups.schema, DataRecord.schema, DataStatsLog.schema, DataTable.schema, DataTableSize.schema, DataTabletotablecrossref.schema, DataTableTree.schema, DataValue.schema, HatFile.schema, HatFileAccess.schema, SheFunction.schema, SystemEventlog.schema, UserAccessLog.schema, UserMailTokens.schema, UserRole.schema, UserRoleAvailable.schema, UserUser.schema).reduceLeft(_ ++ _)
  @deprecated("Use .schema instead of .ddl", "3.0")
  def ddl = schema

  /**
   * Entity class storing rows of table Applications
   *  @param dateCreated Database column date_created SqlType(timestamp)
   *  @param dateSetup Database column date_setup SqlType(timestamp), Default(None)
   *  @param title Database column title SqlType(varchar), PrimaryKey
   *  @param description Database column description SqlType(varchar)
   *  @param logoUrl Database column logo_url SqlType(varchar)
   *  @param url Database column url SqlType(varchar)
   *  @param authUrl Database column auth_url SqlType(varchar)
   *  @param browser Database column browser SqlType(bool)
   *  @param category Database column category SqlType(varchar)
   *  @param setup Database column setup SqlType(bool)
   *  @param loginAvailable Database column login_available SqlType(bool)
   *  @param namespace Database column namespace SqlType(varchar), Default()
   */
  case class ApplicationsRow(dateCreated: org.joda.time.LocalDateTime, dateSetup: Option[org.joda.time.LocalDateTime] = None, title: String, description: String, logoUrl: String, url: String, authUrl: String, browser: Boolean, category: String, setup: Boolean, loginAvailable: Boolean, namespace: String = "")
  /** GetResult implicit for fetching ApplicationsRow objects using plain SQL queries */
  implicit def GetResultApplicationsRow(implicit e0: GR[org.joda.time.LocalDateTime], e1: GR[Option[org.joda.time.LocalDateTime]], e2: GR[String], e3: GR[Boolean]): GR[ApplicationsRow] = GR {
    prs =>
      import prs._
      ApplicationsRow.tupled((<<[org.joda.time.LocalDateTime], <<?[org.joda.time.LocalDateTime], <<[String], <<[String], <<[String], <<[String], <<[String], <<[Boolean], <<[String], <<[Boolean], <<[Boolean], <<[String]))
  }
  /** Table description of table applications. Objects of this class serve as prototypes for rows in queries. */
  class Applications(_tableTag: Tag) extends profile.api.Table[ApplicationsRow](_tableTag, Some("hat"), "applications") {
    def * = (dateCreated, dateSetup, title, description, logoUrl, url, authUrl, browser, category, setup, loginAvailable, namespace) <> (ApplicationsRow.tupled, ApplicationsRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(dateCreated), dateSetup, Rep.Some(title), Rep.Some(description), Rep.Some(logoUrl), Rep.Some(url), Rep.Some(authUrl), Rep.Some(browser), Rep.Some(category), Rep.Some(setup), Rep.Some(loginAvailable), Rep.Some(namespace)).shaped.<>({ r => import r._; _1.map(_ => ApplicationsRow.tupled((_1.get, _2, _3.get, _4.get, _5.get, _6.get, _7.get, _8.get, _9.get, _10.get, _11.get, _12.get))) }, (_: Any) => throw new Exception("Inserting into ? projection not supported."))

    /** Database column date_created SqlType(timestamp) */
    val dateCreated: Rep[org.joda.time.LocalDateTime] = column[org.joda.time.LocalDateTime]("date_created")
    /** Database column date_setup SqlType(timestamp), Default(None) */
    val dateSetup: Rep[Option[org.joda.time.LocalDateTime]] = column[Option[org.joda.time.LocalDateTime]]("date_setup", O.Default(None))
    /** Database column title SqlType(varchar), PrimaryKey */
    val title: Rep[String] = column[String]("title", O.PrimaryKey)
    /** Database column description SqlType(varchar) */
    val description: Rep[String] = column[String]("description")
    /** Database column logo_url SqlType(varchar) */
    val logoUrl: Rep[String] = column[String]("logo_url")
    /** Database column url SqlType(varchar) */
    val url: Rep[String] = column[String]("url")
    /** Database column auth_url SqlType(varchar) */
    val authUrl: Rep[String] = column[String]("auth_url")
    /** Database column browser SqlType(bool) */
    val browser: Rep[Boolean] = column[Boolean]("browser")
    /** Database column category SqlType(varchar) */
    val category: Rep[String] = column[String]("category")
    /** Database column setup SqlType(bool) */
    val setup: Rep[Boolean] = column[Boolean]("setup")
    /** Database column login_available SqlType(bool) */
    val loginAvailable: Rep[Boolean] = column[Boolean]("login_available")
    /** Database column namespace SqlType(varchar), Default() */
    val namespace: Rep[String] = column[String]("namespace", O.Default(""))
  }
  /** Collection-like TableQuery object for table Applications */
  lazy val Applications = new TableQuery(tag => new Applications(tag))

  /**
   * Entity class storing rows of table ApplicationStatus
   *  @param id Database column id SqlType(varchar), PrimaryKey
   *  @param version Database column version SqlType(varchar)
   *  @param enabled Database column enabled SqlType(bool)
   */
  case class ApplicationStatusRow(id: String, version: String, enabled: Boolean)
  /** GetResult implicit for fetching ApplicationStatusRow objects using plain SQL queries */
  implicit def GetResultApplicationStatusRow(implicit e0: GR[String], e1: GR[Boolean]): GR[ApplicationStatusRow] = GR {
    prs =>
      import prs._
      ApplicationStatusRow.tupled((<<[String], <<[String], <<[Boolean]))
  }
  /** Table description of table application_status. Objects of this class serve as prototypes for rows in queries. */
  class ApplicationStatus(_tableTag: Tag) extends profile.api.Table[ApplicationStatusRow](_tableTag, Some("hat"), "application_status") {
    def * = (id, version, enabled) <> (ApplicationStatusRow.tupled, ApplicationStatusRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(version), Rep.Some(enabled)).shaped.<>({ r => import r._; _1.map(_ => ApplicationStatusRow.tupled((_1.get, _2.get, _3.get))) }, (_: Any) => throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(varchar), PrimaryKey */
    val id: Rep[String] = column[String]("id", O.PrimaryKey)
    /** Database column version SqlType(varchar) */
    val version: Rep[String] = column[String]("version")
    /** Database column enabled SqlType(bool) */
    val enabled: Rep[Boolean] = column[Boolean]("enabled")
  }
  /** Collection-like TableQuery object for table ApplicationStatus */
  lazy val ApplicationStatus = new TableQuery(tag => new ApplicationStatus(tag))

  /**
   * Entity class storing rows of table BundleContextless
   *  @param id Database column id SqlType(serial), AutoInc, PrimaryKey
   *  @param name Database column name SqlType(varchar)
   *  @param dateCreated Database column date_created SqlType(timestamp)
   *  @param lastUpdated Database column last_updated SqlType(timestamp)
   */
  case class BundleContextlessRow(id: Int, name: String, dateCreated: org.joda.time.LocalDateTime, lastUpdated: org.joda.time.LocalDateTime)
  /** GetResult implicit for fetching BundleContextlessRow objects using plain SQL queries */
  implicit def GetResultBundleContextlessRow(implicit e0: GR[Int], e1: GR[String], e2: GR[org.joda.time.LocalDateTime]): GR[BundleContextlessRow] = GR {
    prs =>
      import prs._
      BundleContextlessRow.tupled((<<[Int], <<[String], <<[org.joda.time.LocalDateTime], <<[org.joda.time.LocalDateTime]))
  }
  /** Table description of table bundle_contextless. Objects of this class serve as prototypes for rows in queries. */
  class BundleContextless(_tableTag: Tag) extends profile.api.Table[BundleContextlessRow](_tableTag, Some("hat"), "bundle_contextless") {
    def * = (id, name, dateCreated, lastUpdated) <> (BundleContextlessRow.tupled, BundleContextlessRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(name), Rep.Some(dateCreated), Rep.Some(lastUpdated)).shaped.<>({ r => import r._; _1.map(_ => BundleContextlessRow.tupled((_1.get, _2.get, _3.get, _4.get))) }, (_: Any) => throw new Exception("Inserting into ? projection not supported."))

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

  /**
   * Entity class storing rows of table BundleContextlessDataSourceDataset
   *  @param id Database column id SqlType(serial), AutoInc, PrimaryKey
   *  @param bundleId Database column bundle_id SqlType(int4)
   *  @param sourceName Database column source_name SqlType(varchar)
   *  @param datasetName Database column dataset_name SqlType(varchar)
   *  @param datasetTableId Database column dataset_table_id SqlType(int4)
   *  @param description Database column description SqlType(varchar)
   *  @param fieldStructure Database column field_structure SqlType(varchar)
   *  @param fieldIds Database column field_ids SqlType(_int4)
   */
  case class BundleContextlessDataSourceDatasetRow(id: Int, bundleId: Int, sourceName: String, datasetName: String, datasetTableId: Int, description: String, fieldStructure: String, fieldIds: List[Int])
  /** GetResult implicit for fetching BundleContextlessDataSourceDatasetRow objects using plain SQL queries */
  implicit def GetResultBundleContextlessDataSourceDatasetRow(implicit e0: GR[Int], e1: GR[String], e2: GR[List[Int]]): GR[BundleContextlessDataSourceDatasetRow] = GR {
    prs =>
      import prs._
      BundleContextlessDataSourceDatasetRow.tupled((<<[Int], <<[Int], <<[String], <<[String], <<[Int], <<[String], <<[String], <<[List[Int]]))
  }
  /** Table description of table bundle_contextless_data_source_dataset. Objects of this class serve as prototypes for rows in queries. */
  class BundleContextlessDataSourceDataset(_tableTag: Tag) extends profile.api.Table[BundleContextlessDataSourceDatasetRow](_tableTag, Some("hat"), "bundle_contextless_data_source_dataset") {
    def * = (id, bundleId, sourceName, datasetName, datasetTableId, description, fieldStructure, fieldIds) <> (BundleContextlessDataSourceDatasetRow.tupled, BundleContextlessDataSourceDatasetRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(bundleId), Rep.Some(sourceName), Rep.Some(datasetName), Rep.Some(datasetTableId), Rep.Some(description), Rep.Some(fieldStructure), Rep.Some(fieldIds)).shaped.<>({ r => import r._; _1.map(_ => BundleContextlessDataSourceDatasetRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get, _7.get, _8.get))) }, (_: Any) => throw new Exception("Inserting into ? projection not supported."))

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
    /** Database column field_ids SqlType(_int4) */
    val fieldIds: Rep[List[Int]] = column[List[Int]]("field_ids")

    /** Foreign key referencing BundleContextless (database name bundle_contextless_data_source_dataset_bundle_id_fkey) */
    lazy val bundleContextlessFk = foreignKey("bundle_contextless_data_source_dataset_bundle_id_fkey", bundleId, BundleContextless)(r => r.id, onUpdate = ForeignKeyAction.NoAction, onDelete = ForeignKeyAction.NoAction)
    /** Foreign key referencing DataTable (database name bundle_contextless_data_source_dataset_dataset_table_id_fkey) */
    lazy val dataTableFk = foreignKey("bundle_contextless_data_source_dataset_dataset_table_id_fkey", datasetTableId, DataTable)(r => r.id, onUpdate = ForeignKeyAction.NoAction, onDelete = ForeignKeyAction.NoAction)
  }
  /** Collection-like TableQuery object for table BundleContextlessDataSourceDataset */
  lazy val BundleContextlessDataSourceDataset = new TableQuery(tag => new BundleContextlessDataSourceDataset(tag))

  /**
   * Entity class storing rows of table DataBundles
   *  @param bundleId Database column bundle_id SqlType(varchar), PrimaryKey
   *  @param bundle Database column bundle SqlType(jsonb)
   */
  case class DataBundlesRow(bundleId: String, bundle: play.api.libs.json.JsValue)
  /** GetResult implicit for fetching DataBundlesRow objects using plain SQL queries */
  implicit def GetResultDataBundlesRow(implicit e0: GR[String], e1: GR[play.api.libs.json.JsValue]): GR[DataBundlesRow] = GR {
    prs =>
      import prs._
      DataBundlesRow.tupled((<<[String], <<[play.api.libs.json.JsValue]))
  }
  /** Table description of table data_bundles. Objects of this class serve as prototypes for rows in queries. */
  class DataBundles(_tableTag: Tag) extends profile.api.Table[DataBundlesRow](_tableTag, Some("hat"), "data_bundles") {
    def * = (bundleId, bundle) <> (DataBundlesRow.tupled, DataBundlesRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(bundleId), Rep.Some(bundle)).shaped.<>({ r => import r._; _1.map(_ => DataBundlesRow.tupled((_1.get, _2.get))) }, (_: Any) => throw new Exception("Inserting into ? projection not supported."))

    /** Database column bundle_id SqlType(varchar), PrimaryKey */
    val bundleId: Rep[String] = column[String]("bundle_id", O.PrimaryKey)
    /** Database column bundle SqlType(jsonb) */
    val bundle: Rep[play.api.libs.json.JsValue] = column[play.api.libs.json.JsValue]("bundle")
  }
  /** Collection-like TableQuery object for table DataBundles */
  lazy val DataBundles = new TableQuery(tag => new DataBundles(tag))

  /**
   * Entity class storing rows of table DataCombinators
   *  @param combinatorId Database column combinator_id SqlType(varchar), PrimaryKey
   *  @param combinator Database column combinator SqlType(jsonb)
   */
  case class DataCombinatorsRow(combinatorId: String, combinator: play.api.libs.json.JsValue)
  /** GetResult implicit for fetching DataCombinatorsRow objects using plain SQL queries */
  implicit def GetResultDataCombinatorsRow(implicit e0: GR[String], e1: GR[play.api.libs.json.JsValue]): GR[DataCombinatorsRow] = GR {
    prs =>
      import prs._
      DataCombinatorsRow.tupled((<<[String], <<[play.api.libs.json.JsValue]))
  }
  /** Table description of table data_combinators. Objects of this class serve as prototypes for rows in queries. */
  class DataCombinators(_tableTag: Tag) extends profile.api.Table[DataCombinatorsRow](_tableTag, Some("hat"), "data_combinators") {
    def * = (combinatorId, combinator) <> (DataCombinatorsRow.tupled, DataCombinatorsRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(combinatorId), Rep.Some(combinator)).shaped.<>({ r => import r._; _1.map(_ => DataCombinatorsRow.tupled((_1.get, _2.get))) }, (_: Any) => throw new Exception("Inserting into ? projection not supported."))

    /** Database column combinator_id SqlType(varchar), PrimaryKey */
    val combinatorId: Rep[String] = column[String]("combinator_id", O.PrimaryKey)
    /** Database column combinator SqlType(jsonb) */
    val combinator: Rep[play.api.libs.json.JsValue] = column[play.api.libs.json.JsValue]("combinator")
  }
  /** Collection-like TableQuery object for table DataCombinators */
  lazy val DataCombinators = new TableQuery(tag => new DataCombinators(tag))

  /**
   * Entity class storing rows of table DataDebit
   *  @param dataDebitKey Database column data_debit_key SqlType(varchar), PrimaryKey
   *  @param dateCreated Database column date_created SqlType(timestamp)
   *  @param requestClientName Database column request_client_name SqlType(varchar)
   *  @param requestClientUrl Database column request_client_url SqlType(varchar)
   *  @param requestClientLogoUrl Database column request_client_logo_url SqlType(varchar)
   *  @param requestApplicationId Database column request_application_id SqlType(varchar), Default(None)
   *  @param requestDescription Database column request_description SqlType(varchar), Default(None)
   */
  case class DataDebitRow(dataDebitKey: String, dateCreated: org.joda.time.LocalDateTime, requestClientName: String, requestClientUrl: String, requestClientLogoUrl: String, requestApplicationId: Option[String] = None, requestDescription: Option[String] = None)
  /** GetResult implicit for fetching DataDebitRow objects using plain SQL queries */
  implicit def GetResultDataDebitRow(implicit e0: GR[String], e1: GR[org.joda.time.LocalDateTime], e2: GR[Option[String]]): GR[DataDebitRow] = GR {
    prs =>
      import prs._
      DataDebitRow.tupled((<<[String], <<[org.joda.time.LocalDateTime], <<[String], <<[String], <<[String], <<?[String], <<?[String]))
  }
  /** Table description of table data_debit. Objects of this class serve as prototypes for rows in queries. */
  class DataDebit(_tableTag: Tag) extends profile.api.Table[DataDebitRow](_tableTag, Some("hat"), "data_debit") {
    def * = (dataDebitKey, dateCreated, requestClientName, requestClientUrl, requestClientLogoUrl, requestApplicationId, requestDescription) <> (DataDebitRow.tupled, DataDebitRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(dataDebitKey), Rep.Some(dateCreated), Rep.Some(requestClientName), Rep.Some(requestClientUrl), Rep.Some(requestClientLogoUrl), requestApplicationId, requestDescription).shaped.<>({ r => import r._; _1.map(_ => DataDebitRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6, _7))) }, (_: Any) => throw new Exception("Inserting into ? projection not supported."))

    /** Database column data_debit_key SqlType(varchar), PrimaryKey */
    val dataDebitKey: Rep[String] = column[String]("data_debit_key", O.PrimaryKey)
    /** Database column date_created SqlType(timestamp) */
    val dateCreated: Rep[org.joda.time.LocalDateTime] = column[org.joda.time.LocalDateTime]("date_created")
    /** Database column request_client_name SqlType(varchar) */
    val requestClientName: Rep[String] = column[String]("request_client_name")
    /** Database column request_client_url SqlType(varchar) */
    val requestClientUrl: Rep[String] = column[String]("request_client_url")
    /** Database column request_client_logo_url SqlType(varchar) */
    val requestClientLogoUrl: Rep[String] = column[String]("request_client_logo_url")
    /** Database column request_application_id SqlType(varchar), Default(None) */
    val requestApplicationId: Rep[Option[String]] = column[Option[String]]("request_application_id", O.Default(None))
    /** Database column request_description SqlType(varchar), Default(None) */
    val requestDescription: Rep[Option[String]] = column[Option[String]]("request_description", O.Default(None))
  }
  /** Collection-like TableQuery object for table DataDebit */
  lazy val DataDebit = new TableQuery(tag => new DataDebit(tag))

  /**
   * Entity class storing rows of table DataDebitBundle
   *  @param dataDebitKey Database column data_debit_key SqlType(varchar)
   *  @param bundleId Database column bundle_id SqlType(varchar)
   *  @param dateCreated Database column date_created SqlType(timestamp)
   *  @param startDate Database column start_date SqlType(timestamp)
   *  @param endDate Database column end_date SqlType(timestamp)
   *  @param rolling Database column rolling SqlType(bool)
   *  @param enabled Database column enabled SqlType(bool)
   *  @param conditions Database column conditions SqlType(varchar), Default(None)
   */
  case class DataDebitBundleRow(dataDebitKey: String, bundleId: String, dateCreated: org.joda.time.LocalDateTime, startDate: org.joda.time.LocalDateTime, endDate: org.joda.time.LocalDateTime, rolling: Boolean, enabled: Boolean, conditions: Option[String] = None)
  /** GetResult implicit for fetching DataDebitBundleRow objects using plain SQL queries */
  implicit def GetResultDataDebitBundleRow(implicit e0: GR[String], e1: GR[org.joda.time.LocalDateTime], e2: GR[Boolean], e3: GR[Option[String]]): GR[DataDebitBundleRow] = GR {
    prs =>
      import prs._
      DataDebitBundleRow.tupled((<<[String], <<[String], <<[org.joda.time.LocalDateTime], <<[org.joda.time.LocalDateTime], <<[org.joda.time.LocalDateTime], <<[Boolean], <<[Boolean], <<?[String]))
  }
  /** Table description of table data_debit_bundle. Objects of this class serve as prototypes for rows in queries. */
  class DataDebitBundle(_tableTag: Tag) extends profile.api.Table[DataDebitBundleRow](_tableTag, Some("hat"), "data_debit_bundle") {
    def * = (dataDebitKey, bundleId, dateCreated, startDate, endDate, rolling, enabled, conditions) <> (DataDebitBundleRow.tupled, DataDebitBundleRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(dataDebitKey), Rep.Some(bundleId), Rep.Some(dateCreated), Rep.Some(startDate), Rep.Some(endDate), Rep.Some(rolling), Rep.Some(enabled), conditions).shaped.<>({ r => import r._; _1.map(_ => DataDebitBundleRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get, _7.get, _8))) }, (_: Any) => throw new Exception("Inserting into ? projection not supported."))

    /** Database column data_debit_key SqlType(varchar) */
    val dataDebitKey: Rep[String] = column[String]("data_debit_key")
    /** Database column bundle_id SqlType(varchar) */
    val bundleId: Rep[String] = column[String]("bundle_id")
    /** Database column date_created SqlType(timestamp) */
    val dateCreated: Rep[org.joda.time.LocalDateTime] = column[org.joda.time.LocalDateTime]("date_created")
    /** Database column start_date SqlType(timestamp) */
    val startDate: Rep[org.joda.time.LocalDateTime] = column[org.joda.time.LocalDateTime]("start_date")
    /** Database column end_date SqlType(timestamp) */
    val endDate: Rep[org.joda.time.LocalDateTime] = column[org.joda.time.LocalDateTime]("end_date")
    /** Database column rolling SqlType(bool) */
    val rolling: Rep[Boolean] = column[Boolean]("rolling")
    /** Database column enabled SqlType(bool) */
    val enabled: Rep[Boolean] = column[Boolean]("enabled")
    /** Database column conditions SqlType(varchar), Default(None) */
    val conditions: Rep[Option[String]] = column[Option[String]]("conditions", O.Default(None))

    /** Primary key of DataDebitBundle (database name data_debit_bundle_pkey) */
    val pk = primaryKey("data_debit_bundle_pkey", (dataDebitKey, bundleId))

    /** Foreign key referencing DataBundles (database name data_debit_bundle_bundle_id_fkey) */
    lazy val dataBundlesFk1 = foreignKey("data_debit_bundle_bundle_id_fkey", bundleId, DataBundles)(r => r.bundleId, onUpdate = ForeignKeyAction.NoAction, onDelete = ForeignKeyAction.NoAction)
    /** Foreign key referencing DataBundles (database name data_debit_bundle_conditions_fkey) */
    lazy val dataBundlesFk2 = foreignKey("data_debit_bundle_conditions_fkey", conditions, DataBundles)(r => Rep.Some(r.bundleId), onUpdate = ForeignKeyAction.NoAction, onDelete = ForeignKeyAction.NoAction)
    /** Foreign key referencing DataDebitContract (database name data_debit_bundle_data_debit_key_fkey) */
    lazy val dataDebitContractFk = foreignKey("data_debit_bundle_data_debit_key_fkey", dataDebitKey, DataDebitContract)(r => r.dataDebitKey, onUpdate = ForeignKeyAction.NoAction, onDelete = ForeignKeyAction.NoAction)
  }
  /** Collection-like TableQuery object for table DataDebitBundle */
  lazy val DataDebitBundle = new TableQuery(tag => new DataDebitBundle(tag))

  /**
   * Entity class storing rows of table DataDebitContract
   *  @param dataDebitKey Database column data_debit_key SqlType(varchar), PrimaryKey
   *  @param dateCreated Database column date_created SqlType(timestamp)
   *  @param clientId Database column client_id SqlType(uuid)
   */
  case class DataDebitContractRow(dataDebitKey: String, dateCreated: org.joda.time.LocalDateTime, clientId: java.util.UUID)
  /** GetResult implicit for fetching DataDebitContractRow objects using plain SQL queries */
  implicit def GetResultDataDebitContractRow(implicit e0: GR[String], e1: GR[org.joda.time.LocalDateTime], e2: GR[java.util.UUID]): GR[DataDebitContractRow] = GR {
    prs =>
      import prs._
      DataDebitContractRow.tupled((<<[String], <<[org.joda.time.LocalDateTime], <<[java.util.UUID]))
  }
  /** Table description of table data_debit_contract. Objects of this class serve as prototypes for rows in queries. */
  class DataDebitContract(_tableTag: Tag) extends profile.api.Table[DataDebitContractRow](_tableTag, Some("hat"), "data_debit_contract") {
    def * = (dataDebitKey, dateCreated, clientId) <> (DataDebitContractRow.tupled, DataDebitContractRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(dataDebitKey), Rep.Some(dateCreated), Rep.Some(clientId)).shaped.<>({ r => import r._; _1.map(_ => DataDebitContractRow.tupled((_1.get, _2.get, _3.get))) }, (_: Any) => throw new Exception("Inserting into ? projection not supported."))

    /** Database column data_debit_key SqlType(varchar), PrimaryKey */
    val dataDebitKey: Rep[String] = column[String]("data_debit_key", O.PrimaryKey)
    /** Database column date_created SqlType(timestamp) */
    val dateCreated: Rep[org.joda.time.LocalDateTime] = column[org.joda.time.LocalDateTime]("date_created")
    /** Database column client_id SqlType(uuid) */
    val clientId: Rep[java.util.UUID] = column[java.util.UUID]("client_id")

    /** Foreign key referencing UserUser (database name data_debit_contract_client_id_fkey) */
    lazy val userUserFk = foreignKey("data_debit_contract_client_id_fkey", clientId, UserUser)(r => r.userId, onUpdate = ForeignKeyAction.NoAction, onDelete = ForeignKeyAction.NoAction)
  }
  /** Collection-like TableQuery object for table DataDebitContract */
  lazy val DataDebitContract = new TableQuery(tag => new DataDebitContract(tag))

  /**
   * Entity class storing rows of table DataDebitPermissions
   *  @param permissionsId Database column permissions_id SqlType(serial), AutoInc, PrimaryKey
   *  @param dataDebitKey Database column data_debit_key SqlType(varchar)
   *  @param dateCreated Database column date_created SqlType(timestamp)
   *  @param purpose Database column purpose SqlType(varchar)
   *  @param start Database column start SqlType(timestamp)
   *  @param period Database column period SqlType(int8)
   *  @param cancelAtPeriodEnd Database column cancel_at_period_end SqlType(bool)
   *  @param canceledAt Database column canceled_at SqlType(timestamp), Default(None)
   *  @param termsUrl Database column terms_url SqlType(varchar)
   *  @param bundleId Database column bundle_id SqlType(varchar)
   *  @param conditions Database column conditions SqlType(varchar), Default(None)
   *  @param accepted Database column accepted SqlType(bool)
   */
  case class DataDebitPermissionsRow(permissionsId: Int, dataDebitKey: String, dateCreated: org.joda.time.LocalDateTime, purpose: String, start: org.joda.time.LocalDateTime, period: Long, cancelAtPeriodEnd: Boolean, canceledAt: Option[org.joda.time.LocalDateTime] = None, termsUrl: String, bundleId: String, conditions: Option[String] = None, accepted: Boolean)
  /** GetResult implicit for fetching DataDebitPermissionsRow objects using plain SQL queries */
  implicit def GetResultDataDebitPermissionsRow(implicit e0: GR[Int], e1: GR[String], e2: GR[org.joda.time.LocalDateTime], e3: GR[Long], e4: GR[Boolean], e5: GR[Option[org.joda.time.LocalDateTime]], e6: GR[Option[String]]): GR[DataDebitPermissionsRow] = GR {
    prs =>
      import prs._
      DataDebitPermissionsRow.tupled((<<[Int], <<[String], <<[org.joda.time.LocalDateTime], <<[String], <<[org.joda.time.LocalDateTime], <<[Long], <<[Boolean], <<?[org.joda.time.LocalDateTime], <<[String], <<[String], <<?[String], <<[Boolean]))
  }
  /** Table description of table data_debit_permissions. Objects of this class serve as prototypes for rows in queries. */
  class DataDebitPermissions(_tableTag: Tag) extends profile.api.Table[DataDebitPermissionsRow](_tableTag, Some("hat"), "data_debit_permissions") {
    def * = (permissionsId, dataDebitKey, dateCreated, purpose, start, period, cancelAtPeriodEnd, canceledAt, termsUrl, bundleId, conditions, accepted) <> (DataDebitPermissionsRow.tupled, DataDebitPermissionsRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(permissionsId), Rep.Some(dataDebitKey), Rep.Some(dateCreated), Rep.Some(purpose), Rep.Some(start), Rep.Some(period), Rep.Some(cancelAtPeriodEnd), canceledAt, Rep.Some(termsUrl), Rep.Some(bundleId), conditions, Rep.Some(accepted)).shaped.<>({ r => import r._; _1.map(_ => DataDebitPermissionsRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get, _7.get, _8, _9.get, _10.get, _11, _12.get))) }, (_: Any) => throw new Exception("Inserting into ? projection not supported."))

    /** Database column permissions_id SqlType(serial), AutoInc, PrimaryKey */
    val permissionsId: Rep[Int] = column[Int]("permissions_id", O.AutoInc, O.PrimaryKey)
    /** Database column data_debit_key SqlType(varchar) */
    val dataDebitKey: Rep[String] = column[String]("data_debit_key")
    /** Database column date_created SqlType(timestamp) */
    val dateCreated: Rep[org.joda.time.LocalDateTime] = column[org.joda.time.LocalDateTime]("date_created")
    /** Database column purpose SqlType(varchar) */
    val purpose: Rep[String] = column[String]("purpose")
    /** Database column start SqlType(timestamp) */
    val start: Rep[org.joda.time.LocalDateTime] = column[org.joda.time.LocalDateTime]("start")
    /** Database column period SqlType(int8) */
    val period: Rep[Long] = column[Long]("period")
    /** Database column cancel_at_period_end SqlType(bool) */
    val cancelAtPeriodEnd: Rep[Boolean] = column[Boolean]("cancel_at_period_end")
    /** Database column canceled_at SqlType(timestamp), Default(None) */
    val canceledAt: Rep[Option[org.joda.time.LocalDateTime]] = column[Option[org.joda.time.LocalDateTime]]("canceled_at", O.Default(None))
    /** Database column terms_url SqlType(varchar) */
    val termsUrl: Rep[String] = column[String]("terms_url")
    /** Database column bundle_id SqlType(varchar) */
    val bundleId: Rep[String] = column[String]("bundle_id")
    /** Database column conditions SqlType(varchar), Default(None) */
    val conditions: Rep[Option[String]] = column[Option[String]]("conditions", O.Default(None))
    /** Database column accepted SqlType(bool) */
    val accepted: Rep[Boolean] = column[Boolean]("accepted")

    /** Foreign key referencing DataBundles (database name data_debit_permissions_bundle_id_fkey) */
    lazy val dataBundlesFk1 = foreignKey("data_debit_permissions_bundle_id_fkey", bundleId, DataBundles)(r => r.bundleId, onUpdate = ForeignKeyAction.NoAction, onDelete = ForeignKeyAction.NoAction)
    /** Foreign key referencing DataBundles (database name data_debit_permissions_conditions_fkey) */
    lazy val dataBundlesFk2 = foreignKey("data_debit_permissions_conditions_fkey", conditions, DataBundles)(r => Rep.Some(r.bundleId), onUpdate = ForeignKeyAction.NoAction, onDelete = ForeignKeyAction.NoAction)
    /** Foreign key referencing DataDebit (database name data_debit_permissions_data_debit_key_fkey) */
    lazy val dataDebitFk = foreignKey("data_debit_permissions_data_debit_key_fkey", dataDebitKey, DataDebit)(r => r.dataDebitKey, onUpdate = ForeignKeyAction.NoAction, onDelete = ForeignKeyAction.NoAction)
  }
  /** Collection-like TableQuery object for table DataDebitPermissions */
  lazy val DataDebitPermissions = new TableQuery(tag => new DataDebitPermissions(tag))

  /**
   * Entity class storing rows of table DataField
   *  @param id Database column id SqlType(serial), AutoInc, PrimaryKey
   *  @param dateCreated Database column date_created SqlType(timestamp)
   *  @param lastUpdated Database column last_updated SqlType(timestamp)
   *  @param name Database column name SqlType(varchar)
   *  @param tableIdFk Database column table_id_fk SqlType(int4)
   *  @param deleted Database column deleted SqlType(bool), Default(false)
   */
  case class DataFieldRow(id: Int, dateCreated: org.joda.time.LocalDateTime, lastUpdated: org.joda.time.LocalDateTime, name: String, tableIdFk: Int, deleted: Boolean = false)
  /** GetResult implicit for fetching DataFieldRow objects using plain SQL queries */
  implicit def GetResultDataFieldRow(implicit e0: GR[Int], e1: GR[org.joda.time.LocalDateTime], e2: GR[String], e3: GR[Boolean]): GR[DataFieldRow] = GR {
    prs =>
      import prs._
      DataFieldRow.tupled((<<[Int], <<[org.joda.time.LocalDateTime], <<[org.joda.time.LocalDateTime], <<[String], <<[Int], <<[Boolean]))
  }
  /** Table description of table data_field. Objects of this class serve as prototypes for rows in queries. */
  class DataField(_tableTag: Tag) extends profile.api.Table[DataFieldRow](_tableTag, Some("hat"), "data_field") {
    def * = (id, dateCreated, lastUpdated, name, tableIdFk, deleted) <> (DataFieldRow.tupled, DataFieldRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(dateCreated), Rep.Some(lastUpdated), Rep.Some(name), Rep.Some(tableIdFk), Rep.Some(deleted)).shaped.<>({ r => import r._; _1.map(_ => DataFieldRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get))) }, (_: Any) => throw new Exception("Inserting into ? projection not supported."))

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
    /** Database column deleted SqlType(bool), Default(false) */
    val deleted: Rep[Boolean] = column[Boolean]("deleted", O.Default(false))

    /** Foreign key referencing DataTable (database name data_table_fk) */
    lazy val dataTableFk = foreignKey("data_table_fk", tableIdFk, DataTable)(r => r.id, onUpdate = ForeignKeyAction.NoAction, onDelete = ForeignKeyAction.NoAction)
  }
  /** Collection-like TableQuery object for table DataField */
  lazy val DataField = new TableQuery(tag => new DataField(tag))

  /**
   * Entity class storing rows of table DataJson
   *  @param recordId Database column record_id SqlType(uuid), PrimaryKey
   *  @param source Database column source SqlType(varchar)
   *  @param owner Database column owner SqlType(uuid)
   *  @param date Database column date SqlType(timestamp)
   *  @param data Database column data SqlType(jsonb)
   *  @param hash Database column hash SqlType(bytea)
   */
  case class DataJsonRow(recordId: java.util.UUID, source: String, owner: java.util.UUID, date: org.joda.time.LocalDateTime, data: play.api.libs.json.JsValue, hash: Array[Byte])
  /** GetResult implicit for fetching DataJsonRow objects using plain SQL queries */
  implicit def GetResultDataJsonRow(implicit e0: GR[java.util.UUID], e1: GR[String], e2: GR[org.joda.time.LocalDateTime], e3: GR[play.api.libs.json.JsValue], e4: GR[Array[Byte]]): GR[DataJsonRow] = GR {
    prs =>
      import prs._
      DataJsonRow.tupled((<<[java.util.UUID], <<[String], <<[java.util.UUID], <<[org.joda.time.LocalDateTime], <<[play.api.libs.json.JsValue], <<[Array[Byte]]))
  }
  /** Table description of table data_json. Objects of this class serve as prototypes for rows in queries. */
  class DataJson(_tableTag: Tag) extends profile.api.Table[DataJsonRow](_tableTag, Some("hat"), "data_json") {
    def * = (recordId, source, owner, date, data, hash) <> (DataJsonRow.tupled, DataJsonRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(recordId), Rep.Some(source), Rep.Some(owner), Rep.Some(date), Rep.Some(data), Rep.Some(hash)).shaped.<>({ r => import r._; _1.map(_ => DataJsonRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get))) }, (_: Any) => throw new Exception("Inserting into ? projection not supported."))

    /** Database column record_id SqlType(uuid), PrimaryKey */
    val recordId: Rep[java.util.UUID] = column[java.util.UUID]("record_id", O.PrimaryKey)
    /** Database column source SqlType(varchar) */
    val source: Rep[String] = column[String]("source")
    /** Database column owner SqlType(uuid) */
    val owner: Rep[java.util.UUID] = column[java.util.UUID]("owner")
    /** Database column date SqlType(timestamp) */
    val date: Rep[org.joda.time.LocalDateTime] = column[org.joda.time.LocalDateTime]("date")
    /** Database column data SqlType(jsonb) */
    val data: Rep[play.api.libs.json.JsValue] = column[play.api.libs.json.JsValue]("data")
    /** Database column hash SqlType(bytea) */
    val hash: Rep[Array[Byte]] = column[Array[Byte]]("hash")

    /** Foreign key referencing UserUser (database name data_json_owner_fkey) */
    lazy val userUserFk = foreignKey("data_json_owner_fkey", owner, UserUser)(r => r.userId, onUpdate = ForeignKeyAction.NoAction, onDelete = ForeignKeyAction.NoAction)

    /** Uniqueness Index over (hash) (database name data_json_hash_key) */
    val index1 = index("data_json_hash_key", hash, unique = true)
  }
  /** Collection-like TableQuery object for table DataJson */
  lazy val DataJson = new TableQuery(tag => new DataJson(tag))

  /**
   * Entity class storing rows of table DataJsonGroupRecords
   *  @param groupId Database column group_id SqlType(uuid)
   *  @param recordId Database column record_id SqlType(uuid)
   */
  case class DataJsonGroupRecordsRow(groupId: java.util.UUID, recordId: java.util.UUID)
  /** GetResult implicit for fetching DataJsonGroupRecordsRow objects using plain SQL queries */
  implicit def GetResultDataJsonGroupRecordsRow(implicit e0: GR[java.util.UUID]): GR[DataJsonGroupRecordsRow] = GR {
    prs =>
      import prs._
      DataJsonGroupRecordsRow.tupled((<<[java.util.UUID], <<[java.util.UUID]))
  }
  /** Table description of table data_json_group_records. Objects of this class serve as prototypes for rows in queries. */
  class DataJsonGroupRecords(_tableTag: Tag) extends profile.api.Table[DataJsonGroupRecordsRow](_tableTag, Some("hat"), "data_json_group_records") {
    def * = (groupId, recordId) <> (DataJsonGroupRecordsRow.tupled, DataJsonGroupRecordsRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(groupId), Rep.Some(recordId)).shaped.<>({ r => import r._; _1.map(_ => DataJsonGroupRecordsRow.tupled((_1.get, _2.get))) }, (_: Any) => throw new Exception("Inserting into ? projection not supported."))

    /** Database column group_id SqlType(uuid) */
    val groupId: Rep[java.util.UUID] = column[java.util.UUID]("group_id")
    /** Database column record_id SqlType(uuid) */
    val recordId: Rep[java.util.UUID] = column[java.util.UUID]("record_id")

    /** Primary key of DataJsonGroupRecords (database name data_json_group_records_pkey) */
    val pk = primaryKey("data_json_group_records_pkey", (groupId, recordId))

    /** Foreign key referencing DataJson (database name data_json_group_records_record_id_fkey) */
    lazy val dataJsonFk = foreignKey("data_json_group_records_record_id_fkey", recordId, DataJson)(r => r.recordId, onUpdate = ForeignKeyAction.NoAction, onDelete = ForeignKeyAction.NoAction)
    /** Foreign key referencing DataJsonGroups (database name data_json_group_records_group_id_fkey) */
    lazy val dataJsonGroupsFk = foreignKey("data_json_group_records_group_id_fkey", groupId, DataJsonGroups)(r => r.groupId, onUpdate = ForeignKeyAction.NoAction, onDelete = ForeignKeyAction.NoAction)
  }
  /** Collection-like TableQuery object for table DataJsonGroupRecords */
  lazy val DataJsonGroupRecords = new TableQuery(tag => new DataJsonGroupRecords(tag))

  /**
   * Entity class storing rows of table DataJsonGroups
   *  @param groupId Database column group_id SqlType(uuid), PrimaryKey
   *  @param owner Database column owner SqlType(uuid)
   *  @param date Database column date SqlType(timestamp)
   */
  case class DataJsonGroupsRow(groupId: java.util.UUID, owner: java.util.UUID, date: org.joda.time.LocalDateTime)
  /** GetResult implicit for fetching DataJsonGroupsRow objects using plain SQL queries */
  implicit def GetResultDataJsonGroupsRow(implicit e0: GR[java.util.UUID], e1: GR[org.joda.time.LocalDateTime]): GR[DataJsonGroupsRow] = GR {
    prs =>
      import prs._
      DataJsonGroupsRow.tupled((<<[java.util.UUID], <<[java.util.UUID], <<[org.joda.time.LocalDateTime]))
  }
  /** Table description of table data_json_groups. Objects of this class serve as prototypes for rows in queries. */
  class DataJsonGroups(_tableTag: Tag) extends profile.api.Table[DataJsonGroupsRow](_tableTag, Some("hat"), "data_json_groups") {
    def * = (groupId, owner, date) <> (DataJsonGroupsRow.tupled, DataJsonGroupsRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(groupId), Rep.Some(owner), Rep.Some(date)).shaped.<>({ r => import r._; _1.map(_ => DataJsonGroupsRow.tupled((_1.get, _2.get, _3.get))) }, (_: Any) => throw new Exception("Inserting into ? projection not supported."))

    /** Database column group_id SqlType(uuid), PrimaryKey */
    val groupId: Rep[java.util.UUID] = column[java.util.UUID]("group_id", O.PrimaryKey)
    /** Database column owner SqlType(uuid) */
    val owner: Rep[java.util.UUID] = column[java.util.UUID]("owner")
    /** Database column date SqlType(timestamp) */
    val date: Rep[org.joda.time.LocalDateTime] = column[org.joda.time.LocalDateTime]("date")

    /** Foreign key referencing UserUser (database name data_json_groups_owner_fkey) */
    lazy val userUserFk = foreignKey("data_json_groups_owner_fkey", owner, UserUser)(r => r.userId, onUpdate = ForeignKeyAction.NoAction, onDelete = ForeignKeyAction.NoAction)
  }
  /** Collection-like TableQuery object for table DataJsonGroups */
  lazy val DataJsonGroups = new TableQuery(tag => new DataJsonGroups(tag))

  /**
   * Entity class storing rows of table DataRecord
   *  @param id Database column id SqlType(serial), AutoInc, PrimaryKey
   *  @param dateCreated Database column date_created SqlType(timestamp)
   *  @param lastUpdated Database column last_updated SqlType(timestamp)
   *  @param name Database column name SqlType(varchar)
   *  @param deleted Database column deleted SqlType(bool), Default(false)
   */
  case class DataRecordRow(id: Int, dateCreated: org.joda.time.LocalDateTime, lastUpdated: org.joda.time.LocalDateTime, name: String, deleted: Boolean = false)
  /** GetResult implicit for fetching DataRecordRow objects using plain SQL queries */
  implicit def GetResultDataRecordRow(implicit e0: GR[Int], e1: GR[org.joda.time.LocalDateTime], e2: GR[String], e3: GR[Boolean]): GR[DataRecordRow] = GR {
    prs =>
      import prs._
      DataRecordRow.tupled((<<[Int], <<[org.joda.time.LocalDateTime], <<[org.joda.time.LocalDateTime], <<[String], <<[Boolean]))
  }
  /** Table description of table data_record. Objects of this class serve as prototypes for rows in queries. */
  class DataRecord(_tableTag: Tag) extends profile.api.Table[DataRecordRow](_tableTag, Some("hat"), "data_record") {
    def * = (id, dateCreated, lastUpdated, name, deleted) <> (DataRecordRow.tupled, DataRecordRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(dateCreated), Rep.Some(lastUpdated), Rep.Some(name), Rep.Some(deleted)).shaped.<>({ r => import r._; _1.map(_ => DataRecordRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get))) }, (_: Any) => throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(serial), AutoInc, PrimaryKey */
    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    /** Database column date_created SqlType(timestamp) */
    val dateCreated: Rep[org.joda.time.LocalDateTime] = column[org.joda.time.LocalDateTime]("date_created")
    /** Database column last_updated SqlType(timestamp) */
    val lastUpdated: Rep[org.joda.time.LocalDateTime] = column[org.joda.time.LocalDateTime]("last_updated")
    /** Database column name SqlType(varchar) */
    val name: Rep[String] = column[String]("name")
    /** Database column deleted SqlType(bool), Default(false) */
    val deleted: Rep[Boolean] = column[Boolean]("deleted", O.Default(false))
  }
  /** Collection-like TableQuery object for table DataRecord */
  lazy val DataRecord = new TableQuery(tag => new DataRecord(tag))

  /**
   * Entity class storing rows of table DataStatsLog
   *  @param statsId Database column stats_id SqlType(bigserial), AutoInc, PrimaryKey
   *  @param stats Database column stats SqlType(jsonb)
   */
  case class DataStatsLogRow(statsId: Long, stats: play.api.libs.json.JsValue)
  /** GetResult implicit for fetching DataStatsLogRow objects using plain SQL queries */
  implicit def GetResultDataStatsLogRow(implicit e0: GR[Long], e1: GR[play.api.libs.json.JsValue]): GR[DataStatsLogRow] = GR {
    prs =>
      import prs._
      DataStatsLogRow.tupled((<<[Long], <<[play.api.libs.json.JsValue]))
  }
  /** Table description of table data_stats_log. Objects of this class serve as prototypes for rows in queries. */
  class DataStatsLog(_tableTag: Tag) extends profile.api.Table[DataStatsLogRow](_tableTag, Some("hat"), "data_stats_log") {
    def * = (statsId, stats) <> (DataStatsLogRow.tupled, DataStatsLogRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(statsId), Rep.Some(stats)).shaped.<>({ r => import r._; _1.map(_ => DataStatsLogRow.tupled((_1.get, _2.get))) }, (_: Any) => throw new Exception("Inserting into ? projection not supported."))

    /** Database column stats_id SqlType(bigserial), AutoInc, PrimaryKey */
    val statsId: Rep[Long] = column[Long]("stats_id", O.AutoInc, O.PrimaryKey)
    /** Database column stats SqlType(jsonb) */
    val stats: Rep[play.api.libs.json.JsValue] = column[play.api.libs.json.JsValue]("stats")
  }
  /** Collection-like TableQuery object for table DataStatsLog */
  lazy val DataStatsLog = new TableQuery(tag => new DataStatsLog(tag))

  /**
   * Entity class storing rows of table DataTable
   *  @param id Database column id SqlType(serial), AutoInc, PrimaryKey
   *  @param dateCreated Database column date_created SqlType(timestamp)
   *  @param lastUpdated Database column last_updated SqlType(timestamp)
   *  @param name Database column name SqlType(varchar)
   *  @param sourceName Database column source_name SqlType(varchar)
   *  @param deleted Database column deleted SqlType(bool), Default(false)
   */
  case class DataTableRow(id: Int, dateCreated: org.joda.time.LocalDateTime, lastUpdated: org.joda.time.LocalDateTime, name: String, sourceName: String, deleted: Boolean = false)
  /** GetResult implicit for fetching DataTableRow objects using plain SQL queries */
  implicit def GetResultDataTableRow(implicit e0: GR[Int], e1: GR[org.joda.time.LocalDateTime], e2: GR[String], e3: GR[Boolean]): GR[DataTableRow] = GR {
    prs =>
      import prs._
      DataTableRow.tupled((<<[Int], <<[org.joda.time.LocalDateTime], <<[org.joda.time.LocalDateTime], <<[String], <<[String], <<[Boolean]))
  }
  /** Table description of table data_table. Objects of this class serve as prototypes for rows in queries. */
  class DataTable(_tableTag: Tag) extends profile.api.Table[DataTableRow](_tableTag, Some("hat"), "data_table") {
    def * = (id, dateCreated, lastUpdated, name, sourceName, deleted) <> (DataTableRow.tupled, DataTableRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(dateCreated), Rep.Some(lastUpdated), Rep.Some(name), Rep.Some(sourceName), Rep.Some(deleted)).shaped.<>({ r => import r._; _1.map(_ => DataTableRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get))) }, (_: Any) => throw new Exception("Inserting into ? projection not supported."))

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
    /** Database column deleted SqlType(bool), Default(false) */
    val deleted: Rep[Boolean] = column[Boolean]("deleted", O.Default(false))

    /** Index over (name) (database name data_table_name) */
    val index1 = index("data_table_name", name)
    /** Uniqueness Index over (name,sourceName) (database name data_table_name_source) */
    val index2 = index("data_table_name_source", (name, sourceName), unique = true)
    /** Index over (sourceName) (database name data_table_source_name) */
    val index3 = index("data_table_source_name", sourceName)
  }
  /** Collection-like TableQuery object for table DataTable */
  lazy val DataTable = new TableQuery(tag => new DataTable(tag))

  /**
   * Entity class storing rows of table DataTableSize
   *  @param relation Database column relation SqlType(text), Default(None)
   *  @param totalSize Database column total_size SqlType(int8), Default(None)
   */
  case class DataTableSizeRow(relation: Option[String] = None, totalSize: Option[Long] = None)
  /** GetResult implicit for fetching DataTableSizeRow objects using plain SQL queries */
  implicit def GetResultDataTableSizeRow(implicit e0: GR[Option[String]], e1: GR[Option[Long]]): GR[DataTableSizeRow] = GR {
    prs =>
      import prs._
      DataTableSizeRow.tupled((<<?[String], <<?[Long]))
  }
  /** Table description of table data_table_size. Objects of this class serve as prototypes for rows in queries. */
  class DataTableSize(_tableTag: Tag) extends profile.api.Table[DataTableSizeRow](_tableTag, Some("hat"), "data_table_size") {
    def * = (relation, totalSize) <> (DataTableSizeRow.tupled, DataTableSizeRow.unapply)

    /** Database column relation SqlType(text), Default(None) */
    val relation: Rep[Option[String]] = column[Option[String]]("relation", O.Default(None))
    /** Database column total_size SqlType(int8), Default(None) */
    val totalSize: Rep[Option[Long]] = column[Option[Long]]("total_size", O.Default(None))
  }
  /** Collection-like TableQuery object for table DataTableSize */
  lazy val DataTableSize = new TableQuery(tag => new DataTableSize(tag))

  /**
   * Entity class storing rows of table DataTabletotablecrossref
   *  @param id Database column id SqlType(serial), AutoInc, PrimaryKey
   *  @param dateCreated Database column date_created SqlType(timestamp)
   *  @param lastUpdated Database column last_updated SqlType(timestamp)
   *  @param relationshipType Database column relationship_type SqlType(varchar)
   *  @param table1 Database column table1 SqlType(int4)
   *  @param table2 Database column table2 SqlType(int4)
   *  @param deleted Database column deleted SqlType(bool), Default(false)
   */
  case class DataTabletotablecrossrefRow(id: Int, dateCreated: org.joda.time.LocalDateTime, lastUpdated: org.joda.time.LocalDateTime, relationshipType: String, table1: Int, table2: Int, deleted: Boolean = false)
  /** GetResult implicit for fetching DataTabletotablecrossrefRow objects using plain SQL queries */
  implicit def GetResultDataTabletotablecrossrefRow(implicit e0: GR[Int], e1: GR[org.joda.time.LocalDateTime], e2: GR[String], e3: GR[Boolean]): GR[DataTabletotablecrossrefRow] = GR {
    prs =>
      import prs._
      DataTabletotablecrossrefRow.tupled((<<[Int], <<[org.joda.time.LocalDateTime], <<[org.joda.time.LocalDateTime], <<[String], <<[Int], <<[Int], <<[Boolean]))
  }
  /** Table description of table data_tabletotablecrossref. Objects of this class serve as prototypes for rows in queries. */
  class DataTabletotablecrossref(_tableTag: Tag) extends profile.api.Table[DataTabletotablecrossrefRow](_tableTag, Some("hat"), "data_tabletotablecrossref") {
    def * = (id, dateCreated, lastUpdated, relationshipType, table1, table2, deleted) <> (DataTabletotablecrossrefRow.tupled, DataTabletotablecrossrefRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(dateCreated), Rep.Some(lastUpdated), Rep.Some(relationshipType), Rep.Some(table1), Rep.Some(table2), Rep.Some(deleted)).shaped.<>({ r => import r._; _1.map(_ => DataTabletotablecrossrefRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get, _7.get))) }, (_: Any) => throw new Exception("Inserting into ? projection not supported."))

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
    /** Database column deleted SqlType(bool), Default(false) */
    val deleted: Rep[Boolean] = column[Boolean]("deleted", O.Default(false))

    /** Foreign key referencing DataTable (database name data_table_data_tabletotablecrossref_fk) */
    lazy val dataTableFk1 = foreignKey("data_table_data_tabletotablecrossref_fk", table2, DataTable)(r => r.id, onUpdate = ForeignKeyAction.NoAction, onDelete = ForeignKeyAction.NoAction)
    /** Foreign key referencing DataTable (database name data_table_data_tabletotablecrossref_fk1) */
    lazy val dataTableFk2 = foreignKey("data_table_data_tabletotablecrossref_fk1", table1, DataTable)(r => r.id, onUpdate = ForeignKeyAction.NoAction, onDelete = ForeignKeyAction.NoAction)
  }
  /** Collection-like TableQuery object for table DataTabletotablecrossref */
  lazy val DataTabletotablecrossref = new TableQuery(tag => new DataTabletotablecrossref(tag))

  /**
   * Entity class storing rows of table DataTableTree
   *  @param id Database column id SqlType(int4), Default(None)
   *  @param dateCreated Database column date_created SqlType(timestamp), Default(None)
   *  @param lastUpdated Database column last_updated SqlType(timestamp), Default(None)
   *  @param name Database column name SqlType(varchar), Default(None)
   *  @param sourceName Database column source_name SqlType(varchar), Default(None)
   *  @param deleted Database column deleted SqlType(bool), Default(None)
   *  @param table1 Database column table1 SqlType(int4), Default(None)
   *  @param path Database column path SqlType(_int4), Default(None)
   *  @param rootTable Database column root_table SqlType(int4), Default(None)
   */
  case class DataTableTreeRow(id: Option[Int] = None, dateCreated: Option[org.joda.time.LocalDateTime] = None, lastUpdated: Option[org.joda.time.LocalDateTime] = None, name: Option[String] = None, sourceName: Option[String] = None, deleted: Option[Boolean] = None, table1: Option[Int] = None, path: Option[List[Int]] = None, rootTable: Option[Int] = None)
  /** GetResult implicit for fetching DataTableTreeRow objects using plain SQL queries */
  implicit def GetResultDataTableTreeRow(implicit e0: GR[Option[Int]], e1: GR[Option[org.joda.time.LocalDateTime]], e2: GR[Option[String]], e3: GR[Option[Boolean]], e4: GR[Option[List[Int]]]): GR[DataTableTreeRow] = GR {
    prs =>
      import prs._
      DataTableTreeRow.tupled((<<?[Int], <<?[org.joda.time.LocalDateTime], <<?[org.joda.time.LocalDateTime], <<?[String], <<?[String], <<?[Boolean], <<?[Int], <<?[List[Int]], <<?[Int]))
  }
  /** Table description of table data_table_tree. Objects of this class serve as prototypes for rows in queries. */
  class DataTableTree(_tableTag: Tag) extends profile.api.Table[DataTableTreeRow](_tableTag, Some("hat"), "data_table_tree") {
    def * = (id, dateCreated, lastUpdated, name, sourceName, deleted, table1, path, rootTable) <> (DataTableTreeRow.tupled, DataTableTreeRow.unapply)

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
    /** Database column deleted SqlType(bool), Default(None) */
    val deleted: Rep[Option[Boolean]] = column[Option[Boolean]]("deleted", O.Default(None))
    /** Database column table1 SqlType(int4), Default(None) */
    val table1: Rep[Option[Int]] = column[Option[Int]]("table1", O.Default(None))
    /** Database column path SqlType(_int4), Default(None) */
    val path: Rep[Option[List[Int]]] = column[Option[List[Int]]]("path", O.Default(None))
    /** Database column root_table SqlType(int4), Default(None) */
    val rootTable: Rep[Option[Int]] = column[Option[Int]]("root_table", O.Default(None))
  }
  /** Collection-like TableQuery object for table DataTableTree */
  lazy val DataTableTree = new TableQuery(tag => new DataTableTree(tag))

  /**
   * Entity class storing rows of table DataValue
   *  @param id Database column id SqlType(serial), AutoInc, PrimaryKey
   *  @param dateCreated Database column date_created SqlType(timestamp)
   *  @param lastUpdated Database column last_updated SqlType(timestamp)
   *  @param value Database column value SqlType(text)
   *  @param fieldId Database column field_id SqlType(int4)
   *  @param recordId Database column record_id SqlType(int4)
   *  @param deleted Database column deleted SqlType(bool), Default(false)
   */
  case class DataValueRow(id: Int, dateCreated: org.joda.time.LocalDateTime, lastUpdated: org.joda.time.LocalDateTime, value: String, fieldId: Int, recordId: Int, deleted: Boolean = false)
  /** GetResult implicit for fetching DataValueRow objects using plain SQL queries */
  implicit def GetResultDataValueRow(implicit e0: GR[Int], e1: GR[org.joda.time.LocalDateTime], e2: GR[String], e3: GR[Boolean]): GR[DataValueRow] = GR {
    prs =>
      import prs._
      DataValueRow.tupled((<<[Int], <<[org.joda.time.LocalDateTime], <<[org.joda.time.LocalDateTime], <<[String], <<[Int], <<[Int], <<[Boolean]))
  }
  /** Table description of table data_value. Objects of this class serve as prototypes for rows in queries. */
  class DataValue(_tableTag: Tag) extends profile.api.Table[DataValueRow](_tableTag, Some("hat"), "data_value") {
    def * = (id, dateCreated, lastUpdated, value, fieldId, recordId, deleted) <> (DataValueRow.tupled, DataValueRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(dateCreated), Rep.Some(lastUpdated), Rep.Some(value), Rep.Some(fieldId), Rep.Some(recordId), Rep.Some(deleted)).shaped.<>({ r => import r._; _1.map(_ => DataValueRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get, _7.get))) }, (_: Any) => throw new Exception("Inserting into ? projection not supported."))

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
    /** Database column deleted SqlType(bool), Default(false) */
    val deleted: Rep[Boolean] = column[Boolean]("deleted", O.Default(false))

    /** Foreign key referencing DataField (database name data_field_data_value_fk) */
    lazy val dataFieldFk = foreignKey("data_field_data_value_fk", fieldId, DataField)(r => r.id, onUpdate = ForeignKeyAction.NoAction, onDelete = ForeignKeyAction.NoAction)
    /** Foreign key referencing DataRecord (database name data_record_data_value_fk) */
    lazy val dataRecordFk = foreignKey("data_record_data_value_fk", recordId, DataRecord)(r => r.id, onUpdate = ForeignKeyAction.NoAction, onDelete = ForeignKeyAction.NoAction)
  }
  /** Collection-like TableQuery object for table DataValue */
  lazy val DataValue = new TableQuery(tag => new DataValue(tag))

  /**
   * Entity class storing rows of table HatFile
   *  @param id Database column id SqlType(varchar), PrimaryKey
   *  @param name Database column name SqlType(varchar)
   *  @param source Database column source SqlType(varchar)
   *  @param dateCreated Database column date_created SqlType(timestamp)
   *  @param lastUpdated Database column last_updated SqlType(timestamp)
   *  @param tags Database column tags SqlType(_text), Default(None)
   *  @param title Database column title SqlType(varchar), Default(None)
   *  @param description Database column description SqlType(varchar), Default(None)
   *  @param sourceUrl Database column source_url SqlType(varchar), Default(None)
   *  @param status Database column status SqlType(jsonb)
   *  @param contentPublic Database column content_public SqlType(bool), Default(false)
   */
  case class HatFileRow(id: String, name: String, source: String, dateCreated: org.joda.time.LocalDateTime, lastUpdated: org.joda.time.LocalDateTime, tags: Option[List[String]] = None, title: Option[String] = None, description: Option[String] = None, sourceUrl: Option[String] = None, status: play.api.libs.json.JsValue, contentPublic: Boolean = false)
  /** GetResult implicit for fetching HatFileRow objects using plain SQL queries */
  implicit def GetResultHatFileRow(implicit e0: GR[String], e1: GR[org.joda.time.LocalDateTime], e2: GR[Option[List[String]]], e3: GR[Option[String]], e4: GR[play.api.libs.json.JsValue], e5: GR[Boolean]): GR[HatFileRow] = GR {
    prs =>
      import prs._
      HatFileRow.tupled((<<[String], <<[String], <<[String], <<[org.joda.time.LocalDateTime], <<[org.joda.time.LocalDateTime], <<?[List[String]], <<?[String], <<?[String], <<?[String], <<[play.api.libs.json.JsValue], <<[Boolean]))
  }
  /** Table description of table hat_file. Objects of this class serve as prototypes for rows in queries. */
  class HatFile(_tableTag: Tag) extends profile.api.Table[HatFileRow](_tableTag, Some("hat"), "hat_file") {
    def * = (id, name, source, dateCreated, lastUpdated, tags, title, description, sourceUrl, status, contentPublic) <> (HatFileRow.tupled, HatFileRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(name), Rep.Some(source), Rep.Some(dateCreated), Rep.Some(lastUpdated), tags, title, description, sourceUrl, Rep.Some(status), Rep.Some(contentPublic)).shaped.<>({ r => import r._; _1.map(_ => HatFileRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6, _7, _8, _9, _10.get, _11.get))) }, (_: Any) => throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(varchar), PrimaryKey */
    val id: Rep[String] = column[String]("id", O.PrimaryKey)
    /** Database column name SqlType(varchar) */
    val name: Rep[String] = column[String]("name")
    /** Database column source SqlType(varchar) */
    val source: Rep[String] = column[String]("source")
    /** Database column date_created SqlType(timestamp) */
    val dateCreated: Rep[org.joda.time.LocalDateTime] = column[org.joda.time.LocalDateTime]("date_created")
    /** Database column last_updated SqlType(timestamp) */
    val lastUpdated: Rep[org.joda.time.LocalDateTime] = column[org.joda.time.LocalDateTime]("last_updated")
    /** Database column tags SqlType(_text), Default(None) */
    val tags: Rep[Option[List[String]]] = column[Option[List[String]]]("tags", O.Default(None))
    /** Database column title SqlType(varchar), Default(None) */
    val title: Rep[Option[String]] = column[Option[String]]("title", O.Default(None))
    /** Database column description SqlType(varchar), Default(None) */
    val description: Rep[Option[String]] = column[Option[String]]("description", O.Default(None))
    /** Database column source_url SqlType(varchar), Default(None) */
    val sourceUrl: Rep[Option[String]] = column[Option[String]]("source_url", O.Default(None))
    /** Database column status SqlType(jsonb) */
    val status: Rep[play.api.libs.json.JsValue] = column[play.api.libs.json.JsValue]("status")
    /** Database column content_public SqlType(bool), Default(false) */
    val contentPublic: Rep[Boolean] = column[Boolean]("content_public", O.Default(false))
  }
  /** Collection-like TableQuery object for table HatFile */
  lazy val HatFile = new TableQuery(tag => new HatFile(tag))

  /**
   * Entity class storing rows of table HatFileAccess
   *  @param fileId Database column file_id SqlType(varchar)
   *  @param userId Database column user_id SqlType(uuid)
   *  @param content Database column content SqlType(bool), Default(false)
   */
  case class HatFileAccessRow(fileId: String, userId: java.util.UUID, content: Boolean = false)
  /** GetResult implicit for fetching HatFileAccessRow objects using plain SQL queries */
  implicit def GetResultHatFileAccessRow(implicit e0: GR[String], e1: GR[java.util.UUID], e2: GR[Boolean]): GR[HatFileAccessRow] = GR {
    prs =>
      import prs._
      HatFileAccessRow.tupled((<<[String], <<[java.util.UUID], <<[Boolean]))
  }
  /** Table description of table hat_file_access. Objects of this class serve as prototypes for rows in queries. */
  class HatFileAccess(_tableTag: Tag) extends profile.api.Table[HatFileAccessRow](_tableTag, Some("hat"), "hat_file_access") {
    def * = (fileId, userId, content) <> (HatFileAccessRow.tupled, HatFileAccessRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(fileId), Rep.Some(userId), Rep.Some(content)).shaped.<>({ r => import r._; _1.map(_ => HatFileAccessRow.tupled((_1.get, _2.get, _3.get))) }, (_: Any) => throw new Exception("Inserting into ? projection not supported."))

    /** Database column file_id SqlType(varchar) */
    val fileId: Rep[String] = column[String]("file_id")
    /** Database column user_id SqlType(uuid) */
    val userId: Rep[java.util.UUID] = column[java.util.UUID]("user_id")
    /** Database column content SqlType(bool), Default(false) */
    val content: Rep[Boolean] = column[Boolean]("content", O.Default(false))

    /** Primary key of HatFileAccess (database name hat_file_access_pkey) */
    val pk = primaryKey("hat_file_access_pkey", (fileId, userId))

    /** Foreign key referencing HatFile (database name hat_file_access_file_id_fkey) */
    lazy val hatFileFk = foreignKey("hat_file_access_file_id_fkey", fileId, HatFile)(r => r.id, onUpdate = ForeignKeyAction.NoAction, onDelete = ForeignKeyAction.NoAction)
    /** Foreign key referencing UserUser (database name hat_file_access_user_id_fkey) */
    lazy val userUserFk = foreignKey("hat_file_access_user_id_fkey", userId, UserUser)(r => r.userId, onUpdate = ForeignKeyAction.NoAction, onDelete = ForeignKeyAction.NoAction)
  }
  /** Collection-like TableQuery object for table HatFileAccess */
  lazy val HatFileAccess = new TableQuery(tag => new HatFileAccess(tag))

  /**
   * Entity class storing rows of table SheFunction
   *  @param name Database column name SqlType(varchar), PrimaryKey
   *  @param description Database column description SqlType(varchar)
   *  @param trigger Database column trigger SqlType(jsonb)
   *  @param enabled Database column enabled SqlType(bool)
   *  @param bundleId Database column bundle_id SqlType(varchar)
   *  @param lastExecution Database column last_execution SqlType(timestamptz), Default(None)
   */
  case class SheFunctionRow(name: String, description: String, trigger: play.api.libs.json.JsValue, enabled: Boolean, bundleId: String, lastExecution: Option[org.joda.time.DateTime] = None)
  /** GetResult implicit for fetching SheFunctionRow objects using plain SQL queries */
  implicit def GetResultSheFunctionRow(implicit e0: GR[String], e1: GR[play.api.libs.json.JsValue], e2: GR[Boolean], e3: GR[Option[org.joda.time.DateTime]]): GR[SheFunctionRow] = GR {
    prs =>
      import prs._
      SheFunctionRow.tupled((<<[String], <<[String], <<[play.api.libs.json.JsValue], <<[Boolean], <<[String], <<?[org.joda.time.DateTime]))
  }
  /** Table description of table she_function. Objects of this class serve as prototypes for rows in queries. */
  class SheFunction(_tableTag: Tag) extends profile.api.Table[SheFunctionRow](_tableTag, Some("hat"), "she_function") {
    def * = (name, description, trigger, enabled, bundleId, lastExecution) <> (SheFunctionRow.tupled, SheFunctionRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(name), Rep.Some(description), Rep.Some(trigger), Rep.Some(enabled), Rep.Some(bundleId), lastExecution).shaped.<>({ r => import r._; _1.map(_ => SheFunctionRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6))) }, (_: Any) => throw new Exception("Inserting into ? projection not supported."))

    /** Database column name SqlType(varchar), PrimaryKey */
    val name: Rep[String] = column[String]("name", O.PrimaryKey)
    /** Database column description SqlType(varchar) */
    val description: Rep[String] = column[String]("description")
    /** Database column trigger SqlType(jsonb) */
    val trigger: Rep[play.api.libs.json.JsValue] = column[play.api.libs.json.JsValue]("trigger")
    /** Database column enabled SqlType(bool) */
    val enabled: Rep[Boolean] = column[Boolean]("enabled")
    /** Database column bundle_id SqlType(varchar) */
    val bundleId: Rep[String] = column[String]("bundle_id")
    /** Database column last_execution SqlType(timestamptz), Default(None) */
    val lastExecution: Rep[Option[org.joda.time.DateTime]] = column[Option[org.joda.time.DateTime]]("last_execution", O.Default(None))

    /** Foreign key referencing DataBundles (database name she_function_bundle_id_fkey) */
    lazy val dataBundlesFk = foreignKey("she_function_bundle_id_fkey", bundleId, DataBundles)(r => r.bundleId, onUpdate = ForeignKeyAction.NoAction, onDelete = ForeignKeyAction.NoAction)
  }
  /** Collection-like TableQuery object for table SheFunction */
  lazy val SheFunction = new TableQuery(tag => new SheFunction(tag))

  /**
   * Entity class storing rows of table SystemEventlog
   *  @param id Database column id SqlType(serial), AutoInc, PrimaryKey
   *  @param eventType Database column event_type SqlType(varchar), Length(45,true)
   *  @param date Database column date SqlType(date)
   *  @param time Database column time SqlType(time)
   *  @param creator Database column creator SqlType(varchar), Length(100,true)
   *  @param command Database column command SqlType(varchar), Length(100,true)
   *  @param result Database column result SqlType(varchar), Length(45,true)
   */
  case class SystemEventlogRow(id: Int, eventType: String, date: org.joda.time.LocalDate, time: org.joda.time.LocalTime, creator: String, command: String, result: String)
  /** GetResult implicit for fetching SystemEventlogRow objects using plain SQL queries */
  implicit def GetResultSystemEventlogRow(implicit e0: GR[Int], e1: GR[String], e2: GR[org.joda.time.LocalDate], e3: GR[org.joda.time.LocalTime]): GR[SystemEventlogRow] = GR {
    prs =>
      import prs._
      SystemEventlogRow.tupled((<<[Int], <<[String], <<[org.joda.time.LocalDate], <<[org.joda.time.LocalTime], <<[String], <<[String], <<[String]))
  }
  /** Table description of table system_eventlog. Objects of this class serve as prototypes for rows in queries. */
  class SystemEventlog(_tableTag: Tag) extends profile.api.Table[SystemEventlogRow](_tableTag, Some("hat"), "system_eventlog") {
    def * = (id, eventType, date, time, creator, command, result) <> (SystemEventlogRow.tupled, SystemEventlogRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(eventType), Rep.Some(date), Rep.Some(time), Rep.Some(creator), Rep.Some(command), Rep.Some(result)).shaped.<>({ r => import r._; _1.map(_ => SystemEventlogRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get, _7.get))) }, (_: Any) => throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(serial), AutoInc, PrimaryKey */
    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    /** Database column event_type SqlType(varchar), Length(45,true) */
    val eventType: Rep[String] = column[String]("event_type", O.Length(45, varying = true))
    /** Database column date SqlType(date) */
    val date: Rep[org.joda.time.LocalDate] = column[org.joda.time.LocalDate]("date")
    /** Database column time SqlType(time) */
    val time: Rep[org.joda.time.LocalTime] = column[org.joda.time.LocalTime]("time")
    /** Database column creator SqlType(varchar), Length(100,true) */
    val creator: Rep[String] = column[String]("creator", O.Length(100, varying = true))
    /** Database column command SqlType(varchar), Length(100,true) */
    val command: Rep[String] = column[String]("command", O.Length(100, varying = true))
    /** Database column result SqlType(varchar), Length(45,true) */
    val result: Rep[String] = column[String]("result", O.Length(45, varying = true))
  }
  /** Collection-like TableQuery object for table SystemEventlog */
  lazy val SystemEventlog = new TableQuery(tag => new SystemEventlog(tag))

  /**
   * Entity class storing rows of table UserAccessLog
   *  @param date Database column date SqlType(timestamp)
   *  @param userId Database column user_id SqlType(uuid)
   *  @param `type` Database column type SqlType(varchar)
   *  @param scope Database column scope SqlType(varchar)
   *  @param applicationName Database column application_name SqlType(varchar), Default(None)
   *  @param applicationResource Database column application_resource SqlType(varchar), Default(None)
   */
  case class UserAccessLogRow(date: org.joda.time.LocalDateTime, userId: java.util.UUID, `type`: String, scope: String, applicationName: Option[String] = None, applicationResource: Option[String] = None)
  /** GetResult implicit for fetching UserAccessLogRow objects using plain SQL queries */
  implicit def GetResultUserAccessLogRow(implicit e0: GR[org.joda.time.LocalDateTime], e1: GR[java.util.UUID], e2: GR[String], e3: GR[Option[String]]): GR[UserAccessLogRow] = GR {
    prs =>
      import prs._
      UserAccessLogRow.tupled((<<[org.joda.time.LocalDateTime], <<[java.util.UUID], <<[String], <<[String], <<?[String], <<?[String]))
  }
  /**
   * Table description of table user_access_log. Objects of this class serve as prototypes for rows in queries.
   *  NOTE: The following names collided with Scala keywords and were escaped: type
   */
  class UserAccessLog(_tableTag: Tag) extends profile.api.Table[UserAccessLogRow](_tableTag, Some("hat"), "user_access_log") {
    def * = (date, userId, `type`, scope, applicationName, applicationResource) <> (UserAccessLogRow.tupled, UserAccessLogRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(date), Rep.Some(userId), Rep.Some(`type`), Rep.Some(scope), applicationName, applicationResource).shaped.<>({ r => import r._; _1.map(_ => UserAccessLogRow.tupled((_1.get, _2.get, _3.get, _4.get, _5, _6))) }, (_: Any) => throw new Exception("Inserting into ? projection not supported."))

    /** Database column date SqlType(timestamp) */
    val date: Rep[org.joda.time.LocalDateTime] = column[org.joda.time.LocalDateTime]("date")
    /** Database column user_id SqlType(uuid) */
    val userId: Rep[java.util.UUID] = column[java.util.UUID]("user_id")
    /**
     * Database column type SqlType(varchar)
     *  NOTE: The name was escaped because it collided with a Scala keyword.
     */
    val `type`: Rep[String] = column[String]("type")
    /** Database column scope SqlType(varchar) */
    val scope: Rep[String] = column[String]("scope")
    /** Database column application_name SqlType(varchar), Default(None) */
    val applicationName: Rep[Option[String]] = column[Option[String]]("application_name", O.Default(None))
    /** Database column application_resource SqlType(varchar), Default(None) */
    val applicationResource: Rep[Option[String]] = column[Option[String]]("application_resource", O.Default(None))

    /** Foreign key referencing UserUser (database name user_access_log_user_id_fkey) */
    lazy val userUserFk = foreignKey("user_access_log_user_id_fkey", userId, UserUser)(r => r.userId, onUpdate = ForeignKeyAction.NoAction, onDelete = ForeignKeyAction.NoAction)
  }
  /** Collection-like TableQuery object for table UserAccessLog */
  lazy val UserAccessLog = new TableQuery(tag => new UserAccessLog(tag))

  /**
   * Entity class storing rows of table UserMailTokens
   *  @param id Database column id SqlType(varchar), PrimaryKey
   *  @param email Database column email SqlType(varchar)
   *  @param expirationTime Database column expiration_time SqlType(timestamp)
   *  @param isSignup Database column is_signup SqlType(bool)
   */
  case class UserMailTokensRow(id: String, email: String, expirationTime: org.joda.time.LocalDateTime, isSignup: Boolean)
  /** GetResult implicit for fetching UserMailTokensRow objects using plain SQL queries */
  implicit def GetResultUserMailTokensRow(implicit e0: GR[String], e1: GR[org.joda.time.LocalDateTime], e2: GR[Boolean]): GR[UserMailTokensRow] = GR {
    prs =>
      import prs._
      UserMailTokensRow.tupled((<<[String], <<[String], <<[org.joda.time.LocalDateTime], <<[Boolean]))
  }
  /** Table description of table user_mail_tokens. Objects of this class serve as prototypes for rows in queries. */
  class UserMailTokens(_tableTag: Tag) extends profile.api.Table[UserMailTokensRow](_tableTag, Some("hat"), "user_mail_tokens") {
    def * = (id, email, expirationTime, isSignup) <> (UserMailTokensRow.tupled, UserMailTokensRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(email), Rep.Some(expirationTime), Rep.Some(isSignup)).shaped.<>({ r => import r._; _1.map(_ => UserMailTokensRow.tupled((_1.get, _2.get, _3.get, _4.get))) }, (_: Any) => throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(varchar), PrimaryKey */
    val id: Rep[String] = column[String]("id", O.PrimaryKey)
    /** Database column email SqlType(varchar) */
    val email: Rep[String] = column[String]("email")
    /** Database column expiration_time SqlType(timestamp) */
    val expirationTime: Rep[org.joda.time.LocalDateTime] = column[org.joda.time.LocalDateTime]("expiration_time")
    /** Database column is_signup SqlType(bool) */
    val isSignup: Rep[Boolean] = column[Boolean]("is_signup")
  }
  /** Collection-like TableQuery object for table UserMailTokens */
  lazy val UserMailTokens = new TableQuery(tag => new UserMailTokens(tag))

  /**
   * Entity class storing rows of table UserRole
   *  @param userId Database column user_id SqlType(uuid)
   *  @param role Database column role SqlType(varchar)
   *  @param extra Database column extra SqlType(varchar), Default(None)
   */
  case class UserRoleRow(userId: java.util.UUID, role: String, extra: Option[String] = None)
  /** GetResult implicit for fetching UserRoleRow objects using plain SQL queries */
  implicit def GetResultUserRoleRow(implicit e0: GR[java.util.UUID], e1: GR[String], e2: GR[Option[String]]): GR[UserRoleRow] = GR {
    prs =>
      import prs._
      UserRoleRow.tupled((<<[java.util.UUID], <<[String], <<?[String]))
  }
  /** Table description of table user_role. Objects of this class serve as prototypes for rows in queries. */
  class UserRole(_tableTag: Tag) extends profile.api.Table[UserRoleRow](_tableTag, Some("hat"), "user_role") {
    def * = (userId, role, extra) <> (UserRoleRow.tupled, UserRoleRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(userId), Rep.Some(role), extra).shaped.<>({ r => import r._; _1.map(_ => UserRoleRow.tupled((_1.get, _2.get, _3))) }, (_: Any) => throw new Exception("Inserting into ? projection not supported."))

    /** Database column user_id SqlType(uuid) */
    val userId: Rep[java.util.UUID] = column[java.util.UUID]("user_id")
    /** Database column role SqlType(varchar) */
    val role: Rep[String] = column[String]("role")
    /** Database column extra SqlType(varchar), Default(None) */
    val extra: Rep[Option[String]] = column[Option[String]]("extra", O.Default(None))

    /** Foreign key referencing UserRoleAvailable (database name user_role_role_fkey) */
    lazy val userRoleAvailableFk = foreignKey("user_role_role_fkey", role, UserRoleAvailable)(r => r.name, onUpdate = ForeignKeyAction.NoAction, onDelete = ForeignKeyAction.NoAction)
    /** Foreign key referencing UserUser (database name user_role_user_id_fkey) */
    lazy val userUserFk = foreignKey("user_role_user_id_fkey", userId, UserUser)(r => r.userId, onUpdate = ForeignKeyAction.NoAction, onDelete = ForeignKeyAction.NoAction)
  }
  /** Collection-like TableQuery object for table UserRole */
  lazy val UserRole = new TableQuery(tag => new UserRole(tag))

  /**
   * Entity class storing rows of table UserRoleAvailable
   *  @param name Database column name SqlType(varchar), PrimaryKey
   */
  case class UserRoleAvailableRow(name: String)
  /** GetResult implicit for fetching UserRoleAvailableRow objects using plain SQL queries */
  implicit def GetResultUserRoleAvailableRow(implicit e0: GR[String]): GR[UserRoleAvailableRow] = GR {
    prs =>
      import prs._
      UserRoleAvailableRow(<<[String])
  }
  /** Table description of table user_role_available. Objects of this class serve as prototypes for rows in queries. */
  class UserRoleAvailable(_tableTag: Tag) extends profile.api.Table[UserRoleAvailableRow](_tableTag, Some("hat"), "user_role_available") {
    def * = name <> (UserRoleAvailableRow, UserRoleAvailableRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = Rep.Some(name).shaped.<>(r => r.map(_ => UserRoleAvailableRow(r.get)), (_: Any) => throw new Exception("Inserting into ? projection not supported."))

    /** Database column name SqlType(varchar), PrimaryKey */
    val name: Rep[String] = column[String]("name", O.PrimaryKey)
  }
  /** Collection-like TableQuery object for table UserRoleAvailable */
  lazy val UserRoleAvailable = new TableQuery(tag => new UserRoleAvailable(tag))

  /**
   * Entity class storing rows of table UserUser
   *  @param userId Database column user_id SqlType(uuid), PrimaryKey
   *  @param dateCreated Database column date_created SqlType(timestamp)
   *  @param lastUpdated Database column last_updated SqlType(timestamp)
   *  @param email Database column email SqlType(varchar)
   *  @param pass Database column pass SqlType(varchar), Default(None)
   *  @param name Database column name SqlType(varchar)
   *  @param enabled Database column enabled SqlType(bool), Default(false)
   */
  case class UserUserRow(userId: java.util.UUID, dateCreated: org.joda.time.LocalDateTime, lastUpdated: org.joda.time.LocalDateTime, email: String, pass: Option[String] = None, name: String, enabled: Boolean = false)
  /** GetResult implicit for fetching UserUserRow objects using plain SQL queries */
  implicit def GetResultUserUserRow(implicit e0: GR[java.util.UUID], e1: GR[org.joda.time.LocalDateTime], e2: GR[String], e3: GR[Option[String]], e4: GR[Boolean]): GR[UserUserRow] = GR {
    prs =>
      import prs._
      UserUserRow.tupled((<<[java.util.UUID], <<[org.joda.time.LocalDateTime], <<[org.joda.time.LocalDateTime], <<[String], <<?[String], <<[String], <<[Boolean]))
  }
  /** Table description of table user_user. Objects of this class serve as prototypes for rows in queries. */
  class UserUser(_tableTag: Tag) extends profile.api.Table[UserUserRow](_tableTag, Some("hat"), "user_user") {
    def * = (userId, dateCreated, lastUpdated, email, pass, name, enabled) <> (UserUserRow.tupled, UserUserRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(userId), Rep.Some(dateCreated), Rep.Some(lastUpdated), Rep.Some(email), pass, Rep.Some(name), Rep.Some(enabled)).shaped.<>({ r => import r._; _1.map(_ => UserUserRow.tupled((_1.get, _2.get, _3.get, _4.get, _5, _6.get, _7.get))) }, (_: Any) => throw new Exception("Inserting into ? projection not supported."))

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
    /** Database column enabled SqlType(bool), Default(false) */
    val enabled: Rep[Boolean] = column[Boolean]("enabled", O.Default(false))
  }
  /** Collection-like TableQuery object for table UserUser */
  lazy val UserUser = new TableQuery(tag => new UserUser(tag))
}
