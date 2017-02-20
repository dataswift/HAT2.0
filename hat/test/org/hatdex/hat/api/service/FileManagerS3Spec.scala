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

import java.io.StringReader
import java.util.UUID

import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.ObjectMetadata
import com.atlassian.jwt.core.keys.KeyUtils
import com.google.inject.Provides
import com.google.inject.name.Named
import com.mohiva.play.silhouette.api.services.DynamicEnvironmentProviderService
import com.mohiva.play.silhouette.api.{ Environment, LoginInfo, Silhouette, SilhouetteProvider }
import com.mohiva.play.silhouette.test._
import org.hatdex.hat.authentication.HatFrontendAuthEnvironment
import org.hatdex.hat.authentication.models.HatUser
import org.hatdex.hat.dal.SlickPostgresDriver.backend.Database
import org.hatdex.hat.phata.controllers.FakeHatConfiguration
import org.hatdex.hat.resourceManagement.{ HatServer, HatServerProvider }
import org.specs2.concurrent.ExecutionEnv
import org.specs2.mock.Mockito
import org.specs2.specification.Scope
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.{ Request, Result }
import play.api.test.{ FakeRequest, PlaySpecification }
import play.api.{ Application, Configuration, Logger }

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration.FiniteDuration
import scala.concurrent.duration._

class FileManagerS3Spec(implicit ee: ExecutionEnv) extends PlaySpecification with Mockito {

  val logger = Logger(this.getClass)

  "The `getUploadUrl` method" should {
    "return a signed url for a provided key" in new Context {
      val mockS3client = FileManagerS3Mock().mockS3client
      val fileManager = new FileManagerS3(s3Configuration, mockS3client)
      val result: Future[String] = fileManager.getUploadUrl("testFile")

      result must startWith("https://hat-storage-test.s3.amazonaws.com/hat.hubofallthings.net/testFile?AWSAccessKeyId=testAwsAccessKey&Expires=").await
    }
  }

  "The `getContentUrl` method" should {
    "return a signed url for a provided key" in new Context {
      val mockS3client = FileManagerS3Mock().mockS3client
      val fileManager = new FileManagerS3(s3Configuration, mockS3client)
      val result: Future[String] = fileManager.getContentUrl("testFile")

      result must startWith("https://hat-storage-test.s3.amazonaws.com/hat.hubofallthings.net/testFile?AWSAccessKeyId=testAwsAccessKey&Expires=").await
    }
  }

  "The `deleteContents` method" should {
    "return quietly when deleting any file" in new Context {
      val mockS3client = FileManagerS3Mock().mockS3client
      val fileManager = new FileManagerS3(s3Configuration, mockS3client)
      val result: Future[Unit] = fileManager.deleteContents("deleteFile")

      result must not(throwAn[Exception]).await
      there was one(mockS3client).deleteObject("hat-storage-test", "hat.hubofallthings.net/deleteFile")
    }
  }

  "The `getFileSize` method" should {
    "return 0 for files that do not exist" in new Context {
      val mockS3client = FileManagerS3Mock().mockS3client
      val fileManager = new FileManagerS3(s3Configuration, mockS3client)
      val result: Future[Long] = fileManager.getFileSize("nonExistentFile")

      result must equalTo(0L).await(3, 10.seconds)
    }

    "extract file size for files that do exist" in new Context {
      val mockS3client = FileManagerS3Mock().mockS3client
      val fileManager = new FileManagerS3(s3Configuration, mockS3client)
      val result: Future[Long] = fileManager.getFileSize("testFile")

      result must equalTo(123456L).await
      there was one(mockS3client).getObjectMetadata("hat-storage-test", "hat.hubofallthings.net/testFile")
    }
  }
}

case class FileManagerS3Mock() extends Mockito {
  private val s3Configuration = AwsS3Configuration("hat-storage-test", "testAwsAccessKey", "testAwsSecret", 5.minutes)
  private val awsCreds: BasicAWSCredentials = new BasicAWSCredentials(s3Configuration.accessKeyId, s3Configuration.secretKey)
  val mockS3client: AmazonS3Client = spy(new AmazonS3Client(awsCreds))

  private val s3ObjectMetadata = new ObjectMetadata()
  s3ObjectMetadata.setContentLength(123456L)
  doReturn(s3ObjectMetadata).when(mockS3client).getObjectMetadata("hat-storage-test", "hat.hubofallthings.net/testFile")
  doNothing.when(mockS3client).deleteObject("hat-storage-test", "hat.hubofallthings.net/deleteFile")
}

trait Context extends Scope {
  val hatAddress = "hat.hubofallthings.net"
  val hatUrl = s"http://$hatAddress"
  private val keyUtils = new KeyUtils()
  private val configuration = Configuration.from(FakeHatConfiguration.config)
  private val hatConfig = configuration.getConfig(s"hat.$hatAddress").get

  private def hatDatabase: Database = Database.forURL(hatConfig.getString("database.properties.url").get)

  implicit val hatServer: HatServer = HatServer(hatAddress, "hat", "user@hat.org",
    keyUtils.readRsaPrivateKeyFromPem(new StringReader(hatConfig.getString("privateKey").get)),
    keyUtils.readRsaPublicKeyFromPem(new StringReader(hatConfig.getString("publicKey").get)), hatDatabase)

  val owner = HatUser(UUID.randomUUID(), "hatuser", Some("pa55w0rd"), "hatuser", "owner", true)
  implicit val env: Environment[HatFrontendAuthEnvironment] = FakeEnvironment[HatFrontendAuthEnvironment](Seq(owner.loginInfo -> owner), hatServer)

  val s3Configuration = AwsS3Configuration("hat-storage-test", "testAwsAccessKey", "testAwsSecret", 5.minutes)

  def provides3Client(configuration: AwsS3Configuration): AmazonS3Client = {
    val awsCreds: BasicAWSCredentials = new BasicAWSCredentials(configuration.accessKeyId, configuration.secretKey)
    new AmazonS3Client(awsCreds)
  }
}

