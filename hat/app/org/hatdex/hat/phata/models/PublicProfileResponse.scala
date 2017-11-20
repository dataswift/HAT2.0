package org.hatdex.hat.phata.models

import play.api.libs.json.{ Format, Json }

case class PublicProfileResponse(
    public: Boolean,
    profile: Option[Map[String, Map[String, String]]],
    notables: Option[Seq[Notable]])

object PublicProfileResponse {
  implicit val publicProfileResponseFormat: Format[PublicProfileResponse] = Json.format[PublicProfileResponse]
}
