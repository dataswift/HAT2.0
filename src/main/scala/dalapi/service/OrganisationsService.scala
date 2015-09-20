package dalapi.service

import dal.SlickPostgresDriver.simple._
import dal.Tables._
import dalapi.models._
import org.joda.time.LocalDateTime
import spray.http.MediaTypes._
import spray.http.StatusCodes._
import spray.httpx.SprayJsonSupport._
import spray.routing._

import scala.util.{Failure, Success, Try}


// this trait defines our service behavior independently from the service actor
trait OrganisationsService extends EntityServiceApi {
  val entityKind = "organisation"

  val routes = {
    pathPrefix(entityKind) {
      create ~
      linkToLocation ~
      linkToOrganisation ~
      linkToThing ~
      linkToPropertyStatic ~
      linkToPropertyDynamic// ~
//      addOrganisationType
    }
  }

  import JsonProtocol._

  def createEntity = entity(as[ApiOrganisation]) { organisation =>
    db.withSession { implicit session =>
      val organisationsorganisationRow = new OrganisationsOrganisationRow(0, LocalDateTime.now(), LocalDateTime.now(), organisation.name)
      val result = Try((OrganisationsOrganisation returning OrganisationsOrganisation) += organisationsorganisationRow)

      complete {
        result match {
          case Success(createdOrganisation) =>
            ApiOrganisation.fromDbModel(createdOrganisation)
          case Failure(e) =>
            (BadRequest, e.getMessage)
        }
      }
    }
  }

  protected def createLinkLocation(entityId: Int, locationId: Int, relationshipType: String, recordId: Int)
                                  (implicit session: Session): Try[Int] = {
    // FIXME: locationID and OrganisationID swapped around in the DB!
    val crossref = new OrganisationsOrganisationlocationcrossrefRow(0, LocalDateTime.now(), LocalDateTime.now(),
      locationId, entityId, relationshipType, true, recordId)
    Try((OrganisationsOrganisationlocationcrossref returning OrganisationsOrganisationlocationcrossref.map(_.id)) += crossref)
  }

  protected def createLinkThing(entityId: Int, thingId: Int, relationshipType: String, recordId: Int)
                               (implicit session: Session): Try[Int] = {
    // FIXME: thingID and OrganisationID swapped around in the DB!
    // FIXME: OrganisationsOrganisationthingcrossrefRow ID is STRING
//    val crossref = new OrganisationsOrganisationthingcrossrefRow(0, LocalDateTime.now(), LocalDateTime.now(),
//      thingId, entityId, relationshipType, true, recordId)
//    Try((OrganisationsOrganisationthingcrossref returning OrganisationsOrganisationthingcrossref.map(_.id)) += crossref)
    Failure(new NotImplementedError("Operation Not Supprted"))
  }

  protected def createLinkOrganisation(entityId: Int, organisationId: Int, relationshipType: String, recordId: Int)
                                      (implicit session: Session): Try[Int] = {
    val crossref = new OrganisationsOrganisationtoorganisationcrossrefRow(0, LocalDateTime.now(), LocalDateTime.now(),
      entityId, organisationId, relationshipType, true, recordId)
    Try((OrganisationsOrganisationtoorganisationcrossref returning OrganisationsOrganisationtoorganisationcrossref.map(_.id)) += crossref)
  }

  protected def createLinkPerson(entityId: Int, personId: Int, relationshipType: String, recordId: Int)
                                (implicit session: Session): Try[Int] = {
    Failure(new NotImplementedError("Operation Not Supprted"))
  }

  protected def createLinkEvent(entityId: Int, eventId: Int, relationshipType: String, recordId: Int)
                               (implicit session: Session): Try[Int] = {
    Failure(new NotImplementedError("Operation Not Supprted"))
  }
  
