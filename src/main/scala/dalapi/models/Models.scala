package dalapi.models

case class ApiGenericId(
                         id: Int
                         )

case class ApiEvent(id: Option[Int], name: String)

case class ApiLocation(id: Option[Int], name: String)

case class ApiOrganisation(id: Option[Int], name: String)

case class ApiPerson(id: Option[Int], name: String, personId: String)

case class ApiProperty(id: Option[Int], name: String, description: Option[String], typeId: Int, unitOfMeasurementId: Int)

case class ApiRelationship(relationshipType: String)

case class ApiPropertyRelationshipStatic(relationshipType: String, fieldId: Int, recordId: Int)

case class ApiPropertyRelationshipDynamic(relationshipType: String, fieldId: Int)

case class ApiSystemType(id: Option[Int], name: String, description: Option[String])
