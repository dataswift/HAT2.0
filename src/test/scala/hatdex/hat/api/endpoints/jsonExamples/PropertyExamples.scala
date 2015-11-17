package hatdex.hat.api.endpoints.jsonExamples

object PropertyExamples {
  val bodyWeight =
    """
      |{
      | "name": "bodyWeight",
      | "description": "Body weight of a person",
      | "propertyType": {
      |     "name": "QuantitativeValue",
      | },
      | "unitOfMeasurement": {
      |    "name": "kilograms",
      |    "description": "measurement of weight",
      |    "symbol": "kg",
      | }
      |}
    """.stripMargin

  val bodyWeightRelationship =
    """
      |{
      | "property": {
      |   "id": 1,
      |   "name": "bodyWeight"
      | },
      | "relationshipType": "weight",
      | "field": {
      |   "id": 50,
      |   "name": "Weight"
      | },
      | "record": {
      |   "id": 8,
      |   "name": "Day 1"
      | }
      |}
    """.stripMargin
}