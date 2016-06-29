package hatdex.hat.authentication.authorization

import org.joda.time.LocalDateTime

import hatdex.hat.authentication.models.User
import hatdex.hat.dal.Tables.DataDebitRow

object DataDebitAuthorization {
  def hasPermissionAccessDataDebit(dataDebit: Option[DataDebitRow])(implicit user: User): Boolean = {
    dataDebit match {
      case Some(debit) =>
        ((debit.recipientId equals user.userId.toString) &&
          debit.enabled &&
          debit.endDate.isAfter(LocalDateTime.now()) &&
          debit.startDate.isBefore(LocalDateTime.now())) ||   // All the above conditions match OR the user is the owner
        user.role == "owner"
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
