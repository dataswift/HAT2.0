package hatdex.hat.api

import hatdex.hat.dal.SlickPostgresDriver.simple._
import hatdex.hat.dal.Tables._

object TestDataCleanup {
  def cleanupPropertyCrossrefs(implicit session: Session) = {
    ThingsSystempropertydynamiccrossref.delete
    ThingsSystempropertystaticcrossref.delete
    PeopleSystempropertydynamiccrossref.delete
    PeopleSystempropertystaticcrossref.delete
    OrganisationsSystempropertydynamiccrossref.delete
    OrganisationsSystempropertystaticcrossref.delete
    LocationsSystempropertydynamiccrossref.delete
    LocationsSystempropertystaticcrossref.delete
    EventsSystempropertydynamiccrossref.delete
    EventsSystempropertystaticcrossref.delete
    SystemPropertyrecord.delete
  }

  def cleanupEntityCrossrefs(implicit session: Session) = {
    EventsEventlocationcrossref.delete
    EventsEventorganisationcrossref.delete
    EventsEventtoeventcrossref.delete
    EventsEventpersoncrossref.delete
    EventsEventthingcrossref.delete
    OrganisationsOrganisationtoorganisationcrossref.delete
    OrganisationsOrganisationthingcrossref.delete
    OrganisationsOrganisationlocationcrossref.delete
    PeoplePersontopersoncrossref.delete
    PeoplePersonlocationcrossref.delete
    PeoplePersonorganisationcrossref.delete
    PeoplePersontopersonrelationshiptype.delete
    LocationsLocationthingcrossref.delete
    LocationsLocationtolocationcrossref.delete
    ThingsThingtothingcrossref.delete
    ThingsThingpersoncrossref.delete
  }

  def cleanupEntities(implicit session: Session) = {
    ThingsThing.delete
    PeoplePerson.delete
    OrganisationsOrganisation.delete
    LocationsLocation.delete
    EventsEvent.delete
  }

  def cleanupBundles(implicit session: Session) = {
    BundleTableslicecondition.delete
    BundleTableslice.delete
    BundleJoin.delete
    BundleContextless.delete
    BundleTable.delete
  }

  def cleanupSystemData(implicit session: Session) = {
    SystemTypetotypecrossref.delete
    SystemProperty.delete
    SystemType.delete
    SystemUnitofmeasurement.delete
  }
  
  def cleanupData(implicit session: Session) = {
    DataValue.delete
    DataRecord.delete
    DataField.delete
    DataTabletotablecrossref.delete
    DataTable.delete
  }

  def cleanupAll(implicit session: Session) = {
    cleanupPropertyCrossrefs
    cleanupEntityCrossrefs
    cleanupEntities
    cleanupBundles
    cleanupSystemData
    cleanupData
  }
}
