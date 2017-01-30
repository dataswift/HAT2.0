package org.hatdex.hat.phata.models

import play.api.data.{ Form, Forms, Mapping }

case class LoginDetails(
  username: String,
  password: String,
  remember: Option[Boolean],
  name: Option[String],
  redirect: Option[String]
)

object LoginDetails {
  private val loginDetailsMapping: Mapping[LoginDetails] = Forms.mapping(
    "username" -> Forms.nonEmptyText,
    "password" -> Forms.nonEmptyText,
    "remember" -> Forms.optional(Forms.boolean),
    "name" -> Forms.optional(Forms.text),
    "redirect" -> Forms.optional(Forms.text)
  )(LoginDetails.apply)(LoginDetails.unapply)

  val loginForm: Form[LoginDetails] = Form(loginDetailsMapping)
}

case class PasswordChange(
  oldPassword: String,
  newPassword: String,
  confirmPassword: String
)

object PasswordChange {
  private val passwordChangeMapping: Mapping[PasswordChange] = Forms.mapping(
    "oldPassword" -> Forms.nonEmptyText,
    "newPassword" -> Forms.tuple(
      "password" -> Forms.nonEmptyText(minLength = 12),
      "confirm" -> Forms.nonEmptyText(minLength = 12)
    ).verifying(
        "constraints.passwords.match",
        passConfirm => passConfirm._1 == passConfirm._2
      )
  )({
      case (oldPassword, (password, confirm)) =>
        PasswordChange(oldPassword, password, confirm)
    })({
      case passwordChange: PasswordChange =>
        Some((passwordChange.oldPassword, (passwordChange.newPassword, passwordChange.confirmPassword)))
    })

  val passwordChangeForm = Form(passwordChangeMapping)
}

