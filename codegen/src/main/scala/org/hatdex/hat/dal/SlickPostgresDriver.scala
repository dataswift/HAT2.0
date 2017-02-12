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