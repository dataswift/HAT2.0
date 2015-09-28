package hat.authentication

import scala.concurrent.{ ExecutionContext, Future }
import spray.routing.authentication.ContextAuthenticator
import spray.routing.RequestContext
import spray.http.HttpRequest
import spray.http.HttpHeader
import spray.routing.RequestContext
import scala.concurrent.ExecutionContext.Implicits.global
import spray.routing.AuthenticationFailedRejection
import AuthenticationFailedRejection._
import spray.routing._
import spray.routing.HttpService._
import spray.routing.authentication._

trait RestAuthenticator[U] extends ContextAuthenticator[U] {

  type ParamExtractor = RequestContext => Map[String, String]

  val keys: List[String]

  val extractor: ParamExtractor = (ctx: RequestContext) => ctx.request.uri.query.toMap

  val authenticator: Map[String, String] => Future[Option[U]]

  def getChallengeHeaders(httpRequest: HttpRequest): List[HttpHeader] = Nil

  def apply(ctx: RequestContext): Future[Authentication[U]] = {
    val queryParameters = extractor(ctx)
    println(s"### $queryParameters")
    authenticator(queryParameters) map {
      case Some(entity) ⇒ Right(entity)
      case None ⇒
        val cause = if (queryParameters.isEmpty) CredentialsMissing else CredentialsRejected
        Left(AuthenticationFailedRejection(cause, getChallengeHeaders(ctx.request)))
    }
  }
}



