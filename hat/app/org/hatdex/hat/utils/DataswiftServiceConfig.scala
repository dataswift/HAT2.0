package org.hatdex.hat.utils

case class DataswiftServiceConfig(
    name: String,
    host: String,
    path: Option[String]) {

  lazy val address: String = s"$host${path.getOrElse("")}"
}
