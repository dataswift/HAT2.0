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

import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.ObjectMetadata
import org.scalamock.scalatest.MockFactory

case class FileManagerS3Mock() extends MockFactory {
  // TOOD: Move this to localstack test container
  val mockS3client: AmazonS3 = stub[AmazonS3]

  private val s3ObjectMetadata = new ObjectMetadata()
  s3ObjectMetadata.setContentLength(123456L)

  // TODO: Fails in CI due to AWS Creds
  (mockS3client
    .getObjectMetadata(_: String, _: String))
    .when("hat-storage-test", "hat.hubofallthings.net/testFile")
    .returning(s3ObjectMetadata)
//  when(mockS3client.deleteObject("hat-storage-test", "hat.hubofallthings.net/deleteFile")).thenReturn(None)
}
