/*
 * Copyright (C) 2016 Andrius Aucinas <andrius.aucinas@hatdex.org>
 * SPDX-License-Identifier: AGPL-3.0
 *
 * This file is part of the Hub of All Things project (HAT).
 *
 * HAT is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation, version 3 of
 * the License.
 *
 * HAT is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See
 * the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General
 * Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 */

package hatdex.hat.api.endpoints.jsonExamples

object DataExamples {
  val tableKitchen =
    """{
      | "name": "kitchen",
      | "source": "fibaro"
    }""".stripMargin

  val malformedTable =
    """{
      | "nam": "kitchen",
      | "source": "fibaro"
    }""".stripMargin

  val tableKitchenElectricity =
    """{
      "name": "kitchenElectricity",
      "source": "fibaro"
    }"""

  val relationshipParent =
    """{
      | "relationshipType": "parent child"
    }""".stripMargin

  val testField =
    """
      |{
      | "name": "tableTestField0",
      | "tableId": 0
      |}
    """.stripMargin

  val nestedTableKitchen =
    """
      |{
      | "name": "largeKitchen",
      | "source": "fibaro",
      | "fields": [
      |   { "name": "tableTestField" },
      |   { "name": "tableTestField2" }
      | ],
      | "subTables": [
      |   {
      |     "name": "largeKitchenElectricity",
      |     "source": "fibaro",
      |     "fields": [
      |       {
      |         "name": "tableTestField3"
      |       },
      |       {
      |         "name": "tableTestField4"
      |       }
      |     ]
      |   }
      | ]
      |}
    """.stripMargin

  val testRecord =
    """{
      | "name": "testRecord 1"
  }""".stripMargin

  val testRecord2 =
    """{
      | "name": "testRecord 2"
  }""".stripMargin

