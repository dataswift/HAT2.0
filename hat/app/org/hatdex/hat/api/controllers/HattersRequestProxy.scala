package org.hatdex.hat.api.controllers

import akka.Done
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.util.PasswordHasherRegistry
import com.mohiva.play.silhouette.api.Silhouette
import javax.inject.Inject
import org.hatdex.hat.api.json.ApplicationJsonProtocol
import org.hatdex.hat.api.models._
import org.hatdex.hat.api.service.{ MailTokenService, RemoteExecutionContext, UsersService }
import org.hatdex.hat.authentication.{ HatApiAuthEnvironment, HatApiController }
import org.hatdex.hat.phata.models.{ HatClaimCompleteRequest, MailTokenUser }
import org.hatdex.hat.api.json.HatJsonFormats._
import org.hatdex.hat.resourceManagement.HatServer
import org.hatdex.hat.utils.HatBodyParsers
import play.api.{ Configuration, Logger }
import play.api.libs.json.Json
import play.api.libs.ws.WSClient
import play.api.mvc.{ Action, ControllerComponents }

import scala.concurrent.Future

class HattersRequestProxy @Inject() (
    components: ControllerComponents,
    parsers: HatBodyParsers,
    silhouette: Silhouette[HatApiAuthEnvironment],
    wsClient: WSClient)(
    implicit
    val ec: RemoteExecutionContext,
    tokenService: MailTokenService[MailTokenUser],
    authInfoRepository: AuthInfoRepository[HatServer],
    passwordHasherRegistry: PasswordHasherRegistry,
    usersService: UsersService,
    configuration: Configuration)
  extends HatApiController(components, silhouette) with ApplicationJsonProtocol {

  protected val logger = Logger(this.getClass)
  protected val path = "api/products/hat/claim"
  protected val hattersUrl = s"${configuration.underlying.getString("hatters.scheme")}${configuration.underlying.getString("hatters.address")}"

  def proxyRequestHatClaim(claimToken: String): Action[HatClaimCompleteRequest] = UserAwareAction.async(parsers.json[HatClaimCompleteRequest]) { implicit request =>
    implicit val hatClaimComplete: HatClaimCompleteRequest = request.body
    logger.info(s"Proxy POST request to $hattersUrl/$path with parameters: ${request.queryString}")

    tokenService.retrieve(claimToken).flatMap {
      case Some(token) if token.isSignUp && !token.isExpired && token.email == request.dynamicEnvironment.ownerEmail =>
        usersService.listUsers.map(_.find(_.roles.contains(Owner()))).flatMap {
          case Some(user) =>
            for {
              _ <- updateHatMembership(hatClaimComplete)
              _ <- authInfoRepository.update(user.loginInfo, passwordHasherRegistry.current.hash(request.body.password))
              _ <- tokenService.expire(token.id)
              authenticator <- env.authenticatorService.create(user.loginInfo)
              result <- env.authenticatorService.renew(authenticator, Ok(Json.toJson(SuccessResponse("HAT claimed"))))
            } yield {
              //env.eventBus.publish(LoginEvent(user, request))
              //mailer.passwordChanged(token.email, user)
              result
            }

          case None => Future.successful(Unauthorized(Json.toJson(ErrorMessage("HAT claim unauthorized", "No user matching token"))))
        }

      case Some(_) =>
        Future.successful(Unauthorized(Json.toJson(ErrorMessage("Invalid Token", "Token expired or invalid"))))

      case None =>
        Future.successful(Unauthorized(Json.toJson(ErrorMessage("Invalid Token", "Token does not exist"))))
    }
  }

  private def updateHatMembership(claim: HatClaimCompleteRequest): Future[Done] = {
    val futureResponse = wsClient.url(s"$hattersUrl/$path")
      //.withHttpHeaders("x-auth-token" → token.accessToken)
      .post(Json.toJson(claim.copy(password = "")))

    futureResponse.flatMap { response =>
      response.status match {
        case OK =>
          Future.successful(Done)
        case _ =>
          logger.error(s"Failed to claim HAT with Hatters. Claim details:\n$claim\nHatters response: ${response.body}")
          Future.failed(new UnknownError("HAT claim failed"))
      }
    }
  }
}
