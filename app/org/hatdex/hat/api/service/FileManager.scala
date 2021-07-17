package org.hatdex.hat.api.service

import org.hatdex.hat.resourceManagement.HatServer

import scala.concurrent.Future

trait FileManager {

  def getUploadUrl(
      filename: String,
      maybeContentType: Option[String]
    )(implicit hatServer: HatServer): Future[String]

  def getContentUrl(filename: String)(implicit hatServer: HatServer): Future[String]

  def getFileSize(fileName: String)(implicit hatServer: HatServer): Future[Long]

  def deleteContents(filename: String)(implicit hatServer: HatServer): Future[Unit]

}
