package dalapi

import akka.actor.ActorLogging
import com.gettyimages.spray.swagger.SwaggerHttpService
import com.wordnik.swagger.model.ApiInfo
import spray.routing.{HttpService, HttpServiceActor}
import scala.reflect.runtime.universe._
import autodal.SlickPostgresDriver.simple._

// we don't implement our route structure directly in the service actor because
// we want to be able to test it independently, without having to spin up an actor
class ApiServiceActor extends HttpServiceActor with ActorLogging {

  // the HttpService trait defines only one abstract member, which
  // connects the services environment to the enclosing actor or test
  override def actorRefFactory = context

  val swaggerService = new SwaggerHttpService {
    override def apiTypes = Seq(typeOf[InboundDataService], typeOf[HelloService])
    override def apiVersion = "2.0"
    override def baseUrl = "/" // let swagger-ui determine the host and port
    override def docsPath = "api-docs"
    override def actorRefFactory = context
    override def apiInfo = Some(new ApiInfo("HAT 2.0 API", "The HAT API.", "TOC Url", "Andrius Aucinas @AndriusA", "Apache V2", "http://www.apache.org/licenses/LICENSE-2.0"))

    //authorizations, not used
  }

  // Initialise all the service the actor handles
  val helloService = new HelloService {
    def actorRefFactory = context
  }

  val swaggerSite = new SwaggerSite {
    def actorRefFactory = context
  }

  val inboundDataService = new InboundDataService {
    def actorRefFactory = context
  }

  // Concatenate all their handled routes
  val routes = helloService.routes ~ swaggerService.routes ~ swaggerSite.site ~ inboundDataService.routes

  def receive = runRoute(routes)
}


trait SwaggerSite extends HttpService {
  val site =
    pathPrefix("api-docs-ui") { getFromResource("swagger-ui/index.html") } ~
      getFromResourceDirectory("swagger-ui")
}
