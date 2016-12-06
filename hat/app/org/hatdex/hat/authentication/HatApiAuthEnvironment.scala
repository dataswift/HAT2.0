/*
 * Copyright (C) HAT Data Exchange Ltd - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Andrius Aucinas <andrius.aucinas@hatdex.org>, 10 2016
 */

package org.hatdex.hat.authentication

import com.mohiva.play.silhouette.api.Env
import com.mohiva.play.silhouette.impl.authenticators.{ CookieAuthenticator, JWTRS256Authenticator }
import org.hatdex.hat.resourceManagement.HatServer

trait HatAuthEnvironment extends Env {
  type I = models.HatUser
  type D = HatServer
}

trait HatApiAuthEnvironment extends HatAuthEnvironment {
  type A = JWTRS256Authenticator
}

trait HatFrontendAuthEnvironment extends HatAuthEnvironment {
  type A = CookieAuthenticator
}
