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
import hatdex.hat.api.models.{ErrorMessage, ApiError}
import hatdex.hat.authentication.models.{User, AccessToken}
import hatdex.hat.dal.Tables._
import hatdex.hat.dal.SlickPostgresDriver.api._
import org.bouncycastle.openssl.PEMReader
import org.joda.time.DateTime
import spray.http.StatusCodes._

import scala.concurrent.Future
import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext.Implicits.global
import org.joda.time.Duration
import org.joda.time.Duration._

trait JwtTokenHandler {
  val conf = ConfigFactory.load()
  Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider())
  val issuer = "hat"
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

  def fetchOrGenerateToken(user: User, accessScope: String = "validate", validity: Duration = standardMinutes(5)): Future[AccessToken] = {
    val maybeToken = DatabaseInfo.db.run(UserAccessToken.filter(_.userId === user.userId).take(1).result).map(_.headOption)

    maybeToken flatMap {
      case Some(token) if validateJwtToken(token.accessToken) && getTokenAccessScope(token.accessToken).contains(accessScope) =>
        Future(AccessToken(token.accessToken, token.userId))

      case _ =>
        val newAccessToken = UserAccessTokenRow(getJwtToken(user.userId, accessScope, validity), user.userId)
        DatabaseInfo.db.run((UserAccessToken += newAccessToken).asTry) map {
          case Success(_) => AccessToken(newAccessToken.accessToken, user.userId)
          case Failure(e) => throw ApiError(InternalServerError, ErrorMessage("Error while creating access_token, please try again later", e.getMessage))
        }
    }
  }

  def getJwtToken(userId: UUID, accessScope: String, validity: Duration): String = {
    val issuedAt = new DateTime()
    val expiresAt = issuedAt.plus(validity)

    val signer: JWSSigner = new RSASSASigner(privateKey)

    // Prepare JWT with claims set
    val builder: JWTClaimsSet.Builder = new JWTClaimsSet.Builder()
    builder.subject(subject)
      .issuer(issuer)
      .expirationTime(expiresAt.toDate)
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
    val signedJWT = SignedJWT.parse(token)
    val verifier: JWSVerifier = new RSASSAVerifier(publicKey)
    val claimSet = signedJWT.getJWTClaimsSet

    val signed = signedJWT.verify(verifier)
    val fresh = claimSet.getExpirationTime.after(DateTime.now().toDate)
    println(s"Fresh? ${claimSet.getExpirationTime}, ${DateTime.now().toDate}, ${claimSet.getExpirationTime.before(DateTime.now().toDate)}")
    val rightIssuer = claimSet.getIssuer == issuer
    val subjectMatches = claimSet.getSubject == subject

    signed && fresh && rightIssuer && subjectMatches
  }

  def getTokenAccessScope(token: String): Option[String] = {
    val signedJWT = SignedJWT.parse(token)
    val verifier: JWSVerifier = new RSASSAVerifier(publicKey)
    val claimSet = signedJWT.getJWTClaimsSet

    val signed = signedJWT.verify(verifier)
    if (signed) {
      Option(claimSet.getClaim("accessScope").asInstanceOf[String])
    } else {
      None
    }
  }
}
