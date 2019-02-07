package org.hatdex.hat.phata.models

import play.api.libs.json._
import play.api.libs.functional.syntax._

case class Vendor(name: String, url: String, logo: String)

object Vendor {
  implicit val vendorReads: Reads[Vendor] = (
    (JsPath \ "name").read[String] and (JsPath \ "url").read[String] and (JsPath \ "logo").read[String])(Vendor.apply _)
  implicit val vendorWrites: Writes[Vendor] = Json.format[Vendor]
}
