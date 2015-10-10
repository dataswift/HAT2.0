package hatdex.hat.api.service.jsonExamples

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
}
