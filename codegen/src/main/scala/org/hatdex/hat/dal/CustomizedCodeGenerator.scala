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

import slick.codegen.SourceCodeGenerator
import slick.driver.PostgresDriver
import slick.jdbc.meta.MTable

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import org.hatdex.hat.dal.SlickPostgresDriver.api._
import slick.profile.SqlProfile.ColumnOption

/**
 *  This customizes the Slick code generator.
 *  Uses joda time package mappings from SQL date/time fields.
 */
object CustomizedCodeGenerator {

  def main(args: Array[String]): Unit = {
    println(" [CODEGEN] Running Customized Generator")
    codegenFuture.onSuccess {
      case codegen =>
        println(s" [CODEGEN] success - write to file ${args(0)}, package ${args(1)}")
        val writefileFuture = codegen.writeToFile(
          "org.hatdex.hat.dal.SlickPostgresDriver",
          args(0),
          args(1),
          "Tables",
          "Tables.scala")
    }

    codegenFuture.onFailure {
      case _ =>
        println("Customized Generator failed!")
    }

    Await.result(codegenFuture, 5 minutes)
  }

  val db = Database.forConfig("devdb")

  val excluded = Seq("databasechangelog", "databasechangeloglock")

  val tablesAndViews = MTable.getTables(None, None, None, Some(Seq("TABLE", "VIEW"))) //TABLE, and VIEW represent metadata, i.e. get database objects which are tables and views
    .map(_.filterNot(t => excluded contains t.name.name))

  val modelFuture = db.run {
    tablesAndViews.flatMap(SlickPostgresDriver.createModelBuilder(_, false).buildModel)
  }

  val codegenFuture = modelFuture.map(model => new slick.codegen.SourceCodeGenerator(model) {
    override def Table = new Table(_) { table =>
      override def Column = new Column(_) { column =>
        // customize db type -> scala type mapping, pls adjust it according to your environment
        override def rawType: String = model.tpe match {
          case "java.sql.Date"      => "org.joda.time.LocalDate"
          case "java.sql.Time"      => "org.joda.time.LocalTime"
          case "java.sql.Timestamp" => "org.joda.time.LocalDateTime"
          // currently, all types that's not built-in support were mapped to `String`
          case "String" => model.options.find(_.isInstanceOf[ColumnOption.SqlType]).map(_.asInstanceOf[ColumnOption.SqlType].typeName).map({
            case "hstore"   => "Map[String, String]"
            case "geometry" => "com.vividsolutions.jts.geom.Geometry"
            case "int8[]"   => "List[Long]"
            case "int4[]"   => "List[Int]"
            case "text[]"   => "List[String]"
            case "_int4"    => "List[Int]"
            case "_text"    => "List[String]"
            case "jsonb"    => "play.api.libs.json.JsValue"
            case _          => "String"
          }).getOrElse("String")
          case _ => super.rawType
        }
      }
    }

    def fullCode = code

    // ensure to use our customized postgres driver at `import profile.simple._`
    override def packageCode(profile: String, pkg: String, container: String, parentType: Option[String]): String = {
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
  import profile.api._
  ${indent(fullCode)}
}
      """.trim()
    }

  })
}