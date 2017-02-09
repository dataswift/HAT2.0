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

package org.hatdex.hat.modules

import java.util.UUID
import javax.inject.Inject

import com.typesafe.config.Config
import net.ceedubs.ficus.Ficus._
import net.ceedubs.ficus.readers.ArbitraryTypeReader._
import net.codingwell.scalaguice.ScalaModule
import org.hatdex.hat.api.service.{ DalExecutionContext, UsersService }
import org.hatdex.hat.authentication.models.HatUser
import org.hatdex.hat.dal.SchemaMigration
import org.hatdex.hat.dal.SlickPostgresDriver.backend.Database
import play.api.libs.concurrent.AkkaGuiceSupport
import play.api.{ Configuration, Logger }

import scala.concurrent.Future

class DevHatInitializationModule extends ScalaModule with AkkaGuiceSupport {

  /**
   * Configures the module.
   */
  def configure() = {
    bind[DevHatInitializer].asEagerSingleton()
  }
}

class DevHatInitializer @Inject() (
    configuration: Configuration,
    schemaMigration: SchemaMigration,
    usersService: UsersService) extends DalExecutionContext {
  val logger = Logger(this.getClass)

  val devHats = configuration.underlying.as[Seq[DevHatConfig]]("devhats")
  val devHatMigrations = configuration.getStringSeq("devhatMigrations").get

  devHats.map(initializeHat)

  def initializeHat(hat: DevHatConfig) = {
    implicit val database = Database.forConfig("", hat.database)

    val eventuallyMigrated = for {
      _ <- schemaMigration.run(devHatMigrations)
      _ <- setupCredentials(hat)
    } yield ()

    eventuallyMigrated map {
      case _ =>
        logger.info(s"Database successfully initialized for ${hat.owner}")
    } recover {
      case e =>
        logger.error(s"Database initialisation failed for ${hat.owner}: ${e.getMessage}", e)
    } map {
      case _ =>
        database.shutdown
    }
  }

  def setupCredentials(hat: DevHatConfig)(implicit database: Database): Future[Unit] = {
    val ownerId = UUID.fromString("694dd8ed-56ae-4910-abf1-6ec4887b4c42")
    val platformId = UUID.fromString("6507ae16-13d7-479b-8ebc-65c28fec1634")
    for {
      _ <- usersService.saveUser(HatUser(ownerId, hat.owner, Some(hat.ownerPasswordHash), hat.ownerName, "owner", enabled = true))
      _ <- usersService.saveUser(HatUser(platformId, hat.platform, Some(hat.platformPasswordHash), hat.platformName, "platform", enabled = true))
    } yield ()
  }
}

case class DevHatConfig(
  owner: String,
  ownerName: String,
  ownerPasswordHash: String,
  platform: String,
  platformName: String,
  platformPasswordHash: String,
  database: Config)
