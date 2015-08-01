package dalapi

import akka.actor.ActorLogging
import com.wordnik.swagger.annotations._
import dal.Tables._
import dal.SlickPostgresDriver.simple._
import dalapi.models.{ApiDataTable, ApiDataRecord, ApiDataField, ApiDataValue}
import org.joda.time.LocalDateTime
import spray.http.MediaTypes._
import spray.json._
import spray.routing._
import spray.httpx.SprayJsonSupport._
import spray.util.LoggingContext
import spray.http.StatusCodes._
import com.typesafe.config.{Config, ConfigFactory}
import scala.annotation.meta.field
import scala.concurrent.ExecutionContext.Implicits.global
import dalapi.models._
import akka.event.Logging

// this trait defines our service behavior independently from the service actor
trait DataService extends HttpService with InboundService {

  val routes = { pathPrefix("data") {
      createTable ~
        addTableToTable ~
        createField ~
        createRecord ~
        createValue ~
        storeValueList ~
        getField ~
        getFieldValues ~
        getTable ~
        getRecord ~
        getRecordValues ~
        getValue
    }
  }

  import ApiJsonProtocol._

  /*
   * Creates a new virtual table for storing arbitrary incoming data
   */
  def createTable = path("table") {
    post {
      entity(as[ApiDataTable]) { table =>
        db.withSession { implicit session =>
          val newTable = new DataTableRow(0, LocalDateTime.now(), LocalDateTime.now(), table.name, table.source)
          val tableId = (DataTable returning DataTable.map(_.id)) += newTable

          complete(Created, {
            constructTableStructure(tableId)
          })
        }
      }

    }
  }

  /*
   * Marks provided table as a "child" of another, e.g. to created nested data structured
   */
  def addTableToTable = path("table" / IntNumber / "table" / IntNumber) {
    (parentId: Int, childId: Int) =>
      post {
        entity(as[ApiRelationship]) { relationship =>
          db.withSession { implicit session =>
            val dataTableToTableCrossRefRow = new DataTabletotablecrossrefRow(
              1, LocalDateTime.now(), LocalDateTime.now(),
              relationship.relationshipType, parentId, childId
            )
            val id = (DataTabletotablecrossref returning DataTabletotablecrossref.map(_.id)) += dataTableToTableCrossRefRow

            complete {
              ApiGenericId(id)
            }
          }
        }
      }
  }

  /*
   * Get specific table information. Includes all fields and sub-tables
   */
  def getTable = path("table" / IntNumber) {
    (tableId: Int) =>
      get {
        complete {
          constructTableStructure(tableId)
        }
      }
  }

  /*
   * Create a new field in a virtual table
   */
  def createField = path("field") {
    post {
      entity(as[ApiDataField]) { field =>
        db.withSession { implicit session =>
          val newField = new DataFieldRow(0, LocalDateTime.now(), LocalDateTime.now(), field.name, field.tableId)
          val id = (DataField returning DataField.map(_.id)) += newField
          complete {
            field.copy(id = Some(id))
          }
        }
      }
    }
  }

  /*
   * Get field (information only) by ID
   */
  def getField = path("field" / IntNumber){ (fieldId: Int) =>
    get {
      complete {
        retrieveDataFieldId(fieldId)
      }
    }
  }

  /*
   * Get data stored in a specific field.
   * Returns all Data Values stored in the field
   */
  def getFieldValues = path("field" / IntNumber / "values") { (fieldId: Int) =>
    get {
      db.withSession { implicit session =>
        val field = retrieveDataFieldId(fieldId)
        val values = DataValue.filter(_.fieldId === fieldId).run

        // Translate all DB values to ApiDataValue
        val apiDataValues = values.map { value =>
          new ApiDataValue(
            Some(value.id), Some(value.dateCreated), Some(value.lastUpdated),
            value.value, value.fieldId, value.recordId
          )
        }

        complete {
          field.copy(values = Some(apiDataValues))
        }
      }
    }
  }

  /*
   * Insert a new, potentially named, data record
   */
  def createRecord = path("record") {
    post {
      entity(as[ApiDataRecord]) { record =>
        db.withSession { implicit session =>
          val newRecord = new DataRecordRow(0, LocalDateTime.now(), LocalDateTime.now(), record.name)
          val recordId = (DataRecord returning DataRecord.map(_.id)) += newRecord
          complete {
            record.copy(id = Some(recordId))
          }
        }
      }
    }
  }

