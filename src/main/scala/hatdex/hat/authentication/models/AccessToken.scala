package hatdex.hat.authentication.models

import java.util.UUID

case class AccessToken(accessToken: String, userId: UUID)
