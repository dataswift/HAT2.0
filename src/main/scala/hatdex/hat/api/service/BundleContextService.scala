/*
 * Copyright (C) 2016 Andrius Aucinas <andrius.aucinas@hatdex.org>
 * SPDX-License-Identifier: AGPL-3.0
 *
 * This file is part of the Hub of All Things project (HAT).
 *
 * HAT is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation, version 3 of
 * the License.
 *
 * HAT is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See
 * the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General
 * Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package hatdex.hat.api.service

import akka.event.LoggingAdapter
import hatdex.hat.Utils
import hatdex.hat.api.DatabaseInfo
import hatdex.hat.api.models._
import hatdex.hat.dal.SlickPostgresDriver.api._
import hatdex.hat.dal.Tables._
import org.joda.time.LocalDateTime

import scala.concurrent.{ExecutionContext, Future}

// this trait defines our service behavior independently from the service actor
trait BundleContextService {

  val logger: LoggingAdapter
  implicit val dalExecutionContext: ExecutionContext

  def eventsService: EventsService

  def peopleService: PeopleService

  def thingsService: ThingsService

  def locationsService: LocationsService

  def organisationsService: OrganisationsService

  protected[api] def getBundleContextData(bundleContextId: Int): Future[Seq[ApiEntity]] = {
    val eventualBundles = getBundleList(bundleContextId)
    eventualBundles flatMap { bundles =>
      val results = bundles map {
        case (bundle, _) =>
          bundle.entities map { entities =>
            val allEntities = entities map { entitySelection =>
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
              val eventualStoredEntities = DatabaseInfo.db.run(matchingEntity.result)
              //            logger.debug(s"Entity matching entity selector ${entitySelection}: ${maybeStoredEntities}")
              implicit val getValues = true
              eventualStoredEntities flatMap { storedEntities =>
                val entityValues = storedEntities map {
                  case EntityRow(entityId, _, _, name, "event", _, _, _, _, _) =>
                    val eEvent = eventsService.getEvent(entityId, recursive = false, propertySelectors = entitySelection.properties)
                    eEvent.map(event => ApiEntity("event", event, None, None, None, None))
                  case EntityRow(entityId, _, _, name, "person", _, _, _, _, _) =>
                    val ePerson = peopleService.getPerson(entityId, recursive = false, propertySelectors = entitySelection.properties)
                    ePerson.map(person => ApiEntity("person", None, person, None, None, None))
                  case EntityRow(entityId, _, _, name, "thing", _, _, _, _, _) =>
                    val eThing = thingsService.getThing(entityId, recursive = false, propertySelectors = entitySelection.properties)
                    eThing.map(thing => ApiEntity("thing", None, None, None, thing, None))
                  case EntityRow(entityId, _, _, name, "location", _, _, _, _, _) =>
                    val eLocation = locationsService.getLocation(entityId, recursive = false, propertySelectors = entitySelection.properties)
                    eLocation.map(location => ApiEntity("location", None, None, location, None, None))
                  case EntityRow(entityId, _, _, name, "organisation", _, _, _, _, _) =>
                    val eOrganisation = organisationsService.getOrganisation(entityId, recursive = false, propertySelectors = entitySelection.properties)
                    eOrganisation.map(organisation => ApiEntity("organisation", None, None, None, None, organisation))
                }

                Future.sequence(entityValues)
              }
            }
            Future.sequence(allEntities).map(_.flatten)

          }
      }
      Future.sequence(results.flatten).map(_.flatten)
    }
  }

  private def getBundleList(bundleContextId: Int): Future[Seq[(ApiBundleContext, Option[Int])]] = {
    val eventualConnectedBundles = getConnectedBundles(bundleContextId)

    val eventualEntityProperties = eventualConnectedBundles flatMap { connectedBundles =>
      logger.debug(s"Retrieved connected bundles: $connectedBundles")
      val entityQueries = connectedBundles map { bundle =>
        for {
          (e, p) <- BundleContextEntitySelection.filter(_.bundleContextId === bundle._1.id)
            .joinLeft(BundleContextPropertySelection)
            .on(_.id === _.bundleContextEntitySelectionId)
        } yield (e, p)
      }
      val allEntityPropertiesQ = entityQueries.reduceLeft((acc, query) => acc ++ query)
      DatabaseInfo.db.run(allEntityPropertiesQ.result)
    }

    for {
      allEntityProperties <- eventualEntityProperties
      connectedBundles <- eventualConnectedBundles
    } yield {
      val bundleEntities = allEntityProperties.groupBy(_._1.bundleContextId)
        .map {
          case (bundleId, bundleEntityProperties) =>
            val retrievedEntities = bundleEntityProperties.groupBy(_._1)
              .map {
                case (entitySelection, propertySelections) =>
                  val apiPropertySelections = propertySelections.map(_._2)
                    .flatMap(x => x.map(ApiBundleContextPropertySelection.fromDbModel))
                  ApiBundleContextEntitySelection.fromDbModel(entitySelection)
                    .copy(properties = Utils.seqOption(apiPropertySelections))
              }

            (bundleId, retrievedEntities)
        }

      connectedBundles map { bundle =>
        val entitySelections = bundle._1.id.flatMap(bundleEntities.get)
        val apiBundle = bundle._1
        (apiBundle.copy(entities = entitySelections.map(_.toSeq)), bundle._2)
      }
    }
  }

  private def getConnectedBundles(bundleContextId: Int): Future[Seq[(ApiBundleContext, Option[Int])]] = {
    DatabaseInfo.db.run(BundleContextTree.filter(_.rootBundle === bundleContextId).result) map { bundles =>
      bundles map { bundle =>
        (ApiBundleContext.fromNestedBundle(bundle), bundle.bundleParent)
      }
    }
  }

  /*
   * Stores bundle table provided from the incoming API call
   */
  protected[api] def storeBundleContext(bundleContext: ApiBundleContext): Future[ApiBundleContext] = {
    val bundleRow = BundleContextRow(0, LocalDateTime.now(), LocalDateTime.now(), bundleContext.name)
    val eventualBundle = DatabaseInfo.db.run((BundleContext returning BundleContext) += bundleRow)

    for {
      bundle <- eventualBundle
      entities <- Future.sequence(bundleContext.entities.getOrElse(Seq()).map(e => storeBundleContextEntitySelection(bundle.id, e))).map(Utils.seqOption)
      subBundles <- Future.sequence(bundleContext.bundles.getOrElse(Seq()).map(storeBundleContext)).map(Utils.seqOption)
      bundleRelQs <- Future.successful(subBundles.getOrElse(Seq()).map(b => BundleContextToBundleCrossrefRow(0, LocalDateTime.now(), LocalDateTime.now(), bundle.id, b.id.getOrElse(0))))
      _ <- DatabaseInfo.db.run(BundleContextToBundleCrossref ++= bundleRelQs)
    } yield ApiBundleContext.fromDbModel(bundle).copy(entities = entities, bundles = subBundles)
  }

  protected[api] def getBundleContextById(bundleContextId: Int): Future[Option[ApiBundleContext]] = {
    //    logger.debug(s"Retrieving bundle ID ${bundleContextId}")
    for {
      bundles <- getBundleList(bundleContextId)
      tree <- buildBundleTree(bundles.find(_._1.id.contains(bundleContextId)).map(_._1), bundles)
    } yield tree
  }

  protected def storeBundleContextEntitySelection(bundleId: Int, entitySelection: ApiBundleContextEntitySelection): Future[ApiBundleContextEntitySelection] = {
    val entityRow = BundleContextEntitySelectionRow(0, bundleId, LocalDateTime.now(), LocalDateTime.now(),
      entitySelection.entityName, entitySelection.entityId, entitySelection.entityKind)

    val eventualEntity = DatabaseInfo.db.run((BundleContextEntitySelection returning BundleContextEntitySelection) += entityRow)

      def insertProperties(insertedEntity: BundleContextEntitySelectionRow): Future[Seq[ApiBundleContextPropertySelection]] = {
        entitySelection.properties map { properties =>
          Future.sequence {
            properties map { property =>
              storeBundlePropertySelection(insertedEntity.id, property)
            }
          }
        } getOrElse {
          Future.successful(Seq())
        }
      }

    for {
      insertedEntity <- eventualEntity
      insertedProperties <- insertProperties(insertedEntity).map(Utils.seqOption)
    } yield {
      ApiBundleContextEntitySelection.fromDbModel(insertedEntity)
        .copy(properties = insertedProperties)
    }
  }

  protected def storeBundlePropertySelection(entitySelectionId: Int, propertySelection: ApiBundleContextPropertySelection): Future[ApiBundleContextPropertySelection] = {
    val propertyRow = BundleContextPropertySelectionRow(0, entitySelectionId, LocalDateTime.now(), LocalDateTime.now(),
      propertySelection.propertyRelationshipKind, propertySelection.propertyRelationshipId,
      propertySelection.propertyName, propertySelection.propertyType, propertySelection.propertyUnitofmeasurement)

    DatabaseInfo.db.run((BundleContextPropertySelection returning BundleContextPropertySelection) += propertyRow)
      .map(ApiBundleContextPropertySelection.fromDbModel)
  }

  private def buildBundleTree(rootBundle: Option[ApiBundleContext], bundles: Iterable[(ApiBundleContext, Option[Int])]): Future[Option[ApiBundleContext]] = {
    rootBundle map { root =>
      logger.debug(s"Building recursively for $root from $bundles")
      val childBundles = bundles.filter(x => root.id == x._2)
      val assembledChildBundles = childBundles map { cBundle =>
        buildBundleTree(Some(cBundle._1), bundles)
      }

      val eventualBundle = Future.sequence(assembledChildBundles)
        .map(_.flatten.toSeq)
        .map(Utils.seqOption)
        .map { childBundles =>
          root.copy(bundles = childBundles)
        }
      eventualBundle.map(b => Some(b))
    } getOrElse {
      Future.successful(None)
    }
  }
}