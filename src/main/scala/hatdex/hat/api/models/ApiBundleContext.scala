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
package hatdex.hat.api.models

import org.joda.time.LocalDateTime
import hatdex.hat.dal.Tables._

import scala.collection.immutable.Map

case class ApiBundleContextEntitySelection(
    id: Option[Int],
    dateCreated: Option[LocalDateTime],
    lastUpdated: Option[LocalDateTime],
    entityName: Option[String],
    entityId: Option[Int],
    entityKind: Option[String],
    properties: Option[Seq[ApiBundleContextPropertySelection]])

object ApiBundleContextEntitySelection {
  def fromDbModel(entitySelection: BundleContextEntitySelectionRow): ApiBundleContextEntitySelection = {
    ApiBundleContextEntitySelection(Some(entitySelection.id),
      Some(entitySelection.dateCreated), Some(entitySelection.lastUpdated),
      entitySelection.entityName, entitySelection.entityId, entitySelection.entityKind,
      None)
  }
}

case class ApiBundleContextPropertySelection(
    id: Option[Int],
    dateCreated: Option[LocalDateTime],
    lastUpdated: Option[LocalDateTime],
    propertyRelationshipKind: Option[String],
    propertyRelationshipId: Option[Int],
    propertyName: Option[String],
    propertyType: Option[String],
    propertyUnitofmeasurement: Option[String])

object ApiBundleContextPropertySelection {
  def fromDbModel(propertySelection: BundleContextPropertySelectionRow): ApiBundleContextPropertySelection = {
    ApiBundleContextPropertySelection(Some(propertySelection.propertySelectionId),
      Some(propertySelection.dateCreated), Some(propertySelection.lastUpdated),
      propertySelection.propertyRelationshipKind, propertySelection.propertyRelationshipId,
      propertySelection.propertyName, propertySelection.propertyType, propertySelection.propertyUnitofmeasurement)
  }
}

case class ApiBundleContext(
    id: Option[Int],
    dateCreated: Option[LocalDateTime],
    lastUpdated: Option[LocalDateTime],
    name: String,
    entities: Option[Seq[ApiBundleContextEntitySelection]],
    bundles: Option[Seq[ApiBundleContext]])

object ApiBundleContext {
  def fromDbModel(bundle: BundleContextRow): ApiBundleContext = {
    ApiBundleContext(Some(bundle.id), Some(bundle.dateCreated), Some(bundle.lastUpdated), bundle.name, None, None)
  }
  def fromNestedBundle(bundle: BundleContextTreeRow): ApiBundleContext = {
    ApiBundleContext(bundle.id, bundle.dateCreated, bundle.lastUpdated, bundle.name.getOrElse(""), None, None)
  }
}