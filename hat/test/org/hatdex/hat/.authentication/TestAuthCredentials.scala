package org.hatdex.hat.authentication

import spray.http.HttpHeaders.RawHeader

trait TestAuthCredentials {
  val ownerAuthToken = HatAuthTestHandler.validUsers.find(_.role == "owner").map(_.userId).flatMap { ownerId =>
    HatAuthTestHandler.validAccessTokens.find(_.userId == ownerId).map(_.accessToken)
  } getOrElse ("")
  val ownerAuthHeader = RawHeader("X-Auth-Token", ownerAuthToken)

  val dataDebitAuthToken = HatAuthTestHandler.validUsers.find(_.role == "dataDebit").map(_.userId).flatMap { ownerId =>
    HatAuthTestHandler.validAccessTokens.find(_.userId == ownerId).map(_.accessToken)
  } getOrElse ("")
  val dataDebitAuthHeader = RawHeader("X-Auth-Token", dataDebitAuthToken)
}
