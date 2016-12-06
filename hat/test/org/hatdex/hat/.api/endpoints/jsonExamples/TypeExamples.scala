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

package org.hatdex.hat.api.endpoints.jsonExamples

object TypeExamples {
  val postalAddress =
    """
      |{
      | "name": "PostalAddress",
      | "description": "Physical address of the item"
      |}
    """.stripMargin

  val date =
    """
      |{
      | "name": "Date",
      | "description": "Date in time"
      |}
    """.stripMargin

  val place =
    """
      |{
      | "name": "Place",
      | "description": "A somewhat fixed, physical extension"
      |}
    """.stripMargin

  val addressOfPlace =
    """
      |{
      | "relationshipType": "address"
      |}
    """.stripMargin

  val uomMeters =
    """
      |{
      | "name": "meters",
      | "description": "mesurement of length/distance",
      | "symbol": "m"
      |}
    """.stripMargin

  val quantitativeValue =
    """
      | {
      |     "name": "QuantitativeValue",
      |     "description": "A generic quantitative value"
      | }
    """.stripMargin

  val uomWeight =
    """
      |{
      |    "name": "kilograms",
      |    "description": "measurement of weight",
      |    "symbol": "kg"
      | }
    """.stripMargin
}
