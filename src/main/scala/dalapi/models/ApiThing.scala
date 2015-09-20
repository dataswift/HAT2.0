package dalapi.models

import dal.Tables.ThingsThingRow

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

object ApiThing {
  def fromDbModel(entity: ThingsThingRow) : ApiThing = {
    new ApiThing(Some(entity.id), entity.name, None, None, None, None)
  }
}

case class ApiThingRelationship(relationshipType: String, thing: ApiThing)