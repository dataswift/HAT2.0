package dalapi

import akka.actor.{ActorRefFactory, ActorLogging}
import dalapi.service.{EventsService, DataService, HelloService, BundleService}
import spray.routing.HttpServiceActor

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

  val dataService = new DataService {
    def actorRefFactory = context
  }

  val bundleService = new BundleService {
    override implicit def actorRefFactory: ActorRefFactory = context
  }

  val inboundEventsService = new EventsService {
    def actorRefFactory = context
  }

  // Concatenate all their handled routes
  val routes = helloService.routes ~ dataService.routes ~ bundleService.routes

  def receive = runRoute(routes)
}
