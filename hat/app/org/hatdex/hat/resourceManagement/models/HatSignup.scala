package org.hatdex.hat.resourceManagement.models

import java.util.UUID

import org.joda.time.DateTime
import play.api.libs.json.Json

case class DatabaseInstance(id: UUID, name: String, password: String)

case class DatabaseServer(id: Int, host: String, port: Int, dateCreated: DateTime, databases: Seq[DatabaseInstance])

case class HatKeys(
  privateKey: String,
  publicKey: String
)

case class HatSignup(
  id: UUID,
  fullName: String,
  username: String,
  email: String,
  pass: String,
  dbPass: String,
  created: Boolean,
  registerTime: DateTime,
  database: Option[DatabaseInstance],
  databaseServer: Option[DatabaseServer],
  keys: Option[HatKeys])

object HatSignup {
  implicit val databaseInstanceFormat = Json.format[DatabaseInstance]
  implicit val databaseServerFormat = Json.format[DatabaseServer]
  implicit val hatKeysFormat = Json.format[HatKeys]
  implicit val hatSignupFormat = Json.format[HatSignup]
}
