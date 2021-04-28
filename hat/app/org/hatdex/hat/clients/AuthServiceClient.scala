package org.hatdex.hat.clients

import io.dataswift.adjudicator.Types.{ DeviceId, HatName }
import org.hatdex.hat.clients.AuthServiceRequestTypes._

import scala.concurrent.Future

trait AuthServiceClient {
  def getPublicKey(
      hatName: HatName,
      deviceId: DeviceId,
      keyId: String): Future[Either[PublicKeyRequestFailure, PublicKeyReceived]]

  def joinDevice(
      hatName: String,
      deviceId: DeviceId): Future[Either[
    JoinDeviceRequestFailure.ServiceRespondedWithFailure,
    DeviceJoined
  ]]

  def leaveDevice(
      hatName: String,
      deviceId: DeviceId): Future[Either[LeaveDeviceRequestFailure, DeviceLeft]]
}
