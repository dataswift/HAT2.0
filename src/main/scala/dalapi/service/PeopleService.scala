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
trait PeopleService extends EntityServiceApi {
  val entityKind = "person"

  val routes = {
    pathPrefix(entityKind) {
      createApi ~
      getApi ~
      createPersonRelationshipType ~
      linkToPerson ~
      linkToLocation ~
      linkToOrganisation ~
      linkToPropertyStatic ~
      linkToPropertyDynamic ~
      addTypeApi ~
      getPropertiesStaticApi ~
      getPropertiesDynamicApi
    }
  }

  import JsonProtocol._

  def createEntity = entity(as[ApiPerson]) { person =>
    db.withSession { implicit session =>
      val personspersonRow = new PeoplePersonRow(0, LocalDateTime.now(), LocalDateTime.now(), person.name, person.personId)
      val result = Try((PeoplePerson returning PeoplePerson) += personspersonRow)

      complete {
        result match {
          case Success(createdPerson) =>
            val newEntity = new EntityRow(0, LocalDateTime.now(), LocalDateTime.now(), createdPerson.name, "person", None, None, None, None, Some(createdPerson.id))
            Try(Entity += newEntity)
            ApiPerson.fromDbModel(createdPerson)
          case Failure(e) =>
            (BadRequest, e.getMessage)
        }
      }
    }
  }

  def createPersonRelationshipType = path("relationshipType") {
    post {
        entity(as[ApiPersonRelationshipType]) { relationship =>
          db.withSession { implicit session =>
            val reltypeRow = new PeoplePersontopersonrelationshiptypeRow(0, LocalDateTime.now(), LocalDateTime.now(), relationship.name, relationship.description)
            val reltypeId = (PeoplePersontopersonrelationshiptype returning PeoplePersontopersonrelationshiptype.map(_.id)) += reltypeRow
            complete(Created, {
              relationship.copy(id = Some(reltypeId))
            })
          }

        }
    }
  }

