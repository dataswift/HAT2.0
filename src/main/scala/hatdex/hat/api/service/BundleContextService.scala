package hatdex.hat.api.service

import akka.event.LoggingAdapter
import hatdex.hat.Utils
import hatdex.hat.api.models._
import hatdex.hat.dal.SlickPostgresDriver.simple._
import hatdex.hat.dal.Tables._
import org.joda.time.LocalDateTime

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
    None
  }

  protected def getBundleContextData(bundleContextId: Int)(implicit session: Session): Option[ApiBundleContextData] = {
    None
  }

  def retrieveDataDebitContextualValues(debit: DataDebitRow, bundleId: Int)(implicit session: Session): Option[ApiBundleContextData] = {
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
}