  /*
   * Get record
   */
  def getRecord = path("record" / IntNumber) { (recordId: Int) =>
    get {
      db.withSession { implicit session =>
        val record = DataRecord.filter(_.id === recordId).run.head

        complete {
          ApiDataRecord(Some(record.id), Some(record.dateCreated), Some(record.lastUpdated), record.name, None)
        }
      }
    }
  }

  /*
   * Get values associated with a record.
   * Constructs a hierarchy of fields and data within each field for the record
   * FIXME: flat hierarchy for now
   */
  def getRecordValues = path("record" / IntNumber / "values") { (recordId: Int) =>
    get {
      db.withSession { implicit session =>
        val fieldValues = (DataValue.filter(_.recordId === recordId) join DataField on (_.fieldId === _.id)).run

        val apiDataValues = fieldValues.map { case ((value, field)) =>
          val dataValue = new ApiDataValue(Some(value.id), Some(value.dateCreated), Some(value.lastUpdated), value.value, value.fieldId, value.recordId)
          new ApiDataField(Some(field.id), Some(field.dateCreated), Some(field.lastUpdated), field.tableIdFk, field.name, Some(Seq(dataValue)))
        }

        val record = DataRecord.filter(_.id === recordId).run.head
        val apiRecord = new ApiDataRecord(Some(record.id), Some(record.dateCreated), Some(record.lastUpdated), record.name, Some(apiDataValues))
        complete {
          apiRecord
        }
      }
    }
  }

  /*
   * Create (insert) a new data value
   */
  def createValue = path("value") {
    post {
      entity(as[ApiDataValue]) { value =>
        db.withSession { implicit session =>
          val newValue = new DataValueRow(0, LocalDateTime.now(), LocalDateTime.now(), value.value, value.fieldId, value.recordId)
          val id = (DataValue returning DataValue.map(_.id)) += newValue

          complete {
            value.copy(id = Some(id))
          }
        }
      }
    }
  }

  /*
   * Retrieve a data value by ID
   */
  def getValue = path("value" / IntNumber) {  (valueId: Int) =>
    get {
      db.withSession { implicit session =>
        val value = DataValue.filter(_.id === valueId).run.head
        val apiValue = new ApiDataValue(Some(value.id), Some(value.dateCreated), Some(value.lastUpdated), value.value, value.fieldId, value.recordId)

        complete {
          apiValue
        }
      }
    }

  }

  /*
   * Batch-insert data values as a list
   */
  def storeValueList = path("value" / "list") {
    post {
      entity(as[Seq[ApiDataValue]]) { values =>
        db.withSession { implicit session =>
          val dataValues = values.map { value =>
            DataValueRow(0, LocalDateTime.now(), LocalDateTime.now(), value.value, value.fieldId, value.recordId)
          }

          val insertedValues = (DataValue returning DataValue) ++= dataValues

          val returning = insertedValues.map { value =>
            new ApiDataValue(Some(value.id), Some(value.dateCreated), Some(value.lastUpdated), value.value, value.fieldId, value.recordId)
          }

          complete {
            returning
          }
        }
      }
    }
  }

  /*
   * Recursively construct nested DataTable records with associated fields and sub-tables
   */
  private def constructTableStructure(tableId: Int): ApiDataTable = {
    db.withSession { implicit session =>
      val table = DataTable.filter(_.id === tableId).run.head
      val fields = DataField.filter(_.tableIdFk === tableId).run
      val apiFields = fields.map { field =>
        new ApiDataField(Some(field.id), Some(field.dateCreated), Some(field.lastUpdated), field.tableIdFk, field.name, None)
      }

      val subtables = DataTabletotablecrossref.filter(_.table1 === tableId).map(_.table2).run
      val apiTables = subtables.map { subtableId =>
        constructTableStructure(subtableId)
      }

      val apiTable = new ApiDataTable(
        Some(table.id),
        Some(table.dateCreated),
        Some(table.lastUpdated),
        table.name,
        table.sourceName,
        Some(apiFields),
        apiTables
      )

      apiTable
    }
  }


  /*
   * Private function finding data field by ID
   */
  private def retrieveDataFieldId(fieldId: Int): ApiDataField = {
    db.withSession { implicit session =>
      val field = DataField.filter(_.id === fieldId).run.head
      new ApiDataField(Some(field.id), Some(field.dateCreated), Some(field.lastUpdated), field.tableIdFk, field.name, None)
    }
  }
}