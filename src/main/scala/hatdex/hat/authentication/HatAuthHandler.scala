package hatdex.hat.authentication

import hatdex.hat.api.DatabaseInfo
import hatdex.hat.authentication.models.{AccessToken, User}
import org.mindrot.jbcrypt.BCrypt

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

import hatdex.hat.dal.SlickPostgresDriver.api._
import hatdex.hat.dal.Tables._

/**
 * Here we need to make authenticate function of AuthHandlers
 */

object HatAuthHandler {

  object AccessTokenHandler extends JwtTokenHandler {
    val authenticator = authFunction _
    private def authFunction(params: Map[String, String]): Future[Option[User]] = {
      println(s"[AuthHandler] $params")
      val mayBeToken = Future {
        params.get("X-Auth-Token")
      }

      mayBeToken flatMap {
        case Some(token: String) if validateJwtToken(token) =>
          println(s"[AuthHandler] maybe token: $token")
          val userQuery = UserAccessToken.filter(_.accessToken === token).flatMap(_.userUserFk).filter(_.enabled === true).take(1)
          val matchingUsers = DatabaseInfo.db.run(userQuery.result)
          val maybeRole = getTokenAccessScope(token)
          matchingUsers.map { users =>
            users.headOption
              .map(user => User(user.userId, user.email, None, user.name, maybeRole.getOrElse("")))
          }
        case Some(_) =>
          // Invalid token
          Future.successful(None)
        case None =>
          Future.successful(None)
      }
    }
  }

  // Normally only allowing the owner to authenticate with password
  object UserPassHandler {
    val authenticator = authFunction _
    private def authFunction(params: Map[String, String]): Future[Option[User]] = {
      val emailOpt = params.get("username")
      val passwordOpt = params.get("password")

      val maybeCredentials = Future {
        for {
          email <- emailOpt
          password <- passwordOpt
        } yield (email, password)
      }

      maybeCredentials flatMap {
        case Some((email, password)) =>
          val userQuery = UserUser.filter(_.email === email).filter(_.enabled === true).take(1)
          val matchingUsers = DatabaseInfo.db.run(userQuery.result)
          matchingUsers.map { users =>
            users.headOption
              .filter(user => BCrypt.checkpw(password, user.pass.getOrElse("")))
              .map(user => User(user.userId, user.email, None, user.name, user.role))
          }
        case None =>
          Future.successful(None)
      }
    }
  }
}