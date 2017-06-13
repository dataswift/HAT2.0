/*
 * Copyright (C) 2017 HAT Data Exchange Ltd
 * SPDX-License-Identifier: AGPL-3.0
 *
 * This file is part of the Hub of All Things project (HAT).
 *
 * HAT is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation, version 3 of
 * the License.
 *
 * HAT is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See
 * the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General
 * Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 *
 * Written by Andrius Aucinas <andrius.aucinas@hatdex.org>
 * 2 / 2017
 */

package org.hatdex.hat.api.service

import java.util.UUID

import org.hatdex.hat.authentication.models.{ HatAccessLog, HatUser, UserRole }
import org.hatdex.hat.dal.ModelTranslation
import org.hatdex.hat.dal.SlickPostgresDriver.api._
import org.hatdex.hat.dal.Tables._
import org.joda.time.LocalDateTime
import play.api.Logger

import scala.concurrent.Future

class UsersService extends DalExecutionContext {
  val logger = Logger(this.getClass)

  def listUsers()(implicit db: Database): Future[Seq[HatUser]] = {
    queryUser(UserUser)
  }

  def getUser(userId: UUID)(implicit db: Database): Future[Option[HatUser]] = {
    queryUser(UserUser.filter(_.userId === userId))
      .map(_.headOption)
  }

  def getUser(username: String)(implicit db: Database): Future[Option[HatUser]] = {
    queryUser(UserUser.filter(_.email === username))
      .map(_.headOption)
  }

  def getUserForCredentials(username: String, passwordHash: String)(implicit db: Database): Future[Option[HatUser]] = {
    queryUser(UserUser.filter(_.email === username))
      .map(_.headOption)
  }

  private def queryUser(userFilter: Query[UserUser, UserUserRow, Seq])(implicit db: Database): Future[Seq[HatUser]] = {
    val debits = DataDebit.map(d => (d.recipientId, d.dataDebitKey.asColumnOf[String]))
    val debits2 = DataDebitContract.map(d => (d.clientId.asColumnOf[String], d.dataDebitKey))
    val dd = debits.unionAll(debits2)

    val userWithRoles = userFilter
      .joinLeft(dd)
      .on(_.userId.asColumnOf[String] === _._1)

    db.run(userWithRoles.result) map { roles =>
      val users = roles.groupBy(_._1.userId) map {
        case (userId, records) =>
          val hatUser = ModelTranslation.fromDbModel(records.head._1)
          val roles = records.flatMap(_._2).map(r => UserRole.userRoleDeserialize("dataDebit", Some(r._2), true)._1)
          hatUser.copy(roles = hatUser.roles ++ roles)
      } toSeq

      users
    }
  }

  def saveUser(user: HatUser)(implicit db: Database): Future[HatUser] = {
    val userRow = UserUserRow(user.userId, LocalDateTime.now(), LocalDateTime.now(),
      user.email, user.pass, // The password is assumed to come in hashed, hence stored as is!
      user.name, user.primaryRole.title, enabled = user.enabled)

    db.run {
      for {
        _ <- (UserUser returning UserUser).insertOrUpdate(userRow)
        updated <- UserUser.filter(_.userId === user.userId).result
      } yield updated
    } map { user =>
      ModelTranslation.fromDbModel(user.head)
    }
  }

  def deleteUser(userId: UUID)(implicit db: Database): Future[Unit] = {
    val deleteUserQuery = UserUser.filter(_.userId === userId)
      .filterNot(_.role === "owner")
      .filterNot(_.role === "platform")

    db.run(deleteUserQuery.delete).map(_ => ())
  }

  def changeUserState(userId: UUID, enabled: Boolean)(implicit db: Database): Future[Unit] = {
    val query = UserUser.filter(_.userId === userId)
      .map(u => (u.enabled, u.lastUpdated))
      .update((enabled, LocalDateTime.now()))
    db.run(query).map(_ => ())
  }

  def removeUser(username: String)(implicit db: Database): Future[Unit] = {
    db.run {
      UserUser.filter(_.email === username).delete
    } map { _ => () }
  }

  def previousLogin(user: HatUser)(implicit db: Database): Future[Option[HatAccessLog]] = {
    val query = for {
      access <- UserAccessLog.filter(l => l.userId === user.userId).sortBy(_.date.desc).take(2).drop(1)
      user <- access.userUserFk
    } yield (access, user)
    db.run(query.result)
      .map(_.headOption)
      .map(_.map(au => ModelTranslation.fromDbModel(au._1, ModelTranslation.fromDbModel(au._2))))
  }

  def logLogin(user: HatUser, loginType: String, scope: String, appName: Option[String], appResource: Option[String])(implicit db: Database): Future[Unit] = {
    val accessLog = UserAccessLogRow(LocalDateTime.now(), user.userId, loginType, scope, appName, appResource)
    db.run(UserAccessLog += accessLog).map(_ => ())
  }

}
