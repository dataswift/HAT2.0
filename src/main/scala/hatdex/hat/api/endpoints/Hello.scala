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
package hatdex.hat.api.endpoints

import akka.actor.ActorRefFactory
import akka.event.LoggingAdapter
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import hatdex.hat.api.DatabaseInfo
import hatdex.hat.api.actors.{ EmailMessage, EmailService }
import hatdex.hat.api.models.HatService
import hatdex.hat.api.service.{ UserProfileService, BundleService }
import hatdex.hat.authentication.HatAuthHandler.UserPassHandler
import hatdex.hat.authentication.authorization.UserAuthorization
import hatdex.hat.authentication.models.User
import hatdex.hat.authentication.{ HatServiceAuthHandler, JwtTokenHandler }
import hatdex.hat.dal.SlickPostgresDriver.api._
import hatdex.hat.dal.Tables._
import org.joda.time.Duration._
import org.joda.time.LocalDateTime
import org.mindrot.jbcrypt.BCrypt
import spray.http.MediaTypes._
import spray.http.{ Uri, HttpCookie, StatusCodes }
import spray.routing.HttpService
import spray.httpx.SprayJsonSupport._
import spray.httpx.PlayTwirlSupport._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.{ Failure, Success }

trait Hello extends HttpService with UserProfileService with HatServiceAuthHandler with JwtTokenHandler with BundleService {
  val routes = {
    home ~
      authHat ~
      hatLogin ~
      profile ~
      logout ~
      getPublicKey ~
      assets ~
      passwordChange ~
      resetPassword ~
      resetPasswordConfirm
  }
  val logger: LoggingAdapter
  val emailService: EmailService

  implicit def actorRefFactory: ActorRefFactory

  val approvedHatServices = Seq(
    HatService("Personal HAT page", "", "/assets/images/haticon.png", "", "/profile", browser = false),
    HatService("MarketSquare", "", "/assets/images/MarketSquare-logo.svg", "https://marketsquare.hubofallthings.com", "/authenticate/hat", browser = false),
    HatService("Rumpel", "", "/assets/images/Rumpel-logo.svg", "https://rumpel.hubofallthings.com", "/users/authenticate", browser = true),
    HatService("Rumpel", "", "/assets/images/Rumpel-logo.svg", "http://rumpel-stage.hubofallthings.com.s3-website-eu-west-1.amazonaws.com", "/users/authenticate", browser = true))

  def home = pathEndOrSingleSlash {
    get {
      respondWithMediaType(`text/html`) {
        accessTokenHandler { implicit user: User =>
          logger.debug("Showing MY HAT")
          myhat
        } ~ {
          onComplete(getPublicProfile) {
            case Success((true, publicFields))  => complete(hatdex.hat.views.html.index(formatProfile(publicFields.toSeq)))
            case Success((false, publicFields)) => complete(hatdex.hat.views.html.indexPrivate(formatProfile(publicFields.toSeq)))
            case Failure(e)                     => complete(hatdex.hat.views.html.indexPrivate(Map()))
          }
        }
      }
    } ~ post {
      formField('username.as[String], 'password.as[String], 'remember.?, 'name.?, 'redirect.?) {
        case (username, password, remember, maybeName, maybeRedirect) => login(username, password, remember, maybeName, maybeRedirect)
      }
    }
  }

