/*
 * Copyright (c) 2015.
 *
 * This work is licensed under the
 * Creative Commons Attribution-NonCommercial-NoDerivatives 4.0 International License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/4.0/
 * or send a letter to Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 */

package hatdex.hat.api.external

import akka.event.LoggingAdapter
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import hatdex.hat.api.endpoints.jsonExamples.DataExamples
import hatdex.hat.api.json.{DateTimeMarshalling, UuidMarshalling, HatJsonProtocol}
import hatdex.hat.api.models.{ApiDataRecord, ApiDataField, ApiDataValue, ApiDataTable}
import spray.json._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object TestJsonProtocol extends SprayJsonSupport with DefaultJsonProtocol with UuidMarshalling with DateTimeMarshalling {
  implicit val apiDataValueFormat: RootJsonFormat[ApiDataValue] = rootFormat(lazyFormat(jsonFormat6(ApiDataValue.apply)))
  implicit val dataFieldformat = jsonFormat6(ApiDataField.apply)
  // Need to go via "lazyFormat" for recursive types
  implicit val virtualTableFormat: RootJsonFormat[ApiDataTable] = rootFormat(lazyFormat(jsonFormat7(ApiDataTable.apply)))
  implicit val apiDataRecord = jsonFormat5(ApiDataRecord.apply)
}

class RemoteApiSpec extends BaseRemoteApiSpec {
  val logger: LoggingAdapter = system.log

  def testspec(hatAddress: String, ownerAuthParams: Map[String, String]) = {
    "The HAT" should {
      "be alive" in {
        val responseFuture: Future[HttpResponse] = Http().singleRequest(HttpRequest(uri = hatAddress))
        responseFuture.map(_.status) must beEqualTo(StatusCodes.OK).awaitWithTimeout
        responseFuture.map(_.entity.toString) must contain("Hello HAT 2.0!").awaitWithTimeout
      }

      "disallow unauthorized request to non-public routes" in {
        val responseFuture: Future[HttpResponse] = Http().singleRequest(HttpRequest(uri = hatAddress + "/hat"))
        responseFuture.map(_.status) must beEqualTo(StatusCodes.Unauthorized).awaitWithTimeout
      }

      "accept authorisation" in {
        val path = Uri.Path("/hat")
        val query = Uri.Query(ownerAuthParams)
        val responseFuture: Future[HttpResponse] = Http().singleRequest(HttpRequest(HttpMethods.GET, Uri(hatAddress).withPath(path).withQuery(query)))
        responseFuture.map(_.status) must beEqualTo(StatusCodes.OK).awaitWithTimeout
        responseFuture.map(_.entity.toString) must contain("Welcome to your Hub of All Things").awaitWithTimeout
      }
    }

    "Data APIs" should {
      "Create or find test table" in {
        // Create main table
        val path = Uri.Path("/data/table")

        val tableCreateResponse = Http().singleRequest(HttpRequest(
          HttpMethods.POST,
          Uri(hatAddress).withPath(path).withQuery(Uri.Query(ownerAuthParams)),
          entity = HttpEntity(MediaTypes.`application/json`, DataExamples.tableKitchen)
        ))

        val response: Future[HttpResponse] = tableCreateResponse.flatMap { response =>
          response.status match {
            case StatusCodes.Created =>
              logger.debug("Table created")
              tableCreateResponse
            case StatusCodes.BadRequest =>
              logger.debug(s"Table not created, looking up (${response.entity.toString})")
              val tableParams = Map("name" -> "kitchen", "source" -> "fibaro")
              val query = Uri.Query(ownerAuthParams ++ tableParams)
              val fetchTableResponse: Future[HttpResponse] = Http().singleRequest(HttpRequest(HttpMethods.GET, Uri(hatAddress).withPath(path).withQuery(query)))
              fetchTableResponse.map(_.status) must beEqualTo(StatusCodes.OK).awaitWithTimeout
              fetchTableResponse
            case _ =>
              logger.debug("Unexpected response status")
              throw new Exception("Unexpected response status")
          }
        }

        val table = response.flatMap { resp =>
          logger.debug("Response: " + resp.entity.toString)
          import TestJsonProtocol._
          Unmarshal(resp.entity).to[ApiDataTable]
        }

//        table must contain("fibaro").awaitWithTimeout
        table.map(_.name) must beEqualTo("fibaro").awaitWithTimeout

      }

    }
  }
}