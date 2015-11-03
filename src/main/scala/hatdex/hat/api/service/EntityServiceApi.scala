package hatdex.hat.api.service

import hatdex.hat.api.json.JsonProtocol
import hatdex.hat.authentication.HatServiceAuthHandler
import hatdex.hat.authentication.models.User
import hatdex.hat.dal.SlickPostgresDriver.simple._
import hatdex.hat.dal.Tables._
import hatdex.hat.api.DatabaseInfo
import hatdex.hat.api.models._
import org.joda.time.LocalDateTime
import spray.http.MediaTypes._
import spray.http.StatusCodes._
import spray.httpx.SprayJsonSupport._
import spray.routing._

import scala.util.{Failure, Success, Try}

trait EntityServiceApi extends HttpService with EntityService with DatabaseInfo with HatServiceAuthHandler {

  import JsonProtocol._

  def createApi = {
    post {
      createEntity
    }
  }

  def getApi = path(IntNumber) { (entityId: Int) =>
    get {
      userPassHandler { implicit user: User =>
        println("Getting entity for user")
        db.withSession { implicit session =>
          implicit val getValues: Boolean = false
          getEntity(entityId)
        }
      }
    }
  }

  def getAllApi = pathEnd {
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

  def getApiValues = path(IntNumber / "values") { (entityId: Int) =>
    get {
      userPassHandler { implicit user: User =>
        println("Getting values for user")
        db.withSession { implicit session =>
          implicit val getValues: Boolean = true
          getEntity(entityId)
        }
      }
    }
  }

  private def getAllEntitiesSimple(implicit session: Session) = {
    import spray.json._
    import JsonProtocol._
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
      case _ => Seq()
    }

    complete {
      result.toString
    }
  }

  private def getEntity(entityId: Int)(implicit session: Session, getValues: Boolean) = {
    val result = entityKind match {
      case "person" => getPerson(entityId)
      case "thing" => getThing(entityId)
      case "event" => getEvent(entityId)
      case "location" => getLocation(entityId)
      case "organisation" => getOrganisation(entityId)
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

  def linkToOrganisation = path(IntNumber / "organisation" / IntNumber) { (entityId: Int, organisationId: Int) =>
    post {
      entity(as[ApiRelationship]) { relationship =>
        db.withSession { implicit session =>
          val recordId = createRelationshipRecord(s"$entityKind/$entityId/organisation/$organisationId:${relationship.relationshipType}")

          val result = createLinkOrganisation(entityId, organisationId, relationship.relationshipType, recordId)

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

  def linkToPerson = path(IntNumber / "person" / IntNumber) { (entityId: Int, personId: Int) =>
    post {
      entity(as[ApiRelationship]) { relationship =>
        db.withSession { implicit session =>
          val recordId = createRelationshipRecord(s"$entityKind/$entityId/person/$personId:${relationship.relationshipType}")

          val result = createLinkPerson(entityId, personId, relationship.relationshipType, recordId)

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

  def linkToThing = path(IntNumber / "thing" / IntNumber) { (entityId: Int, thingId: Int) =>
    post {
      entity(as[ApiRelationship]) { relationship =>
        db.withSession { implicit session =>
          val recordId = createRelationshipRecord(s"$entityKind/$entityId/thing/$thingId:${relationship.relationshipType}")

          val result = createLinkThing(entityId, thingId, relationship.relationshipType, recordId)

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

  def linkToEvent = path(IntNumber / "event" / IntNumber) { (entityId: Int, eventId: Int) =>
    post {
      entity(as[ApiRelationship]) { relationship =>
        db.withSession { implicit session =>
          val recordId = createRelationshipRecord(s"$entityKind/$entityId/event/$eventId:${relationship.relationshipType}")

          val result = createLinkEvent(entityId, eventId, relationship.relationshipType, recordId)

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

  def getPropertiesStaticApi = path(IntNumber / "property" / "static") {
    (entityId: Int) =>
      get {
        db.withSession { implicit session: Session =>
          complete {
            implicit val getValues: Boolean = false
            getPropertiesStatic(entityId)
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
            getPropertiesDynamic(entityId)
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
            getPropertiesStatic(entityId)
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
            getPropertiesDynamic(entityId)
          }
        }
      }
  }

  // staticproperty as a way of differentiating between a property that is linked statically 
  // and the propertyRelatonship link
  def getPropertyStaticValueApi = path(IntNumber / "staticproperty" / IntNumber / "values") {
    (entityId: Int, propertyRelationshipId: Int) =>
      get {
        db.withSession { implicit session: Session =>
          complete {
            getPropertyStaticValues(entityId, propertyRelationshipId)
          }
        }
      }
  }

  // dynamicproperty as a way of differentiating between a property that is linked dynamically 
  // and the propertyRelatonship link
  def getPropertyDynamicValueApi = path(IntNumber / "dynamicproperty" / IntNumber / "values") {
    (entityId: Int, propertyRelationshipId: Int) =>
      get {
        db.withSession { implicit session: Session =>
          complete {
            getPropertyDynamicValues(entityId, propertyRelationshipId)
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
   * Link event to a property statically (tying it in with a specific record ID)
   */
  def linkToPropertyStatic = path(IntNumber / "property" / "static" / IntNumber) { (entityId: Int, propertyId: Int) =>
    post {
      entity(as[ApiPropertyRelationshipStatic]) { relationship =>
        val result: Try[Int] = (relationship.field.id, relationship.record.id) match {
          case (Some(fieldId), Some(recordId)) =>
            val propertyRecordId = createPropertyRecord(
              s"$entityKind/$entityId/property/static/$propertyId:${relationship.relationshipType}($fieldId,$recordId,${relationship.relationshipType}")

            db.withSession { implicit session =>
              createPropertyLinkStatic(entityId, propertyId, recordId, fieldId, relationship.relationshipType, propertyRecordId)
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
   * Link event to a property dynamically
   */
  def linkToPropertyDynamic = path(IntNumber / "property" / "dynamic" / IntNumber) { (entityId: Int, propertyId: Int) =>
    post {
      entity(as[ApiPropertyRelationshipDynamic]) { relationship =>
        val result: Try[Int] = relationship.field.id match {
          case Some(fieldId) =>
            val propertyRecordId = createPropertyRecord(
              s"""$entityKind/$entityId/property/dynamic/$propertyId:${relationship.relationshipType}
                  |($fieldId,${relationship.relationshipType})""".stripMargin)

            db.withSession { implicit session =>
              createPropertyLinkDynamic(entityId, propertyId, fieldId, relationship.relationshipType, propertyRecordId)
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

  protected def createRelationshipRecord(relationshipName: String) = {
    db.withSession { implicit session =>
      val newRecord = new SystemRelationshiprecordRow(0, LocalDateTime.now(), LocalDateTime.now(), relationshipName)
      val record = (SystemRelationshiprecord returning SystemRelationshiprecord) += newRecord
      record.id
    }
  }

  protected def createPropertyRecord(relationshipName: String) = {
    db.withSession { implicit session =>
      val newRecord = new SystemPropertyrecordRow(0, LocalDateTime.now(), LocalDateTime.now(), relationshipName)
      val record = (SystemPropertyrecord returning SystemPropertyrecord) += newRecord
      record.id
    }
  }

}
