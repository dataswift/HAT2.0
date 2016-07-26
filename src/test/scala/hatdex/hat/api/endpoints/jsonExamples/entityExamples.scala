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

object EntityExamples {
  val locationValid =
    """{
      | "name": "home"
   }""".stripMargin

  val locationBadName =
    """{
      | "nam": "home"
   }""".stripMargin

  val locationHomeStairs =
    """{
      | "name": "stairs"
    }""".stripMargin

  val thingValid =
    """{
      | "name": "tv"
    }""".stripMargin

  val thingBadName =
    """{
      | "nam": "tv"
   }""".stripMargin

  val otherThingValid =
    """{
      | "name": "smartphone"
    }""".stripMargin

  val orgValid =
    """{
      | "name": "HATorg"
    }""".stripMargin

  val otherOrgValid =
    """{
      | "name": "HATcontrol"
    }""".stripMargin

  val orgBadName =
    """{
      | "nam": "HATorg"
    }""".stripMargin

  val personValid =  /* if changing personId here, make sure to update the expectation at LocationsServiceSpec*/
    """{
      | "name": "HATperson",
      | "personId": "abcde-fghij"
    }""".stripMargin

  val personRelative =  /* if changing personId here, make sure to update the expectation at LocationsServiceSpec*/
    """{
      | "name": "HATRelative",
      | "personId": "fghij-abcde"
    }""".stripMargin

  val personBadName =  /* if changing personId here, make sure to update the expectation at LocationsServiceSpec*/
    """{
      | "nam": "HATperson",
      | "personId": "abcde-fghij"
    }""".stripMargin

  val eventValid =
    """{
      | "name": "sunrise"
    }""".stripMargin

  val eventBadName =
    """{
      | "nam": "sunset"
    }""".stripMargin

  val otherEventValid =
    """{
      | "name": "breakfast"
    }""".stripMargin


  val relationshipNextTo =
    """{
      | "relationshipType": "next to"
    }""".stripMargin

  val relationshipDuring =
    """{
      | "relationshipType": "During"
    }""".stripMargin

  val relationshipOwnedBy =
    """{
      | "relationshipType": "Owned By"
    }""".stripMargin

  val relationshipActiveAt =
    """{
      | "relationshipType": "Active At"
    }""".stripMargin

  val relationshipHappensAt =
    """{
      | "relationshipType": "Happens At"
    }""".stripMargin

  val relationshipWorksAt =
    """{
      | "relationshipType": "Works At"
    }""".stripMargin

  val relationshipPersonRelative =
    """{
      | "name": "Family Member",
      | "description": "Extended family member of a given person"
    }""".stripMargin

  val relationshipType =
    """{
      | "relationshipType": "EntityType"
    }""".stripMargin

  val relationshipControls =
    """{
      | "relationshipType": "controls"
    }""".stripMargin
}
