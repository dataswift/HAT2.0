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

import java.text.Normalizer

import org.hatdex.hat.api.json.HatJsonFormats
import org.hatdex.hat.api.models.{ ApiHatFile, HatFileStatus }
import org.hatdex.hat.authentication.models.HatUser
import org.hatdex.hat.dal.ModelTranslation
import org.hatdex.hat.dal.SlickPostgresDriver.api._
import org.hatdex.hat.dal.Tables._
import org.joda.time.LocalDateTime
import play.api.Logger
import play.api.libs.json.Json

import scala.concurrent.Future

class FileMetadataService extends DalExecutionContext {
  val logger = Logger(this.getClass)

  def getUniqueFileId(file: ApiHatFile)(implicit db: Database): Future[ApiHatFile] = {
    val fileExtension = file.name.split('.').drop(1).lastOption.map(ext => s".$ext").getOrElse("")
    val fileName = file.name.split('.').takeWhile(p => s".$p" != fileExtension).mkString("")
    val fullNameNormalized = Normalizer.normalize(file.source + fileName, Normalizer.Form.NFD)
      .replaceAll("[^\\w ]", "")
      .replace(" ", "-")
      .toLowerCase

    val similarFilesSearch = HatFile.filter(t => t.id like s"$fullNameNormalized%$fileExtension")
    val eventualUniqueFilename = db.run(similarFilesSearch.result).map(_.map(_.id)).map(_.toList) map { similarFileNames =>
      if (!similarFileNames.contains(fullNameNormalized + fileExtension)) {
        fullNameNormalized + fileExtension
      }
      else {
        val endsWithNumber = s"(.+-)([0-9]+)$fileExtension".r
        val suffixes = similarFileNames.map {
          case endsWithNumber(_, number) => number.toInt
          case _                         => 0
        }
        s"$fullNameNormalized-${suffixes.max + 1}$fileExtension"
      }
    }

    eventualUniqueFilename map { fn =>
      file.copy(fileId = Some(fn))
    }
  }

  def save(file: ApiHatFile)(implicit db: Database): Future[ApiHatFile] = {
    logger.info(s"Saving file $file")
    import HatJsonFormats.apiHatFileStatusFormat
    val dbFile = HatFileRow(file.fileId.get, file.name, file.source,
      file.dateCreated.map(_.toLocalDateTime).getOrElse(LocalDateTime.now()),
      file.lastUpdated.map(_.toLocalDateTime).getOrElse(LocalDateTime.now()),
      file.tags.map(_.toList), file.title, file.description, file.sourceURL, Json.toJson(file.status))

    val updatedFileQuery = HatFile.joinLeft(HatFileAccess).on(_.id === _.fileId).filter(_._1.id === file.fileId.get)

    val fileQuery = (HatFile returning HatFile).insertOrUpdate(dbFile)
      .andThen(updatedFileQuery.result)

    val result = db.run(fileQuery)
      .map(groupFilePermissions)
      .map(_.head)

    result.onFailure {
      case e =>
        logger.error(s"Error while saving file: ${e.getMessage}", e)
    }
    result
  }

  private def groupFilePermissions(files: Seq[(HatFileRow, Option[HatFileAccessRow])]): Iterable[ApiHatFile] = {
    files.groupBy(_._1).map {
      case (file, permissions) =>
        ModelTranslation.fromDbModel(file)
          .copy(permissions = Some(permissions.unzip._2.flatten.map(ModelTranslation.fromDbModel)))
    }
  }

  def grantAccess(file: ApiHatFile, user: HatUser, content: Boolean)(implicit db: Database): Future[Unit] = {
    val filePermissionRow = HatFileAccessRow(file.fileId.get, user.userId, content)
    logger.debug(s"Inserting file permissions: $filePermissionRow")
    db.run(HatFileAccess.forceInsert(filePermissionRow)).map(_ => ())
  }

  def grantAccessPattern(fileTemplate: ApiHatFile, user: HatUser, content: Boolean)(implicit db: Database): Future[Unit] = {
    val matchingFileQuery = for {
      file <- findFilesQuery(fileTemplate)
    } yield file.id

    db.run(matchingFileQuery.result) flatMap { fileIds =>
      val accessRows = fileIds map { fileId =>
        HatFileAccessRow(fileId, user.userId, content)
      }
      db.run(HatFileAccess.forceInsertAll(accessRows))
    } map { _ => () }
  }

  def delete(fileId: String)(implicit db: Database): Future[ApiHatFile] = {
    import HatJsonFormats.apiHatFileStatusFormat
    val query = for {
      _ <- HatFile.filter(_.id === fileId)
        .map(v => (v.status, v.lastUpdated))
        .update((Json.toJson(HatFileStatus.Deleted()), LocalDateTime.now()))
      updatedFile <- HatFile.filter(_.id === fileId).result
    } yield updatedFile
    db.run(query).map(updated => ModelTranslation.fromDbModel(updated.head))
  }

  private def findFilesQuery(fileTemplate: ApiHatFile): Query[HatFile, HatFileRow, Seq] = {
    HatFile.filter { t =>
      Some(fileTemplate.name).filterNot(_.isEmpty).fold(true.bind)(tsVector(t.name) @@ tsQuery(_)) &&
        Some(fileTemplate.source).filterNot(_.isEmpty).fold(true.bind)(tsVector(t.source) @@ tsQuery(_)) &&
        fileTemplate.status.fold(true.bind)(t.status.+>>("status") === _.status) &&
        fileTemplate.title.fold(true.bind)(tsVector(t.title.getOrElse("")) @@ tsQuery(_)) &&
        fileTemplate.description.fold(true.bind)(tsVector(t.description.getOrElse("")) @@ tsQuery(_)) &&
        fileTemplate.tags.fold(true.bind)(t.tags.getOrElse(List[String]()) @> _.toList)
    }
  }

  def getById(fileId: String)(implicit db: Database): Future[Option[ApiHatFile]] = {
    val fileQuery = HatFile.joinLeft(HatFileAccess).on(_.id === _.fileId).filter(_._1.id === fileId)

    db.run(fileQuery.result)
      .map(groupFilePermissions)
      .map(_.headOption)
  }

  def search(fileTemplate: ApiHatFile)(implicit db: Database): Future[Seq[ApiHatFile]] = {
    val fileQuery = for {
      file <- findFilesQuery(fileTemplate).joinLeft(HatFileAccess).on(_.id === _.fileId)
    } yield file

    db.run(fileQuery.result)
      .map(groupFilePermissions)
      .map(_.toSeq)
  }
}
