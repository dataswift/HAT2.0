/*
 * Copyright (c) 2015.
 *
 * This work is licensed under the
 * Creative Commons Attribution-NonCommercial-NoDerivatives 4.0 International License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/4.0/
 * or send a letter to Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 */

package hatdex.hat.api.external

import akka.http.scaladsl.Http
import akka.http.scaladsl.model._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._

class RemoteApiSpec extends BaseRemoteApiSpec {
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
  }
}