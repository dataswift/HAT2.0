/*
 * Copyright (C) 2016 Andrius Aucinas <andrius.aucinas@hatdex.org>
 * SPDX-License-Identifier: AGPL-3.0
 *
 * This file is part of the Hub of All Things project (HAT).
 *
 * HAT is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation, version 3 of
 * the License.
 *
 * HAT is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See
 * the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General
 * Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 */

package org.hatdex.hat.phata.models

import org.hatdex.hat.api.json.DateTimeMarshalling
import org.joda.time.DateTime
import play.api.libs.json.Json

case class NotableAuthor(
  id: Option[String],
  name: Option[String],
  nick: Option[String],
  phata: String,
  photo_url: Option[String]
)

case class NotableLocation(
  latitude: String,
  longitude: String,
  accuracy: Option[String],
  altitude: Option[String],
  altitude_accuracy: Option[String],
  heading: Option[String],
  speed: Option[String],
  shared: Option[String]
)

case class NotablePhoto(
  link: String,
  source: String,
  caption: String,
  shared: String
)

case class Notable(
  id: Int,
  recordDateLastUpdated: DateTime,
  message: String,
  kind: String,
  created_time: DateTime,
  updated_time: DateTime,
  public_until: Option[DateTime],
  shared: Boolean,
  shared_on: Option[List[String]],
  author: NotableAuthor,
  location: Option[NotableLocation],
  photo: Option[NotablePhoto]
)

object Notable extends DateTimeMarshalling {
  implicit val notableAuthorFormat = Json.format[NotableAuthor]
  implicit val notableLocationFormat = Json.format[NotableLocation]
  implicit val notablePhotoFormat = Json.format[NotablePhoto]
  implicit val notableJsonFormat = Json.format[Notable]
}
