package org.hatdex.hat.api.service

import java.util.UUID

import org.hatdex.hat.authentication.models.HatUser
import org.hatdex.hat.dal.SlickPostgresDriver.api._
import org.hatdex.hat.dal.Tables._
import org.hatdex.hat.dal.ModelTranslation
import org.joda.time.LocalDateTime
import play.api.Logger

import scala.concurrent.{ ExecutionContext, Future }

class UsersService extends DalExecutionContext {
  val logger = Logger(this.getClass)

  def listUsers()(implicit db: Database): Future[Seq[HatUser]] = {
    db.run {
      UserUser.result
    } map { users =>
      users.map(ModelTranslation.fromDbModel)
    }
  }

  def getUser(userId: UUID)(implicit db: Database): Future[Option[HatUser]] = {
    db.run {
      UserUser.filter(_.userId === userId).result
    } map { users =>
      users.map(ModelTranslation.fromDbModel)
        .headOption
    }
  }

  def getUser(username: String)(implicit db: Database): Future[Option[HatUser]] = {
    logger.info(s"Get user by username: ${username}")
    db.run {
      UserUser.filter(_.email === username).result
    } map { users =>
      users.map(ModelTranslation.fromDbModel)
        .headOption
    }
  }

  def getUserForCredentials(username: String, passwordHash: String)(implicit db: Database): Future[Option[HatUser]] = {
    db.run {
      UserUser.filter(_.email === username).result
    } map { users =>
      users.map(ModelTranslation.fromDbModel)
        .headOption
    }
  }

  def saveUser(user: HatUser)(implicit db: Database): Future[HatUser] = {
    val userRow = UserUserRow(user.userId, LocalDateTime.now(), LocalDateTime.now(),
      user.email, user.pass, // The password is assumed to come in hashed, hence stored as is!
      user.name, user.role, enabled = user.enabled)

    db.run {
      (UserUser returning UserUser).insertOrUpdate(userRow)
    } map { user =>
      ModelTranslation.fromDbModel(user.get)
    }
  }

  def deleteUser(userId: UUID)(implicit db: Database): Future[Unit] = {
    val deleteUserQuery = UserUser.filter(_.userId === userId)
      .filterNot(_.role === "owner")
      .filterNot(_.role === "platform")

    val deleteTokensQuery = UserAccessToken.filter(_.userId in deleteUserQuery.map(_.userId)).delete

    db.run(DBIO.seq(deleteTokensQuery, deleteUserQuery.delete))
  }

  def changeUserState(userId: UUID, enabled: Boolean)(implicit db: Database): Future[Unit] = {
    val query = UserUser.filter(_.userId === userId)
      .map(u => (u.enabled))
      .update((enabled))
    db.run(query).map(_ => ())
  }

  def removeUser(username: String)(implicit db: Database): Future[Unit] = {
    logger.info(s"Get user by username: ${username}")
    db.run {
      UserUser.filter(_.email === username).delete
    } map { _ => () }
  }
}
