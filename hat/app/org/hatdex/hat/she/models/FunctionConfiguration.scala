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
 * 11 / 2017
 */

package org.hatdex.hat.she.models

import java.util.UUID

import org.hatdex.hat.api.json.{ DataFeedItemJsonProtocol, RichDataJsonFormats }
import org.hatdex.hat.api.models.applications.DataFeedItem
import org.hatdex.hat.api.models.{ Drawable, EndpointData, EndpointDataBundle }
import org.hatdex.hat.dal.ModelTranslation
import org.hatdex.hat.dal.Tables.{ DataBundlesRow, SheFunctionRow }
import org.joda.time.{ DateTime, Period }
import play.api.libs.json._

case class Response(
    namespace: String,
    endpoint: String,
    data: Seq[JsValue],
    linkedRecords: Seq[UUID])

case class Request(
    data: Map[String, Seq[EndpointData]],
    linkRecords: Boolean)

case class FunctionGraphics(
    logo: Drawable,
    screenshots: Seq[Drawable])

case class FunctionConfiguration(
    name: String,
    description: String,
    headline: String,
    graphics: Option[FunctionGraphics],
    trigger: FunctionTrigger.Trigger,
    available: Boolean,
    enabled: Boolean,
    dataBundle: EndpointDataBundle,
    lastExecution: Option[DateTime],
    dataPreview: Option[Seq[DataFeedItem]],
    dataPreviewEndpoint: Option[String]) {
  def update(other: FunctionConfiguration): FunctionConfiguration = {
    FunctionConfiguration(
      this.name,
      other.description,
      other.headline,
      other.graphics,
      other.trigger,
      other.available,
      this.enabled || other.enabled,
      other.dataBundle,
      this.lastExecution.orElse(other.lastExecution),
      other.dataPreview,
      other.dataPreviewEndpoint)
  }
}

object FunctionConfiguration {
  def apply(function: SheFunctionRow, bundle: DataBundlesRow, available: Boolean = false): FunctionConfiguration = {
    import org.hatdex.hat.she.models.FunctionConfigurationJsonProtocol._
    import org.hatdex.hat.she.models.FunctionTrigger.Trigger
    import DataFeedItemJsonProtocol.feedItemFormat

    FunctionConfiguration(
      function.name,
      function.description,
      function.headline,
      function.graphics.map(_.as[FunctionGraphics]),
      function.trigger.as[Trigger],
      available, // false by default, needs to take current runtime configuration into account to update
      function.enabled,
      ModelTranslation.fromDbModel(bundle),
      function.lastExecution,
      function.dataPreview.map(_.as[Seq[DataFeedItem]]),
      function.dataPreviewEndpoint)
  }
}

object FunctionTrigger {
  // Sealed type for the different types of function triggers available
  sealed trait Trigger {
    val triggerType: String
  }

  case class TriggerPeriodic(period: Period) extends Trigger {
    final val triggerType: String = "periodic"
  }

  case class TriggerIndividual() extends Trigger {
    final val triggerType: String = "individual"
  }

  case class TriggerManual() extends Trigger {
    final val triggerType: String = "manual"
  }

}

trait FunctionConfigurationJsonProtocol extends JodaWrites with JodaReads with RichDataJsonFormats with DataFeedItemJsonProtocol {
  import FunctionTrigger._

  protected implicit val triggerPeriodicFormat: Format[TriggerPeriodic] = Json.format[TriggerPeriodic]

  implicit val triggerFormat: Format[Trigger] = new Format[Trigger] {
    def reads(json: JsValue): JsResult[Trigger] = (json \ "triggerType").as[String] match {
      case "periodic"   => Json.fromJson[TriggerPeriodic](json)(triggerPeriodicFormat)
      case "individual" => JsSuccess(TriggerIndividual())
      case "manual"     => JsSuccess(TriggerManual())
      case triggerType  => JsError(s"Unexpected JSON value $triggerType in $json")
    }

    def writes(trigger: Trigger): JsValue = {
      val triggerJson = trigger match {
        case ds: TriggerPeriodic  => Json.toJson(ds)(triggerPeriodicFormat)
        case _: TriggerIndividual => JsObject(Seq())
        case _: TriggerManual     => JsObject(Seq())
      }
      triggerJson.as[JsObject].+(("triggerType", Json.toJson(trigger.triggerType)))
    }
  }

  implicit val drawableFormat: Format[Drawable] = Json.format[Drawable]
  implicit val functionGraphicsFormat: Format[FunctionGraphics] = Json.format[FunctionGraphics]

  implicit val functionConfigurationFormat: Format[FunctionConfiguration] = Json.format[FunctionConfiguration]
  implicit val responseFormat: Format[Response] = Json.format[Response]
  implicit val requestFormat: Format[Request] = Json.format[Request]
}

object FunctionConfigurationJsonProtocol extends FunctionConfigurationJsonProtocol
