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

object DataExamples {
  val tableKitchen =
    """{
      | "name": "kitchen",
      | "source": "fibaro"
    }""".stripMargin

  val malformedTable =
    """{
      | "nam": "kitchen",
      | "source": "fibaro"
    }""".stripMargin

  val tableKitchenElectricity =
    """{
      "name": "kitchenElectricity",
      "source": "fibaro"
    }"""

  val relationshipParent =
    """{
      | "relationshipType": "parent child"
    }""".stripMargin

  val testField =
    """
      |{
      | "name": "tableTestField",
      | "tableId": 0
      |}
    """.stripMargin

  val nestedTableKitchen =
    """
      |{
      | "name": "largeKitchen",
      | "source": "fibaro",
      | "fields": [
      |   { "name": "tableTestField" },
      |   { "name": "tableTestField2" }
      | ],
      | "subTables": [
      |   {
      |     "name": "largeKitchenElectricity",
      |     "source": "fibaro",
      |     "fields": [
      |       {
      |         "name": "tableTestField3"
      |       },
      |       {
      |         "name": "tableTestField4"
      |       }
      |     ]
      |   }
      | ]
      |}
    """.stripMargin

  val testRecord =
    """{
      | "name": "testRecord 1"
  }""".stripMargin

  val testRecord2 =
    """{
      | "name": "testRecord 2"
  }""".stripMargin

}
