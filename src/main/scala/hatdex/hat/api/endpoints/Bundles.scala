package hatdex.hat.api.endpoints

import akka.event.LoggingAdapter
import hatdex.hat.api.DatabaseInfo
import hatdex.hat.api.json.JsonProtocol
import hatdex.hat.api.models._
import hatdex.hat.api.service.BundleService
import hatdex.hat.authentication.HatServiceAuthHandler
import hatdex.hat.authentication.models.User
import hatdex.hat.authentication.authorization.UserAuthorization
import spray.http.StatusCodes._
import spray.httpx.SprayJsonSupport._
import spray.routing._

import scala.util.{ Failure, Success }

// this trait defines our service behavior independently from the service actor
trait Bundles extends HttpService with BundleService with HatServiceAuthHandler {

  val db = DatabaseInfo.db

  val logger: LoggingAdapter

  val routes = {
    pathPrefix("bundles" / "contextless") {
      createBundleContextless ~
        createBundleTable ~
        getBundleTable ~
        getBundleContextless ~
        getBundleTableValuesApi ~
        getBundleContextlessValuesApi
    }
  }

  import JsonProtocol._

  /*
   * Creates a new virtual table for storing arbitrary incoming data
   */
  def createBundleContextless = pathEnd {
    post {
      accessTokenHandler { implicit user: User =>
        authorize(UserAuthorization.withRole("owner")) {
          logger.debug(s"BundleService POST /bundles/contextless authenticated")
          entity(as[ApiBundleContextless]) { bundle =>
            logger.debug(s"BundleService POST /bundles/contextless parsed")
            db.withSession { implicit session =>
              val result = storeBundleContextless(bundle)
              session.close()
              complete {
                result match {
                  case Success(storedBundle) => (Created, storedBundle)
                  case Failure(e)            => (BadRequest, ErrorMessage("Error creating bundle", e.getMessage))
                }
              }
            }
          }
        }
      }
    }
  }

  /*
   * Creates a bundle table as per POST'ed data, including table information and its slicing conditions
   */
  def createBundleTable = path("table") {
    post {
      accessTokenHandler { implicit user: User =>
        authorize(UserAuthorization.withRole("owner")) {
          logger.debug(s"BundleService POST /bundles/contextless/table authenticated")
          entity(as[ApiBundleTable]) { bundleTable =>
            logger.debug("BundleService POST /bundles/contextless/table")
            db.withSession { implicit session =>
              val result = storeBundleTable(bundleTable)
              session.close()
              complete {
                result match {
                  case Success(storedBundleTable) => (Created, storedBundleTable)
                  case Failure(e)                 => (BadRequest, ErrorMessage("Error creating bundle table", e.getMessage))
                }
              }
            }
          }
        }
      }
    }
  }

  /*
   * Retrieves bundle table structure by ID
   */
  def getBundleTable = path("table" / IntNumber) { (bundleTableId: Int) =>
    get {
      accessTokenHandler { implicit user: User =>
        authorize(UserAuthorization.withRole("owner")) {
          logger.debug(s"BundleService GET /bundles/contextless/table/$bundleTableId")
          db.withSession { implicit session =>
            val bundleTable = getBundleTableById(bundleTableId)
            session.close()
            complete {
              bundleTable match {
                case Some(table) => table
                case None        => (NotFound, ErrorMessage("Bundle Not Found", s"Bundle Table $bundleTableId not found"))
              }
            }
          }
        }
      }
    }
  }

  /*
   * Retrieves bundle table data
   */
  def getBundleTableValuesApi = path("table" / IntNumber / "values") { (bundleTableId: Int) =>
    get {
      accessTokenHandler { implicit user: User =>
        authorize(UserAuthorization.withRole("owner")) {
          logger.debug(s"BundleService GET /bundles/contextless/table/$bundleTableId/values")
          db.withSession { implicit session =>
            val someResults = getBundleTableValues(bundleTableId)
            session.close()

            complete {
              someResults match {
                case Some(results) => results
                case None          => (NotFound, ErrorMessage("Contextless Bundle Not Found", s"Contextless Bundle Table $bundleTableId not found"))
              }
            }
          }
        }
      }
    }
  }

  /*
   * Retrieves contextless bundle structure by ID
   */
  def getBundleContextless = path(IntNumber) { (bundleId: Int) =>
    get {
      accessTokenHandler { implicit user: User =>
        authorize(UserAuthorization.withRole("owner")) {
          logger.debug(s"BundleService GET /bundles/contextless/$bundleId")
          db.withSession { implicit session =>
            val bundle = getBundleContextlessById(bundleId)
            session.close()
            complete {
              bundle match {
                case Some(foundBundle) => foundBundle
                case None              => (NotFound, ErrorMessage("Contextless bundle not found", s"Contextless Bundle $bundleId not found"))
              }
            }
          }
        }
      }
    }
  }

  /*
   * Retrieves contextless bundle data
   */
  def getBundleContextlessValuesApi = path(IntNumber / "values") { (bundleId: Int) =>
    get {
      accessTokenHandler { implicit user: User =>
        authorize(UserAuthorization.withRole("owner")) {
          logger.debug(s"BundleService GET /bundles/contextless/$bundleId/values")
          db.withSession { implicit session =>
            val someResults = getBundleContextlessValues(bundleId)
            session.close()
            complete {
              someResults match {
                case Some(results) => results
                case None          => (NotFound, ErrorMessage("Contextless Bundle not found", s"Contextless Bundle $bundleId not found"))
              }
            }
          }
        }
      }
    }
  }
}