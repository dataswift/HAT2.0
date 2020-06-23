package org.hatdex.hat.api.service.applications

object ApplicationExceptions {
  abstract class HatApplicationManagementException(message: String, cause: Throwable = None.orNull) extends Exception(message, cause)

  case class HatApplicationSetupException(appId: String, message: String, cause: Throwable = None.orNull)
    extends HatApplicationManagementException(message, cause)
  case class HatApplicationDependencyException(appId: String, message: String, cause: Throwable = None.orNull)
    extends HatApplicationManagementException(message, cause)
}
