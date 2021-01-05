package org.hatdex.hat.utils

import org.specs2.mock.Mockito
import play.api.Logger
import play.api.test.{ PlaySpecification }

import org.hatdex.hat.api.models.{ NamespaceRead, NamespaceWrite }

class NamespaceTestSpec extends PlaySpecification with Mockito {

  val logger = Logger(this.getClass)

  sequential

  "The Namespace Verification" should {
    "Control: Allow the correct namespace" in {
      val namespace              = "correctnamespace"
      val applicationPermissions = List(NamespaceWrite("correctnamespace"), NamespaceRead("correctnamespace"))

      val roles = applicationPermissions.map {
        case NamespaceRead(n) if n == namespace  => Some(namespace)
        case NamespaceWrite(n) if n == namespace => Some(namespace)
        case _                                   => None
      }
      logger.error(
        s"Roles: ${roles} - Namespace: ${namespace} - results should be true: ${!roles.flatten.isEmpty}"
      )

      !roles.flatten.isEmpty == true
    }

    "Control: Disallow the incorrect namespace" in {
      val namespace              = "incorrectnamespace"
      val applicationPermissions = List(NamespaceWrite("correctnamespace"), NamespaceRead("correctnamespace"))

      val roles = applicationPermissions.map {
        case NamespaceRead(n) if n == namespace  => Some(namespace)
        case NamespaceWrite(n) if n == namespace => Some(namespace)
        case _                                   => None
      }
      logger.error(
        s"Roles: ${roles} - Namespace: ${namespace} - results should be false: ${!roles.flatten.isEmpty}"
      )

      !roles.flatten.isEmpty == false
    }
  }
}
