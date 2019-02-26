///*
// * Copyright (C) 2019 HAT Data Exchange Ltd
// * SPDX-License-Identifier: AGPL-3.0
// *
// * This file is part of the Hub of All Things project (HAT).
// *
// * HAT is free software: you can redistribute it and/or modify
// * it under the terms of the GNU Affero General Public License
// * as published by the Free Software Foundation, version 3 of
// * the License.
// *
// * HAT is distributed in the hope that it will be useful, but
// * WITHOUT ANY WARRANTY; without even the implied warranty of
// * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See
// * the GNU Affero General Public License for more details.
// *
// * You should have received a copy of the GNU Affero General
// * Public License along with this program. If not, see
// * <http://www.gnu.org/licenses/>.
// *
// * Written by Marios Tsekis <marios.tsekis@hatdex.org>
// * 2 / 2019
// */
//package org.hatdex.hat.she.service
//
//import javax.inject.Inject
//import org.hatdex.hat.api.service.richData.RichDataService
//import org.hatdex.hat.resourceManagement.HatServer
//import play.api.Logger
//import play.api.libs.json.JsValue
//
//import scala.concurrent.{ ExecutionContext, Future }
//
//class StaticDataGeneratorService @Inject() ()(
//    implicit
//    richDataService: RichDataService,
//    val ec: ExecutionContext) {
//
//  private val logger = Logger(this.getClass)
//
//  private val staticDataMappers: Seq[(String, StaticDataEndpointMapper)] = Seq(
//    "facebook/profile" -> new FacebookProfileStaticDataMapper())
//
//  def getStaticData(endpoint: String)(implicit hatServer: HatServer): Future[Option[Map[String, JsValue]]] = {
//
//    val mappers = staticDataMappers.find(_._1.startsWith(endpoint))
//
//    logger.debug(s"Fetching feed data for ${mappers.map(_._1)}")
//
//    mappers match {
//      case Some((_, mapper)) => mapper.staticDataRecords()
//      case None              => Future.successful(None)
//    }
//  }
//}
