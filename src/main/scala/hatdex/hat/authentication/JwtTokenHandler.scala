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

import java.io.StringReader
import java.security.interfaces.RSAPublicKey
import java.security.{ KeyPair, PrivateKey, Security }
import java.util.UUID

import akka.event.LoggingAdapter
import com.nimbusds.jose.crypto.{ RSASSASigner, RSASSAVerifier }
import com.nimbusds.jose.{ JWSAlgorithm, JWSHeader, JWSSigner, JWSVerifier }
import com.nimbusds.jwt.{ JWTClaimsSet, SignedJWT }
import com.typesafe.config.ConfigFactory
import hatdex.hat.api.DatabaseInfo
import hatdex.hat.api.models.{ ErrorMessage, ApiError }
import hatdex.hat.authentication.models.{ User, AccessToken }
import hatdex.hat.dal.Tables._
import hatdex.hat.dal.SlickPostgresDriver.api._
import org.bouncycastle.openssl.PEMReader
import org.joda.time.DateTime
import spray.http.StatusCodes._

import scala.concurrent.Future
import scala.util.{ Try, Failure, Success }
import scala.concurrent.ExecutionContext.Implicits.global
import org.joda.time.Duration
import org.joda.time.Duration._

trait JwtTokenHandler {
  val conf = ConfigFactory.load()
  Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider())
  val issuer = s"${conf.getString("hat.name")}.${conf.getString("hat.domain")}"
  val subject = "hat"

  lazy val publicKey: RSAPublicKey = {
    val confPublicKey: String = conf.getString("auth.publicKey")
    val reader = new PEMReader(new StringReader(confPublicKey))
    reader.readObject().asInstanceOf[RSAPublicKey]
  }

  lazy val privateKey: PrivateKey = {
    val confPrivateKey: String = conf.getString("auth.privateKey")
    val reader = new PEMReader(new StringReader(confPrivateKey))
    val keyPair = reader.readObject().asInstanceOf[KeyPair]
    keyPair.getPrivate
  }

  def fetchOrGenerateToken(
    user: User,
    resource: String,
    accessScope: String = "validate",
    validity: Duration = standardHours(2)): Future[AccessToken] = {

    val maybeToken = fetchToken(user, resource, accessScope, validity)

    maybeToken flatMap {
      case Some(token) if validateJwtToken(token.accessToken) => Future.successful(token)
      case _ => getToken(user, resource, accessScope, validity)
    }
  }

  def fetchToken(
    user: User,
    resource: String,
    accessScope: String = "validate",
    validity: Duration = standardHours(2)): Future[Option[AccessToken]] = {
    val tokenQuery = UserAccessToken.filter(_.userId === user.userId)
      .filter(_.resource === resource)
      .filter(_.scope === accessScope)
    val eventualToken = DatabaseInfo.db.run(tokenQuery.take(1).result).map(_.headOption)
    eventualToken map { _.map(token => AccessToken(token.accessToken, token.userId)) }
  }

  def getToken(
    user: User,
    resource: String,
    accessScope: String = "validate",
    validity: Duration = standardHours(2)): Future[AccessToken] = {
    val tokenQuery = UserAccessToken.filter(_.userId === user.userId)
      .filter(_.resource === resource)
      .filter(_.scope === accessScope)
    val newAccessToken = UserAccessTokenRow(
      getJwtToken(user.userId, resource, accessScope, validity),
      user.userId, accessScope, resource)
    val newTokenQuery = for {
      _ <- tokenQuery.delete
      token <- UserAccessToken += newAccessToken
    } yield token

      DatabaseInfo.db.run(newTokenQuery.transactionally.asTry) map {
        case Success(_) => AccessToken(newAccessToken.accessToken, user.userId)
        case Failure(e) => throw ApiError(InternalServerError, ErrorMessage("Error while creating access_token, please try again later", e.getMessage))
      }

  }

  private def getJwtToken(userId: UUID, resource: String, accessScope: String, validity: Duration): String = {
    val issuedAt = new DateTime()
    val expiresAt = issuedAt.plus(validity)

    val signer: JWSSigner = new RSASSASigner(privateKey)

    // Prepare JWT with claims set
    val builder: JWTClaimsSet.Builder = new JWTClaimsSet.Builder()
    builder.subject(subject)
      .issuer(issuer)
      .expirationTime(expiresAt.toDate)
      .claim("resource", resource)
      .claim("accessScope", accessScope)
    val claimsSet = builder.build()

    val signedJWT: SignedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.RS256), claimsSet)

    // Compute the RSA signature
    signedJWT.sign(signer)

    validateJwtToken(signedJWT.serialize())
    // Serialize to an encoded string
    signedJWT.serialize()
  }

  def validateJwtToken(token: String): Boolean = {
    val maybeSignedJWT = Try(SignedJWT.parse(token))

    maybeSignedJWT.map { signedJWT =>
      val verifier: JWSVerifier = new RSASSAVerifier(publicKey)
      val claimSet = signedJWT.getJWTClaimsSet

      val signed = signedJWT.verify(verifier)

      val fresh = claimSet.getExpirationTime.after(DateTime.now().toDate)
      val rightIssuer = claimSet.getIssuer == issuer
      val subjectMatches = claimSet.getSubject == subject

      signed && fresh && rightIssuer && subjectMatches
    } getOrElse {
      false
    }
  }

  def verifyResource(token: String, resource: String): Boolean = {
    val signedJWT = SignedJWT.parse(token)
    val verifier: JWSVerifier = new RSASSAVerifier(publicKey)
    val claimSet = signedJWT.getJWTClaimsSet
    val signed = signedJWT.verify(verifier)
    val resourceMatches = Option(claimSet.getClaim("resource")).contains(resource)
    signed && resourceMatches
  }

  def verifyAccessScope(token: String, accessScope: String): Boolean = {
    val signedJWT = SignedJWT.parse(token)
    val claimSet = signedJWT.getJWTClaimsSet
    Option(claimSet.getClaim("accessScope")).contains(accessScope)
  }

  def getTokenAccessScope(token: String): Option[String] = {
    val signedJWT = SignedJWT.parse(token)
    val verifier: JWSVerifier = new RSASSAVerifier(publicKey)
    val claimSet = signedJWT.getJWTClaimsSet

    val signed = signedJWT.verify(verifier)
    if (signed) {
      Option(claimSet.getClaim("accessScope").asInstanceOf[String])
    }
    else {
      None
    }
  }
}
