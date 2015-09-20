package dalapi

import dalapi.service.DataService
import org.specs2.mutable.Specification
import spray.http.HttpMethods._
import spray.http.StatusCodes._
import spray.http._
import spray.testkit.Specs2RouteTest

class DataServiceSpec extends Specification with Specs2RouteTest with DataService {
  def actorRefFactory = system

  val tableKitchenElectricity = """{
    "name": "kichenElectricity",
    "source": "fibaro"
  }"""

  sequential

  "DataService" should {
    "Accept a new table created" in {
      HttpRequest(POST, "/table", entity = HttpEntity(MediaTypes.`application/json`, tableKitchenElectricity)) ~> createTable ~> check {
        response.status should be equalTo Created
        responseAs[String] must contain("kichenElectricity")
      }
    }
  }
}
