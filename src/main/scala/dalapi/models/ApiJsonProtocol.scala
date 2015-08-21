package dalapi.models

import org.joda.time.LocalDateTime
import org.joda.time.format.ISODateTimeFormat
import spray.json._

object ApiJsonProtocol extends DefaultJsonProtocol {
  implicit object DateTimeFormat extends RootJsonFormat[LocalDateTime] {

    val formatter = ISODateTimeFormat.dateTimeNoMillis

    def write(obj: LocalDateTime): JsValue = {
      JsString(formatter.print(obj))
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

  // Data
  implicit val apiDataValueFormat = jsonFormat6(ApiDataValue.apply)
  implicit val dataFieldformat = jsonFormat6(ApiDataField.apply)
  // Need to go via "lazyFormat" for recursive types
  implicit val virtualTableFormat: RootJsonFormat[ApiDataTable] = rootFormat(lazyFormat(jsonFormat7(ApiDataTable.apply)))
  implicit val apiDataRecord = jsonFormat5(ApiDataRecord.apply)

  // Any id (used for crossreferences)
  implicit val apiGenericId = jsonFormat1(ApiGenericId)

  // Events
  implicit val apiEvent = jsonFormat2(ApiEvent)

  // Locations
  implicit val apiLocation = jsonFormat2(ApiLocation)

  // Organistaions
  implicit val apiOrganisation = jsonFormat2(ApiOrganisation)

  // People
  implicit val apiPerson = jsonFormat3(ApiPerson)
  implicit val apiPersonRelationship = jsonFormat3(ApiPersonRelationshipType)

  // Things
  implicit val apiThings = jsonFormat2(ApiThing)

  // Properties
  implicit val apiProperty = jsonFormat5(ApiProperty)

  // Crossrefs
  implicit val apiRelationship = jsonFormat1(ApiRelationship)

  // Property relationships
  implicit val apiPropertyRelationshipStatic = jsonFormat3(ApiPropertyRelationshipStatic)
  implicit val apiPropertyRelationshipDynamic = jsonFormat2(ApiPropertyRelationshipDynamic)

  // Types
  implicit val apiType = jsonFormat3(ApiSystemType)
  implicit val apiUom = jsonFormat4(ApiSystemUnitofmeasurement)

  // Bundles
  implicit val apiOperator = jsonEnum(ComparisonOperator)

  implicit val apiBundleTableCondition = jsonFormat6(ApiBundleTableCondition.apply)
  implicit val apiBundleTableSlice = jsonFormat5(ApiBundleTableSlice.apply)
  implicit val apiBundleTable = jsonFormat6(ApiBundleTable.apply)
  implicit val apiBundleCombination = jsonFormat8(ApiBundleCombination.apply)
  implicit val apiBundleContextless = jsonFormat5(ApiBundleContextless.apply)
}
