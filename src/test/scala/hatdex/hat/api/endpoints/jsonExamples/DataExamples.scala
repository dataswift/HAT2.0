package hatdex.hat.api.endpoints.jsonExamples

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
