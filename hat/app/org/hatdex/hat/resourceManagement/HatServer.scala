package org.hatdex.hat.resourceManagement

import java.security.interfaces.{ RSAPrivateKey, RSAPublicKey }

import com.mohiva.play.silhouette.api.DynamicSecureEnvironment
import org.hatdex.hat.dal.SlickPostgresDriver.backend.Database

case class HatServer(
    domain: String,
    hatName: String,
    ownerEmail: String,
    privateKey: RSAPrivateKey,
    publicKey: RSAPublicKey,
    db: Database) extends DynamicSecureEnvironment {
  def id = domain
}

