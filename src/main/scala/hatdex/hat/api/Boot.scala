package hatdex.hat.api

import akka.actor.ActorDSL._
import akka.actor.{ ActorLogging, ActorSystem, Props }
import akka.io.IO
import akka.io.Tcp.Bound
import akka.pattern.{ BackoffSupervisor, Backoff }
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import spray.can.Http
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

object Boot extends App {

  // we need an ActorSystem to host our application in
  implicit val system = ActorSystem("on-spray-can")

  // create and start our service actor
  val dalapiServiceProps = Props[ApiService]

  val supervisor = BackoffSupervisor.props(
    Backoff.onStop(
      dalapiServiceProps,
      childName = "hatdex.hat.dalapi-service",
      minBackoff = 3.seconds,
      maxBackoff = 30.seconds,
      randomFactor = 0.2 // adds 20% "noise" to vary the intervals slightly
      ))

  system.actorOf(supervisor, name = "dalapi-service-supervisor")

  implicit val timeout = Timeout(5.seconds)

  val ioListener = actor("ioListener")(new Act with ActorLogging {
    become {
      case b @ Bound(connection) => log.debug(b.toString)
    }
  })

  val conf = ConfigFactory.load()
  val port = conf.getInt("applicationPort")
  val host = conf.getString("applicationHost")

  system.actorSelection("user/dalapi-service-supervisor/hatdex.hat.dalapi-service") resolveOne() map { service =>
    IO(Http).tell(Http.Bind(service, host, port), ioListener)
  } recover { case e =>
    system.log.error(s"dalapi-service actor could not be resolved: ${e.getMessage}")
  }
}