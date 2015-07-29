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


// this trait defines our service behavior independently from the service actor
trait InboundDataService extends HttpService {

  val routes = { pathPrefix("inbound" / "data") {
      createTable ~ createField ~ createRecord ~ createValue ~ storeValueList
    }
  }

  val conf = ConfigFactory.load()
  val dbconfig = conf.getString("applicationDb")
  val db = Database.forConfig(dbconfig)
  implicit val session: Session = db.createSession()

  import InboundJsonProtocol._

  def createTable = path("table") {
    post {
      respondWithMediaType(`application/json`) {
        entity(as[ApiDataTable]) { table =>
          val newTable = new DataTableRow(0, LocalDateTime.now(), LocalDateTime.now(), table.name, table.source)
          val tableId = (DataTable returning DataTable.map(_.id)) += newTable
          complete(Created, {
            table.copy(id = Some(tableId))
          })
        }
      }
    }
  }

  def addTableToTable = path("table" / IntNumber / "table" / IntNumber) { (parentId: Int, childId: Int) =>
    post {
      respondWithMediaType(`application/json`) {
        val dataTableToTableCrossRefRow = new DataTabletotablecrossrefRow(1, LocalDateTime.now(), LocalDateTime.now(), "Parent_Child", parentId, childId)
        val tableToTablecrossrefId = (DataTabletotablecrossref returning DataTabletotablecrossref.map(_.id)) += dataTableToTableCrossRefRow

        complete {
          ApiGenericId(tableToTablecrossrefId)
        }
      }
    }
  }


  def createField = path("field") {
    post {
      respondWithMediaType(`application/json`) {
        entity(as[ApiDataField]) { field =>
          val newField = new DataFieldRow(0, LocalDateTime.now(), LocalDateTime.now(), field.name, field.tableId)
          val fieldId = (DataField returning DataField.map(_.id)) += newField
          complete {
            field.copy(id = Some(fieldId))
          }
        }
      }
    }
  }

  def createRecord = path("record") {
    post {
      respondWithMediaType(`application/json`) {
        entity(as[ApiDataRecord]) { record =>
          val newRecord = new DataRecordRow(0, LocalDateTime.now(), LocalDateTime.now(), record.name)
          val recordId = (DataRecord returning DataRecord.map(_.id)) += newRecord
          complete{
            record.copy(id = Some(recordId))
          }
        }
      }
    }
  }

  def createValue = path("value") {
    post {
      respondWithMediaType(`application/json`) {
        entity(as[ApiDataValue]) { value =>
          val newValue = new DataValueRow(0, LocalDateTime.now(), LocalDateTime.now(), value.value, value.fieldId, value.recordId)
          DataValue += newValue
          complete{
            value
          }
        }
      }
    }
  }

  def storeValueList = path("value" / "list") {
    post {
      respondWithMediaType(`application/json`) {
        entity(as[Seq[ApiDataValue]]) { values =>
          val dataValues = values.map { value =>
            DataValueRow(0, LocalDateTime.now(), LocalDateTime.now(), value.value, value.fieldId, value.recordId)
          }

          DataValue ++= dataValues
          complete{
            values.head
          }
        }
      }
    }
  }


}