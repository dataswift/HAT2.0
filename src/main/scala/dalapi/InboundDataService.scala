package dalapi

import javax.ws.rs.Path

import akka.actor.ActorLogging
import com.wordnik.swagger.annotations._
import dal.Tables._
import autodal.SlickPostgresDriver.simple._
import org.joda.time.LocalDateTime
import spray.http.MediaTypes._
import spray.json._
import spray.routing._
import spray.httpx.SprayJsonSupport._
import spray.util.LoggingContext

import scala.annotation.meta.field
import scala.concurrent.ExecutionContext.Implicits.global


// this trait defines our service behavior independently from the service actor
@Api(value = "/inbound", description = "Inbound Data Service", position = 0)
trait InboundDataService extends HttpService {

  val routes = { pathPrefix("inbound") {
      createTable ~ createField ~ createRecord ~ createValue ~ storeValueList
    }
  }

  val db = Database.forConfig("devdb")
  implicit val session: Session = db.createSession()

  import InboundJsonProtocol._

  @Path("/table")
  @ApiOperation(value = "table",
    notes = "Creates a virtual table, returns table ID",
    httpMethod = "POST",
    response = classOf[ApiDataTable])
  @ApiImplicitParams(Array(
    new ApiImplicitParam(
      name = "table",
      value = "The new Virtual Data Table to be added",
      required = true,
      dataType = "ApiDataTable",
      paramType = "body")
  ))
  def createTable = path("table") {
    post {
      respondWithMediaType(`application/json`) {
        entity(as[ApiDataTable]) { table =>
          val newTable = new DataTableRow(0, LocalDateTime.now(), LocalDateTime.now(), table.name, false, table.source)
          val tableId = (DataTable returning DataTable.map(_.id)) += newTable
          complete {
            table.copy(id = Some(tableId))
          }
        }
      }
    }
  }

  @Path("/field")
  @ApiOperation(value = "field",
    notes = "Creates a field in a virtual data table",
    httpMethod = "POST",
    response = classOf[ApiDataField])
  @ApiImplicitParams(Array(
    new ApiImplicitParam(
      name = "field",
      value = "The new Data Field to be added",
      required = true,
      dataType = "ApiDataField",
      paramType = "body")
  ))
  def createField = path("field") {
    post {
      respondWithMediaType(`application/json`) {
        entity(as[ApiDataField]) { field =>
          val newField = new DataFieldRow(0, LocalDateTime.now(), LocalDateTime.now(), field.tableId, field.name)
          val fieldId = (DataField returning DataField.map(_.id)) += newField
          complete {
            field.copy(id = Some(fieldId))
          }
        }
      }
    }
  }

  @Path("/record")
  @ApiOperation(value = "data record",
    notes = "Creates a new data record",
    httpMethod = "POST",
    response = classOf[ApiDataRecord])
  @ApiImplicitParams(Array(
    new ApiImplicitParam(
      name = "field",
      value = "The new Data Field to be added",
      required = true,
      dataType = "ApiDataRecord",
      paramType = "body")
  ))
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

  @Path("/value")
  @ApiOperation(value = "value",
    notes = "Stores a single new value",
    httpMethod = "POST",
    response = classOf[ApiDataValue])
  @ApiImplicitParams(Array(
    new ApiImplicitParam(
      name = "value",
      value = "The single value to be stored",
      required = true,
      dataType = "ApiDataValue",
      paramType = "body")
  ))
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

  @Path("/valueList")
  @ApiOperation(value = "Add a list of values at once",
    notes = "Stores a list of new values",
    httpMethod = "POST",
    response = classOf[ApiDataValue])
  @ApiImplicitParams(Array(
    new ApiImplicitParam(
      name = "values",
      value = "The list of values to be inserted",
      required = true,
      dataType = "Seq[ApiDataValue]",   // FIXME: SwaggerUI does not recognise Sequence of types... Array and List are not parsed correctly...
      paramType = "body")
  ))
  def storeValueList = path("valueList") {
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