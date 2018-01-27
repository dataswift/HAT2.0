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

package org.hatdex.hat.resourceManagement

import javax.inject.{ Inject, Singleton }

import com.typesafe.config.ConfigFactory
import org.hatdex.hat.dal.SchemaMigration
import org.hatdex.libs.dal.HATPostgresProfile.api.Database
import play.api.cache.AsyncCacheApi
import play.api.libs.ws.WSClient
import play.api.{ Configuration, Logger }

import scala.concurrent.{ ExecutionContext, Future }
import scala.collection.JavaConverters._
import scala.concurrent.duration.Duration

trait HatDatabaseProvider {
  val schemaMigration: SchemaMigration

  def database(hat: String)(implicit ec: ExecutionContext): Future[Database]

  def shutdown(db: Database): Future[Unit] = {
    // Execution context for the future is defined by specifying the executor during initialisation

    db.shutdown
  }

  def update(db: Database): Future[Unit] = {
    schemaMigration.run()(db)
  }
}

@Singleton
class HatDatabaseProviderConfig @Inject() (configuration: Configuration, val schemaMigration: SchemaMigration) extends HatDatabaseProvider {
  def database(hat: String)(implicit ec: ExecutionContext): Future[Database] = {
    Future {
      Database.forConfig(s"hat.${hat.replace(':', '.')}.database", configuration.underlying)
    } recoverWith {
      case e =>
        Future.failed(new HatServerDiscoveryException(s"Database configuration for $hat incorrect or unavailable", e))
    }
  }
}

@Singleton
class HatDatabaseProviderMilliner @Inject() (
    val configuration: Configuration,
    val cache: AsyncCacheApi,
    val ws: WSClient,
    val schemaMigration: SchemaMigration) extends HatDatabaseProvider with MillinerHatSignup {
  val logger = Logger(this.getClass)

  def database(hat: String)(implicit ec: ExecutionContext): Future[Database] = {
    getHatSignup(hat) map { signup =>
      val database = signup.database.get
      val config = Map(
        "dataSourceClassName" -> "org.postgresql.ds.PGSimpleDataSource",
        "properties" -> Map(
          "user" -> database.name,
          "password" -> database.password,
          "databaseName" -> database.name,
          "portNumber" -> signup.databaseServer.get.port,
          "serverName" -> signup.databaseServer.get.host),
        "numThreads" -> configuration.get[Int]("resourceManagement.hatDBThreads"),
        "idleTimeout" -> configuration.get[Duration]("resourceManagement.hatDBIdleTimeout").toMillis).asJava

      val dbConfig = ConfigFactory.parseMap(config)
      //      val databaseUrl = s"jdbc:postgresql://${signup.databaseServer.get.host}:${signup.databaseServer.get.port}/${signup.database.get.name}"
      //      val executor = AsyncExecutor(hat, numThreads = 3, queueSize = 1000)
      //      Database.forURL(databaseUrl, signup.database.get.name, signup.database.get.password, driver = "org.postgresql.Driver" /*, executor = slickAsyncExecutor*/ )
      Database.forConfig("", dbConfig)
    }
  }
}

