package dalapi

import akka.actor.Actor
import com.gettyimages.spray.swagger.SwaggerHttpService
import com.wordnik.swagger.annotations.{Api, ApiOperation}
import com.wordnik.swagger.model.ApiInfo
import spray.http.MediaTypes._
import spray.routing._

import scala.reflect.runtime.universe._

// we don't implement our route structure directly in the service actor because
// we want to be able to test it independently, without having to spin up an actor
class InboundDataServiceActor extends HttpServiceActor with Actor {

  // the HttpService trait defines only one abstract member, which
  // connects the services environment to the enclosing actor or test
  override def actorRefFactory = context

  val swaggerService = new SwaggerHttpService {
    override def apiTypes = Seq(typeOf[InboundDataService])
    override def apiVersion = "2.0"
    override def baseUrl = "/" // let swagger-ui determine the host and port
    override def docsPath = "api-docs"
    override def actorRefFactory = context
    override def apiInfo = Some(new ApiInfo("HAT 2.0 API", "The HAT API.", "TOC Url", "Andrius Aucinas @AndriusA", "Apache V2", "http://www.apache.org/licenses/LICENSE-2.0"))

    //authorizations, not used
  }

  val inboundDataService = new InboundDataService {
    def actorRefFactory = context
  }

  val swaggerSite = new SwaggerSite {
    def actorRefFactory = context
  }

  val routes = inboundDataService.routes ~ swaggerService.routes ~ swaggerSite.site

  def receive = runRoute(routes)
}


// this trait defines our service behavior independently from the service actor
@Api(value = "/", description = "Low level inbound data operations", position = 0)
trait InboundDataService extends HttpService {

  val routes = home ~ data

  @ApiOperation(value = "Get the system greeting",
    notes = "Returns a hello message only",
    httpMethod = "GET",
    response = classOf[String],
    produces = "text/html")
  def home = get {
    path("") {
        respondWithMediaType(`text/html`) { // XML is marshalled to `text/xml` by default, so we simply override here
          complete {
            <html>
              <body>
                <h1>Hello HAT 2.0!</h1>
              </body>
            </html>
          }
        }
    }
  }

  def data = get {
    path("data") {
        respondWithMediaType(`text/html`) {
          complete {
            <html>
              <body>
                <h1>Here be dragons</h1>
              </body>
            </html>
          }
        }

    }
  }
}

trait SwaggerSite extends HttpService {
  val site =
    pathPrefix("api-docs-ui") { getFromResource("swagger-ui/index.html") } ~
      getFromResourceDirectory("swagger-ui")
}