  def linkOrganisationToLocation = path(IntNumber / "location" / IntNumber) { (organisationId: Int, locationId: Int) =>
    post {
      entity(as[ApiRelationship]) { relationship =>
        db.withSession { implicit session =>
          val recordId = createRelationshipRecord(s"organisation/$organisationId/location/$locationId:${relationship.relationshipType}")

          val crossref = new OrganisationsOrganisationlocationcrossrefRow(1, LocalDateTime.now(), LocalDateTime.now(),
            locationId, organisationId, relationship.relationshipType, true, recordId)

          val result = Try((OrganisationsOrganisationlocationcrossref returning OrganisationsOrganisationlocationcrossref.map(_.id)) += crossref)

          complete {
            result match {
              case Success(crossrefId) =>
                (Created, ApiGenericId(crossrefId))
              case Failure(e) =>
                (BadRequest, e.getMessage)
            }
          }

        }
      }
    }
  }

  /*
   * Link two organisations together, e.g. as one organisation part of another organisation with a parentChild relationship type
   */
  def linkOrganisationToOrganisation = path(IntNumber / "organisation" / IntNumber) { (organisationId: Int, organisation2Id: Int) =>
    post {
      entity(as[ApiRelationship]) { relationship =>
        db.withSession { implicit session =>
          val recordId = createRelationshipRecord(s"organisation/$organisationId/organisation/$organisation2Id:${relationship.relationshipType}")

          val crossref = new OrganisationsOrganisationtoorganisationcrossrefRow(0, LocalDateTime.now(), LocalDateTime.now(),
            organisationId, organisation2Id, relationship.relationshipType, true, recordId)
          val result = Try((OrganisationsOrganisationtoorganisationcrossref returning OrganisationsOrganisationtoorganisationcrossref.map(_.id)) += crossref)

          // Return the created crossref
          complete {
            result match {
              case Success(crossrefId) =>
                (Created, ApiGenericId(crossrefId))
              case Failure(e) =>
                (BadRequest, e.getMessage)
            }
          }

        }
      }
    }
  }

  /*
   * Link organisation to a property statically (tying it in with a specific record ID)
   */
  protected def createPropertyLinkDynamic(entityId: Int, propertyId: Int,
                                                   fieldId: Int, relationshipType: String, propertyRecordId: Int)
                                                  (implicit session: Session) : Try[Int] = {
    val crossref = new OrganisationsSystempropertydynamiccrossrefRow(
      0, LocalDateTime.now(), LocalDateTime.now(),
      entityId, propertyId,
      fieldId, relationshipType,
      true, propertyRecordId)

    Try((OrganisationsSystempropertydynamiccrossref returning OrganisationsSystempropertydynamiccrossref.map(_.id)) += crossref)
  }

  /*
   * Link organisation to a property dynamically
   */
  protected def createPropertyLinkStatic(entityId: Int, propertyId: Int,
                                                  recordId: Int, fieldId: Int, relationshipType: String, propertyRecordId: Int)
                                                 (implicit session: Session) : Try[Int] = {
    val crossref = new OrganisationsSystempropertystaticcrossrefRow(
      0, LocalDateTime.now(), LocalDateTime.now(),
      entityId, propertyId,
      recordId, fieldId, relationshipType,
      true, propertyRecordId)

    Try((OrganisationsSystempropertystaticcrossref returning OrganisationsSystempropertystaticcrossref.map(_.id)) += crossref)
  }

  /*
   * Tag organisation with a type
   */
  protected def addEntityType(entityId: Int, typeId: Int, relationship: ApiRelationship)
                             (implicit session: Session) : Try[Int] = {

    Failure(new NotImplementedError("Adding entity to organisations not supported"))
//    val organisationType = new OrganisationsSystemtypecrossrefRow(0, LocalDateTime.now(), LocalDateTime.now(), organisationId, typeId, relationship.relationshipType, true)
//    Try((OrganisationsSystemtypecrossref returning OrganisationsSystemtypecrossref.map(_.id)) += organisationType)
  }

