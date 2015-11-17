package hatdex.hat.api

import akka.actor.ActorLogging
import akka.event.Logging
import hatdex.hat.api.endpoints._
import hatdex.hat.api.models.ErrorMessage
import spray.http._
import spray.httpx.SprayJsonSupport._
import spray.httpx.marshalling
import spray.json.DefaultJsonProtocol._
import spray.routing.directives.LogEntry
import spray.routing.{HttpServiceActor, MalformedRequestContentRejection, Rejected, RejectionHandler}

// we don't implement our route structure directly in the service actor because
// we want to be able to test it independently, without having to spin up an actor
class ApiService extends HttpServiceActor with ActorLogging with Cors {
  override def actorRefFactory = context

  // The HttpService trait defines only one abstract member, which
  // connects the services environment to the enclosing actor or test.
  // We also want logging, hence the abstract logger member is included
  // in HAT services as well
  trait LoggingHttpService {
    def actorRefFactory = context
    val logger = log
  }

  // Initialise all the service the actor handles
  val helloService = new Hello with LoggingHttpService
  val apiDataService = new Data with LoggingHttpService
  val apiBundleService = new Bundles with LoggingHttpService
  val dataDebitService = new DataDebit with LoggingHttpService
  val apiPropertyService = new Property with LoggingHttpService
  val eventsService = new Event with LoggingHttpService
  val locationsService = new Location with LoggingHttpService
  val peopleService = new Person with LoggingHttpService
  val thingsService = new Thing with LoggingHttpService
  val organisationsService = new Organisation with LoggingHttpService
  val userService = new Users with LoggingHttpService
  val typeService = new Type with LoggingHttpService

  // logs request method, uri and response status at debug level
  def requestMethodAndResponseStatusAsInfo(req: HttpRequest): Any => Option[LogEntry] = {
    case res: HttpResponse => Some(LogEntry(req.method + ":" + req.uri + ":" + res.message.status, Logging.InfoLevel))
    case Rejected(rejections) => Some(LogEntry(req.method + ":" + req.uri + ":" + rejections.toString(), Logging.ErrorLevel)) // log rejections
    case _ => None // other kind of responses
  }

  // Wraps rejections in JSON to be sent back to the client
  def jsonRejectionHandler = RejectionHandler {
    case MalformedRequestContentRejection (msg, cause) :: Nil =>
      implicit val errorFormat = jsonFormat2(ErrorMessage.apply)
      complete (StatusCodes.BadRequest, ErrorMessage ("The request content was malformed", msg) )

    // default case that wraps the Default error message into json
    case x if RejectionHandler.Default.isDefinedAt (x) =>
      ctx => RejectionHandler.Default (x) {
        ctx.withHttpResponseMapped {
          case resp@HttpResponse (_, HttpEntity.NonEmpty (ContentType (MediaTypes.`text/plain`, _), msg), _, _) =>
            implicit val errorFormat = jsonFormat2(ErrorMessage.apply)
            resp.withEntity (marshalling.marshalUnsafe (ErrorMessage (msg.asString, "") ) )
        }
      }
  }

  // Concatenate all the handled routes
  val routes = logRequestResponse(requestMethodAndResponseStatusAsInfo _) {
    cors {
      handleRejections(jsonRejectionHandler) {
        helloService.routes ~
          apiDataService.routes ~
          apiPropertyService.routes ~
          apiBundleService.routes ~
          eventsService.routes ~
          locationsService.routes ~
          peopleService.routes ~
          thingsService.routes ~
          organisationsService.routes ~
          userService.routes ~
          typeService.routes ~
          dataDebitService.routes
      }
    }
  }

  def receive = runRoute(routes)
}
