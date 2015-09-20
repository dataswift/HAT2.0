package dalapi.models

import dal.Tables.PeoplePersonRow

case class ApiPerson(
    id: Option[Int],
    name: String,
    personId: String,
    staticProperties: Option[Seq[ApiPropertyRelationshipStatic]],
    dynamicProperties: Option[Seq[ApiPropertyRelationshipDynamic]],
    people: Option[Seq[ApiPersonRelationship]],
    locations: Option[Seq[ApiLocationRelationship]],
    organisations: Option[Seq[ApiOrganisationRelationship]])

object ApiPerson {
  def fromDbModel(entity: PeoplePersonRow) : ApiPerson = {
    new ApiPerson(Some(entity.id), entity.name, entity.personId, None, None, None, None, None)
  }
}

case class ApiPersonRelationship(relationshipType: String, person: ApiPerson)

case class ApiPersonRelationshipType(id: Option[Int], name: String, description: Option[String])