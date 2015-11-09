package hatdex.hat.api.endpoints.jsonExamples

/**
 * Created by andrius on 10/10/15.
 */
object DataDebitExamples {
  val dataDebitExample =
    """
      |  {
      |    "name": "DD Kitchen electricity on weekend parties",
      |    "startDate": "2015-09-30T10:00:00Z",
      |    "endDate": "2015-10-30T10:00:00Z",
      |    "rolling": false,
      |    "sell": true,
      |    "price": 100.0,
      |    "kind": "contextless",
      |    "bundleContextless": {
      |       "id": 3,
      |       "name": "Kitchen electricity on weekend parties"
      |    }
      |  }
    """.stripMargin
}
