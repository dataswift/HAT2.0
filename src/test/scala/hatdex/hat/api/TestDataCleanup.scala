package hatdex.hat.api

import hatdex.hat.dal.SlickPostgresDriver.simple._
import hatdex.hat.dal.Tables._

object TestDataCleanup {
  def cleanupPropertyCrossrefs(implicit session: Session) = {
    ThingsSystempropertydynamiccrossref.delete
    ThingsSystempropertystaticcrossref.delete
    ThingsSystemtypecrossref.delete

    PeopleSystempropertydynamiccrossref.delete
    PeopleSystempropertystaticcrossref.delete
    PeopleSystemtypecrossref.delete

    OrganisationsSystempropertydynamiccrossref.delete
    OrganisationsSystempropertystaticcrossref.delete
    OrganisationsSystemtypecrossref.delete

    LocationsSystempropertydynamiccrossref.delete
    LocationsSystempropertystaticcrossref.delete
    LocationsSystemtypecrossref.delete

    EventsSystempropertydynamiccrossref.delete
    EventsSystempropertystaticcrossref.delete
    EventsSystemtypecrossref.delete
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
    Entity.delete
    ThingsThing.delete
    PeoplePerson.delete
    OrganisationsOrganisation.delete
    LocationsLocation.delete
    EventsEvent.delete
  }

  def cleanupDataDebits(implicit session: Session) = {
    DataDebit.delete
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
    SystemPropertyrecord.delete
    SystemProperty.delete
    SystemType.delete
    SystemUnitofmeasurement.delete
    SystemRelationshiprecord.delete
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
    cleanupDataDebits
    cleanupBundles
    cleanupSystemData
    cleanupData
  }
}
