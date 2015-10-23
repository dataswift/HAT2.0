package hatdex.hat.api.service.jsonExamples

/**
 * Created by andrius on 10/10/15.
 */
object DataExamples {
  val tableKitchen =
    """{
      | "name": "kitchen",
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
      | "name": "kitchen",
      | "source": "fibaro",
      | "fields": [
      |   { "name": "tableTestField" },
      |   { "name": "tableTestField2" }
      | ],
      | "subTables": [
      |   {
      |     "name": "kitchenElectricity",
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

  val locationValid =
   """{
     | "name": "home"
   }""".stripMargin

  val locationBadName =
    """{
      | "nam": "home"
   }""".stripMargin
}
