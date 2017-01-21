/*
 * Copyright (C) HAT Data Exchange Ltd - All Rights Reserved
 *  Unauthorized copying of this file, via any medium is strictly prohibited
 *  Proprietary and confidential
 *  Written by Andrius Aucinas <andrius.aucinas@hatdex.org>, 10 2016
 */

package org.hatdex.hat.authentication.models

import java.util.UUID

import com.mohiva.play.silhouette.api.{ Identity, LoginInfo }
import org.hatdex.hat.resourceManagement.HatServer

case class HatUser(userId: UUID, email: String, pass: Option[String], name: String, role: String, enabled: Boolean) extends Identity {
  def loginInfo(implicit hatServer: HatServer) = LoginInfo(hatServer.domain, email)
}

