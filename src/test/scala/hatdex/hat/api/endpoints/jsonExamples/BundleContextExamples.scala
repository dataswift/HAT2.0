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
      |       },
      |       {
      |         "propertyRelationshipKind": "static",
      |         "propertyUnitofmeasurement": "kilograms"
      |       }
      |     ]
      |   }
      |  ]
      |}
    """.stripMargin
}
