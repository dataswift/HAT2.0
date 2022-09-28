package org.hatdex.hat.utils

import org.hatdex.hat.client.TrustProxyTypes._
import pdi.jwt.{ Jwt, JwtAlgorithm, JwtClaim }
import play.api.libs.json.Json

import java.security.PublicKey
import java.security.KeyFactory
import java.security.spec.X509EncodedKeySpec
import java.util.Base64
import scala.util.{ Failure, Success, Try }

object TrustProxyUtils {
  def stringToPublicKey(publicKeyAsString: String): PublicKey = {
    val pKey                      = publicKeyAsString
    val rsaKeyFactory: KeyFactory = KeyFactory.getInstance("RSA")

    val strippedKeyText = pKey.stripMargin
      .replace("\n", "")
      .replace("-----BEGIN PUBLIC KEY-----", "")
      .replace("-----END PUBLIC KEY-----", "")

    val bytes = Base64.getDecoder.decode(strippedKeyText)

    rsaKeyFactory.generatePublic(new X509EncodedKeySpec(bytes))
  }

  def decodeToken(
      token: String,
      key: PublicKey): Option[JwtClaim] = {
    val ret: Try[JwtClaim] = Jwt.decode(token, key, Seq(JwtAlgorithm.RS256))

    ret match {
      case Success(value) =>
        println(value)
        Some(value)
      case Failure(exception) =>
        None
    }
  }

  def verifyToken(
      token: String,
      publicKey: PublicKey,
      email: String,
      pdaUrl: String,
      issuer: String): Boolean = {
    val jwtClaim = decodeToken(token, publicKey)
    jwtClaim match {
      case Some(value) =>
        val c = Json.parse(value.content).as[TrustProxyContent]
        println(c)
        println(issuer, c.iss)
        println(pdaUrl, c.pdaUrl)
        println(email, c.email)
        println(c.iss == issuer)
        println(c.pdaUrl == pdaUrl)
        println(c.email == email)

        if (c.iss == issuer && c.pdaUrl == pdaUrl && c.email == email) {
          println("TRUE")
          true
        } else {
          println("FALSE")
          false
        }
      case _ =>
        false
    }
  }

}
