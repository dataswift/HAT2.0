package dalapi

import javax.ws.rs.Path

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
        getTable
    }
  }

  import InboundJsonProtocol._

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
              "Parent_Child", parentId, childId
            )
            val id = (DataTabletotablecrossref returning DataTabletotablecrossref.map(_.id)) += dataTableToTableCrossRefRow

            complete {
              ApiGenericId(id)
            }
          }
        }
      }
  }

  def getTable = path("table" / IntNumber) {
    (tableId: Int) =>
      get {
        complete {
          constructTableStructure(tableId)
        }
      }
  }

  private def constructTableStructure(tableId: Int): ApiDataTable = {
    db.withSession { implicit session =>
      val table = DataTable.filter(_.id === tableId).run.head
      val fields = DataField.filter(_.tableIdFk === tableId).run
      val apiFields = fields.map { field =>
        new ApiDataField(Some(field.id), Some(field.dateCreated), Some(field.lastUpdated), field.tableIdFk, field.name)
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

  def getField = path("field" / IntNumber) {
    (fieldId: Int) =>
      get {
        db.withSession { implicit session =>
          val field = DataField.filter(_.id === fieldId).run.head
          val apiField = new ApiDataField(Some(field.id), Some(field.dateCreated), Some(field.lastUpdated), field.tableIdFk, field.name)
          complete {
            apiField
          }
        }
      }
  }

  def createRecord = path("record") {
    post {
      respondWithMediaType(`application/json`) {
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
  }

  def createValue = path("value") {
    post {
      respondWithMediaType(`application/json`) {
        entity(as[ApiDataValue]) { value =>
          db.withSession { implicit session =>
            val newValue = new DataValueRow(0, LocalDateTime.now(), LocalDateTime.now(), value.value, value.fieldId, value.recordId)
            DataValue += newValue
            complete {
              value
            }
          }
        }
      }
    }
  }

  def storeValueList = path("value" / "list") {
    post {
      respondWithMediaType(`application/json`) {
        entity(as[Seq[ApiDataValue]]) { values =>
          db.withSession { implicit session =>
            val dataValues = values.map { value =>
              DataValueRow(0, LocalDateTime.now(), LocalDateTime.now(), value.value, value.fieldId, value.recordId)
            }

            DataValue ++= dataValues
            complete {
              values.head
            }
          }
        }
      }
    }
  }


}