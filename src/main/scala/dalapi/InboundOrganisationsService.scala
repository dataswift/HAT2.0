package dalapi

import com.typesafe.config.ConfigFactory
import dal.SlickPostgresDriver.simple._
import dal.Tables._
import dalapi.models._
import org.joda.time.LocalDateTime
import spray.http.MediaTypes._
import spray.http.StatusCodes._
import spray.httpx.SprayJsonSupport._
import spray.routing._


// this trait defines our service behavior independently from the service actor
trait InboundOrganisationsService extends HttpService with InboundService {

  val routes = {
    pathPrefix("inbound") {
      createOrganisation ~
      linkOrganisationToLocation ~
      linkOrganisationToOrganisation ~
      linkOrganisationToPropertyStatic ~
      linkOrganisationToPropertyDynamic// ~
//      addOrganisationType
    }
  }

  import ApiJsonProtocol._

  def createOrganisation = path("organisation") {
    post {
      respondWithMediaType(`application/json`) {
        entity(as[ApiOrganisation]) { organisation =>
          db.withSession { implicit session =>
            val organisationRow = new OrganisationsOrganisationRow(0, LocalDateTime.now(), LocalDateTime.now(), organisation.name)
            val orgId = (OrganisationsOrganisation returning OrganisationsOrganisation.map(_.id)) += organisationRow
            complete(Created, {
              organisation.copy(id = Some(orgId))
            })
          }

        }
      }
    }
  }

  def linkOrganisationToLocation = path("organisation" / IntNumber / "location" / IntNumber) { (organisationId : Int, locationId : Int) =>
    post {
      entity(as[ApiRelationship]) { relationship =>
        db.withSession { implicit session =>
          val recordId = createRelationshipRecord(s"organisation/$organisationId/location/$locationId:${relationship.relationshipType}")

          val crossref = new OrganisationsOrganisationlocationcrossrefRow(0, LocalDateTime.now(), LocalDateTime.now(), locationId, organisationId, relationship.relationshipType, true, recordId)
          val crossrefId = (OrganisationsOrganisationlocationcrossref returning OrganisationsOrganisationlocationcrossref.map(_.id)) += crossref

          // Return the created crossref
          complete {
            ApiGenericId(crossrefId)
          }

        }
      }
    }
  }

  /*
   * Link two organisations together, e.g. as one organisation part of another organisation with a parentChild relationship type
   */
  def linkOrganisationToOrganisation = path("organisation" / IntNumber / "organisation" / IntNumber) { (organisationId : Int, toOrganisationId : Int) =>
    post {
      entity(as[ApiRelationship]) { relationship =>
        db.withSession { implicit session =>
          val recordId = createRelationshipRecord(s"organisation/$organisationId/organisation/$toOrganisationId:${relationship.relationshipType}")

          // Create the crossreference record and insert into db
          val crossref = new OrganisationsOrganisationtoorganisationcrossrefRow(0, LocalDateTime.now(), LocalDateTime.now(), organisationId, toOrganisationId, relationship.relationshipType, true, recordId)
          val crossrefId = (OrganisationsOrganisationtoorganisationcrossref returning OrganisationsOrganisationtoorganisationcrossref.map(_.id)) += crossref

          // Return the created crossref
          complete {
            ApiGenericId(crossrefId)
          }

        }
      }
    }
  }

  /*
   * Link organisation to a property statically (tying it in with a specific record ID)
   */
  def linkOrganisationToPropertyStatic = path("organisation" / IntNumber / "property" / IntNumber / "static") { (organisationId: Int, propertyId: Int) =>
    post {
      entity(as[ApiPropertyRelationshipStatic]) { relationship =>

        val recordId = createPropertyRecord(s"organisation/$organisationId/property/$propertyId:${relationship.relationshipType}(${relationship.fieldId},${relationship.recordId},${relationship.relationshipType})")

        // Create the crossreference record and insert into db
        val crossref = new OrganisationsSystempropertystaticcrossrefRow(
          0, LocalDateTime.now(), LocalDateTime.now(),
          organisationId, propertyId,
          relationship.fieldId, relationship.recordId, relationship.relationshipType,
          true, recordId
        )

        db.withSession { implicit session =>
          val crossrefId = (OrganisationsSystempropertystaticcrossref returning OrganisationsSystempropertystaticcrossref.map(_.id)) += crossref
          // Return the created crossref
          complete {
            ApiGenericId(crossrefId)
          }
        }
      }
    }
  }

  /*
   * Link organisation to a property dynamically
   */
  def linkOrganisationToPropertyDynamic = path("organisation" / IntNumber / "property" / IntNumber / "dynamic") { (organisationId: Int, propertyId: Int) =>
    post {
      entity(as[ApiPropertyRelationshipDynamic]) { relationship =>

        val recordId = createPropertyRecord(s"organisation/$organisationId/property/$propertyId:${relationship.relationshipType}(${relationship.fieldId},${relationship.relationshipType})")

        // Create the crossreference record and insert into db
        val crossref = new OrganisationsSystempropertydynamiccrossrefRow(
          0, LocalDateTime.now(), LocalDateTime.now(),
          organisationId, propertyId,
          relationship.fieldId, relationship.relationshipType,
          true, recordId
        )

        db.withSession { implicit session =>
          val crossrefId = (OrganisationsSystempropertydynamiccrossref returning OrganisationsSystempropertydynamiccrossref.map(_.id)) += crossref
          // Return the created crossref
          complete {
            ApiGenericId(crossrefId)
          }
        }
      }
    }
  }

  /*
   * Tag organisation with a type
   */
//  def addOrganisationType = path("organisation" / IntNumber / "type" / IntNumber) { (organisationId: Int, typeId: Int) =>
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
}

