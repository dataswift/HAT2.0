package hatdex.hat.api.json

import hatdex.hat.api.models._
import hatdex.hat.authentication.models.{User, AccessToken}
import spray.json._

object JsonProtocol extends DefaultJsonProtocol with UuidMarshalling with DateTimeMarshalling with ComparisonOperatorMarshalling {
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
  implicit val apiBundleTableCondition = jsonFormat6(ApiBundleTableCondition.apply)
  implicit val apiBundleTableSlice = jsonFormat5(ApiBundleTableSlice.apply)
  implicit val apiBundleTable = jsonFormat7(ApiBundleTable.apply)
  implicit val apiBundleCombination = jsonFormat8(ApiBundleCombination.apply)
  implicit val apiBundleContextless = jsonFormat5(ApiBundleContextless.apply)

  implicit val apiBundleContextlessData = jsonFormat3(ApiBundleContextlessData.apply)

  implicit val apiBundleContextProperty =
    jsonFormat8(ApiBundleContextPropertySelection.apply)
  implicit val apiBundleContextEntity =
    jsonFormat7(ApiBundleContextEntitySelection.apply)
  implicit val apiBundleContext: RootJsonFormat[ApiBundleContext] =
    rootFormat(lazyFormat(jsonFormat6(ApiBundleContext.apply)))

  implicit val apiDataDebit = jsonFormat12(ApiDataDebit.apply)
  implicit val apiDataDebitOut = jsonFormat12(ApiDataDebitOut.apply)

  // Users
  implicit val apiUserFormat = jsonFormat5(User.apply)
  implicit val apiAccessTokenFormat = jsonFormat2(AccessToken.apply)

  implicit val apiError = jsonFormat2(ErrorMessage.apply)
}
