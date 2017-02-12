/*
 * Copyright (C) 2017 HAT Data Exchange Ltd
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
 *
 * Written by Andrius Aucinas <andrius.aucinas@hatdex.org>
 * 2 / 2017
 */

package org.hatdex.hat.dal

import com.github.tminglei.slickpg._
import play.api.libs.json.{ JsValue, Json }

trait SlickPostgresDriver extends ExPostgresDriver
    with PgArraySupport
    with PgDateSupportJoda
    with PgRangeSupport
    with PgHStoreSupport
    with PgSearchSupport
    with PgPlayJsonSupport
    with PgPostGISSupport {

  override val pgjson = "jsonb"
  override val api = MyAPI

  object MyAPI extends API with ArrayImplicits
      with DateTimeImplicits
      with RangeImplicits
      with HStoreImplicits
      with SearchImplicits
      with PlayJsonImplicits
      with SearchAssistants {
    implicit val intListTypeMapper = new SimpleArrayJdbcType[Int]("int4").to(_.toList)
    implicit val strListTypeMapper = new SimpleArrayJdbcType[String]("text").to(_.toList)
    implicit val playJsonArrayTypeMapper =
      new AdvancedArrayJdbcType[JsValue](
        pgjson,
        (s) => utils.SimpleArrayUtils.fromString[JsValue](Json.parse(_))(s).orNull,
        (v) => utils.SimpleArrayUtils.mkString[JsValue](_.toString())(v)
      ).to(_.toList)
  }

}

object SlickPostgresDriver extends SlickPostgresDriver