/*
 * Copyright (C) 2019 HAT Data Exchange Ltd
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
 * Written by Marios Tsekis <marios.tsekis@hatdex.org>
 * 2 / 2019
 */
package org.hatdex.hat.she.service

import java.util.UUID

import org.hatdex.hat.api.models.{ EndpointData, EndpointQuery, PropertyQuery }
import org.hatdex.hat.api.service.richData.RichDataService
import org.hatdex.hat.resourceManagement.HatServer
import play.api.Logger
import play.api.libs.json._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait StaticDataEndpointMapper extends JodaWrites with JodaReads {
  protected lazy val logger: Logger = Logger(this.getClass)

  def dataQuery(): PropertyQuery
  def mapDataRecord(recordId: UUID, content: JsValue): Option[Map[String, JsValue]]

  final def staticDataRecords()(
    implicit
    hatServer: HatServer, richDataService: RichDataService): Future[Option[Map[String, JsValue]]] = {

    val query = dataQuery()
    val eventualDataSource: Future[Seq[EndpointData]] = richDataService.propertyData(query.endpoints, query.orderBy,
      orderingDescending = query.ordering.contains("descending"), skip = 0, limit = query.limit, createdAfter = None)(hatServer.db)

    eventualDataSource
      .map { dataSource =>

        dataSource.headOption.flatMap(item => mapDataRecord(item.recordId.get, item.data))
      }
  }
}

class FacebookProfileStaticDataMapper extends StaticDataEndpointMapper {
  def dataQuery(): PropertyQuery = {
    PropertyQuery(
      List(
        EndpointQuery("facebook/profile", None, None, None)), Some("hat_updated_time"), Some("descending"), Some(1))
  }

  def mapDataRecord(recordId: UUID, content: JsValue): Option[Map[String, JsValue]] = {

    val test = content.validate[Map[String, JsValue]]
    test match {
      case JsSuccess(value, _) => Some(value.filterKeys(key => key != "friends"))
      case e: JsError =>
        logger.error(s" $e")
        None
    }
  }
}