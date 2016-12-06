package org.hatdex.hat.authentication

import javax.inject.Inject

import com.mohiva.play.silhouette.api.LoginInfo
import org.hatdex.hat.api.service.UsersService
import org.hatdex.hat.authentication.models.HatUser
import org.hatdex.hat.resourceManagement.HatServer

import scala.concurrent.Future

/**
 * Handles actions to users.
 *
 * @param usersService The underlying database User Service implementation
 */
class AuthUserServiceImpl @Inject() (usersService: UsersService) extends AuthUserService {
  /**
   * Retrieves a user that matches the specified login info.
   *
   * @param loginInfo The login info to retrieve a user.
   * @return The retrieved user or None if no user could be retrieved for the given login info.
   */
  def retrieve(loginInfo: LoginInfo)(implicit dyn: HatServer): Future[Option[HatUser]] = {
    usersService.getUser(loginInfo.providerKey)(dyn.db)
  }

  /**
   * Saves a user.
   *
   * @param user The user to save.
   * @return The saved user.
   */
  def save(user: HatUser)(implicit dyn: HatServer) = usersService.saveUser(user)(dyn.db)

  /**
   * Link user profiles together
   *
   * @param mainUser The user to link to.
   * @param mainUser The linked user
   */
  def link(mainUser: HatUser, linkedUser: HatUser)(implicit dyn: HatServer) = Future.failed(new RuntimeException("Profile linking not implemented"))
}