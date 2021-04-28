package org.hatdex.hat.api.service

import io.dataswift.models.hat.ApiHatFile
import io.dataswift.models.hat.applications.HatApplication
import org.hatdex.hat.authentication.HatApiAuthEnvironment
import org.hatdex.hat.authentication.models.HatUser
import org.hatdex.hat.resourceManagement.HatServer

import java.net.URL
import scala.concurrent.Future

trait FileUploadService {

  def startUpload(
      file: ApiHatFile,
      user: HatUser
    )(implicit hatServer: HatServer): Future[ApiHatFile]

  def completeUpload(
      fileId: String,
      user: HatUser,
      maybeApplication: Option[HatApplication]
    )(implicit hatServer: HatServer,
      authenticator: HatApiAuthEnvironment#A): Future[ApiHatFile]

  def update(
      file: ApiHatFile,
      user: HatUser,
      maybeApplication: Option[HatApplication]
    )(implicit hatServer: HatServer,
      authenticator: HatApiAuthEnvironment#A): Future[ApiHatFile]

  def getFile(
      fileId: String,
      user: HatUser,
      maybeApplication: Option[HatApplication]
    )(implicit hatServer: HatServer,
      authenticator: HatApiAuthEnvironment#A): Future[ApiHatFile]

  def getContentUrl(
      fileId: String,
      maybeUser: Option[HatUser],
      maybeApplication: Option[HatApplication],
      authenticator: Option[HatApiAuthEnvironment#A]
    )(implicit hatServer: HatServer): Future[URL]

  def fileAccessAllowed(
      user: HatUser,
      file: ApiHatFile,
      appStatus: Option[HatApplication]
    )(implicit authenticator: HatApiAuthEnvironment#A): Boolean

  def listFiles(
      user: HatUser,
      fileTemplate: ApiHatFile,
      appStatus: Option[HatApplication]
    )(implicit hatServer: HatServer,
      authenticator: HatApiAuthEnvironment#A): Future[Seq[ApiHatFile]]
}
