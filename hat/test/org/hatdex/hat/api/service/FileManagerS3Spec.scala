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

import scala.concurrent.Future

import io.dataswift.test.common.BaseSpec
import org.hatdex.hat.api.HATTestContext
import org.scalatest.time.{ Millis, Second, Span }
import org.scalatest.{ BeforeAndAfterAll, BeforeAndAfterEach }

class FileManagerS3Spec extends BaseSpec with BeforeAndAfterEach with BeforeAndAfterAll with HATTestContext {

  implicit val defaultPatience: PatienceConfig =
    PatienceConfig(timeout = Span(1, Second), interval = Span(50, Millis))

  "The `getUploadUrl` method" should "return a signed url for a provided key" in {
    val fileManager            = application.injector.instanceOf[FileManager]
    val result: Future[String] = fileManager.getUploadUrl("testFile", None)

    result.futureValue must startWith(expectedS3UrlPrefix)
  }

  "The `getContentUrl` method" should "return a signed url for a provided key" in {
    val fileManager            = application.injector.instanceOf[FileManager]
    val result: Future[String] = fileManager.getContentUrl("testFile")

    result.futureValue must startWith(expectedS3UrlPrefix)
  }

  "The `deleteContents` method" should "return quietly when deleting any file" in {
    val fileManager = application.injector.instanceOf[FileManager]

    try fileManager.deleteContents("deleteFile")
    catch {
      case e: Exception => fail(e)
    }
  }

  "The `getFileSize` method" should "return 0 for files that do not exist" in {
    val fileManager          = application.injector.instanceOf[FileManager]
    val result: Future[Long] = fileManager.getFileSize("nonExistentFile")

    result.futureValue mustBe 0L
  }

}
