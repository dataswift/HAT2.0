package dalapi.models

/**
 * API format of the Thing, with only the name as the mandatory field
 */
case class ApiThing(id: Option[Int], name: String)
