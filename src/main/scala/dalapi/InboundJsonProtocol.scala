package dalapi

import com.wordnik.swagger.annotations.{ApiModelProperty, ApiModel}
import dalapi.models.{ApiDataTable, ApiDataRecord, ApiDataField, ApiDataValue}
import spray.json.{NullOptions, DefaultJsonProtocol}
import scala.reflect.runtime.universe._
import scala.annotation.meta.field
import dalapi.models._

object InboundJsonProtocol extends DefaultJsonProtocol with NullOptions {
  implicit val virtualTableFormat = jsonFormat3(ApiDataTable)
  implicit val dataFieldformat = jsonFormat3(ApiDataField)
  implicit val apiDataRecord = jsonFormat2(ApiDataRecord)
  implicit val apiDataValueFormat = jsonFormat4(ApiDataValue)
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
}
