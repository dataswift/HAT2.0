package org.hatdex.hat.client

import io.dataswift.adjudicator.Types.{ DeviceId, HatName }
import org.hatdex.hat.client.AuthServiceRequestTypes._

import scala.concurrent.Future

trait AuthServiceClient {
  def getPublicKey(
      hatName: HatName,
      deviceId: DeviceId,
      keyId: String): Future[Either[PublicKeyRequestFailure, PublicKeyReceived]]

  def joinDevice(
      hatName: String,
      deviceId: DeviceId): Future[Either[
    JoinDeviceServiceFailure,
    DeviceJoined
  ]]

  def leaveDevice(
      hatName: String,
      deviceId: DeviceId): Future[Either[LeaveDeviceRequestFailure, DeviceLeft]]
}
