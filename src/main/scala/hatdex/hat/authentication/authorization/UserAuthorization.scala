package hatdex.hat.authentication.authorization

import hatdex.hat.authentication.models.User
import hatdex.hat.dal.Tables.DataDebitRow

object UserAuthorization {
  def hasPermissionCreateUser(implicit user: User): Boolean = {
    user.role match {
      case "owner" =>
        true
      case "platform" =>
        true
      case _ =>
        false
    }
  }
}
