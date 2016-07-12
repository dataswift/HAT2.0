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

import akka.actor.{ ActorSystem, ActorRefFactory }
import akka.event.LoggingAdapter
import hatdex.hat.api.TestDataCleanup
import hatdex.hat.api.endpoints.jsonExamples.{ BundleExamples, DataDebitExamples }
import hatdex.hat.api.json.JsonProtocol
import hatdex.hat.api.models._
import hatdex.hat.authentication.{ TestAuthCredentials, HatAuthTestHandler }
import hatdex.hat.authentication.authenticators.{ AccessTokenHandler, UserPassHandler }
import hatdex.hat.authentication.models.User
import hatdex.hat.dal.SlickPostgresDriver.api._
import hatdex.hat.dal.Tables._
import org.joda.time.LocalDateTime
import org.mindrot.jbcrypt.BCrypt
import org.specs2.mutable.Specification
import org.specs2.specification.{ BeforeAfterAll, Scope }
import spray.http.HttpHeaders.RawHeader
import spray.http.HttpMethods._
import spray.http.StatusCodes._
import spray.http._
import spray.json._
import spray.testkit.Specs2RouteTest
import spray.httpx.SprayJsonSupport._

class DataDebitSpec extends Specification with Specs2RouteTest with BeforeAfterAll with DataDebit with DataDebitRequiredServices with TestAuthCredentials {
  def actorRefFactory = system
  val logger: LoggingAdapter = system.log
  override lazy val testLogger = logger
  import JsonProtocol._

  override def accessTokenHandler = AccessTokenHandler.AccessTokenAuthenticator(authenticator = HatAuthTestHandler.AccessTokenHandler.authenticator).apply()

  // Prepare the data to create test bundles on
  def beforeAll() = {
    TestDataCleanup.cleanupAll
  }

  // Clean up all data
  def afterAll() = {
    TestDataCleanup.cleanupAll
  }

  object Context extends DataDebitContextualContext with DataDebitRequiredServices {
    def actorRefFactory = system
    val logger: LoggingAdapter = testLogger
  }

  class Context extends Scope {
    val property = Context.property
    val populatedData = Context.populatedData
    val populatedTable = Context.dataTable
  }

  sequential

  "Data Debit Service" should {
    "Accept a contextless Data Debit proposal" in new Context {

      val contextlessBundle = BundleExamples.fullbundle

      val bundleData = JsonParser(contextlessBundle).convertTo[ApiBundleContextless]
      val dataDebitData = JsonParser(DataDebitExamples.dataDebitExample).convertTo[ApiDataDebit]

      val dataDebit = {
        val dataDebit = HttpRequest(POST, "/dataDebit/propose")
          .withHeaders(dataDebitAuthHeader)
          .withEntity(HttpEntity(MediaTypes.`application/json`, dataDebitData.copy(bundleContextless = Some(bundleData)).toJson.toString)) ~>
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
            resp must contain("testValue1")
            resp must contain("testValue2-1")
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

trait DataDebitContext extends Specification with Specs2RouteTest with DataDebit with TestAuthCredentials {
  import JsonProtocol._

  val propertySpec = new PropertySpec()
  val property = propertySpec.createWeightProperty
  val dataSpec = new DataSpec()
  dataSpec.createBasicTables
  val populatedData = dataSpec.populateDataReusable

  val personSpec = new PersonSpec()

  val newPerson = personSpec.createNewPerson
  newPerson.id must beSome

  val dataTable = populatedData match {
    case (dataTable, dataField, record) =>
      dataTable
  }
  val dataField = populatedData match {
    case (dataTable, dataField, record) =>
      dataField
  }

  val bundlesSpec = new BundlesSpec()
  val moreData = bundlesSpec.populateData()
}

trait DataDebitContextualContext extends DataDebitContext {
  import JsonProtocol._

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
        responseAs[String] must contain("testValue1")
        responseAs[String] must contain("testValue2-1")
        responseAs[String] must not contain ("testValue3")
      }
    }
}

trait DataDebitRequiredServices {
  def actorRefFactory: ActorRefFactory
  val logger: LoggingAdapter
  lazy val testLogger = logger
  val system: ActorSystem

  trait LoggingHttpService {
    def actorRefFactory = system
    lazy val logger = testLogger
  }

  val bundlesService = new Bundles with LoggingHttpService
  val bundleContextService = new BundlesContext with LoggingHttpService {
    val eventsService = new Event with LoggingHttpService
    val locationsService = new Location with LoggingHttpService
    val peopleService = new Person with LoggingHttpService
    val thingsService = new Thing with LoggingHttpService
    val organisationsService = new Organisation with LoggingHttpService
  }
}