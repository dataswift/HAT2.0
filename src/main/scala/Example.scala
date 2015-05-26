import demo.Config
import dal.Tables
import Tables._
import Tables.profile.simple._

object Example extends App {
  val db = Database.forURL(Config.url, driver=Config.jdbcDriver)

  // Using generated code. Our Build.sbt makes sure they are generated before compilation.

  val q = DataFieldtofieldcrossref
  val q2 = DataFieldtofieldcrossref.join(DataField).on(_.field1 === _.id)

  db.withSession { implicit session =>
    println( q.run.mkString("\n") )

    println( q2.run.mkString("\n") )
  }
}