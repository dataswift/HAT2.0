package org.hatdex.hat.api.service.applications

import io.dataswift.models.hat.applications.Application
import org.hatdex.dex.apiV3.DexClient
import org.hatdex.dex.apiV3.Errors.ApiException
import org.hatdex.hat.api.service.RemoteExecutionContext
import play.api.cache.AsyncCacheApi
import play.api.libs.ws.WSClient
import play.api.{ Configuration, Logger }

import javax.inject.Inject
import scala.concurrent.Future
import scala.concurrent.duration.FiniteDuration

class TrustedApplicationProviderDex @Inject() (
    wsClient: WSClient,
    configuration: Configuration,
    cache: AsyncCacheApi
  )(implicit val rec: RemoteExecutionContext)
    extends TrustedApplicationProvider {

  private val logger = Logger(this.getClass)

  private val dexAddress = configuration.underlying.getString("exchange.address")
  private val dexClient = new DexClient(wsClient, dexAddress)

  private val applicationsCacheDuration = configuration.get[FiniteDuration]("application-cache-ttl")

  private val includeUnpublished: Boolean =
    configuration.getOptional[Boolean]("exchange.beta").getOrElse(false)

  def applications: Future[Seq[Application]] =
    cache.getOrElseUpdate(
      "apps:dexApplications",
      applicationsCacheDuration
    ) {
      dexClient.applications(Some(includeUnpublished))
    }

  def application(id: String): Future[Option[Application]] =
    cache
      .getOrElseUpdate(
        s"apps:dex:$id",
        applicationsCacheDuration
      ) {
        dexClient.application(id, None)
      }
      .map(Some(_))
      .recover {
        case e: ApiException =>
          logger.warn(s"Application config not found: ${e.getMessage}")
          None

        case e =>
          logger.error(s"Unexpected failure while fetching application. ${e.getMessage}")
          None
      }

}
