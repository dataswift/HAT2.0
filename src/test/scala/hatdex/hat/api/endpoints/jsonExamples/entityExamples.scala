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
}
