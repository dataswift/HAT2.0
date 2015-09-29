package hatdex.hat.api.models

import java.util.UUID

import org.joda.time.LocalDateTime
import org.joda.time.format.ISODateTimeFormat
import spray.json._


trait UuidMarshalling {
  implicit object UuidJsonFormat extends JsonFormat[UUID] {
    def write(x: UUID) = JsString(x toString ())
    def read(value: JsValue) = value match {
      case JsString(x) => UUID.fromString(x)
      case x => deserializationError("Expected UUID as JsString, but got " + x)
    }
  }
}

object JsonProtocol extends DefaultJsonProtocol with UuidMarshalling {
  implicit object DateTimeFormat extends RootJsonFormat[LocalDateTime] {

    val formatter = ISODateTimeFormat.dateTimeNoMillis

    def write(obj: LocalDateTime): JsValue = {
      JsString(formatter.print(obj.toDateTime))
    }

    def read(json: JsValue): LocalDateTime = json match {
      case JsString(s) => try {
        formatter.parseLocalDateTime(s)
      }
      catch {
        case t: Throwable => error(s)
      }
      case _ =>
        error(json.toString())
    }

    def error(v: Any): LocalDateTime = {
      val example = formatter.print(0)
      deserializationError(f"'$v' is not a valid date value. Dates must be in compact ISO-8601 format, e.g. '$example'")
    }
  }

  def jsonEnum[T <: Enumeration](enu: T) = new JsonFormat[T#Value] {
    def write(obj: T#Value) = JsString(obj.toString)

    def read(json: JsValue) = json match {
      case JsString(txt) => enu.withName(txt)
      case something => throw new DeserializationException(s"Expected a value from enum $enu instead of $something")
    }
  }

  // The scala way of doing "Enums"
  import ComparisonOperators._

  // serialising/deserialising between json and sealed case class
  implicit object ElementKindFormat extends JsonFormat[ComparisonOperator] {
    def write(obj: ComparisonOperator) = JsString(obj.toString)

    def read(json: JsValue): ComparisonOperator = {
      json match {
        case JsString(txt) =>
          try {
            ComparisonOperators.fromString(txt)
          } catch {
            case t: Throwable => error(txt)
          }
        case _ =>
          error(json)
      }
    }

    def error(v: Any): ComparisonOperator = {
      val availableOperators = comparisonOperators.toString()
      deserializationError(f"'$v' is not a valid operator, must be one of $availableOperators")
    }
  }

  // Data
  implicit val apiDataValueFormat = jsonFormat6(ApiDataValue.apply)
  implicit val dataFieldformat = jsonFormat6(ApiDataField.apply)
  // Need to go via "lazyFormat" for recursive types
  implicit val virtualTableFormat: RootJsonFormat[ApiDataTable] = rootFormat(lazyFormat(jsonFormat7(ApiDataTable.apply)))
  implicit val apiDataRecord = jsonFormat5(ApiDataRecord.apply)

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

  // Properties
  implicit val apiProperty = jsonFormat7(ApiProperty.apply)

  // Crossrefs
  implicit val apiRelationship = jsonFormat1(ApiRelationship.apply)

  // Property relationships
  implicit val apiPropertyRelationshipStatic = jsonFormat7(ApiPropertyRelationshipStatic.apply)
  implicit val apiPropertyRelationshipDynamic = jsonFormat6(ApiPropertyRelationshipDynamic.apply)

  // Bundles
  implicit val apiBundleTableCondition = jsonFormat6(ApiBundleTableCondition.apply)
  implicit val apiBundleTableSlice = jsonFormat5(ApiBundleTableSlice.apply)
  implicit val apiBundleTable = jsonFormat7(ApiBundleTable.apply)
  implicit val apiBundleCombination = jsonFormat8(ApiBundleCombination.apply)
  implicit val apiBundleContextless = jsonFormat5(ApiBundleContextless.apply)

  implicit val apiBundleContextlessData = jsonFormat3(ApiBundleContextlessData.apply)

  implicit val apiDataDebit = jsonFormat12(ApiDataDebit.apply)
  implicit val apiDataDebitOut = jsonFormat12(ApiDataDebitOut.apply)
}
