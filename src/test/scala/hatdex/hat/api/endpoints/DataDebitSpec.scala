/*
 * Copyright (C) 2016 Andrius Aucinas <andrius.aucinas@hatdex.org>
 * SPDX-License-Identifier: AGPL-3.0
 *
 * This file is part of the Hub of All Things project (HAT).
 *
 * HAT is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation, version 3 of
 * the License.
 *
 * HAT is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See
 * the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General
 * Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 */

package hatdex.hat.api.endpoints

import java.util.UUID

import akka.actor.{ActorRefFactory, ActorSystem}
import akka.event.LoggingAdapter
import hatdex.hat.api.actors.DalExecutionContext
import hatdex.hat.api.{DatabaseInfo, TestDataCleanup, TestFixtures}
import hatdex.hat.api.endpoints.jsonExamples.{BundleExamples, DataDebitExamples}
import hatdex.hat.api.json.JsonProtocol
import hatdex.hat.api.models._
import hatdex.hat.authentication.{HatAuthTestHandler, TestAuthCredentials}
import hatdex.hat.authentication.authenticators.{AccessTokenHandler, UserPassHandler}
import hatdex.hat.authentication.models.User
import hatdex.hat.dal.SlickPostgresDriver.api._
import hatdex.hat.dal.Tables._
import org.joda.time.LocalDateTime
import org.mindrot.jbcrypt.BCrypt
import org.specs2.mutable.Specification
import org.specs2.specification.{BeforeAfterAll, Scope}
import spray.http.HttpHeaders.RawHeader
import spray.http.HttpMethods._
import spray.http.StatusCodes._
import spray.http._
import spray.json._
import spray.testkit.Specs2RouteTest
import spray.httpx.SprayJsonSupport._

import scala.concurrent.Await
import scala.concurrent.duration.Duration

class DataDebitSpec extends Specification with Specs2RouteTest with BeforeAfterAll with DataDebit with DataDebitRequiredServices with TestAuthCredentials with DalExecutionContext {
  def actorRefFactory = system
  val logger: LoggingAdapter = system.log
  override val testLogger = logger
  import JsonProtocol._

  override def accessTokenHandler = AccessTokenHandler.AccessTokenAuthenticator(authenticator = HatAuthTestHandler.AccessTokenHandler.authenticator).apply()

  // Prepare the data to create test bundles on
  def beforeAll() = {
    val f = TestDataCleanup.cleanupAll
    Await.result(f, Duration("40 seconds"))
  }

  // Clean up all data
  def afterAll() = {
//    TestDataCleanup.cleanupAll
  }

  object Context extends DataDebitContextualContext with DataDebitRequiredServices with DalExecutionContext {
    def actorRefFactory = system
    val logger: LoggingAdapter = system.log
  }

  class Context extends Scope {
    logger.info("Setting up Data Debit Spec context")
    Context.setup()
  }

  sequential

