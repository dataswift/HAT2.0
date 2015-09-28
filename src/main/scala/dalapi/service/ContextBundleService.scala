package dalapi.service

import dal.SlickPostgresDriver.simple._
import dal.Tables._
import dalapi.models._
import dalapi.DatabaseInfo
import org.joda.time.LocalDateTime
import spray.http.StatusCodes._
import spray.httpx.SprayJsonSupport._
import spray.routing._

import scala.util.{Failure, Try, Success}

// this trait defines our service behavior independently from the service actor
trait ContextBundleService extends HttpService with DatabaseInfo {

  val routes = {
    pathPrefix("bundles") {
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

  /*
   * Stores bundle table provided from the incoming API call
   */
  private def storeBundleContext(bundleContext: ApiBundleContext)(implicit session: Session): Try[ApiBundleContext] = {
    // Require the bundle to be based on a data table that already exists
    bundleContext.entity.id match {
      case Some(bundlecontextId) =>
        val bundleContextRow = new bundleContextRow(0, parentbundleId, LocalDateTime.now(), LocalDateTime.now(), bundleTable.name, entityselectionId)

        // Using Try to handle errors
        Try((BundleContext returning BundleContext) += bundleContextRow) flatMap { insertedBundleContext =>
          // Convert from database format to API format
          val insertedApiBundleContext = ApiBundleContext.fromBundleContext(insertedBundleContext)(bundleContext.table)
          // A partial function to store all table slices related to this bundle table
          def storeBundlePropertySlice = storePropertySlice (insertedApiBundleContext) _

          val apiPropertySlices = bundleContext.slices map { propertySlices =>
            val propertyslices = bundlePropertySlices.map(storeBundlePropertySlice)
            // Flattens to a simple Try with list if slices or returns the first error that occurred
            flatten(propertyslices)
          }

          apiPropertySlices match {
            case Some(Success(propertyslices)) =>
              Success(insertedApiBundleContext.copy(propertyslices = Some(propertyslices)))
            case Some(Failure(e)) =>
              Failure(e)
            case None =>
              // Bundle Table with no slicing
              Success(insertedApiBundleContext)
          }

        }
      case None =>
        Failure(new IllegalArgumentException("Table provided for bundling must contain ID"))
    }

  }

   private def getBundleContextById(bundleContextId: Int)(implicit session: Session): Option[ApiBundleContext] = {
    // Traversing entity graph the Slick way
    val tableQuery = for {
      bundleContext <- BundleContext.filter(_.id === bundleContextId)
      entitySelection <- bundleContext.entitySelectionFk
    } yield (bundleContext, entitySelection)

    val table = tableQuery.run.headOption

    // Map back from database types to API ones
    bundleContext map {
      case (bundleContext: bundleContextRow, entitySelection: entitySelectionRow) =>
        val apiDataTable = ApiEntitySelection.fromentitySelection(entitySelection)(None)(None)
        val apibundleContext = ApibundleContext.frombundleContext(bundleContext)(apiEntitySelection)
        val slices = getBundleContextSlices(apibundleContext)
        apibundleContext.copy(slices = slices)
    }
  }

  private def storePropertySlice(bundleContext: ApiBundleContext)
                        (slice: ApiBundlePropertySlice)
                        (implicit session: Session): Try[ApiBundlePropertySlice] = {

    (bundleContext.table.id, bundleContext.id, slice.table.id) match {
      case (None, _, _) =>
        Failure(new IllegalArgumentException("Table provided for bundling must contain ID"))

      case (slice.table.id, Some((bundleContextId), Some(sliceTableId)) =>

        val sliceRow = new bundlePropertySliceRow(
          0, LocalDateTime.now(), LocalDateTime.now(), propertyrecordcrossreId)

        Try((bundlePropertySlice returning bundlePropertySlice) += sliceRow) flatMap { insertedSlice =>
          val insertedApibundlePropertySlice = ApibundlePropertySlice.frombundlePropertySlice(insertedSlice)(slice.table)

          def storeSliceCondition = storeCondition(insertedApibundlePropertySlice) _
          val conditions = slice.conditions.map(storeSliceCondition)

          // If all conditions have been inserted successfully,
          // Return the complete ApibundlePropertySlice object;
          // Otherwise, the first error that occurred
          flatten(conditions) map { apiConditions =>
            insertedApibundlePropertySlice.copy(conditions = apiConditions)
          }
        }

      case _ =>
        Failure(new IllegalArgumentException("Bundle table must refer to the same table as each slice of the table"))
    }

  }

  private def getbundlePropertySlices(bundleContext: ApiBundleContext)
                                  (implicit session: Session): Option[Seq[ApibundlePropertySlice]] = {
    bundleContext.id map { bundleContextId =>
      // Traversing entity graph the Slick way
      val slicesQuery = for {
        slice <- bundlePropertySlice.filter(_.bundleContextId === bundleContextId)
        bundleProperyRecordCrossRef <- slice.bundleProperyRecordCrossRefFk
      } yield (slice, bundleProperyRecordCrossRef)

      val slices = slicesQuery.run

      slices map { case (slice: bundlePropertySliceRow, bundleProperyRecordCrossRef: BundleProperyRecordCrossRefRow) =>
        // Returning the Data Table information of the slice without subTables or fields
        val apiBundleProperyRecordCrossRef = ApBundleProperyRecordCrossRef.fromBundleProperyRecordCrossRef(bundleProperyRecordCrossRef)(None)(None)
        val apiSlice = ApibundlePropertySlice.frombundlePropertySlice(slice)(apiBundleProperyRecordCrossRef)
        val conditions = getSliceConditions(apiSlice)
        apiSlice.copy(conditions = conditions)
      }
    }
  }

  private def storeCondition(bundlePropertySlice: ApibundlePropertySlice)
                            (propertyCondition: ApiPropertySliceCondition)
                            (implicit session: Session): Try[ApiBundleTableCondition] = {

    (bundlePropertySlice.table.id, condition.field.id, bundlePropertySlice.id) match {
      case (Some(condition.field.tableId), Some(propertyrecordcrossreId), Some(sliceId)) =>
        val conditionRow = new bundlePropertySliceconditionRow(
          0, LocalDateTime.now(), LocalDateTime.now(),
          propertysliceId, condition.operator.toString, condition.value)

        Try((bundlePropertySlicecondition returning bundlePropertySlicecondition) += conditionRow) map { insertedCondition =>
          ApiBundleTableCondition.frombundlePropertySliceCondition(insertedCondition)(condition.field)
        }

      case _ =>
        Failure(new IllegalArgumentException("Invalid table slice or table field provided"))

    }
  }

  private def getSliceConditions(bundlePropertySlice: ApibundlePropertySlice)
                                (implicit session: Session): Seq[ApiBundleTableCondition] = {
    bundlePropertySlice.id match {
      case Some(propertySliceId) =>
        // Traversing entity graph the Slick way
        val conditionsQuery = for {
          c <- bundlePropertySlicecondition.filter(_.propertySliceId === bundlePropertySlice.id)
          f <- c.propertyrecordcrossreIdFk
        } yield (c, f)

        val conditions = conditionsQuery.run
        conditions map { case (condition:bundlePropertySliceconditionRow, field:ApiPropertyRecordCrossRefIdRow) =>
          val apiField = ApiPropertyRecordCrossRef.fromDataField(field)
          ApiPropertySliceCondition.frombundlePropertySliceCondition(condition)(apiField)
        }

      case None =>
        Seq()
    }
  }

   private def getBundleContextById(bundleContextId: Int)(implicit session: Session): Option[ApiBundleContext] = {
    BundleContext.filter(_.id === bundleContextId).run.headOption map { bundlecontext =>
      // Traversing entity graph the Slick way
      // A fairly complex case with a lot of related data that needs to be retrieved for each bundle join
      val tableQuery = for {
        join <- BundleContextJoin.filter(_.bundleContextId === bundleContextId)
        bundleContext <- join.bundleContextFk
        propertyRecordCrossRef <- join.propertyRecordCrossRefFk
        entitySelection <- join.entitySelectionFk
      } yield (join, bundleContext, propertyRecordCrossRef, entitySelection)

      val tables = tableQuery.run

      val apiTables = tables.map {
        case (join:BundleContextJoin, bundleContext: BundleContextRow, entitySelection: EntitySelectionRow, propertyRecordCrossRef: PropertyRecordCrossRef) =>
          val apiPropertyRecordCrossRef = ApiPropertyRecordCrossRef.fromPropertyRecordCrossRef(propertyRecordCrossRef)(None)(None)
          val apiEntitySelection = ApiBEntitySelection.fromEntitySelection(entitySelection)(apiEntitySelection)
          val apiBundleContext = ApiBundleContext.fromDataField(bundleContextd)
          ApiBundleCombination.frombundleContext(join)(Some(apiJoinField), Some(propertyRecordCrossRef), entitySelection)
      }

      ApiBundleContext.fromBundleContextTables(bundle)(apiTables)
    }

  }

  protected def flatten[T](xs: Seq[Try[T]]): Try[Seq[T]] = {
    val (ss: Seq[Success[T]]@unchecked, fs: Seq[Failure[T]]@unchecked) =
      xs.partition(_.isSuccess)

    if (fs.isEmpty) Success(ss map (_.get))
    else Failure[Seq[T]](fs(0).exception) // Only keep the first failure


}
\ No newline at end of file