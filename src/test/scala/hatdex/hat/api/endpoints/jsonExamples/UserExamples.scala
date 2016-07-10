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

package hatdex.hat.api.endpoints.jsonExamples

/**
 * Created by andrius on 10/10/15.
 */
object UserExamples {
  val userExample =
    """
      |  {
      |    "userId": "bb5385ab-9931-40b2-b65c-239210b408f3",
      |    "email": "apiClient@platform.com",
      |    "pass": "$2a$10$6YoHtQqSdit9zzVSzrkK7.E.JQuioFNAggTY7vZRL4RSeY.sUbUIu",
      |    "name": "apiclient.platform.com",
      |    "role": "dataDebit"
      |  }
    """.stripMargin
  // pass is bcrypt salted hash of simplepass

  val ownerUserExample =
    """
      |  {
      |    "userId": "cf6da178-ac77-4c97-b274-b7ed34d16aea",
      |    "email": "apiClient@platform.com",
      |    "pass": "$2a$10$6YoHtQqSdit9zzVSzrkK7.E.JQuioFNAggTY7vZRL4RSeY.sUbUIu",
      |    "name": "apiclient.platform.com",
      |    "role": "owner"
      |  }
    """.stripMargin
}
