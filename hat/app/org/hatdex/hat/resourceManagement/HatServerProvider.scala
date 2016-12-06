package org.hatdex.hat.resourceManagement

import java.security.interfaces.RSAPublicKey
import javax.inject.{ Inject, Named, Singleton }

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import com.mohiva.play.silhouette.api.services.DynamicEnvironmentProviderService
import org.hatdex.hat.resourceManagement.actors.HatServerProviderActor
import play.api.Logger
import play.api.mvc.Request

import scala.concurrent.Future
import scala.concurrent.duration._

@Singleton
class HatServerProvider @Inject() (@Named("hatServerProviderActor") serverProviderActor: ActorRef, hatKeyProvider: HatKeyProvider) extends DynamicEnvironmentProviderService[HatServer] {
  import play.api.libs.concurrent.Execution.Implicits._

  private val logger = Logger("HatServerProvider")

  def retrieve[B](request: Request[B]): Future[Option[HatServer]] = {
    val hatAddress = request.host.split(':').headOption.getOrElse(request.host)
    retrieve(hatAddress)
  }

  def retrieve(hatAddress: String): Future[Option[HatServer]] = {
    logger.info(s"Retrieving environment for $hatAddress")
    implicit val timeout: Timeout = 10.seconds
    (serverProviderActor ? HatServerProviderActor.HatServerRetrieve(hatAddress)) flatMap {
      case server: HatServer =>
        logger.debug(s"Got back server $server")
        Future.successful(Some(server))
      case error: HatServerDiscoveryException =>
        logger.debug(s"Got back error $error")
        Future.failed(error)
      case message =>
        logger.warn(s"Unknown message $message from HAT Server provider actor")
        Future.failed(new HatServerDiscoveryException("Unknown message"))
    } recoverWith {
      case e =>
        logger.warn(s"Error while retrieving HAT Server info: ${e.getMessage}")
        Future.failed(new HatServerDiscoveryException("HAT Server info retrieval failed", e))
    }
  }

  def toString(publicKey: RSAPublicKey) = hatKeyProvider.toString(publicKey)

}

