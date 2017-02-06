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

package org.hatdex.hat.phata.service

import javax.inject.Inject

import org.hatdex.hat.api.json.HatJsonFormats
import org.hatdex.hat.api.models.{ ApiDataRecord, ApiDataTable }
import org.hatdex.hat.api.service.{ BundleService, DalExecutionContext, DataService }
import org.hatdex.hat.dal.SlickPostgresDriver.backend.Database
import org.hatdex.hat.phata.models._
import org.hatdex.hat.resourceManagement.HatServer
import org.hatdex.hat.utils.FutureTransformations
import org.joda.time.{ DateTime, LocalDateTime }
import org.pegdown.{ Extensions, PegDownProcessor }
import play.api.libs.functional.syntax._
import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.{ Configuration, Logger }

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.{ Failure, Success, Try }

class NotablesService @Inject() (bundleService: BundleService, dataService: DataService, configuration: Configuration) extends DalExecutionContext {
  private val logger = Logger(this.getClass)
  private implicit def hatServer2db(implicit hatServer: HatServer): Database = hatServer.db

  def getPublicNotes()(implicit server: HatServer): Future[Seq[Notable]] = {
    val eventualMaybeProfileTable = bundleService.sourceDatasetTables(Seq(("rumpel", "notablesv1")), None).map(_.headOption)

    val eventualNotableRecords = eventualMaybeProfileTable flatMap { maybeTable =>
      FutureTransformations.transform(maybeTable.map(getTableValues))
    }

    val eventualNotables = for {
      notableRecords <- eventualNotableRecords.map(_.get)
    } yield {
      val flattenedNotables = notableRecords.map(HatJsonFormats.flattenRecordValues)
      val pegdownTimeout = 10.seconds
      val parser = new PegDownProcessor(Extensions.ALL_WITH_OPTIONALS, pegdownTimeout.toMillis)
      flattenedNotables.map(convertNotableStructures(_, parser))
    }

    val someNotables = eventualNotables recover { case e => Iterable() }

    someNotables map { notables =>
      if (notables.nonEmpty) {
        logger.debug(s"Found some notables")
      }
      notables map { notable =>
        notable recover {
          case e =>
            logger.debug(s"Notable parsing failed $e")
        }
      }
    }

    someNotables.map(_.collect {
      case Success(notable) if notable.shared && notable.shared_on.exists(_.contains("marketsquare")) && notable.public_until.nonEmpty && notable.public_until.get.isAfter(DateTime.now()) => notable
      case Success(notable) if notable.shared && notable.shared_on.exists(_.contains("marketsquare")) && notable.public_until.isEmpty => notable
    }).map(_.toSeq.sortBy(_.created_time))
  }

  private implicit def dateTimeOrdering: Ordering[DateTime] = Ordering.fromLessThan(_ isAfter _)

  import Notable.notableJsonFormat

  private def convertNotableStructures(notableRaw: JsObject, mdProcessor: PegDownProcessor): Try[Notable] = {
    notableRaw.transform(notablesHatJsonTransformer)
      .map { v => Try(v.as[Notable]) }
      .recover {
        case e =>
          logger.error(s"Error parsing notable: $e")
          Failure(new RuntimeException("Error parsing"))
      }
      .get
  }

  private val notablesHatJsonTransformer = __.json.update(
    (__ \ 'data \ 'notablesv1 \ 'id).json.copyFrom((__ \ 'id).json.pick) and // copy record id into the notable data
      (__ \ 'data \ 'notablesv1 \ 'recordDateLastUpdated).json.copyFrom((__ \ 'lastUpdated).json.pick) reduce // copy record update timestamp into the notable data
  ) andThen
    (__ \ 'data \ 'notablesv1).json.pick(
      __.json.update(
        (__ \ 'author).json.copyFrom((__ \ 'authorv1).json.pick) and // rename authorv1 to author
          (__ \ 'location).json.copyFrom((__ \ 'locationv1).json.pick orElse Reads.pure(Json.toJson(None))) and // rename locationv1 to location
          (__ \ 'shared).json.update(of[JsString].map { // remap "shared" string property to boolean
            case JsString("true") => JsBoolean(true)
            case _                => JsBoolean(false)
          }) reduce

      ) andThen
        (__ \ 'shared_on).json.update(of[JsString].map {
          case JsString(sharedOn) =>
            val sharedNetworks = sharedOn.split(',').map(_.trim)
            logger.debug(s"Shared Networks: ${JsArray(sharedNetworks.map(v => Json.toJson(v)))}")
            JsArray(sharedNetworks.map(v => Json.toJson(v)))
          case _ =>
            JsArray(Seq[JsValue]())
        })
    )

  private def renderNoteText(markdown: String, mdProcessor: PegDownProcessor) = {
    mdProcessor.synchronized {
      Try(mdProcessor.markdownToHtml(markdown)).toOption.getOrElse("")
    }
  }

  private def getTableValues(table: ApiDataTable)(implicit server: HatServer): Future[Seq[ApiDataRecord]] = {
    val fieldset = dataService.getStructureFields(table)

    val startTime = LocalDateTime.now().minusDays(365)
    val endTime = LocalDateTime.now()
    val eventualValues = dataService.fieldsetValues(fieldset, startTime, endTime, Some(10))

    eventualValues.map(values => dataService.restructureTableValuesToRecords(values, Seq(table)))
  }

}
