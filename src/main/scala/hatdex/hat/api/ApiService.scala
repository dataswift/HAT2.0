package hatdex.hat.api

import akka.actor.{ActorRefFactory, ActorLogging}
import akka.event.Logging
import hatdex.hat.api.models.ErrorMessage
import hatdex.hat.api.service._
import hatdex.hat.authentication.HatAuthHandler
import hatdex.hat.authentication.authenticators.{UserPassHandler, AccessTokenHandler}
import spray.http._
import spray.json.DefaultJsonProtocol._
import spray.routing.{MalformedRequestContentRejection, RejectionHandler, Rejected, HttpServiceActor}
import spray.routing.directives.LogEntry

import scala.util.parsing.json.JSONObject

// we don't implement our route structure directly in the service actor because
// we want to be able to test it independently, without having to spin up an actor
class ApiService extends HttpServiceActor with ActorLogging with Cors {
  // the HttpService trait defines only one abstract member, which
  // connects the services environment to the enclosing actor or test
  override def actorRefFactory = context

  // Initialise all the service the actor handles
  val helloService = new HelloService {
    def actorRefFactory = context
  }

  val apiDataService = new DataService {
    def actorRefFactory = context

    val logger = log
  }

  val apiPropertyService = new PropertyService {
    def actorRefFactory = context

    val dataService = apiDataService
  }

  val apiBundleService = new BundleService {
    def actorRefFactory = context

    val dataService = apiDataService
  }

  val eventsService = new EventsService {
    def actorRefFactory = context

    val dataService = apiDataService
    val propertyService = apiPropertyService
  }

  val locationsService = new LocationsService {
    def actorRefFactory = context

    val dataService = apiDataService
    val propertyService = apiPropertyService
  }

  val peopleService = new PeopleService {
    def actorRefFactory = context

    val dataService = apiDataService
    val propertyService = apiPropertyService
  }

  val thingsService = new ThingsService {
    def actorRefFactory = context

    val dataService = apiDataService
    val propertyService = apiPropertyService
  }

  val organisationsService = new OrganisationsService {
    def actorRefFactory = context

    val dataService = apiDataService
    val propertyService = apiPropertyService
  }

  val dataDebitService = new DataDebitService {
    override val bundleService: BundleService = apiBundleService

    override implicit def actorRefFactory: ActorRefFactory = context
  }

  val userService = new UserService {
    override implicit def actorRefFactory: ActorRefFactory = context
  }

  val typeService = new TypeService {
    override implicit def actorRefFactory: ActorRefFactory = context
    val logger = log
  }

  // logs request method, uri and response status at debug level
  def requestMethodAndResponseStatusAsInfo(req: HttpRequest): Any => Option[LogEntry] = {
    case res: HttpResponse => Some(LogEntry(req.method + ":" + req.uri + ":" + res.message.status, Logging.InfoLevel))
    case Rejected(rejections) => Some(LogEntry(req.method + ":" + req.uri + ":" + rejections.toString(), Logging.ErrorLevel)) // log rejections
    case _ => None // other kind of responses
  }

  //  def jsonify(response: HttpResponse): HttpResponse = {
  //    response.withEntity(HttpBody(ContentType.`application/json`,
  //      JSONObject(Map(
  //        "details" -> response.entity.asString,
  //        "errorType" -> "API Error"
  //      )).toString()))
  //  }

  //  implicit val apiRejectionHandler = RejectionHandler {
  //    case MalformedRequestContentRejection(msg, cause) :: Nil =>
  //      complete((StatusCodes.BadRequest, ApiError("The request content was malformed", msg)))
  //  }

  import spray.httpx.SprayJsonSupport._
  implicit val errorFormat = jsonFormat2(ErrorMessage.apply)

  implicit val jsonRejectionHandler = RejectionHandler {
    case MalformedRequestContentRejection (msg, cause) :: Nil =>
      complete (StatusCodes.BadRequest, ErrorMessage ("The request content was malformed", msg) )

    // add more custom handling here

    // default case that wraps the Default error message into json
    case x if RejectionHandler.Default.isDefinedAt (x) =>
      ctx => RejectionHandler.Default (x) {
        ctx.withHttpResponseMapped {
          case resp@HttpResponse (_, HttpEntity.NonEmpty (ContentType (MediaTypes.`text/plain`, _), msg), _, _) =>

            import spray.httpx.marshalling

            resp.withEntity (marshalling.marshalUnsafe (ErrorMessage (msg.asString, "") ) )
        }
      }
  }

  // Concatenate all their handled routes
  val routes = logRequestResponse(requestMethodAndResponseStatusAsInfo _) {
    cors {
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
        typeService.routes
    }
  }

  def receive = runRoute(routes)
}