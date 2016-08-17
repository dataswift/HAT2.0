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
package hatdex.hat.authentication

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

  val extractor: ParamExtractor = {
    (ctx: RequestContext) => {
      ctx.request.cookies.map(c => c.name -> c.content).toMap ++  // Include cookies as parameters
        ctx.request.uri.query.toMap ++                            // as well as query parameters
        ctx.request.headers.map(h => h.name -> h.value).toMap ++  // and headers, in increasing importance
        ctx.request.headers.map(h => h.name.toLowerCase -> h.value).toMap     // as well as lowercase headers
    }
  }

  val authenticator: Map[String, String] => Future[Option[U]]

  def getChallengeHeaders(httpRequest: HttpRequest): List[HttpHeader] = Nil

  def apply(ctx: RequestContext): Future[Authentication[U]] = {
    val parameters = extractor(ctx)
    authenticator(parameters) map {
      case Some(entity) ⇒ Right(entity)
      case None ⇒
        val cause = if (parameters.isEmpty) CredentialsMissing else CredentialsRejected
        Left(AuthenticationFailedRejection(cause, getChallengeHeaders(ctx.request)))
    }
  }
}