  def getLocations(organisationId: Int)
                  (implicit session: Session): Seq[ApiLocationRelationship] = {

    val locationLinks = OrganisationsOrganisationlocationcrossref.filter(_.organisationId === organisationId).run

    locationLinks flatMap { link: OrganisationsOrganisationlocationcrossrefRow =>
      val apiLocation = getLocation(link.locationId)
      apiLocation.map { location =>
        new ApiLocationRelationship(link.relationshipType, location)
      }
    }
  }

  def getOrganisations(organisationID: Int)
               (implicit session: Session): Seq[ApiOrganisationRelationship] = {
    val organisationLinks = OrganisationsOrganisationtoorganisationcrossref.filter(_.organisationOneId === organisationID).run

    organisationLinks flatMap { link: OrganisationsOrganisationtoorganisationcrossrefRow =>
      val apiOrganisation = getOrganisation(link.organisationTwoId)
      apiOrganisation.map { organisation =>
        new ApiOrganisationRelationship(link.relationshipType, organisation)
      }
    }
  }

  def getPeople(entityId: Int)
               (implicit session: Session): Seq[ApiPersonRelationship] = {
    Seq()
  }

  def getThings(entityId: Int)
               (implicit session: Session): Seq[ApiThingRelationship] = {
    //FIXME: organisationID is a string, can't match
    Seq()
//    val thingLinks = OrganisationsOrganisationthingcrossref.filter(_.organisationId === entityId).run
//
//    thingLinks flatMap { link: OrganisationsOrganisationthingcrossrefRow =>
//      val apiThing = getThing(link.thingId)
//      apiThing.map { thing =>
//        new ApiThingRelationship(link.relationshipType, thing)
//      }
//    }
  }

  def getEvents(eventID: Int)
               (implicit session: Session): Seq[ApiEventRelationship] = {
    Seq()
  }

  protected def getPropertiesStatic(organisationId: Int)
                                   (implicit session: Session): Seq[ApiPropertyRelationshipStatic] = {

    val crossrefQuery = OrganisationsSystempropertystaticcrossref.filter(_.organisationId === organisationId)

    val dataQuery = for {
      crossref <- crossrefQuery
      property <- crossref.systemPropertyFk
      propertyType <- property.systemTypeFk
      propertyUom <- property.systemUnitofmeasurementFk
      field <- crossref.dataFieldFk
      record <- crossref.dataRecordFk
    } yield (crossref, property, propertyType, propertyUom, field, record)

    val data = dataQuery.run

    data.map {
      case (crossref: OrganisationsSystempropertystaticcrossrefRow, property: SystemPropertyRow, propertyType: SystemTypeRow,
      propertyUom: SystemUnitofmeasurementRow, field: DataFieldRow, record: DataRecordRow) =>
        ApiPropertyRelationshipStatic.fromDbModel(crossref, property, propertyType, propertyUom, field, record)
    }
  }

  protected def getPropertiesDynamic(organisationId: Int)
                                    (implicit session: Session): Seq[ApiPropertyRelationshipDynamic] = {

    val crossrefQuery = OrganisationsSystempropertydynamiccrossref.filter(_.organisationId === organisationId)

    val dataQuery = for {
      crossref <- crossrefQuery
      property <- crossref.systemPropertyFk
      propertyType <- property.systemTypeFk
      propertyUom <- property.systemUnitofmeasurementFk
      field <- crossref.dataFieldFk
    } yield (crossref, property, propertyType, propertyUom, field)

    val data = dataQuery.run

    data.map {
      case (crossref: OrganisationsSystempropertydynamiccrossrefRow, property: SystemPropertyRow, propertyType: SystemTypeRow,
      propertyUom: SystemUnitofmeasurementRow, field: DataFieldRow) =>
        ApiPropertyRelationshipDynamic.fromDbModel(crossref, property, propertyType, propertyUom, field)
    }
  }
}

