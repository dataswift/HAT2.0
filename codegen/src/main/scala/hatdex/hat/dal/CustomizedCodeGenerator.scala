package hatdex.hat.dal

//import autodal.Config._
import slick.codegen.SourceCodeGenerator
import slick.driver.PostgresDriver

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import slick.driver.JdbcDriver.api._

//import SlickPostgresDriver.simple._

/**
 *  This customizes the Slick code generator.
 *  Uses joda time package mappings from SQL date/time fields.
 */
object CustomizedCodeGenerator{

  def main(args: Array[String]) : Unit = {
    println( " [CODEGEN] Running Customized Generator" )
    codegenFuture.onSuccess { case codegen =>
      println (s" [CODEGEN] success - write to file ${args(0)}, package ${args(1)}")
      val writefileFuture = codegen.writeToFile(
        "hatdex.hat.dal.SlickPostgresDriver",
        args(0),
        args(1),
        "Tables",
        "Tables.scala"
      )
    }

    codegenFuture.onFailure { case _ =>
      println( "Customized Generator failed!" )
    }

    Await.result(codegenFuture, 5 minutes)
  }

  val db = Database.forConfig("devdb")

  val excluded = Seq("databasechangelog", "databasechangeloglock")

  val modelAction = PostgresDriver.createModel( Some(PostgresDriver.defaultTables.map(_.filterNot(t => excluded contains t.name.name))) )
  val modelFuture = db.run(modelAction)

  val codegenFuture = modelFuture.map(model => new SourceCodeGenerator(model){
    override def Table = new Table(_) { table =>
      override def Column = new Column(_) { column =>
        // customize db type -> scala type mapping, pls adjust it according to your environment
        override def rawType = model.tpe match {
          case "java.sql.Date" => "org.joda.time.LocalDate"
          case "java.sql.Time" => "org.joda.time.LocalTime"
          case "java.sql.Timestamp" => "org.joda.time.LocalDateTime"
          // currently, all types that's not built-in support were mapped to `String`
          case "String" => "String"
          case _ => super.rawType
        }
      }
    }

    def fullCode = code

    // ensure to use our customized postgres driver at `import profile.simple._`
    override def packageCode(profile: String, pkg: String, container: String, parentType: Option[String]) : String = {
      s"""
package ${pkg}
// AUTO-GENERATED Slick data model
/** Stand-alone Slick data model for immediate use */
object ${container} extends {
  val profile = $profile
} with ${container}
/** Slick data model trait for extension, choice of backend or usage in the cake pattern. (Make sure to initialize this late.) */
trait ${container} {
  val profile: $profile
  import profile.simple._
  ${indent(fullCode)}
}
      """.trim()
    }

  })
}