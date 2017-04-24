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

import java.sql.Connection
import javax.inject.Inject

import liquibase.database.DatabaseFactory
import liquibase.database.jvm.JdbcConnection
import liquibase.resource.ClassLoaderResourceAccessor
import liquibase.{ Contexts, LabelExpression, Liquibase }
import org.hatdex.hat.api.service.DalExecutionContext
import org.hatdex.hat.dal.SlickPostgresDriver.api.Database
import play.api.{ Configuration, Logger }

import scala.collection.JavaConverters._
import scala.concurrent.{ Future, blocking }
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
class SchemaMigration @Inject() (configuration: Configuration) extends DalExecutionContext {

  val logger = Logger(this.getClass)

  def run()(implicit db: Database): Future[Unit] = {
    configuration.getStringSeq("hat.schemaMigrations").map { migrations =>
      logger.info(s"Running database schema migrations on $migrations")
      run(migrations)
    } getOrElse {
      logger.warn("No evolutions configured")
      Future.successful(())
    }
  }

  /**
   * Invoke this method to apply all DB migrations.
   */
  def run(changeLogFiles: Seq[String])(implicit db: Database): Future[Unit] = {
    logger.info(s"Running schema migrations: ${changeLogFiles.mkString(", ")}")
    changeLogFiles.foldLeft(Future.successful(())) { (execution, evolution) => execution.flatMap { _ => updateDb(evolution) } }
  }

  def resetDatabase()(implicit db: Database): Future[Unit] = {
    val eventuallyEvolved = Future {
      Try {
        db.createSession().conn
      } map { dbConnection =>
        val liquibase = blocking {
          createLiquibase(dbConnection, "")
        }
        liquibase.getLog.setLogLevel("severe")
        blocking {
          Try(liquibase.dropAll())
            .recover {
              case e =>
                liquibase.forceReleaseLocks()
                logger.error(s"Error dropping all database information")
                throw e
            }
          liquibase.forceReleaseLocks()
        }
      } get
    }

    eventuallyEvolved onFailure {
      case e =>
        logger.error(s"Error updating database: ${e.getMessage}")
    }

    eventuallyEvolved
  }

  def rollback(changeLogFiles: Seq[String])(implicit db: Database): Future[Unit] = {
    logger.info(s"Rolling back schema migrations: ${changeLogFiles.mkString(", ")}")
    changeLogFiles.foldLeft(Future.successful(())) { (execution, evolution) => execution.flatMap { _ => updateDb(evolution) } }
  }

  private def updateDb(diffFilePath: String)(implicit db: Database): Future[Unit] = {
    val eventuallyEvolved = Future {
      Try {
        db.createSession().conn
      } map { dbConnection =>
        logger.info(s"Liquibase running evolutions $diffFilePath on db: [${dbConnection.getMetaData.getURL}]")
        val changesets = "structuresonly,data"
        val liquibase = blocking {
          createLiquibase(dbConnection, diffFilePath)
        }
        liquibase.getLog.setLogLevel("severe")
        blocking {
          listChangesets(liquibase, new Contexts(changesets))
          Try(liquibase.update(changesets))
            .recover {
              case e =>
                liquibase.forceReleaseLocks()
                logger.error(s"Error executing schema evolutions: ${e.getMessage}")
                throw e
            }
          liquibase.forceReleaseLocks()
        }
      } get
    }

    eventuallyEvolved onFailure {
      case e =>
        logger.error(s"Error updating database: ${e.getMessage}")
    }

    eventuallyEvolved
  }

  private def rollbackDb(diffFilePath: String)(implicit db: Database): Future[Unit] = {
    val eventuallyEvolved = Future {
      Try {
        db.createSession().conn
      } map { dbConnection =>
        logger.info(s"Liquibase rolling back evolutions $diffFilePath on db: [${dbConnection.getMetaData.getURL}]")
        val changesets = "structuresonly,data"
        val liquibase = blocking {
          createLiquibase(dbConnection, diffFilePath)
        }
        blocking {
          val contexts = new Contexts(changesets)
          val changesetsExecuted = liquibase.getChangeSetStatuses(contexts, new LabelExpression()).asScala.filterNot(_.getWillRun)
          Try(liquibase.rollback(changesetsExecuted.length, contexts, new LabelExpression()))
            .recover {
              case e =>
                liquibase.forceReleaseLocks()
                logger.error(s"Error executing schema evolutions: ${e.getMessage}")
                throw e
            }
          liquibase.forceReleaseLocks()
        }
      } get
    }

    eventuallyEvolved onFailure {
      case e =>
        logger.error(s"Error updating database: ${e.getMessage}")
    }

    eventuallyEvolved
  }

  private def listChangesets(liquibase: Liquibase, contexts: Contexts): Unit = {
    val changesetStatuses = liquibase.getChangeSetStatuses(contexts, new LabelExpression()).asScala

    logger.info("Existing changesets:")
    changesetStatuses.foreach { cs =>
      if (cs.getWillRun) {
        logger.info(s"${cs.getChangeSet.toString} will run")
      }
      else {
        logger.info(s"${cs.getChangeSet.toString} will not run - previously executed on ${cs.getDateLastExecuted}")
      }
    }
  }

  private def createLiquibase(dbConnection: Connection, diffFilePath: String): Liquibase = {
    val classLoader = configuration.getClass.getClassLoader
    val resourceAccessor = new ClassLoaderResourceAccessor(classLoader)

    val database = DatabaseFactory.getInstance()
      .findCorrectDatabaseImplementation(new JdbcConnection(dbConnection))
    database.setDefaultSchemaName("hat")
    database.setLiquibaseSchemaName("public")
    new Liquibase(diffFilePath, resourceAccessor, database)
  }
}