  private def login(username: String, password: String, remember: Option[String], maybeName: Option[String], maybeRedirect: Option[String]) = {
    val params = Map[String, String]("username" -> username, "password" -> password)
    val validity = if (remember.contains("remember")) {
      standardDays(7)
    }
    else {
      standardMinutes(60)
    }
    val fUser = UserPassHandler.authenticator(params)
    val fToken = fUser.flatMap { maybeUser =>
      // Fetch Access token if user has authenticated
      val maybeFutureToken = maybeUser.map(user => fetchOrGenerateToken(user, issuer, accessScope = user.role, validity = validity))
      // Transform option of future to future of option
      maybeFutureToken.map { futureToken =>
        futureToken.map { token =>
          Some(token)
        }
      } getOrElse {
        Future.successful(None)
      }
    }

    val fCredentials = for {
      maybeUser <- fUser
      maybeToken <- fToken
    } yield (maybeUser, maybeToken)

    onComplete(fCredentials) {
      case Success((Some(user), Some(token))) =>
        setCookie(HttpCookie("X-Auth-Token", content = token.accessToken)) {
          (maybeName, maybeRedirect) match {
            case (Some(name), Some(redirectUrl)) => redirect(Uri("/hatlogin").withQuery(Uri.Query("name" -> name, "redirect" -> redirectUrl)), StatusCodes.Found)
            case _                               => redirect("/", StatusCodes.Found)
          }
        }
      case Success(_) =>
        deleteCookie("X-Auth-Token") {
          complete(hatdex.hat.views.html.simpleMessage("Invalid Credentials!", formatProfile(Seq())))
        }
      case Failure(e) =>
        deleteCookie("X-Auth-Token") {
          complete {
            logger.error(s"Error while authenticating: ${e.getMessage}")
            hatdex.hat.views.html.simpleMessage("Error while authenticating", formatProfile(Seq()))
          }
        }
    }
  }

  def profile = path("profile") {
    get {
      onComplete(getPublicProfile) {
        case Success((true, publicFields))  => complete(hatdex.hat.views.html.index(formatProfile(publicFields.toSeq)))
        case Success((false, publicFields)) => complete(hatdex.hat.views.html.indexPrivate(formatProfile(publicFields.toSeq)))
        case Failure(e)                     => complete(hatdex.hat.views.html.indexPrivate(Map()))
      }
    }
  }

