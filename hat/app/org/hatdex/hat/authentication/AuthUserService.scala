package org.hatdex.hat.authentication

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.services.IdentityService
import org.hatdex.hat.authentication.models.HatUser
import org.hatdex.hat.resourceManagement.HatServer

import scala.concurrent.Future

/**
 * Handles actions to users.
 */
trait AuthUserService extends IdentityService[HatUser, HatServer] {

  /**
   * Retrieves a user that matches the specified ID.
   *
   * @param id The ID to retrieve a user.
   * @return The retrieved user or None if no user could be retrieved for the given ID.
   */
  def retrieve(loginInfo: LoginInfo)(implicit dyn: HatServer): Future[Option[HatUser]]

  /**
   * Saves a user.
   *
   * @param user The user to save.
   * @return The saved user.
   */
  def save(user: HatUser)(implicit dyn: HatServer): Future[HatUser]

  def link(mainUser: HatUser, linkedUser: HatUser)(implicit dyn: HatServer): Future[Unit]
}
