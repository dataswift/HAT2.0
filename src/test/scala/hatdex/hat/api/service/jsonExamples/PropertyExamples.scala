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