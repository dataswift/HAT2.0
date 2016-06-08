package hatdex.hat.api.endpoints.jsonExamples

/**
 * Created by andrius on 10/10/15.
 */
object BundleExamples {
  val bundleTableKitchenWrong =
    """
      |  {
      |    "name": "Electricity in the kitchen",
      |    "table": {
      |      "name": "kichenElectricity",
      |      "source": "fibaro"
      |    }
      |  }
    """.stripMargin

  val bundleTableKitchen =
    """
      |  {
      |    "name": "Everything kitchen",
      |    "table": {
      |      "id": 2,
      |      "name": "kitchen",
      |      "source": "fibaro"
      |    }
      |  }
    """.stripMargin

  val bundleTableKitchenElectricity =
    """
      |  {
      |    "name": "Electricity in the kitchen",
      |    "table": {
      |      "id": 3,
      |      "name": "kichenElectricity",
      |      "source": "fibaro"
      |    }
      |  }
    """.stripMargin

  val bundleWeekendEvents =
    """
      |  {
      |    "name": "Weekend events at home",
      |    "table": {
      |      "id": 4,
      |      "name": "event",
      |      "source": "Facebook"
      |    },
      |    "slices": [
      |      {
      |        "table": {
      |          "id": 4,
      |          "name": "event",
      |          "source": "Facebook"
      |        },
      |        "conditions": [
      |          {
      |            "field": {
      |              "id": 13,
      |              "tableId": 4,
      |              "name": "location"
      |            },
      |            "value": "event location 1",
      |            "operator": "equal"
      |          }
      |        ]
      |      },
      |      {
      |        "table": {
      |          "id": 4,
      |          "name": "event",
      |          "source": "Facebook"
      |        },
      |        "conditions": [
      |          {
      |            "field": {
      |              "id": 13,
      |              "tableId": 4,
      |              "name": "location"
      |            },
      |            "value": "event location 2",
      |            "operator": "equal"
      |          },
      |          {
      |            "field": {
      |              "id": 14,
      |              "tableId": 4,
      |              "name": "startTime"
      |            },
      |            "value": "event startTime 2",
      |            "operator": "equal"
      |          }
      |        ]
      |      }
      |    ]
      |  }
    """.stripMargin

  val bundleContextlessNoJoin =
  """
    |   {
    |     "name": "Kitchen electricity on weekend parties",
    |     "tables": [
    |       {
    |         "name": "Weekend events at home Combination",
    |         "bundleTable": {
    |           "id": 0,
    |           "name": "Weekend events at home",
    |           "table": {
    |             "id": 4,
    |             "name": "event",
    |             "source": "Facebook"
    |           }
    |         }
    |       },
    |       {
    |         "name": "Electricity in the kitchen Combination",
    |         "bundleTable": {
    |           "id": 0,
    |           "name": "Electricity in the kitchen",
    |           "table": {
    |             "id": 3,
    |             "name": "kichenElectricity",
    |             "source": "fibaro"
    |           }
    |         }
    |       }
    |     ]
    |   }
  """.stripMargin

  val bundleContextlessJoin =
    """
      |   {
      |     "name": "Kitchen electricity on weekend parties",
      |     "tables": [
      |       {
      |         "name": "Weekend events at home",
      |         "bundleTable": {
      |           "id": 0,
      |           "name": "Weekend events at home",
      |           "table": {
      |             "id": 4,
      |             "name": "event",
      |             "source": "Facebook"
      |           }
      |         }
      |       },
      |       {
      |         "name": "Electricity in the kitchen",
      |         "bundleTable": {
      |           "id": 0,
      |           "name": "Electricity in the kitchen",
      |           "table": {
      |             "id": 3,
      |             "name": "kichenElectricity",
      |             "source": "fibaro"
      |           }
      |         },
      |         "bundleJoinField": {
      |           "id": 14,
      |           "tableId": 4,
      |           "name": "startTime"
      |         },
      |         "bundleTableField": {
      |           "id": 10,
      |           "tableId": 3,
      |           "name": "timestamp"
      |         },
      |         "operator": "equal"
      |       }
      |     ]
      |   }
    """.stripMargin

