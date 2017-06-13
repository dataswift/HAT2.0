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

import java.sql.Timestamp

import com.github.tminglei.slickpg._
import org.joda.time.DateTime
import play.api.libs.json.{ JsNull, JsValue, Json }
import slick.jdbc.JdbcType

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
        (v) => utils.SimpleArrayUtils.mkString[JsValue](_.toString())(v)).to(_.toList)

    import scala.language.implicitConversions

    override implicit val playJsonTypeMapper: JdbcType[JsValue] =
      new GenericJdbcType[JsValue](
        pgjson,
        (v) => Json.parse(v),
        (v) => Json.stringify(v),
        zero = JsNull,
        hasLiteralForm = false)

    override implicit def playJsonColumnExtensionMethods(c: Rep[JsValue]) = {
      new FixedJsonColumnExtensionMethods[JsValue, JsValue](c)
    }
    override implicit def playJsonOptionColumnExtensionMethods(c: Rep[Option[JsValue]]) = {
      new FixedJsonColumnExtensionMethods[JsValue, Option[JsValue]](c)
    }

    class FixedJsonColumnExtensionMethods[JSONType, P1](override val c: Rep[P1])(
        implicit
        tm: JdbcType[JSONType]) extends JsonColumnExtensionMethods[JSONType, P1](c) {
      override def <@:[P2, R](c2: Rep[P2])(implicit om: o#arg[JSONType, P2]#to[Boolean, R]) = {
        om.column(jsonLib.ContainsBy, n, c2.toNode)
      }
    }

    val toJson: Rep[String] => Rep[JsValue] = SimpleFunction.unary[String, JsValue]("to_jsonb")
    val toTimestamp: Rep[Double] => Rep[Timestamp] = SimpleFunction.unary[Double, Timestamp]("to_timestamp")
    val datePart: (Rep[String], Rep[DateTime]) => Rep[String] = SimpleFunction.binary[String, DateTime, String]("date_part")
    val datePartTimestamp: (Rep[String], Rep[Timestamp]) => Rep[String] = SimpleFunction.binary[String, Timestamp, String]("date_part")
  }

}

object SlickPostgresDriver extends SlickPostgresDriver