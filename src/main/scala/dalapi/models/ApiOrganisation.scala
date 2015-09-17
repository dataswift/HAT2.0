package dalapi.models

case class ApiOrganisation(
    id: Option[Int],
    name: String,
    staticProperties: Option[Seq[ApiPropertyRelationshipStatic]],
    dynamicProperties: Option[Seq[ApiPropertyRelationshipDynamic]],
    organisations: Option[Seq[ApiOrganisationRelationship]],
    locations: Option[Seq[ApiLocationRelationship]])

case class ApiOrganisationRelationship(relationshipType: String, organisation: ApiOrganisation)