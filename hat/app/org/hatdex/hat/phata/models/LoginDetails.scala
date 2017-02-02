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
  newPassword: String,
  confirmPassword: String
)

object PasswordChange {
  private val passwordChangeMapping: Mapping[PasswordChange] = Forms.mapping(
    "newPassword" -> Forms.tuple(
      "password" -> Forms.nonEmptyText(minLength = 8),
      "confirm" -> Forms.nonEmptyText(minLength = 8)
    ).verifying(
        "constraints.passwords.match",
        passConfirm => passConfirm._1 == passConfirm._2
      )
  )({
      case ((password, confirm)) =>
        PasswordChange(password, confirm)
    })({
      case passwordChange: PasswordChange =>
        Some(((passwordChange.newPassword, passwordChange.confirmPassword)))
    })

  val passwordChangeForm = Form(passwordChangeMapping)
}

