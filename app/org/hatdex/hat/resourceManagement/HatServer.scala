/*
 * Copyright (C) 2017 HAT Data Exchange Ltd
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
 *
 * Written by Andrius Aucinas <andrius.aucinas@hatdex.org>
 * 2 / 2017
 */

package org.hatdex.hat.resourceManagement

import com.mohiva.play.silhouette.api.DynamicSecureEnvironment
import org.hatdex.libs.dal.HATPostgresProfile.api.Database

import java.security.interfaces.{ RSAPrivateKey, RSAPublicKey }

case class HatServer(
    domain: String,
    hatName: String,
    ownerEmail: String,
    privateKey: RSAPrivateKey,
    publicKey: RSAPublicKey,
    db: Database)
    extends DynamicSecureEnvironment {
  def id = domain
}
