package org.hatdex.hat.fixture

import play.api.inject.guice.GuiceApplicationBuilder
import org.hatdex.hat.api.HATTestContext

abstract class DeviceVerificationFixture extends HATTestContext {
  val injector = {
    val application =
      new GuiceApplicationBuilder()
        .configure()
        .build()
    application.injector
  }
}
