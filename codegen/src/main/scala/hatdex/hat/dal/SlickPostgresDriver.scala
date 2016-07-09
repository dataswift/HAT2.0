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

  override lazy val Implicit = new ImplicitsPlus {}
  override val simple = new SimpleQLPlus {}
  override val pgjson = "jsonb"
  override val api = MyAPI

  trait ImplicitsPlus extends Implicits
      with ArrayImplicits
      with DateTimeImplicits
      with RangeImplicits
      with HStoreImplicits
      with SearchImplicits
      with SprayJsonPlainImplicits
      with SparyJsonImplicits // FIXME name "Spary" -> "Spray" in 0.15.0-M1 release
      with PostGISImplicits {
    implicit val intListTypeMapper = new SimpleArrayJdbcType[Int]("int4").to(_.toList)
    implicit val strListTypeMapper = new SimpleArrayJdbcType[String]("text").to(_.toList)
  }

  trait SimpleQLPlus extends SimpleQL
    with ImplicitsPlus
    with SearchAssistants

  object MyAPI extends API with ArrayImplicits
      with DateTimeImplicits
      with RangeImplicits
      with HStoreImplicits
      with SearchImplicits
      with SprayJsonPlainImplicits
      with SparyJsonImplicits // FIXME name "Spary" -> "Spray" in 0.15.0-M1 release
      with SearchAssistants {
    implicit val intListTypeMapper = new SimpleArrayJdbcType[Int]("int4").to(_.toList)
    implicit val strListTypeMapper = new SimpleArrayJdbcType[String]("text").to(_.toList)
  }

}

object SlickPostgresDriver extends SlickPostgresDriver