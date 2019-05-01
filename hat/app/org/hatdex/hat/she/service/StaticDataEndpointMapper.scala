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

  def dataQueries(): Seq[PropertyQuery]
  def mapDataRecord(recordId: UUID, content: JsValue, endpoint: String): Seq[StaticDataValues]

  final def staticDataRecords()(
    implicit
    hatServer: HatServer, richDataService: RichDataService): Future[Seq[StaticDataValues]] = {

    val staticData = Future.sequence(dataQueries.map { query =>

      val eventualDataSource: Future[Seq[EndpointData]] = richDataService.propertyData(query.endpoints, query.orderBy,
        orderingDescending = query.ordering.contains("descending"), skip = 0, limit = query.limit, createdAfter = None)(hatServer.db)

      eventualDataSource.map { dataSource => dataSource.map(item => mapDataRecord(item.recordId.get, item.data, item.endpoint)).headOption.getOrElse(Seq()) }
    })

    staticData.map(_.flatten)
  }
}

class FacebookProfileStaticDataMapper extends StaticDataEndpointMapper {
  def dataQueries(): Seq[PropertyQuery] = {
    Seq(
      PropertyQuery(List(EndpointQuery("facebook/profile", None, None, None)), Some("hat_updated_time"), Some("descending"), Some(1)),
      PropertyQuery(List(EndpointQuery("facebook/likes/pages", None, None, None)), Some("created_time"), Some("descending"), Some(1)))
  }

  def mapDataRecord(recordId: UUID, content: JsValue, endpoint: String): Seq[StaticDataValues] = {

    val eventualData = content.validate[Map[String, JsValue]]
    eventualData match {
      case JsSuccess(value, _) =>
        val lastPartOfEndpointString = endpoint.split("/").last
        if (endpoint.contains("likes")) {

          val numberOfPagesLiked = value.filterKeys(key => key == "number_of_pages_liked")
          if (numberOfPagesLiked.isEmpty) {

            Seq()
          }
          else {

            Seq(StaticDataValues(lastPartOfEndpointString, numberOfPagesLiked))
          }
        }
        else {

          Seq(StaticDataValues(lastPartOfEndpointString, value.filterKeys(key => key != "friends" && key != "languages")))
        }
      case e: JsError =>
        logger.error(s"Couldn't validate static data JSON for $endpoint. $e")
        Seq()
    }
  }
}

class TwitterProfileStaticDataMapper extends StaticDataEndpointMapper {
  def dataQueries(): Seq[PropertyQuery] = {
    Seq(PropertyQuery(
      List(
        EndpointQuery("twitter/tweets", None, None, None)), Some("id"), Some("descending"), Some(1)))
  }

  def mapDataRecord(recordId: UUID, content: JsValue, endpoint: String): Seq[StaticDataValues] = {

    val maybeUserData = (content \ "user").asOpt[Map[String, JsValue]]
    val lastPartOfEndpointString = endpoint.split("/").last
    maybeUserData match {
      case Some(user) => Seq(StaticDataValues(lastPartOfEndpointString, user.filterKeys(key => key != "entities")))
      case _          => Seq()
    }
  }
}

class SpotifyProfileStaticDataMapper extends StaticDataEndpointMapper {
  def dataQueries(): Seq[PropertyQuery] = {
    Seq(PropertyQuery(
      List(
        EndpointQuery("spotify/profile", None, None, None)), Some("dateCreated"), Some("descending"), Some(1)))
  }

  def mapDataRecord(recordId: UUID, content: JsValue, endpoint: String): Seq[StaticDataValues] = {

    val eventualData = content.validate[JsObject]
    eventualData match {
      case JsSuccess(value, _) =>

        val lastPartOfEndpointString = endpoint.split("/").last
        val maybeTransformedData = transformData(value).flatMap(item => item.validate[Map[String, JsValue]])
        maybeTransformedData match {
          case JsSuccess(data, _) =>

            Seq(StaticDataValues(lastPartOfEndpointString, (data - "images" - "external_urls")))
          case e: JsError =>

            logger.error(s"Couldn't validate static data JSON for $endpoint. $e")
            Seq()
        }
      case e: JsError =>
        logger.error(s"Couldn't validate static data JSON for $endpoint. $e")
        Seq()
    }
  }

  private def transformData(rawData: JsObject): JsResult[JsValue] = {

    val transformation = __.json.update(
      __.read[JsObject].map(profile => {
        val followers = (profile \ "followers" \ "total").asOpt[JsNumber].getOrElse(JsNumber(0))

        profile ++ JsObject(Map(
          "followers" -> followers))
      }))

    rawData.transform(transformation)
  }
}

class InstagramProfileStaticDataMapper extends StaticDataEndpointMapper {
  def dataQueries(): Seq[PropertyQuery] = {
    Seq(PropertyQuery(
      List(
        EndpointQuery("instagram/profile", None, None, None)), Some("hat_updated_time"), Some("descending"), Some(1)))
  }

  def mapDataRecord(recordId: UUID, content: JsValue, endpoint: String): Seq[StaticDataValues] = {

    val eventualData = content.validate[JsObject]

    eventualData match {
      case JsSuccess(value, _) =>

        val lastPartOfEndpointString = endpoint.split("/").last

        val maybeTransformedData = transformData(value).flatMap(item => item.validate[Map[String, JsValue]])
        maybeTransformedData match {
          case JsSuccess(data, _) =>

            Seq(StaticDataValues(lastPartOfEndpointString, (data - "counts")))
          case e: JsError =>

            logger.error(s"Couldn't validate static data JSON for $endpoint. $e")
            Seq()
        }
      case e: JsError =>
        logger.error(s"Couldn't validate static data JSON for $endpoint. $e")
        Seq()
    }
  }

  private def transformData(rawData: JsObject): JsResult[JsValue] = {

    val transformation = __.json.update(
      __.read[JsObject].map(profile => {
        logger.info(s"Trying to map profile: $profile")
        val totalImagesUploaded = (profile \ "counts" \ "media").asOpt[JsNumber].getOrElse(JsNumber(0))
        val totalFollowers = (profile \ "counts" \ "followed_by").asOpt[JsNumber].getOrElse(JsNumber(0))
        val totalPeopleUsersFollows = (profile \ "counts" \ "follows").asOpt[JsNumber].getOrElse(JsNumber(0))

        profile ++ JsObject(Map(
          "media" -> totalImagesUploaded,
          "follows" -> totalPeopleUsersFollows,
          "followers" -> totalFollowers))
      }))

    rawData.transform(transformation)
  }
}

class FitbitProfileStaticDataMapper extends StaticDataEndpointMapper {
  def dataQueries(): Seq[PropertyQuery] = {
    Seq(PropertyQuery(
      List(
        EndpointQuery("fitbit/profile", None, None, None)), Some("hat_updated_time"), Some("descending"), Some(1)))
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