  /*
   * Link two people together, e.g. as one person part of another person with a given relationship type
   */
  override def linkToPerson = path(IntNumber / "person" / IntNumber) { (personId: Int, person2Id: Int) =>
    post {
      entity(as[ApiPersonRelationshipType]) { relationship =>
        db.withSession { implicit session =>
          val recordId = createRelationshipRecord(s"$entityKind/$personId/person/$person2Id:${relationship.name}")

          val result = relationship.id match {
            case Some(relationshipTypeId) =>
              createLinkPerson(personId, person2Id, relationshipTypeId, recordId)
            case None =>
              Failure(new IllegalArgumentException("People can only be linked with an existing relationship type"))
          }

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

  protected def createLinkPerson(entityId: Int, personId: Int, relationshipType: String, recordId: Int)
                                (implicit session: Session): Try[Int] = {
    Failure(new IllegalArgumentException("People must be linked via a defined relationship type"))
  }

  protected def createLinkPerson(entityId: Int, personId: Int, relationshipTypeId: Int, recordId: Int)
                                      (implicit session: Session): Try[Int] = {
    val crossref = new PeoplePersontopersoncrossrefRow(0, LocalDateTime.now(), LocalDateTime.now(),
      entityId, personId, recordId, true, relationshipTypeId)
    Try((PeoplePersontopersoncrossref returning PeoplePersontopersoncrossref.map(_.id)) += crossref)
  }

  protected def createLinkLocation(entityId: Int, locationId: Int, relationshipType: String, recordId: Int)
                                  (implicit session: Session): Try[Int] = {
    val crossref = new PeoplePersonlocationcrossrefRow(0, LocalDateTime.now(), LocalDateTime.now(),
      entityId, locationId, relationshipType, true, recordId)
    Try((PeoplePersonlocationcrossref returning PeoplePersonlocationcrossref.map(_.id)) += crossref)
  }

  protected def createLinkOrganisation(entityId: Int, organisationId: Int, relationshipType: String, recordId: Int)
                                      (implicit session: Session): Try[Int] = {
    val crossref = new PeoplePersonorganisationcrossrefRow(0, LocalDateTime.now(), LocalDateTime.now(),
      entityId, organisationId, relationshipType, true, recordId)
    Try((PeoplePersonorganisationcrossref returning PeoplePersonorganisationcrossref.map(_.id)) += crossref)
  }

  protected def createLinkEvent(entityId: Int, eventId: Int, relationshipType: String, recordId: Int)
                               (implicit session: Session): Try[Int] = {
    Failure(new NotImplementedError("Operation Not Supprted"))
  }

  protected def createLinkThing(entityId: Int, thingId: Int, relationshipType: String, recordId: Int)
                               (implicit session: Session): Try[Int] = {
    Failure(new NotImplementedError("Operation Not Supprted"))
  }

  /*
   * Link person to a property statically (tying it in with a specific record ID)
   */
  protected def createPropertyLinkDynamic(entityId: Int, propertyId: Int,
                                                   fieldId: Int, relationshipType: String, propertyRecordId: Int)
                                                  (implicit session: Session) : Try[Int] = {
    val crossref = new PeopleSystempropertydynamiccrossrefRow(
      0, LocalDateTime.now(), LocalDateTime.now(),
      entityId, propertyId,
      fieldId, relationshipType,
      true, propertyRecordId)

    Try((PeopleSystempropertydynamiccrossref returning PeopleSystempropertydynamiccrossref.map(_.id)) += crossref)
  }

  /*
  * Link person to a property dynamically
  */
  protected def createPropertyLinkStatic(entityId: Int, propertyId: Int,
                                                  recordId: Int, fieldId: Int, relationshipType: String, propertyRecordId: Int)
                                                 (implicit session: Session) : Try[Int] = {
    val crossref = new PeopleSystempropertystaticcrossrefRow(
      0, LocalDateTime.now(), LocalDateTime.now(),
      entityId, propertyId,
      recordId, fieldId, relationshipType,
      true, propertyRecordId)

    Try((PeopleSystempropertystaticcrossref returning PeopleSystempropertystaticcrossref.map(_.id)) += crossref)
  }

  /*
   * Tag person with a type
   */
  protected def addEntityType(entityId: Int, typeId: Int, relationship: ApiRelationship)
                             (implicit session: Session) : Try[Int] = {

    val entityType = new PeopleSystemtypecrossrefRow(0, LocalDateTime.now(), LocalDateTime.now(),
      entityId, typeId, relationship.relationshipType, true)
    Try((PeopleSystemtypecrossref returning PeopleSystemtypecrossref.map(_.id)) += entityType)
  }

  def getLocations(personId: Int)
                  (implicit session: Session, getValues: Boolean): Seq[ApiLocationRelationship] = {

    val locationLinks = PeoplePersonlocationcrossref.filter(_.personId === personId).run

    locationLinks flatMap { link: PeoplePersonlocationcrossrefRow =>
      val apiLocation = getLocation(link.locationId)
      apiLocation.map { location =>
        new ApiLocationRelationship(link.relationshipType, location)
      }
    }
  }

  def getOrganisations(personID: Int)
                      (implicit session: Session, getValues: Boolean): Seq[ApiOrganisationRelationship] = {
    val links = PeoplePersonorganisationcrossref.filter(_.personId === personID).run

    links flatMap { link: PeoplePersonorganisationcrossrefRow =>
      val apiOrganisation = getOrganisation(link.organisationId)
      apiOrganisation.map { organisation =>
        new ApiOrganisationRelationship(link.relationshipType, organisation)
      }
    }
  }

  def getPeople(personID: Int)
               (implicit session: Session, getValues: Boolean): Seq[ApiPersonRelationship] = {
    val links = for {
      link <- PeoplePersontopersoncrossref.filter(_.personOneId === personID)
      relationship <- link.peoplePersontopersonrelationshiptypeFk
    } yield (link, relationship)

    links.run flatMap {
      case (link: PeoplePersontopersoncrossrefRow, rel: PeoplePersontopersonrelationshiptypeRow) =>
        val apiPerson = getPerson(link.personTwoId)
        apiPerson.map { person =>
          new ApiPersonRelationship(rel.name, person)
        }
    }
  }

  def getThings(eventID: Int)
               (implicit session: Session, getValues: Boolean): Seq[ApiThingRelationship] = {
    Seq();
  }

  def getEvents(eventID: Int)
               (implicit session: Session, getValues: Boolean): Seq[ApiEventRelationship] = {
    Seq();
  }


  protected def getPropertiesStatic(personId: Int)
                                   (implicit session: Session, getValues: Boolean): Seq[ApiPropertyRelationshipStatic] = {

    val crossrefQuery = PeopleSystempropertystaticcrossref.filter(_.personId === personId)
    val properties = getPropertiesStaticQuery(crossrefQuery)
    getValues match {
      case false =>
        properties
      case true =>
        properties.map(propertyService.getPropertyRelationshipValues)
    }
  }

  protected def getPropertiesDynamic(personId: Int)
                                    (implicit session: Session, getValues: Boolean): Seq[ApiPropertyRelationshipDynamic] = {

    val crossrefQuery = PeopleSystempropertydynamiccrossref.filter(_.personId === personId)
    val properties = getPropertiesDynamicQuery(crossrefQuery)
    getValues match {
      case false =>
        properties
      case true =>
        properties.map(propertyService.getPropertyRelationshipValues)
    }
  }

  private def getPropertiesStaticQuery(crossrefQuery: Query[PeopleSystempropertystaticcrossref, PeopleSystempropertystaticcrossrefRow, Seq])
                                      (implicit session: Session): Seq[ApiPropertyRelationshipStatic] = {
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
      case (crossref: PeopleSystempropertystaticcrossrefRow, property: SystemPropertyRow, propertyType: SystemTypeRow,
      propertyUom: SystemUnitofmeasurementRow, field: DataFieldRow, record: DataRecordRow) =>
        ApiPropertyRelationshipStatic.fromDbModel(crossref, property, propertyType, propertyUom, field, record)
    }
  }

  private def getPropertiesDynamicQuery(crossrefQuery: Query[PeopleSystempropertydynamiccrossref, PeopleSystempropertydynamiccrossrefRow, Seq])
                                       (implicit session: Session): Seq[ApiPropertyRelationshipDynamic] = {
    val dataQuery = for {
      crossref <- crossrefQuery
      property <- crossref.systemPropertyFk
      propertyType <- property.systemTypeFk
      propertyUom <- property.systemUnitofmeasurementFk
      field <- crossref.dataFieldFk
    } yield (crossref, property, propertyType, propertyUom, field)

    val data = dataQuery.run

    data.map {
      case (crossref: PeopleSystempropertydynamiccrossrefRow, property: SystemPropertyRow, propertyType: SystemTypeRow,
      propertyUom: SystemUnitofmeasurementRow, field: DataFieldRow) =>
        ApiPropertyRelationshipDynamic.fromDbModel(crossref, property, propertyType, propertyUom, field)
    }
  }
}

