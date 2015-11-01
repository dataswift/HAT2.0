package hatdex.hat.api.service.jsonExamples

object PropertyExamples {
  val bodyWeight =
    """
      |{
      | "name": "bodyWeight",
      | "description": "Body weight of a person",
      | "propertyType": {
      |     "name": "QuantitativeValue",
      |     "id": 49
      | },
      | "unitOfMeasurement": {
      |    "name": "kilograms",
      |    "description": "measurement of weight",
      |    "symbol": "kg",
      |    "id": 1
      | }
      |}
    """.stripMargin
}
