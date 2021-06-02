package org.hatdex.hat.api.controllers.v2

import com.mohiva.play.silhouette.api.Silhouette
import eu.timepit.refined.auto._
import io.dataswift.adjudicator.ShortLivedTokenOps
import io.dataswift.models.hat.applications.Application
import org.hatdex.hat.NamespaceUtils.NamespaceUtils
import org.hatdex.hat.api.service.UserService
import org.hatdex.hat.api.service.applications.TrustedApplicationProvider
import org.hatdex.hat.authentication.models.HatUser
import org.hatdex.hat.authentication.{ HatApiAuthEnvironment, HatApiController }
import org.hatdex.hat.client.AdjudicatorClient
import org.hatdex.hat.client.AdjudicatorRequestTypes.{
  InvalidPublicKeyFailure,
  PublicKeyReceived,
  PublicKeyServiceFailure
}
import org.hatdex.hat.resourceManagement.HatServer
import play.api.Logging
import play.api.mvc.{ Action, BodyParser, ControllerComponents, Result }
import org.hatdex.hat.api.controllers.common._

import javax.inject.Inject
import scala.concurrent.{ ExecutionContext, Future }
import scala.util.{ Failure, Success }
import play.api.mvc.Request
import play.api.libs.json.Reads
import play.api.libs.json.Json
import play.api.mvc.Headers
import play.mvc.BodyParser.AnyContent

// Temp
object MachineData {
  case class SLTokenBody(
      iss: String,
      exp: Long,
      deviceId: String)
  implicit val sltokenBodyReads: Reads[SLTokenBody] = Json.reads[SLTokenBody]
}

