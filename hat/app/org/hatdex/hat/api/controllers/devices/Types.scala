package org.hatdex.hat.api.controllers.devices

import io.dataswift.adjudicator.Types.{ DeviceId, HatName, ShortLivedToken }
import org.hatdex.hat.authentication.models.HatUser
import pdi.jwt.JwtClaim

object Types {
  // -- Types
  case class DeviceDataInfoRefined(
      token: ShortLivedToken,
      hatName: HatName,
      deviceId: DeviceId)

  sealed trait RequiredNamespacePermissions
  case object Read extends RequiredNamespacePermissions
  case object Write extends RequiredNamespacePermissions
  case object ReadWrite extends RequiredNamespacePermissions

  // -- Errors
  sealed abstract class DeviceVerificationFailure
  object DeviceVerificationFailure {
    final case class ServiceRespondedWithFailure(failureDescription: String) extends DeviceVerificationFailure
    final case class InvalidTokenFailure(failureDescription: String) extends DeviceVerificationFailure
    final case object ApplicationAndNamespaceNotValid extends DeviceVerificationFailure
    final case class InvalidDeviceDataRequestFailure(
        failureDescription: String)
        extends DeviceVerificationFailure
    final case object FailedDeviceRefinement extends DeviceVerificationFailure
    final case class DeviceRequestFailure(message: String) extends DeviceVerificationFailure
  }

  sealed abstract class DeviceVerificationSuccess
  object DeviceVerificationSuccess {
    final case class JwtClaimVerified(jwtClaim: JwtClaim) extends DeviceVerificationSuccess
    final case class DeviceRequestSuccess(hatUser: HatUser) extends DeviceVerificationSuccess
    final case class DeviceRequestVerificationSuccess(message: String) extends DeviceVerificationSuccess
  }

}
