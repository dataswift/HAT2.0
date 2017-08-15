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
 * 8 / 2017
 */

package org.hatdex.hat.api.controllers

import javax.inject.Inject

import akka.stream.Materializer
import akka.stream.scaladsl.{Flow, Keep, RunnableGraph, Sink}
import com.mohiva.play.silhouette.api.Silhouette
import com.mohiva.play.silhouette.api.util.Clock
import org.hatdex.hat.api.json.HatJsonFormats
import org.hatdex.hat.api.models._
import org.hatdex.hat.api.service.DataService
import org.hatdex.hat.api.service.monitoring.HatDataEventDispatcher
import org.hatdex.hat.api.service.richData._
import org.hatdex.hat.authentication.{HatApiAuthEnvironment, HatApiController, WithRole}
import org.hatdex.hat.resourceManagement._
import org.hatdex.hat.utils.HatBodyParsers
import org.joda.time.LocalDateTime
import play.api.i18n.MessagesApi
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.json._
import play.api.mvc._
import play.api.{Configuration, Logger}

import scala.concurrent.Future

class DataMigration @Inject() (
    val messagesApi: MessagesApi,
    configuration: Configuration,
    parsers: HatBodyParsers,
    silhouette: Silhouette[HatApiAuthEnvironment],
    clock: Clock,
    hatServerProvider: HatServerProvider,
    dataEventDispatcher: HatDataEventDispatcher,
    dataService: RichDataService,
    oldDataService: DataService,
    implicit val materializer: Materializer) extends HatApiController(silhouette, clock, hatServerProvider, configuration) with RichDataJsonFormats {

  private val logger = Logger(this.getClass)

  def migrateData(fromSource: String, fromTableName: String, toNamespace: String, toEndpoint: String): Action[AnyContent] =
    SecuredAction(WithRole(Owner())).async { implicit request =>
      val toDataEndpoint = s"$toNamespace/$toEndpoint"
      val parallelMigrations = 10

      val eventualCount: Future[Long] = oldDataService.findTable(fromTableName, fromSource).map(_.head.id.get) flatMap { tableId =>

        val migratedCountSource = oldDataService.getTableValuesStreaming(tableId) map { record =>
          HatJsonFormats.flattenRecordValues(record).transform(
            (__ \ 'data).json.pick(
              __.json.update(
                (__ \ 'lastUpdated)
                  .json
                  .put(Json.toJson(
                    record.lastUpdated.getOrElse(LocalDateTime.now()))))))
        } via {
          Flow[JsResult[JsObject]].filter(_.isSuccess) // Filter out unsuccessfully extracted data objects
        } via {
          Flow[JsResult[JsObject]].map(_.get) // Unwrap Json Objects
        } via Flow[JsObject].mapAsync(parallelMigrations) { oldJson =>
          dataService.saveData(request.identity.userId, Seq(EndpointData(toDataEndpoint, None, oldJson, None)))
            // .andThen(dataEventDispatcher.dispatchEventDataCreated(s"saved data for $toDataEndpoint"))
            .map(_ => 1L)
            .recover {
              case e =>
                logger.debug(s"Error while migrating data record: ${e.getMessage}")
                0L
            }
        }

        val sink = Sink.fold[Long, Long](0)(_ + _)
        val runnable: RunnableGraph[Future[Long]] = migratedCountSource.toMat(sink)(Keep.right)
        runnable.run()
      }

      eventualCount map { count =>
        Ok(Json.toJson(SuccessResponse(s"Migrated $count records")))
      }

    }

}

