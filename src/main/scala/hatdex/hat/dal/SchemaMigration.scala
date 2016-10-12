/*
 * Copyright (C) 2016 Andrius Aucinas <andrius.aucinas@hatdex.org>
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
 */

package hatdex.hat.dal

import java.sql.Connection

import akka.actor.ActorSystem
import akka.event.Logging
import hatdex.hat.api.DatabaseInfo
import hatdex.hat.api.actors.IoExecutionContext
import liquibase.{Contexts, Liquibase}
import liquibase.resource.FileSystemResourceAccessor
import liquibase.database.DatabaseFactory
import liquibase.database.jvm.JdbcConnection
import liquibase.resource.ClassLoaderResourceAccessor
import slick.jdbc.JdbcDataSource
import slick.util.Logging

import collection.JavaConverters._
import scala.concurrent.{Future, blocking}
import scala.util.Try

/**
 * Runs Liquibase based database schema and data migrations. This is the only place for all related
 * modules to run updates from.
 *
 * Liquibase finds its files on the classpath and applies them to DB. If migration fails
 * this class will throw an exception and by default your application should not continue to run.
 *
 * It does not matter which module runs this migration first.
 */
class SchemaMigration(system: ActorSystem) {
  import IoExecutionContext.ioThreadPool
  val logger = Logging.getLogger(system, "SchemaMigration")

  val masterChangeLogFile = "13_liveEvolutions.sql"

  def createLiquibase(dbConnection: Connection, diffFilePath: String): Future[Liquibase] = {
    val classLoader = getClass.getClassLoader
    val resourceAccessor = new ClassLoaderResourceAccessor(classLoader)
    Future {
      blocking {
        val database = DatabaseFactory.getInstance()
          .findCorrectDatabaseImplementation(new JdbcConnection(dbConnection))
        database.setDefaultSchemaName("hat")
        database.setLiquibaseSchemaName("public")
        new Liquibase(masterChangeLogFile, resourceAccessor, database)
      }
    }
  }

  def updateDb(db: JdbcDataSource, diffFilePath: String): Future[Boolean] = {
    val dbConnection = connProvider.createConnection()
    logger.info(s"Liquibase running evolutions $diffFilePath on db: [${dbConnection.getMetaData.getURL}]")
    val eventualLiquibase = createLiquibase(dbConnection, diffFilePath)

    eventualLiquibase map { liquibase =>
      val changesetStatuses = liquibase.getChangeSetStatuses(new Contexts("structuresonly, data")).asScala
      logger.info(s"existing changesets: \n${changesetStatuses.map(cs => cs.getChangeSet.toString+" - "+cs.getWillRun+"\n")}")
      Try(
        blocking {
          liquibase.update("structuresonly, data")
        }).map { _ =>
          blocking {
            logger.info(s"Schema evolutions completed, releasing locks")
            liquibase.forceReleaseLocks()
            dbConnection.rollback()
            dbConnection.close()
            true
          }
        }
        .recover {
          case e =>
            logger.error(s"Error: ${e.getMessage}", e)
            blocking {
              liquibase.forceReleaseLocks()
              dbConnection.rollback()
              dbConnection.close()
            }
            throw e
        } get
    } recover {
      case e =>
        logger.error(s"Error instantiating Liquibase: ${e.getMessage}, ${e}, ${e.getStackTrace.mkString("\n")}", e)
        throw e
    }
  }

  def connProvider = DatabaseInfo.db.source

  /**
   * Invoke this method to apply all DB migrations.
   */
  def run(): Future[Boolean] = {
    val db = connProvider
    updateDb(db, masterChangeLogFile)
  }

}
