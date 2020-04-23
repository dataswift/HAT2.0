package org.hatdex.hat.she.mappers

import java.util.UUID

import org.hatdex.hat.api.models.{ EndpointQuery, EndpointQueryFilter, PropertyQuery }
import org.hatdex.hat.api.models.applications.{ DataFeedItem, DataFeedItemContent, DataFeedItemMedia, DataFeedItemTitle }
import org.hatdex.hat.she.models.StaticDataValues
import org.joda.time.DateTime
import play.api.libs.json.{ JsError, JsNumber, JsObject, JsResult, JsSuccess, JsValue, __ }

import scala.util.Try

class InstagramMediaMapper extends DataEndpointMapper {
  override protected val dataDeduplicationField: Option[String] = Some("id")

  def dataQueries(fromDate: Option[DateTime], untilDate: Option[DateTime]): Seq[PropertyQuery] = {
    Seq(PropertyQuery(
      List(EndpointQuery("instagram/feed", None, dateFilter(fromDate, untilDate).map(f ⇒ Seq(EndpointQueryFilter("timestamp", None, f))), None)),
      Some("timestamp"), Some("descending"), None))
  }

  def mapDataRecord(recordId: UUID, content: JsValue, tailRecordId: Option[UUID] = None, tailContent: Option[JsValue] = None): Try[DataFeedItem] = {
    for {
      createdTime <- Try(new DateTime((content \ "timestamp").as[DateTime]))
      description <- Try((content \ "caption").as[String])
      kind <- Try((content \ "media_type").as[String])
      title <- Try(Some(DataFeedItemTitle("You posted", None, Some(kind.toLowerCase))))
      feedItemContent <- Try(Some(DataFeedItemContent(
        Some(description),
        None,
        kind match {
          case "IMAGE" =>
            Some(Seq(DataFeedItemMedia(
              (content \ "media_url").asOpt[String],
              (content \ "media_url").asOpt[String])))
          case _ => None
        },
        None)))
    } yield {
      val regex = "#\\w+".r
      val tags = regex.findAllIn(description).toList
      logger.debug(s"Tags: $tags")
      DataFeedItem("instagram", createdTime, tags, title, feedItemContent, None)
    }
  }
}

class InstagramProfileStaticDataMapper extends StaticDataEndpointMapper {
  def dataQueries(): Seq[PropertyQuery] = {
    Seq(PropertyQuery(
      List(
        EndpointQuery("instagram/profile", None, None, None)), Some("hat_created_time"), Some("descending"), Some(1)))
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
        val totalImagesUploaded = (profile \ "media_count").asOpt[JsNumber].getOrElse(JsNumber(0))

        profile ++ JsObject(Map(
          "media" -> totalImagesUploaded))
      }))

    rawData.transform(transformation)
  }
}
