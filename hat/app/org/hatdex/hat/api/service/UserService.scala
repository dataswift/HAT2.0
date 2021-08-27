package org.hatdex.hat.api.service
import io.dataswift.models.hat.UserRole
import org.hatdex.hat.authentication.models.{ HatAccessLog, HatUser }
import org.hatdex.hat.resourceManagement.HatServer

import java.util.UUID
import scala.concurrent.Future

trait UserService {

  def listUsers()(implicit server: HatServer): Future[Seq[HatUser]]

  def getUser(userId: UUID)(implicit server: HatServer): Future[Option[HatUser]]

  def getMockUser(hatName: String, domain: String): Future[Option[HatUser]]

  def getUser(username: String)(implicit server: HatServer): Future[Option[HatUser]]

  def getUserByRole(role: UserRole)(implicit server: HatServer): Future[Seq[HatUser]]

  def saveUser(user: HatUser)(implicit server: HatServer): Future[HatUser]

  def deleteUser(userId: UUID)(implicit server: HatServer): Future[Unit]

  def changeUserState(
      userId: UUID,
      enabled: Boolean
    )(implicit server: HatServer): Future[Unit]

  def removeUser(username: String)(implicit server: HatServer): Future[Unit]

  def previousLogin(
      user: HatUser
    )(implicit server: HatServer): Future[Option[HatAccessLog]]

  def logLogin(
      user: HatUser,
      loginType: String,
      scope: String,
      appName: Option[String],
      appResource: Option[String]
    )(implicit server: HatServer): Future[Unit]
}
