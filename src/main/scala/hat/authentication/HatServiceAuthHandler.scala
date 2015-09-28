package hat.authentication

import hat.authentication.authenticators.{UserPassHandler, AccessTokenHandler}

object HatServiceAuthHandler {
  val accessTokenHandler = AccessTokenHandler.AccessTokenAuthenticator(authenticator = HatAuthHandler.AccessTokenHandler.authenticator).apply()
  val userPassHandler = UserPassHandler.UserPassAuthenticator(authenticator = HatAuthHandler.UserPassHandler.authenticator).apply()
}
