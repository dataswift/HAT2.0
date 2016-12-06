package org.hatdex.hat.resourceManagement

import javax.inject.{ Inject, Singleton }

import org.hatdex.hat.dal.SchemaMigration
import org.hatdex.hat.dal.SlickPostgresDriver.backend.Database
import play.api.Configuration

import scala.concurrent.{ ExecutionContext, Future }

@Singleton
class HatDatabaseProvider @Inject() (configuration: Configuration, schemaMigration: SchemaMigration) {
  def database(hat: String)(implicit ec: ExecutionContext): Future[Database] = {
    Future {
      Database.forConfig(s"hat.$hat.database")
    } recoverWith {
      case e =>
        Future.failed(new HatServerDiscoveryException(s"Database configuration for $hat incorrect or unavailable", e))
    }
  }

  def shutdown(db: Database)(implicit ec: ExecutionContext): Future[Unit] = {
    db.shutdown
  }

  def update(db: Database)(implicit ec: ExecutionContext) = {
    schemaMigration.run()(db)
  }
}
