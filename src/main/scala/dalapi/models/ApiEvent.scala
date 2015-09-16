package dalapi.models

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

case class ApiEventRelationship(relationshipType: String, event: ApiEvent)

case class ApiPersonRelationship(relationshipType: String, person: ApiPerson)
case class ApiThingRelationship(relationshipType: String, thing: ApiThing)
case class ApiOrganisationRelationship(relationshipType: String, organisation: ApiOrganisation)