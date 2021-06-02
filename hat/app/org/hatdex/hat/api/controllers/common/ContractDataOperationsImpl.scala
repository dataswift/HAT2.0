package org.hatdex.hat.api.controllers.common

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

class ContractDataOperationsImpl @Inject() (
    components: ControllerComponents,
    parsers: HatBodyParsers,
    silhouette: Silhouette[HatApiAuthEnvironment],
    dataService: RichDataService
  )(implicit ec: ExecutionContext)
    extends HatApiController(components, silhouette)
    with ContractDataOperations
    with Logging {

  import io.dataswift.models.hat.json.RichDataJsonFormats._
  private val defaultRecordLimit = 1000

  override def handleCreateContractData(
      hatUser: HatUser,
      contractDataCreate: Option[JsValue],
      namespace: String,
      endpoint: String,
      skipErrors: Option[Boolean]
    )(implicit hatServer: HatServer): Future[Result] =
    contractDataCreate match {
      // Missing Json Body, do nothing, return error
      case None =>
        logger.error(s"saveContractData included no json body - ns:${namespace} - endpoint:${endpoint}")
        Future.successful(NotFound)
      // There is a Json body, process either an JsArray or a JsValue
      case Some(jsBody) =>
        val dataEndpoint = s"$namespace/$endpoint"
        jsBody match {
          case array: JsArray =>
            logger.info(s"saveContractData.body is JsArray: ${contractDataCreate}")
            handleJsArray(
              hatUser.userId,
              array,
              dataEndpoint,
              skipErrors
            )
          case value: JsValue =>
            logger.info(s"saveContractData.body is JsValue: ${contractDataCreate}")
            handleJsValue(
              hatUser.userId,
              value,
              dataEndpoint
            )
        }
    }

  // -- Update
  override def handleUpdateContractData(
      hatUser: HatUser,
      contractDataUpdate: Seq[EndpointData],
      namespace: String
    )(implicit hatServer: HatServer): Future[Result] =
    contractDataUpdate.length match {
      // Missing Json Body, do nothing, return error
      case 0 =>
        logger.error(s"updateContractData included no json body - ns:${namespace}")
        Future.successful(NotFound)
      // There is a Json body, process either an JsArray or a JsValue
      case _ =>
        val dataSeq = contractDataUpdate.map(item => item.copy(endpoint = s"${namespace}/${item.endpoint}"))
        dataService.updateRecords(
          hatUser.userId,
          dataSeq
        ) map { saved =>
          Created(Json.toJson(saved))
        } recover {
          case RichDataMissingException(message, _) =>
            BadRequest(Json.toJson(dataUpdateMissing(message)))
        }
    }

  override def makeData(
      namespace: String,
      endpoint: String,
      orderBy: Option[String],
      ordering: Option[String],
      skip: Option[Int],
      take: Option[Int]
    )(implicit db: HATPostgresProfile.api.Database): Future[Result] = {
    val dataEndpoint = s"$namespace/$endpoint"
    val query =
      Seq(EndpointQuery(dataEndpoint, None, None, None))
    val data = dataService.propertyData(
      query,
      orderBy,
      ordering.contains("descending"),
      skip.getOrElse(0),
      take.orElse(Some(defaultRecordLimit))
    )
    data.map(d => Ok(Json.toJson(d)))
  }

  private def handleJsArray(
      userId: UUID,
      array: JsArray,
      dataEndpoint: String,
      skipErrors: Option[Boolean]
    )(implicit hatServer: HatServer): Future[Result] = {
    val values =
      array.value.map(EndpointData(dataEndpoint, None, None, None, _, None))
    logger.info(s"handleJsArray: Values: ${values}")
    dataService
      .saveData(userId, values.toSeq, skipErrors.getOrElse(false))
      .map(saved => Created(Json.toJson(saved)))
  }

  private def handleJsValue(
      userId: UUID,
      value: JsValue,
      dataEndpoint: String
    )(implicit hatServer: HatServer): Future[Result] = {
    val values = Seq(EndpointData(dataEndpoint, None, None, None, value, None))
    logger.info(s"handleJsArray: Values: ${values}")
    dataService
      .saveData(userId, values)
      .map(saved => Created(Json.toJson(saved.head)))
  }

  private def dataUpdateMissing(message: String): ErrorMessage =
    ErrorMessage("Data Missing", s"Could not update records: $message")
}