  "Data Debit Service" should {
    "Accept a contextless Data Debit proposal" in new Context {

      val bundleData = JsonParser(BundleExamples.fullbundle).convertTo[ApiBundleContextless]
      val dataDebitData = JsonParser(DataDebitExamples.dataDebitExample).convertTo[ApiDataDebit].copy(bundleContextless = Some(bundleData))

      import hatdex.hat.dal.SlickPostgresDriver.api._
        DatabaseInfo.db.run { DataTable.result }
          .map(tables => logger.info(s"Tables: $tables"))
//      logger.info(s"populated data table: $populatedTables")

      val dataDebit = {
        val dataDebit = HttpRequest(POST, "/dataDebit/propose")
          .withHeaders(dataDebitAuthHeader)
          .withEntity(HttpEntity(MediaTypes.`application/json`, dataDebitData.toJson.toString)) ~>
          sealRoute(routes) ~>
          check {
            eventually {
              response.status should be equalTo Created
              val responseString = responseAs[String]
              responseString must contain("key")
            }
            responseAs[ApiDataDebit]
          }

        HttpRequest(GET, s"/dataDebit/${dataDebit.key.get}/values")
          .withHeaders(dataDebitAuthHeader) ~>
          sealRoute(routes) ~>
          check {
            eventually {
              logger.info(s"Bundles values response ${responseAs[String]}")
              response.status should be equalTo Forbidden
            }
          }

        dataDebit
      }

      dataDebit.key must beSome

      HttpRequest(PUT, s"/dataDebit/${dataDebit.key.get}/enable")
        .withHeaders(ownerAuthHeader) ~>
        sealRoute(routes) ~>
        check {
          eventually {
            response.status should be equalTo OK
          }
        }

      HttpRequest(GET, s"/dataDebit/${dataDebit.key.get}/values")
        .withHeaders(dataDebitAuthHeader) ~>
        sealRoute(routes) ~>
        check {
          eventually {
            response.status should be equalTo OK
            responseAs[ApiDataDebitOut].bundleContextless must beSome

            val responseString = responseAs[String]
            //logger.info(s"RESPONSE $responseString")
            responseString must contain("dataGroups")
            responseString must contain("event record 1")
            responseString must contain("event record 2")
            responseString must contain("event record 3")

            responseString must contain("kitchen record 1")
            responseString must contain("kitchen record 2")
            responseString must contain("kitchen record 3")

            responseString must contain("kitchen value 1")
            responseString must contain("event name 1")
            responseString must contain("event location 1")
          }
        }
    }

    "Accept a contextual Data Debit proposal" in new Context {

      val dataDebit = {
        val dataDebit = HttpRequest(POST, "/dataDebit/propose")
          .withHeaders(dataDebitAuthHeader)
          .withEntity(HttpEntity(MediaTypes.`application/json`, DataDebitExamples.dataDebitContextual)) ~>
          sealRoute(routes) ~>
          check {
            eventually {
              response.status should be equalTo Created
              val responseString = responseAs[String]
              responseString must contain("key")
            }
            responseAs[ApiDataDebit]
          }

        HttpRequest(GET, s"/${dataDebit.key.get}/values")
          .withHeaders(dataDebitAuthHeader) ~>
          sealRoute(retrieveDataDebitValuesApi) ~>
          check {
            eventually {
              logger.info(s"Bundles values response ${responseAs[String]}")
              response.status should be equalTo Forbidden
            }
          }

        dataDebit
      }

      dataDebit.key must beSome

      val t = {
        HttpRequest(PUT, s"/dataDebit/${dataDebit.key.get}/enable")
          .withHeaders(ownerAuthHeader) ~>
          sealRoute(routes) ~>
          check {
            eventually {
              response.status should be equalTo OK
            }
          }
      }

      HttpRequest(GET, s"/dataDebit/${dataDebit.key.get}/values")
        .withHeaders(dataDebitAuthHeader) ~>
        sealRoute(routes) ~>
        check {
          eventually {
            response.status should be equalTo OK
            val resp = responseAs[String]
            resp must contain("HATperson")
            resp must contain("kitchen value 1")
            resp must contain("kitchen value 2")
          }
          responseAs[ApiDataDebitOut]
        }

      HttpRequest(PUT, s"/dataDebit/${dataDebit.key.get}/disable")
        .withHeaders(ownerAuthHeader) ~>
        sealRoute(routes) ~>
        check {
          eventually {
            response.status should be equalTo OK
          }
        }
    }

    "Reject a contextless Data Debit proposal with incorrect bundle definition" in new Context {
      val contextlessBundle = BundleExamples.fieldlessDataset

      val bundleData = JsonParser(contextlessBundle).convertTo[ApiBundleContextless]
      val dataDebitData = JsonParser(DataDebitExamples.dataDebitExample).convertTo[ApiDataDebit]

      HttpRequest(POST, "/dataDebit/propose")
        .withHeaders(dataDebitAuthHeader)
        .withEntity(HttpEntity(MediaTypes.`application/json`, dataDebitData.copy(bundleContextless = Some(bundleData)).toJson.toString)) ~>
        sealRoute(routes) ~>
        check {
          response.status should be equalTo BadRequest
        }

    }

    "Reject malformed reuqests" in new Context {
      HttpRequest(POST, "/dataDebit/propose")
        .withHeaders(dataDebitAuthHeader)
        .withEntity(HttpEntity(MediaTypes.`application/json`, DataDebitExamples.dataDebitInvalid)) ~>
        sealRoute(routes) ~>
        check {
          response.status should be equalTo BadRequest
        }

      HttpRequest(POST, "/dataDebit/propose")
        .withHeaders(dataDebitAuthHeader)
        .withEntity(HttpEntity(MediaTypes.`application/json`, DataDebitExamples.dataDebitWrongKeyContextless)) ~>
        sealRoute(routes) ~>
        check {
          response.status should be equalTo BadRequest
        }

      HttpRequest(POST, "/dataDebit/propose")
        .withHeaders(dataDebitAuthHeader)
        .withEntity(HttpEntity(MediaTypes.`application/json`, DataDebitExamples.dataDebitWrongKeyContextual)) ~>
        sealRoute(routes) ~>
        check {
          response.status should be equalTo BadRequest
        }
    }

    "Not enable non-existent data debits" in {
      HttpRequest(PUT, s"/dataDebit/acdacdac-2e3d-41df-a1a3-7cf6d23a8abe/enable")
        .withHeaders(ownerAuthHeader) ~>
        sealRoute(routes) ~>
        check {
          response.status should be equalTo NotFound
        }

      HttpRequest(PUT, s"/dataDebit/acdacdac-2e3d-41df-a1a3-7cf6d23a8abe/disable")
        .withHeaders(ownerAuthHeader) ~>
        sealRoute(routes) ~>
        check {
          response.status should be equalTo NotFound
        }
    }

    "List Data Debits" in {
      HttpRequest(GET, s"/dataDebit")
        .withHeaders(ownerAuthHeader) ~>
        sealRoute(routes) ~>
        check {
          response.status should be equalTo OK
          responseAs[Seq[ApiDataDebit]] must not have size(0)
        }
    }
  }
}

