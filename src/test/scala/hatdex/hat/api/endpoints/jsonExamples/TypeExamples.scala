package hatdex.hat.api.endpoints.jsonExamples

/**
 * Created by andrius on 31/10/15.
 */
object TypeExamples {
  val postalAddress =
    """
      |{
      | "name": "PostalAddress",
      | "description": "Physical address of the item"
      |}
    """.stripMargin

  val date =
    """
      |{
      | "name": "Date",
      | "description": "Date in time"
      |}
    """.stripMargin

  val place =
    """
      |{
      | "name": "Place",
      | "description": "A somewhat fixed, physical extension"
      |}
    """.stripMargin

  val addressOfPlace =
    """
      |{
      | "relationshipType": "address"
      |}
    """.stripMargin

  val uomMeters =
    """
      |{
      | "name": "meters",
      | "description": "mesurement of length/distance",
      | "symbol": "m"
      |}
    """.stripMargin

  val quantitativeValue =
    """
      | {
      |     "name": "QuantitativeValue",
      |     "description": "A generic quantitative value"
      | }
    """.stripMargin

  val uomWeight =
    """
      |{
      |    "name": "kilograms",
      |    "description": "measurement of weight",
      |    "symbol": "kg"
      | }
    """.stripMargin
}
