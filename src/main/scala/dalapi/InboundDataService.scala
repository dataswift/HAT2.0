package dalapi

import akka.actor.Actor
import spray.routing._
import spray.http._
import MediaTypes._

// we don't implement our route structure directly in the service actor because
// we want to be able to test it independently, without having to spin up an actor
class InboundDataServiceActor extends Actor with InboundDataService {

  // the HttpService trait defines only one abstract member, which
  // connects the services environment to the enclosing actor or test
  def actorRefFactory = context

  val routes = home ~ data
  def receive = runRoute(routes)
}


// this trait defines our service behavior independently from the service actor
trait InboundDataService extends HttpService {

  val home =
    path("") {
      get {
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

  val data =
    path("data") {
      get {
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