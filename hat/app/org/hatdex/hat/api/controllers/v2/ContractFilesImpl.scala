package org.hatdex.hat.api.controllers.v2

import com.mohiva.play.silhouette.api.Silhouette
import io.dataswift.models.hat.ErrorMessage
import io.dataswift.models.hat.json.HatJsonFormats
import org.hatdex.hat.api.controllers.common._
import org.hatdex.hat.api.service.{ FileNotAuthorisedException, FileUploadService }
import org.hatdex.hat.authentication.{ HatApiAuthEnvironment, HatApiController }
import org.hatdex.hat.utils.HatBodyParsers
import play.api.Logging
import play.api.libs.json.Json
import play.api.mvc.{ Action, ControllerComponents }

import java.io.FileNotFoundException
import javax.inject.Inject
import scala.concurrent.{ ExecutionContext, Future }

class ContractFilesImpl @Inject() (
    components: ControllerComponents,
    parsers: HatBodyParsers,
    silhouette: Silhouette[HatApiAuthEnvironment],
    contractAction: ContractAction,
    fileUploadService: FileUploadService
  )(implicit ec: ExecutionContext)
    extends HatApiController(components, silhouette)
    with ContractFiles
    with Logging {

  import HatJsonFormats._

  def startUpload: Action[ContractFile] =
    contractAction.doWithToken(parsers.json[ContractFile], None, permissions = Write) {
      (contractFile, user, hatServer, _) =>
        fileUploadService
          .startUpload(contractFile.file, user)(hatServer)
          .map(file => Ok(Json.toJson(file)))
          .recoverWith {
            case e =>
              logger.error(s"Error uploading file: ${e.getMessage}", e)
              Future.successful(BadRequest(Json.toJson(ErrorMessage("Error uploading file", e.getMessage))))
          }
    }

  def completeUpload(fileId: String): Action[ContractDataReadRequest] =
    contractAction.doWithToken(parsers.json[ContractDataReadRequest], None, permissions = Write) {
      (_, user, hatServer, maybeAuthenticator) =>
        fileUploadService.completeUpload(fileId, user, None)(hatServer, maybeAuthenticator).map { completed =>
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

  def getDetail(fileId: String): Action[ContractDataReadRequest] =
    contractAction.doWithToken(parsers.json[ContractDataReadRequest], None, permissions = Read) {
      (_, user, hatServer, maybeAuthenticator) =>
        fileUploadService
          .getFile(fileId, user, None)(hatServer, maybeAuthenticator)
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

  def getContent(fileId: String): Action[ContractDataReadRequest] =
    contractAction.doWithToken(parsers.json[ContractDataReadRequest], None, permissions = Read) {
      (_, user, hatServer, maybeAuthenticator) =>
        fileUploadService
          .getContentUrl(fileId, Some(user), None, maybeAuthenticator)(hatServer)
          .map(url => Redirect(url.toString))
          .recover(_ => NotFound)
    }

  def updateFile(fileId: String): Action[ContractFile] =
    contractAction.doWithToken(parsers.json[ContractFile], None, permissions = Write) {
      (contractUpdate, user, hatServer, maybeAuthenticator) =>
        fileUploadService
          .update(contractUpdate.file, user, None)(hatServer, maybeAuthenticator)
          .map(file => Ok(Json.toJson(file)))
          .recover {
            case notFound: FileNotFoundException =>
              NotFound(Json.toJson(ErrorMessage("No such file", notFound.getMessage)))
            case th =>
              logger.error(s"Error updating file '$fileId': ${th.getMessage}")
              BadRequest(Json.toJson(ErrorMessage("Error updating file", "Unexpected exception")))
          }
    }

  def deleteFile(fileId: String): Action[ContractDataReadRequest] =
    contractAction.doWithToken(parsers.json[ContractDataReadRequest], None, permissions = Write) {
      (_, user, hatServer, maybeAuthenticator) =>
        val result =
          for {
            file <-
              fileUploadService.getFile(fileId, user, None, cleanPermissions = false)(hatServer, maybeAuthenticator)
            accessAllowed <- Future(fileUploadService.fileAccessAllowed(user, file, None)(maybeAuthenticator))
            if accessAllowed
            _ = accessAllowed // to work around incorrect compiler warning
            deleted <- fileUploadService.delete(fileId)(hatServer)
          } yield Ok(Json.toJson(deleted))
        result.recover {
          case notFound: FileNotFoundException =>
            NotFound(Json.toJson(ErrorMessage("No such file", notFound.getMessage)))
          case th =>
            logger.error(s"Error deleting file '$fileId': ${th.getMessage}")
            BadRequest(Json.toJson(ErrorMessage("Error deleting file", "Unexpected exception")))
        }
    }

}
