package org.hatdex.hat.modules

import com.google.inject.Provides
import net.codingwell.scalaguice.ScalaModule
import org.hatdex.hat.api.controllers.{ ContractAction, ContractActionImpl }
import org.hatdex.hat.api.service.{ UserService, UserServiceImpl }
import org.hatdex.hat.client.{ AdjudicatorClient, AdjudicatorWsClient, AuthServiceClient, AuthServiceWsClient }
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
    implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global

    new AdjudicatorWsClient(
      configuration,
      wsClient
    )
  }

  @Provides @JSingleton
  def authServiceClient(
      configuration: Configuration,
      wsClient: WSClient): AuthServiceClient = {
    implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global

    new AuthServiceWsClient(
      configuration,
      wsClient
    )
  }
}