class ContractActionImpl @Inject() (
    components: ControllerComponents,
    silhouette: Silhouette[HatApiAuthEnvironment],
    userService: UserService,
    trustedApplicationProvider: TrustedApplicationProvider,
    adjudicatorClient: AdjudicatorClient
  )(implicit ec: ExecutionContext)
    extends HatApiController(components, silhouette)
    with Logging
    with ContractAction {

  override def doWithToken[A]( // <: MaybeWithToken](
      parser: Option[BodyParser[A]],
      maybeNamespace: Option[String],
      isWriteAction: Boolean // replace with type
    )(contractAction: (A, HatUser, HatServer, Option[HatApiAuthEnvironment#A]) => Future[Result]): Action[A] =
    UserAwareAction.async(parser.get) { implicit request =>
      val maybeUser: Future[Option[HatUser]] = for {
        _ <- getTokenFromHeaders(request.headers)
        user <- userService.getUser(request.dynamicEnvironment.hatName)
      } yield user

      maybeUser.flatMap {
        case Some(user) =>
          contractAction(request.body, user, request.dynamicEnvironment, request.authenticator)
      }
    }

  // -- Auth Token handler
  private def getTokenFromHeaders(headers: Headers): Future[Option[MachineData.SLTokenBody]] = {
    val slTokenBody = for {
      _xAuthToken <- headers.get("X-Auth-Token")
      slTokenBody <- Some("abc") //ShortLivedTokenOps.getBody(xAuthToken).toOption
      ret <- Json.parse(slTokenBody).validate[MachineData.SLTokenBody].asOpt
    } yield ret

    Future.successful(slTokenBody)
  }

  private def contractValid(
      contract: ContractDataInfoRefined,
      maybeNamespace: Option[String],
      isWriteAction: Boolean
    )(implicit hatServer: HatServer): Future[(Option[HatUser], Either[RequestValidationFailure, RequestVerified])] =
    for {
      hatUser <- userService.getUser(contract.hatName.value)
      requestAssessment <- assessRequest(contract, maybeNamespace, isWriteAction)
    } yield (hatUser, requestAssessment)

  private def assessRequest(
      contractDataInfo: ContractDataInfoRefined,
      maybeNamespace: Option[String],
      isWriteAction: Boolean): Future[Either[RequestValidationFailure, RequestVerified]] = {
    val eventuallyMaybeDecision = verifyContract(contractDataInfo)
    val eventuallyMaybeApp =
      trustedApplicationProvider.application(contractDataInfo.contractId.value.toString)

    eventuallyMaybeDecision.flatMap { maybeDecision =>
      eventuallyMaybeApp.flatMap { maybeApp =>
        logger.info(
          s"AssessRequest: decision $maybeDecision - app id ${maybeApp.map(_.id)} - namespace $maybeNamespace"
        )
        if (decide(maybeDecision, maybeApp, maybeNamespace, isWriteAction))
          Future.successful(Right(RequestVerified(s"Token: ${contractDataInfo.contractId}")))
        else {
          logger.info(s"assessRequest: decide returned false - $contractDataInfo - $maybeNamespace")
          Future.successful(Left(InvalidShortLivedToken(s"Token: ${contractDataInfo.contractId}")))
        }
      }
    } recover {
      case e =>
        logger.error(s"ContractData.assessRequest:Failure ${e}")
        Left(InvalidShortLivedToken(s"Token: ${contractDataInfo.contractId}"))
    }
  }

  private def decide(
      eitherDecision: Either[
        ContractVerificationFailure,
        ContractVerificationSuccess
      ],
      maybeApp: Option[Application],
      maybeNamespace: Option[String],
      isWriteAction: Boolean): Boolean =
    (eitherDecision, maybeApp) match {
      case (Right(JwtClaimVerified(_)), Some(app)) =>
        logger.debug(s"JwtClaim verified for app ${app.id}")
        maybeNamespace forall (verifyNamespace(app, _, isWriteAction))
      case (Left(decision), _) =>
        logger.info(s"contract not verified: $decision")
        false
      case (_, _) =>
        logger.info("contract not verified, catch all")
        false
    }

  private def verifyNamespace(
      app: Application,
      namespace: String,
      isWriteAction: Boolean): Boolean =
    if (isWriteAction) NamespaceUtils.testWriteNamespacePermissions(app.permissions.rolesGranted, namespace)
    else NamespaceUtils.testReadNamespacePermissions(app.permissions.rolesGranted, namespace)

  private def verifyContract(contractDataInfo: ContractDataInfoRefined): Future[
    Either[ContractVerificationFailure, ContractVerificationSuccess]
  ] =
    ShortLivedTokenOps.getKeyId(contractDataInfo.token.value).toOption match {
      case Some(keyId) =>
        verifyTokenWithAdjudicator(contractDataInfo, keyId)
      case _ =>
        Future.successful(Left(InvalidContractDataRequestFailure("Contract Data Request or KeyId missing")))
    }

  private def verifyJwtClaim(
      contractRequestBodyRefined: ContractDataInfoRefined,
      publicKeyAsByteArray: Array[Byte]): Either[ContractVerificationFailure, ContractVerificationSuccess] = {
    logger.debug(s"verifyJwtClaim.token: ${contractRequestBodyRefined.token}")
    val tryJwtClaim = ShortLivedTokenOps.verifyToken(
      Some(contractRequestBodyRefined.token.toString),
      publicKeyAsByteArray
    )
    tryJwtClaim match {
      case Success(jwtClaim) => Right(JwtClaimVerified(jwtClaim))
      case Failure(th) =>
        logger.error(s"verifyJwtClaim failed: ${th.getMessage}")
        Left(InvalidTokenFailure(s"Token: ${contractRequestBodyRefined.token} was not verified."))
    }
  }

  private def verifyTokenWithAdjudicator(
      contractRequestBodyRefined: ContractDataInfoRefined,
      keyId: String): Future[
    Either[ContractVerificationFailure, ContractVerificationSuccess]
  ] =
    adjudicatorClient
      .getPublicKey(
        contractRequestBodyRefined.hatName,
        contractRequestBodyRefined.contractId,
        keyId
      )
      .map {
        case Left(PublicKeyServiceFailure(failureDescription)) =>
          Left(ServiceRespondedWithFailure(s"The Adjudicator Service responded with an error: ${failureDescription}"))
        case Left(InvalidPublicKeyFailure(failureDescription)) =>
          Left(ServiceRespondedWithFailure(s"The Adjudicator Service responded with an error: ${failureDescription}"))
        case Right(PublicKeyReceived(publicKey)) =>
          verifyJwtClaim(contractRequestBodyRefined, publicKey)
      }

}
