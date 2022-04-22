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
 * 8 / 2018
 */

package org.hatdex.hat.she.models

import akka.actor.ActorSystem
import akka.stream.alpakka.awslambda.scaladsl.AwsLambdaFlow
import akka.stream.scaladsl.{ Sink, Source }
import akka.stream.{ ActorMaterializer, Materializer }
import io.dataswift.models.hat.EndpointDataBundle
import io.dataswift.models.hat.applications.Version
import org.hatdex.dex.apiV2.Errors.{ ApiException, DataFormatException }
import org.hatdex.hat.api.service.RemoteExecutionContext
import org.joda.time.DateTime
import play.api.libs.json.{ Format, Json }
import play.api.{ Configuration, Logger }
import software.amazon.awssdk.services.sts.auth.StsAssumeRoleWithWebIdentityCredentialsProvider
import software.amazon.awssdk.core.SdkBytes
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.lambda.LambdaAsyncClient
import software.amazon.awssdk.services.lambda.model.{ InvokeRequest, InvokeResponse }

import javax.inject.Inject
import scala.concurrent.{ ExecutionContext, Future }

class LambdaFunctionExecutable(
    id: String,
    version: Version,
    baseUrl: String,
    val namespace: String,
    val endpoint: String,
    val configuration: FunctionConfiguration
  )(config: Configuration,
    lambdaExecutor: AwsLambdaExecutor)
    extends FunctionExecutable {
  import FunctionConfigurationJsonProtocol._
  import io.dataswift.models.hat.json.RichDataJsonFormats._

  protected val logger: Logger = Logger(this.getClass)
  private val lambdaLogs       = config.get[String]("she.aws.logs")

  logger.debug(s"Initialised SHE lambda function $id v$version $baseUrl")

  def execute(
      configuration: FunctionConfiguration,
      requestData: Request): Future[Seq[Response]] = {
    val request = InvokeRequest
      .builder()
      .functionName(s"$baseUrl-$id")
      .logType(lambdaLogs)
      .payload(
        SdkBytes.fromUtf8String(
          Json
            .toJson(
              Map(
                "functionConfiguration" -> Json.toJson(configuration),
                "request" -> Json.toJson(requestData)
              )
            )
            .toString()
        )
      )
      .build()
    lambdaExecutor.execute[Seq[Response]](request)
  }

  override def bundleFilterByDate(
      fromDate: Option[DateTime],
      untilDate: Option[DateTime]): Future[EndpointDataBundle] = {
    val request: InvokeRequest = InvokeRequest
      .builder()
      .functionName(s"$baseUrl-$id-bundle")
      .logType(lambdaLogs)
      .payload(
        SdkBytes.fromUtf8String(
          Json
            .toJson(
              Map(
                "fromDate" -> fromDate.map(_.toString),
                "untilDate" -> untilDate.map(_.toString)
              )
            )
            .toString()
        )
      )
      .build()
    lambdaExecutor.execute[EndpointDataBundle](request)
  }
}

class LambdaFunctionLoader @Inject() (
    config: Configuration,
    lambdaExecutor: AwsLambdaExecutor) {

  import FunctionConfigurationJsonProtocol.functionConfigurationFormat

  protected val logger: Logger = Logger(this.getClass)
  private val lambdaLogs       = config.get[String]("she.aws.logs")

  def load(
      id: String,
      version: Version,
      baseUrl: String,
      namespace: String,
      endpoint: String
    )(implicit ec: ExecutionContext): Future[LambdaFunctionExecutable] = {
    val request = InvokeRequest
      .builder()
      .functionName(s"$baseUrl-$id-configuration")
      .logType(lambdaLogs)
      .payload(SdkBytes.fromUtf8String("\"\""))
      .build()

    lambdaExecutor
      .execute[FunctionConfiguration](request)
      .map(c =>
        new LambdaFunctionExecutable(
          id,
          version,
          baseUrl,
          namespace,
          endpoint,
          c
        )(
          config,
          lambdaExecutor
        )
      )
  }
}

class AwsLambdaExecutor @Inject() (
    configuration: Configuration
  )(implicit
    val actorSystem: ActorSystem,
    ec: RemoteExecutionContext) {

  private val mock = configuration.get[Boolean]("she.aws.mock")

  protected val logger: Logger = Logger(this.getClass)

  implicit private val materializer: Materializer = ActorMaterializer()

  implicit private val lambdaClient: LambdaAsyncClient =
    LambdaAsyncClient
      .builder()
      .region(Region.of(configuration.get[String]("she.aws.region")))
      .credentialsProvider(StsAssumeRoleWithWebIdentityCredentialsProvider.builder().build())
      .build()

  actorSystem.registerOnTermination(lambdaClient.close())

  def execute[T](
      request: InvokeRequest
    )(implicit jsonFormatter: Format[T]): Future[T] =
    if (mock) Future.successful(null.asInstanceOf[T])
    else
      Source
        .single(request)
        .via(AwsLambdaFlow(1)(lambdaClient))
        .runWith(Sink.head)
        .map {
          case r: InvokeResponse if r.statusCode() == 200 =>
            logger.debug(s"""Function responded with:
               | Status: ${r.statusCode()}
               | Body: ${r.payload().asUtf8String()}
               | Logs: ${Option(r.logResult()).map(log => java.util.Base64.getDecoder.decode(log))}
            """.stripMargin)
            val jsResponse =
              Json.parse(r.payload().asUtf8String()).validate[T] recover {
                  case e =>
                    val message = s"Error parsing lambda response: $e"
                    logger.error(message)
                    throw DataFormatException(message)
                }
            jsResponse.get
          case r =>
            val message =
              s"Retrieving SHE function configuration failed: $r, ${r.payload().asUtf8String()}"
            logger.error(message)
            throw new ApiException(message)
        }
}
