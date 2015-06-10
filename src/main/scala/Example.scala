import dal.Tables
import Tables._
import Tables.profile.simple._
import slick.jdbc.meta.MTable
import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import autodal.SlickPostgresDriver.simple._

object Example extends App {
  val db = Database.forConfig("devdb")
  implicit val session: Session = db.createSession()

  runQueries

  def runQueries = {
//    val q = DataFieldtofieldcrossref
//    val q2 = DataFieldtofieldcrossref.join(DataField).on(_.field1 === _.id)
//    println( q.run.mkString("\n") )
//    println( q2.run.mkString("\n") )
  }

}