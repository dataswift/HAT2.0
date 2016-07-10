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
      |  "lastUpdated": "2016-07-10T17:49:56+01:00",
      |  "price": 100.0,
      |  "key": "1d99018a-bb90-4e27-87ad-3c5efae1ed29",
      |  "dateCreated": "2016-07-10T17:49:56+01:00",
      |  "bundleContextless": {
      |    "id": 26,
      |    "name": "test bundle with full data",
      |    "dataGroups": {
      |      "Facebook": [{
      |        "name": "event",
      |        "table": {
      |          "name": "event",
      |          "source": "Facebook",
      |          "lastUpdated": "2016-07-10T17:49:54+01:00",
      |          "id": 4,
      |          "dateCreated": "2016-07-10T17:49:54+01:00",
      |          "fields": [{
      |            "name": "name",
      |            "lastUpdated": "2016-07-10T17:49:54+01:00",
      |            "id": 12,
      |            "dateCreated": "2016-07-10T17:49:54+01:00",
      |            "tableId": 4
      |          }, {
      |            "name": "location",
      |            "lastUpdated": "2016-07-10T17:49:54+01:00",
      |            "id": 13,
      |            "dateCreated": "2016-07-10T17:49:54+01:00",
      |            "tableId": 4
      |          }, {
      |            "name": "startTime",
      |            "lastUpdated": "2016-07-10T17:49:54+01:00",
      |            "id": 14,
      |            "dateCreated": "2016-07-10T17:49:54+01:00",
      |            "tableId": 4
      |          }, {
      |            "name": "endTime",
      |            "lastUpdated": "2016-07-10T17:49:54+01:00",
      |            "id": 15,
      |            "dateCreated": "2016-07-10T17:49:54+01:00",
      |            "tableId": 4
      |          }]
      |        },
      |        "data": [{
      |          "name": "event record 2",
      |          "lastUpdated": "2016-07-10T17:49:54+01:00",
      |          "id": 5,
      |          "dateCreated": "2016-07-10T17:49:54+01:00",
      |          "tables": [{
      |            "name": "event",
      |            "source": "Facebook",
      |            "lastUpdated": "2016-07-10T17:49:54+01:00",
      |            "id": 4,
      |            "dateCreated": "2016-07-10T17:49:54+01:00",
      |            "fields": [{
      |              "name": "name",
      |              "lastUpdated": "2016-07-10T17:49:54+01:00",
      |              "id": 12,
      |              "dateCreated": "2016-07-10T17:49:54+01:00",
      |              "tableId": 4,
      |              "values": [{
      |                "id": 317,
      |                "dateCreated": "2016-07-10T17:49:54+01:00",
      |                "lastUpdated": "2016-07-10T17:49:54+01:00",
      |                "value": "event name 2"
      |              }]
      |            }, {
      |              "name": "location",
      |              "lastUpdated": "2016-07-10T17:49:54+01:00",
      |              "id": 13,
      |              "dateCreated": "2016-07-10T17:49:54+01:00",
      |              "tableId": 4,
      |              "values": [{
      |                "id": 318,
      |                "dateCreated": "2016-07-10T17:49:54+01:00",
      |                "lastUpdated": "2016-07-10T17:49:54+01:00",
      |                "value": "event location 2"
      |              }]
      |            }, {
      |              "name": "startTime",
      |              "lastUpdated": "2016-07-10T17:49:54+01:00",
      |              "id": 14,
      |              "dateCreated": "2016-07-10T17:49:54+01:00",
      |              "tableId": 4,
      |              "values": [{
      |                "id": 319,
      |                "dateCreated": "2016-07-10T17:49:54+01:00",
      |                "lastUpdated": "2016-07-10T17:49:54+01:00",
      |                "value": "event startTime 2"
      |              }]
      |            }, {
      |              "name": "endTime",
      |              "lastUpdated": "2016-07-10T17:49:54+01:00",
      |              "id": 15,
      |              "dateCreated": "2016-07-10T17:49:54+01:00",
      |              "tableId": 4,
      |              "values": [{
      |                "id": 320,
      |                "dateCreated": "2016-07-10T17:49:54+01:00",
      |                "lastUpdated": "2016-07-10T17:49:54+01:00",
      |                "value": "event endTime 2"
      |              }]
      |            }]
      |          }]
      |        }, {
      |          "name": "event record 3",
      |          "lastUpdated": "2016-07-10T17:49:54+01:00",
      |          "id": 6,
      |          "dateCreated": "2016-07-10T17:49:54+01:00",
      |          "tables": [{
      |            "name": "event",
      |            "source": "Facebook",
      |            "lastUpdated": "2016-07-10T17:49:54+01:00",
      |            "id": 4,
      |            "dateCreated": "2016-07-10T17:49:54+01:00",
      |            "fields": [{
      |              "name": "name",
      |              "lastUpdated": "2016-07-10T17:49:54+01:00",
      |              "id": 12,
      |              "dateCreated": "2016-07-10T17:49:54+01:00",
      |              "tableId": 4,
      |              "values": [{
      |                "id": 321,
      |                "dateCreated": "2016-07-10T17:49:54+01:00",
      |                "lastUpdated": "2016-07-10T17:49:54+01:00",
      |                "value": "event name 3"
      |              }]
      |            }, {
      |              "name": "location",
      |              "lastUpdated": "2016-07-10T17:49:54+01:00",
      |              "id": 13,
      |              "dateCreated": "2016-07-10T17:49:54+01:00",
      |              "tableId": 4,
      |              "values": [{
      |                "id": 322,
      |                "dateCreated": "2016-07-10T17:49:54+01:00",
      |                "lastUpdated": "2016-07-10T17:49:54+01:00",
      |                "value": "event location 3"
      |              }]
      |            }, {
      |              "name": "startTime",
      |              "lastUpdated": "2016-07-10T17:49:54+01:00",
      |              "id": 14,
      |              "dateCreated": "2016-07-10T17:49:54+01:00",
      |              "tableId": 4,
      |              "values": [{
      |                "id": 323,
      |                "dateCreated": "2016-07-10T17:49:54+01:00",
      |                "lastUpdated": "2016-07-10T17:49:54+01:00",
      |                "value": "event startTime 3"
      |              }]
      |            }, {
      |              "name": "endTime",
      |              "lastUpdated": "2016-07-10T17:49:54+01:00",
      |              "id": 15,
      |              "dateCreated": "2016-07-10T17:49:54+01:00",
      |              "tableId": 4,
      |              "values": [{
      |                "id": 324,
      |                "dateCreated": "2016-07-10T17:49:54+01:00",
      |                "lastUpdated": "2016-07-10T17:49:54+01:00",
      |                "value": "event endTime 3"
      |              }]
      |            }]
      |          }]
      |        }, {
      |          "name": "event record 1",
      |          "lastUpdated": "2016-07-10T17:49:54+01:00",
      |          "id": 4,
      |          "dateCreated": "2016-07-10T17:49:54+01:00",
      |          "tables": [{
      |            "name": "event",
      |            "source": "Facebook",
      |            "lastUpdated": "2016-07-10T17:49:54+01:00",
      |            "id": 4,
      |            "dateCreated": "2016-07-10T17:49:54+01:00",
      |            "fields": [{
      |              "name": "name",
      |              "lastUpdated": "2016-07-10T17:49:54+01:00",
      |              "id": 12,
      |              "dateCreated": "2016-07-10T17:49:54+01:00",
      |              "tableId": 4,
      |              "values": [{
      |                "id": 313,
      |                "dateCreated": "2016-07-10T17:49:54+01:00",
      |                "lastUpdated": "2016-07-10T17:49:54+01:00",
      |                "value": "event name 1"
      |              }]
      |            }, {
      |              "name": "location",
      |              "lastUpdated": "2016-07-10T17:49:54+01:00",
      |              "id": 13,
      |              "dateCreated": "2016-07-10T17:49:54+01:00",
      |              "tableId": 4,
      |              "values": [{
      |                "id": 314,
      |                "dateCreated": "2016-07-10T17:49:54+01:00",
      |                "lastUpdated": "2016-07-10T17:49:54+01:00",
      |                "value": "event location 1"
      |              }]
      |            }, {
      |              "name": "startTime",
      |              "lastUpdated": "2016-07-10T17:49:54+01:00",
      |              "id": 14,
      |              "dateCreated": "2016-07-10T17:49:54+01:00",
      |              "tableId": 4,
      |              "values": [{
      |                "id": 315,
      |                "dateCreated": "2016-07-10T17:49:54+01:00",
      |                "lastUpdated": "2016-07-10T17:49:54+01:00",
      |                "value": "event startTime 1"
      |              }]
      |            }, {
      |              "name": "endTime",
      |              "lastUpdated": "2016-07-10T17:49:54+01:00",
      |              "id": 15,
      |              "dateCreated": "2016-07-10T17:49:54+01:00",
      |              "tableId": 4,
      |              "values": [{
      |                "id": 316,
      |                "dateCreated": "2016-07-10T17:49:54+01:00",
      |                "lastUpdated": "2016-07-10T17:49:54+01:00",
      |                "value": "event endTime 1"
      |              }]
      |            }]
      |          }]
      |        }]
      |      }],
      |      "Fibaro": [{
      |        "name": "kitchen",
      |        "table": {
      |          "name": "kitchen",
      |          "source": "Fibaro",
      |          "lastUpdated": "2016-07-10T17:49:54+01:00",
      |          "subTables": [{
      |            "name": "kichenElectricity",
      |            "source": "Fibaro",
      |            "lastUpdated": "2016-07-10T17:49:54+01:00",
      |            "id": 3,
      |            "dateCreated": "2016-07-10T17:49:54+01:00",
      |            "fields": [{
      |              "name": "timestamp",
      |              "lastUpdated": "2016-07-10T17:49:54+01:00",
      |              "id": 10,
      |              "dateCreated": "2016-07-10T17:49:54+01:00",
      |              "tableId": 3
      |            }, {
      |              "name": "value",
      |              "lastUpdated": "2016-07-10T17:49:54+01:00",
      |              "id": 11,
      |              "dateCreated": "2016-07-10T17:49:54+01:00",
      |              "tableId": 3
      |            }]
      |          }],
      |          "id": 2,
      |          "dateCreated": "2016-07-10T17:49:54+01:00"
      |        },
      |        "data": [{
      |          "name": "kitchen record 2",
      |          "lastUpdated": "2016-07-10T17:49:54+01:00",
      |          "id": 2,
      |          "dateCreated": "2016-07-10T17:49:54+01:00",
      |          "tables": [{
      |            "name": "kitchen",
      |            "source": "Fibaro",
      |            "lastUpdated": "2016-07-10T17:49:54+01:00",
      |            "subTables": [{
      |              "name": "kichenElectricity",
      |              "source": "Fibaro",
      |              "lastUpdated": "2016-07-10T17:49:54+01:00",
      |              "id": 3,
      |              "dateCreated": "2016-07-10T17:49:54+01:00",
      |              "fields": [{
      |                "name": "timestamp",
      |                "lastUpdated": "2016-07-10T17:49:54+01:00",
      |                "id": 10,
      |                "dateCreated": "2016-07-10T17:49:54+01:00",
      |                "tableId": 3,
      |                "values": [{
      |                  "id": 309,
      |                  "dateCreated": "2016-07-10T17:49:54+01:00",
      |                  "lastUpdated": "2016-07-10T17:49:54+01:00",
      |                  "value": "kitchen time 2"
      |                }]
      |              }, {
      |                "name": "value",
      |                "lastUpdated": "2016-07-10T17:49:54+01:00",
      |                "id": 11,
      |                "dateCreated": "2016-07-10T17:49:54+01:00",
      |                "tableId": 3,
      |                "values": [{
      |                  "id": 310,
      |                  "dateCreated": "2016-07-10T17:49:54+01:00",
      |                  "lastUpdated": "2016-07-10T17:49:54+01:00",
      |                  "value": "kitchen value 2"
      |                }]
      |              }]
      |            }],
      |            "id": 2,
      |            "dateCreated": "2016-07-10T17:49:54+01:00"
      |          }]
      |        }, {
      |          "name": "kitchen record 1",
      |          "lastUpdated": "2016-07-10T17:49:54+01:00",
      |          "id": 1,
      |          "dateCreated": "2016-07-10T17:49:54+01:00",
      |          "tables": [{
      |            "name": "kitchen",
      |            "source": "Fibaro",
      |            "lastUpdated": "2016-07-10T17:49:54+01:00",
      |            "subTables": [{
      |              "name": "kichenElectricity",
      |              "source": "Fibaro",
      |              "lastUpdated": "2016-07-10T17:49:54+01:00",
      |              "id": 3,
      |              "dateCreated": "2016-07-10T17:49:54+01:00",
      |              "fields": [{
      |                "name": "timestamp",
      |                "lastUpdated": "2016-07-10T17:49:54+01:00",
      |                "id": 10,
      |                "dateCreated": "2016-07-10T17:49:54+01:00",
      |                "tableId": 3,
      |                "values": [{
      |                  "id": 307,
      |                  "dateCreated": "2016-07-10T17:49:54+01:00",
      |                  "lastUpdated": "2016-07-10T17:49:54+01:00",
      |                  "value": "kitchen time 1"
      |                }]
      |              }, {
      |                "name": "value",
      |                "lastUpdated": "2016-07-10T17:49:54+01:00",
      |                "id": 11,
      |                "dateCreated": "2016-07-10T17:49:54+01:00",
      |                "tableId": 3,
      |                "values": [{
      |                  "id": 308,
      |                  "dateCreated": "2016-07-10T17:49:54+01:00",
      |                  "lastUpdated": "2016-07-10T17:49:54+01:00",
      |                  "value": "kitchen value 1"
      |                }]
      |              }]
      |            }],
      |            "id": 2,
      |            "dateCreated": "2016-07-10T17:49:54+01:00"
      |          }]
      |        }, {
      |          "name": "kitchen record 3",
      |          "lastUpdated": "2016-07-10T17:49:54+01:00",
      |          "id": 3,
      |          "dateCreated": "2016-07-10T17:49:54+01:00",
      |          "tables": [{
      |            "name": "kitchen",
      |            "source": "Fibaro",
      |            "lastUpdated": "2016-07-10T17:49:54+01:00",
      |            "subTables": [{
      |              "name": "kichenElectricity",
      |              "source": "Fibaro",
      |              "lastUpdated": "2016-07-10T17:49:54+01:00",
      |              "id": 3,
      |              "dateCreated": "2016-07-10T17:49:54+01:00",
      |              "fields": [{
      |                "name": "timestamp",
      |                "lastUpdated": "2016-07-10T17:49:54+01:00",
      |                "id": 10,
      |                "dateCreated": "2016-07-10T17:49:54+01:00",
      |                "tableId": 3,
      |                "values": [{
      |                  "id": 311,
      |                  "dateCreated": "2016-07-10T17:49:54+01:00",
      |                  "lastUpdated": "2016-07-10T17:49:54+01:00",
      |                  "value": "kitchen time 3"
      |                }]
      |              }, {
      |                "name": "value",
      |                "lastUpdated": "2016-07-10T17:49:54+01:00",
      |                "id": 11,
      |                "dateCreated": "2016-07-10T17:49:54+01:00",
      |                "tableId": 3,
      |                "values": [{
      |                  "id": 312,
      |                  "dateCreated": "2016-07-10T17:49:54+01:00",
      |                  "lastUpdated": "2016-07-10T17:49:54+01:00",
      |                  "value": "kitchen value 3"
      |                }]
      |              }]
      |            }],
      |            "id": 2,
      |            "dateCreated": "2016-07-10T17:49:54+01:00"
      |          }]
      |        }]
      |      }]
      |    }
      |  },
      |  "kind": "contextless",
      |  "startDate": "2015-09-30T10:00:00+01:00",
      |  "sell": true
      |}
    """.stripMargin
}
