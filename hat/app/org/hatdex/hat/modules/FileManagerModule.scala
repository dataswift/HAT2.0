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

package org.hatdex.hat.modules

import com.amazonaws.services.s3.{ AmazonS3, AmazonS3ClientBuilder }
import com.google.inject.Provides
import net.codingwell.scalaguice.ScalaModule
import org.hatdex.hat.api.repository.{ FileMetadataRepository, FileMetadataRepositorySlick }
import org.hatdex.hat.api.service.{
  AwsS3Configuration,
  FileManager,
  FileManagerS3,
  FileUploadService,
  FileUploadServiceImpl
}
import play.api.Configuration

import javax.inject.{ Singleton => JSingleton }

class FileManagerModule extends ScalaModule {

  override def configure(): Unit = {
    bind[FileUploadService].to[FileUploadServiceImpl]
    bind[FileMetadataRepository].to[FileMetadataRepositorySlick]
    bind[FileManager].to[FileManagerS3]
    ()
  }

  @Provides @JSingleton
  def provideAwsS3Configuration(configuration: Configuration): AwsS3Configuration = {
    import AwsS3Configuration.configLoader
    configuration.get[AwsS3Configuration]("storage.s3Configuration")
  }

  @Provides @JSingleton
  def provideS3Client: AmazonS3 = AmazonS3ClientBuilder.defaultClient()

}
