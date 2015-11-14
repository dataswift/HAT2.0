package hatdex.hat.api

import akka.event.LoggingAdapter
import spray.http._
import spray.http.HttpHeaders._
import spray.http.HttpMethods._
import spray.routing._
import com.typesafe.config.ConfigFactory

trait Cors extends Directives {
  this: HttpService =>
  val conf = ConfigFactory.load()

  private val optionsCorsHeaders = List(
    `Access-Control-Allow-Headers`(conf.getStringList("cors.allow_headers").toArray.mkString(", ")),
    `Access-Control-Max-Age`(60 * 60 * 24 * 20), // cache pre-flight response for 20 days
    `Access-Control-Allow-Credentials`(conf.getBoolean("cors.allow_credentials"))
  )

  /**
   * Based on the provided RequestContext, return an Access-Control-Allow-Origin header with the
   * user-provided Origin if that Origin is acceptable, or a None if it's not.
   */
  private def getAllowedOrigins(context: RequestContext): Option[`Access-Control-Allow-Origin`] = {

    val configAllowOrigins = conf.getString("cors.allow_origins")
    context.request.header[Origin].collect {
      case origin if configAllowOrigins.contains(origin.value) =>
        `Access-Control-Allow-Origin`(SomeOrigins(origin.originList))
      case origin if configAllowOrigins.contains("*") =>
        `Access-Control-Allow-Origin`(AllOrigins)
    }
  }

  def cors[T]: Directive0 = mapRequestContext { context =>
    context.withRouteResponseHandling {
      // If an OPTIONS request was rejected as 405, complete the request by responding with the
      // defined CORS details and the allowed options grabbed from the rejection
      case Rejected(reasons)
        if context.request.method == HttpMethods.OPTIONS && reasons.exists(_.isInstanceOf[MethodRejection]) =>
        val allowedMethods: List[HttpMethod] = reasons.collect { case rejection: MethodRejection => rejection.supported }

        // complete the response with CORS headers
        context.complete(HttpResponse().withHeaders(
          `Access-Control-Allow-Methods`(HttpMethods.OPTIONS, allowedMethods :_*) ::  getAllowedOrigins(context) ++: optionsCorsHeaders
        ))

    }.withHttpResponseHeadersMapped { headers =>
      var allHeaders = headers ++: getAllowedOrigins(context)
      allHeaders.toList
    }
  }
}
