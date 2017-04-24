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
 * 4 / 2017
 */

package org.hatdex.hat.api.controllers

import java.util.UUID
import javax.inject.Inject

import com.mohiva.play.silhouette.api.Silhouette
import com.mohiva.play.silhouette.api.util.Clock
import org.hatdex.hat.api.json.HatJsonFormats
import org.hatdex.hat.api.models.{ ApiHatFile, ErrorMessage, HatFileStatus }
import org.hatdex.hat.api.service._
import org.hatdex.hat.authentication.models.HatUser
import org.hatdex.hat.authentication.{ HatApiAuthEnvironment, HatApiController, WithRole }
import org.hatdex.hat.resourceManagement._
import org.hatdex.hat.utils.HatBodyParsers
import org.joda.time.DateTime
import play.api.i18n.MessagesApi
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.json.{ Format, Json }
import play.api.mvc._
import play.api.{ Configuration, Logger }

import scala.concurrent.Future

class RichData @Inject() (
    val messagesApi: MessagesApi,
    configuration: Configuration,
    parsers: HatBodyParsers,
    silhouette: Silhouette[HatApiAuthEnvironment],
    hatServerProvider: HatServerProvider,
    clock: Clock,
    hatDatabaseProvider: HatDatabaseProvider,
    dataService: RichDataService,
    usersService: UsersService) extends HatApiController(silhouette, clock, hatServerProvider, configuration) with RichDataJsonFormats {

  val logger = Logger(this.getClass)

  def getEndpointData(endpoint: String, recordId: Option[UUID], orderBy: Option[String], take: Option[Int]): Action[AnyContent] =
    SecuredAction(WithRole("dataCredit", "owner")).async { implicit request =>

      val data = dataService.propertyData(List(EndpointQuery(endpoint, None, None, None)), orderBy, take.getOrElse(1000))

      data.map(d => Ok(Json.toJson(d)))
    }

  //  def startUpload: Action[ApiHatFile] = SecuredAction(WithRole("dataCredit", "owner")).async(parsers.json[ApiHatFile]) { implicit request =>
  //
  //  }

}

