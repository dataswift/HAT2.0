package org.hatdex.hat.client

import dev.profunktor.auth.jwt.JwtSecretKey
import io.dataswift.adjudicator.{ HatClaim, JwtClaimBuilder }
import pdi.jwt.{ Jwt, JwtAlgorithm, JwtClaim, JwtHeader }
import play.api.Logging
import play.api.http.Status._
import play.api.libs.json.Json
import play.api.libs.ws.{ WSClient, WSRequest }

import scala.concurrent.{ ExecutionContext, Future }

object TrustProxyRequestTypes {
  // Public Key Request
  sealed trait PublicKeyRequestFailure
  case class PublicKeyServiceFailure(failureDescription: String) extends PublicKeyRequestFailure
  case class InvalidPublicKeyFailure(failureDescription: String) extends PublicKeyRequestFailure
  case class PublicKeyReceived(publicKey: String) extends AnyVal
}

trait TrustProxyClient {
  import TrustProxyRequestTypes._
  def getPublicKey(
      ws: WSClient
    )(implicit ec: ExecutionContext): Future[Either[PublicKeyRequestFailure, PublicKeyReceived]]
}

class TrustProxyWsClient(
    ws: WSClient)
    extends TrustProxyClient
    with Logging {
  import TrustProxyRequestTypes._

  override def getPublicKey(
      ws: WSClient
    )(implicit ec: ExecutionContext): Future[Either[PublicKeyRequestFailure, PublicKeyReceived]] = {

    val url = s"https://pdaproxy.playconcepts.co.uk/publickey"

    runPublicKeyRequest(makeRequest(url, ws))
  }
  // Base Request
  private def makeRequest(
      url: String,
      ws: WSClient
    )(implicit ec: ExecutionContext): WSRequest = {
    logger.info(s"makeRequest: ${url}")
    ws.url(url)
  }

  private def runPublicKeyRequest(
      req: WSRequest
    )(implicit ec: ExecutionContext): Future[Either[PublicKeyRequestFailure, PublicKeyReceived]] = {
    logger.info(s"runPublicKeyRequest")
    req.get().map { response =>
      // println(response.body)
      response.status match {
        case OK =>
          val body = response.body
          // println(s"str: ${body}")
          Right(PublicKeyReceived(body))
        case _ =>
          Left(
            PublicKeyServiceFailure(
              s"The Adjudicator Service responded with an error: ${response.statusText}"
            )
          )
      }
    } recover {
      case e =>
        Left(
          PublicKeyServiceFailure(
            s"The Adjudicator Service responded with an error: ${e.getMessage}"
          )
        )
    }
  }

}
