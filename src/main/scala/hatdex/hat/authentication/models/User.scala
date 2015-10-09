package hatdex.hat.authentication.models

import java.util.UUID

case class User(userId: UUID, email: String, pass: Option[String], name: String, role: String)

object User {

}
