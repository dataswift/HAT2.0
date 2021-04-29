package org.hatdex.hat.api.service

import io.dataswift.models.hat.applications.HatApplication
import io.dataswift.models.hat.{ ApiHatFile, HatFileStatus, ManageFiles, Owner }
import org.hatdex.hat.api.repository.FileMetadataRepository
import org.hatdex.hat.authentication.models.HatUser
import org.hatdex.hat.authentication.{ ContainsApplicationRole, HatApiAuthEnvironment, WithRole }
import org.hatdex.hat.resourceManagement.HatServer
import org.hatdex.libs.dal.HATPostgresProfile.api.Database
import org.joda.time.DateTime
import play.api.Logging

import java.io.FileNotFoundException
import java.net.URL
import javax.inject.Inject
import scala.concurrent.{ ExecutionContext, Future }

final class FileNotAuthorisedException(fileId: String)
    extends RuntimeException(s"File $fileId not available - unauthorised or incomplete")

class FileUploadServiceImpl @Inject() (
    fileMetadataRepository: FileMetadataRepository,
    fileManager: FileManager
  )(implicit ec: ExecutionContext)
    extends FileUploadService
    with Logging {

  override def startUpload(
      file: ApiHatFile,
      user: HatUser
    )(implicit hatServer: HatServer): Future[ApiHatFile] = {
    implicit val db: Database = hatServer.db
    val cleanFile = file.copy(
      fileId = None,
      status = Some(HatFileStatus.New()),
      contentUrl = None,
      contentPublic = Some(false),
      permissions = None
    )
    for {
      fileWithId <- fileMetadataRepository.getUniqueFileId(cleanFile)
      savedFile <- fileMetadataRepository.save(fileWithId)
      uploadUrl <- fileManager.getUploadUrl(
                     fileWithId.fileId.get,
                     fileWithId.contentType
                   )
      _ <- fileMetadataRepository.grantAccess(savedFile, user, content = true)
      file <- fileMetadataRepository.getById(savedFile.fileId.get).map(_.get)
    } yield file.copy(contentUrl = Some(uploadUrl))
  }

  override def completeUpload(
      fileId: String,
      user: HatUser,
      maybeApplication: Option[HatApplication]
    )(implicit hatServer: HatServer,
      maybeAuthenticator: Option[HatApiAuthEnvironment#A]): Future[ApiHatFile] = {
    implicit val db: Database = hatServer.db
    fileMetadataRepository.getById(fileId) flatMap {
      case Some(file) if fileContentAccessAllowed(user, file, maybeApplication, checkComplete = false) =>
        logger.info(s"Marking $file complete")
        for {
          fileSize <- fileManager.getFileSize(fileId) if fileSize > 0
          savedFile <- fileMetadataRepository.save(file.copy(status = Some(HatFileStatus.Completed(fileSize))))
        } yield savedFile
      case Some(file) =>
        logger.info(s"Not marking $fileId complete, access forbidden")
        fileNotFound(fileId)
      case _ =>
        fileNotFound(fileId)
    }
  }

  override def update(
      file: ApiHatFile,
      user: HatUser,
      maybeApplication: Option[HatApplication]
    )(implicit hatServer: HatServer,
      maybeAuthenticator: Option[HatApiAuthEnvironment#A]): Future[ApiHatFile] =
    file.fileId map { id =>
      implicit val db: Database = hatServer.db
      fileMetadataRepository.getById(id) flatMap {
        case Some(dbFile) if fileContentAccessAllowed(user, file, maybeApplication) =>
          val updatedFile = dbFile.copy(
            name = file.name,
            lastUpdated = Some(DateTime.now()),
            tags = file.tags,
            title = file.title,
            description = file.description
          )
          fileMetadataRepository.save(updatedFile)
        case _ => fileNotFound(id)
      }
    } getOrElse Future.failed(new IllegalArgumentException("file id required"))

  override def getFile(
      fileId: String,
      user: HatUser,
      maybeApplication: Option[HatApplication]
    )(implicit hatServer: HatServer,
      maybeAuthenticator: Option[HatApiAuthEnvironment#A]): Future[ApiHatFile] = {
    implicit val db: Database = hatServer.db
    fileMetadataRepository.getById(fileId) flatMap {
      case Some(file) if fileContentAccessAllowed(user, file, maybeApplication) =>
        fileWithContentUrl(user, file)
      case Some(file) if fileAccessAllowed(user, file, maybeApplication) =>
        Future.successful(filePermissionsCleaned(user, file))
      case Some(_) => Future.failed(new FileNotAuthorisedException(fileId))
      case None    => fileNotFound(fileId)
    }
  }

  private def fileWithContentUrl(
      user: HatUser,
      file: ApiHatFile
    )(implicit hatServer: HatServer,
      maybeAuthenticator: Option[HatApiAuthEnvironment#A]): Future[ApiHatFile] =
    fileManager
      .getContentUrl(file.fileId.get)
      .map(url => file.copy(contentUrl = Some(url)))
      .map(filePermissionsCleaned(user, _))

  override def getContentUrl(
      fileId: String,
      maybeUser: Option[HatUser],
      maybeApplication: Option[HatApplication],
      maybeAuthenticator: Option[HatApiAuthEnvironment#A]
    )(implicit hatServer: HatServer): Future[URL] = {
    implicit val db: Database = hatServer.db
    fileMetadataRepository.getById(fileId) flatMap {
      case Some(file) if file.contentPublic.contains(true) => getFileUrl(fileId)
      case Some(file)                                      => getFileUrlIfAccessAllowed(file, maybeUser, maybeApplication, maybeAuthenticator)
      case _                                               => fileNotFound(fileId)
    }
  }

  override def fileAccessAllowed(
      user: HatUser,
      file: ApiHatFile,
      appStatus: Option[HatApplication]
    )(implicit maybeAuthenticator: Option[HatApiAuthEnvironment#A]): Boolean =
    file.permissions.exists(_.exists(_.userId == user.userId)) || isOwnerOrCanManage(user, file.source, appStatus)

  override def listFiles(
      user: HatUser,
      fileTemplate: ApiHatFile,
      appStatus: Option[HatApplication]
    )(implicit hatServer: HatServer,
      authenticator: HatApiAuthEnvironment#A): Future[Seq[ApiHatFile]] = {
    implicit val db: Database                          = hatServer.db
    implicit val auth: Option[HatApiAuthEnvironment#A] = Some(authenticator)
    fileMetadataRepository
      .search(fileTemplate)
      .map(_.filter(fileAccessAllowed(user, _, appStatus)))
      .flatMap { foundFiles =>
        Future
          .traverse(foundFiles) { file =>
            if (fileContentAccessAllowed(user, file, appStatus))
              fileWithContentUrl(user, file)
            else
              Future.successful(filePermissionsCleaned(user, file))
          }
      }
  }

  override def delete(fileId: String)(implicit hatServer: HatServer): Future[ApiHatFile] = {
    implicit val db: Database = hatServer.db
    fileMetadataRepository.getById(fileId) flatMap {
      case Some(file) =>
        for {
          _ <- fileManager.deleteContents(fileId)
          deleted <- fileMetadataRepository.save(file.copy(status = Some(HatFileStatus.Deleted())))
        } yield deleted
      case _ => fileNotFound(fileId)
    }
  }

  private def fileNotFound(fileId: String): Future[Nothing] =
    Future.failed(new FileNotFoundException(s"File $fileId not found"))

  private def getFileUrl(fileId: String)(implicit hatServer: HatServer): Future[URL] =
    fileManager.getContentUrl(fileId).map(s => new URL(s))

  private def getFileUrlIfAccessAllowed(
      file: ApiHatFile,
      maybeUser: Option[HatUser],
      maybeApplication: Option[HatApplication],
      maybeAuthenticator: Option[HatApiAuthEnvironment#A]
    )(implicit hatServer: HatServer): Future[URL] = {
    val fileId = file.fileId.get
    val maybeUrl = for {
      user <- maybeUser
      authenticator <- maybeAuthenticator
    } yield
      if (fileContentAccessAllowed(user, file, maybeApplication)(Some(authenticator)))
        getFileUrl(fileId)
      else
        fileNotFound(fileId)
    maybeUrl.getOrElse(fileNotFound(fileId))
  }

  private def isOwnerOrCanManage(
      user: HatUser,
      source: String,
      appStatus: Option[HatApplication]
    )(implicit maybeAuthenticator: Option[HatApiAuthEnvironment#A]): Boolean =
    maybeAuthenticator exists { authenticator =>
      appStatus.exists(
        ContainsApplicationRole.isAuthorized(
          user,
          _,
          authenticator,
          ManageFiles(source)
        )
      ) || WithRole.isAuthorized(user, authenticator, Owner())
    }

  private def fileContentAccessAllowed(
      user: HatUser,
      file: ApiHatFile,
      appStatus: Option[HatApplication],
      checkComplete: Boolean = true
    )(implicit maybeAuthenticator: Option[HatApiAuthEnvironment#A]): Boolean = {
    val statusOk = if (checkComplete) file.status.exists(_.isInstanceOf[HatFileStatus.Completed]) else true
    (statusOk && file.permissions.exists(_.exists { p =>
      logger.info(s"permission $p")
      p.userId == user.userId && p.contentReadable
    })) ||
    isOwnerOrCanManage(user, "*", appStatus)
  }

  private def filePermissionsCleaned(
      user: HatUser,
      file: ApiHatFile
    )(implicit maybeAuthenticator: Option[HatApiAuthEnvironment#A]): ApiHatFile =
    if (maybeAuthenticator.exists(WithRole.isAuthorized(user, _, Owner())))
      file
    else
      file.copy(permissions = None)

}
