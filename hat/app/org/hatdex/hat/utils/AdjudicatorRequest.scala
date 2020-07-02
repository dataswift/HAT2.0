package org.hatdex.hat.utils

import io.dataswift.adjudicator.Types.{ ContractId }
import play.api.libs.ws.{ WSClient, WSRequest }
import play.api.http.Status._
import play.api.libs.json.Json

import scala.concurrent.{ ExecutionContext, Future }

object AdjudicatorRequestTypes {

  // Public Key Request
  sealed trait PublicKeyRequestFailure
  object PublicKeyRequestFailure {

    final case class ServiceRespondedWithFailure(failureDescription: String)
      extends PublicKeyRequestFailure

    final case class InvalidPublicKeyFailure(failureDescription: String)
      extends PublicKeyRequestFailure

  }
  final case class PublicKeyReceived(publicKeyAsByteArray: Array[Byte]) extends AnyVal

  // Join Contract Request
  sealed trait JoinContractRequestFailure
  object JoinContractRequestFailure {

    final case class ServiceRespondedWithFailure(failureDescription: String)
      extends JoinContractRequestFailure

    final case class JoinContractFailure(failureDescription: String)
      extends JoinContractRequestFailure

  }
  // TODO: if we attempt to extend AnyVal here, the compiler complains
  final case class ContractJoined(contractId: ContractId)

  // Leave Contract Request
  sealed trait LeaveContractRequestFailure
  object LeaveContractRequestFailure {

    final case class ServiceRespondedWithFailure(failureDescription: String)
      extends LeaveContractRequestFailure

    final case class LeaveContractFailure(failureDescription: String)
      extends LeaveContractRequestFailure

  }
  final case class ContractLeft(contractId: ContractId)
}

class AdjudicatorRequest(adjudicatorEndpoint: String, ws: WSClient) {
  import AdjudicatorRequestTypes._

  def getPublicKey(hatName: String, contractId: ContractId, keyId: String)(implicit ec: ExecutionContext): Future[Either[PublicKeyRequestFailure, PublicKeyReceived]] = {

    val url =
      s"${adjudicatorEndpoint}/v1/contracts/${contractId}/hat/${hatName}/${keyId}"
    val eventuallyResponse = makeRequest(url, ws).get()

    eventuallyResponse.map { response =>
      response.status match {
        case OK => {
          val maybeArrayByte = Json.parse(response.body).asOpt[Array[Byte]]
          maybeArrayByte match {
            case None =>
              Left(PublicKeyRequestFailure.InvalidPublicKeyFailure(
                "The response was not able to be decoded into an Array[Byte]."))
            case Some(arrayByte) => Right(PublicKeyReceived(arrayByte))
          }
        }
        case _ => {
          Left(PublicKeyRequestFailure.ServiceRespondedWithFailure(
            s"The Adjudicator Service responded with an error: ${response.statusText}"))
        }
      }
    } recover {
      case e => {
        Left(PublicKeyRequestFailure.ServiceRespondedWithFailure(
          s"The Adjudicator Service responded with an error: ${e.getMessage}"))
      }
    }
  }

  def joinContract(hatName: String, contractId: ContractId)(
    implicit
    ec: ExecutionContext): Future[Either[JoinContractRequestFailure.ServiceRespondedWithFailure, ContractJoined]] = {

    val url =
      s"${adjudicatorEndpoint}/v1/contracts/${contractId}/hat/${hatName}"

    val eventuallyResponse = makeRequest(url, ws).put("")

    eventuallyResponse.map { response =>
      response.status match {
        case OK => {
          Right(ContractJoined(contractId))
        }
        case _ => {
          Left(JoinContractRequestFailure.ServiceRespondedWithFailure(
            s"The Adjudicator Service responded with an error: ${response.statusText}"))
        }
      }
    } recover {
      case e => {
        Left(JoinContractRequestFailure.ServiceRespondedWithFailure(
          s"The Adjudicator Service responded with an error: ${e.getMessage}"))
      }
    }
  }

  def leaveContract(hatName: String, contractId: ContractId)(
    implicit
    ec: ExecutionContext): Future[Either[LeaveContractRequestFailure, ContractLeft]] = {

    val url =
      s"${adjudicatorEndpoint}/v1/contracts/${contractId}/hat/${hatName}"

    val eventuallyResponse = makeRequest(url, ws).delete()
    eventuallyResponse.map { response =>
      response.status match {
        case OK => {
          Right(ContractLeft(contractId))
        }
        case _ => {
          Left(LeaveContractRequestFailure.ServiceRespondedWithFailure(
            s"The Adjudicator Service responded with an error: ${response.statusText}"))
        }
      }
    } recover {
      case e => {
        Left(LeaveContractRequestFailure.ServiceRespondedWithFailure(
          s"The Adjudicator Service responded with an error: ${e.getMessage}"))
      }
    }
  }

  private def makeRequest(url: String, ws: WSClient)(
    implicit
    ec: ExecutionContext): WSRequest = {
    ws.url(url)
    // TODO: Auth to ADJ goes here.
    //.withHttpHeaders("Accept" -> "application/json"
    //, "X-Auth-Token" -> hatSharedSecret)
    // )
    //request.get()
  }
}
