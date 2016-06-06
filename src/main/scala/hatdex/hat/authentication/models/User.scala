package hatdex.hat.authentication.models

import java.util.UUID

import hatdex.hat.dal.Tables.UserUserRow

case class User(userId: UUID, email: String, pass: Option[String], name: String, role: String)

object User {
  def fromDbModel(user: UserUserRow): User = {
    User(user.userId, user.email, user.pass, user.name, user.role)
  }
}
