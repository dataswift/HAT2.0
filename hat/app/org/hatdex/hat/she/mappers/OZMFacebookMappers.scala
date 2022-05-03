package org.hatdex.hat.she.mappers

import io.dataswift.models.hat.applications._
import io.dataswift.models.hat.{ EndpointQuery, EndpointQueryFilter, PropertyQuery }
import org.joda.time.DateTime
import play.api.libs.json.{ JsValue }

import java.util.UUID
import scala.util.{ Failure, Try }

/**
 * Direct copy of FacebookMappers.scala
 * and modified accordingly
 * To be reviewed in conjunction with Rumpel review
 */
class OZMFacebookProfileMapper extends DataEndpointMapper with FeedItemComparator {
  def dataQueries(
      fromDate: Option[DateTime],
      untilDate: Option[DateTime]): Seq[PropertyQuery] =
    Seq(
      PropertyQuery(
        List(
          EndpointQuery(
            "ozm-facebook/profile",
            None,
            dateFilter(fromDate, untilDate).map(f => Seq(EndpointQueryFilter("hat_updated_time", None, f))),
            None
          )
        ),
        Some("hat_updated_time"),
        Some("descending"),
        None
      )
    )

  def mapDataRecord(
      recordId: UUID,
      content: JsValue,
      tailRecordId: Option[UUID] = None,
      tailContent: Option[JsValue] = None): Try[DataFeedItem] = {
    val comparison =
      compare(content, tailContent).filter(
        _._1 == false
      ) // remove all fields that have the same values pre/current
    if (comparison.isEmpty)
      Failure(new RuntimeException("Comparison Failure. Data the same"))
    else
      for {
        title <- Try(
                   DataFeedItemTitle("Your OZM Facebook Profile has changed.", None, None)
                 )
        itemContent <- {
          val contentText = comparison.map(item => s"${item._2}\n").mkString
          Try(DataFeedItemContent(Some(contentText), None, None, None))
        }
      } yield DataFeedItem(
        "facebook",
        (tailContent.getOrElse(content) \ "hat_updated_time").as[DateTime],
        Seq("profile"),
        Some(title),
        Some(itemContent),
        None
      )
  }

  def compare(
      content: JsValue,
      tailContent: Option[JsValue]): Seq[(Boolean, String)] =
    if (tailContent.isEmpty)
      Seq()
    else
      Seq(
        compareString(content, tailContent.get, "gender", "Gender")
      )
}


class OZMFacebookFeedMapper extends DataEndpointMapper {
  def dataQueries(
      fromDate: Option[DateTime],
      untilDate: Option[DateTime]): Seq[PropertyQuery] =
    Seq(
      PropertyQuery(
        List(
          EndpointQuery(
            "ozm-facebook/feed",
            None,
            dateFilter(fromDate, untilDate).map(f => Seq(EndpointQueryFilter("created_time", None, f))),
            None
          )
        ),
        Some("created_time"),
        Some("descending"),
        None
      )
    )

  def mapDataRecord(
      recordId: UUID,
      content: JsValue,
      tailRecordId: Option[UUID] = None,
      tailContent: Option[JsValue] = None): Try[DataFeedItem] =
    for {
      title <- Try(
                 if ((content \ "type").as[String] == "photo")
                   DataFeedItemTitle("You posted a photo", None, Some("photo"))
                 else if ((content \ "type").as[String] == "link")
                   DataFeedItemTitle("You shared a story", None, None)
                 else
                   DataFeedItemTitle("You posted", None, None)
               )
      media <- Try(
                 (content \ "picture")
                   .asOpt[String]
                   .map(url =>
                     List(
                       DataFeedItemMedia(
                         Some(url),
                         (content \ "full_picture").asOpt[String]
                       )
                     )
                   )
                   .getOrElse {
                     List(
                       DataFeedItemMedia(None, (content \ "full_picture").asOpt[String])
                     )
                   }
               )
      itemContent <- Try(
                       DataFeedItemContent(
                         Some(s"""${(content \ "message")
                           .asOpt[String]
                           .getOrElse(
                             (content \ "story")
                               .asOpt[String]
                               .getOrElse((content \ "description").as[String])
                           )}
             |
             |${(content \ "link")
                           .asOpt[String]
                           .getOrElse("")}""".stripMargin.trim),
                         None,
                         Some(media),
                         None
                       )
                     )
      date <- Try((content \ "created_time").as[DateTime])
      tags <- Try(Seq("post", (content \ "type").as[String]))
    } yield {
      DataFeedItem(
        "facebook",
        date,
        tags,
        Some(title),
        Some(itemContent),
        None
      )
    }
}
