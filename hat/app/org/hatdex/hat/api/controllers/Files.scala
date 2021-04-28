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

import com.mohiva.play.silhouette.api.Silhouette
import io.dataswift.models.hat._
import io.dataswift.models.hat.json.HatJsonFormats
import org.hatdex.hat.api.repository.FileMetadataRepository
import org.hatdex.hat.api.service.applications.ApplicationsService
import org.hatdex.hat.api.service.{ FileNotAuthorisedException, FileUploadService, UserService }
import org.hatdex.hat.authentication.{ ContainsApplicationRole, HatApiAuthEnvironment, HatApiController, WithRole }
import org.hatdex.hat.utils.HatBodyParsers
import play.api.Logging
import play.api.libs.json.Json
import play.api.mvc._

import java.io.FileNotFoundException
import java.util.UUID
import javax.inject.Inject
import scala.concurrent.{ ExecutionContext, Future }

class Files @Inject() (
    components: ControllerComponents,
    parsers: HatBodyParsers,
    silhouette: Silhouette[HatApiAuthEnvironment],
    fileUploadService: FileUploadService,
    fileMetadataRepository: FileMetadataRepository,
    userService: UserService
  )(implicit ec: ExecutionContext,
    applicationsService: ApplicationsService)
    extends HatApiController(components, silhouette)
    with Logging {
  import HatJsonFormats._

  def startUpload: Action[ApiHatFile] =
    SecuredAction(
      WithRole(DataCredit(""), Owner()) || ContainsApplicationRole(
          ManageFiles("*"),
          Owner()
        )
    ).async(parsers.json[ApiHatFile]) { implicit request =>
      fileUploadService
        .startUpload(request.body, request.identity)
        .map(file => Ok(Json.toJson(file)))
        .recoverWith {
          case e =>
            logger.error(s"Error uploading file: ${e.getMessage}", e)
            Future.successful(BadRequest(Json.toJson(ErrorMessage("Error uploading file", e.getMessage))))
        }
    }

  def completeUpload(fileId: String): Action[AnyContent] =
    SecuredAction(
      WithRole(DataCredit(""), Owner()) || ContainsApplicationRole(
          ManageFiles("*"),
          Owner()
        )
    ).async { implicit request =>
      request2ApplicationStatus(request) flatMap { maybeApplication =>
        fileUploadService.completeUpload(fileId, request.identity, maybeApplication).map { completed =>
          Ok(Json.toJson(completed))
        } recover {
          case notFound: FileNotFoundException =>
            NotFound(Json.toJson(ErrorMessage("No such file", notFound.getMessage)))
          case e =>
            logger.warn(s"Could not complete file upload: ${e.getMessage}")
            BadRequest(
              Json.toJson(
                ErrorMessage(
                  "File not available",
                  "Not fully uploaded file can not be completed"
                )
              )
            )
        }
      }
    }

  def getDetail(fileId: String): Action[AnyContent] =
    SecuredAction.async { implicit request =>
      request2ApplicationStatus(request) flatMap { maybeApplication =>
        fileUploadService
          .getFile(fileId, request.identity, maybeApplication)
          .map(file => Ok(Json.toJson(file)))
          .recover {
            case noAuthEx: FileNotAuthorisedException =>
              NotFound(Json.toJson(ErrorMessage("File not available", noAuthEx.getMessage)))
            case notFound: FileNotFoundException =>
              NotFound(Json.toJson(ErrorMessage("No such file", notFound.getMessage)))
            case th =>
              logger.warn(s"Error fetching file '$fileId': ${th.getMessage}")
              BadRequest(Json.toJson(ErrorMessage("Error fetching file", "Unexpected exception")))
          }
      }
    }

  def getContent(fileId: String): Action[AnyContent] =
    UserAwareAction.async { implicit request =>
      request2ApplicationStatus(request) flatMap { maybeApplication =>
        fileUploadService
          .getContentUrl(fileId, request.identity, maybeApplication, request.authenticator)
          .map(url => Redirect(url.toString))
          .recover(_ => NotFound)
      }
    }

  def listFiles(): Action[ApiHatFile] =
    SecuredAction.async(parsers.json[ApiHatFile]) { implicit request =>
      request2ApplicationStatus(request) flatMap { maybeApplication =>
        fileUploadService
          .listFiles(request.identity, request.body, maybeApplication)
          .map(files => Ok(Json.toJson(files)))
          .recover {
            case th =>
              logger.warn(s"Error listing files: ${th.getMessage}")
              BadRequest(Json.toJson(ErrorMessage("Error listing file", "Unexpected exception")))
          }
      }
    }

  def updateFile(fileId: String): Action[ApiHatFile] =
    SecuredAction.async(parsers.json[ApiHatFile]) { implicit request =>
      request2ApplicationStatus(request) flatMap { maybeApplication =>
        fileUploadService
          .update(request.body, request.identity, maybeApplication)
          .map(file => Ok(Json.toJson(file)))
          .recover {
            case notFound: FileNotFoundException =>
              NotFound(Json.toJson(ErrorMessage("No such file", notFound.getMessage)))
            case th =>
              logger.error(s"Error updating file '$fileId': ${th.getMessage}")
              BadRequest(Json.toJson(ErrorMessage("Error updating file", "Unexpected exception")))
          }
      }
    }

  def deleteFile(fileId: String): Action[AnyContent] =
    SecuredAction(
      WithRole(Owner()) || ContainsApplicationRole(Owner(), ManageFiles("*"))
    ).async { implicit request =>
      fileUploadService
        .delete(fileId)
        .map(file => Ok(Json.toJson(file)))
        .recover {
          case notFound: FileNotFoundException =>
            NotFound(Json.toJson(ErrorMessage("No such file", notFound.getMessage)))
          case th =>
            logger.error(s"Error deleting file '$fileId': ${th.getMessage}")
            BadRequest(Json.toJson(ErrorMessage("Error deleting file", "Unexpected exception")))
        }
    }

  def allowAccess(
      fileId: String,
      userId: UUID,
      content: Boolean): Action[AnyContent] =
    SecuredAction(
      WithRole(Owner()) || ContainsApplicationRole(Owner(), ManageFiles("*"))
    ).async { implicit request =>
      request2ApplicationStatus(request) flatMap { maybeAsApplication =>
        fileMetadataRepository.getById(fileId) flatMap {
          case Some(file) if fileUploadService.fileAccessAllowed(request.identity, file, maybeAsApplication) =>
            val eventuallyGranted = for {
              user <- userService.getUser(userId) if user.isDefined
              _ <- fileMetadataRepository.grantAccess(file, user.get, content)
              updated <- fileMetadataRepository.getById(fileId).map(_.get)
            } yield updated
            eventuallyGranted.map(f => Ok(Json.toJson(f)))
          case _ =>
            Future.successful(
              NotFound(
                Json.toJson(
                  ErrorMessage("No such file", s"File $fileId not found")
                )
              )
            )
        }
      }
    }

  def restrictAccess(
      fileId: String,
      userId: UUID): Action[AnyContent] =
    SecuredAction(
      WithRole(Owner()) || ContainsApplicationRole(Owner(), ManageFiles("*"))
    ).async { implicit request =>
      request2ApplicationStatus(request) flatMap { maybeAsApplication =>
        fileMetadataRepository.getById(fileId) flatMap {
          case Some(file) if fileUploadService.fileAccessAllowed(request.identity, file, maybeAsApplication) =>
            val eventuallyGranted = for {
              user <- userService.getUser(userId) if user.isDefined
              _ <- fileMetadataRepository.restrictAccess(file, user.get)
              updated <- fileMetadataRepository.getById(fileId).map(_.get)
            } yield updated
            eventuallyGranted.map(f => Ok(Json.toJson(f)))
          case _ =>
            Future.successful(
              NotFound(
                Json.toJson(
                  ErrorMessage("No such file", s"File $fileId not found")
                )
              )
            )
        }
      }
    }

  def changePublicAccess(
      fileId: String,
      public: Boolean): Action[AnyContent] =
    SecuredAction(
      WithRole(Owner()) || ContainsApplicationRole(Owner(), ManageFiles("*"))
    ).async { implicit request =>
      request2ApplicationStatus(request) flatMap { maybeAsApplication =>
        fileMetadataRepository.getById(fileId) flatMap {
          case Some(file) if fileUploadService.fileAccessAllowed(request.identity, file, maybeAsApplication) =>
            val eventuallyGranted = for {
              _ <- fileMetadataRepository.save(
                     file.copy(contentPublic = Some(public))
                   )
              updated <- fileMetadataRepository.getById(fileId).map(_.get)
            } yield updated
            eventuallyGranted.map(f => Ok(Json.toJson(f)))
          case _ =>
            Future.successful(
              NotFound(
                Json.toJson(
                  ErrorMessage("No such file", s"File $fileId not found")
                )
              )
            )
        }
      }
    }

  def allowAccessPattern(
      userId: UUID,
      content: Boolean): Action[ApiHatFile] =
    SecuredAction(
      WithRole(Owner()) || ContainsApplicationRole(Owner(), ManageFiles("*"))
    ).async(parsers.json[ApiHatFile]) { implicit request =>
      val eventuallyAllowedFiles = for {
        user <- userService.getUser(userId) if user.isDefined
        _ <- fileMetadataRepository.grantAccessPattern(
               request.body,
               user.get,
               content
             )
        matchingFiles <- fileMetadataRepository.search(request.body)
      } yield matchingFiles

      eventuallyAllowedFiles map { files =>
        Ok(Json.toJson(files))
      } recover {
        case e =>
          logger.error(
            s"Error while granting access to a pattern of files: ${e.getMessage}",
            e
          )
          BadRequest(
            Json.toJson(
              ErrorMessage(
                "Could not grant access",
                "No such user or unexpected error while granting access"
              )
            )
          )
      }
    }

  def restrictAccessPattern(userId: UUID): Action[ApiHatFile] =
    SecuredAction(
      WithRole(Owner()) || ContainsApplicationRole(Owner(), ManageFiles("*"))
    ).async(parsers.json[ApiHatFile]) { implicit request =>
      val eventuallyAllowedFiles = for {
        user <- userService.getUser(userId) if user.isDefined
        _ <- fileMetadataRepository.restrictAccessPattern(request.body, user.get)
        matchingFiles <- fileMetadataRepository.search(request.body)
      } yield matchingFiles

      eventuallyAllowedFiles map { files =>
        Ok(Json.toJson(files))
      } recover {
        case e =>
          logger.error(
            s"Error while granting access to a pattern of files: ${e.getMessage}",
            e
          )
          BadRequest(
            Json.toJson(
              ErrorMessage(
                "Could not grant access",
                "No such user or unexpected error while granting access"
              )
            )
          )
      }
    }

}
