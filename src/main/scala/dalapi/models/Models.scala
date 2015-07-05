package dalapi.models

case class ApiGenericId(
                         id: Int
                         )

case class ApiEvent(id: Option[Int], name: String)

case class ApiLocation(id: Option[Int], name: String)

case class ApiOrganisation(id: Option[Int], name: String)

case class ApiPerson(id: Option[Int], name: String, personId: String)

case class ApiThing(id: Option[Int], name: String)

case class ApiProperty(id: Option[Int], name: String, description: String)

case class ApiRelationship(description: Option[String], relationshipType: Option[String])