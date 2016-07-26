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

object BundleContextExamples {
  val emptyBundle =
    """
      |{
      |  "name": "emptyBundleTest1",
      |  "bundles": [
      |    {
      |      "name": "emptyBundleTest2"
      |    },
      |    {
      |      "name": "emptyBundleTest3"
      |    }
      |  ]
      |}
    """.stripMargin

  val entityBundleSunrise =
    """
      |{
      |  "name": "emptyBundleTest2-1",
      |  "entities": [
      |   {
      |     "entityName": "sunrise"
      |   }
      |  ]
      |}
    """.stripMargin

  val entityBundleKind =
    """
      |{
      |  "name": "emptyBundleTest3-1",
      |  "entities": [
      |   {
      |     "entityKind": "person"
      |   }
      |  ]
      |}
    """.stripMargin

  val entityBundlePerson =
    """
      |{
      |  "name": "emptyBundleTest6-1",
      |  "entities": [
      |   {
      |     "entityName": "HATperson"
      |   }
      |  ]
      |}
    """.stripMargin

  val entityBundlePersonNoProps =
    """
      |{
      |  "name": "emptyBundleTest8-1",
      |  "entities": [
      |   {
      |     "entityName": "HATperson",
      |     "properties": [
      |       {
      |         "propertyName": "non-exitent property"
      |       }
      |     ]
      |   }
      |  ]
      |}
    """.stripMargin

  val entityBundlePersonProps =
    """
      |{
      |  "name": "emptyBundleTest9-1",
      |  "entities": [
      |   {
      |     "entityName": "HATperson",
      |     "properties": [
      |       {
      |         "propertyName": "BodyWeight"
      |       }
      |     ]
      |   }
      |  ]
      |}
    """.stripMargin



  val entityBundleAllPeople =
    """
      |{
      |  "name": "emptyBundleTest7-1",
      |  "entities": [
      |   {
      |     "entityKind": "person"
      |   }
      |  ]
      |}
    """.stripMargin

  val entitiesBundleKindName =
    """
      |{
      |  "name": "emptyBundleTest4-1",
      |  "entities": [
      |   {
      |     "entityName": "sunrise"
      |   },
      |   {
      |     "entityKind": "person"
      |   }
      |  ]
      |}
    """.stripMargin

  val entityBundleProperties =
    """
      |{
      |  "name": "emptyBundleTest5-1",
      |  "entities": [
      |   {
      |     "entityKind": "person",
      |     "properties": [
      |       {
      |         "propertyRelationshipKind": "dynamic",
      |         "propertyName": "BodyWeight"
      |       },
      |       {
      |         "propertyRelationshipKind": "dynamic",
      |         "propertyType": "QuantitativeValue"
      |       }
      |     ]
      |   },
      |   {
      |     "entityName": "sunrise"
      |   }
      |  ]
      |}
    """.stripMargin
}
