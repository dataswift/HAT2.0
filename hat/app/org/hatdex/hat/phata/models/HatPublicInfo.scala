package org.hatdex.hat.phata.models

import java.security.interfaces.RSAPublicKey

import org.hatdex.hat.resourceManagement.HatServer

case class HatPublicInfo(domain: String, hatName: String, publicKey: RSAPublicKey) {
  def id = domain
}

object HatPublicInfo {
  implicit def hatServer2PublicInfo(implicit hatServer: HatServer): HatPublicInfo = HatPublicInfo(hatServer.domain, hatServer.hatName, hatServer.publicKey)
}