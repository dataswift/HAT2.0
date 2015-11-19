package hatdex.hat.api.endpoints

import akka.event.LoggingAdapter
import hatdex.hat.api.DatabaseInfo
import hatdex.hat.api.json.JsonProtocol
import hatdex.hat.api.models._
import hatdex.hat.api.service.BundleService
import hatdex.hat.authentication.HatServiceAuthHandler
import hatdex.hat.authentication.models.User
import spray.http.StatusCodes._
import spray.httpx.SprayJsonSupport._
import spray.routing._

import scala.util.{Failure, Success}

// this trait defines our service behavior independently from the service actor
trait BundlesContext extends HttpService with BundleService with HatServiceAuthHandler {

  val db = DatabaseInfo.db

  val logger: LoggingAdapter

  val routes = {
    pathPrefix("bundles" / "context") {
      createBundleContext ~
        getBundleTable ~
        createBundleConext ~
        getBundleContext ~
        getBundleContextValues ~
        getEntitySelection ~
        getEntitySelectionValues ~
        getBundlePropertyRecordCrossRef ~
        getBundlrePropertyRecordCrossRefValues
    }
  }

  import JsonProtocol._

  /*
   * Creates a bundle table as per POST'ed data, including table information and its slicing conditions
   */
  def createBundleContext = path("context") {
    post {
      entity(as[ApiBundleContext]) { bundleContext =>
        db.withSession { implicit session =>
          val result = storeBundleContext(bundleContext)

          complete {
            result match {
              case Success(storedBundleContext) =>
                (Created, storedBundleContext)
              case Failure(e) =>
                (BadRequest, e.getMessage)
            }
          }
        }
      }
    }
  }

  /*
   * Retrieves bundle table structure by ID
   */
  def getBundleContext = path("context" / IntNumber) {
    (bundleContextId: Int) =>
      get {
        db.withSession { implicit session =>
          val bundleContext = getBundleContextById(bundleContextId)
          complete {
            bundleContext match {
              case Some(table) =>
                table
              case None =>
                (NotFound, s"Bundle Table ${bundleTableId} not found")
            }
          }
        }
      }
  }

  /*
   * Retrieves bundle table data
   */
  def getBundleContextValues = path("context" / IntNumber / "values") {
    (bundleTableId: Int) =>
      get {
        complete {
          (NotImplemented, s"Data retrieval for Conext bundles not yet implemented")
        }
      }
  }


  /*
   * Creates a new virtual table for storing arbitrary incoming data
   */
  def createBundleContext = path("context") {
    post {
      entity(as[ApiBundleContext]) { bundle =>
        db.withSession { implicit session =>
          val result = storeBundleContext(bundle)

          complete {
            result match {
              case Success(storedBundle) =>
                (Created, storedBundle)
              case Failure(e) =>
                (BadRequest, e.getMessage)
            }
          }

        }
      }
    }
  }

  /*
   * Retrieves Conext bundle structure by ID
   */
  def getEntitySelection = path("selection" / IntNumber) {
    (entityselectionId: Int) =>
      get {
        db.withSession { implicit session =>
          val entityselection = getEntitySelectionById(entityselectionId)

          complete {
            bundle match {
              case Some(foundentityselection) =>
                foundentityselection
              case None =>
                (NotFound, s"Context Bundle $bundleId not found")
            }
          }
        }
      }
  }

  /*
   * Retrieves Conext bundle data
   */
  def getEntitySelectionValues = path("selection" / IntNumber / "values") {
    (entityselectionId: Int) =>
      get {
        complete {
          (NotImplemented, s"Data retrieval for Conext bundles not yet implemented")
        }
      }
  }

  /*
   * Retrieves Conext bundle structure by ID
   */
  def getBundlePropertyRecordCrossRef = path("selection" / IntNumber) {
    (bundlepropertyrecordcrossrefId: Int) =>
      get {
        db.withSession { implicit session =>
          val bundlepropertyrecordcrossref = bundlepropertyrecordcrossrefById(bundlepropertyrecordcrossrefId)

          complete {
            bundle match {
              case Some(foundbundlepropertyrecordcrossref) =>
                foundbundlepropertyrecordcrossref
              case None =>
                (NotFound, s"Context Bundle $bundleId not found")
            }
          }
        }
      }
  }

  /*
   * Retrieves Conext bundle data
   */
  def getBundlePropertyRecordCrossRefValues = path("selection" / IntNumber / "values") {
    (bundlepropertyrecordcrossrefId: Int) =>
      get {
        complete {
          (NotImplemented, s"Data retrieval for Conext bundles not yet implemented")
        }
      }
  }
}