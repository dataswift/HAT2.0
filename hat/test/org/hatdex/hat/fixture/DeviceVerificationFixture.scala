package org.hatdex.hat.fixture

import io.dataswift.test.common.BaseSpec
import play.api.inject.guice.GuiceApplicationBuilder

abstract class DeviceVerificationFixture extends BaseSpec {
  val injector = {
    val application =
      new GuiceApplicationBuilder()
        .configure()
        .build()
    application.injector
  }
}
