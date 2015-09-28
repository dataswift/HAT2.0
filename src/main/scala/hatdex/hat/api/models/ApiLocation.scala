package hatdex.hat.api.models

import hatdex.hat.dal.Tables.LocationsLocationRow

case class ApiLocation(
    id: Option[Int],
    name: String,
    staticProperties: Option[Seq[ApiPropertyRelationshipStatic]],
    dynamicProperties: Option[Seq[ApiPropertyRelationshipDynamic]],
    locations: Option[Seq[ApiLocationRelationship]],
    things: Option[Seq[ApiThingRelationship]])

object ApiLocation {
  def fromDbModel(location: LocationsLocationRow) : ApiLocation = {
    new ApiLocation(Some(location.id), location.name, None, None, None, None)
  }
}

case class ApiLocationRelationship(relationshipType: String, location: ApiLocation)