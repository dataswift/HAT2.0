package org.hatdex.hat.resourceManagement

import org.hatdex.hat.resourceManagement.models.HatSignup
import play.api.cache.CacheApi
import play.api.{ Configuration, Logger }
import play.api.http.Status._
import play.api.libs.json.{ JsError, JsSuccess }
import play.api.libs.ws.{ WSClient, WSRequest, WSResponse }

import scala.concurrent.{ ExecutionContext, Future }

trait MillinerHatSignup {
  val logger: Logger
  val ws: WSClient
  val configuration: Configuration
  val schema = configuration.getString("resourceManagement.millinerAddress") match {
    case Some(address) if address.startsWith("https") => "https://"
    case Some(address) if address.startsWith("http") => "http://"
    case _ => "http://"
  }

  val millinerAddress = configuration.getString("resourceManagement.millinerAddress").get
    .stripPrefix("http://")
    .stripPrefix("https://")
  val hatSharedSecret = configuration.getString("resourceManagement.hatSharedSecret").get

  val cache: CacheApi

  def getHatSignup(hatAddress: String)(implicit ec: ExecutionContext): Future[HatSignup] = {
    cache.get[HatSignup](s"configuration:$hatAddress") map { signup =>
      logger.debug("Serving hat signup info from cache")
      Future.successful(signup)
    } getOrElse {
      val request: WSRequest = ws.url(s"$schema$millinerAddress/api/manage/configuration/$hatAddress")
        .withVirtualHost(millinerAddress)
        .withHeaders("Accept" -> "application/json", "X-Auth-Token" -> hatSharedSecret)

      val futureResponse: Future[WSResponse] = request.get()
      futureResponse.map { response =>
        response.status match {
          case OK =>
            response.json.validate[HatSignup] match {
              case signup: JsSuccess[HatSignup] =>
                logger.debug(s"Got back configuration: ${signup.value}")
                cache.set(s"configuration:$hatAddress", signup.value)
                signup.value
              case e: JsError =>
                logger.error(s"Parsing HAT configuration failed: ${e}")
                throw new RuntimeException("Fetching HAT configuration failed")
            }
          case _ =>
            logger.error(s"Fetching HAT configuration failed: ${response.body}")
            throw new RuntimeException("Fetching HAT configuration failed")
        }
      }
    }
  }

}
