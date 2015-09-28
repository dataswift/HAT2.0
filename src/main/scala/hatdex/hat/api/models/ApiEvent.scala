package hatdex.hat.api.models

import hatdex.hat.dal.Tables._

case class ApiEvent(
    id: Option[Int],
    name: String,
    staticProperties: Option[Seq[ApiPropertyRelationshipStatic]],
    dynamicProperties: Option[Seq[ApiPropertyRelationshipDynamic]],
    events: Option[Seq[ApiEventRelationship]],
    locations: Option[Seq[ApiLocationRelationship]],
    people: Option[Seq[ApiPersonRelationship]],
    things: Option[Seq[ApiThingRelationship]],
    organisations: Option[Seq[ApiOrganisationRelationship]])

object ApiEvent {
  def fromDbModel(event: EventsEventRow) : ApiEvent = {
    new ApiEvent(Some(event.id), event.name, None, None, None, None, None, None, None)
  }
}

case class ApiEventRelationship(relationshipType: String, event: ApiEvent)