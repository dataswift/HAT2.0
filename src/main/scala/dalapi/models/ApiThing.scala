package dalapi.models

/**
 * API format of the Thing, with only the name as the mandatory field
 */
case class ApiThing(
    id: Option[Int],
    name: String,
    staticProperties: Option[Seq[ApiPropertyRelationshipStatic]],
    dynamicProperties: Option[Seq[ApiPropertyRelationshipDynamic]],
    things: Option[Seq[ApiThingRelationship]],
    people: Option[Seq[ApiPersonRelationship]])

case class ApiThingRelationship(relationshipType: String, thing: ApiThing)