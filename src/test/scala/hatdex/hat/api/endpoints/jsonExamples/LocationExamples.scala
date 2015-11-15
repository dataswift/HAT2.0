package hatdex.hat.api.endpoints.jsonExamples

/**
 * Created by rolandas on 11/2/15.
 */
object LocationExamples {
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

  val validThing =
    """{
      | "name": "tv"
    }""".stripMargin

  val validOrg =
    """{
      | "name": "HATorg"
    }""".stripMargin

  val validPerson =  /* if changing personId here, make sure to update the expectation at LocationsServiceSpec*/
    """{
      | "name": "HATperson",
      | "personId": "abcde-fghij"
    }""".stripMargin

  val validEvent =
    """{
      | "name": "sunset"
    }""".stripMargin
}
