package hatdex.hat.api.endpoints

import akka.event.LoggingAdapter
import hatdex.hat.api.DatabaseInfo
import hatdex.hat.api.json.JsonProtocol
import hatdex.hat.api.models._
import hatdex.hat.api.service.{BundleContextService, BundleService}
import hatdex.hat.authentication.HatServiceAuthHandler
import hatdex.hat.authentication.models.User
import spray.http.StatusCodes._
import spray.httpx.SprayJsonSupport._
import spray.routing._

import scala.util.{Failure, Success}

// this trait defines our service behavior independently from the service actor
trait BundlesContext extends HttpService with BundleContextService with HatServiceAuthHandler {

  val db = DatabaseInfo.db

  val logger: LoggingAdapter

  val routes = {
    pathPrefix("bundles" / "context") {
      createBundleContext ~
        getBundleContext ~
        getBundleContextValues ~
        addEntitySelectionApi ~
        addPropertySelectionApi
    }
  }

  import JsonProtocol._

  /*
   * Creates a bundle table as per POST'ed data, including table information and its slicing conditions
   */
  def createBundleContext = pathEnd {
    post {
      entity(as[ApiBundleContext]) { bundleContext =>
        db.withSession { implicit session =>
          val result = storeBundleContext(bundleContext)

          complete {
            result match {
              case Success(storedBundleContext) =>
                (Created, storedBundleContext)
              case Failure(e) =>
                (BadRequest, ErrorMessage("Could not create Contextual Bundle", e.getMessage))
            }
          }
        }
      }
    }
  }

  /*
   * Retrieves bundle table structure by ID
   */
  def getBundleContext = path(IntNumber) { (bundleContextId: Int) =>
    get {
      db.withSession { implicit session =>
        val maybeBundleContext = getBundleContextById(bundleContextId)
        session.close()
        complete {
          maybeBundleContext match {
            case Some(bundleContext) =>
              bundleContext
            case None =>
              (NotFound, ErrorMessage("Bundle Not Found", s"Bundle ${bundleContextId} not found or empty"))
          }
        }
      }
    }
  }

  /*
   * Retrieves bundle table data
   */
  def getBundleContextValues = path(IntNumber / "values") { (bundleContextId: Int) =>
    get {
      val maybeBundleData = db.withSession { implicit session =>
        val bundleData = getBundleContextData(bundleContextId)
        session.close()
        bundleData
      }
      complete {
        maybeBundleData
      }
    }
  }

  /*
   * Retrieves Conext bundle structure by ID
   */
  def addEntitySelectionApi = path(IntNumber / "entitySelection") { (bundleId: Int) =>
    post {
      entity(as[ApiBundleContextEntitySelection]) { entitySelection =>
        db.withSession { implicit session =>
          val maybeInsertedSelection = storeBundleContextEntitySelection(bundleId, entitySelection)
          session.close()

          complete {
            maybeInsertedSelection match {
              case Success(insertedSelection) =>
                (Created, insertedSelection)
              case Failure(e) =>
                (BadRequest, ErrorMessage("Could not add Entity Selection", e.getMessage))
            }
          }
        }
      }
    }
  }

  def addPropertySelectionApi = path(IntNumber / "entitySelection" / IntNumber / "propertySelection") { (bundleId: Int, entitySelectionId: Int) =>
    post {
      entity(as[ApiBundleContextPropertySelection]) { propertySelection =>
        db.withSession { implicit session =>
          val maybeInsertedSelection = storeBundlePropertySelection(entitySelectionId, propertySelection)
          session.close()

          complete {
            maybeInsertedSelection match {
              case Success(insertedSelection) =>
                (Created, insertedSelection)
              case Failure(e) =>
                (BadRequest, ErrorMessage("Could not add Property Selection", e.getMessage))
            }
          }
        }
      }
    }
  }
}