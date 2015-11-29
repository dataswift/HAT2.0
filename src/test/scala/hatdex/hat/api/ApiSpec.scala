/*
 * Copyright (c) 2015.
 *
 * This work is licensed under the
 * Creative Commons Attribution-NonCommercial-NoDerivatives 4.0 International License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/4.0/
 * or send a letter to Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 */

package hatdex.hat.api

import akka.event.LoggingAdapter
import hatdex.hat.api.endpoints._
import hatdex.hat.api.endpoints.jsonExamples.DataExamples
import hatdex.hat.api.json.JsonProtocol
import hatdex.hat.api.models.ErrorMessage
import hatdex.hat.api.service._
import hatdex.hat.authentication.HatAuthTestHandler
import hatdex.hat.authentication.authenticators.{AccessTokenHandler, UserPassHandler}
import org.specs2.mutable.Specification
import spray.http.HttpMethods._
import spray.http.StatusCodes._
import spray.http._
import spray.testkit.Specs2RouteTest
import spray.httpx.SprayJsonSupport._


class ApiSpec extends Specification with Specs2RouteTest with Api {
  def actorRefFactory = system // Connect the service API to the test ActorSystem

  val logger: LoggingAdapter = system.log

  import JsonProtocol._

  trait LoggingHttpService {
    def actorRefFactory = system

    val logger: LoggingAdapter = system.log
  }

  val helloService = new Hello with LoggingHttpService
  val apiDataService = new Data with LoggingHttpService {
    override def accessTokenHandler = AccessTokenHandler.AccessTokenAuthenticator(authenticator = HatAuthTestHandler.AccessTokenHandler.authenticator).apply()

    override def userPassHandler = UserPassHandler.UserPassAuthenticator(authenticator = HatAuthTestHandler.UserPassHandler.authenticator).apply()
  }
  val apiBundleService = new Bundles with LoggingHttpService

  val apiPropertyService = new Property with LoggingHttpService
  val eventsService = new Event with LoggingHttpService
  val locationsService = new Location with LoggingHttpService
  val peopleService = new Person with LoggingHttpService
  val thingsService = new Thing with LoggingHttpService
  val organisationsService = new Organisation with LoggingHttpService

  val apiBundlesContextService = new BundlesContext with LoggingHttpService {
    def eventsService: EventsService = ApiSpec.this.eventsService
    def peopleService: PeopleService = ApiSpec.this.peopleService
    def thingsService: ThingsService = ApiSpec.this.thingsService
    def locationsService: LocationsService = ApiSpec.this.locationsService
    def organisationsService: OrganisationsService = ApiSpec.this.organisationsService
  }

  val dataDebitService = new DataDebit with LoggingHttpService {
    val bundlesService: BundleService = apiBundleService
    val bundleContextService: BundleContextService = apiBundlesContextService
  }

  val userService = new Users with LoggingHttpService
  val typeService = new Type with LoggingHttpService

  val testRoute = handleRejections(jsonRejectionHandler) {
    apiDataService.routes
  }

  val ownerAuthParams = "?username=bob@gmail.com&password=pa55w0rd"

  sequential

  "Api Service" should {
    "respond with correctly formatted error message for non-existent routes" in {
      Get("/data/randomRoute") ~> sealRoute(testRoute) ~> check {
        response.status should be equalTo NotFound
        responseAs[ErrorMessage].message must contain("could not be found")
      }
    }

    "respond with correctly formatted error message for unauthorised routes" in {
      Get("/data/sources") ~> sealRoute(testRoute) ~> check {
        response.status should be equalTo Unauthorized
        responseAs[ErrorMessage].message must contain("requires authentication")
      }
    }

    "respond with correctly formatted error message for bad request" in {
      HttpRequest(
        POST,
        "/data/table" + ownerAuthParams,
        entity = HttpEntity(MediaTypes.`application/json`, DataExamples.malformedTable)
      ) ~>
        sealRoute(testRoute) ~>
        check {
          response.status should be equalTo BadRequest
          responseAs[ErrorMessage].message must contain("request content was malformed")
        }

    }

  }
}