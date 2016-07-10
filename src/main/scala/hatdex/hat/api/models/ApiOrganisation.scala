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

import hatdex.hat.dal.Tables.OrganisationsOrganisationRow

case class ApiOrganisation(
    id: Option[Int],
    name: String,
    staticProperties: Option[Seq[ApiPropertyRelationshipStatic]],
    dynamicProperties: Option[Seq[ApiPropertyRelationshipDynamic]],
    organisations: Option[Seq[ApiOrganisationRelationship]],
    locations: Option[Seq[ApiLocationRelationship]],
    things: Option[Seq[ApiThingRelationship]])

object ApiOrganisation {
  def fromDbModel(entity: OrganisationsOrganisationRow) : ApiOrganisation = {
    new ApiOrganisation(Some(entity.id), entity.name, None, None, None, None, None)
  }
}

case class ApiOrganisationRelationship(relationshipType: String, organisation: ApiOrganisation)