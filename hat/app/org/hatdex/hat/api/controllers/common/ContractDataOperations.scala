package org.hatdex.hat.api.controllers.common

import org.hatdex.libs.dal.HATPostgresProfile
import org.hatdex.hat.authentication.models.HatUser
import org.hatdex.hat.resourceManagement.HatServer
import play.api.mvc.Result
import play.api.libs.json.JsValue
import io.dataswift.models.hat.EndpointData

import scala.concurrent.Future

trait ContractDataOperations {
  def makeData(
      namespace: String,
      endpoint: String,
      orderBy: Option[String],
      ordering: Option[String],
      skip: Option[Int],
      take: Option[Int]
    )(implicit db: HATPostgresProfile.api.Database): Future[Result]

  def handleCreateContractData(
      hatUser: HatUser,
      contractDataCreate: Option[JsValue],
      namespace: String,
      endpoint: String,
      skipErrors: Option[Boolean]
    )(implicit hatServer: HatServer): Future[Result]

  def handleUpdateContractData(
      hatUser: HatUser,
      contractDataUpdate: Seq[EndpointData],
      namespace: String
    )(implicit hatServer: HatServer): Future[Result]
}
