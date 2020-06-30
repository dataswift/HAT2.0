package org.hatdex.hat.utils

import io.dataswift.adjudicator.Types.{ Contract, ContractId }
import play.api.libs.ws.{ WSClient, WSRequest }
import play.api.http.Status._
import play.api.libs.json.Json

import scala.concurrent.{ ExecutionContext, Future }

// TODO: move these some place more appropriate
// Error ADTs

// Public Key Request
sealed trait PublicKeyRequestFailure
object PublicKeyRequestFailure {
  final case class ServiceRespondedWithFailure(failureDescription: String)
    extends PublicKeyRequestFailure
  final case class InvalidPublicKeyFailure(failureDescription: String)
    extends PublicKeyRequestFailure
}

sealed trait PublicKeyRequestSuccess
object PublicKeyRequestSuccess {
  final case class PublicKeyReceived(publicKeyAsByteArray: Array[Byte])
    extends PublicKeyRequestSuccess
}

// Join Contract Request
sealed trait JoinContractRequestFailure
object JoinContractRequestFailure {
  final case class ServiceRespondedWithFailure(failureDescription: String)
    extends JoinContractRequestFailure
  final case class JoinContractFailure(failureDescription: String)
    extends JoinContractRequestFailure
}

sealed trait JoinContractRequestSuccess
object JoinContractRequestSuccess {
  final case object ContractJoined
    extends JoinContractRequestSuccess
}

// Leave Contract Request
sealed trait LeaveContractRequestFailure
object LeaveContractRequestFailure {
  final case class ServiceRespondedWithFailure(failureDescription: String)
    extends LeaveContractRequestFailure
  final case class LeaveContractFailure(failureDescription: String)
    extends LeaveContractRequestFailure
}

sealed trait LeaveContractRequestSuccess
object LeaveContractRequestSuccess {
  final case object ContractLeft extends LeaveContractRequestSuccess
}

class AdjudicatorRequest(adjudicatorEndpoint: String, ws: WSClient) {

  def getPublicKey(hatName: String, contractId: ContractId, keyId: String)(implicit ec: ExecutionContext): Future[Either[PublicKeyRequestFailure, PublicKeyRequestSuccess]] = {

    import PublicKeyRequestSuccess._

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
    ec: ExecutionContext): Future[Either[JoinContractRequestFailure.ServiceRespondedWithFailure, JoinContractRequestSuccess]] = {

    import JoinContractRequestSuccess._

    val url =
      s"${adjudicatorEndpoint}/v1/contracts/${contractId}/hat/${hatName}"

    val eventuallyResponse = makeRequest(url, ws).put("")

    eventuallyResponse.map { response =>
      response.status match {
        case OK => {
          Right(ContractJoined)
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
    ec: ExecutionContext): Future[Either[LeaveContractRequestFailure, LeaveContractRequestSuccess]] = {

    import LeaveContractRequestSuccess._

    val url =
      s"${adjudicatorEndpoint}/v1/contracts/${contractId}/hat/${hatName}"

    val eventuallyResponse = makeRequest(url, ws).delete()
    eventuallyResponse.map { response =>
      response.status match {
        case OK => {
          Right(ContractLeft)
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
