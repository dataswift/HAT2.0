package hatdex.hat.api

import akka.actor.ActorDSL._
import akka.actor.{ActorLogging, ActorSystem, Props}
import akka.io.IO
import akka.io.Tcp.Bound
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import spray.can.Http
import scala.concurrent.duration._

object Boot extends App {

  // we need an ActorSystem to host our application in
  implicit val system = ActorSystem("on-spray-can")

  // create and start our service actor
  val service = system.actorOf(Props[ApiService], "hatdex.hat.dalapi-service")

  implicit val timeout = Timeout(5.seconds)

  val ioListener = actor("ioListener")(new Act with ActorLogging {
    become {
      case b @ Bound(connection) => log.info(b.toString)
    }
  })

  val conf = ConfigFactory.load()
  val port = conf.getInt("applicationPort")
  val host = conf.getString("applicationHost")

  IO(Http).tell(Http.Bind(service, host, port), ioListener)
}