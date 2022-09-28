package org.hatdex.hat.modules

import com.google.inject.Provides
import dev.profunktor.auth.jwt.JwtSecretKey
import net.codingwell.scalaguice.ScalaModule
import org.hatdex.hat.api.controllers.{ ContractAction, ContractActionImpl }
import org.hatdex.hat.api.service.{ UserService, UserServiceImpl }
import org.hatdex.hat.client.{ AdjudicatorClient, AdjudicatorWsClient, TrustProxyClient, TrustProxyWsClient }
import play.api.Configuration
import play.api.libs.ws.WSClient

import javax.inject.{ Singleton => JSingleton }

class AppModule extends ScalaModule {

  override def configure(): Unit = {
    bind[UserService].to[UserServiceImpl].in(classOf[JSingleton])
    bind[ContractAction].to[ContractActionImpl].in(classOf[JSingleton])
  }

  @Provides @JSingleton
  def adjudicatorClient(
      configuration: Configuration,
      wsClient: WSClient): AdjudicatorClient = {
    val adjudicatorAddress      = configuration.get[String]("adjudicator.address")
    val adjudicatorSharedSecret = configuration.get[String]("adjudicator.sharedSecret")
    new AdjudicatorWsClient(
      adjudicatorAddress,
      JwtSecretKey(adjudicatorSharedSecret),
      wsClient
    )
  }

  @Provides
  @JSingleton
  def trustProxyClient(
      //configuration: Configuration,
      wsClient: WSClient): TrustProxyClient =
    new TrustProxyWsClient(
      wsClient
    )

}
