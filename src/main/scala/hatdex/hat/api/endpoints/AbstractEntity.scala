package hatdex.hat.api.endpoints

import hatdex.hat.api.DatabaseInfo
import hatdex.hat.api.json.JsonProtocol
import hatdex.hat.api.models._
import hatdex.hat.api.service.AbstractEntityService
import hatdex.hat.authentication.HatServiceAuthHandler
import hatdex.hat.authentication.models.User
import hatdex.hat.dal.SlickPostgresDriver.simple._
import hatdex.hat.dal.Tables._
import spray.http.MediaTypes._
import spray.http.StatusCodes._
import spray.httpx.SprayJsonSupport._
import spray.routing._

import scala.util.{Failure, Success, Try}

trait AbstractEntity extends HttpService with AbstractEntityService with HatServiceAuthHandler {

  import JsonProtocol._
  val db = DatabaseInfo.db

  def createApi = {
    post {
      createEntity
    }
  }

  def getApi = path(IntNumber) { (entityId: Int) =>
    get {
      userPassHandler { implicit user: User =>
        db.withSession { implicit session =>
          implicit val getValues: Boolean = false
          val entity = getEntity(entityId)
          session.close()
          entity
        }
      }
    }
  }

  def getAllApi = pathEnd {
    respondWithMediaType(`application/json`) {
      get {
        userPassHandler { implicit user =>
          db.withSession { implicit session =>
            val entities = getAllEntitiesSimple
            session.close()
            entities
          }
        }
      }
    }
  }

  def getApiValues = path(IntNumber / "values") { (entityId: Int) =>
    get {
      userPassHandler { implicit user: User =>
        db.withSession { implicit session =>
          implicit val getValues: Boolean = true
          val entity = getEntity(entityId)
          session.close()
          entity
        }
      }
    }
  }

  private def getAllEntitiesSimple(implicit session: Session) = {
    import JsonProtocol._
    import spray.json._
    val result = entityKind match {
      case "person" =>
        PeoplePerson.run.map(ApiPerson.fromDbModel).toJson
      case "thing" =>
        ThingsThing.run.map(ApiThing.fromDbModel).toJson
      case "event" =>
        EventsEvent.run.map(ApiEvent.fromDbModel).toJson
      case "location" =>
        LocationsLocation.run.map(ApiLocation.fromDbModel).toJson
      case "organisation" =>
        OrganisationsOrganisation.run.map(ApiOrganisation.fromDbModel).toJson
      case _ => Seq[ApiPerson]().toJson
    }

    complete {
      result.toString
    }
  }

  private def getEntity(entityId: Int)(implicit session: Session, getValues: Boolean) = {
    val result = entityKind match {
      case "person" => getPerson(entityId, recursive = true)
      case "thing" => getThing(entityId, recursive = true)
      case "event" => getEvent(entityId, recursive = true)
      case "location" => getLocation(entityId, recursive = true)
      case "organisation" => getOrganisation(entityId, recursive = true)
      case _ => None
    }

    complete {
      result match {
        case Some(entity: ApiPerson) =>
          entity
        case Some(entity: ApiThing) =>
          entity
        case Some(entity: ApiEvent) =>
          entity
        case Some(entity: ApiLocation) =>
          entity
        case Some(entity: ApiOrganisation) =>
          entity
        case _ =>
          (NotFound, ErrorMessage("NotFound", s"$entityKind with ID $entityId not found"))
      }
    }
  }

  def linkToLocation = path(IntNumber / "location" / IntNumber) { (entityId: Int, locationId: Int) =>
    post {
      entity(as[ApiRelationship]) { relationship =>
        db.withSession { implicit session =>
          val recordId = createRelationshipRecord(s"$entityKind/$entityId/location/$locationId:${relationship.relationshipType}")

          val result = createLinkLocation(entityId, locationId, relationship.relationshipType, recordId)

          session.close()

          complete {
            result match {
              case Success(crossrefId) =>
                (Created, ApiGenericId(crossrefId))
              case Failure(e) =>
                (BadRequest, ErrorMessage(s"Error Linking ${entityKind} to Location", e.getMessage))
            }
          }

        }
      }
    }
  }