  val apiDataRecordsExample =
    """
      |[
      |  {
      |    "name": "2016-07-26T02:33:53.019Z",
      |    "lastUpdated": "2016-07-26T02:33:53Z",
      |    "id": 2200,
      |    "dateCreated": "2016-07-26T02:33:53Z",
      |    "tables": [
      |      {
      |        "name": "profile",
      |        "source": "rumpel",
      |        "lastUpdated": "2016-07-26T02:33:38Z",
      |        "subTables": [
      |          {
      |            "name": "fb_profile_photo",
      |            "source": "rumpel",
      |            "lastUpdated": "2016-07-26T02:33:38Z",
      |            "id": 154,
      |            "dateCreated": "2016-07-26T02:33:38Z",
      |            "fields": [
      |              {
      |                "name": "private",
      |                "lastUpdated": "2016-07-26T02:33:38Z",
      |                "id": 284,
      |                "dateCreated": "2016-07-26T02:33:38Z",
      |                "tableId": 154,
      |                "values": [
      |                  {
      |                    "id": 31177,
      |                    "dateCreated": "2016-07-26T02:33:53Z",
      |                    "value": "true"
      |                  }
      |                ]
      |              }
      |            ]
      |          },
      |          {
      |            "name": "personal",
      |            "source": "rumpel",
      |            "lastUpdated": "2016-07-26T02:33:38Z",
      |            "id": 155,
      |            "dateCreated": "2016-07-26T02:33:38Z",
      |            "fields": [
      |              {
      |                "name": "middle_name",
      |                "lastUpdated": "2016-07-26T02:33:38Z",
      |                "id": 290,
      |                "dateCreated": "2016-07-26T02:33:38Z",
      |                "tableId": 155,
      |                "values": [
      |                  {
      |                    "id": 31180,
      |                    "dateCreated": "2016-07-26T02:33:53Z",
      |                    "value": ""
      |                  }
      |                ]
      |              },
      |              {
      |                "name": "private",
      |                "lastUpdated": "2016-07-26T02:33:38Z",
      |                "id": 292,
      |                "dateCreated": "2016-07-26T02:33:38Z",
      |                "tableId": 155,
      |                "values": [
      |                  {
      |                    "id": 31183,
      |                    "dateCreated": "2016-07-26T02:33:53Z",
      |                    "value": "false"
      |                  }
      |                ]
      |              },
      |              {
      |                "name": "title",
      |                "lastUpdated": "2016-07-26T02:33:38Z",
      |                "id": 288,
      |                "dateCreated": "2016-07-26T02:33:38Z",
      |                "tableId": 155,
      |                "values": [
      |                  {
      |                    "id": 31178,
      |                    "dateCreated": "2016-07-26T02:33:53Z",
      |                    "value": "Dr"
      |                  }
      |                ]
      |              },
      |              {
      |                "name": "first_name",
      |                "lastUpdated": "2016-07-26T02:33:38Z",
      |                "id": 289,
      |                "dateCreated": "2016-07-26T02:33:38Z",
      |                "tableId": 155,
      |                "values": [
      |                  {
      |                    "id": 31179,
      |                    "dateCreated": "2016-07-26T02:33:53Z",
      |                    "value": "Andrius"
      |                  }
      |                ]
      |              },
      |              {
      |                "name": "last_name",
      |                "lastUpdated": "2016-07-26T02:33:38Z",
      |                "id": 291,
      |                "dateCreated": "2016-07-26T02:33:38Z",
      |                "tableId": 155,
      |                "values": [
      |                  {
      |                    "id": 31181,
      |                    "dateCreated": "2016-07-26T02:33:53Z",
      |                    "value": "Aucinas"
      |                  }
      |                ]
      |              },
      |              {
      |                "name": "preferred_name",
      |                "lastUpdated": "2016-07-26T02:33:38Z",
      |                "id": 293,
      |                "dateCreated": "2016-07-26T02:33:38Z",
      |                "tableId": 155,
      |                "values": [
      |                  {
      |                    "id": 31182,
      |                    "dateCreated": "2016-07-26T02:33:53Z",
      |                    "value": ""
      |                  }
      |                ]
      |              }
      |            ]
      |          },
      |          {
      |            "name": "birth",
      |            "source": "rumpel",
      |            "lastUpdated": "2016-07-26T02:33:38Z",
      |            "id": 156,
      |            "dateCreated": "2016-07-26T02:33:38Z",
      |            "fields": [
      |              {
      |                "name": "date",
      |                "lastUpdated": "2016-07-26T02:33:38Z",
      |                "id": 286,
      |                "dateCreated": "2016-07-26T02:33:38Z",
      |                "tableId": 156,
      |                "values": [
      |                  {
      |                    "id": 31186,
      |                    "dateCreated": "2016-07-26T02:33:53Z",
      |                    "value": ""
      |                  }
      |                ]
      |              },
      |              {
      |                "name": "private",
      |                "lastUpdated": "2016-07-26T02:33:38Z",
      |                "id": 287,
      |                "dateCreated": "2016-07-26T02:33:38Z",
      |                "tableId": 156,
      |                "values": [
      |                  {
      |                    "id": 31187,
      |                    "dateCreated": "2016-07-26T02:33:53Z",
      |                    "value": "true"
      |                  }
      |                ]
      |              }
      |            ]
      |          },
      |          {
      |            "name": "gender",
      |            "source": "rumpel",
      |            "lastUpdated": "2016-07-26T02:33:38Z",
      |            "id": 157,
      |            "dateCreated": "2016-07-26T02:33:38Z",
      |            "fields": [
      |              {
      |                "name": "type",
      |                "lastUpdated": "2016-07-26T02:33:38Z",
      |                "id": 296,
      |                "dateCreated": "2016-07-26T02:33:38Z",
      |                "tableId": 157,
      |                "values": [
      |                  {
      |                    "id": 31188,
      |                    "dateCreated": "2016-07-26T02:33:53Z",
      |                    "value": ""
      |                  }
      |                ]
      |              },
      |              {
      |                "name": "private",
      |                "lastUpdated": "2016-07-26T02:33:38Z",
      |                "id": 297,
      |                "dateCreated": "2016-07-26T02:33:38Z",
      |                "tableId": 157,
      |                "values": [
      |                  {
      |                    "id": 31189,
      |                    "dateCreated": "2016-07-26T02:33:53Z",
      |                    "value": "true"
      |                  }
      |                ]
      |              }
      |            ]
      |          },
      |          {
      |            "name": "nick",
      |            "source": "rumpel",
      |            "lastUpdated": "2016-07-26T02:33:38Z",
      |            "id": 158,
      |            "dateCreated": "2016-07-26T02:33:38Z",
      |            "fields": [
      |              {
      |                "name": "private",
      |                "lastUpdated": "2016-07-26T02:33:38Z",
      |                "id": 295,
      |                "dateCreated": "2016-07-26T02:33:38Z",
      |                "tableId": 158,
      |                "values": [
      |                  {
      |                    "id": 31185,
      |                    "dateCreated": "2016-07-26T02:33:53Z",
      |                    "value": "true"
      |                  }
      |                ]
      |              },
      |              {
      |                "name": "name",
      |                "lastUpdated": "2016-07-26T02:33:38Z",
      |                "id": 294,
      |                "dateCreated": "2016-07-26T02:33:38Z",
      |                "tableId": 158,
      |                "values": [
      |                  {
      |                    "id": 31184,
      |                    "dateCreated": "2016-07-26T02:33:53Z",
      |                    "value": ""
      |                  }
      |                ]
      |              }
      |            ]
      |          },
      |          {
      |            "name": "age",
      |            "source": "rumpel",
      |            "lastUpdated": "2016-07-26T02:33:38Z",
      |            "id": 159,
      |            "dateCreated": "2016-07-26T02:33:38Z",
      |            "fields": [
      |              {
      |                "name": "private",
      |                "lastUpdated": "2016-07-26T02:33:38Z",
      |                "id": 300,
      |                "dateCreated": "2016-07-26T02:33:38Z",
      |                "tableId": 159,
      |                "values": [
      |                  {
      |                    "id": 31191,
      |                    "dateCreated": "2016-07-26T02:33:53Z",
      |                    "value": "true"
      |                  }
      |                ]
      |              },
      |              {
      |                "name": "group",
      |                "lastUpdated": "2016-07-26T02:33:38Z",
      |                "id": 302,
      |                "dateCreated": "2016-07-26T02:33:38Z",
      |                "tableId": 159,
      |                "values": [
      |                  {
      |                    "id": 31190,
      |                    "dateCreated": "2016-07-26T02:33:53Z",
      |                    "value": ""
      |                  }
      |                ]
      |              }
      |            ]
      |          },
      |          {
      |            "name": "primary_email",
      |            "source": "rumpel",
      |            "lastUpdated": "2016-07-26T02:33:38Z",
      |            "id": 160,
      |            "dateCreated": "2016-07-26T02:33:38Z",
      |            "fields": [
      |              {
      |                "name": "value",
      |                "lastUpdated": "2016-07-26T02:33:38Z",
      |                "id": 298,
      |                "dateCreated": "2016-07-26T02:33:38Z",
      |                "tableId": 160,
      |                "values": [
      |                  {
      |                    "id": 31192,
      |                    "dateCreated": "2016-07-26T02:33:53Z",
      |                    "value": ""
      |                  }
      |                ]
      |              },
      |              {
      |                "name": "private",
      |                "lastUpdated": "2016-07-26T02:33:38Z",
      |                "id": 299,
      |                "dateCreated": "2016-07-26T02:33:38Z",
      |                "tableId": 160,
      |                "values": [
      |                  {
      |                    "id": 31193,
      |                    "dateCreated": "2016-07-26T02:33:53Z",
      |                    "value": "true"
      |                  }
      |                ]
      |              }
      |            ]
      |          },
      |          {
      |            "name": "alternative_email",
      |            "source": "rumpel",
      |            "lastUpdated": "2016-07-26T02:33:38Z",
      |            "id": 161,
      |            "dateCreated": "2016-07-26T02:33:38Z",
      |            "fields": [
      |              {
      |                "name": "private",
      |                "lastUpdated": "2016-07-26T02:33:38Z",
      |                "id": 308,
      |                "dateCreated": "2016-07-26T02:33:38Z",
      |                "tableId": 161,
      |                "values": [
      |                  {
      |                    "id": 31195,
      |                    "dateCreated": "2016-07-26T02:33:53Z",
      |                    "value": "true"
      |                  }
      |                ]
      |              },
      |              {
      |                "name": "value",
      |                "lastUpdated": "2016-07-26T02:33:38Z",
      |                "id": 306,
      |                "dateCreated": "2016-07-26T02:33:38Z",
      |                "tableId": 161,
      |                "values": [
      |                  {
      |                    "id": 31194,
      |                    "dateCreated": "2016-07-26T02:33:53Z",
      |                    "value": ""
      |                  }
      |                ]
      |              }
      |            ]
      |          },
      |          {
      |            "name": "home_phone",
      |            "source": "rumpel",
      |            "lastUpdated": "2016-07-26T02:33:38Z",
      |            "id": 162,
      |            "dateCreated": "2016-07-26T02:33:38Z",
      |            "fields": [
      |              {
      |                "name": "no",
      |                "lastUpdated": "2016-07-26T02:33:38Z",
      |                "id": 283,
      |                "dateCreated": "2016-07-26T02:33:38Z",
      |                "tableId": 162,
      |                "values": [
      |                  {
      |                    "id": 31196,
      |                    "dateCreated": "2016-07-26T02:33:53Z",
      |                    "value": ""
      |                  }
      |                ]
      |              },
      |              {
      |                "name": "private",
      |                "lastUpdated": "2016-07-26T02:33:38Z",
      |                "id": 285,
      |                "dateCreated": "2016-07-26T02:33:38Z",
      |                "tableId": 162,
      |                "values": [
      |                  {
      |                    "id": 31197,
      |                    "dateCreated": "2016-07-26T02:33:53Z",
      |                    "value": "true"
      |                  }
      |                ]
      |              }
      |            ]
      |          },
      |          {
      |            "name": "mobile",
      |            "source": "rumpel",
      |            "lastUpdated": "2016-07-26T02:33:38Z",
      |            "id": 163,
      |            "dateCreated": "2016-07-26T02:33:38Z",
      |            "fields": [
      |              {
      |                "name": "private",
      |                "lastUpdated": "2016-07-26T02:33:38Z",
      |                "id": 301,
      |                "dateCreated": "2016-07-26T02:33:38Z",
      |                "tableId": 163,
      |                "values": [
      |                  {
      |                    "id": 31199,
      |                    "dateCreated": "2016-07-26T02:33:53Z",
      |                    "value": "true"
      |                  }
      |                ]
      |              },
      |              {
      |                "name": "no",
      |                "lastUpdated": "2016-07-26T02:33:38Z",
      |                "id": 303,
      |                "dateCreated": "2016-07-26T02:33:38Z",
      |                "tableId": 163,
      |                "values": [
      |                  {
      |                    "id": 31198,
      |                    "dateCreated": "2016-07-26T02:33:53Z",
      |                    "value": ""
      |                  }
      |                ]
      |              }
      |            ]
      |          },
      |          {
      |            "name": "address_details",
      |            "source": "rumpel",
      |            "lastUpdated": "2016-07-26T02:33:38Z",
      |            "id": 164,
      |            "dateCreated": "2016-07-26T02:33:38Z",
      |            "fields": [
      |              {
      |                "name": "street",
      |                "lastUpdated": "2016-07-26T02:33:39Z",
      |                "id": 312,
      |                "dateCreated": "2016-07-26T02:33:39Z",
      |                "tableId": 164,
      |                "values": [
      |                  {
      |                    "id": 31201,
      |                    "dateCreated": "2016-07-26T02:33:53Z",
      |                    "value": ""
      |                  }
      |                ]
      |              },
      |              {
      |                "name": "private",
      |                "lastUpdated": "2016-07-26T02:33:39Z",
      |                "id": 315,
      |                "dateCreated": "2016-07-26T02:33:39Z",
      |                "tableId": 164,
      |                "values": [
      |                  {
      |                    "id": 31203,
      |                    "dateCreated": "2016-07-26T02:33:53Z",
      |                    "value": "true"
      |                  }
      |                ]
      |              },
      |              {
      |                "name": "no",
      |                "lastUpdated": "2016-07-26T02:33:39Z",
      |                "id": 311,
      |                "dateCreated": "2016-07-26T02:33:39Z",
      |                "tableId": 164,
      |                "values": [
      |                  {
      |                    "id": 31200,
      |                    "dateCreated": "2016-07-26T02:33:53Z",
      |                    "value": ""
      |                  }
      |                ]
      |              },
      |              {
      |                "name": "postcode",
      |                "lastUpdated": "2016-07-26T02:33:39Z",
      |                "id": 314,
      |                "dateCreated": "2016-07-26T02:33:39Z",
      |                "tableId": 164,
      |                "values": [
      |                  {
      |                    "id": 31202,
      |                    "dateCreated": "2016-07-26T02:33:53Z",
      |                    "value": ""
      |                  }
      |                ]
      |              }
      |            ]
      |          },
      |          {
      |            "name": "address_global",
      |            "source": "rumpel",
      |            "lastUpdated": "2016-07-26T02:33:38Z",
      |            "id": 165,
      |            "dateCreated": "2016-07-26T02:33:38Z",
      |            "fields": [
      |              {
      |                "name": "country",
      |                "lastUpdated": "2016-07-26T02:33:39Z",
      |                "id": 317,
      |                "dateCreated": "2016-07-26T02:33:39Z",
      |                "tableId": 165,
      |                "values": [
      |                  {
      |                    "id": 31206,
      |                    "dateCreated": "2016-07-26T02:33:53Z",
      |                    "value": ""
      |                  }
      |                ]
      |              },
      |              {
      |                "name": "city",
      |                "lastUpdated": "2016-07-26T02:33:39Z",
      |                "id": 316,
      |                "dateCreated": "2016-07-26T02:33:39Z",
      |                "tableId": 165,
      |                "values": [
      |                  {
      |                    "id": 31204,
      |                    "dateCreated": "2016-07-26T02:33:53Z",
      |                    "value": ""
      |                  }
      |                ]
      |              },
      |              {
      |                "name": "private",
      |                "lastUpdated": "2016-07-26T02:33:39Z",
      |                "id": 318,
      |                "dateCreated": "2016-07-26T02:33:39Z",
      |                "tableId": 165,
      |                "values": [
      |                  {
      |                    "id": 31207,
      |                    "dateCreated": "2016-07-26T02:33:53Z",
      |                    "value": "true"
      |                  }
      |                ]
      |              },
      |              {
      |                "name": "county",
      |                "lastUpdated": "2016-07-26T02:33:39Z",
      |                "id": 319,
      |                "dateCreated": "2016-07-26T02:33:39Z",
      |                "tableId": 165,
      |                "values": [
      |                  {
      |                    "id": 31205,
      |                    "dateCreated": "2016-07-26T02:33:53Z",
      |                    "value": ""
      |                  }
      |                ]
      |              }
      |            ]
      |          },
      |          {
      |            "name": "website",
      |            "source": "rumpel",
      |            "lastUpdated": "2016-07-26T02:33:38Z",
      |            "id": 166,
      |            "dateCreated": "2016-07-26T02:33:38Z",
      |            "fields": [
      |              {
      |                "name": "link",
      |                "lastUpdated": "2016-07-26T02:33:38Z",
      |                "id": 304,
      |                "dateCreated": "2016-07-26T02:33:38Z",
      |                "tableId": 166,
      |                "values": [
      |                  {
      |                    "id": 31208,
      |                    "dateCreated": "2016-07-26T02:33:53Z",
      |                    "value": ""
      |                  }
      |                ]
      |              },
      |              {
      |                "name": "private",
      |                "lastUpdated": "2016-07-26T02:33:38Z",
      |                "id": 305,
      |                "dateCreated": "2016-07-26T02:33:38Z",
      |                "tableId": 166,
      |                "values": [
      |                  {
      |                    "id": 31209,
      |                    "dateCreated": "2016-07-26T02:33:53Z",
      |                    "value": "true"
      |                  }
      |                ]
      |              }
      |            ]
      |          },
      |          {
      |            "name": "blog",
      |            "source": "rumpel",
      |            "lastUpdated": "2016-07-26T02:33:38Z",
      |            "id": 167,
      |            "dateCreated": "2016-07-26T02:33:38Z",
      |            "fields": [
      |              {
      |                "name": "link",
      |                "lastUpdated": "2016-07-26T02:33:39Z",
      |                "id": 320,
      |                "dateCreated": "2016-07-26T02:33:39Z",
      |                "tableId": 167,
      |                "values": [
      |                  {
      |                    "id": 31210,
      |                    "dateCreated": "2016-07-26T02:33:53Z",
      |                    "value": ""
      |                  }
      |                ]
      |              },
      |              {
      |                "name": "private",
      |                "lastUpdated": "2016-07-26T02:33:39Z",
      |                "id": 321,
      |                "dateCreated": "2016-07-26T02:33:39Z",
      |                "tableId": 167,
      |                "values": [
      |                  {
      |                    "id": 31211,
      |                    "dateCreated": "2016-07-26T02:33:53Z",
      |                    "value": "true"
      |                  }
      |                ]
      |              }
      |            ]
      |          },
      |          {
      |            "name": "facebook",
      |            "source": "rumpel",
      |            "lastUpdated": "2016-07-26T02:33:38Z",
      |            "id": 168,
      |            "dateCreated": "2016-07-26T02:33:38Z",
      |            "fields": [
      |              {
      |                "name": "link",
      |                "lastUpdated": "2016-07-26T02:33:39Z",
      |                "id": 322,
      |                "dateCreated": "2016-07-26T02:33:39Z",
      |                "tableId": 168,
      |                "values": [
      |                  {
      |                    "id": 31212,
      |                    "dateCreated": "2016-07-26T02:33:53Z",
      |                    "value": ""
      |                  }
      |                ]
      |              },
      |              {
      |                "name": "private",
      |                "lastUpdated": "2016-07-26T02:33:39Z",
      |                "id": 323,
      |                "dateCreated": "2016-07-26T02:33:39Z",
      |                "tableId": 168,
      |                "values": [
      |                  {
      |                    "id": 31213,
      |                    "dateCreated": "2016-07-26T02:33:53Z",
      |                    "value": "true"
      |                  }
      |                ]
      |              }
      |            ]
      |          },
      |          {
      |            "name": "linkedin",
      |            "source": "rumpel",
      |            "lastUpdated": "2016-07-26T02:33:38Z",
      |            "id": 169,
      |            "dateCreated": "2016-07-26T02:33:38Z",
      |            "fields": [
      |              {
      |                "name": "link",
      |                "lastUpdated": "2016-07-26T02:33:38Z",
      |                "id": 307,
      |                "dateCreated": "2016-07-26T02:33:38Z",
      |                "tableId": 169,
      |                "values": [
      |                  {
      |                    "id": 31214,
      |                    "dateCreated": "2016-07-26T02:33:53Z",
      |                    "value": ""
      |                  }
      |                ]
      |              },
      |              {
      |                "name": "private",
      |                "lastUpdated": "2016-07-26T02:33:38Z",
      |                "id": 313,
      |                "dateCreated": "2016-07-26T02:33:38Z",
      |                "tableId": 169,
      |                "values": [
      |                  {
      |                    "id": 31215,
      |                    "dateCreated": "2016-07-26T02:33:53Z",
      |                    "value": "true"
      |                  }
      |                ]
      |              }
      |            ]
      |          },
      |          {
      |            "name": "twitter",
      |            "source": "rumpel",
      |            "lastUpdated": "2016-07-26T02:33:38Z",
      |            "id": 170,
      |            "dateCreated": "2016-07-26T02:33:38Z",
      |            "fields": [
      |              {
      |                "name": "link",
      |                "lastUpdated": "2016-07-26T02:33:39Z",
      |                "id": 324,
      |                "dateCreated": "2016-07-26T02:33:39Z",
      |                "tableId": 170,
      |                "values": [
      |                  {
      |                    "id": 31216,
      |                    "dateCreated": "2016-07-26T02:33:53Z",
      |                    "value": ""
      |                  }
      |                ]
      |              },
      |              {
      |                "name": "private",
      |                "lastUpdated": "2016-07-26T02:33:39Z",
      |                "id": 325,
      |                "dateCreated": "2016-07-26T02:33:39Z",
      |                "tableId": 170,
      |                "values": [
      |                  {
      |                    "id": 31217,
      |                    "dateCreated": "2016-07-26T02:33:53Z",
      |                    "value": "true"
      |                  }
      |                ]
      |              }
      |            ]
      |          },
      |          {
      |            "name": "google",
      |            "source": "rumpel",
      |            "lastUpdated": "2016-07-26T02:33:38Z",
      |            "id": 171,
      |            "dateCreated": "2016-07-26T02:33:38Z",
      |            "fields": [
      |              {
      |                "name": "private",
      |                "lastUpdated": "2016-07-26T02:33:39Z",
      |                "id": 329,
      |                "dateCreated": "2016-07-26T02:33:39Z",
      |                "tableId": 171,
      |                "values": [
      |                  {
      |                    "id": 31219,
      |                    "dateCreated": "2016-07-26T02:33:53Z",
      |                    "value": "true"
      |                  }
      |                ]
      |              },
      |              {
      |                "name": "link",
      |                "lastUpdated": "2016-07-26T02:33:39Z",
      |                "id": 326,
      |                "dateCreated": "2016-07-26T02:33:39Z",
      |                "tableId": 171,
      |                "values": [
      |                  {
      |                    "id": 31218,
      |                    "dateCreated": "2016-07-26T02:33:53Z",
      |                    "value": ""
      |                  }
      |                ]
      |              }
      |            ]
      |          },
      |          {
      |            "name": "youtube",
      |            "source": "rumpel",
      |            "lastUpdated": "2016-07-26T02:33:38Z",
      |            "id": 172,
      |            "dateCreated": "2016-07-26T02:33:38Z",
      |            "fields": [
      |              {
      |                "name": "private",
      |                "lastUpdated": "2016-07-26T02:33:39Z",
      |                "id": 310,
      |                "dateCreated": "2016-07-26T02:33:39Z",
      |                "tableId": 172,
      |                "values": [
      |                  {
      |                    "id": 31221,
      |                    "dateCreated": "2016-07-26T02:33:53Z",
      |                    "value": "true"
      |                  }
      |                ]
      |              },
      |              {
      |                "name": "link",
      |                "lastUpdated": "2016-07-26T02:33:39Z",
      |                "id": 309,
      |                "dateCreated": "2016-07-26T02:33:39Z",
      |                "tableId": 172,
      |                "values": [
      |                  {
      |                    "id": 31220,
      |                    "dateCreated": "2016-07-26T02:33:53Z",
      |                    "value": ""
      |                  }
      |                ]
      |              }
      |            ]
      |          },
      |          {
      |            "name": "emergency_contact",
      |            "source": "rumpel",
      |            "lastUpdated": "2016-07-26T02:33:38Z",
      |            "id": 173,
      |            "dateCreated": "2016-07-26T02:33:38Z",
      |            "fields": [
      |              {
      |                "name": "private",
      |                "lastUpdated": "2016-07-26T02:33:39Z",
      |                "id": 330,
      |                "dateCreated": "2016-07-26T02:33:39Z",
      |                "tableId": 173,
      |                "values": [
      |                  {
      |                    "id": 31226,
      |                    "dateCreated": "2016-07-26T02:33:53Z",
      |                    "value": "true"
      |                  }
      |                ]
      |              },
      |              {
      |                "name": "first_name",
      |                "lastUpdated": "2016-07-26T02:33:39Z",
      |                "id": 327,
      |                "dateCreated": "2016-07-26T02:33:39Z",
      |                "tableId": 173,
      |                "values": [
      |                  {
      |                    "id": 31222,
      |                    "dateCreated": "2016-07-26T02:33:53Z",
      |                    "value": ""
      |                  }
      |                ]
      |              },
      |              {
      |                "name": "last_name",
      |                "lastUpdated": "2016-07-26T02:33:39Z",
      |                "id": 328,
      |                "dateCreated": "2016-07-26T02:33:39Z",
      |                "tableId": 173,
      |                "values": [
      |                  {
      |                    "id": 31223,
      |                    "dateCreated": "2016-07-26T02:33:53Z",
      |                    "value": ""
      |                  }
      |                ]
      |              },
      |              {
      |                "name": "relationship",
      |                "lastUpdated": "2016-07-26T02:33:39Z",
      |                "id": 331,
      |                "dateCreated": "2016-07-26T02:33:39Z",
      |                "tableId": 173,
      |                "values": [
      |                  {
      |                    "id": 31225,
      |                    "dateCreated": "2016-07-26T02:33:53Z",
      |                    "value": ""
      |                  }
      |                ]
      |              },
      |              {
      |                "name": "mobile",
      |                "lastUpdated": "2016-07-26T02:33:39Z",
      |                "id": 334,
      |                "dateCreated": "2016-07-26T02:33:39Z",
      |                "tableId": 173,
      |                "values": [
      |                  {
      |                    "id": 31224,
      |                    "dateCreated": "2016-07-26T02:33:53Z",
      |                    "value": ""
      |                  }
      |                ]
      |              }
      |            ]
      |          },
      |          {
      |            "name": "about",
      |            "source": "rumpel",
      |            "lastUpdated": "2016-07-26T02:33:38Z",
      |            "id": 174,
      |            "dateCreated": "2016-07-26T02:33:38Z",
      |            "fields": [
      |              {
      |                "name": "title",
      |                "lastUpdated": "2016-07-26T02:33:39Z",
      |                "id": 335,
      |                "dateCreated": "2016-07-26T02:33:39Z",
      |                "tableId": 174,
      |                "values": [
      |                  {
      |                    "id": 31227,
      |                    "dateCreated": "2016-07-26T02:33:53Z",
      |                    "value": ""
      |                  }
      |                ]
      |              },
      |              {
      |                "name": "body",
      |                "lastUpdated": "2016-07-26T02:33:39Z",
      |                "id": 332,
      |                "dateCreated": "2016-07-26T02:33:39Z",
      |                "tableId": 174,
      |                "values": [
      |                  {
      |                    "id": 31228,
      |                    "dateCreated": "2016-07-26T02:33:53Z",
      |                    "value": ""
      |                  }
      |                ]
      |              },
      |              {
      |                "name": "private",
      |                "lastUpdated": "2016-07-26T02:33:39Z",
      |                "id": 333,
      |                "dateCreated": "2016-07-26T02:33:39Z",
      |                "tableId": 174,
      |                "values": [
      |                  {
      |                    "id": 31229,
      |                    "dateCreated": "2016-07-26T02:33:53Z",
      |                    "value": "true"
      |                  }
      |                ]
      |              }
      |            ]
      |          }
      |        ],
      |        "id": 153,
      |        "dateCreated": "2016-07-26T02:33:38Z",
      |        "fields": [
      |          {
      |            "name": "private",
      |            "lastUpdated": "2016-07-26T02:33:38Z",
      |            "id": 282,
      |            "dateCreated": "2016-07-26T02:33:38Z",
      |            "tableId": 153,
      |            "values": [
      |              {
      |                "id": 31176,
      |                "dateCreated": "2016-07-26T02:33:53Z",
      |                "value": "false"
      |              }
      |            ]
      |          }
      |        ]
      |      }
      |    ]
      |  }
      |]
    """.stripMargin

}
