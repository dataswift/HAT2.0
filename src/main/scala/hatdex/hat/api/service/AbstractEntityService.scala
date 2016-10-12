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
package hatdex.hat.api.service

import akka.event.LoggingAdapter
import hatdex.hat.api.DatabaseInfo
import hatdex.hat.api.models._
import hatdex.hat.dal.SlickPostgresDriver.api._
import hatdex.hat.dal.Tables._
import org.joda.time.LocalDateTime
import spray.routing

import scala.concurrent.{ExecutionContext, Future}

trait AbstractEntityService {
  val entityKind: String
  val logger: LoggingAdapter
  implicit val dalExecutionContext: ExecutionContext

  protected def createEntity: routing.Route

  protected def createLinkLocation(entityId: Int, locationId: Int, relationshipType: String, recordId: Int): Future[Int]

  protected def createLinkOrganisation(entityId: Int, organisationId: Int, relationshipType: String, recordId: Int): Future[Int]

  protected def createLinkPerson(entityId: Int, personId: Int, relationshipType: String, recordId: Int): Future[Int]

  protected def createLinkThing(entityId: Int, thingId: Int, relationshipType: String, recordId: Int): Future[Int]

  protected def createLinkEvent(entityId: Int, eventId: Int, relationshipType: String, recordId: Int): Future[Int]

  protected[api] def getEvent(eventID: Int, recursive: Boolean = false, propertySelectors: Option[Seq[ApiBundleContextPropertySelection]] = None)(implicit getValues: Boolean): Future[Option[ApiEvent]] = {
    logger.debug(s"For $entityKind get Event $eventID")

    val eventualEntity = for {
      e <- DatabaseInfo.db.run(EventsEvent.filter(_.id === eventID).result).map(_.head)
      ps <- getPropertiesStatic(e.id, propertySelectors).map(seqOption)
      pd <- getPropertiesDynamic(e.id, propertySelectors).map(seqOption)
      things <- maybeGetThings(recursive)(e.id)
      people <- maybeGetPeople(recursive)(e.id)
      locations <- maybeGetLocations(recursive)(e.id)
      organisations <- maybeGetOrganisations(recursive)(e.id)
      events <- maybeGetEvents(recursive)(e.id)
    } yield {
      Some(ApiEvent(Some(e.id), e.name, ps, pd, events, locations, people, things, organisations))
    }

    eventualEntity recover {
      case e: NoSuchElementException => None
      case e                         => throw e
    }
  }

  protected[api] def getLocation(locationID: Int, recursive: Boolean = false, propertySelectors: Option[Seq[ApiBundleContextPropertySelection]] = None)(implicit getValues: Boolean): Future[Option[ApiLocation]] = {
    logger.debug(s"For $entityKind get Location $locationID")

    val eventualEntity = for {
      e <- DatabaseInfo.db.run(LocationsLocation.filter(_.id === locationID).result).map(_.head)
      ps <- getPropertiesStatic(e.id, propertySelectors).map(seqOption)
      pd <- getPropertiesDynamic(e.id, propertySelectors).map(seqOption)
      things <- maybeGetThings(recursive)(e.id)
      people <- maybeGetPeople(recursive)(e.id)
      locations <- maybeGetLocations(recursive)(e.id)
      organisations <- maybeGetOrganisations(recursive)(e.id)
      events <- maybeGetEvents(recursive)(e.id)
    } yield {
      Some(ApiLocation(Some(e.id), e.name, ps, pd, locations, things))
    }

    eventualEntity recover {
      case e: NoSuchElementException => None
      case e                         => throw e
    }
  }

  protected[api] def getOrganisation(organisationId: Int, recursive: Boolean = false, propertySelectors: Option[Seq[ApiBundleContextPropertySelection]] = None)(implicit getValues: Boolean): Future[Option[ApiOrganisation]] = {
    logger.debug(s"For $entityKind get Organisation $organisationId")
    val eventualEntity = for {
      e <- DatabaseInfo.db.run(OrganisationsOrganisation.filter(_.id === organisationId).result).map(_.head)
      ps <- getPropertiesStatic(e.id, propertySelectors).map(seqOption)
      pd <- getPropertiesDynamic(e.id, propertySelectors).map(seqOption)
      things <- maybeGetThings(recursive)(e.id)
      people <- maybeGetPeople(recursive)(e.id)
      locations <- maybeGetLocations(recursive)(e.id)
      organisations <- maybeGetOrganisations(recursive)(e.id)
      events <- maybeGetEvents(recursive)(e.id)
    } yield {
      Some(ApiOrganisation(Some(e.id), e.name, ps, pd, organisations, locations, things))
    }

    eventualEntity recover {
      case e: NoSuchElementException => None
      case e                         => throw e
    }
  }

  protected[api] def getPerson(personId: Int, recursive: Boolean = false, propertySelectors: Option[Seq[ApiBundleContextPropertySelection]] = None)(implicit getValues: Boolean): Future[Option[ApiPerson]] = {
    val eventualEntity = for {
      e <- DatabaseInfo.db.run(PeoplePerson.filter(_.id === personId).result).map(_.head)
      ps <- getPropertiesStatic(e.id, propertySelectors).map(seqOption)
      pd <- getPropertiesDynamic(e.id, propertySelectors).map(seqOption)
      locations <- maybeGetLocations(recursive)(e.id)
      organisations <- maybeGetOrganisations(recursive)(e.id)
      people <- maybeGetPeople(recursive)(e.id)
    } yield {
      Some(ApiPerson(Some(e.id), e.name, e.personId, ps, pd, people, locations, organisations))
    }

    eventualEntity recover {
      case e: NoSuchElementException => None
      case e                         => throw e
    }
  }