  def hatLogin = path("hatlogin") {
    get {
      respondWithMediaType(`text/html`) {
        accessTokenHandler { implicit user: User =>
          authorize(UserAuthorization.withRole("owner")) {
            parameters('name, 'redirect) { (name: String, redirectUrl: String) =>
              val redirectUri = Uri(redirectUrl)
              val service = approvedHatServices.find(s => s.title == name && redirectUrl.startsWith(s.url))
                .map(_.copy(url = s"${redirectUri.scheme}:${redirectUri.authority.toString}" /*, authUrl = redirectUri.toRelative.toString()*/ ))
                .getOrElse(HatService(name, redirectUrl, "/assets/images/haticon.png", redirectUrl, redirectUri.path.toString(), browser = false))

              val accessScope = "validate"
              val resource = service.url
              val validity = standardDays(1)

              val eventualResponse = fetchToken(user, resource, accessScope, validity) flatMap { maybeToken =>
                maybeToken map { token =>
                  // known service, redirect

                  // get a fresh token
                  val eventuallyFreshToken = if (service.browser == false) {
                    fetchOrGenerateToken(user, resource = service.url, accessScope = "validate", validity = standardDays(1))
                  }
                  else {
                    fetchOrGenerateToken(user, issuer, accessScope = user.role)
                  }

                  eventuallyFreshToken map { token =>
                    val uri = Uri(service.url).withPath(Uri.Path(service.authUrl)).withQuery(Uri.Query("token" -> token.accessToken))
                    val redirectUrl = uri.toString
                    redirect(redirectUrl, StatusCodes.Found)
                  }
                } getOrElse {
                  // show page for login confirmation
                  val eventualToken = fetchOrGenerateToken(user, resource, accessScope, validity)
                  eventualToken.flatMap { token =>
                    val uri = Uri(service.url).withPath(Uri.Path(service.authUrl)).withQuery(Uri.Query("token" -> token.accessToken))
                    val services = Seq(service.copy(url = uri.toString()))
                    Future.successful(complete((StatusCodes.OK, hatdex.hat.views.html.authenticated(user, services))))
                  }
                }
              }

              onComplete(eventualResponse) {
                case Success(response) => response
                case Failure(e)        => complete((StatusCodes.InternalServerError, s"Error occurred while logging you into $redirectUrl: ${e.getMessage}"))
              }
            }
          }
        } ~ {
          parameters('name, 'redirect) { (name: String, redirectUrl: String) =>
            val configuration = getHatConfiguration
            complete(hatdex.hat.views.html.login(name, redirectUrl, Map("hat" -> configuration)))
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

  def myhat(implicit user: User) = {
    respondWithMediaType(`text/html`) {
      get {
        val services = approvedHatServices

        val serviceCredentials = services.map { service =>
          val token = if (service.browser == false) {
            fetchOrGenerateToken(user, resource = service.url, accessScope = "validate", validity = standardDays(1))
          }
          else {
            fetchOrGenerateToken(user, issuer, accessScope = user.role)
          }
          token.map { accessToken =>
            if (service.url.nonEmpty) {
              if (service.browser == false) {
                val uri = Uri(service.url).withPath(Uri.Path(service.authUrl)).withQuery(Uri.Query("token" -> accessToken.accessToken))
                service.copy(url = uri.toString())
              }
              else {
                val uri = Uri(service.url).withPath(Uri.Path(service.authUrl+"/"+accessToken.accessToken))
                service.copy(url = uri.toString())
              }
            }
            else {
              service
            }
          }
        }
        val futureCredentials = Future.sequence(serviceCredentials)
        onComplete(futureCredentials) {
          case Success(credentials) =>
            complete {
              hatdex.hat.views.html.authenticated(user, credentials)
            }
          case Failure(e) =>
            logger.warning(s"Error resolving access tokens for auth page: ${e.getMessage}")
            complete {
              hatdex.hat.views.html.authenticated(user, Seq())
            }
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
        respondWithMediaType(`text/html`) {
          get {
            complete {
              hatdex.hat.views.html.passwordChange(user, Seq(), false)
            }
          } ~ post {
            formField('oldPassword.as[String], 'newPassword.as[String], 'confirmPassword.as[String]) {
              case (oldPassword, newPassword, confirmPassword) =>
                if (newPassword != confirmPassword) {
                  complete {
                    hatdex.hat.views.html.passwordChange(user, Seq("Passwords do not match"))
                  }
                }
                else if (!BCrypt.checkpw(oldPassword, user.pass.getOrElse(""))) {
                  complete {
                    hatdex.hat.views.html.passwordChange(user, Seq("Old password incorrect"))
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
                    case Success(change) => complete(hatdex.hat.views.html.passwordChange(user, Seq(), true))
                    case Failure(e)      => complete(hatdex.hat.views.html.passwordChange(user, Seq("An error occurred while changing your password, please try again"), false))
                  }
                }
            }

          }
        }
      }
    }
  }

  def resetPassword = path("passwordreset") {
    respondWithMediaType(`text/html`) {
      get {
        complete {
          hatdex.hat.views.html.passwordReset()
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
                      hatdex.hat.views.txt.emailPasswordReset.render(user, resetLink).toString(),
                      hatdex.hat.views.html.emailPasswordReset.render(user, resetLink).toString(),
                      1 minute, 5)
                    emailService.send(message)
                  }
                }
              }

            }
            complete {
              hatdex.hat.views.html.simpleMessage("If the email you have entered is correct, you will shortly receive an email with password reset instructions")
            }
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
                hatdex.hat.views.html.passwordResetConfirm(user, Seq(), token = parameters.get("X-Auth-Token").getOrElse(""))
              }
            } ~ post {
              formField('newPassword.as[String], 'confirmPassword.as[String]) {
                case (newPassword, confirmPassword) =>
                  if (newPassword != confirmPassword) {
                    complete {
                      hatdex.hat.views.html.passwordResetConfirm(user, Seq("Passwords do not match"), token = parameters.get("X-Auth-Token").getOrElse(""))
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
                      case Success(change) => complete(hatdex.hat.views.html.passwordResetConfirm(user, Seq(), true, token = parameters.get("X-Auth-Token").get))
                      case Failure(e)      => complete(hatdex.hat.views.html.passwordResetConfirm(user, Seq("An error occurred while changing your password, please try again"), false, token = parameters.get("X-Auth-Token").get))
                    }
                  }
              }
            }
        }
      }
    }
  }

  def getPublicKey = path("publickey") {
    get {
      respondWithMediaType(`text/plain`) {
        complete {
          conf.getString("auth.publicKey")
        }
      }
    }
  }

  def assets = pathPrefix("assets") {
    getFromResourceDirectory("assets")
  }

}

