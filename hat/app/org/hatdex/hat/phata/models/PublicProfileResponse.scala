package org.hatdex.hat.phata.models

import play.api.libs.json.{ JsValue, Json }

case class PublicProfileResponse(
  public: Boolean,
  profile: Option[Map[String, Map[String, String]]],
  notables: Option[Seq[Notable]]
)

object PublicProfileResponse {
  implicit val publicProfileResponseFormat = Json.format[PublicProfileResponse]
}
