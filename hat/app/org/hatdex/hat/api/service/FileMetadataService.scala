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
import javax.inject.Inject

import org.hatdex.hat.api.models.{ ApiHatFile, HatFileStatus }
import org.hatdex.hat.dal.ModelTranslation
import org.hatdex.hat.dal.SlickPostgresDriver.api._
import org.hatdex.hat.dal.Tables._
import org.joda.time.LocalDateTime
import play.api.Logger

import scala.concurrent.Future

class FileMetadataService @Inject() () extends DalExecutionContext {
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
    val dbFile = HatFileRow(file.fileId.get, file.name, file.source,
      file.dateCreated.map(_.toLocalDateTime).getOrElse(LocalDateTime.now()),
      file.lastUpdated.map(_.toLocalDateTime).getOrElse(LocalDateTime.now()),
      file.tags.map(_.toList), file.title, file.description, file.sourceURL, file.status.map(_.toString).getOrElse(""))

    db.run {
      for {
        _ <- (HatFile returning HatFile).insertOrUpdate(dbFile)
        updated <- HatFile.filter(_.id === file.fileId.get).result
      } yield updated
    } map { file =>
      logger.debug(s"eh? $file")
      ModelTranslation.fromDbModel(file.head)
    }
  }

  def delete(fileId: String)(implicit db: Database): Future[ApiHatFile] = {
    val query = for {
      _ <- HatFile.filter(_.id === fileId)
        .map(v => (v.status, v.lastUpdated))
        .update((HatFileStatus.Deleted.toString, LocalDateTime.now()))
      updatedFile <- HatFile.filter(_.id === fileId).result
    } yield updatedFile
    db.run(query).map(updated => ModelTranslation.fromDbModel(updated.head))
  }

  def getById(fileId: String)(implicit db: Database): Future[Option[ApiHatFile]] = {
    db.run(HatFile.filter(_.id === fileId).result)
      .map(_.headOption.map(ModelTranslation.fromDbModel))
  }

  def search(fileTemplate: ApiHatFile)(implicit db: Database): Future[Seq[ApiHatFile]] = {
    val searchQuery = HatFile.filter { t =>
      if (fileTemplate.name.nonEmpty) {
        tsVector(t.name) @@ tsQuery(fileTemplate.name)
      }
      else {
        slick.lifted.LiteralColumn(true)
      } &&
        (if (fileTemplate.source.nonEmpty) {
          tsVector(t.source) @@ tsQuery(fileTemplate.source)
        }
        else {
          slick.lifted.LiteralColumn(true)
        }) &&
        (fileTemplate.status map {
          status => t.status === status.toString
        } getOrElse slick.lifted.LiteralColumn(true)) &&
        (fileTemplate.title map {
          title => tsVector(t.title.getOrElse("")) @@ tsQuery(title)
        } getOrElse slick.lifted.LiteralColumn(true)) &&
        (fileTemplate.description map {
          description => tsVector(t.description.getOrElse("")) @@ tsQuery(description)
        } getOrElse slick.lifted.LiteralColumn(true)) &&
        (fileTemplate.tags map {
          tags => t.tags.getOrElse(List[String]()) @> tags.toList
        } getOrElse slick.lifted.LiteralColumn(true))
    }

    db.run(searchQuery.result)
      .map(r => r.map(ModelTranslation.fromDbModel))
  }
}
