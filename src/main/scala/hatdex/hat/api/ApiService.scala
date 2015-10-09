package hatdex.hat.api

import akka.actor.{ActorRefFactory, ActorLogging}
import akka.event.Logging
import hatdex.hat.api.service._
import hatdex.hat.authentication.HatAuthHandler
import hatdex.hat.authentication.authenticators.{UserPassHandler, AccessTokenHandler}
import spray.http.{HttpResponse, HttpRequest}
import spray.routing.HttpServiceActor
import spray.routing.directives.LogEntry

// we don't implement our route structure directly in the service actor because
// we want to be able to test it independently, without having to spin up an actor
class ApiService extends HttpServiceActor with ActorLogging {
  // the HttpService trait defines only one abstract member, which
  // connects the services environment to the enclosing actor or test
  override def actorRefFactory = context

  // Initialise all the service the actor handles
  val helloService = new HelloService {
    def actorRefFactory = context
  }

  val apiDataService = new DataService {
    def actorRefFactory = context
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

  // logs request method, uri and response status at debug level
  def requestMethodAndResponseStatusAsInfo(req: HttpRequest): Any => Option[LogEntry] = {
    case res: HttpResponse => Some(LogEntry(req.method + ":" + req.uri + ":" + res.message.status, Logging.DebugLevel))
    case _ => None // other kind of responses
  }


  // Concatenate all their handled routes
  val routes = logRequestResponse(requestMethodAndResponseStatusAsInfo _) {
    helloService.routes ~
      apiDataService.routes ~
      apiPropertyService.routes ~
      apiBundleService.routes ~
      eventsService.routes ~
      locationsService.routes ~
      peopleService.routes ~
      thingsService.routes ~
      organisationsService.routes ~
      userService.routes
  }

  def receive = runRoute(routes)
}
