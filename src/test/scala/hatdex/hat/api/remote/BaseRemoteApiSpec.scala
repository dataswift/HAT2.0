package hatdex.hat.api.remote

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import org.specs2.main.CommandLine
import org.specs2.matcher.Matcher
import org.specs2.mutable.Specification
import org.specs2.specification.mutable.CommandLineArguments

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

case class Retry(value: Int) extends AnyVal
case class Timeout(value: FiniteDuration) extends AnyVal

trait BaseRemoteApiSpec extends Specification with CommandLineArguments {
  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  implicit val callRetries = Retry(2)
  implicit val callTimeout = Timeout(5 seconds)

  implicit class MatherWithImplicitValues[A](m: Matcher[A]) {
    def awaitWithTimeout(implicit r: Retry, t: Timeout) = {
      m.await(retries = r.value, timeout = t.value)
    }
  }

  def is(commandLine: CommandLine) = {
    val hatAddress: String = commandLine.value("host").getOrElse("http://localhost:8080")
    val ownerAuthParams = Map(
      "username" -> commandLine.value("username").getOrElse("andrius"),
      "password" -> commandLine.value("password").getOrElse("pa55w0rd")
    )

    testspec(hatAddress, ownerAuthParams)
  }

  def testspec(hatAddress: String, ownerAuthParams: Map[String, String])
}
