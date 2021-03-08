package org.hatdex.hat.utils
import play.api.Logger

import io.dataswift.models.hat.{ NamespaceRead, NamespaceWrite, UserRole }
import org.hatdex.hat.NamespaceUtils._
import org.scalatestplus.mockito.MockitoSugar
import io.dataswift.test.common.BaseSpec

class NamespaceTestSpec extends BaseSpec with MockitoSugar {

  val logger = Logger(this.getClass)

  "The Namespace Verification" should "NamespaceUtils: Allow the correct namespace" in {
    val namespace              = "correctnamespace"
    val applicationPermissions = List(NamespaceWrite("correctnamespace"), NamespaceRead("correctnamespace"))

    val readRoles  = NamespaceUtils.testReadNamespacePermissions(applicationPermissions, namespace)
    val writeRoles = NamespaceUtils.testWriteNamespacePermissions(applicationPermissions, namespace)

    readRoles == writeRoles
  }

  it should "NamespaceUtils: Disallow the incorrect namespace" in {
    val namespace              = "incorrectnamespace"
    val applicationPermissions = List(NamespaceWrite("correctnamespace"), NamespaceRead("correctnamespace"))

    val readRoles  = NamespaceUtils.testReadNamespacePermissions(applicationPermissions, namespace)
    val writeRoles = NamespaceUtils.testWriteNamespacePermissions(applicationPermissions, namespace)

    readRoles == false
    writeRoles == false
  }

  it should "NamespaceUtils: Empty App Permission" in {
    val namespace              = "incorrectnamespace"
    val applicationPermissions = List.empty

    val readRoles  = NamespaceUtils.testReadNamespacePermissions(applicationPermissions, namespace)
    val writeRoles = NamespaceUtils.testWriteNamespacePermissions(applicationPermissions, namespace)

    readRoles == false
    writeRoles == false
  }

  it should "NamespaceUtils: Empty Namespace" in {
    val namespace              = ""
    val applicationPermissions = List(NamespaceWrite("correctnamespace"), NamespaceRead("correctnamespace"))

    val readRoles  = NamespaceUtils.testReadNamespacePermissions(applicationPermissions, namespace)
    val writeRoles = NamespaceUtils.testWriteNamespacePermissions(applicationPermissions, namespace)

    readRoles == writeRoles
  }

  it should "NamespaceUtils: Mis-matched types" in {
    val namespace                             = "correctnamespace"
    val applicationPermissions: Seq[UserRole] = List(NamespaceWrite("correctnamespace"))

    val readRoles  = NamespaceUtils.testReadNamespacePermissions(applicationPermissions, namespace)
    val writeRoles = NamespaceUtils.testWriteNamespacePermissions(applicationPermissions, namespace)

    readRoles == false
    writeRoles == true
  }
}