  def linkToOrganisation = path(IntNumber / "organisation" / IntNumber) { (entityId: Int, organisationId: Int) =>
    post {
      entity(as[ApiRelationship]) { relationship =>
        db.withSession { implicit session =>
          val recordId = createRelationshipRecord(s"$entityKind/$entityId/organisation/$organisationId:${relationship.relationshipType}")

          val result = createLinkOrganisation(entityId, organisationId, relationship.relationshipType, recordId)

          session.close()

          complete {
            result match {
              case Success(crossrefId) =>
                (Created, ApiGenericId(crossrefId))
              case Failure(e) =>
                (BadRequest, ErrorMessage(s"Error Linking ${entityKind} to Organisation", e.getMessage))
            }
          }

        }
      }
    }
  }

  def linkToPerson = path(IntNumber / "person" / IntNumber) { (entityId: Int, personId: Int) =>
    post {
      entity(as[ApiRelationship]) { relationship =>
        db.withSession { implicit session =>
          val recordId = createRelationshipRecord(s"$entityKind/$entityId/person/$personId:${relationship.relationshipType}")

          val result = createLinkPerson(entityId, personId, relationship.relationshipType, recordId)

          session.close()

          // Return the created crossref
          complete {
            result match {
              case Success(crossrefId) =>
                (Created, ApiGenericId(crossrefId))
              case Failure(e) =>
                (BadRequest, ErrorMessage(s"Error Linking ${entityKind} to Person", e.getMessage))
            }
          }

        }
      }
    }
  }

  def linkToThing = path(IntNumber / "thing" / IntNumber) { (entityId: Int, thingId: Int) =>
    post {
      entity(as[ApiRelationship]) { relationship =>
        db.withSession { implicit session =>
          val recordId = createRelationshipRecord(s"$entityKind/$entityId/thing/$thingId:${relationship.relationshipType}")

          val result = createLinkThing(entityId, thingId, relationship.relationshipType, recordId)

          session.close()

          // Return the created crossref
          complete {
            result match {
              case Success(crossrefId) =>
                (Created, ApiGenericId(crossrefId))
              case Failure(e) =>
                (BadRequest, ErrorMessage(s"Error Linking ${entityKind} to Thing", e.getMessage))
            }
          }

        }
      }
    }
  }

  def linkToEvent = path(IntNumber / "event" / IntNumber) { (entityId: Int, eventId: Int) =>
    post {
      entity(as[ApiRelationship]) { relationship =>
        db.withSession { implicit session =>
          val recordId = createRelationshipRecord(s"$entityKind/$entityId/event/$eventId:${relationship.relationshipType}")

          val result = createLinkEvent(entityId, eventId, relationship.relationshipType, recordId)

          session.close()

          // Return the created crossref
          complete {
            result match {
              case Success(crossrefId) =>
                (Created, ApiGenericId(crossrefId))
              case Failure(e) =>
                (BadRequest, ErrorMessage(s"Error Linking ${entityKind} to Event", e.getMessage))
            }
          }

        }
      }
    }
  }

  def getPropertiesStaticApi = path(IntNumber / "property" / "static") {
    (entityId: Int) =>
      get {
        db.withSession { implicit session: Session =>
          complete {
            implicit val getValues: Boolean = false
            val properties = getPropertiesStatic(entityId, None)
            session.close()
            properties
          }
        }
      }
  }

  def getPropertiesDynamicApi = path(IntNumber / "property" / "dynamic") {
    (entityId: Int) =>
      get {
        db.withSession { implicit session: Session =>
          complete {
            implicit val getValues: Boolean = false
            val properties = getPropertiesDynamic(entityId, None)
            session.close()
            properties
          }
        }
      }
  }

  def getPropertiesStaticValuesApi = path(IntNumber / "property" / "static" / "values") {
    (entityId: Int) =>
      get {
        db.withSession { implicit session: Session =>
          complete {
            implicit val getValues: Boolean = true
            val properties = getPropertiesStatic(entityId, None)
            session.close()
            properties
          }
        }
      }
  }

