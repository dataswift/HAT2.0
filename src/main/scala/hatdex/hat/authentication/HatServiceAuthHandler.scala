package hatdex.hat.authentication

import hatdex.hat.authentication.authenticators.{UserPassHandler, AccessTokenHandler}

trait HatServiceAuthHandler {
  def accessTokenHandler = AccessTokenHandler.AccessTokenAuthenticator(
    authenticator = HatAuthHandler.AccessTokenHandler.authenticator
  ).apply()

  def userPassHandler = UserPassHandler.UserPassAuthenticator(
    authenticator = HatAuthHandler.UserPassHandler.authenticator
  ).apply()

  def userPassApiHandler = UserPassHandler.UserPassAuthenticator(
    authenticator = HatAuthHandler.UserPassApiHandler.authenticator
  ).apply()
}
