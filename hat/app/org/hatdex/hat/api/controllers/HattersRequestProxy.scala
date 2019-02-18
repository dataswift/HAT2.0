package org.hatdex.hat.api.controllers

import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.util.PasswordHasherRegistry
import com.mohiva.play.silhouette.api.Silhouette
import javax.inject.Inject
import org.hatdex.hat.api.json.ApplicationJsonProtocol
import org.hatdex.hat.api.models._
import org.hatdex.hat.api.service.{ MailTokenService, RemoteExecutionContext, UsersService }
import org.hatdex.hat.api.service.applications.ApplicationsService
import org.hatdex.hat.authentication.{ ContainsApplicationRole, HatApiAuthEnvironment, HatApiController, WithRole }
import org.hatdex.hat.phata.models.MailTokenUser
import org.hatdex.hat.api.json.HatJsonFormats._
import org.hatdex.hat.resourceManagement.HatServer
import play.api.http.HttpEntity
import play.api.{ Configuration, Logger }
import play.api.libs.json.Json
import play.api.libs.ws.WSClient
import play.api.mvc.{ Action, AnyContent, ControllerComponents }

import scala.concurrent.Future

class HattersRequestProxy @Inject() (
    components: ControllerComponents,
    silhouette: Silhouette[HatApiAuthEnvironment],
    wsClient: WSClient)(
    implicit
    val ec: RemoteExecutionContext,
    applicationsService: ApplicationsService,
    tokenService: MailTokenService[MailTokenUser],
    authInfoRepository: AuthInfoRepository[HatServer],
    passwordHasherRegistry: PasswordHasherRegistry,
    usersService: UsersService,
    configuration: Configuration)
  extends HatApiController(components, silhouette) with ApplicationJsonProtocol {

  val logger = Logger(this.getClass)

  def proxyRequestHatClaim(claimToken: String): Action[AnyContent] = SecuredAction(ContainsApplicationRole(Owner()) || WithRole(Owner())).async { implicit request =>
    val path = "api/products/hat/claim"
    val hattersUrl = s"${configuration.underlying.getString("hatters.scheme")}${configuration.underlying.getString("hatters.address")}"
    logger.info(s"Proxy POST request to $hattersUrl/$path with parameters: ${request.queryString}")

    tokenService.retrieve(claimToken).flatMap {
      case Some(token) if token.isSignUp && !token.isExpired =>
        if (token.email == request.dynamicEnvironment.ownerEmail) {
          // TODO : do change password here
          usersService.listUsers.map(_.find(_.roles.contains(Owner()))).flatMap {
            case Some(user) =>
              for {
                _ <- authInfoRepository.update(user.loginInfo, passwordHasherRegistry.current.hash(request.body.asJson.get("password").toString()))
                authenticator <- env.authenticatorService.create(user.loginInfo)
                result <- env.authenticatorService.renew(authenticator, Ok(Json.toJson(SuccessResponse("HAT claimed"))))
              } yield {
                //env.eventBus.publish(LoginEvent(user, request))
                //mailer.passwordChanged(token.email, user)
                val baseRequest = wsClient.url(s"$hattersUrl/$path")
                  //.withHttpHeaders("x-auth-token" → token.accessToken)
                  .addQueryStringParameters(request.queryString.map(p ⇒ (p._1, p._2.head)).toSeq: _*)
                  .withMethod("POST")

                // TODO remove password from json
                request.body.asJson.fold(baseRequest)(b ⇒ baseRequest.withBody(b))
                  .stream()
                  .map(r ⇒ new Status(r.status).sendEntity(HttpEntity.Strict(r.bodyAsBytes, Some("application/json"))))
                Future.successful(Ok(Json.toJson(SuccessResponse("HAT claimed"))))
                result
              }
            case None => Future.successful(Unauthorized(Json.toJson(ErrorMessage("Password reset unauthorized", "No user matching token"))))
          }

        }
        else {
          // TODO email hat claimed?
          Future.successful(Unauthorized(Json.toJson(ErrorMessage("Invalid Token", "Token expired or invalid"))))
        }
      case Some(_) =>
        // tokenService.consume(claimToken)
        Future.successful(Unauthorized(Json.toJson(ErrorMessage("Invalid Token", "Token expired or invalid"))))
      case None =>
        Future.successful(Unauthorized(Json.toJson(ErrorMessage("Invalid Token", "Token does not exist"))))
    }
  }
}