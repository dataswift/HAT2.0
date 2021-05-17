package org.hatdex.hat.api.repository

import io.dataswift.models.hat.ApiHatFile
import org.hatdex.hat.authentication.models.HatUser
import org.hatdex.libs.dal.HATPostgresProfile

import scala.concurrent.Future

/*
 * Copyright (C) 2019 - 2020 Dataswift Ltd - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 */
trait FileMetadataRepository {

  def getUniqueFileId(
      file: ApiHatFile
    )(implicit db: HATPostgresProfile.api.Database): Future[ApiHatFile]

  def save(file: ApiHatFile)(implicit db: HATPostgresProfile.api.Database): Future[ApiHatFile]

  def grantAccess(
      file: ApiHatFile,
      user: HatUser,
      content: Boolean
    )(implicit db: HATPostgresProfile.api.Database): Future[Unit]

  def restrictAccess(
      file: ApiHatFile,
      user: HatUser
    )(implicit db: HATPostgresProfile.api.Database): Future[Unit]

  def grantAccessPattern(
      fileTemplate: ApiHatFile,
      user: HatUser,
      content: Boolean
    )(implicit db: HATPostgresProfile.api.Database): Future[Unit]

  def restrictAccessPattern(
      fileTemplate: ApiHatFile,
      user: HatUser
    )(implicit db: HATPostgresProfile.api.Database): Future[Unit]

  def delete(fileId: String)(implicit db: HATPostgresProfile.api.Database): Future[ApiHatFile]

  def getById(fileId: String)(implicit db: HATPostgresProfile.api.Database): Future[Option[ApiHatFile]]

  def search(fileTemplate: ApiHatFile)(implicit db: HATPostgresProfile.api.Database): Future[Seq[ApiHatFile]]
}
