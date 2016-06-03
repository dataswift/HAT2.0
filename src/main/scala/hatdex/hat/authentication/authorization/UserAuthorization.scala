package hatdex.hat.authentication.authorization

import hatdex.hat.authentication.models.User
import hatdex.hat.dal.Tables.DataDebitRow

object UserAuthorization {
  /**
    * Only allows those users that have at least a service of the selected.
    * Master service is always allowed.
    * Ex: WithService("serviceA", "serviceB") => only users with services "serviceA" OR "serviceB" (or "master") are allowed.
    */
  def withRole(anyOf: String*)(implicit user: User): Boolean = {
    anyOf.intersect(Seq(user.role)).nonEmpty
  }

  def withRoles(allOf: String*)(implicit user: User): Boolean = {
    allOf.intersect(Seq(user.role)).size == allOf.size
  }

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
