package hatdex.hat.api.endpoints.jsonExamples

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

  val dataDebitInvalid = // Invalid data debit kind
    """
      |  {
      |    "name": "DD Kitchen electricity on weekend parties",
      |    "startDate": "2015-09-30T10:00:00Z",
      |    "endDate": "2015-10-30T10:00:00Z",
      |    "rolling": false,
      |    "sell": true,
      |    "price": 100.0,
      |    "kind": "context",
      |    "bundleContextless": {
      |       "id": 3,
      |       "name": "Kitchen electricity on weekend parties"
      |    }
      |  }
    """.stripMargin

  val dataDebitContextual =
    """
      |  {
      |    "name": "DD Person body weight",
      |    "startDate": "2015-09-30T10:00:00Z",
      |    "endDate": "2015-10-30T10:00:00Z",
      |    "rolling": false,
      |    "sell": true,
      |    "price": 100.0,
      |    "kind": "contextual",
      |    "bundleContextual": {
      |       "name": "emptyBundleTest9-1",
      |       "entities": [
      |         {
      |          "entityName": "HATperson",
      |          "properties": [
      |            {
      |             "propertyName": "BodyWeight"
      |            }
      |          ]
      |         }
      |       ]
      |    }
      |  }
    """.stripMargin

  val dataDebitWrongKeyContextless =
    """
      |  {
      |    "name": "DD Person body weight",
      |    "startDate": "2015-09-30T10:00:00Z",
      |    "endDate": "2015-10-30T10:00:00Z",
      |    "rolling": false,
      |    "sell": true,
      |    "price": 100.0,
      |    "kind": "contextless",
      |    "bundleContextual": {
      |       "name": "emptyBundleTest9-1",
      |       "entities": [
      |         {
      |          "entityName": "HATperson",
      |          "properties": [
      |            {
      |             "propertyName": "BodyWeight"
      |            }
      |          ]
      |         }
      |       ]
      |    }
      |  }
    """.stripMargin

  val dataDebitWrongKeyContextual =
    """
      |  {
      |    "name": "DD Person body weight",
      |    "startDate": "2015-09-30T10:00:00Z",
      |    "endDate": "2015-10-30T10:00:00Z",
      |    "rolling": false,
      |    "sell": true,
      |    "price": 100.0,
      |    "kind": "contextual",
      |    "bundleContextless": {
      |       "id": 3,
      |       "name": "Kitchen electricity on weekend parties"
      |    }
      |  }
    """.stripMargin
}
