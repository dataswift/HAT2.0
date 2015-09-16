package dalapi.models

case class ApiLocation(
                     id: Option[Int],
                     name: String,
                     staticProperties: Option[Seq[ApiPropertyRelationshipStatic]],
                     dynamicProperties: Option[Seq[ApiPropertyRelationshipDynamic]],
                     locations: Option[Seq[ApiLocationRelationship]],
                     things: Option[Seq[ApiThingRelationship]])

case class ApiLocationRelationship(relationshipType: String, location: ApiLocation)