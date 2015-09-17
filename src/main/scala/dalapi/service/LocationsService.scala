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
trait LocationsService extends HttpService with InboundService with EntityService {

  val routes = {
    pathPrefix("location") {
      createLocation ~
      linkLocationToLocation ~
      linkLocationToThing ~
      linkLocationToPropertyStatic ~
      linkLocationToPropertyDynamic ~
      addLocationType
    }
  }

  import JsonProtocol._

  def createLocation = path("") {
    post {
      respondWithMediaType(`application/json`) {
        entity(as[ApiLocation]) { location =>
          db.withSession { implicit session =>
            val locationslocationRow = new LocationsLocationRow(0, LocalDateTime.now(), LocalDateTime.now(), location.name)
            val result = Try((LocationsLocation returning LocationsLocation.map(_.id)) += locationslocationRow)

            complete {
              result match {
                case Success(locationId) =>
                  location.copy(id = Some(locationId))
                case Failure(e) =>
                  (BadRequest, e.getMessage)
              }
            }
          }

        }
      }
    }
  }

  def linkLocationToLocation = path(IntNumber / "location" / IntNumber) { (locationId: Int, location2Id: Int) =>
    post {
      entity(as[ApiRelationship]) { relationship =>
        db.withSession { implicit session =>
          val recordId = createRelationshipRecord(s"location/$locationId/location/$locationId:${relationship.relationshipType}")

          val crossref = new LocationsLocationtolocationcrossrefRow(1, LocalDateTime.now(), LocalDateTime.now(),
            locationId, location2Id, relationship.relationshipType, true, recordId)

          val result = Try((LocationsLocationtolocationcrossref returning LocationsLocationtolocationcrossref.map(_.id)) += crossref)

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

  def linkLocationToThing = path(IntNumber / "thing" / IntNumber) { (locationId : Int, thingId : Int) =>
    post {
      entity(as[ApiRelationship]) { relationship =>
        db.withSession { implicit session =>
          val recordId = createRelationshipRecord(s"location/$locationId/thing/$thingId:${relationship.relationshipType}")

          val crossref = new LocationsLocationthingcrossrefRow(0, LocalDateTime.now(), LocalDateTime.now(),
            thingId, locationId, relationship.relationshipType, true, recordId)
          val result = Try((LocationsLocationthingcrossref returning LocationsLocationthingcrossref.map(_.id)) += crossref)

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
   * Link location to a property statically (tying it in with a specific record ID)
   */
  def linkLocationToPropertyStatic = path(IntNumber / "property" / "static" / IntNumber) { (locationId: Int, propertyId: Int) =>
    post {
      entity(as[ApiPropertyRelationshipStatic]) { relationship =>
        val result: Try[Int] = (relationship.field.id, relationship.record.id) match {
          case (Some(fieldId), Some(recordId)) =>
            val propertyRecordId = createPropertyRecord(
              s"location/$locationId/property/$propertyId:${relationship.relationshipType}(${fieldId},${recordId},${relationship.relationshipType}")

            // Create the crossreference record and insert into db
            val crossref = new LocationsSystempropertystaticcrossrefRow(
              0, LocalDateTime.now(), LocalDateTime.now(),
              locationId, propertyId,
              recordId, fieldId, relationship.relationshipType,
              true, propertyRecordId
            )

            db.withSession { implicit session =>
              Try((LocationsSystempropertystaticcrossref returning LocationsSystempropertystaticcrossref.map(_.id)) += crossref)
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
   * Link location to a property dynamically
   */
  def linkLocationToPropertyDynamic = path(IntNumber / "property" / "dynamic" / IntNumber ) { (locationId: Int, propertyId: Int) =>
    post {
      entity(as[ApiPropertyRelationshipDynamic]) { relationship =>
        val result: Try[Int] = relationship.field.id match {
          case Some(fieldId) =>
            val propertyRecordId = createPropertyRecord(
              s"""location/$locationId/property/$propertyId:${relationship.relationshipType}
                  |(${fieldId},${relationship.relationshipType})""".stripMargin)

            // Create the crossreference record and insert into db
            val crossref = new LocationsSystempropertydynamiccrossrefRow(
              0, LocalDateTime.now(), LocalDateTime.now(),
              locationId, propertyId,
              fieldId, relationship.relationshipType,
              true, propertyRecordId)

            db.withSession { implicit session =>
              Try((LocationsSystempropertydynamiccrossref returning LocationsSystempropertydynamiccrossref.map(_.id)) += crossref)
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
   * Tag location with a type
   */
  def addLocationType = path(IntNumber / "type" / IntNumber) { (locationId: Int, typeId: Int) =>
    post {
      entity(as[ApiRelationship]) { relationship =>
        db.withSession { implicit session =>
          val locationType = new LocationsSystemtypecrossrefRow(0, LocalDateTime.now(), LocalDateTime.now(),
            locationId, typeId, relationship.relationshipType, true)
          val result = Try((LocationsSystemtypecrossref returning LocationsSystemtypecrossref.map(_.id)) += locationType)

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

  def getThings(locationID: Int)
               (implicit session: Session) : Seq[ApiThingRelationship] = {
    val locationLinks = LocationsLocationthingcrossref.filter(_.locationId === locationID).run

    locationLinks flatMap { link : LocationsLocationthingcrossrefRow =>
//      val apiThing = new ApiThing(Some(link.thingId), link.thingId.toString)
//      new ApiThingRelationship(link.relationshipType, apiThing)
      None
    }
  }

  def getLocations(locationID: Int)
               (implicit session: Session) : Seq[ApiLocationRelationship] = {
    val locationLinks = LocationsLocationtolocationcrossref.filter(_.locOneId === locationID).run

    locationLinks flatMap { link : LocationsLocationtolocationcrossrefRow =>
      val apiLocation = getLocation(link.locTwoId)
      apiLocation.map { location =>
        new ApiLocationRelationship(link.relationshipType, location)
      }
    }
  }

  protected def getOrganisations(entityId: Int)(implicit session: Session) : Seq[ApiOrganisationRelationship] = {
    // No links directly from Location
    Seq()
  }

  protected def getPeople(entityId: Int)(implicit session: Session) : Seq[ApiPersonRelationship] = {
    // No links directly from Location
    Seq()
  }

  protected def getEvents(entityId: Int)(implicit session: Session) : Seq[ApiEventRelationship] = {
    // No links directly from Location
    Seq()
  }

  protected def getPropertiesStatic(locationId: Int)
                                 (implicit session: Session): Seq[ApiPropertyRelationshipStatic] = {

    val crossrefQuery = LocationsSystempropertystaticcrossref.filter(_.locationId === locationId)

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
      case (crossref: LocationsSystempropertystaticcrossrefRow, property: SystemPropertyRow, propertyType: SystemTypeRow,
      propertyUom: SystemUnitofmeasurementRow, field: DataFieldRow, record: DataRecordRow) =>
        ApiPropertyRelationshipStatic.fromDbModel(crossref, property, propertyType, propertyUom, field, record)
    }
  }

  protected def getPropertiesDynamic(locationId: Int)
                                  (implicit session: Session): Seq[ApiPropertyRelationshipDynamic] = {

    val crossrefQuery = LocationsSystempropertydynamiccrossref.filter(_.locationId === locationId)

    val dataQuery = for {
      crossref <- crossrefQuery
      property <- crossref.systemPropertyFk
      propertyType <- property.systemTypeFk
      propertyUom <- property.systemUnitofmeasurementFk
      field <- crossref.dataFieldFk
    } yield (crossref, property, propertyType, propertyUom, field)

    val data = dataQuery.run

    data.map {
      case (crossref: LocationsSystempropertydynamiccrossrefRow, property: SystemPropertyRow, propertyType: SystemTypeRow,
      propertyUom: SystemUnitofmeasurementRow, field: DataFieldRow) =>
        ApiPropertyRelationshipDynamic.fromDbModel(crossref, property, propertyType, propertyUom, field)
    }
  }
}

