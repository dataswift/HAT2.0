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
 * 11 / 2017
 */

package org.hatdex.hat.she.service

import java.security.MessageDigest
import javax.inject.Inject

import akka.Done
import org.hatdex.hat.api.models.{ EndpointData, Owner }
import org.hatdex.hat.api.service.UsersService
import org.hatdex.hat.api.service.richData.RichDataService
import org.hatdex.hat.she.models.{ FunctionConfiguration, FunctionExecutable, Request, Response }
import org.hatdex.libs.dal.HATPostgresProfile.api._
import org.joda.time.DateTime
import org.hatdex.hat.dal.Tables._
import play.api.Logger
import play.api.libs.json.Json

import scala.concurrent.{ ExecutionContext, Future }
import scala.util.Success

class FunctionService @Inject() (
    functionRegistry: FunctionExecutableRegistry,
    dataService: RichDataService,
    usersService: UsersService)(
    implicit
    ec: ExecutionContext) {

  private val logger = Logger(this.getClass)

  protected def mergeRegisteredSaved(registeredFunctions: Seq[FunctionConfiguration], savedFunctions: Seq[FunctionConfiguration]): Seq[FunctionConfiguration] = {
    val registered: Map[String, FunctionConfiguration] = registeredFunctions.map(r => r.name -> r).toMap
    val saved: Map[String, FunctionConfiguration] = savedFunctions.map(r => r.name -> r).toMap

    val functions = for ((k, v) <- saved ++ registered)
      yield k -> (if ((saved contains k) && (registered contains k)) saved(k).update(v) else v)

    functions.values.toSeq
  }

  def all(active: Boolean)(implicit db: Database): Future[Seq[FunctionConfiguration]] = {
    for {
      registeredFunctions <- Future.successful(functionRegistry.getSeq[FunctionExecutable].map(_.configuration))
      savedFunctions <- saved()
    } yield {
      logger.debug(
        s"""Got a list of registered and saved functions:
           | $registeredFunctions
           | $savedFunctions""".stripMargin)
      mergeRegisteredSaved(registeredFunctions, savedFunctions)
        .filter(f => !active || f.enabled && f.available) // only return enabled ones if filtering is on
    }
  }

  def get(name: String)(implicit db: Database): Future[Option[FunctionConfiguration]] = {
    val query = for {
      function <- SheFunction.filter(_.name === name)
      bundle <- function.dataBundlesFk
    } yield (function, bundle)

    for {
      r <- Future.successful(functionRegistry.getSeq[FunctionExecutable].map(_.configuration).filter(_.name == name))
      f <- db.run(query.take(1).result)
        .map(_.map(f => FunctionConfiguration(f._1, f._2).copy(available = r.exists(_.name == f._1.name))))
    } yield {
      mergeRegisteredSaved(r, f)
        .headOption
    }
  }

  def saved()(implicit db: Database): Future[Seq[FunctionConfiguration]] = {
    val query = for {
      function <- SheFunction
      bundle <- function.dataBundlesFk
    } yield (function, bundle)

    for {
      r <- Future.successful(functionRegistry.getSeq[FunctionExecutable].map(_.configuration))
        .andThen {
          case Success(s) => logger.debug(s"Got registered functions: $s")
        }
      f <- db.run(query.result)
        .map(_.map(f => FunctionConfiguration(f._1, f._2).copy(available = r.exists(_.name == f._1.name))))
        .andThen {
          case Success(s) => logger.debug(s"Got saved functions: $s")
        }
    } yield f
  }

  def save(configuration: FunctionConfiguration)(implicit db: Database): Future[FunctionConfiguration] = {
    logger.debug(s"Save function configuration $configuration")
    import org.hatdex.hat.api.json.RichDataJsonFormats.propertyQueryFormat
    import org.hatdex.hat.she.models.FunctionConfigurationJsonProtocol.triggerFormat
    val functionRow = SheFunctionRow(configuration.name, configuration.description, Json.toJson(configuration.trigger),
      configuration.enabled, configuration.dataBundle.name, configuration.lastExecution)
    val bundleRow = DataBundlesRow(configuration.dataBundle.name, Json.toJson(configuration.dataBundle.bundle))

    db.run(DBIO.seq(
      DataBundles.insertOrUpdate(bundleRow),
      SheFunction.insertOrUpdate(functionRow)).transactionally)
      .flatMap(_ => get(configuration.name))
      .map(_.get)
  }

  def run(configuration: FunctionConfiguration, startTime: Option[DateTime])(implicit db: Database): Future[Done] = {
    functionRegistry.get[FunctionExecutable](configuration.name)
      .map { function: FunctionExecutable =>
        val fromDate = startTime.orElse(Some(DateTime.now().minusMonths(6)))
        val untilDate = Some(DateTime.now().plusMonths(3))
        val executionTime = DateTime.now()
        for {
          data <- dataService.bundleData(function.bundleFilterByDate(fromDate, untilDate), createdAfter = configuration.lastExecution) // Get all bundle data from a specific date until now
          response <- function.execute(configuration, Request(data, linkRecords = true)) // Execute the function
            .map(removeDuplicateData) // Remove duplicate data in case some records mapped onto the same values when transformed
          owner <- usersService.getUserByRole(Owner()).map(_.head) // Fetch the owner user - functions run on their behalf
          _ <- dataService.saveDataGroups(owner.userId, response.map(r => (r.data.map(EndpointData(s"${r.namespace}/${r.endpoint}", None, _, None)), r.linkedRecords)), skipErrors = true)
          _ <- save(configuration.copy(lastExecution = Some(executionTime)))
        } yield Done
      } getOrElse {
        Future.failed(new RuntimeException("The requested function is not available"))
      }
  }

  //TODO: expensive operation!
  private def removeDuplicateData(response: Seq[Response]): Seq[Response] = {
    val md = MessageDigest.getInstance("SHA-256")
    response.map({ r ⇒
      val digest = md.digest(r.data.head.toString().getBytes)
      (BigInt(digest), r)
    })
      .sortBy(_._1)
      .foldRight(Seq[(BigInt, Response)]())({
        case (e, ls) if ls.isEmpty || ls.head._1 != e._1 ⇒ e +: ls
        case (_, ls)                                     ⇒ ls
      })
      .unzip._2 // Drop the digest
  }
}
