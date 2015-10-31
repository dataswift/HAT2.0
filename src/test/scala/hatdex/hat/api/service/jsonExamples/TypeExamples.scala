package hatdex.hat.api.service.jsonExamples

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
}
