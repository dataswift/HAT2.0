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

import javax.inject.Inject

import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.{ GeneratePresignedUrlRequest, SSEAlgorithm }
import com.google.inject.name.Named
import org.hatdex.hat.resourceManagement.HatServer
import play.api.Logger

import scala.concurrent.Future
import scala.concurrent.duration.FiniteDuration

trait FileManager {
  def getUploadUrl(filename: String)(implicit hatServer: HatServer): Future[String]
  def getContentUrl(filename: String)(implicit hatServer: HatServer): Future[String]
  def getFileSize(fileName: String)(implicit hatServer: HatServer): Future[Long]
  def deleteContents(filename: String)(implicit hatServer: HatServer): Future[Unit]
}

case class AwsS3Configuration(
  bucketName: String,
  accessKeyId: String,
  secretKey: String,
  region: String,
  signedUrlExpiry: FiniteDuration)

class FileManagerS3 @Inject() (
    awsS3Configuration: AwsS3Configuration,
    @Named("s3client-file-manager") s3client: AmazonS3) extends FileManager with RemoteApiExecutionContext {

  private val logger = Logger(this.getClass)
  private val bucketName = awsS3Configuration.bucketName

  def getUploadUrl(fileName: String)(implicit hatServer: HatServer): Future[String] = {
    val expiration = org.joda.time.DateTime.now().plus(awsS3Configuration.signedUrlExpiry.toMillis)

    val generatePresignedUrlRequest = new GeneratePresignedUrlRequest(bucketName, s"${hatServer.domain}/$fileName")
      .withMethod(com.amazonaws.HttpMethod.PUT)
      .withExpiration(expiration.toDate)
      .withSSEAlgorithm(SSEAlgorithm.AES256)

    val url = Future(s3client.generatePresignedUrl(generatePresignedUrlRequest))
    url.map(_.toString)
  }

  def getContentUrl(fileName: String)(implicit hatServer: HatServer): Future[String] = {
    val expiration = org.joda.time.DateTime.now().plus(awsS3Configuration.signedUrlExpiry.toMillis)

    val generatePresignedUrlRequest = new GeneratePresignedUrlRequest(bucketName, s"${hatServer.domain}/$fileName")
      .withMethod(com.amazonaws.HttpMethod.GET)
      .withExpiration(expiration.toDate)

    val url = Future(s3client.generatePresignedUrl(generatePresignedUrlRequest))
    url.map(_.toString)
  }

  def getFileSize(fileName: String)(implicit hatServer: HatServer): Future[Long] = {
    logger.info(s"Getting file size for $bucketName ${hatServer.domain}/$fileName")
    Future(s3client.getObjectMetadata(bucketName, s"${hatServer.domain}/$fileName"))
      .map { metadata => Option(metadata.getContentLength).getOrElse(0L) }
      .recover { case _ => 0L }
  }

  def deleteContents(fileName: String)(implicit hatServer: HatServer): Future[Unit] = {
    Future(s3client.deleteObject(bucketName, s"${hatServer.domain}/$fileName"))
  }
}
