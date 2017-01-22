package org.hatdex.hat.resourceManagement.actors

import javax.inject.Inject

import akka.actor.{ Props, _ }
import akka.util.Timeout
import net.ceedubs.ficus.Ficus._
import org.hatdex.hat.resourceManagement.IoExecutionContext
import play.api.Configuration
import play.api.libs.concurrent.InjectedActorSupport

import scala.collection.mutable
import scala.concurrent.Future
import scala.concurrent.duration._

class HatServerProviderActor @Inject() (hatServerActorFactory: HatServerActor.Factory, configuration: Configuration) extends Actor with ActorLogging with InjectedActorSupport {
  import HatServerProviderActor._
  import IoExecutionContext.ioThreadPool

  private val activeServers = mutable.HashMap[String, ActorRef]()
  private implicit val hatServerTimeout: Timeout = configuration.underlying.as[FiniteDuration]("resourceManagement.serverProvisioningTimeout")
  private var hatsActive: Long = 0L

  def receive: Receive = {
    case HatServerRetrieve(hat) =>
      log.debug(s"Retrieve HAT server $hat for $sender")
      val retrievingSender = sender
      getHatServerProviderActor(hat) map { hatServerProviderActor =>
        log.debug(s"Got HAT server provider actor, forwarding retrieval message with sender $sender $retrievingSender")
        hatServerProviderActor tell (HatServerActor.HatRetrieve(), retrievingSender)
      } recover {
        case e =>
          log.warning(s"Error while getting HAT server provider actor: ${e.getMessage}")
      }

    case HatServerStarted(_) =>
      hatsActive += 1
      log.warning(s"Total HATs active: $hatsActive")

    case HatServerStopped(_) =>
      hatsActive -= 1

    case GetHatServersActive() =>
      sender ! HatServersActive(hatsActive)

    case message =>
      log.debug(s"Received unexpected message $message")
      log.warning(s"Total HATs active: $hatsActive")
  }

  private def getHatServerProviderActor(hat: String): Future[ActorRef] = {
    context.actorSelection(s"/user/hatServerProviderActor/hat:$hat").resolveOne(hatServerTimeout.duration / 2) map { hatServerActor =>
      log.debug(s"HAT server $hat actor resolved")
      hatServerActor
    } recover {
      case ActorNotFound(selection) =>
        log.debug(s"HAT server $hat actor not found, injecting child")
        val hatServerActor = injectedChild(hatServerActorFactory(hat), s"hat:$hat", props = (props: Props) => props.withDispatcher("hat-server-provider-actor-dispatcher"))
        activeServers(hat) = hatServerActor
        log.debug(s"Injected actor $hatServerActor")
        hatServerActor
      case e =>
        log.warning(s"HAT server $hat actor error: ${e.getMessage}")
        throw e
    }
  }

}

object HatServerProviderActor {
  case class HatServerRetrieve(hat: String)

  case class HatServerStarted(hat: String)
  case class HatServerStopped(hat: String)

  case class GetHatServersActive()
  case class HatServersActive(active: Long)
}

