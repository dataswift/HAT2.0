package hatdex.hat.authentication

import hatdex.hat.api.DatabaseInfo
import hatdex.hat.authentication.models.{AccessToken, User}
import org.mindrot.jbcrypt.BCrypt

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

import hatdex.hat.dal.SlickPostgresDriver.simple._
import hatdex.hat.dal.Tables._

/**
 * Here we need to make authenticate function of AuthHandlers
 */

object HatAuthHandler extends DatabaseInfo {

  object AccessTokenHandler {
    val authenticator = authFunction _
    private def authFunction(params: Map[String, String]) = Future {
      val mayBeToken = params.get("access_token")
      db.withSession { implicit session:Session =>
        val mayBeUser = for {
          token <- mayBeToken
          user <- UserAccessToken.filter(_.accessToken === token).flatMap(_.userUserFk).firstOption
        } yield {
            User(user.userId, user.email, None, user.name, user.role)
          }
        mayBeUser
      }
    }
  }

  // Normally only allowing the owner to authenticate with password
  object UserPassHandler {
    val authenticator = authFunction _
    private def authFunction(params: Map[String, String]) = Future {
      val emailOpt = params.get("username")
      val passwordOpt = params.get("password")
      db.withSession { implicit session: Session =>
        val mayBeUser = for {
          email <- emailOpt
          password <- passwordOpt
          user <- UserUser.filter(_.email === email).filter(_.role === "owner").firstOption
          if BCrypt.checkpw(password, user.pass.getOrElse(""))
        } yield {
            User(user.userId, user.email, None, user.name, user.role)
          }
        mayBeUser
      }
    }
  }

  // Except for the special case, used to create authentication token
  object UserPassApiHandler {
    val authenticator = authFunction _
    private def authFunction(params: Map[String, String]) = Future {
      val emailOpt = params.get("username")
      val passwordOpt = params.get("password")
      db.withSession { implicit session: Session =>
        val mayBeUser = for {
          email <- emailOpt
          password <- passwordOpt
          user <- UserUser.filter(_.email === email).firstOption
          if BCrypt.checkpw(password, user.pass.getOrElse(""))
        } yield {
            User(user.userId, user.email, None, user.name, user.role)
          }
        mayBeUser
      }
    }
  }
}