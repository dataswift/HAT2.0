package org.hatdex.hat.utils

case class DataswiftServiceConfig(
    name: String,
    host: String,
    //apiVersion: String,
    secure: Boolean,
    path: Option[String]) {

  lazy val address: String = {
    val scheme = if (secure) "https" else "http"

    s"$scheme://$host${path.getOrElse("")}"
  }
}
