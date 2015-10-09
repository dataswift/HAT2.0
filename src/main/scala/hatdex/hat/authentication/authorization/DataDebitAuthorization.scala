package hatdex.hat.authentication.authorization

import hatdex.hat.authentication.models.User
import hatdex.hat.dal.Tables.DataDebitRow

object DataDebitAuthorization {
  def hasPermissionAccessDataDebit(dataDebit: Option[DataDebitRow])(implicit user: User): Boolean = {
    dataDebit match {
      case Some(debit) =>
        (debit.recipientId equals user.userId.toString) && debit.enabled
      case None =>
        false
    }
  }

  def hasPermissionModifyDataDebit(dataDebit: Option[DataDebitRow])(implicit user: User): Boolean = {
    user.role match {
      case "owner" =>
        true
      case _ =>
        false
    }
  }
}
