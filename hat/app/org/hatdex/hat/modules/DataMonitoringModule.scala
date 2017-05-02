/*
 * Copyright (C) 2017 HAT Data Exchange Ltd
 * SPDX-License-Identifier: AGPL-3.0
 *
 * This file is part of the Hub of All Things project (HAT).
 *
 * HAT is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation, version 3 of
 * the License.
 *
 * HAT is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See
 * the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General
 * Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 *
 * Written by Andrius Aucinas <andrius.aucinas@hatdex.org>
 * 5 / 2017
 */

package org.hatdex.hat.modules

import com.amazonaws.auth.{ AWSStaticCredentialsProvider, BasicAWSCredentials }
import com.amazonaws.services.s3.{ AmazonS3, AmazonS3ClientBuilder }
import com.google.inject.name.Named
import com.google.inject.{ AbstractModule, Provides }
import net.ceedubs.ficus.Ficus._
import net.ceedubs.ficus.readers.ArbitraryTypeReader._
import net.codingwell.scalaguice.ScalaModule
import org.hatdex.hat.api.service.monitoring.{ EndpointSubscriberManagerActor, HatDataEventBus, InjectedEndpointSubscriberActor }
import org.hatdex.hat.api.service.{ AwsS3Configuration, FileManager, FileManagerS3 }
import org.hatdex.hat.resourceManagement.actors.{ HatServerActor, HatServerProviderActor }
import play.api.Configuration
import play.api.libs.concurrent.AkkaGuiceSupport

class DataMonitoringModule extends AbstractModule with ScalaModule with AkkaGuiceSupport {

  def configure = {
    bindActor[EndpointSubscriberManagerActor]("endpointSubscriberManager")
    bindActorFactory[InjectedEndpointSubscriberActor, InjectedEndpointSubscriberActor.Factory]
    bind[HatDataEventBus].to[HatDataEventBus]
  }

}
