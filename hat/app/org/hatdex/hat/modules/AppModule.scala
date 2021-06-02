package org.hatdex.hat.modules

import com.google.inject.Provides
import dev.profunktor.auth.jwt.JwtSecretKey
import net.codingwell.scalaguice.ScalaModule
import org.hatdex.hat.api.controllers.v1
import org.hatdex.hat.api.controllers.v2
import org.hatdex.hat.api.service.{ UserService, UserServiceImpl }
import org.hatdex.hat.client.{ AdjudicatorClient, AdjudicatorWsClient }
import play.api.Configuration
import play.api.libs.ws.WSClient

import javax.inject.{ Singleton => JSingleton }

class AppModule extends ScalaModule {

  override def configure(): Unit = {
    bind[UserService].to[UserServiceImpl].in(classOf[JSingleton])
    bind[v1.ContractAction].to[v1.ContractActionImpl].in(classOf[JSingleton])
    bind[v2.ContractAction].to[v2.ContractActionImpl].in(classOf[JSingleton])
  }

  @Provides @JSingleton
  def adjudicatorClient(
      configuration: Configuration,
      wsClient: WSClient): AdjudicatorClient = {
    val adjudicatorAddress      = configuration.get[String]("adjudicator.address")
    val adjudicatorScheme       = configuration.get[String]("adjudicator.scheme")
    val adjudicatorEndpoint     = s"$adjudicatorScheme$adjudicatorAddress"
    val adjudicatorSharedSecret = configuration.get[String]("adjudicator.sharedSecret")
    new AdjudicatorWsClient(
      adjudicatorEndpoint,
      JwtSecretKey(adjudicatorSharedSecret),
      wsClient
    )
  }

}
