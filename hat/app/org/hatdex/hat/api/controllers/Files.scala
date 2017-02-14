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

package org.hatdex.hat.api.controllers

import javax.inject.Inject

import com.mohiva.play.silhouette.api.Silhouette
import com.mohiva.play.silhouette.api.util.Clock
import org.hatdex.hat.api.json.HatJsonFormats
import org.hatdex.hat.api.models.{ ApiHatFile, ErrorMessage, HatFileStatus }
import org.hatdex.hat.api.service.{ FileManager, FileMetadataService }
import org.hatdex.hat.authentication.{ HatApiAuthEnvironment, HatApiController }
import org.hatdex.hat.resourceManagement._
import org.joda.time.DateTime
import play.api.i18n.MessagesApi
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.json.Json
import play.api.mvc._
import play.api.{ Configuration, Logger }

import scala.concurrent.Future

class Files @Inject() (
    val messagesApi: MessagesApi,
    configuration: Configuration,
    silhouette: Silhouette[HatApiAuthEnvironment],
    hatServerProvider: HatServerProvider,
    clock: Clock,
    hatDatabaseProvider: HatDatabaseProvider,
    fileMetadataService: FileMetadataService,
    fileManager: FileManager
) extends HatApiController(silhouette, clock, hatServerProvider, configuration) with HatJsonFormats {

  val logger = Logger(this.getClass)

  def startUpload: Action[ApiHatFile] = SecuredAction.async(BodyParsers.parse.json[ApiHatFile]) { implicit request =>
    val eventualUploadUrl = for {
      fileWithId <- fileMetadataService.getUniqueFileId(request.body.copy(fileId = None, status = Some(HatFileStatus.New), contentUrl = None))
      savedFile <- fileMetadataService.save(fileWithId)
      uploadUrl <- fileManager.getUploadUrl(fileWithId.fileId.get)
    } yield (savedFile, uploadUrl)

    eventualUploadUrl.map { case (savedFile, url) => Ok(Json.toJson(savedFile.copy(contentUrl = Some(url)))) }
      .recoverWith {
        case e =>
          logger.error(s"Error uploading file: ${e.getMessage}")
          Future.successful(BadRequest(Json.toJson(ErrorMessage("Error uploading file", e.getMessage))))
      }
  }

  def completeUpload(fileId: String): Action[AnyContent] = SecuredAction.async { implicit request =>
    fileMetadataService.getById(fileId) flatMap {
      case Some(file) =>
        (for {
          exists <- fileManager.fileExists(file.fileId.get)
          completed <- fileMetadataService.save(file.copy(status = Some(HatFileStatus.Completed))) if exists
        } yield {
          Ok(Json.toJson(completed))
        }) recover {
          case _ => BadRequest(Json.toJson(ErrorMessage("File not available", s"Not fully uploaded file can not be completed")))
        }

      case None =>
        Future.successful(NotFound(Json.toJson(ErrorMessage("No such file", s"File $fileId not found"))))
    }
  }

  def getContents(fileId: String): Action[AnyContent] = SecuredAction.async { implicit request =>
    fileMetadataService.getById(fileId) flatMap {
      case Some(file) if file.status.contains(HatFileStatus.Completed) =>
        fileManager.getContentUrl(file.fileId.get).map(url => Ok(Json.toJson(file.copy(contentUrl = Some(url)))))
      case Some(file) =>
        logger.debug(s"Found file $file")
        Future.successful(NotFound(Json.toJson(ErrorMessage("File not available", s"File $fileId not available - has uploading the file been completed?"))))
      case None =>
        Future.successful(NotFound(Json.toJson(ErrorMessage("No such file", s"File $fileId not found"))))
    }
  }

  def listFiles(): Action[ApiHatFile] = SecuredAction.async(BodyParsers.parse.json[ApiHatFile]) { implicit request =>
    fileMetadataService.search(request.body) flatMap { foundFiles =>
      val fileContentsEventual = foundFiles map { file =>
        if (file.status.contains(HatFileStatus.Completed)) {
          fileManager.getContentUrl(file.fileId.get).map(url => file.copy(contentUrl = Some(url)))
        }
        else {
          Future.successful(file)
        }
      }
      Future.sequence(fileContentsEventual).map(files => Ok(Json.toJson(files)))
    }
  }

  def updateFile(fileId: String): Action[ApiHatFile] = SecuredAction.async(BodyParsers.parse.json[ApiHatFile]) { implicit request =>
    fileMetadataService.getById(fileId) flatMap {
      case Some(file) =>
        val updatedFile = file.copy(
          name = request.body.name,
          lastUpdated = request.body.lastUpdated.orElse(Some(DateTime.now())),
          tags = request.body.tags,
          title = request.body.title,
          description = request.body.description)

        fileMetadataService.save(updatedFile).map(f => Ok(Json.toJson(f)))
      case None =>
        Future.successful(NotFound(Json.toJson(ErrorMessage("No such file", s"File $fileId not found"))))
    }
  }

  def deleteFile(fileId: String): Action[AnyContent] = SecuredAction.async { implicit request =>
    fileMetadataService.getById(fileId) flatMap {
      case Some(file) =>
        val eventuallyDeleted = for {
          _ <- fileManager.deleteContents(file.fileId.get)
          deleted <- fileMetadataService.save(file.copy(status = Some(HatFileStatus.Deleted)))
        } yield deleted
        eventuallyDeleted.map(file => Ok(Json.toJson(file)))
      case None =>
        Future.successful(NotFound(Json.toJson(ErrorMessage("No such file", s"File $fileId not found"))))
    }
  }

}

