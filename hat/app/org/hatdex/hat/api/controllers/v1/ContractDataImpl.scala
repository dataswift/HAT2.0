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

package org.hatdex.hat.api.controllers.v1

import com.mohiva.play.silhouette.api.Silhouette
import io.dataswift.models.hat.{ EndpointData, EndpointQuery, ErrorMessage }
import org.hatdex.hat.api.service.richData.{ RichDataMissingException, RichDataService }
import org.hatdex.hat.authentication.models.HatUser
import org.hatdex.hat.authentication.{ HatApiAuthEnvironment, HatApiController }
import org.hatdex.hat.resourceManagement.HatServer
import org.hatdex.hat.utils.HatBodyParsers
import org.hatdex.libs.dal.HATPostgresProfile
import pdi.jwt.JwtClaim
import play.api.Logging
import play.api.libs.json.{ JsArray, JsValue, Json }
import play.api.mvc._
import org.hatdex.hat.api.controllers.common._

import java.util.UUID
import javax.inject.Inject
import scala.concurrent.{ ExecutionContext, Future }

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

  override def readContractData(
      namespace: String,
      endpoint: String,
      orderBy: Option[String],
      ordering: Option[String],
      skip: Option[Int],
      take: Option[Int]): Action[ContractDataReadRequest] =
    contractAction.doWithContract(parsers.json[ContractDataReadRequest], Some(namespace), isWriteAction = false) {
      (_, _, hatServer, _) =>
        contractDataOperations.makeData(namespace, endpoint, orderBy, ordering, skip, take)(hatServer.db)
    }

  override def createContractData(
      namespace: String,
      endpoint: String,
      skipErrors: Option[Boolean]): Action[ContractDataCreateRequest] =
    contractAction.doWithContract(parsers.json[ContractDataCreateRequest], Some(namespace), isWriteAction = true) {
      (createRequest, user, hatServer, _) =>
        contractDataOperations.handleCreateContractData(user, createRequest.body, namespace, endpoint, skipErrors)(
          hatServer
        )
    }

  override def updateContractData(namespace: String): Action[ContractDataUpdateRequest] =
    contractAction.doWithContract(parsers.json[ContractDataUpdateRequest], Some(namespace), isWriteAction = true) {
      (updateRequest, user, hatServer, _) =>
        contractDataOperations.handleUpdateContractData(user, updateRequest.body, namespace)(hatServer)
    }
}
