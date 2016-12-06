/*
 * Copyright (C) HAT Data Exchange Ltd - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Andrius Aucinas <andrius.aucinas@hatdex.org>, 10 2016
 */

package org.hatdex.hat.authentication

import javax.inject.Inject

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.util.PasswordInfo
import com.mohiva.play.silhouette.persistence.daos.DelegableAuthInfoDAO
import org.hatdex.hat.authentication.Implicits._
import org.hatdex.hat.resourceManagement.HatServer
import play.api.Logger

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class PasswordInfoService @Inject() (userService: AuthUserServiceImpl) extends DelegableAuthInfoDAO[PasswordInfo, HatServer] {
  val logger = Logger("org.hatdex.hat.authentication")
  def add(loginInfo: LoginInfo, authInfo: PasswordInfo)(implicit hat: HatServer): Future[PasswordInfo] =
    update(loginInfo, authInfo)

  def find(loginInfo: LoginInfo)(implicit hat: HatServer): Future[Option[PasswordInfo]] = {
    logger.info(s"Finding password info for $loginInfo")
    userService.retrieve(loginInfo).map {
      case Some(user) if user.pass.isDefined =>
        logger.info(s"Got back user ${user}")
        Some(user.pass.get)
      case _ =>
        logger.info("No such user")
        None
    }
  }

  def remove(loginInfo: LoginInfo)(implicit hat: HatServer): Future[Unit] = Future.successful(()) //userService.remove(loginInfo)

  def save(loginInfo: LoginInfo, authInfo: PasswordInfo)(implicit hat: HatServer): Future[PasswordInfo] =
    find(loginInfo).flatMap {
      case Some(_) => update(loginInfo, authInfo)
      case None    => add(loginInfo, authInfo)
    }

  def update(loginInfo: LoginInfo, authInfo: PasswordInfo)(implicit hat: HatServer): Future[PasswordInfo] =
    userService.retrieve(loginInfo).map {
      case Some(user) => {
        userService.save(user.copy(pass = Some(authInfo)))
        authInfo
      }
      case _ => throw new Exception("PasswordInfoDAO - update : the user must exists to update its password")
    }

}