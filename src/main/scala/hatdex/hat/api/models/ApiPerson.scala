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

import hatdex.hat.dal.Tables.{PeoplePersontopersonrelationshiptypeRow, PeoplePersonRow}

case class ApiPerson(
    id: Option[Int],
    name: String,
    personId: String,
    staticProperties: Option[Seq[ApiPropertyRelationshipStatic]],
    dynamicProperties: Option[Seq[ApiPropertyRelationshipDynamic]],
    people: Option[Seq[ApiPersonRelationship]],
    locations: Option[Seq[ApiLocationRelationship]],
    organisations: Option[Seq[ApiOrganisationRelationship]])

object ApiPerson {
  def fromDbModel(entity: PeoplePersonRow) : ApiPerson = {
    ApiPerson(Some(entity.id), entity.name, entity.personId, None, None, None, None, None)
  }
}

case class ApiPersonRelationship(relationshipType: String, person: ApiPerson)

case class ApiPersonRelationshipType(id: Option[Int], name: String, description: Option[String])

object ApiPersonRelationshipType {
  def fromDbModel(relationship: PeoplePersontopersonrelationshiptypeRow) : ApiPersonRelationshipType = {
    ApiPersonRelationshipType(Some(relationship.id), relationship.name, relationship.description)
  }
}