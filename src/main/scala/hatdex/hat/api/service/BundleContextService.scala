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

      val entities = Utils.reverseOptionTry(entitiesInserted)

      val bundlesInserted = bundleContext.bundles.map { bundles =>
        val maybeSubBundles = Utils.flatten(bundles.map(storeBundleContext))

        maybeSubBundles map { subBundles =>
          subBundles map { b =>
            val relRow = BundleContextToBundleCrossrefRow(0, LocalDateTime.now(), LocalDateTime.now(), bundle.id, b.id.getOrElse(0))
            BundleContextToBundleCrossref += relRow
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
    logger.debug(s"Retrieving bundle ID ${bundleContextId}")
    val bundles = getBundleList(bundleContextId)

    logger.debug("Constructing a tree for all bundles: " + bundles.toString)
    buildBundleTree(bundles.find(_._1.id.contains(bundleContextId)).map(_._1), bundles)
  }

  protected def getBundleContextData(bundleContextId: Int)(implicit session: Session): Seq[ApiEntity] = {
//    val bundles = getBundleList(bundleContextId).map(_._1)
//
//    val results = bundles map { bundle =>
//      bundle.entities map { entities =>
//        entities map { entity =>
//          val matchingEntity = (entity.entityId, entity.entityKind, entity.entityName) match {
//            case (Some(entityId), _, _) =>
//              Entity.filter(_.id == entityId)
//
//            case (None, Some(entityKind), Some(entityName)) =>
//              Entity.filter(_.kind == entityKind).filter(_.name == entityName)
//          }
//
//          val joined = matchingEntity.joinLeft(EventsEvent).on(_.id === _.id)
//            .joinLeft(PeoplePerson).on(_._1.id === _.id)
//            .joinLeft(ThingsThing).on(_._1._1.id === _.id)
//            .joinLeft(LocationsLocation).on(_._1._1._1.id === _.id)
//            .joinLeft(OrganisationsOrganisation).on(_._1._1._1._1.id === _.id)
//            .take(1).run.headOption
//          joined map {
//            case (((((abstractEntity, event), person), thing), location), organisation) =>
//              (abstractEntity, event, person, thing, location, organisation)
//          }
//
//          entity.properties map { properties =>
//            properties map { property =>
//              property.
//
//            }
//          }
//        }
//      }
//    }
//
//    results
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
        e <- BundleContextEntitySelection.filter(_.bundleContextId === bundle._1.id)
        p <- BundleContextPropertySelection.filter(_.bundleContextEntitySelectionId === e.id)
      } yield (e, p)
    }

    val allEntityPropertiesQ = entityQueries.reduceLeft { (acc, query) =>
      acc union query
    }

    val allEntityProperties = allEntityPropertiesQ.run
    logger.debug("All linked entities: " + allEntityProperties.toString())
    val bundleEntities = allEntityProperties.groupBy(_._1.bundleContextId)
      .map { case (bundleId, bundleEntityProperties) =>
        val retrievedEntities = bundleEntityProperties.groupBy(_._1)
          .map { case (entitySelection, propertySelections) =>
            val apiPropertySelections = propertySelections.map(_._2)
              .map(ApiBundleContextPropertySelection.fromDbModel)
            ApiBundleContextEntitySelection.fromDbModel(entitySelection)
              .copy(properties = Some(apiPropertySelections))
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

    sql"""
       WITH RECURSIVE recursive_bundle_context(id, date_created, last_updated, name, bundle_parent) AS (
          SELECT b.id, b.date_created, b.last_updated, b.name, b2b.bundle_parent FROM bundle_context b
       	  LEFT JOIN bundle_context_to_bundle_crossref b2b
       	    ON b.id = b2b.bundle_child
           WHERE b.id = 1
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
      val childBundles = bundles.filter(x => bundle.id.contains(x._2))
      val assembledChildBundles = childBundles flatMap { cBundle =>
        buildBundleTree(Some(cBundle._1), bundles)
      }
      bundle.copy(bundles = Utils.seqOption(assembledChildBundles.toSeq))
    }
  }
}