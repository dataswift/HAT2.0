package org.hatdex.hat.resourceManagement

import java.io.{ StringReader, StringWriter }
import java.security.interfaces.{ RSAPrivateKey, RSAPublicKey }
import javax.inject.{ Inject, Singleton }

import com.atlassian.jwt.core.keys.KeyUtils
import org.bouncycastle.util.io.pem.{ PemObject, PemWriter }
import play.api.cache.CacheApi
import play.api.{ Configuration, Logger }
import play.api.libs.ws.WSClient

import scala.concurrent.{ ExecutionContext, Future }
import scala.util.Try

trait HatKeyProvider {
  protected val keyUtils = new KeyUtils()

  def publicKey(hat: String)(implicit ec: ExecutionContext): Future[RSAPublicKey]

  def privateKey(hat: String)(implicit ec: ExecutionContext): Future[RSAPrivateKey]

  def toString(rsaPublicKey: RSAPublicKey): String = {
    val pemObject = new PemObject("PUBLIC KEY", rsaPublicKey.getEncoded)
    val stringPemWriter = new StringWriter()
    val pemWriter: PemWriter = new PemWriter(stringPemWriter)
    pemWriter.writeObject(pemObject)
    pemWriter.flush()
    val pemPublicKey = stringPemWriter.toString
    pemPublicKey
  }

  def issuer(hat: String) = hat

  protected def readRsaPublicKey(publicKey: String): Future[RSAPublicKey] = {
    val reader = new StringReader(publicKey)
    Try(keyUtils.readRsaPublicKeyFromPem(reader))
      .map(Future.successful)
      .recover {
        case e =>
          Future.failed(new HatServerDiscoveryException(s"Public Key reading failed", e))
      }
      .get
  }

  protected def readRsaPrivateKey(privateKey: String): Future[RSAPrivateKey] = {
    val reader = new StringReader(privateKey)
    Try(keyUtils.readRsaPrivateKeyFromPem(reader))
      .map(Future.successful)
      .recover {
        case e =>
          Future.failed(new HatServerDiscoveryException(s"Private Key reading failed", e))
      }
      .get
  }
}

@Singleton
class HatKeyProviderConfig @Inject() (configuration: Configuration) extends HatKeyProvider {
  def publicKey(hat: String)(implicit ec: ExecutionContext): Future[RSAPublicKey] = {
    configuration.getString(s"hat.$hat.publicKey") map { confPublicKey =>
      readRsaPublicKey(confPublicKey)
    } getOrElse {
      Future.failed(new HatServerDiscoveryException(s"Public Key for $hat not found"))
    }
  }

  def privateKey(hat: String)(implicit ec: ExecutionContext): Future[RSAPrivateKey] = {
    configuration.getString(s"hat.$hat.privateKey") map { confPrivateKey =>
      readRsaPrivateKey(confPrivateKey)
    } getOrElse {
      Future.failed(new HatServerDiscoveryException(s"Private Key for $hat not found"))
    }
  }
}

@Singleton
class HatKeyProviderMilliner @Inject() (
    val configuration: Configuration,
    val cache: CacheApi,
    val ws: WSClient) extends HatKeyProvider with MillinerHatSignup {
  val logger = Logger(this.getClass)

  def publicKey(hat: String)(implicit ec: ExecutionContext): Future[RSAPublicKey] = {
    getHatSignup(hat) flatMap { signup =>
      logger.debug(s"Received signup info, parsing public key ${signup.keys.map(_.publicKey)}")
      readRsaPublicKey(signup.keys.get.publicKey)
    } recoverWith {
      case e =>
        Future.failed(new HatServerDiscoveryException(s"Public Key for $hat not found", e))
    }
  }

  def privateKey(hat: String)(implicit ec: ExecutionContext): Future[RSAPrivateKey] = {
    getHatSignup(hat) flatMap { signup =>
      logger.debug(s"Received signup info, parsing private key ${signup.keys.map(_.privateKey)}")
      readRsaPrivateKey(signup.keys.get.privateKey)
    } recoverWith {
      case e =>
        Future.failed(new HatServerDiscoveryException(s"Private Key for $hat not found"))
    }
  }
}

