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
import javax.inject.Inject

import org.hatdex.hat.api.models.{ UserRole, _ }
import org.hatdex.hat.authentication.models.{ HatAccessLog, HatUser }
import org.hatdex.hat.dal.ModelTranslation
import org.hatdex.hat.dal.Tables.{ UserRole ⇒ UserRoleDb, DataDebit ⇒ DataDebitDb, _ }
import org.hatdex.libs.dal.HATPostgresProfile.api._
import org.joda.time.LocalDateTime
import play.api.Logger

import scala.concurrent.Future

class UsersService @Inject() (implicit ec: DalExecutionContext) {
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

  def getUserByRole(role: UserRole)(implicit db: Database): Future[Seq[HatUser]] = {
    val usersWithRole = UserRoleDb.filter(r => r.role === role.title && (r.extra.isEmpty || r.extra === role.extra)).map(_.userId)
    val query = UserUser.filter(_.userId in usersWithRole)
    queryUser(query)
  }

  def getUserForCredentials(username: String, passwordHash: String)(implicit db: Database): Future[Option[HatUser]] = {
    queryUser(UserUser.filter(_.email === username))
      .map(_.headOption)
  }

  private def queryUser(userFilter: Query[UserUser, UserUserRow, Seq])(implicit db: Database): Future[Seq[HatUser]] = {
    val debits = DataDebitDb.map(d => (d.recipientId, d.dataDebitKey.asColumnOf[String]))
    val debits2 = DataDebitContract.map(d => (d.clientId.asColumnOf[String], d.dataDebitKey))
    val dd = debits.unionAll(debits2)

    val eventualUsers = queryUsers(userFilter)

    val userDataDebits = userFilter
      .joinLeft(dd)
      .on(_.userId.asColumnOf[String] === _._1)

    for {
      users <- eventualUsers
      dataDebits <- db.run(userDataDebits.result)
    } yield {
      users map { user =>
        val roles = dataDebits.filter(_._1.userId == user.userId) map { userDataDebit =>
          UserRole.userRoleDeserialize("datadebit", userDataDebit._2.map(_._2))
        }
        user.withRoles(roles: _*)
      }
    }
  }

  implicit def equalDataJsonRowIdentity(a: UserUserRow, b: UserUserRow): Boolean = {
    a.userId == b.userId
  }

  private def queryUsers(userFilter: Query[UserUser, UserUserRow, Seq])(implicit db: Database): Future[Seq[HatUser]] = {
    val query = userFilter.joinLeft(UserRoleDb).on(_.userId === _.userId).sortBy(_._1.userId)
    db.run(query.result) map { results =>
      ModelTranslation.groupRecords(results).map(ModelTranslation.fromDbModel)
    }
  }

  def saveUser(user: HatUser)(implicit db: Database): Future[HatUser] = {
    val userRow = UserUserRow(user.userId, LocalDateTime.now(), LocalDateTime.now(),
      user.email, user.pass, // The password is assumed to come in hashed, hence stored as is!
      user.name, enabled = user.enabled)
    val userRoleRows = user.roles map { role =>
      UserRoleRow(user.userId, role.title, role.extra)
    }

    val query = DBIO.seq(
      UserRoleDb.filter(_.userId === user.userId).delete,
      UserUser.insertOrUpdate(userRow),
      UserRoleDb ++= userRoleRows)

    val upsertedUser = db.run(query.transactionally)

    upsertedUser.map(_ => user)
  }

  def deleteUser(userId: UUID)(implicit db: Database): Future[Unit] = {
    getUser(userId) flatMap {
      case Some(user) if user.roles.contains(Owner()) => Future.failed(new RuntimeException("Can not delete owner user"))
      case Some(user) if user.roles.contains(Platform()) => Future.failed(new RuntimeException("Can not delete platform user"))
      case None => Future.failed(new RuntimeException("User does not exist"))
      case Some(user) =>
        val deleteRoles = UserRoleDb.filter(_.userId === user.userId).delete
        val deleteUsers = UserUser.filter(_.userId === user.userId).delete
        db.run(DBIO.seq(deleteRoles, deleteUsers).transactionally).map(_ => ())
    }
  }

  def changeUserState(userId: UUID, enabled: Boolean)(implicit db: Database): Future[Unit] = {
    val query = UserUser.filter(_.userId === userId)
      .map(u => (u.enabled, u.lastUpdated))
      .update((enabled, LocalDateTime.now()))
    db.run(query).map(_ => ())
  }

  def removeUser(username: String)(implicit db: Database): Future[Unit] = {
    val eventualUserIds = db.run(UserUser.filter(_.email === username).map(_.userId).result)
    eventualUserIds flatMap { userId =>
      Future.sequence(userId.map(deleteUser))
        .map(_ => ())
    }
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
