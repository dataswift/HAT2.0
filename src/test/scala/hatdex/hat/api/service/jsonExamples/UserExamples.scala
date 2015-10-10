package hatdex.hat.api.service.jsonExamples

/**
 * Created by andrius on 10/10/15.
 */
object UserExamples {
  val userExample =
    """
      |  {
      |    "userId": "bb5385ab-9931-40b2-b65c-239210b408f3",
      |    "email": "apiClient@platform.com",
      |    "pass": "$2a$10$6YoHtQqSdit9zzVSzrkK7.E.JQuioFNAggTY7vZRL4RSeY.sUbUIu",
      |    "name": "apiclient.platform.com",
      |    "role": "dataDebit"
      |  }
    """.stripMargin
  // pass is bcrypt salted hash of simplepass

  val ownerUserExample =
    """
      |  {
      |    "userId": "cf6da178-ac77-4c97-b274-b7ed34d16aea",
      |    "email": "apiClient@platform.com",
      |    "pass": "$2a$10$6YoHtQqSdit9zzVSzrkK7.E.JQuioFNAggTY7vZRL4RSeY.sUbUIu",
      |    "name": "apiclient.platform.com",
      |    "role": "owner"
      |  }
    """.stripMargin
}
