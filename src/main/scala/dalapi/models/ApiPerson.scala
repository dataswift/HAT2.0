package dalapi.models

case class ApiPerson(
    id: Option[Int],
    name: String,
    staticProperties: Option[Seq[ApiPropertyRelationshipStatic]],
    dynamicProperties: Option[Seq[ApiPropertyRelationshipDynamic]],
    people: Option[Seq[ApiPersonRelationship]],
    locations: Option[Seq[ApiLocationRelationship]],
    organisations: Option[Seq[ApiOrganisationRelationship]])

case class ApiPersonRelationship(relationshipType: String, person: ApiPerson)

case class ApiPersonRelationshipType(id: Option[Int], name: String, description: Option[String])