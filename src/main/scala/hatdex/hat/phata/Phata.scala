/*
 * Copyright (C) 2016 Andrius Aucinas <andrius.aucinas@hatdex.org>
 * SPDX-License-Identifier: AGPL-3.0
 *
 * This file is part of the Hub of All Things project (HAT).
 *
 * HAT is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation, version 3 of
 * the License.
 *
 * HAT is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See
 * the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General
 * Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package hatdex.hat.phata

import akka.actor.ActorRefFactory
import akka.event.LoggingAdapter
import com.typesafe.config.ConfigFactory
import hatdex.hat.FutureTransformations
import hatdex.hat.api.DatabaseInfo
import hatdex.hat.api.actors.{ EmailMessage, EmailService }
import hatdex.hat.api.models.HatService
import hatdex.hat.api.service.{ BundleService, HatServicesService }
import hatdex.hat.authentication.HatAuthHandler.UserPassHandler
import hatdex.hat.authentication.authorization.UserAuthorization
import hatdex.hat.authentication.models.User
import hatdex.hat.authentication.{ HatServiceAuthHandler, JwtTokenHandler }
import hatdex.hat.dal.SlickPostgresDriver.api._
import hatdex.hat.dal.Tables._
import hatdex.hat.phata.service.{ NotablesService, UserProfileService }
import org.joda.time.DateTime
import org.joda.time.Duration._
import org.mindrot.jbcrypt.BCrypt
import spray.http.MediaTypes._
import spray.http.{ HttpCookie, StatusCodes, Uri }
import spray.httpx.PlayTwirlSupport._
import spray.routing
import spray.routing.HttpService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.{ Failure, Success }

trait Phata extends HttpService with UserProfileService with NotablesService with HatServiceAuthHandler with JwtTokenHandler with HatServicesService with BundleService {
  val routes = {
    respondWithMediaType(`text/html`) {
      home ~
        authHat ~
        loginPage ~
        hatLogin ~
        profile ~
        notables ~
        logout ~
        passwordChange ~
        resetPassword ~
        resetPasswordConfirm
    } ~
      assets
  }

  val logger: LoggingAdapter
  val emailService: EmailService
  val configuration = ConfigFactory.load()

  implicit def actorRefFactory: ActorRefFactory

  lazy val eventualApprovedHatServices: Future[Seq[HatService]] = hatServices(Set("app", "dataplug", "testapp"))

  def home = pathEndOrSingleSlash {
    get {
      accessTokenHandler { implicit user: User =>
        logger.debug("Showing MY HAT")
        myhat
      } ~ getProfile
    } ~ post {
      formField('username.as[String], 'password.as[String], 'remember.?, 'name.?, 'redirect.?) {
        case (username, password, remember, maybeName, maybeRedirect) => login(username, password, remember, maybeName, maybeRedirect)
      }
    }
  }

  private def login(username: String, password: String, remember: Option[String], maybeName: Option[String], maybeRedirect: Option[String]) = {
    val params = Map[String, String]("username" -> username, "password" -> password)
    val validity = if (remember.contains("remember")) {
      standardDays(30)
    }
    else {
      standardMinutes(180)
    }

    val eventualMaybeUser = UserPassHandler.authenticator(params)
    val eventualMaybeToken = eventualMaybeUser.flatMap { maybeUser =>
      // Fetch Access token if user has authenticated
      val maybeFutureToken = maybeUser.map(user => fetchOrGenerateToken(user, issuer, accessScope = user.role, validity = validity))
      // Transform option of future to future of option
      FutureTransformations.transform(maybeFutureToken)
    }

    val eventualCredentials = for {
      maybeUser <- eventualMaybeUser
      maybeToken <- eventualMaybeToken
    } yield (maybeUser, maybeToken)

    onComplete(eventualCredentials) {
      case Success((Some(user), Some(token))) =>
        val expires = spray.http.DateTime(DateTime.now().plus(validity).getMillis)
        val cookie = HttpCookie("X-Auth-Token", content = token.accessToken,
          expires = Option(expires), maxAge = Some(validity.getMillis))
        setCookie(cookie) {
          (maybeName, maybeRedirect) match {
            case (Some(name), Some(redirectUrl)) => redirect(Uri("/hatlogin").withQuery(Uri.Query("name" -> name, "redirect" -> redirectUrl)), StatusCodes.Found)
            case _                               => redirect("/", StatusCodes.Found)
          }
        }
      case Success(_) =>
        deleteCookie("X-Auth-Token") {
          (maybeName, maybeRedirect) match {
            case (Some(name), Some(redirect)) => complete(hatdex.hat.phata.views.html.login(name, redirect, formattedHatConfiguration, Some("Invalid Credentials!")))
            case _                            => complete(hatdex.hat.phata.views.html.simpleLogin(formattedHatConfiguration, Some("Invalid Credentials!")))
          }
        }
      case Failure(e) =>
        deleteCookie("X-Auth-Token") {
          (maybeName, maybeRedirect) match {
            case (Some(name), Some(redirect)) => complete(hatdex.hat.phata.views.html.login(name, redirect, formattedHatConfiguration, Some("Server error while authenticating")))
            case _                            => complete(hatdex.hat.phata.views.html.simpleLogin(formattedHatConfiguration, Some("Server error while authenticating")))
          }
        }
    }
  }

  def profile = path("profile") {
    accessTokenHandler { implicit user: User =>
      get {
        getProfile(Some(user))
      }
    } ~ {
      get {
        getProfile
      }
    }
  }

  def notables = path("notables") {
    accessTokenHandler { implicit user: User =>
      get {
        getNotables(Some(user))
      }
    } ~ {
      get {
        getNotables(None)
      }
    }
  }

  val publicResourcePath: String = conf.getString("public-resource-path")
  private def getProfile: routing.Route = getProfile(None)

  private def getProfile(maybeUser: Option[User]): routing.Route = {
    onComplete {
      getPublicProfile flatMap { profileInfo =>
        getPublicNotes map { notables =>
          (profileInfo, notables)
        }
      }
    } {
      case Success(((true, publicProfile), notables)) => complete(hatdex.hat.phata.views.html.index(publicProfile, maybeUser, notables))
      case Success(((false, publicProfile), _))       => complete(hatdex.hat.phata.views.html.indexPrivate(publicProfile, maybeUser))
      case Failure(e)                                 => complete(hatdex.hat.phata.views.html.indexPrivate(formattedHatConfiguration, maybeUser))
    }
  }

  private def getNotables(maybeUser: Option[User]): routing.Route = {
    parameters('id.?) { id =>
      onComplete(getPublicNotes) {
        case Success(notables) => {
          val selectedNotable = id.map(_.toInt) flatMap { id =>
            notables.find(_.id == id)
          } orElse {
            notables.headOption
          }
          complete(hatdex.hat.phata.views.html.notables(formattedHatConfiguration, maybeUser, selectedNotable, notables))
        }
        case Failure(e) => complete(hatdex.hat.phata.views.html.indexPrivate(formattedHatConfiguration, maybeUser))
      }
    }
  }

  def loginPage = path("signin") {
    get {
      complete(hatdex.hat.phata.views.html.simpleLogin(formattedHatConfiguration))
    }
  }

  def hatLogin = path("hatlogin") {
    get {
      accessTokenHandler { implicit user: User =>
        authorize(UserAuthorization.withRole("owner")) {
          parameters('name, 'redirect) {
            case (name: String, redirectUrl: String) =>

              val eventualResponse = eventualApprovedHatServices flatMap { approvedHatServices =>
                for {
                  service <- findOrCreateHatService(name, redirectUrl)
                  linkedService <- hatServiceLink(user, service)
                } yield {
                  if (service.setup) {
                    redirect(linkedService.url, StatusCodes.Found)
                  }
                  else {
                    val services = Seq(linkedService)
                    complete {
                      (StatusCodes.OK, hatdex.hat.phata.views.html.authenticated(user, services, formattedHatConfiguration))
                    }
                  }
                }
              }

              onComplete(eventualResponse) {
                case Success(response) => response
                case Failure(e) => complete {
                  (StatusCodes.InternalServerError, s"Error occurred while logging you into $redirectUrl: ${e.getMessage}")
                }
              }
          }
        }
      } ~ {
        parameters('name, 'redirect) {
          case (name: String, redirectUrl: String) =>
            complete {
              hatdex.hat.phata.views.html.login(name, redirectUrl, formattedHatConfiguration)
            }
        }
      }
    }
  }

  def authHat = path("hat") {
    accessTokenHandler { implicit user: User =>
      authorize(UserAuthorization.withRole("owner")) {
        myhat
      }
    }
  }

  private def myhat(implicit user: User) = {
    get {
      val futureCredentials = for {
        services <- eventualApprovedHatServices
        serviceCredentials <- Future.sequence(services.map(hatServiceLink(user, _)))
      } yield serviceCredentials

      onComplete(futureCredentials) {
        case Success(credentials) =>
          complete {
            hatdex.hat.phata.views.html.authenticated(user, credentials, formattedHatConfiguration)
          }
        case Failure(e) =>
          logger.warning(s"Error resolving access tokens for auth page: ${e.getMessage}")
          complete {
            hatdex.hat.phata.views.html.authenticated(user, Seq(), formattedHatConfiguration)
          }
      }
    }

  }

  def logout = path("logout") {
    accessTokenHandler { implicit user: User =>
      deleteCookie("X-Auth-Token") {
        redirect("/", StatusCodes.Found)
      }
    }
  }

  def passwordChange = path("password") {
    accessTokenHandler { implicit user: User =>
      authorize(UserAuthorization.withRole("owner")) {
        get {
          complete {
            hatdex.hat.phata.views.html.passwordChange(user, Seq(), changed = false, formattedHatConfiguration)
          }
        } ~ post {
          formFields('oldPassword.as[String], 'newPassword.as[String], 'confirmPassword.as[String]) {
            case (oldPassword, newPassword, confirmPassword) if newPassword != confirmPassword =>
              complete(hatdex.hat.phata.views.html.passwordChange(user, Seq("Passwords do not match"), parameters = formattedHatConfiguration))
            case (oldPassword, newPassword, confirmPassword) if !BCrypt.checkpw(oldPassword, user.pass.getOrElse("")) =>
              complete(hatdex.hat.phata.views.html.passwordChange(user, Seq("Old password incorrect"), parameters = formattedHatConfiguration))
            case (oldPassword, newPassword, confirmPassword) =>
              val passwordHash = BCrypt.hashpw(newPassword, BCrypt.gensalt())
              val tryPasswordChange = DatabaseInfo.db.run {
                UserUser.filter(_.userId === user.userId)
                  .map(user => user.pass)
                  .update(Some(passwordHash))
                  .asTry
              }
              onComplete(tryPasswordChange) {
                case Success(change) => complete(hatdex.hat.phata.views.html.passwordChange(user, Seq(), changed = true, formattedHatConfiguration))
                case Failure(e)      => complete(hatdex.hat.phata.views.html.passwordChange(user, Seq("An error occurred while changing your password, please try again"), changed = false, formattedHatConfiguration))
              }
          }

        }
      }

    }
  }

  def resetPassword = path("passwordreset") {
    get {
      complete {
        hatdex.hat.phata.views.html.passwordReset()
      }
    } ~ post {
      formField('email.as[String]) {
        case email =>
          val configuredEmail = conf.getString("hat.email")
          if (email == configuredEmail) {
            val fUser = DatabaseInfo.db.run {
              UserUser.filter(_.role === "owner").filter(_.enabled === true).take(1).result.headOption
            }

            fUser map { maybeUser =>
              maybeUser.map(User.fromDbModel) map { user =>
                fetchOrGenerateToken(user, issuer, accessScope = "passwordReset", validity = standardHours(1)) map { token =>
                  val protocol = if (conf.getBoolean("hat.tls")) {
                    "https://"
                  }
                  else {
                    "http://"
                  }
                  val resetLink = s"$protocol$issuer/passwordreset/confirm?X-Auth-Token=${token.accessToken}"
                  val message = EmailMessage("HAT - reset your password",
                    configuredEmail, // to
                    s"${conf.getString("hat.name")}@${conf.getString("hat.domain")}", // from
                    hatdex.hat.phata.views.txt.emailPasswordReset.render(user, resetLink).toString(),
                    hatdex.hat.phata.views.html.emailPasswordReset.render(user, resetLink).toString(),
                    1 minute, 5)
                  emailService.send(message)
                }
              }
            }

          }
          complete {
            hatdex.hat.phata.views.html.simpleMessage("If the email you have entered is correct, you will shortly receive an email with password reset instructions")
          }
      }
    }

  }

  def resetPasswordConfirm = path("passwordreset" / "confirm") {
    accessTokenHandler { implicit user: User =>
      // logger.info(s"Logged in user $user")
      authorize(UserAuthorization.withRole("passwordReset")) {
        parameterMap {
          case parameters =>
            get {
              complete {
                hatdex.hat.phata.views.html.passwordResetConfirm(user, Seq(), token = parameters.getOrElse("X-Auth-Token", ""))
              }
            } ~ post {
              formFields('newPassword.as[String], 'confirmPassword.as[String]) {
                case (newPassword, confirmPassword) =>
                  if (newPassword != confirmPassword) {
                    complete {
                      hatdex.hat.phata.views.html.passwordResetConfirm(user, Seq("Passwords do not match"), token = parameters.getOrElse("X-Auth-Token", ""))
                    }
                  }
                  else {
                    val passwordHash = BCrypt.hashpw(newPassword, BCrypt.gensalt())
                    val tryPasswordChange = DatabaseInfo.db.run {
                      UserUser.filter(_.userId === user.userId)
                        .map(user => user.pass)
                        .update(Some(passwordHash))
                        .asTry
                    }
                    onComplete(tryPasswordChange) {
                      case Success(change) => complete(hatdex.hat.phata.views.html.passwordResetConfirm(user, Seq(), changed = true, token = parameters.get("X-Auth-Token").get))
                      case Failure(e)      => complete(hatdex.hat.phata.views.html.passwordResetConfirm(user, Seq("An error occurred while changing your password, please try again"), changed = false, token = parameters.get("X-Auth-Token").get))
                    }
                  }
              }
            }
        }
      }
    }
  }

  def assets = pathPrefix("assets") {
    //FIXME: not a very clean way to expose the whole path
    getFromResourceDirectory("META-INF/resources/webjars/the-hat/2.0-SNAPSHOT/public")
  }

}

