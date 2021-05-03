// package org.hatdex.hat.client

// import io.dataswift.adjudicator.Types.{ DeviceId }

// object AuthServiceRequestTypes {

//   // -- Public Key Request

//   // Failure
//   sealed trait PublicKeyRequestFailure
//   case class PublicKeyServiceFailure(failureDescription: String) extends PublicKeyRequestFailure
//   case class InvalidPublicKeyFailure(failureDescription: String) extends PublicKeyRequestFailure

//   // Success
//   case class PublicKeyReceived(publicKeyAsByteArray: Array[Byte]) extends AnyVal

//   // -- Join Device Request

//   // Failure
//   sealed trait JoinDeviceRequestFailure
//   case class JoinDeviceServiceFailure(failureDescription: String) extends JoinDeviceRequestFailure
//   case class JoinDeviceFailure(failureDescription: String) extends JoinDeviceRequestFailure

//   // TODO: if we attempt to extend AnyVal here, the compiler complains
//   // Success
//   case class DeviceJoined(deviceId: DeviceId) extends AnyVal

//   // Leave Device Request

//   // Failure
//   sealed trait LeaveDeviceRequestFailure
//   case class LeaveDeviceServiceFailure(failureDescription: String) extends LeaveDeviceRequestFailure
//   case class LeaveDeviceFailure(failureDescription: String) extends LeaveDeviceRequestFailure
//   // Success
//   case class DeviceLeft(deviceId: DeviceId) extends AnyVal
// }
