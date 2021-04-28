package org.hatdex.hat.clients

import io.dataswift.adjudicator.Types.{ DeviceId }

object AuthServiceRequestTypes {
  // Public Key Request
  // This is the same as contract
  sealed trait PublicKeyRequestFailure
  object PublicKeyRequestFailure {
    final case class ServiceRespondedWithFailure(failureDescription: String) extends PublicKeyRequestFailure
    final case class InvalidPublicKeyFailure(failureDescription: String) extends PublicKeyRequestFailure
  }
  final case class PublicKeyReceived(publicKeyAsByteArray: Array[Byte]) extends AnyVal

  // Join Device Request
  sealed trait JoinDeviceRequestFailure
  object JoinDeviceRequestFailure {
    final case class ServiceRespondedWithFailure(failureDescription: String) extends JoinDeviceRequestFailure
    final case class JoinDeviceFailure(failureDescription: String) extends JoinDeviceRequestFailure
  }
  // TODO: if we attempt to extend AnyVal here, the compiler complains
  final case class DeviceJoined(deviceId: DeviceId)

  // Leave Device Request
  sealed trait LeaveDeviceRequestFailure
  object LeaveDeviceRequestFailure {
    final case class ServiceRespondedWithFailure(failureDescription: String) extends LeaveDeviceRequestFailure
    final case class LeaveDeviceFailure(failureDescription: String) extends LeaveDeviceRequestFailure
  }
  final case class DeviceLeft(deviceId: DeviceId)
}
