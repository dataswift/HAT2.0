/*
 * Copyright (C) 2019 HAT Data Exchange Ltd
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
 * Written by Marios Tsekis <marios.tsekis@hatdex.org>
 * 2 / 2019
 */
package org.hatdex.hat.she.service

import java.util.UUID

import org.hatdex.hat.api.models.{ EndpointData, EndpointQuery, PropertyQuery }
import org.hatdex.hat.api.service.richData.RichDataService
import org.hatdex.hat.resourceManagement.HatServer
import org.hatdex.hat.she.models.StaticDataValues
import play.api.Logger
import play.api.libs.json._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait StaticDataEndpointMapper extends JodaWrites with JodaReads {
  protected lazy val logger: Logger = Logger(this.getClass)

  def dataQuery(): PropertyQuery
  def mapDataRecord(recordId: UUID, content: JsValue, endpoint: String): Seq[StaticDataValues]

  final def staticDataRecords()(
    implicit
    hatServer: HatServer, richDataService: RichDataService): Future[Seq[StaticDataValues]] = {

    val query = dataQuery()
    val eventualDataSource: Future[Seq[EndpointData]] = richDataService.propertyData(query.endpoints, query.orderBy,
      orderingDescending = query.ordering.contains("descending"), skip = 0, limit = query.limit, createdAfter = None)(hatServer.db)

    eventualDataSource.map { dataSource => dataSource.map(item => mapDataRecord(item.recordId.get, item.data, item.endpoint)).headOption.getOrElse(Seq()) }
  }
}

class FacebookProfileStaticDataMapper extends StaticDataEndpointMapper {
  def dataQuery(): PropertyQuery = {
    PropertyQuery(
      List(
        EndpointQuery("facebook/profile", None, None, None)), Some("hat_updated_time"), Some("descending"), Some(1))
  }

  def mapDataRecord(recordId: UUID, content: JsValue, endpoint: String): Seq[StaticDataValues] = {

    val eventualData = content.validate[Map[String, JsValue]]
    eventualData match {
      case JsSuccess(value, _) =>
        val lastPartOfEndpointString = endpoint.split("/").last
        Seq(StaticDataValues(lastPartOfEndpointString, value.filterKeys(key => key != "friends")))
      case e: JsError =>
        logger.error(s"Couldn't validate static data JSON for $endpoint. $e")
        Seq()
    }
  }
}

class TwitterProfileStaticDataMapper extends StaticDataEndpointMapper {
  def dataQuery(): PropertyQuery = {
    PropertyQuery(
      List(
        EndpointQuery("twitter/tweets", None, None, None)), Some("lastUpdated"), Some("descending"), Some(1))
  }

  def mapDataRecord(recordId: UUID, content: JsValue, endpoint: String): Seq[StaticDataValues] = {

    val maybeUserData = (content \\ "user")

    maybeUserData.headOption
      .map({ item =>
        item.validate[Map[String, JsValue]] match {
          case JsSuccess(value, _) =>
            val lastPartOfEndpointString = endpoint.split("/").last
            Seq(StaticDataValues(lastPartOfEndpointString, value.filterKeys(key => key != "entities")))
          case e: JsError =>
            logger.error(s"Couldn't validate static data JSON for $endpoint. $e")
            Seq()
        }
      })
      .getOrElse(Seq())
  }
}

class SpotifyProfileStaticDataMapper extends StaticDataEndpointMapper {
  def dataQuery(): PropertyQuery = {
    PropertyQuery(
      List(
        EndpointQuery("spotify/profile", None, None, None)), Some("dateCreated"), Some("descending"), Some(1))
  }

  def mapDataRecord(recordId: UUID, content: JsValue, endpoint: String): Seq[StaticDataValues] = {

    val eventualData = content.validate[Map[String, JsValue]]
    eventualData match {
      case JsSuccess(value, _) =>
        val lastPartOfEndpointString = endpoint.split("/").last

        value.filterKeys(key => key != "images" && key != "external_urls").map { item =>

          transformData(item)
        }
        Seq(StaticDataValues(lastPartOfEndpointString, value))
      case e: JsError =>
        logger.error(s"Couldn't validate static data JSON for $endpoint. $e")
        Seq()
    }
  }

  private def transformData(rawData: (String, JsValue)): JsResult[JsValue] = {

    val transformation = __.json.update(
      __.read[JsObject].map(profile => {
        val followers = (profile \ "followers" \ "total").asOpt[JsNumber].getOrElse(JsNumber(0))

        profile ++ JsObject(Map(
          "followers" -> followers))
      }))

    rawData._2.transform(transformation)
  }
}

class InstagramProfileStaticDataMapper extends StaticDataEndpointMapper {
  def dataQuery(): PropertyQuery = {
    PropertyQuery(
      List(
        EndpointQuery("instagram/profile", None, None, None)), Some("hat_updated_time"), Some("descending"), Some(1))
  }

  def mapDataRecord(recordId: UUID, content: JsValue, endpoint: String): Seq[StaticDataValues] = {

    val eventualData = content.validate[Map[String, JsValue]]
    eventualData match {
      case JsSuccess(value, _) =>
        val lastPartOfEndpointString = endpoint.split("/").last

        value.map(item => transformData(item))
        Seq(StaticDataValues(lastPartOfEndpointString, value))
      case e: JsError =>
        logger.error(s"Couldn't validate static data JSON for $endpoint. $e")
        Seq()
    }
  }

  private def transformData(rawData: (String, JsValue)): JsResult[JsValue] = {

    val transformation = __.json.update(
      __.read[JsObject].map(profile => {
        val totalImagesUploaded = (profile \ "counts" \ "media").asOpt[JsNumber].getOrElse(JsNumber(0))
        val totalFollowers = (profile \ "counts" \ "followed_by").asOpt[JsNumber].getOrElse(JsNumber(0))
        val totalPeopleUsersFollows = (profile \ "counts" \ "JsNumber").asOpt[JsString].getOrElse(JsNumber(0))

        profile ++ JsObject(Map(
          "media" -> totalImagesUploaded,
          "follows" -> totalPeopleUsersFollows,
          "followers" -> totalFollowers)) - "counts"
      }))

    rawData._2.transform(transformation)
  }
}

class FitbitProfileStaticDataMapper extends StaticDataEndpointMapper {
  def dataQuery(): PropertyQuery = {
    PropertyQuery(
      List(
        EndpointQuery("fitbit/profile", None, None, None)), Some("hat_updated_time"), Some("descending"), Some(1))
  }

  def mapDataRecord(recordId: UUID, content: JsValue, endpoint: String): Seq[StaticDataValues] = {

    val eventualData = content.validate[Map[String, JsValue]]
    eventualData match {
      case JsSuccess(value, _) =>
        val lastPartOfEndpointString = endpoint.split("/").last

        Seq(StaticDataValues(lastPartOfEndpointString, value.filterKeys(key => key != "features" && key != "topBadges")))
      case e: JsError =>
        logger.error(s"Couldn't validate static data JSON for $endpoint. $e")
        Seq()
    }
  }
}
