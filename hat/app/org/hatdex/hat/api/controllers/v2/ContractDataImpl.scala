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
 * Written by Tyler Weir <tyler.weir@dataswift.io>
 * 1 / 2021
 */

package org.hatdex.hat.api.controllers.v2

import com.mohiva.play.silhouette.api.Silhouette
import io.dataswift.models.hat.EndpointData
import org.hatdex.hat.api.controllers.common._
import org.hatdex.hat.api.service.richData.RichDataService
import org.hatdex.hat.authentication.{ HatApiAuthEnvironment, HatApiController }
import org.hatdex.hat.utils.HatBodyParsers
import play.api.Logging
import play.api.libs.json.JsValue
import play.api.mvc._

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class ContractDataImpl @Inject() (
    components: ControllerComponents,
    parsers: HatBodyParsers,
    silhouette: Silhouette[HatApiAuthEnvironment],
    dataService: RichDataService,
    contractDataOperations: ContractDataOperations,
    contractAction: ContractAction
  )(implicit ec: ExecutionContext)
    extends HatApiController(components, silhouette)
    with ContractData
    with Logging {

  import io.dataswift.models.hat.json.RichDataJsonFormats._

  private val defaultRecordLimit = 1000

  def readContractData(
      namespace: String,
      endpoint: String,
      orderBy: Option[String],
      ordering: Option[String],
      skip: Option[Int],
      take: Option[Int]): Action[AnyContent] =
    contractAction.doWithToken(Some(namespace), permissions = Read) { (_, hatServer, _) =>
      contractDataOperations.makeData(namespace, endpoint, orderBy, ordering, skip, take)(hatServer.db)
    }

  def createContractData(
      namespace: String,
      endpoint: String,
      skipErrors: Option[Boolean]): Action[JsValue] =
    contractAction.doWithToken(parsers.json[JsValue], Some(namespace), permissions = Write) {
      (createRequest, user, hatServer, _) =>
        contractDataOperations.handleCreateContractData(user, Some(createRequest), namespace, endpoint, skipErrors)(
          hatServer
        )
    }

  def updateContractData(namespace: String): Action[Seq[EndpointData]] =
    contractAction.doWithToken(parsers.json[Seq[EndpointData]], Some(namespace), permissions = Write) {
      (updateRequest, user, hatServer, _) =>
        contractDataOperations.handleUpdateContractData(user, updateRequest, namespace)(hatServer)
    }
}
