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

package hatdex.hat.api

import hatdex.hat.dal.SlickPostgresDriver.api._
import hatdex.hat.dal.Tables._

object TestDataCleanup {
  def cleanupAll = {
    val cleanupQuery = DBIO.seq(
      cleanupPropertyCrossrefs,
      cleanupEntityCrossrefs,
      cleanupEntities,
      cleanupStats,
      cleanupDataDebits,
      cleanupBundles,
      cleanupSystemData,
      cleanupData
    )

    DatabaseInfo.db.run(cleanupQuery)
  }

  def cleanupPropertyCrossrefs = {
    DBIO.seq(
      ThingsSystempropertydynamiccrossref.delete,
      ThingsSystempropertystaticcrossref.delete,
      ThingsSystemtypecrossref.delete,

      PeopleSystempropertydynamiccrossref.delete,
      PeopleSystempropertystaticcrossref.delete,
      PeopleSystemtypecrossref.delete,

      OrganisationsSystempropertydynamiccrossref.delete,
      OrganisationsSystempropertystaticcrossref.delete,
      OrganisationsSystemtypecrossref.delete,

      LocationsSystempropertydynamiccrossref.delete,
      LocationsSystempropertystaticcrossref.delete,
      LocationsSystemtypecrossref.delete,

      EventsSystempropertydynamiccrossref.delete,
      EventsSystempropertystaticcrossref.delete,
      EventsSystemtypecrossref.delete
    )
  }

  def cleanupEntityCrossrefs = {
    DBIO.seq(
      EventsEventlocationcrossref.delete,
      EventsEventorganisationcrossref.delete,
      EventsEventtoeventcrossref.delete,
      EventsEventpersoncrossref.delete,
      EventsEventthingcrossref.delete,

      OrganisationsOrganisationtoorganisationcrossref.delete,
      OrganisationsOrganisationthingcrossref.delete,
      OrganisationsOrganisationlocationcrossref.delete,

      PeoplePersontopersoncrossref.delete,
      PeoplePersonlocationcrossref.delete,
      PeoplePersonorganisationcrossref.delete,
      PeoplePersontopersonrelationshiptype.delete,

      LocationsLocationthingcrossref.delete,
      LocationsLocationtolocationcrossref.delete,

      ThingsThingtothingcrossref.delete,
      ThingsThingpersoncrossref.delete
    )
  }

  def cleanupEntities = {
    DBIO.seq(
      Entity.delete,
      ThingsThing.delete,
      PeoplePerson.delete,
      OrganisationsOrganisation.delete,
      LocationsLocation.delete,
      EventsEvent.delete
    )
  }

  def cleanupDataDebits = {
    DataDebit.delete
  }

  def cleanupBundles = {
    DBIO.seq(
      BundleContextlessDataSourceDataset.delete,
      BundleContextless.delete,

      BundleContextPropertySelection.delete,
      BundleContextEntitySelection.delete,
      BundleContextToBundleCrossref.delete,
      BundleContext.delete
    )
  }

  def cleanupSystemData = {
    DBIO.seq(
      SystemTypetotypecrossref.delete,
      SystemPropertyrecord.delete,
      SystemProperty.delete,
      SystemType.delete,
      SystemUnitofmeasurement.delete,
      SystemRelationshiprecord.delete
    )
  }

  def cleanupData = {
    DBIO.seq(
      DataValue.delete,
      DataRecord.delete,
      DataField.delete,
      DataTabletotablecrossref.delete,
      DataTable.delete
    )
  }

  def cleanupStats = {
    DBIO.seq(
      StatsDataDebitClessBundleRecords.delete,
      StatsDataDebitDataFieldAccess.delete,
      StatsDataDebitDataTableAccess.delete,
      StatsDataDebitRecordCount.delete,
      StatsDataDebitOperation.delete
    )
  }
}
