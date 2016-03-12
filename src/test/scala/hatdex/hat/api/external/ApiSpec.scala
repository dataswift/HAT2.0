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
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model._
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.{ActorMaterializer, Materializer}
import hatdex.hat.api.endpoints.jsonExamples.DataExamples
import hatdex.hat.api.json.JsonProtocol
import hatdex.hat.api.models.{ApiDataField, ApiDataTable}
import org.specs2.execute.Success
import spray.json.JsonParser
import spray.json._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RemoteApiSpec extends BaseRemoteApiSpec {
  val logger: LoggingAdapter = system.log

  def testspec(hatAddress: String, ownerAuthParams: Map[String, String]) = {
    implicit val materializer: Materializer = ActorMaterializer()
    import JsonProtocol._

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

    def createTable(name: String, source: String): Future[ApiDataTable] = {
      val path = Uri.Path("/data/table")
      val tableToCreate = ApiDataTable(None, None, None, name = name, source = source, None, None)
      val tableCreateResponse = Http().singleRequest(HttpRequest(
        HttpMethods.POST,
        Uri(hatAddress).withPath(path).withQuery(Uri.Query(ownerAuthParams)),
        entity = HttpEntity(MediaTypes.`application/json`, tableToCreate.toJson.toString())
      ))

      val response: Future[HttpResponse] = tableCreateResponse.flatMap { response =>
        response.status match {
          case StatusCodes.Created =>
            logger.debug("Table created")
            tableCreateResponse
          case StatusCodes.BadRequest =>
            logger.debug(s"Table not created, looking up (${response.entity.toString})")
            val tableParams = Map("name" -> name, "source" -> source)
            val query = Uri.Query(ownerAuthParams ++ tableParams)
            val fetchTableResponse: Future[HttpResponse] = Http().singleRequest(HttpRequest(HttpMethods.GET, Uri(hatAddress).withPath(path).withQuery(query)))
            fetchTableResponse.map(_.status) must beEqualTo(StatusCodes.OK).awaitWithTimeout
            fetchTableResponse
          case _ =>
            logger.debug("Unexpected response status")
            throw new Exception("Unexpected response status")
        }
      }

      response.flatMap(r => Unmarshal[HttpEntity](r.entity).to[ApiDataTable])
    }

    def createField(field: ApiDataField, table: ApiDataTable): Future[ApiDataField] = {
      val path = Uri.Path("/data/field")
      val fieldCreateResponse = Http().singleRequest(HttpRequest(
        HttpMethods.POST,
        Uri(hatAddress).withPath(path).withQuery(Uri.Query(ownerAuthParams)),
        entity = HttpEntity(MediaTypes.`application/json`, field.toJson.toString)
      ))
      fieldCreateResponse.map(_.status) must beEqualTo(StatusCodes.Created).awaitWithTimeout
      fieldCreateResponse.flatMap(r => Unmarshal[HttpEntity](r.entity).to[ApiDataField])
    }

    "Data APIs" should {
      sequential

      "Create or find test table" in {
        val mainTable = createTable("kitchen", "fibaro")
        mainTable.map(_.name) must beEqualTo("kitchen").awaitWithTimeout
      }

      "Setup or verify existing structures" in {
        val mainTable = createTable("kitchen", "fibaro")
        mainTable.map(_.name) must beEqualTo("kitchen").awaitWithTimeout

        val tableStructureFuture = mainTable.flatMap { table =>
          val path = Uri.Path(s"/data/table/${table.id.get}")

          val tableStructureReq = Http().singleRequest(HttpRequest(HttpMethods.GET, Uri(hatAddress).withPath(path).withQuery(Uri.Query(ownerAuthParams))))
          val tableStructure = tableStructureReq.flatMap(r => Unmarshal[HttpEntity](r.entity).to[ApiDataTable])

          tableStructure.flatMap { structure =>
            if (structure.subTables.isDefined && structure.subTables.get.find(_.name == "kitchenElectricity").isDefined) {
              tableStructure
            } else {
              val subTable = createTable("kitchenElectricity", "fibaro")
              subTable.map(_.name) must beEqualTo("kitchenElectricity").awaitWithTimeout
              subTable.map(_.id) must beSome[Int].awaitWithTimeout

              subTable.map { subStructure =>
                val linkingResult = Http().singleRequest(HttpRequest(
                    HttpMethods.POST,
                    Uri(hatAddress)
                      .withPath(Uri.Path(s"/data/table/${structure.id.get}/table/${subStructure.id.get}"))
                      .withQuery(Uri.Query(ownerAuthParams)),
                    entity = HttpEntity(MediaTypes.`application/json`, DataExamples.relationshipParent)
                  ))
                linkingResult.map(_.status) must beEqualTo(StatusCodes.OK).awaitWithTimeout
              }

              Http().singleRequest(
                HttpRequest(HttpMethods.GET, Uri(hatAddress).withPath(path).withQuery(Uri.Query(ownerAuthParams)))
              ).flatMap(r => Unmarshal[HttpEntity](r.entity).to[ApiDataTable])
            }
          }
        }

        tableStructureFuture.map(_.id) must beSome[Int].awaitWithTimeout
        tableStructureFuture.map(_.subTables) must beSome[Seq[ApiDataTable]].awaitWithTimeout
        tableStructureFuture.map(_.subTables.get) must contain((t: ApiDataTable) => t.name must beEqualTo("kitchenElectricity")).atLeastOnce.awaitWithTimeout
      }

      "Find or setup table fields" in {
        val path = Uri.Path("/data/table")
        val tableParams = Map("name" -> "kitchen", "source" -> "fibaro")
        val query = Uri.Query(ownerAuthParams ++ tableParams)
        val fetchTableResponse: Future[HttpResponse] = Http().singleRequest(HttpRequest(HttpMethods.GET, Uri(hatAddress).withPath(path).withQuery(query)))
        fetchTableResponse.map(_.status) must beEqualTo(StatusCodes.OK).awaitWithTimeout

        val tableSummaryFuture = fetchTableResponse.flatMap(r => Unmarshal[HttpEntity](r.entity).to[ApiDataTable])
        tableSummaryFuture.map(_.id) must beSome[Int].awaitWithTimeout
        val fullStructureFuture = tableSummaryFuture flatMap { tableSummary =>
          Http().singleRequest(
            HttpRequest(HttpMethods.GET, Uri(hatAddress).withPath(Uri.Path(s"/data/table/${tableSummary.id.get}")).withQuery(Uri.Query(ownerAuthParams)))
          ).flatMap(r => Unmarshal[HttpEntity](r.entity).to[ApiDataTable])
        }

        fullStructureFuture.map(_.subTables) must beSome[Seq[ApiDataTable]].awaitWithTimeout
        fullStructureFuture.map(_.subTables.get) must contain((t: ApiDataTable) => t.name must beEqualTo("kitchenElectricity")).atLeastOnce.awaitWithTimeout

        fullStructureFuture.map { structure =>
          val structureStr = structure.toString
          if (!structureStr.contains("tableTestField") && !structureStr.contains("subtableTestField2")) {
            logger.debug("Creating fields for the table")
            val field = JsonParser(DataExamples.testField).convertTo[ApiDataField]
            val completeTableField = field.copy(tableId = structure.id)
            createField(completeTableField, structure)

            val subtable = structure.subTables.get      // checking for existence of subtables done before
              .find(_.name == "kitchenElectricity").get   // checking for presence of this table done before

            val subField = field.copy(tableId = subtable.id, name = "subtableTestField1")
            createField(subField, subtable)

            val subField2 = field.copy(tableId = subtable.id, name = "subtableTestField2")
            createField(subField2, subtable)
          }
          else {
            logger.debug("Fields already created")
          }
        }

        val structureWithFieldsFuture = tableSummaryFuture flatMap { tableSummary =>
          Http().singleRequest(
            HttpRequest(HttpMethods.GET, Uri(hatAddress).withPath(Uri.Path(s"/data/table/${tableSummary.id.get}")).withQuery(Uri.Query(ownerAuthParams)))
          ).flatMap(r => Unmarshal[HttpEntity](r.entity).to[ApiDataTable])
        }

        structureWithFieldsFuture.map(_.fields.get) must contain((t: ApiDataField) => t.name must beEqualTo("tableTestField")).atLeastOnce.awaitWithTimeout
      }

      
    }
  }
}