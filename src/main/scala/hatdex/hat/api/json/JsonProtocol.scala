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
package hatdex.hat.api.json

import hatdex.hat.api.models._
import hatdex.hat.api.models.stats._
import hatdex.hat.authentication.models.{ User, AccessToken }
import spray.json._

trait HatJsonProtocol extends DefaultJsonProtocol with UuidMarshalling with DateTimeMarshalling with ComparisonOperatorMarshalling {
  // Data
  implicit val apiDataValueFormat: RootJsonFormat[ApiDataValue] = rootFormat(lazyFormat(jsonFormat6(ApiDataValue.apply)))
  implicit val dataFieldformat = jsonFormat6(ApiDataField.apply)
  // Need to go via "lazyFormat" for recursive types
  implicit val virtualTableFormat: RootJsonFormat[ApiDataTable] = rootFormat(lazyFormat(jsonFormat7(ApiDataTable.apply)))
  implicit val apiDataRecord = jsonFormat5(ApiDataRecord.apply)

  implicit val apiRecordValues = jsonFormat2(ApiRecordValues.apply)

  // Any id (used for crossreferences)
  implicit val apiGenericId = jsonFormat1(ApiGenericId.apply)

  // Types
  implicit val apiType = jsonFormat5(ApiSystemType.apply)
  implicit val apiUom = jsonFormat6(ApiSystemUnitofmeasurement.apply)

  // Events
  implicit val apiEvent: RootJsonFormat[ApiEvent] = rootFormat(lazyFormat(jsonFormat9(ApiEvent.apply)))

  // Locations
  implicit val apiLocation: RootJsonFormat[ApiLocation] = rootFormat(lazyFormat(jsonFormat6(ApiLocation.apply)))

  // Organistaions
  implicit val apiOrganisation: RootJsonFormat[ApiOrganisation] = rootFormat(lazyFormat(jsonFormat7(ApiOrganisation.apply)))

  // People
  implicit val apiPerson: RootJsonFormat[ApiPerson] = rootFormat(lazyFormat(jsonFormat8(ApiPerson.apply)))
  implicit val apiPersonRelationshipType = jsonFormat3(ApiPersonRelationshipType.apply)

  // Things
  implicit val apiThing: RootJsonFormat[ApiThing] = rootFormat(lazyFormat(jsonFormat6(ApiThing.apply)))

  implicit val apiEventRelationship = jsonFormat2(ApiEventRelationship.apply)
  implicit val apiLocationRelationship = jsonFormat2(ApiLocationRelationship.apply)
  implicit val apiOrganisationRelationship = jsonFormat2(ApiOrganisationRelationship.apply)
  implicit val apiPersonRelationship = jsonFormat2(ApiPersonRelationship.apply)
  implicit val apiThingRelationship = jsonFormat2(ApiThingRelationship.apply)

  // Entity Wrapper
  implicit val apiEntity = jsonFormat6(ApiEntity.apply)

  // Properties
  implicit val apiProperty = jsonFormat7(ApiProperty.apply)

  // Crossrefs
  implicit val apiRelationship = jsonFormat1(ApiRelationship.apply)

  // Property relationships
  implicit val apiPropertyRelationshipStatic = jsonFormat7(ApiPropertyRelationshipStatic.apply)
  implicit val apiPropertyRelationshipDynamic = jsonFormat6(ApiPropertyRelationshipDynamic.apply)

  // Bundles
  implicit val apiBundleDataSourceField: RootJsonFormat[ApiBundleDataSourceField] = rootFormat(lazyFormat(jsonFormat3(ApiBundleDataSourceField.apply)))
  implicit val apiBundleDataSourceDataset = jsonFormat3(ApiBundleDataSourceDataset.apply)
  implicit val apiBundleDataSourceStructure = jsonFormat2(ApiBundleDataSourceStructure.apply)
  implicit val apiBundleContextless = jsonFormat5(ApiBundleContextless.apply)

  implicit val apiBundleSourceDataset = jsonFormat3(ApiBundleContextlessDatasetData.apply)
  implicit val apiBundleContextlessData = jsonFormat3(ApiBundleContextlessData.apply)

  implicit val apiBundleContextProperty =
    jsonFormat8(ApiBundleContextPropertySelection.apply)
  implicit val apiBundleContextEntity =
    jsonFormat7(ApiBundleContextEntitySelection.apply)
  implicit val apiBundleContext: RootJsonFormat[ApiBundleContext] =
    rootFormat(lazyFormat(jsonFormat6(ApiBundleContext.apply)))

  implicit val apiDataDebit = jsonFormat13(ApiDataDebit.apply)
  implicit val apiDataDebitOut = jsonFormat12(ApiDataDebitOut.apply)

  // Users
  implicit val apiUserFormat = jsonFormat5(User.apply)
  implicit val apiAccessTokenFormat = jsonFormat2(AccessToken.apply)

  implicit val apiError = jsonFormat2(ErrorMessage.apply)
  implicit val apiSuccess = jsonFormat1(SuccessResponse.apply)

  // Stats
  implicit val fieldStatsFormat = jsonFormat4(DataFieldStats.apply)
  // Need to go via "lazyFormat" for recursive types
  implicit val tableStatsFormat: RootJsonFormat[DataTableStats] = rootFormat(lazyFormat(jsonFormat5(DataTableStats.apply)))

  implicit val dataDebitStatsFormat = jsonFormat7(DataDebitStats.apply)
  implicit val dataCreditStatsFormat = jsonFormat6(DataCreditStats.apply)
  implicit val dataStorageStatsFormat = jsonFormat4(DataStorageStats.apply)

  // serialising/deserialising between json and sealed case class
  implicit object dataStatsFormat extends JsonFormat[DataStats] {
    def write(obj: DataStats) = {
      obj match {
        case dd: DataDebitStats => dd.toJson
        case dc: DataCreditStats => dc.toJson
        case ds: DataStorageStats => ds.toJson
      }
    }

    def read(json: JsValue): DataStats = {
      json.asJsObject.getFields("operation") match {
        case Seq(JsString("datadebit")) =>
          json.convertTo[DataDebitStats]
        case Seq(JsString("datacredit")) =>
          json.convertTo[DataCreditStats]
        case Seq(JsString("storage")) =>
          json.convertTo[DataStorageStats]
        case _ =>
          error(json)
      }
    }

    def error(v: Any): DataStats = {
      deserializationError(f"'$v' is not a valid statistics object")
    }
  }

  implicit object AnyJsonFormat extends JsonFormat[Any] {
    def write(x: Any) = x match {
      case n: Int => JsNumber(n)
      case s: String => JsString(s)
      case x: Seq[_] => seqFormat[Any].write(x)
      case m: Map[String, _] => mapFormat[String, Any].write(m)
      case b: Boolean if b == true => JsTrue
      case b: Boolean if b == false => JsFalse
      case x => serializationError("Do not understand object of type " + x.getClass.getName)
    }
    def read(value: JsValue) = value match {
      case JsNumber(n) => n.intValue()
      case JsString(s) => s
      case a: JsArray => listFormat[Any].read(value)
      case o: JsObject => mapFormat[String, Any].read(value)
      case JsTrue => true
      case JsFalse => false
      case x => deserializationError("Do not understand how to deserialize " + x)
    }
  }
}

object JsonProtocol extends HatJsonProtocol
