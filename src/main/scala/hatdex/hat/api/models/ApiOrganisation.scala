package hatdex.hat.api.models

import hatdex.hat.dal.Tables.OrganisationsOrganisationRow

case class ApiOrganisation(
    id: Option[Int],
    name: String,
    staticProperties: Option[Seq[ApiPropertyRelationshipStatic]],
    dynamicProperties: Option[Seq[ApiPropertyRelationshipDynamic]],
    organisations: Option[Seq[ApiOrganisationRelationship]],
    locations: Option[Seq[ApiLocationRelationship]],
    things: Option[Seq[ApiThingRelationship]])

object ApiOrganisation {
  def fromDbModel(entity: OrganisationsOrganisationRow) : ApiOrganisation = {
    new ApiOrganisation(Some(entity.id), entity.name, None, None, None, None, None)
  }
}

case class ApiOrganisationRelationship(relationshipType: String, organisation: ApiOrganisation)