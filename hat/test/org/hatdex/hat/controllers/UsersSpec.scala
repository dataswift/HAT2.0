import java.util.UUID

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.impl.User
import play.api.test.{FakeRequest, PlaySpecification, WithApplication}
import com.mohiva.play.silhouette.test._
import org.hatdex.hat.api.controllers.Users
import org.hatdex.hat.authentication.HatApiAuthEnvironment
import org.hatdex.hat.authentication.models.HatUser

class UserSpec extends PlaySpecification {

  "The `user` method" should {
    "return status 401 if authenticator but no identity was found" in new WithApplication {
      val identity = HatUser(UUID.randomUUID(), "user@hat.org", Some("pa55w0rd"), "hatuser", "owner", true)
      implicit val env = FakeEnvironment[HatApiAuthEnvironment](Seq(identity.loginInfo -> identity))
      val request = FakeRequest()
        .withAuthenticator(LoginInfo("xing", "comedian@watchmen.com"))

      val controller = app.injector.instanceOf[Users]
      val result = controller.accessToken(request)

      status(result) must equalTo(UNAUTHORIZED)
    }
  }

}