  val bundleValuesExample =
    """
      |{
      |  "name": "Weekend events at home",
      |  "lastUpdated": "2016-06-08T11:02:53+01:00",
      |  "data": [{
      |    "name": "event record 2",
      |    "lastUpdated": "2016-06-08T11:02:50+01:00",
      |    "id": 5,
      |    "dateCreated": "2016-06-08T11:02:50+01:00",
      |    "tables": [{
      |      "name": "event",
      |      "source": "Facebook",
      |      "lastUpdated": "2016-06-08T11:02:50+01:00",
      |      "id": 4,
      |      "dateCreated": "2016-06-08T11:02:50+01:00",
      |      "fields": [{
      |        "name": "name",
      |        "lastUpdated": "2016-06-08T11:02:50+01:00",
      |        "id": 12,
      |        "dateCreated": "2016-06-08T11:02:50+01:00",
      |        "tableId": 4,
      |        "values": [{
      |          "id": 392,
      |          "dateCreated": "2016-06-08T11:02:50+01:00",
      |          "lastUpdated": "2016-06-08T11:02:50+01:00",
      |          "value": "event name 2"
      |        }]
      |      }, {
      |        "name": "location",
      |        "lastUpdated": "2016-06-08T11:02:50+01:00",
      |        "id": 13,
      |        "dateCreated": "2016-06-08T11:02:50+01:00",
      |        "tableId": 4,
      |        "values": [{
      |          "id": 393,
      |          "dateCreated": "2016-06-08T11:02:50+01:00",
      |          "lastUpdated": "2016-06-08T11:02:50+01:00",
      |          "value": "event location 2"
      |        }]
      |      }, {
      |        "name": "startTime",
      |        "lastUpdated": "2016-06-08T11:02:50+01:00",
      |        "id": 14,
      |        "dateCreated": "2016-06-08T11:02:50+01:00",
      |        "tableId": 4,
      |        "values": [{
      |          "id": 394,
      |          "dateCreated": "2016-06-08T11:02:50+01:00",
      |          "lastUpdated": "2016-06-08T11:02:50+01:00",
      |          "value": "event startTime 2"
      |        }]
      |      }, {
      |        "name": "endTime",
      |        "lastUpdated": "2016-06-08T11:02:50+01:00",
      |        "id": 15,
      |        "dateCreated": "2016-06-08T11:02:50+01:00",
      |        "tableId": 4,
      |        "values": [{
      |          "id": 395,
      |          "dateCreated": "2016-06-08T11:02:50+01:00",
      |          "lastUpdated": "2016-06-08T11:02:50+01:00",
      |          "value": "event endTime 2"
      |        }]
      |      }]
      |    }]
      |  }, {
      |    "name": "event record 1",
      |    "lastUpdated": "2016-06-08T11:02:50+01:00",
      |    "id": 4,
      |    "dateCreated": "2016-06-08T11:02:50+01:00",
      |    "tables": [{
      |      "name": "event",
      |      "source": "Facebook",
      |      "lastUpdated": "2016-06-08T11:02:50+01:00",
      |      "id": 4,
      |      "dateCreated": "2016-06-08T11:02:50+01:00",
      |      "fields": [{
      |        "name": "name",
      |        "lastUpdated": "2016-06-08T11:02:50+01:00",
      |        "id": 12,
      |        "dateCreated": "2016-06-08T11:02:50+01:00",
      |        "tableId": 4,
      |        "values": [{
      |          "id": 388,
      |          "dateCreated": "2016-06-08T11:02:50+01:00",
      |          "lastUpdated": "2016-06-08T11:02:50+01:00",
      |          "value": "event name 1"
      |        }]
      |      }, {
      |        "name": "location",
      |        "lastUpdated": "2016-06-08T11:02:50+01:00",
      |        "id": 13,
      |        "dateCreated": "2016-06-08T11:02:50+01:00",
      |        "tableId": 4,
      |        "values": [{
      |          "id": 389,
      |          "dateCreated": "2016-06-08T11:02:50+01:00",
      |          "lastUpdated": "2016-06-08T11:02:50+01:00",
      |          "value": "event location 1"
      |        }]
      |      }, {
      |        "name": "startTime",
      |        "lastUpdated": "2016-06-08T11:02:50+01:00",
      |        "id": 14,
      |        "dateCreated": "2016-06-08T11:02:50+01:00",
      |        "tableId": 4,
      |        "values": [{
      |          "id": 390,
      |          "dateCreated": "2016-06-08T11:02:50+01:00",
      |          "lastUpdated": "2016-06-08T11:02:50+01:00",
      |          "value": "event startTime 1"
      |        }]
      |      }, {
      |        "name": "endTime",
      |        "lastUpdated": "2016-06-08T11:02:50+01:00",
      |        "id": 15,
      |        "dateCreated": "2016-06-08T11:02:50+01:00",
      |        "tableId": 4,
      |        "values": [{
      |          "id": 391,
      |          "dateCreated": "2016-06-08T11:02:50+01:00",
      |          "lastUpdated": "2016-06-08T11:02:50+01:00",
      |          "value": "event endTime 1"
      |        }]
      |      }]
      |    }]
      |  }],
      |  "id": 15,
      |  "dateCreated": "2016-06-08T11:02:53+01:00",
      |  "table": {
      |    "name": "event",
      |    "source": "Facebook",
      |    "lastUpdated": "2016-06-08T11:02:50+01:00",
      |    "id": 4,
      |    "dateCreated": "2016-06-08T11:02:50+01:00"
      |  }
      |}
      |
    """.stripMargin
}
