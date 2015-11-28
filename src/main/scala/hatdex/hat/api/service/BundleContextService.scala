package hatdex.hat.api.service

import akka.event.LoggingAdapter
import hatdex.hat.Utils
import hatdex.hat.api.models._
import hatdex.hat.dal.SlickPostgresDriver.simple._
import hatdex.hat.dal.Tables._
import org.joda.time.LocalDateTime
import slick.jdbc.GetResult
import slick.jdbc.StaticQuery.interpolation

import scala.util.{Failure, Success, Try}

// this trait defines our service behavior independently from the service actor
trait BundleContextService {

  val logger: LoggingAdapter

  def eventsService: EventsService
  def peopleService: PeopleService
  def thingsService: ThingsService
  def locationsService: LocationsService
  def organisationsService: OrganisationsService

  /*
   * Stores bundle table provided from the incoming API call
   */
  protected def storeBundleContext(bundleContext: ApiBundleContext)(implicit session: Session): Try[ApiBundleContext] = {
    val bundleRow = BundleContextRow(0, LocalDateTime.now(), LocalDateTime.now(), bundleContext.name)
    val maybeBundle = Try((BundleContext returning BundleContext) += bundleRow)

    maybeBundle flatMap { bundle =>
      val entitiesInserted = bundleContext.entities map { entities =>
        val entitiesMaybeInserted = entities map { entity =>
          storeBundleContextEntitySelection(bundle.id, entity)
        }
        Utils.flatten(entitiesMaybeInserted)
      }

//      logger.debug("Inserted entity selectors " + entitiesInserted + " for " + bundle)

      val entities = Utils.reverseOptionTry(entitiesInserted)
//      logger.debug("For stored bundle " + bundle)
      val bundlesInserted = bundleContext.bundles.map { bundles =>
//        logger.debug("Handle subbundles " + bundles)
        val maybeSubBundles = Utils.flatten(bundles.map(storeBundleContext))
//        logger.debug("Inserted subbundles " + maybeSubBundles)

        maybeSubBundles map { subBundles =>
          subBundles map { b =>
//            logger.debug("Linking " + b)
            val relRow = BundleContextToBundleCrossrefRow(0, LocalDateTime.now(), LocalDateTime.now(), bundle.id, b.id.getOrElse(0))
            val inserted = Try(BundleContextToBundleCrossref += relRow)
//            logger.debug("Linked " + inserted)
            inserted
          }
        }

        maybeSubBundles
      }

      val bundles = Utils.reverseOptionTry(bundlesInserted)

      val apiBundle = ApiBundleContext.fromDbModel(bundle)
      (entities, bundles) match {
        case (Failure(e), _) =>
          Failure(e)
        case (_, Failure(e)) =>
          Failure(e)
        case (Success(e), Success(b)) =>
          Success(apiBundle.copy(entities = e, bundles = b))
      }
    }
  }

  protected def getBundleContextById(bundleContextId: Int)(implicit session: Session): Option[ApiBundleContext] = {
//    logger.debug(s"Retrieving bundle ID ${bundleContextId}")
    val bundles = getBundleList(bundleContextId)

//    logger.debug("Constructing a tree for all bundles: " + bundles.toString)
    val result = buildBundleTree(bundles.find(_._1.id.contains(bundleContextId)).map(_._1), bundles)
//    logger.debug("Full bundle tree: " + result + " -- " + bundles.find(_._1.id.contains(bundleContextId)) + " for " + bundleContextId)
    result
  }

  protected def getBundleContextData(bundleContextId: Int)(implicit session: Session): Seq[ApiEntity] = {
    val bundles = getBundleList(bundleContextId).map(_._1)
    val results = bundles map { bundle =>
      bundle.entities map { entities =>
        val allEntities = entities flatMap { entitySelection =>
          val matchingEntity = (entitySelection.entityId, entitySelection.entityKind, entitySelection.entityName) match {
            case (Some(entityId), _, _) =>
              logger.debug("Finding entities by ID only")
              Entity.filter(_.id === entityId)
            case (None, Some(kind), Some(entityName)) =>
              logger.debug("Finding entities by kind and name")
              Entity.filter(_.kind === kind).filter(_.name === entityName)
            case (None, None, Some(entityName)) =>
              logger.debug("Finding entities by name only")
              Entity.filter(_.name === entityName)
            case (None, Some(kind), None) =>
              logger.debug("Finding entities by kind only")
              Entity.filter(_.kind === kind)
            case (None, None, None) =>
              logger.debug("Finding no entity")
              Entity.filter(_.id === 0)
          }
          val maybeStoredEntities = matchingEntity.run
          logger.debug(s"Entity matching entity selector ${entitySelection}: ${maybeStoredEntities}")
          implicit val getValues = true
          val entityValues = maybeStoredEntities map { storedEntity =>
            storedEntity.kind match {
              case "event" =>
                Some(ApiEntity("event", eventsService.getEvent(storedEntity.id, recursive = false, propertySelectors = entitySelection.properties), None, None, None, None))
              case "person" =>
                Some(ApiEntity("person", None, peopleService.getPerson(storedEntity.id, recursive = false, propertySelectors = entitySelection.properties), None, None, None))
              case "thing" =>
                Some(ApiEntity("thing", None, None, None, thingsService.getThing(storedEntity.id, recursive = false, propertySelectors = entitySelection.properties), None))
              case "location" =>
                Some(ApiEntity("location", None, None, locationsService.getLocation(storedEntity.id, recursive = false, propertySelectors = entitySelection.properties), None, None))
              case "organisation" =>
                Some(ApiEntity("organisation", None, None, None, None, organisationsService.getOrganisation(storedEntity.id, recursive = false, propertySelectors = entitySelection.properties)))
              case _ =>
                None
            }
          }
          entityValues.flatten
        }
        allEntities
      }
    }

    results.flatten.flatten
  }

