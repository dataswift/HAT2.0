package hatdex.hat.api.models

import hatdex.hat.dal.Tables._

case class ApiEntity(
    kind: String,
    event: Option[ApiEvent],
    person: Option[ApiPerson],
    location: Option[ApiLocation],
    thing: Option[ApiThing],
    organisation: Option[ApiOrganisation])