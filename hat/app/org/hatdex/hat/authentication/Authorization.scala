package org.hatdex.hat.authentication

import com.mohiva.play.silhouette.api.{ Authenticator, Authorization }
import com.mohiva.play.silhouette.impl.authenticators.{ CookieAuthenticator, JWTRS256Authenticator }
import org.hatdex.hat.authentication.models.HatUser
import play.api.mvc.Request

import scala.concurrent.Future

/**
 * Only allows those users that have at least a service of the selected.
 * Master service is always allowed.
 * Ex: WithService("serviceA", "serviceB") => only users with services "serviceA" OR "serviceB" (or "master") are allowed.
 */
case class WithRole(anyOf: String*) extends Authorization[HatUser, JWTRS256Authenticator] {
  def isAuthorized[B](user: HatUser, authenticator: JWTRS256Authenticator)(implicit r: Request[B]): Future[Boolean] = {
    Future.successful {
      WithRole.isAuthorized(user, anyOf: _*)
    }
  }

}
object WithRole {
  def isAuthorized(user: HatUser, anyOf: String*): Boolean =
    anyOf.contains(user.role)
}

case class HasFrontendRole(anyOf: String*) extends Authorization[HatUser, CookieAuthenticator] {
  def isAuthorized[B](user: HatUser, authenticator: CookieAuthenticator)(implicit r: Request[B]): Future[Boolean] = {
    Future.successful {
      WithRole.isAuthorized(user, anyOf: _*)
    }
  }
}
object HasFrontendRole {
  def isAuthorized(user: HatUser, anyOf: String*): Boolean =
    anyOf.contains(user.role)
}

/**
 * Only allows those users that have every of the selected services.
 * Master service is always allowed.
 * Ex: Restrict("serviceA", "serviceB") => only users with services "serviceA" AND "serviceB" (or "master") are allowed.
 */
case class WithRoles(allOf: String*) extends Authorization[HatUser, JWTRS256Authenticator] {
  def isAuthorized[B](user: HatUser, authenticator: JWTRS256Authenticator)(implicit r: Request[B]): Future[Boolean] = {
    Future.successful {
      WithRoles.isAuthorized(user, allOf: _*)
    }
  }
}
object WithRoles {
  def isAuthorized(user: HatUser, allOf: String*): Boolean =
    allOf.forall(_ == user.role)
}
