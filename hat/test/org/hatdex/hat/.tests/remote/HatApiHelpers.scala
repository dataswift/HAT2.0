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

package org.hatdex.hat.tests.remote

import akka.actor.ActorSystem
import akka.event.LoggingAdapter
import akka.http.javadsl.model.StatusCode
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.stream.{ActorMaterializer, Materializer}
import org.hatdex.hat.api.models.{ApiDataField, ApiDataTable, ApiRecordValues}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class HatApiHelpers(logger: LoggingAdapter, hatAddress: String, ownerAuthParams: Map[String, String]) {
  implicit val system = ActorSystem()
  implicit val materializer: Materializer = ActorMaterializer()

  def createTable(name: String, source: String): Future[ApiDataTable] = {
    val path = Uri.Path("/data/table")
    val tableToCreate = ApiDataTable(None, None, None, name = name, source = source, None, None)
    val tableCreateResponse = Http().singleRequest(HttpRequest(
      HttpMethods.POST,
      Uri(hatAddress).withPath(path).withQuery(Uri.Query(ownerAuthParams)),
      entity = HttpEntity(MediaTypes.`application/json`, tableToCreate.toJson.toString())
    ))

    val response: Future[HttpResponse] = tableCreateResponse.flatMap { response =>
      response.status match {
        case StatusCodes.Created =>
          logger.debug("Table created")
          tableCreateResponse
        case StatusCodes.BadRequest =>
          logger.debug(s"Table not created, looking up (${response.entity.toString})")
          val tableParams = Map("name" -> name, "source" -> source)
          val query = Uri.Query(ownerAuthParams ++ tableParams)
          val fetchTableResponse: Future[HttpResponse] = Http().singleRequest(HttpRequest(HttpMethods.GET, Uri(hatAddress).withPath(path).withQuery(query)))
          fetchTableResponse
        case _ =>
          logger.debug("Unexpected response status")
          throw new Exception("Unexpected response status")
      }
    }

    response.flatMap(r => Unmarshal[HttpEntity](r.entity).to[ApiDataTable])
  }

  def createTable(table: ApiDataTable): Future[ApiDataTable] = {
    val path = Uri.Path("/data/table")
    val tableParams = Map("name" -> table.name, "source" -> table.source)
    val query = Uri.Query(ownerAuthParams ++ tableParams)
    val fetchTableResponseFuture: Future[HttpResponse] = Http().singleRequest(HttpRequest(HttpMethods.GET, Uri(hatAddress).withPath(path).withQuery(query)))

    fetchTableResponseFuture.map(_.status).flatMap {
      case StatusCodes.NotFound =>
        val tableCreateResponse = Http().singleRequest(HttpRequest(
          HttpMethods.POST,
          Uri(hatAddress).withPath(path).withQuery(Uri.Query(ownerAuthParams)),
          entity = HttpEntity(MediaTypes.`application/json`, table.toJson.toString())
        ))
        tableCreateResponse.flatMap(r => Unmarshal[HttpEntity](r.entity).to[ApiDataTable])
      case StatusCodes.OK =>
        val tableSummaryFuture = fetchTableResponseFuture.flatMap(r => Unmarshal[HttpEntity](r.entity).to[ApiDataTable])
        tableSummaryFuture.flatMap { tableSummary =>
          Http().singleRequest(
            HttpRequest(HttpMethods.GET, Uri(hatAddress).withPath(Uri.Path(s"/data/table/${tableSummary.id.get}")).withQuery(Uri.Query(ownerAuthParams)))
          ).flatMap(r => Unmarshal[HttpEntity](r.entity).to[ApiDataTable])
        }
      case status:StatusCode =>
        // Unexpected status code
        throw new Exception("Unexpected status code " + status.defaultMessage())
    }
  }

  def getTableStructure(name: String, source: String): Future[ApiDataTable] = {
    val path = Uri.Path("/data/table")
    val tableParams = Map("name" -> name, "source" -> source)
    val query = Uri.Query(ownerAuthParams ++ tableParams)

    val fetchTableResponseFuture: Future[HttpResponse] = Http().singleRequest(HttpRequest(HttpMethods.GET, Uri(hatAddress).withPath(path).withQuery(query)))
    val tableSummaryFuture = fetchTableResponseFuture.flatMap(r => Unmarshal[HttpEntity](r.entity).to[ApiDataTable])

    tableSummaryFuture flatMap { tableSummary =>
      Http().singleRequest(
        HttpRequest(HttpMethods.GET, Uri(hatAddress).withPath(Uri.Path(s"/data/table/${tableSummary.id.get}")).withQuery(Uri.Query(ownerAuthParams)))
      ).flatMap(r => Unmarshal[HttpEntity](r.entity).to[ApiDataTable])
    }
  }

  def createField(field: ApiDataField, table: ApiDataTable): Future[ApiDataField] = {
    val path = Uri.Path("/data/field")
    val fieldCreateResponse = Http().singleRequest(HttpRequest(
      HttpMethods.POST,
      Uri(hatAddress).withPath(path).withQuery(Uri.Query(ownerAuthParams)),
      entity = HttpEntity(MediaTypes.`application/json`, field.toJson.toString)
    ))
    fieldCreateResponse.flatMap(r => Unmarshal[HttpEntity](r.entity).to[ApiDataField])
  }

  def insertDataRecords(dataRecords: Seq[ApiRecordValues]): Future[Seq[ApiRecordValues]] = {
    val path = Uri.Path("/data/record/values")
    val insertDataRecordResponse = Http().singleRequest(HttpRequest(
      HttpMethods.POST,
      Uri(hatAddress).withPath(path).withQuery(Uri.Query(ownerAuthParams)),
      entity = HttpEntity(MediaTypes.`application/json`, dataRecords.toJson.toString)
    ))

    insertDataRecordResponse.flatMap(r => Unmarshal[HttpEntity](r.entity).to[Seq[ApiRecordValues]])
  }
}
