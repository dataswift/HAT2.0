/*
 * Copyright (C) 2016 Andrius Aucinas <andrius.aucinas@hatdex.org>
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
 */

package hatdex.hat.phata.service

import com.typesafe.config.{Config, ConfigFactory}
import hatdex.hat.FutureTransformations
import hatdex.hat.api.models.{ApiDataRecord, ApiDataTable, ProfileField}
import hatdex.hat.api.service.BundleService
import hatdex.hat.phata.models.{Notable, NotableAuthor, NotableLocation, NotablePhoto}
import org.joda.time.{DateTime, LocalDateTime}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Success, Try}
import org.pegdown.{Extensions, PegDownProcessor}

import scala.concurrent.duration._

trait NotablesService extends BundleService {
  val configuration: Config

  def getPublicNotes: Future[Seq[Notable]] = {
    val eventualMaybeProfileTable = sourceDatasetTables(Seq(("rumpel", "notablesv1")), None).map(_.headOption)

    val eventualNotableRecords = eventualMaybeProfileTable flatMap { maybeTable =>
      FutureTransformations.transform(maybeTable map getTableValues)
    }

    import scala.reflect.runtime.universe.{ TypeTag, typeOf, typeTag }
      def getTypeTag[T: TypeTag](t: T) = typeTag[T].tpe
      def getTypeOfTag[T: TypeTag] = typeOf[T]

    val eventualNotables = for {
      notableRecords <- eventualNotableRecords.map(_.get)
    } yield {
      val flattenedNotables = notableRecords.map(flattenRecordValues)
      val pegdownTimeout = 10.seconds
      val parser = new PegDownProcessor(Extensions.ALL_WITH_OPTIONALS, pegdownTimeout.toMillis)
      flattenedNotables.map(convertNotableStructures(_, parser))
    }

    val someNotables = eventualNotables recover {
      case e =>
        logger.warning(s"Error constructing notables: ${e.getMessage}")
        Iterable()
    }

    someNotables map { notables =>
      logger.info(s"Found notables:")
      notables map { notable =>
        notable map { notable =>
          logger.info(s"Notable ${notable.id}, ${notable.updated_time}, ${notable.shared}, ${notable.sharedOn}")
        }
      }
    }

    someNotables.map(_.collect {
      case Success(notable) if notable.shared && notable.public_until.nonEmpty && notable.public_until.get.isAfter(DateTime.now()) => notable
      case Success(notable) if notable.shared => notable
    }).map(_.toSeq.sortBy(_.created_time))
  }

  implicit def dateTimeOrdering: Ordering[DateTime] = Ordering.fromLessThan(_ isAfter _)

  private def convertNotableStructures(notableRaw: Map[String, Any], mdProcessor: PegDownProcessor): Try[Notable] = {
    val notableRawData = notableRaw("data")
      .asInstanceOf[Map[String, Any]]("notablesv1")
      .asInstanceOf[Map[String, Any]]

    val notableAuthorRawData = notableRawData("authorv1").asInstanceOf[Map[String, Any]]
    val notableLocationRawData = notableRawData.get("locationv1").map(_.asInstanceOf[Map[String, Any]])
    val maybeLocation = notableLocationRawData map { notableLocationRawData =>
      NotableLocation(
        notableLocationRawData("latitude").toString,
        notableLocationRawData("longitude").toString,
        notableLocationRawData("accuracy").toString,
        notableLocationRawData.get("altitude").map(_.toString).getOrElse(""),
        notableLocationRawData.get("altitude_accuracy").map(_.toString).getOrElse(""),
        notableLocationRawData.get("heading").map(_.toString).getOrElse(""),
        notableLocationRawData.get("speed").map(_.toString).getOrElse(""),
        notableLocationRawData.get("shared").map(_.toString).getOrElse("[]"))
    }

    val notablePhotoRawData = notableRawData.get("photov1").map(_.asInstanceOf[Map[String, Any]])
    val maybePhoto = notablePhotoRawData map { notablePhotoRawData =>
      NotablePhoto(
        notablePhotoRawData("link").toString,
        notablePhotoRawData("source").toString,
        notablePhotoRawData("caption").toString,
        notablePhotoRawData("shared").toString)
    }

    Try {
      Notable(
        notableRaw("id").asInstanceOf[Int],
        new DateTime(notableRaw("lastUpdated")),
        renderNoteText(notableRawData("message").toString, mdProcessor),
        notableRawData("kind").toString,
        new DateTime(notableRawData("created_time").toString),
        new DateTime(notableRawData("updated_time").toString),
        Try(new DateTime(notableRawData("public_until").toString)).toOption,
        notableRawData("shared").toString.toBoolean,
        notableRawData("shared_on").toString.split(",").toList,
        NotableAuthor(
          notableAuthorRawData.get("id").map(_.toString),
          notableAuthorRawData.get("name").map(_.toString),
          notableAuthorRawData.get("nick").map(_.toString),
          notableAuthorRawData.get("phata").map(_.toString).getOrElse(""),
          notableAuthorRawData.get("photo_url").map(_.toString).flatMap(url => if (url.trim.isEmpty) None else Some(url))),
        maybeLocation,
        maybePhoto
      )
    }
  }

  private def renderNoteText(markdown: String, mdProcessor: PegDownProcessor) = {
    mdProcessor.synchronized {
      Try(mdProcessor.markdownToHtml(markdown)).toOption.getOrElse("")
    }
  }

  private def getTableValues(table: ApiDataTable): Future[Seq[ApiDataRecord]] = {
    val fieldset = getStructureFields(table)

    val startTime = LocalDateTime.now().minusDays(365)
    val endTime = LocalDateTime.now()
    val eventualValues = fieldsetValues(fieldset, startTime, endTime, Some(10))

    eventualValues.map(values => getValueRecords(values, Seq(table)))
  }

}