trait DataDebitContext extends Specification with Specs2RouteTest with DataDebit with Data with TestAuthCredentials with DalExecutionContext {
  val logger: LoggingAdapter
  import JsonProtocol._
  override def routes = super[DataDebit].routes ~ super[Data].routes
  override val db = DatabaseInfo.db

  override def accessTokenHandler = AccessTokenHandler.AccessTokenAuthenticator(authenticator = HatAuthTestHandler.AccessTokenHandler.authenticator).apply()

  def setup(): Unit = {
    logger.info("Setting up Data Debit Context")
    val result = TestFixtures.contextlessBundleContext
    Await.result(result, Duration("20 seconds"))
    logger.info("Contextless Bundle test fixtures setup")
    //  lazy val (dataTable, dataSubtable) = createBasicTables
    //  lazy val (_, dataField, record) = populateDataReusable
    //  lazy val populatedData = (dataTable, dataField, record)
    //    val bundlesSpec = new BundlesSpec()
  }
}

trait DataDebitContextualContext extends DataDebitContext {
  val logger: LoggingAdapter
  import JsonProtocol._

  override def accessTokenHandler = AccessTokenHandler.AccessTokenAuthenticator(authenticator = HatAuthTestHandler.AccessTokenHandler.authenticator).apply()

  override def setup(): Unit = {
    logger.info("Setting up Data Debit Contextual Context")
    super.setup()

    val propertySpec = new PropertySpec()
    val property = propertySpec.createWeightProperty

    val personSpec = new PersonSpec()
    val newPerson = personSpec.createNewPerson

    val dataTable = HttpRequest(GET, "/data/table?name=kitchenElectricity&source=bundlefibaro")
      .withHeaders(ownerAuthHeader) ~>
      sealRoute(routes) ~>
      check {
        response.status should be equalTo OK
        responseAs[ApiDataTable]
      }

    val maybeDataField = dataTable.fields.map { fields =>
      logger.info(s"Fields in data table: $fields")
      val maybeField = fields.find(f => f.name == "value")
      maybeField must beSome
      maybeField.get
    }

    maybeDataField must beSome
    val dataField = maybeDataField.get

    val dynamicPropertyLink = ApiPropertyRelationshipDynamic(
      None, property, None, None, "test property", dataField)

    val propertyLinkId = HttpRequest(POST, s"/person/${newPerson.id.get}/property/dynamic/${property.id.get}")
      .withHeaders(ownerAuthHeader)
      .withEntity(HttpEntity(MediaTypes.`application/json`, dynamicPropertyLink.toJson.toString)) ~>
      sealRoute(personSpec.routes) ~>
      check {
        eventually {
          response.status should be equalTo Created
        }
        responseAs[ApiGenericId]
      }

    val personValues = HttpRequest(GET, s"/person/${newPerson.id.get}/values")
      .withHeaders(ownerAuthHeader) ~>
      sealRoute(personSpec.routes) ~>
      check {
        eventually {
          response.status should be equalTo OK
          responseAs[String] must contain("kitchen value 1")
          responseAs[String] must contain("kitchen value 2")
          responseAs[String] must not contain ("kitchen time 1")
        }
      }


  }
}

trait DataDebitRequiredServices {
  def actorRefFactory: ActorRefFactory
  val logger: LoggingAdapter
  val testLogger = logger
  val system: ActorSystem

  trait LoggingHttpService {
    def actorRefFactory = system
    lazy val logger = testLogger
  }

  val bundlesService = new Bundles with LoggingHttpService with DalExecutionContext
  val bundleContextService = new BundlesContext with LoggingHttpService with DalExecutionContext {
    val eventsService = new Event with LoggingHttpService with DalExecutionContext
    val locationsService = new Location with LoggingHttpService with DalExecutionContext
    val peopleService = new Person with LoggingHttpService with DalExecutionContext
    val thingsService = new Thing with LoggingHttpService with DalExecutionContext
    val organisationsService = new Organisation with LoggingHttpService with DalExecutionContext
  }
}