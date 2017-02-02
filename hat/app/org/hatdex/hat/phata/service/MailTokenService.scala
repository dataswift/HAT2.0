/*
 * Copyright (C) HAT Data Exchange Ltd - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Andrius Aucinas <andrius.aucinas@hatdex.org>, 10 2016
 */

package org.hatdex.hat.phata.service

import org.hatdex.hat.api.actors.DalExecutionContext
import org.hatdex.hat.dal.ModelTranslation
import org.hatdex.hat.dal.SlickPostgresDriver.api._
import org.hatdex.hat.dal.Tables._
import org.hatdex.hat.phata.models.{ MailToken, MailTokenUser }

import scala.concurrent._

trait MailTokenService[T <: MailToken] {
  def create(token: T)(implicit db: Database): Future[Option[T]]
  def retrieve(id: String)(implicit db: Database): Future[Option[T]]
  def consume(id: String)(implicit db: Database): Unit
}

@javax.inject.Singleton
class MailTokenUserService extends MailTokenService[MailTokenUser] with DalExecutionContext {
  def create(token: MailTokenUser)(implicit db: Database): Future[Option[MailTokenUser]] = {
    save(token).map(Some(_))
  }
  def retrieve(id: String)(implicit db: Database): Future[Option[MailTokenUser]] = {
    findById(id)
  }
  def consume(id: String)(implicit db: Database): Unit = {
    delete(id)
  }

  private def findById(id: String)(implicit db: Database): Future[Option[MailTokenUser]] = {
    db.run(UserMailTokens.filter(_.id === id).result).map { tokens =>
      tokens.headOption.map(ModelTranslation.fromDbModel)
    }
  }

  private def save(token: MailTokenUser)(implicit db: Database): Future[MailTokenUser] = {
    val query = (UserMailTokens returning UserMailTokens) += UserMailTokensRow(token.id, token.email, token.expirationTime.toLocalDateTime, token.isSignUp)
    db.run(query)
      .map(ModelTranslation.fromDbModel)
  }

  private def delete(id: String)(implicit db: Database): Unit = {
    db.run(UserMailTokens.filter(_.id === id).delete)
  }
}