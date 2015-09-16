package dalapi.models

import dal.Tables._
import org.joda.time.LocalDateTime

case class ApiGenericId(id: Int)





case class ApiOrganisation(id: Option[Int], name: String)

case class ApiPerson(id: Option[Int], name: String, personId: String)

case class ApiPersonRelationshipType(id: Option[Int], name: String, description: Option[String])
