package org.hatdex.hat.resourceManagement

import java.io.{ StringReader, StringWriter }
import java.security.interfaces.{ RSAPrivateKey, RSAPublicKey }
import javax.inject.{ Inject, Singleton }

import com.atlassian.jwt.core.keys.KeyUtils
import org.bouncycastle.util.io.pem.{ PemObject, PemWriter }
import play.api.Configuration

import scala.concurrent.Future
import scala.util.Try

@Singleton
class HatKeyProvider @Inject() (configuration: Configuration) {
  private val keyUtils = new KeyUtils()

  def publicKey(hat: String): Future[RSAPublicKey] = {
    configuration.getString(s"hat.$hat.publicKey") map { confPublicKey =>
      val reader = new StringReader(confPublicKey)
      Try(keyUtils.readRsaPublicKeyFromPem(reader))
        .map(Future.successful)
        .recover {
          case e =>
            Future.failed(new HatServerDiscoveryException(s"Public Key reading for $hat failed", e))
        }
        .get
    } getOrElse {
      Future.failed(new HatServerDiscoveryException(s"Public Key for $hat not found"))
    }
  }

  def privateKey(hat: String): Future[RSAPrivateKey] = {
    configuration.getString(s"hat.$hat.privateKey") map { confPrivateKey =>
      val reader = new StringReader(confPrivateKey)
      Try(keyUtils.readRsaPrivateKeyFromPem(reader))
        .map(Future.successful)
        .recover {
          case e =>
            Future.failed(new HatServerDiscoveryException(s"Private Key reading for $hat failed", e))
        }
        .get
    } getOrElse {
      Future.failed(new HatServerDiscoveryException(s"Private Key for $hat not found"))
    }
  }

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
}
