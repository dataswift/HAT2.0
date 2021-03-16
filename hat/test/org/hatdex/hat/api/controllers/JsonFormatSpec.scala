package org.hatdex.hat.api.controllers

import io.dataswift.models.hat.{ ApiHatFile, ApiHatFilePermissions, HatFileStatus }
import io.dataswift.test.common.BaseSpec
import play.api.libs.json.Json

import java.util.UUID

class JsonFormatSpec extends BaseSpec {

  import io.dataswift.models.hat.json.HatJsonFormats._

  "the implicit HatJsonFormats" should "serialise a file" in {
    val hatFileSimple = ApiHatFile(
      Some("testFile"),
      "testFile",
      "test",
      None,
      None,
      None,
      None,
      None,
      None,
      Some(HatFileStatus.New()),
      None,
      None,
      None,
      Some(Vector(ApiHatFilePermissions(UUID.randomUUID(), contentReadable = true)))
    )

    val json = Json.toJson(hatFileSimple)

    (json \ "fileId").as[String] mustBe "testFile"
  }

}