  protected[api] def getThing(thingId: Int, recursive: Boolean = false, propertySelectors: Option[Seq[ApiBundleContextPropertySelection]] = None)(implicit getValues: Boolean): Future[Option[ApiThing]] = {
    logger.debug(s"For $entityKind get Thing $thingId")

    val eventualEntity = for {
      e <- DatabaseInfo.db.run(ThingsThing.filter(_.id === thingId).result).map(_.head)
      ps <- getPropertiesStatic(e.id, propertySelectors).map(seqOption)
      pd <- getPropertiesDynamic(e.id, propertySelectors).map(seqOption)
      things <- maybeGetThings(recursive)(e.id)
      people <- maybeGetPeople(recursive)(e.id)
    } yield {
      Some(ApiThing(Some(e.id), e.name, ps, pd, things, people))
    }

    eventualEntity recover {
      case e: NoSuchElementException => None
      case e                         => throw e
    }
  }

  protected def createRelationshipRecord(relationshipName: String): Future[Int] = {
    val newRecord = new SystemRelationshiprecordRow(0, LocalDateTime.now(), LocalDateTime.now(), relationshipName)
    DatabaseInfo.db.run((SystemRelationshiprecord returning SystemRelationshiprecord.map(_.id)) += newRecord)
  }

  protected def createPropertyRecord(relationshipName: String): Future[Int] = {
    val newRecord = new SystemPropertyrecordRow(0, LocalDateTime.now(), LocalDateTime.now(), relationshipName)
    DatabaseInfo.db.run((SystemPropertyrecord returning SystemPropertyrecord.map(_.id)) += newRecord)
  }

  protected def getLocations(entityId: Int)(implicit getValues: Boolean): Future[Seq[ApiLocationRelationship]]

  protected def getOrganisations(entityId: Int)(implicit getValues: Boolean): Future[Seq[ApiOrganisationRelationship]]

  protected def getPeople(entityId: Int)(implicit getValues: Boolean): Future[Seq[ApiPersonRelationship]]

  protected def getThings(entityId: Int)(implicit getValues: Boolean): Future[Seq[ApiThingRelationship]]

  protected def getEvents(entityId: Int)(implicit getValues: Boolean): Future[Seq[ApiEventRelationship]]

  protected def getPropertiesStatic(eventId: Int, propertySelectors: Option[Seq[ApiBundleContextPropertySelection]])(implicit getValues: Boolean): Future[Seq[ApiPropertyRelationshipStatic]]

  protected def getPropertiesDynamic(eventId: Int, propertySelectors: Option[Seq[ApiBundleContextPropertySelection]])(implicit getValues: Boolean): Future[Seq[ApiPropertyRelationshipDynamic]]

  protected def getPropertyStaticValues(eventId: Int, propertyRelationshipId: Int): Future[Seq[ApiPropertyRelationshipStatic]]

  protected def getPropertyDynamicValues(eventId: Int, propertyRelationshipId: Int): Future[Seq[ApiPropertyRelationshipDynamic]]

  protected def addEntityType(entityId: Int, typeId: Int, relationship: ApiRelationship): Future[Int]

  protected def createPropertyLinkStatic(entityId: Int, propertyId: Int,
                                         recordId: Int, fieldId: Int, relationshipType: String, propertyRecordId: Int): Future[Int]

  protected def createPropertyLinkDynamic(entityId: Int, propertyId: Int,
                                          fieldId: Int, relationshipType: String, propertyRecordId: Int): Future[Int]

  def maybeGetThings(recursive: Boolean)(id: Int)(implicit getValues: Boolean) =
    if (recursive) {
      getThings(id).map(seqOption)
    }
    else {
      Future.successful(None)
    }

  def maybeGetPeople(recursive: Boolean)(id: Int)(implicit getValues: Boolean) =
    if (recursive) {
      getPeople(id).map(seqOption)
    }
    else {
      Future.successful(None)
    }

  def maybeGetLocations(recursive: Boolean)(id: Int)(implicit getValues: Boolean) =
    if (recursive) {
      getLocations(id).map(seqOption)
    }
    else {
      Future.successful(None)
    }

  def maybeGetEvents(recursive: Boolean)(id: Int)(implicit getValues: Boolean) =
    if (recursive) {
      getEvents(id).map(seqOption)
    }
    else {
      Future.successful(None)
    }

  def maybeGetOrganisations(recursive: Boolean)(id: Int)(implicit getValues: Boolean) =
    if (recursive) {
      getOrganisations(id).map(seqOption)
    }
    else {
      Future.successful(None)
    }

  // Utility function to return None for empty sequences
  private def seqOption[T](seq: Seq[T]): Option[Seq[T]] = {
    if (seq.isEmpty)
      None
    else
      Some(seq)
  }

}
