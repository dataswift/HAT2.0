package hatdex.hat.dal

import com.github.tminglei.slickpg._

trait SlickPostgresDriver extends ExPostgresDriver
with PgArraySupport
with PgDateSupportJoda
with PgRangeSupport
with PgHStoreSupport
with PgSearchSupport
with PgPostGISSupport {
  ///
  override lazy val Implicit = new ImplicitsPlus {}
  override val simple = new SimpleQLPlus {}

  //////
  trait ImplicitsPlus extends Implicits
  with ArrayImplicits
  with DateTimeImplicits
  with RangeImplicits
  with HStoreImplicits
  with SearchImplicits
  with PostGISImplicits {
    implicit val intListTypeMapper = new SimpleArrayJdbcType[Int]("integer").to(_.toList)
  }

  trait SimpleQLPlus extends SimpleQL
  with ImplicitsPlus
  with SearchAssistants
}

/*
trait SlickPostgresDriver extends ExPostgresDriver
with PgArraySupport
with PgDateSupportJoda
with PgRangeSupport
with PgHStoreSupport
with PgSearchSupport
with PgPostGISSupport {
  ///
//  override lazy val Implicit = new ImplicitsPlus {}
//  override val simple = new SimpleQLPlus {}
//
//  //////
//  trait ImplicitsPlus extends Implicits
//  with ArrayImplicits
//  with DateTimeImplicits
//  with RangeImplicits
//  with HStoreImplicits
//  with SearchImplicits
//  with PostGISImplicits
//
//  trait SimpleQLPlus extends SimpleQL
//  with ImplicitsPlus
//  with SearchAssistants

  override val api = ExtendedApi

  object ExtendedApi extends API with ArrayImplicits
    with DateTimeImplicits
    with RangeImplicits
    with HStoreImplicits
    with SearchImplicits
    with SearchAssistants {
    implicit val strListTypeMapper = new SimpleArrayJdbcType[String]("text").to(_.toList)
  }
}

 */

object SlickPostgresDriver extends SlickPostgresDriver