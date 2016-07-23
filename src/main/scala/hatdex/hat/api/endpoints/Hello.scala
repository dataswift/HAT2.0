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
import hatdex.hat.api.DatabaseInfo
import hatdex.hat.api.actors.{ EmailMessage, EmailService }
import hatdex.hat.api.models.HatService
import hatdex.hat.api.service.BundleService
import hatdex.hat.authentication.HatAuthHandler.UserPassHandler
import hatdex.hat.authentication.authorization.UserAuthorization
import hatdex.hat.authentication.models.User
import hatdex.hat.authentication.{ HatServiceAuthHandler, JwtTokenHandler }
import hatdex.hat.dal.SlickPostgresDriver.api._
import hatdex.hat.dal.Tables.UserUser
import org.joda.time.Duration._
import org.joda.time.LocalDateTime
import org.mindrot.jbcrypt.BCrypt
import spray.http.MediaTypes._
import spray.http.{ HttpCookie, StatusCodes }
import spray.routing.HttpService
import spray.httpx.SprayJsonSupport._
import spray.httpx.PlayTwirlSupport._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.{ Failure, Success }

trait Hello extends HttpService with HatServiceAuthHandler with JwtTokenHandler with BundleService {
  val routes = {
    home ~
      authHat ~
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

  def home = pathEndOrSingleSlash {
    get {
      respondWithMediaType(`text/html`) {
        accessTokenHandler { implicit user: User =>
          myhat
        } ~ {
          onComplete(getPublicProfile) {
            case Success((true, publicFields)) => complete {
              hatdex.hat.views.html.index(formatProfile(publicFields.toSeq))
            }
            case Success((false, publicFields)) => complete {
              logger.info("Private profile!");
              hatdex.hat.views.html.indexPrivate(formatProfile(publicFields.toSeq))
            }
            case Failure(e) => complete {
              logger.info(s"Failure getting table: ${e.getMessage}");
              hatdex.hat.views.html.indexPrivate(Map())
            }
          }
        }
      }
    } ~ post {
      formField('username.as[String], 'password.as[String], 'remember.?) {
        case (username, password, remember) =>
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
                redirect("/", StatusCodes.Found)
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
    }
  }

  def profile = path("profile") {
    get {
      accessTokenHandler { implicit user: User =>
        authorize(UserAuthorization.withRole("owner")) {
          onComplete(getPublicProfile) {
            case Success((true, publicFields))  => complete(hatdex.hat.views.html.index(formatProfile(publicFields.toSeq)))
            case Success((false, publicFields)) => complete(hatdex.hat.views.html.indexPrivate(formatProfile(publicFields.toSeq)))
            case Failure(e)                     => complete(hatdex.hat.views.html.indexPrivate(Map()))
          }
        }
      }
    }
  }

  private def getPublicProfile: Future[(Boolean, Iterable[ProfileField])] = {
    val eventualMaybeProfileTable = sourceDatasetTables(Seq(("rumpel", "profile")), None).map(_.headOption)
    val eventualMaybeFacebookTable = sourceDatasetTables(Seq(("facebook", "profile_picture")), None).map(_.headOption)
    val eventualProfileRecord = eventualMaybeProfileTable flatMap { maybeTable =>
      maybeTable map { table =>
        val fieldset = getStructureFields(table)

        val startTime = LocalDateTime.now().minusDays(365)
        val endTime = LocalDateTime.now()
        val eventualValues = fieldsetValues(fieldset, startTime, endTime)

        eventualValues.map(values => getValueRecords(values, Seq(table)))
          .map { records => records.headOption }
          .map(_.flatMap(_.tables.flatMap(_.headOption)))
      } getOrElse {
        Future.successful(None)
      }
    }

    val eventualProfilePicture = eventualMaybeFacebookTable flatMap { maybeTable =>
      maybeTable map { table =>
        val fieldset = getStructureFields(table)
        val startTime = LocalDateTime.now().minusDays(365)
        val endTime = LocalDateTime.now()
        val eventualValues = fieldsetValues(fieldset, startTime, endTime)
        eventualValues.map(values => getValueRecords(values, Seq(table)))
          .map { records => records.headOption }
          .map { record => record.flatMap(_.tables.flatMap(_.headOption)) }
      } getOrElse {
        Future.successful(None)
      }
    }

    val eventualProfilePictureField = eventualProfilePicture map { maybeValueTable =>
      maybeValueTable map { valueTable =>
        val flattenedValues = flattenTableValues(valueTable)
        ProfileField("fb_profile_picture", Map("url" -> flattenedValues.getOrElse("url", "").toString), true)
      }
    }

    val profile = for {
      profilePictureField <- eventualProfilePictureField
      valueTable <- eventualProfileRecord.map(_.get)
    } yield {
      val flattenedValues = flattenTableValues(valueTable)
      val publicProfile = flattenedValues.get("private").contains("false")
      val profileFields = flattenedValues.collect {
        case ("fb_profile_photo", m: Map[String, String]) if profilePictureField.isDefined =>
          val publicField = m.get("private").contains("false")
          profilePictureField.get.copy(fieldPublic = publicField)
        case (fieldName, m: Map[String, String]) =>
          val publicField = m.get("private").contains("false")
          ProfileField(fieldName, m - "private", publicField)
      }

      (publicProfile, profileFields.filter(_.fieldPublic))
    }

    profile recover {
      case e =>
        (false, Iterable())
    }
  }

  private def formatProfile(profileFields: Seq[ProfileField]): Map[String, Map[String, String]] = {
    val hatParameters: Map[String, String] = Map(
      "hatName" -> conf.getString("hat.name"),
      "hatDomain" -> conf.getString("hat.domain"),
      "hatAddress" -> s"${conf.getString("hat.name")}.${conf.getString("hat.domain")}")

    val links = Map(profileFields collect {
      // links
      case ProfileField("facebook", values, true) => "Facebook" -> values.getOrElse("link", "")
      case ProfileField("website", values, true)  => "Web" -> values.getOrElse("link", "")
      case ProfileField("youtube", values, true)  => "Youtube" -> values.getOrElse("link", "")
      case ProfileField("linkedin", values, true) => "LinkedIn" -> values.getOrElse("link", "")
      case ProfileField("google", values, true)   => "Google" -> values.getOrElse("link", "")
      case ProfileField("blog", values, true)     => "Blog" -> values.getOrElse("link", "")
      case ProfileField("twitter", values, true)  => "Twitter" -> values.getOrElse("link", "")
    }: _*).filterNot(_._2 == "").map { case (k, v) =>
      k -> (if (v.startsWith("http:")) { v } else { s"http://$v" })
    }

    val contact = Map(profileFields collect {
      // contact
      case ProfileField("primary_email", values, true)     => "primary_email" -> values.getOrElse("value", "")
      case ProfileField("alternative_email", values, true) => "alternative_email" -> values.getOrElse("value", "")
      case ProfileField("mobile", values, true)            => "mobile" -> values.getOrElse("no", "")
      case ProfileField("home_phone", values, true)        => "home_phone" -> values.getOrElse("no", "")
    }: _*).filterNot(_._2 == "")

    val personal = Map(profileFields collect {
      case ProfileField("fb_profile_picture", values, true) => "profile_picture" -> values.getOrElse("url", "")
      // address
      case ProfileField("address_global", values, true) => "address_global" -> {
        values.getOrElse("city", "")+" "+
          values.getOrElse("county", "")+" "+
          values.getOrElse("country", "")
      }
      case ProfileField("address_details", values, true) => "address_details" -> {
        values.getOrElse("address_details", "")
      }

      case ProfileField("personal", values, true) =>
        "personal" -> {
          val title = values.get("title").map(_+" ").getOrElse("")
          val preferredName = values.get("preferred_name").map(_+" ").getOrElse("")
          val firstName = values.get("first_name").map { n =>
            if (preferredName != "" && preferredName != n+" ") {
              s"($n) "
            }
            else if (preferredName == "") {
              s"$n "
            }
            else {
              ""
            }
          }.getOrElse("")
          val middleName = values.get("middle_name").map(_+" ").getOrElse("")
          val lastName = values.getOrElse("last_name", "")
          s"$title$preferredName$firstName$middleName$lastName"
        }

      case ProfileField("emergency_contact", values, true) =>
        "emergency_contact" -> {
          values.getOrElse("first_name", "")+" "+
            values.getOrElse("last_name", "")+" "+
            values.getOrElse("relationship", "")+" "+
            ": "+values.getOrElse("mobile", "")+" "
        }
      case ProfileField("gender", values, true) => "gender" -> values.getOrElse("type", "")

      case ProfileField("nick", values, true)   => "nick" -> values.getOrElse("type", "")
      case ProfileField("age", values, true)    => "age" -> values.getOrElse("group", "")
      case ProfileField("birth", values, true)  => "brithDate" -> values.getOrElse("date", "")
    }: _*).filterNot(_._2 == "")

    val about = Map[String, String](
      "title" -> profileFields.find(_.name == "about").map(_.values.getOrElse("title", "")).getOrElse(""),
      "body" -> profileFields.find(_.name == "about").map(_.values.getOrElse("body", "")).getOrElse(""))

    //    val profile = hatParameters ++ profileParameters.filterNot(_._2 == "")

    val profile = Map(
      "hat" -> hatParameters,
      "links" -> links,
      "contact" -> contact,
      "profile" -> personal,
      "about" -> about).filterNot(_._2.isEmpty)

    profile
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
        val services = Seq(
          HatService("MarketSquare", "", "/assets/images/MarketSquare-logo.svg", "https://marketsquare.hubofallthings.com", "/authenticate/hat?token=", browser = false),
          HatService("Rumpel", "", "/assets/images/Rumpel-logo.svg", "https://rumpel.hubofallthings.com", "/users/authenticate/", browser = true))

        val serviceCredentials = services.map { service =>
          val token = if (service.browser == false) {
            fetchOrGenerateToken(user, resource = service.url, accessScope = "validate", validity = standardDays(1))
          }
          else {
            fetchOrGenerateToken(user, issuer, accessScope = user.role)
          }
          token.map { accessToken =>
            service.copy(authUrl = service.authUrl + accessToken.accessToken)
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

  case class ProfileField(name: String, values: Map[String, String], fieldPublic: Boolean)

}
