package org.hatdex.hat.utils

import pdi.jwt.{ Jwt, JwtAlgorithm, JwtClaim, JwtHeader, JwtOptions }

import java.security.PublicKey
import scala.util.{ Failure, Success, Try }

object TrustProxyUtils {
  def decodeToken(
      token: String,
      key: PublicKey): Option[JwtClaim] = {
    val ret: Try[JwtClaim] = Jwt.decode(token, key, Seq(JwtAlgorithm.RS256))

    ret match {
      case Success(value) =>
        println("!!!SUCCESS on decode")
        Some(value)
      case Failure(exception) =>
        println(s"fail: ${exception.toString()}")
        None
    }
  }
}
