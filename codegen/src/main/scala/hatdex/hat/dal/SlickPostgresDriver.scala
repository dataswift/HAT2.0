package hatdex.hat.dal

import com.github.tminglei.slickpg._

trait SlickPostgresDriver extends ExPostgresDriver
    with PgArraySupport
    with PgDateSupportJoda
    with PgRangeSupport
    with PgHStoreSupport
    with PgSearchSupport
    with PgSprayJsonSupport
    with PgPostGISSupport {

  override val pgjson = "jsonb"
  override val api = MyAPI

  object MyAPI extends API with ArrayImplicits
      with DateTimeImplicits
      with RangeImplicits
      with HStoreImplicits
      with SearchImplicits
      with SprayJsonPlainImplicits
      with SprayJsonImplicits
      with SearchAssistants {
    implicit val intListTypeMapper = new SimpleArrayJdbcType[Int]("int4").to(_.toList)
    implicit val strListTypeMapper = new SimpleArrayJdbcType[String]("text").to(_.toList)
  }

}

object SlickPostgresDriver extends SlickPostgresDriver