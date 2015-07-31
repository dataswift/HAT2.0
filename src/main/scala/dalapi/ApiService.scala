package dalapi

import akka.actor.ActorLogging
import com.gettyimages.spray.swagger.SwaggerHttpService
import com.wordnik.swagger.model.ApiInfo
import spray.routing.{HttpService, HttpServiceActor}
import scala.reflect.runtime.universe._
import dal.SlickPostgresDriver.simple._

// we don't implement our route structure directly in the service actor because
// we want to be able to test it independently, without having to spin up an actor
class ApiServiceActor extends HttpServiceActor with ActorLogging {

  // the HttpService trait defines only one abstract member, which
  // connects the services environment to the enclosing actor or test
  override def actorRefFactory = context

  // Initialise all the service the actor handles
  val helloService = new HelloService {
    def actorRefFactory = context
  }

  val swaggerSite = new SwaggerSite {
    def actorRefFactory = context
  }

  val inboundDataService = new DataService {
    def actorRefFactory = context
  }

  val inboundEventsService = new InboundEventsService {
    def actorRefFactory = context
  }

  // Concatenate all their handled routes
  val routes = helloService.routes ~ swaggerSite.site ~ inboundDataService.routes

  def receive = runRoute(routes)
}


trait SwaggerSite extends HttpService {
  val site =
    pathPrefix("api-docs-ui") { getFromResource("swagger-ui/index.html") } ~
      getFromResourceDirectory("swagger-ui")
}