  def retrieveDataDebitContextualValues(debit: DataDebitRow, bundleId: Int)(implicit session: Session): Seq[ApiEntity] = {
    val bundleValues = getBundleContextData(bundleId)
    bundleValues
  }

  protected def storeBundleContextEntitySelection(bundleId: Int, entitySelection: ApiBundleContextEntitySelection)
      (implicit session: Session): Try[ApiBundleContextEntitySelection] = {
    val entityRow = BundleContextEntitySelectionRow(0, bundleId, LocalDateTime.now(), LocalDateTime.now(),
      entitySelection.entityName, entitySelection.entityId, entitySelection.entityKind)

    val maybeEntity = Try((BundleContextEntitySelection returning BundleContextEntitySelection) += entityRow)

    maybeEntity flatMap { insertedEntity =>
      val insertedProperties = entitySelection.properties map { properties =>
        val propertiesMaybeInserted = properties map { property =>
          storeBundlePropertySelection(insertedEntity.id, property)
        }
        Utils.flatten(propertiesMaybeInserted)
      }
      val apiEntity = ApiBundleContextEntitySelection.fromDbModel(insertedEntity)

      insertedProperties match {
        case None =>
          Success(apiEntity)
        case Some(Success(properties)) =>
          Success(apiEntity.copy(properties = Some(properties)))
        case Some(Failure(e)) =>
          Failure(e)
      }
    }
  }

  protected def storeBundlePropertySelection(entitySelectionId: Int, propertySelection: ApiBundleContextPropertySelection)
      (implicit session: Session): Try[ApiBundleContextPropertySelection] = {
    val propertyRow = BundleContextPropertySelectionRow(0, entitySelectionId, LocalDateTime.now(), LocalDateTime.now(),
    propertySelection.propertyRelationshipKind, propertySelection.propertyRelationshipId,
      propertySelection.propertyName, propertySelection.propertyType, propertySelection.propertyUnitofmeasurement)

    Try((BundleContextPropertySelection returning BundleContextPropertySelection) += propertyRow) map
      ApiBundleContextPropertySelection.fromDbModel

  }

  private def getBundleList(bundleContextId: Int)(implicit session: Session) = {
    val connectedBundles = getConnectedBundles(bundleContextId)

    val entityQueries = connectedBundles map { bundle =>
      for {
        (e, p) <- BundleContextEntitySelection.filter(_.bundleContextId === bundle._1.id)
          .joinLeft(BundleContextPropertySelection)
          .on(_.id === _.bundleContextEntitySelectionId)
      } yield (e, p)
    }

    val allEntityPropertiesQ = entityQueries.reduceLeft { (acc, query) =>
      acc union query
    }

    val allEntityProperties = allEntityPropertiesQ.run
//    logger.debug("All linked entities: " + allEntityProperties.toString())
    val bundleEntities = allEntityProperties.groupBy(_._1.bundleContextId)
      .map { case (bundleId, bundleEntityProperties) =>
        val retrievedEntities = bundleEntityProperties.groupBy(_._1)
          .map { case (entitySelection, propertySelections) =>
            val apiPropertySelections = propertySelections.map(_._2)
              .flatMap(x => x.map(ApiBundleContextPropertySelection.fromDbModel))
            ApiBundleContextEntitySelection.fromDbModel(entitySelection)
              .copy(properties = Utils.seqOption(apiPropertySelections))
          }

        (bundleId, retrievedEntities)
      }

    connectedBundles map { bundle =>
      val entitySelections = bundleEntities.get(bundle._1.id)
      val apiBundle = ApiBundleContext.fromDbModel(bundle._1)
      (apiBundle.copy(entities = entitySelections.map(_.toSeq)), bundle._2)
    }
  }

  private def getConnectedBundles(bundleContextId: Int)(implicit session: Session): Seq[(BundleContextRow, Int)] = {
    implicit val getBundlesResult = GetResult(r =>
      BundleContextRow(r.nextInt,
        new LocalDateTime(r.nextTimestamp),
        new LocalDateTime(r.nextTimestamp),
        r.nextString)
    )

    // FIXME: keep an eye on future releases of Slick to avoid embedding SQL:
    sql"""
       WITH RECURSIVE recursive_bundle_context(id, date_created, last_updated, name, bundle_parent) AS (
          SELECT b.id, b.date_created, b.last_updated, b.name, b2b.bundle_parent FROM bundle_context b
       	  LEFT JOIN bundle_context_to_bundle_crossref b2b
       	    ON b.id = b2b.bundle_child
           WHERE b.id = ${bundleContextId}
          UNION ALL
            SELECT b.id, b.date_created, b.last_updated, b.name, b2b.bundle_parent
            FROM recursive_bundle_context r_b, bundle_context b
       	    LEFT JOIN bundle_context_to_bundle_crossref b2b
       	      ON b.id = b2b.bundle_child
              WHERE b2b.bundle_parent = r_b.id
       )
       SELECT * FROM recursive_bundle_context
      """.as[(BundleContextRow, Int)].list
  }

  private def buildBundleTree(rootBundle: Option[ApiBundleContext], bundles: Iterable[(ApiBundleContext, Int)]): Option[ApiBundleContext]= {
    rootBundle map { bundle =>
//      logger.debug("Building recursively for " + bundle)
      val childBundles = bundles.filter(x => bundle.id.contains(x._2))
      val assembledChildBundles = childBundles flatMap { cBundle =>
        buildBundleTree(Some(cBundle._1), bundles)
      }
      bundle.copy(bundles = Utils.seqOption(assembledChildBundles.toSeq))
    }
  }
}