  def getPropertiesDynamicValuesApi = path(IntNumber / "property" / "dynamic" / "values") {
    (entityId: Int) =>
      get {
        db.withSession { implicit session: Session =>
          complete {
            implicit val getValues: Boolean = true
            val properties = getPropertiesDynamic(entityId, None)
            session.close()
            properties
          }
        }
      }
  }

  // staticproperty as a way of differentiating between a property that is linked statically 
  // and the propertyRelatonship link
  def getPropertyStaticValueApi = path(IntNumber / "property" / "static" / IntNumber / "values") {
    (entityId: Int, propertyRelationshipId: Int) =>
      get {
        db.withSession { implicit session: Session =>
          complete {
            val propertyValues = getPropertyStaticValues(entityId, propertyRelationshipId)
            session.close()
            propertyValues
          }
        }
      }
  }

  // dynamicproperty as a way of differentiating between a property that is linked dynamically 
  // and the propertyRelatonship link
  def getPropertyDynamicValueApi = path(IntNumber / "property" / "dynamic" / IntNumber / "values") {
    (entityId: Int, propertyRelationshipId: Int) =>
      get {
        db.withSession { implicit session: Session =>
          complete {
            val propertyValues = getPropertyDynamicValues(entityId, propertyRelationshipId)
            session.close()
            propertyValues
          }
        }
      }
  }

  /*
   * Tag event with a type
   */
  def addTypeApi = path(IntNumber / "type" / IntNumber) { (entityId: Int, typeId: Int) =>
    post {
      entity(as[ApiRelationship]) { relationship =>
        db.withSession { implicit session =>
          val result = addEntityType(entityId, typeId, relationship)
          session.close()
          complete {
            result match {
              case Success(crossrefId) =>
                (Created, ApiGenericId(crossrefId))
              case Failure(e) =>
                (BadRequest, ErrorMessage(s"Error Adding Type to ${entityKind} ${entityId}", e.getMessage))
            }
          }
        }
      }

    }
  }

  /*
   * Link event to a property statically (tying it in with a specific record ID)
   */
  def linkToPropertyStatic = path(IntNumber / "property" / "static" / IntNumber) { (entityId: Int, propertyId: Int) =>
    post {
      entity(as[ApiPropertyRelationshipStatic]) { relationship =>
        val result: Try[Int] = (relationship.field.id, relationship.record.id) match {
          case (Some(fieldId), Some(recordId)) =>
            db.withSession { implicit session =>
              val propertyRecordId = createPropertyRecord(
                s"$entityKind/$entityId/property/static/$propertyId:${relationship.relationshipType}($fieldId,$recordId,${relationship.relationshipType}")

              val propertyLink = createPropertyLinkStatic(entityId, propertyId, recordId, fieldId, relationship.relationshipType, propertyRecordId)
              session.close()
              propertyLink
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
              (BadRequest, ErrorMessage("Error Linking Property Statically", e.getMessage))
          }
        }

      }
    }
  }

  /*
   * Link event to a property dynamically
   */
  def linkToPropertyDynamic = path(IntNumber / "property" / "dynamic" / IntNumber) { (entityId: Int, propertyId: Int) =>
    post {
      entity(as[ApiPropertyRelationshipDynamic]) { relationship =>
        val result: Try[Int] = relationship.field.id match {
          case Some(fieldId) =>
            db.withSession { implicit session =>
              val propertyRecordId = createPropertyRecord(
                s"""$entityKind/$entityId/property/dynamic/$propertyId:${relationship.relationshipType}
                   |($fieldId,${relationship.relationshipType})""".stripMargin)

              val propertyLink = createPropertyLinkDynamic(entityId, propertyId, fieldId, relationship.relationshipType, propertyRecordId)
              session.close()
              propertyLink
            }
          case None =>
            Failure(new IllegalArgumentException("Property relationship must have an existing Data Field with ID"))
        }

        complete {
          result match {
            case Success(crossrefId) =>
              (Created, ApiGenericId(crossrefId))
            case Failure(e) =>
              (BadRequest, ErrorMessage("Error Linking Property Dynamically", e.getMessage))
          }
        }
      }
    }
  }
}
