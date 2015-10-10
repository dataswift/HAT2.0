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

  // FIXME: Need more sophisticated checking to disallow disabling platform and owner accounts
  def hasPermissionDisableUser(implicit user: User): Boolean = {
    user.role match {
      case "owner" =>
        true
      case "platform" =>
        true
      case _ =>
        false
    }
  }

  // Need more sophisitcated checking
  def hasPermissionEnableUser(implicit user: User): Boolean = {
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
