/*
 * Copyright (c) 2015.
 *
 * This work is licensed under the
 * Creative Commons Attribution-NonCommercial-NoDerivatives 4.0 International License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/4.0/
 * or send a letter to Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 */

package hatdex.hat.tests.remote

import akka.event.LoggingAdapter
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model._
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.pattern.Patterns
import akka.stream.{ActorMaterializer, Materializer}
import hatdex.hat.api.endpoints.jsonExamples.DataExamples
import hatdex.hat.api.json.JsonProtocol
import hatdex.hat.api.models._
import spray.json.JsonParser
import spray.json._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration.Duration;
import scala.concurrent.duration._

import org.specs2.mutable._
import org.specs2.runner._

class ApiSpec extends BaseRemoteApiSpec {
  val logger: LoggingAdapter = system.log

  def testspec(hatAddress: String, ownerAuthParams: Map[String, String]) = {
    implicit val materializer: Materializer = ActorMaterializer()
    import JsonProtocol._

    val apiHelpers = new HatApiHelpers(logger = logger, hatAddress = hatAddress, ownerAuthParams = ownerAuthParams)

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
    section("REMOTE")

    "Data APIs for table creation" should {
      sequential

      "Create or find test table" in {
        val mainTable = apiHelpers.createTable("kitchen", "fibaro")
        mainTable.map(_.name) must beEqualTo("kitchen").awaitWithTimeout
      }

      "Setup or verify existing structures" in {
        val eventualDataTable = apiHelpers.getTableStructure("kitchen", "fibaro")

        val tableStructureFuture = eventualDataTable.flatMap { structure =>
          if (structure.subTables.isDefined && structure.subTables.get.find(_.name == "kitchenElectricity").isDefined) {
            eventualDataTable
          } else {
            val subTable = apiHelpers.createTable("kitchenElectricity", "fibaro")
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

            val path = Uri.Path(s"/data/table/${structure.id.get}")
            Http().singleRequest(
              HttpRequest(HttpMethods.GET, Uri(hatAddress).withPath(path).withQuery(Uri.Query(ownerAuthParams)))
            ).flatMap(r => Unmarshal[HttpEntity](r.entity).to[ApiDataTable])
          }
        }

        tableStructureFuture.map(_.id) must beSome[Int].awaitWithTimeout
        tableStructureFuture.map(_.subTables) must beSome[Seq[ApiDataTable]].awaitWithTimeout
        tableStructureFuture.map(_.subTables.get) must contain((t: ApiDataTable) => t.name must beEqualTo("kitchenElectricity")).atLeastOnce.awaitWithTimeout
      }

      "Find or setup table fields" in {
        val fullStructureFuture = apiHelpers.getTableStructure("kitchen", "fibaro")
        fullStructureFuture.map(_.subTables) must beSome[Seq[ApiDataTable]].awaitWithTimeout
        fullStructureFuture.map(_.subTables.get) must contain((t: ApiDataTable) => t.name must beEqualTo("kitchenElectricity")).atLeastOnce.awaitWithTimeout

        fullStructureFuture.map { structure =>
          val structureStr = structure.toString
          if (!structureStr.contains("tableTestField") && !structureStr.contains("subtableTestField2")) {
            logger.debug("Creating fields for the table")
            val field = JsonParser(DataExamples.testField).convertTo[ApiDataField]
            val completeTableField = field.copy(tableId = structure.id)
            apiHelpers.createField(completeTableField, structure)

            val subtable = structure.subTables.get // checking for existence of subtables done before
              .find(_.name == "kitchenElectricity").get // checking for presence of this table done before

            val subField = field.copy(tableId = subtable.id, name = "subtableTestField1")
            apiHelpers.createField(subField, subtable)

            val subField2 = field.copy(tableId = subtable.id, name = "subtableTestField2")
            apiHelpers.createField(subField2, subtable)
          }
          else {
            logger.debug("Fields already created")
          }
        }

        val structureWithFieldsFuture = fullStructureFuture flatMap { tableStructure =>
          Http().singleRequest(
            HttpRequest(HttpMethods.GET, Uri(hatAddress).withPath(Uri.Path(s"/data/table/${tableStructure.id.get}")).withQuery(Uri.Query(ownerAuthParams)))
          ).flatMap(r => Unmarshal[HttpEntity](r.entity).to[ApiDataTable])
        }

        structureWithFieldsFuture.map(_.fields.get) must contain((t: ApiDataField) => t.name must beEqualTo("tableTestField")).atLeastOnce.awaitWithTimeout
      }
    }
    section("REMOTE")

    "Batch Data APIs for table creation" should {
      sequential

      "Create or find complex test table" in {

        val nestedTable = ApiDataTable(None, None, None, name = "simpleTable", source = "apitester",
          fields = Some(Seq(
            ApiDataField(None, None, None, None, name = "tableTestField", None),
            ApiDataField(None, None, None, None, name = "tableTestField2", None)
          )),
          subTables = Some(Seq(
            ApiDataTable(None, None, None, name = "simpleSubtable", source = "apitester",
              fields = Some(Seq(
                ApiDataField(None, None, None, None, name = "tableTestField3", None),
                ApiDataField(None, None, None, None, name = "tableTestField4", None)
              )),
              subTables = None)
          ))
        )

        val helpersTable = apiHelpers.createTable(nestedTable)

        helpersTable.map(_.id) must beSome[Int].awaitWithTimeout
        helpersTable.map(_.name) must beEqualTo("simpleTable").awaitWithTimeout
        helpersTable.map(_.subTables) must beSome[Seq[ApiDataTable]].awaitWithTimeout
        helpersTable.map(_.fields) must beSome[Seq[ApiDataField]].awaitWithTimeout
      }

      "Accept batch data record/value insertion" in {
        val tableStructureFuture = apiHelpers.getTableStructure("simpleTable", "apitester")

        val valuesFuture = tableStructureFuture flatMap { tableStructure =>
          val dataField = tableStructure.fields.flatMap(_.find(_.name == "tableTestField"))
          val dataSubfield3 = tableStructure.subTables
            .flatMap(_.find(_.name == "simpleSubtable"))
            .flatMap(_.fields.flatMap(_.find(_.name == "tableTestField3")))
          val dataSubfield4 = tableStructure.subTables
            .flatMap(_.find(_.name == "simpleSubtable"))
            .flatMap(_.fields.flatMap(_.find(_.name == "tableTestField4")))


          val recordValueList = Seq(
            ApiRecordValues(
              ApiDataRecord(None, None, None, "testRecord 5", None),
              Seq(
                new ApiDataValue(None, None, None, "testValue5-1", dataField, None),
                new ApiDataValue(None, None, None, "testValue5-2", dataSubfield3, None),
                new ApiDataValue(None, None, None, "testValue5-3", dataSubfield4, None)
              )
            ),
            ApiRecordValues(
              ApiDataRecord(None, None, None, "testRecord 6", None),
              Seq(
                new ApiDataValue(None, None, None, "testValue6-1", dataField, None),
                new ApiDataValue(None, None, None, "testValue6-2", dataSubfield3, None),
                new ApiDataValue(None, None, None, "testValue6-3", dataSubfield4, None)
              )
            )
          )

          apiHelpers.insertDataRecords(recordValueList)
        }

        valuesFuture.map(_.length) must beEqualTo(2).awaitWithTimeout
      }

      "Be able to insert data periodically" in { implicit ec: ExecutionContext =>
        val testIterations = 5
        val interval = 60 seconds
        val testIterationSequence = 1 to testIterations
        val testStart = DateTime.now

        val tableStructureFuture = apiHelpers.getTableStructure("simpleTable", "apitester")

        val valuesFuture = tableStructureFuture flatMap { tableStructure =>
          val dataField = tableStructure.fields.flatMap(_.find(_.name == "tableTestField"))
          val dataSubfield3 = tableStructure.subTables
            .flatMap(_.find(_.name == "simpleSubtable"))
            .flatMap(_.fields.flatMap(_.find(_.name == "tableTestField3")))
          val dataSubfield4 = tableStructure.subTables
            .flatMap(_.find(_.name == "simpleSubtable"))
            .flatMap(_.fields.flatMap(_.find(_.name == "tableTestField4")))

          val temp = testIterationSequence map { iteration =>
            val recordValueList = Seq(ApiRecordValues(
              ApiDataRecord(None, None, None, s"testRecord $iteration @$testStart", None),
              Seq(
                new ApiDataValue(None, None, None, s"testValue$iteration-1 @$testStart", dataField, None),
                new ApiDataValue(None, None, None, s"testValue$iteration-2 @$testStart", dataSubfield3, None),
                new ApiDataValue(None, None, None, s"testValue$iteration-3 @$testStart", dataSubfield4, None)
              )
            ))

            val apiResultFuture = Patterns.after(interval * iteration, system.scheduler, ec, apiHelpers.insertDataRecords(recordValueList));
            apiResultFuture.map { apiResult =>
              apiResult.length must beEqualTo(1)
              val apiResultStr = apiResult.toJson.toString()
              apiResultStr must contain(s"testValue$iteration-1 @$testStart")
              apiResultStr must contain(s"testValue$iteration-2 @$testStart")
              apiResultStr must contain(s"testValue$iteration-3 @$testStart")
              logger.debug(s"All good for $iteration")
            }
            apiResultFuture
          }

          Future.sequence(temp)
        }

        valuesFuture.map(_.length) must beEqualTo(testIterations).awaitFor((testIterations + 1) * interval)
      }
    }
    section("REMOTE")
  }
  section("REMOTE")
}

