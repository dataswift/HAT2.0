package org.hatdex.hat.api.controllers.v2

import org.hatdex.hat.api.controllers.common._
import play.api.mvc.Action

trait ContractFiles {

  def startUpload: Action[ContractFile]

  def completeUpload(fileId: String): Action[ContractDataReadRequest]

  def getDetail(fileId: String): Action[ContractDataReadRequest]

  def getContent(fileId: String): Action[ContractDataReadRequest]

  def updateFile(fileId: String): Action[ContractFile]

  def deleteFile(fileId: String): Action[ContractDataReadRequest]
}
