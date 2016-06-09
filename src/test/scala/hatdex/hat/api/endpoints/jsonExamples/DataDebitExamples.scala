package hatdex.hat.api.endpoints.jsonExamples

object DataDebitExamples {
  val dataDebitExample =
    """
      |  {
      |    "name": "DD Kitchen electricity on weekend parties",
      |    "startDate": "2015-09-30T10:00:00Z",
      |    "endDate": "2025-10-30T10:00:00Z",
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
      |    "endDate": "2025-10-30T10:00:00Z",
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
      |    "endDate": "2025-10-30T10:00:00Z",
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
      |    "endDate": "2025-10-30T10:00:00Z",
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
      |    "endDate": "2025-10-30T10:00:00Z",
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

  val dataDebitContextlessValues =
    """
      |{
      |  "rolling": false,
      |  "name": "DD Kitchen electricity on weekend parties",
      |  "endDate": "2025-10-30T10:00:00Z",
      |  "lastUpdated": "2016-06-08T13:52:46+01:00",
      |  "price": 100.0,
      |  "key": "2c366edd-5677-48fd-918a-e6705e879747",
      |  "dateCreated": "2016-06-08T13:52:46+01:00",
      |  "bundleContextless": {
      |    "id": 6,
      |    "name": "Kitchen electricity",
      |    "dataGroups": [{
      |      "Electricity in the kitchen": {
      |        "name": "Electricity in the kitchen",
      |        "lastUpdated": "2016-06-08T13:52:46+01:00",
      |        "data": [{
      |          "name": "testRecord 6",
      |          "lastUpdated": "2016-06-08T13:52:45+01:00",
      |          "id": 239,
      |          "dateCreated": "2016-06-08T13:52:45+01:00",
      |          "tables": [{
      |            "name": "kitchen",
      |            "source": "fibaro",
      |            "lastUpdated": "2016-06-08T13:52:45+01:00",
      |            "subTables": [{
      |              "name": "kitchenElectricity",
      |              "source": "fibaro",
      |              "lastUpdated": "2016-06-08T13:52:45+01:00",
      |              "id": 83,
      |              "dateCreated": "2016-06-08T13:52:45+01:00",
      |              "fields": [{
      |                "name": "subtableTestField1",
      |                "lastUpdated": "2016-06-08T13:52:45+01:00",
      |                "id": 187,
      |                "dateCreated": "2016-06-08T13:52:45+01:00",
      |                "tableId": 83,
      |                "values": [{
      |                  "id": 413,
      |                  "dateCreated": "2016-06-08T13:52:45+01:00",
      |                  "lastUpdated": "2016-06-08T13:52:45+01:00",
      |                  "value": "testValue6-2"
      |                }]
      |              }, {
      |                "name": "subtableTestField2",
      |                "lastUpdated": "2016-06-08T13:52:45+01:00",
      |                "id": 188,
      |                "dateCreated": "2016-06-08T13:52:45+01:00",
      |                "tableId": 83,
      |                "values": [{
      |                  "id": 414,
      |                  "dateCreated": "2016-06-08T13:52:45+01:00",
      |                  "lastUpdated": "2016-06-08T13:52:45+01:00",
      |                  "value": "testValue6-3"
      |                }]
      |              }]
      |            }],
      |            "id": 82,
      |            "dateCreated": "2016-06-08T13:52:45+01:00",
      |            "fields": [{
      |              "name": "tableTestField",
      |              "lastUpdated": "2016-06-08T13:52:45+01:00",
      |              "id": 186,
      |              "dateCreated": "2016-06-08T13:52:45+01:00",
      |              "tableId": 82,
      |              "values": [{
      |                "id": 412,
      |                "dateCreated": "2016-06-08T13:52:45+01:00",
      |                "lastUpdated": "2016-06-08T13:52:45+01:00",
      |                "value": "testValue6-1"
      |              }]
      |            }]
      |          }]
      |        }, {
      |          "name": "testRecord 5",
      |          "lastUpdated": "2016-06-08T13:52:45+01:00",
      |          "id": 238,
      |          "dateCreated": "2016-06-08T13:52:45+01:00",
      |          "tables": [{
      |            "name": "kitchen",
      |            "source": "fibaro",
      |            "lastUpdated": "2016-06-08T13:52:45+01:00",
      |            "subTables": [{
      |              "name": "kitchenElectricity",
      |              "source": "fibaro",
      |              "lastUpdated": "2016-06-08T13:52:45+01:00",
      |              "id": 83,
      |              "dateCreated": "2016-06-08T13:52:45+01:00",
      |              "fields": [{
      |                "name": "subtableTestField1",
      |                "lastUpdated": "2016-06-08T13:52:45+01:00",
      |                "id": 187,
      |                "dateCreated": "2016-06-08T13:52:45+01:00",
      |                "tableId": 83,
      |                "values": [{
      |                  "id": 410,
      |                  "dateCreated": "2016-06-08T13:52:45+01:00",
      |                  "lastUpdated": "2016-06-08T13:52:45+01:00",
      |                  "value": "testValue5-2"
      |                }]
      |              }, {
      |                "name": "subtableTestField2",
      |                "lastUpdated": "2016-06-08T13:52:45+01:00",
      |                "id": 188,
      |                "dateCreated": "2016-06-08T13:52:45+01:00",
      |                "tableId": 83,
      |                "values": [{
      |                  "id": 411,
      |                  "dateCreated": "2016-06-08T13:52:45+01:00",
      |                  "lastUpdated": "2016-06-08T13:52:45+01:00",
      |                  "value": "testValue5-3"
      |                }]
      |              }]
      |            }],
      |            "id": 82,
      |            "dateCreated": "2016-06-08T13:52:45+01:00",
      |            "fields": [{
      |              "name": "tableTestField",
      |              "lastUpdated": "2016-06-08T13:52:45+01:00",
      |              "id": 186,
      |              "dateCreated": "2016-06-08T13:52:45+01:00",
      |              "tableId": 82,
      |              "values": [{
      |                "id": 409,
      |                "dateCreated": "2016-06-08T13:52:45+01:00",
      |                "lastUpdated": "2016-06-08T13:52:45+01:00",
      |                "value": "testValue5-1"
      |              }]
      |            }]
      |          }]
      |        }, {
      |          "name": "testRecord 4",
      |          "lastUpdated": "2016-06-08T13:52:45+01:00",
      |          "id": 237,
      |          "dateCreated": "2016-06-08T13:52:45+01:00",
      |          "tables": [{
      |            "name": "kitchen",
      |            "source": "fibaro",
      |            "lastUpdated": "2016-06-08T13:52:45+01:00",
      |            "subTables": [{
      |              "name": "kitchenElectricity",
      |              "source": "fibaro",
      |              "lastUpdated": "2016-06-08T13:52:45+01:00",
      |              "id": 83,
      |              "dateCreated": "2016-06-08T13:52:45+01:00",
      |              "fields": [{
      |                "name": "subtableTestField1",
      |                "lastUpdated": "2016-06-08T13:52:45+01:00",
      |                "id": 187,
      |                "dateCreated": "2016-06-08T13:52:45+01:00",
      |                "tableId": 83,
      |                "values": [{
      |                  "id": 407,
      |                  "dateCreated": "2016-06-08T13:52:45+01:00",
      |                  "lastUpdated": "2016-06-08T13:52:45+01:00",
      |                  "value": "testValue4-2"
      |                }]
      |              }, {
      |                "name": "subtableTestField2",
      |                "lastUpdated": "2016-06-08T13:52:45+01:00",
      |                "id": 188,
      |                "dateCreated": "2016-06-08T13:52:45+01:00",
      |                "tableId": 83,
      |                "values": [{
      |                  "id": 408,
      |                  "dateCreated": "2016-06-08T13:52:45+01:00",
      |                  "lastUpdated": "2016-06-08T13:52:45+01:00",
      |                  "value": "testValue4-3"
      |                }]
      |              }]
      |            }],
      |            "id": 82,
      |            "dateCreated": "2016-06-08T13:52:45+01:00",
      |            "fields": [{
      |              "name": "tableTestField",
      |              "lastUpdated": "2016-06-08T13:52:45+01:00",
      |              "id": 186,
      |              "dateCreated": "2016-06-08T13:52:45+01:00",
      |              "tableId": 82,
      |              "values": [{
      |                "id": 406,
      |                "dateCreated": "2016-06-08T13:52:45+01:00",
      |                "lastUpdated": "2016-06-08T13:52:45+01:00",
      |                "value": "testValue4-1"
      |              }]
      |            }]
      |          }]
      |        }, {
      |          "name": "testRecord 2",
      |          "lastUpdated": "2016-06-08T13:52:45+01:00",
      |          "id": 236,
      |          "dateCreated": "2016-06-08T13:52:45+01:00",
      |          "tables": [{
      |            "name": "kitchen",
      |            "source": "fibaro",
      |            "lastUpdated": "2016-06-08T13:52:45+01:00",
      |            "subTables": [{
      |              "name": "kitchenElectricity",
      |              "source": "fibaro",
      |              "lastUpdated": "2016-06-08T13:52:45+01:00",
      |              "id": 83,
      |              "dateCreated": "2016-06-08T13:52:45+01:00",
      |              "fields": [{
      |                "name": "subtableTestField1",
      |                "lastUpdated": "2016-06-08T13:52:45+01:00",
      |                "id": 187,
      |                "dateCreated": "2016-06-08T13:52:45+01:00",
      |                "tableId": 83,
      |                "values": [{
      |                  "id": 404,
      |                  "dateCreated": "2016-06-08T13:52:45+01:00",
      |                  "lastUpdated": "2016-06-08T13:52:45+01:00",
      |                  "value": "testValue2-2"
      |                }]
      |              }, {
      |                "name": "subtableTestField2",
      |                "lastUpdated": "2016-06-08T13:52:45+01:00",
      |                "id": 188,
      |                "dateCreated": "2016-06-08T13:52:45+01:00",
      |                "tableId": 83,
      |                "values": [{
      |                  "id": 405,
      |                  "dateCreated": "2016-06-08T13:52:45+01:00",
      |                  "lastUpdated": "2016-06-08T13:52:45+01:00",
      |                  "value": "testValue2-3"
      |                }]
      |              }]
      |            }],
      |            "id": 82,
      |            "dateCreated": "2016-06-08T13:52:45+01:00",
      |            "fields": [{
      |              "name": "tableTestField",
      |              "lastUpdated": "2016-06-08T13:52:45+01:00",
      |              "id": 186,
      |              "dateCreated": "2016-06-08T13:52:45+01:00",
      |              "tableId": 82,
      |              "values": [{
      |                "id": 403,
      |                "dateCreated": "2016-06-08T13:52:45+01:00",
      |                "lastUpdated": "2016-06-08T13:52:45+01:00",
      |                "value": "testValue2-1"
      |              }]
      |            }]
      |          }]
      |        }, {
      |          "name": "testRecord 1",
      |          "lastUpdated": "2016-06-08T13:52:45+01:00",
      |          "id": 235,
      |          "dateCreated": "2016-06-08T13:52:45+01:00",
      |          "tables": [{
      |            "name": "kitchen",
      |            "source": "fibaro",
      |            "lastUpdated": "2016-06-08T13:52:45+01:00",
      |            "subTables": [{
      |              "name": "kitchenElectricity",
      |              "source": "fibaro",
      |              "lastUpdated": "2016-06-08T13:52:45+01:00",
      |              "id": 83,
      |              "dateCreated": "2016-06-08T13:52:45+01:00",
      |              "fields": [{
      |                "name": "subtableTestField1",
      |                "lastUpdated": "2016-06-08T13:52:45+01:00",
      |                "id": 187,
      |                "dateCreated": "2016-06-08T13:52:45+01:00",
      |                "tableId": 83,
      |                "values": [{
      |                  "id": 401,
      |                  "dateCreated": "2016-06-08T13:52:45+01:00",
      |                  "lastUpdated": "2016-06-08T13:52:45+01:00",
      |                  "value": "testValue2"
      |                }]
      |              }, {
      |                "name": "subtableTestField2",
      |                "lastUpdated": "2016-06-08T13:52:45+01:00",
      |                "id": 188,
      |                "dateCreated": "2016-06-08T13:52:45+01:00",
      |                "tableId": 83,
      |                "values": [{
      |                  "id": 402,
      |                  "dateCreated": "2016-06-08T13:52:45+01:00",
      |                  "lastUpdated": "2016-06-08T13:52:45+01:00",
      |                  "value": "testValue3"
      |                }]
      |              }]
      |            }],
      |            "id": 82,
      |            "dateCreated": "2016-06-08T13:52:45+01:00",
      |            "fields": [{
      |              "name": "tableTestField",
      |              "lastUpdated": "2016-06-08T13:52:45+01:00",
      |              "id": 186,
      |              "dateCreated": "2016-06-08T13:52:45+01:00",
      |              "tableId": 82,
      |              "values": [{
      |                "id": 400,
      |                "dateCreated": "2016-06-08T13:52:45+01:00",
      |                "lastUpdated": "2016-06-08T13:52:45+01:00",
      |                "value": "testValue1"
      |              }]
      |            }]
      |          }]
      |        }],
      |        "id": 20,
      |        "dateCreated": "2016-06-08T13:52:46+01:00",
      |        "table": {
      |          "name": "kitchen",
      |          "source": "fibaro",
      |          "lastUpdated": "2016-06-08T13:52:45+01:00",
      |          "id": 82,
      |          "dateCreated": "2016-06-08T13:52:45+01:00"
      |        }
      |      }
      |    }]
      |  },
      |  "kind": "contextless",
      |  "startDate": "2015-09-30T10:00:00+01:00",
      |  "sell": true
      |}
      |
    """.stripMargin
}
