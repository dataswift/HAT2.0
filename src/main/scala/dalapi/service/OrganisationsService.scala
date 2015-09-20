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
trait OrganisationsService extends HttpService with InboundService with EntityService {

  val routes = {
    pathPrefix("organisation") {
      createOrganisation ~
      linkOrganisationToLocation ~
      linkOrganisationToOrganisation ~
      linkOrganisationToPropertyStatic ~
      linkOrganisationToPropertyDynamic// ~
//      addOrganisationType
    }
  }

  import JsonProtocol._

  def createOrganisation = path("") {
    post {
      entity(as[ApiOrganisation]) { organisation =>
        db.withSession { implicit session =>
          val organisationsorganisationRow = new OrganisationsOrganisationRow(0, LocalDateTime.now(), LocalDateTime.now(), organisation.name)
          val result = Try((OrganisationsOrganisation returning OrganisationsOrganisation.map(_.id)) += organisationsorganisationRow)

          complete {
            result match {
              case Success(organisationId) =>
                organisation.copy(id = Some(organisationId))
              case Failure(e) =>
                (BadRequest, e.getMessage)
            }
          }

        }

      }
    }
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
  def linkOrganisationToPropertyStatic = path(IntNumber / "property" / "static" / IntNumber) { (organisationId: Int, propertyId: Int) =>
    post {
      entity(as[ApiPropertyRelationshipStatic]) { relationship =>
        val result: Try[Int] = (relationship.field.id, relationship.record.id) match {
          case (Some(fieldId), Some(recordId)) =>
            val propertyRecordId = createPropertyRecord(
              s"organisation/$organisationId/property/$propertyId:${relationship.relationshipType}(${fieldId},${recordId},${relationship.relationshipType}")

            // Create the crossreference record and insert into db
            val crossref = new OrganisationsSystempropertystaticcrossrefRow(
              0, LocalDateTime.now(), LocalDateTime.now(),
              organisationId, propertyId,
              recordId, fieldId, relationship.relationshipType,
              true, propertyRecordId
            )

            db.withSession { implicit session =>
              Try((OrganisationsSystempropertystaticcrossref returning OrganisationsSystempropertystaticcrossref.map(_.id)) += crossref)
            }
          case (None, _) =>
            Failure(new IllegalArgumentException("Property relationship must have an existing Data Field with ID"))
          case (_, None) =>
            Failure(new IllegalArgumentException("Static Property relationship must have an existing Data Record with ID"))
        }

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

  /*
   * Link organisation to a property dynamically
   */
  def linkOrganisationToPropertyDynamic = path(IntNumber / "property" / "dynamic" / IntNumber ) { (organisationId: Int, propertyId: Int) =>
    post {
      entity(as[ApiPropertyRelationshipDynamic]) { relationship =>
        val result: Try[Int] = relationship.field.id match {
          case Some(fieldId) =>
            val propertyRecordId = createPropertyRecord(
              s"""organisation/$organisationId/property/$propertyId:${relationship.relationshipType}
                  |(${fieldId},${relationship.relationshipType})""".stripMargin)

            // Create the crossreference record and insert into db
            val crossref = new OrganisationsSystempropertydynamiccrossrefRow(
              0, LocalDateTime.now(), LocalDateTime.now(),
              organisationId, propertyId,
              fieldId, relationship.relationshipType,
              true, propertyRecordId)

            db.withSession { implicit session =>
              Try((OrganisationsSystempropertydynamiccrossref returning OrganisationsSystempropertydynamiccrossref.map(_.id)) += crossref)
            }
          case None =>
            Failure(new IllegalArgumentException("Property relationship must have an existing Data Field with ID"))
        }

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

  /*
   * Tag organisation with a type
   */
//  def addOrganisationType = path(IntNumber / "type" / IntNumber) { (organisationId: Int, typeId: Int) =>
//    post {
//      entity(as[ApiRelationship]) { relationship =>
//        db.withSession { implicit session =>
//          val organisationType = new OrganisationsSystemtypecrossrefRow(0, LocalDateTime.now(), LocalDateTime.now(), organisationId, typeId, relationship.relationshipType, true)
//          val organisationTypeId = (OrganisationsSystemtypecrossref returning OrganisationsSystemtypecrossref.map(_.id)) += organisationType
//          complete {
//            ApiGenericId(organisationTypeId)
//          }
//        }
//      }
//
//    }
//  }

  def getPropertiesStaticApi = path(IntNumber / "property" / "static") {
    (eventId: Int) =>
      get {
        db.withSession { implicit session =>
          complete {
            getPropertiesStatic(eventId)
          }
        }
      }
  }

  def getPropertiesDynamicApi = path(IntNumber / "property" / "dynamic") {
    (eventId: Int) =>
      get {
        db.withSession { implicit session =>
          complete {
            getPropertiesDynamic(eventId)
          }
        }
      }
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
    var organisationIds = organisationLinks.map(_.organisationTwoId)

    organisationLinks flatMap { link: OrganisationsOrganisationtoorganisationcrossrefRow =>
      val apiOrganisation = getOrganisation(link.organisationTwoId)
      apiOrganisation.map { organisation =>
        new ApiOrganisationRelationship(link.relationshipType, organisation)
      }
    }
  }

  def getPeople(eventID: Int)
               (implicit session: Session): Seq[ApiPersonRelationship] = {
    Seq();
  }

  def getThings(eventID: Int)
               (implicit session: Session): Seq[ApiThingRelationship] = {
    Seq();
  }

  def getEvents(eventID: Int)
               (implicit session: Session): Seq[ApiEventRelationship] = {
    Seq();
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

