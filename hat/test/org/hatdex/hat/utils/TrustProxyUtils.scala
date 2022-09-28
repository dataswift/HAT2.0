package org.hatdex.hat.utils

import io.dataswift.test.common.BaseSpec
import play.api.Logger

import java.security.PublicKey
import java.security.KeyFactory
import java.security.spec.X509EncodedKeySpec
import java.util.Base64

class TrustProxyUtilsTests extends BaseSpec {
  val logger: Logger                                 = Logger(this.getClass)
  implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global

  "TrustProxy" should "decode a good token" in {
    val token =
      "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJpc3MiOiJwZGEtYXBpLWdhdGV3YXkiLCJlbWFpbCI6ImJvYkBleGFtcGxlLmNvbSIsInBkYVVybCI6ImJvYnRoZXBsdW1iZXIuZXhhbXBsZS5jb20iLCJleHAiOjE2NzIxNjA1MDEsImlhdCI6MTY2NDI5ODEwMX0.cX3Uy4MhUQi2PXnKAcFv74zAvSqh_n5f-Qx94-0EvbZ3DGXHJetlvrGYXcCOSnauNy9JjYvzMNzJRwE7KrnisV9NSkTkSi6scCQ4fvFH-_wueGq_cCd9bgdls9BjgX4HN68c4Mv5rU7IKHcpiBgOe1ZEGzT-bteZ7hMn2q6dCVVXrRgv9dzI1Fm0qNneA7sRwjySYk1jfIDFiY9gL_clg1puO4dZ4KNW-ZC4YFcsxGKXbyMnCOTBzXx0a2aTcbvf9so_9zAxUbAlesPKtQAXVdZFR6GQp0vMWq38tiOuLX9P3X0w-VzFuzDejwDw433eVZ3gFawvEDMTe1aiHVs1_2f5f4xIv2gpXQ0cuawXv60ies4kltGL3u-b8Joi_406fRtC56ZXjMA3_k2hro8PFfVayipKu0kNEKTWkppBB5DAKObpgCtBnck12FbYwNW7FUJJhNVs6UT4kTynW6bE5EqDEZ3nE6dJC4MB2NlNTK_C3vgrBt8npNgPeMbJBtXNU_118Ty2Ns38SRTiZa29raBSsgVQiYo9W6tB4q5ddZ8wyHUJ_bAKloFRu1Z-atVocyZ2Msj2yiwf1ePADwW1twOLSSLJwuKd4XsYviVgnqZ5wMnh-G-UfxTX5Mh5-rIvvBa5MJRBfDS6P8AJmxyrSyRwFAwEabxJlBvMAkpq3SI"

    val publicKey =
      """|-----BEGIN PUBLIC KEY-----
         |MIICIjANBgkqhkiG9w0BAQEFAAOCAg8AMIICCgKCAgEA1QJwoEfJ98lSxX8H14EQ
         |vGMioKfOCZo+U8Ck4pDullzzdQVACYmZ2ONpzCXavWiE9fO/nPNU8P8Kn9WaThTa
         |m++f++2H833UNYMm3hqWbUWMBpMA7zKKox4PX9pq/CN5krP8efK/OXA+1rPaqAyj
         |VePwKkbqp+pE8cX34iNToO3d7v56DppIJ1dA8JBMoNyPkBZQhKv9/T26gMQBK7GW
         |fKoAPGIhCw9zKyd5Hh5aMc3U/fBl+EEqdkXrQ56+rTvL3FkDW2zldT6be4MwPu2i
         |lqHvb+X9xtTgtHHuwpKLuLtnQtNBg/ddvNGiAzdbZKZflSfXo9//6FbzMh39odPN
         |CLKAKDKnVPbYnMw2Iyp7AWZBtLDcxwW7UYq+bXJzE50ir3grsbxhHZOvTZb22lH7
         |atcbRHDq9KTWjhLT03rKYisOO8pxAzyoJtXM4qWJLo23oP9eSRVNLGFxEUdiUd4h
         |G7ctYoqC7JJRIgwHm7iGwT/2T0nSzk7V0VrZrQsnV2nkV54g9gv31Qs+OZgJHsTF
         |2gIZDZLofiXf6FhpjEi0oGsoLH+ii4Iov+Ga9ZQ6ISHdwn7YiCejKZ1/RIGCz51f
         |QRX5EwRUkF+OwlPuTQjErNHg1rFR8vau3uCTmjIhed2/6KKdI5c+Fqy1gY01iNu9
         |Bo4ZCeoNvePHhoIEnQC/wBMCAwEAAQ==
         |-----END PUBLIC KEY-----""".stripMargin

    val rsaPublicKey: PublicKey = TrustProxyUtils.stringToPublicKey(publicKey)
    val ret = TrustProxyUtils.decodeToken(token, rsaPublicKey)

    ret match {
      case Some(claim) =>
        println("sig ok - PASS")
        claim.content mustBe("{\"iss\":\"pda-api-gateway\",\"email\":\"bob@example.com\",\"pdaUrl\":\"bobtheplumber.example.com\"}")
        true
      case None =>
        println("sig not ok - FAIL")
        false
    }
  }

  it should "verify a good token" in {
    val token =
      "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJpc3MiOiJwZGEtYXBpLWdhdGV3YXkiLCJlbWFpbCI6ImJvYkBleGFtcGxlLmNvbSIsInBkYVVybCI6ImJvYnRoZXBsdW1iZXIuZXhhbXBsZS5jb20iLCJleHAiOjE2NzIxNjA1MDEsImlhdCI6MTY2NDI5ODEwMX0.cX3Uy4MhUQi2PXnKAcFv74zAvSqh_n5f-Qx94-0EvbZ3DGXHJetlvrGYXcCOSnauNy9JjYvzMNzJRwE7KrnisV9NSkTkSi6scCQ4fvFH-_wueGq_cCd9bgdls9BjgX4HN68c4Mv5rU7IKHcpiBgOe1ZEGzT-bteZ7hMn2q6dCVVXrRgv9dzI1Fm0qNneA7sRwjySYk1jfIDFiY9gL_clg1puO4dZ4KNW-ZC4YFcsxGKXbyMnCOTBzXx0a2aTcbvf9so_9zAxUbAlesPKtQAXVdZFR6GQp0vMWq38tiOuLX9P3X0w-VzFuzDejwDw433eVZ3gFawvEDMTe1aiHVs1_2f5f4xIv2gpXQ0cuawXv60ies4kltGL3u-b8Joi_406fRtC56ZXjMA3_k2hro8PFfVayipKu0kNEKTWkppBB5DAKObpgCtBnck12FbYwNW7FUJJhNVs6UT4kTynW6bE5EqDEZ3nE6dJC4MB2NlNTK_C3vgrBt8npNgPeMbJBtXNU_118Ty2Ns38SRTiZa29raBSsgVQiYo9W6tB4q5ddZ8wyHUJ_bAKloFRu1Z-atVocyZ2Msj2yiwf1ePADwW1twOLSSLJwuKd4XsYviVgnqZ5wMnh-G-UfxTX5Mh5-rIvvBa5MJRBfDS6P8AJmxyrSyRwFAwEabxJlBvMAkpq3SI"

    val publicKey =
      """|-----BEGIN PUBLIC KEY-----
         |MIICIjANBgkqhkiG9w0BAQEFAAOCAg8AMIICCgKCAgEA1QJwoEfJ98lSxX8H14EQ
         |vGMioKfOCZo+U8Ck4pDullzzdQVACYmZ2ONpzCXavWiE9fO/nPNU8P8Kn9WaThTa
         |m++f++2H833UNYMm3hqWbUWMBpMA7zKKox4PX9pq/CN5krP8efK/OXA+1rPaqAyj
         |VePwKkbqp+pE8cX34iNToO3d7v56DppIJ1dA8JBMoNyPkBZQhKv9/T26gMQBK7GW
         |fKoAPGIhCw9zKyd5Hh5aMc3U/fBl+EEqdkXrQ56+rTvL3FkDW2zldT6be4MwPu2i
         |lqHvb+X9xtTgtHHuwpKLuLtnQtNBg/ddvNGiAzdbZKZflSfXo9//6FbzMh39odPN
         |CLKAKDKnVPbYnMw2Iyp7AWZBtLDcxwW7UYq+bXJzE50ir3grsbxhHZOvTZb22lH7
         |atcbRHDq9KTWjhLT03rKYisOO8pxAzyoJtXM4qWJLo23oP9eSRVNLGFxEUdiUd4h
         |G7ctYoqC7JJRIgwHm7iGwT/2T0nSzk7V0VrZrQsnV2nkV54g9gv31Qs+OZgJHsTF
         |2gIZDZLofiXf6FhpjEi0oGsoLH+ii4Iov+Ga9ZQ6ISHdwn7YiCejKZ1/RIGCz51f
         |QRX5EwRUkF+OwlPuTQjErNHg1rFR8vau3uCTmjIhed2/6KKdI5c+Fqy1gY01iNu9
         |Bo4ZCeoNvePHhoIEnQC/wBMCAwEAAQ==
         |-----END PUBLIC KEY-----""".stripMargin

    val rsaPublicKey: PublicKey = TrustProxyUtils.stringToPublicKey(publicKey)
    val retTrue = TrustProxyUtils.verifyToken(
      token,
      rsaPublicKey,
      "bob@example.com",
      "bobtheplumber.example.com",
      "pda-api-gateway"
    )

    val retFalse = TrustProxyUtils.verifyToken(
      token,
      rsaPublicKey,
      "bo@example.com",
      "bbtheplumber.example.com",
      "pd-api-gateway"
    )

    retTrue mustBe true
    retFalse mustBe false
  }


  it should "not decode a bad token" in {
    val token =
      "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJpc3MiOiJwZGEtYXBpLWdhdGV3YXkiLCJlbWFpbCI6ImJvYkBleGFtcGxlLmNvbSIsInBkYVVybCI6ImJvYnRoZXBsdW1iZXIuZXhhbXBsZS5jb20iLCJleHAiOjE2NzIxNjA1MDEsImlhdCI6MTY2NDI5ODEwMX0.cX3Uy4MhUQi2PXnKAcFv74zAvSqh_n5f-Qx94-0EvbZ3DGXHJetlvrGYXcCOSnauNy9JjYvzMNzJRwE7KrnisV9NSkTkSi6scCQ4fvFH-_wueGq_cCd9bgdls9BjgX4HN68c4Mv5rU7IKHcpiBgOe1ZEGzT-bteZ7hMn2q6dCVVXrRgv9dzI1Fm0qNneA7sRwjySYk1jfIDFiY9gL_clg1puO4dZ4KNW-ZC4YFcsxGKXbyMnCOTBzXx0a2aTcbvf9so_9zAxUbAlesPKtQAXVdZFR6GQp0vMWq38tiOuLX9P3X0w-VzFuzDejwDw433eVZ3gFawvEDMTe1aiHVs1_2f5f4xIv2gpXQ0cuawXv60ies4kltGL3u-b8Joi_406fRtC56ZXjMA3_k2hro8PFfVayipKu0kNEKTWkppBB5DAKObpgCtBnck12FbYwNW7FUJJhNVs6UT4kTynW6bE5EqDEZ3nE6dJC4MB2NlNTK_C3vgrBt8npNgPeMbJBtXNU_118Ty2Ns38SRTiZa29raBSsgVQiYo9W6tB4q5ddZ8wyHUJ_bAKloFRu1Z-atVocyZ2Msj2yiwf1ePADwW1twOLSSLJwuKd4XsYviVgnqZ5wMnh-G-UfxTX5Mh5-rIvvBa5MJRBfDS6P8AJmxyrSyRwFAwEabxJlBvMAkpq3SI"
    val publicKey =
      """|-----BEGIN PUBLIC KEY-----
         |MIICIjANBgkqhkiG9w0BAQEFAAOCAg8AMIICCgKCAgEA1QJwoEfJ98lSxX8H14EQ
         |vGMioKfOCZo+U8Ck4pDullzzdQVACYmZ2ONpzCXavWiE9fO/nPNU8P8Kn9WaThTa
         |m++f++2H833UNYMm3hqWbUWMBpMA7zKKox4PX9pq/CN5krP8efK/OXA+1rPaqAyj
         |VePwKkbqp+pE8cX34iNToO3d7v56DppIJ1dA8JBMoNyPkBZQhKv9/T26gMQBK7GW
         |fKoAPGIhCw9zKyd5Hh5aMc3U/fBl+EEqdkXrQ56+rTvL3FkDW2zldT6be4MwPu2i
         |lqHvb+X9xtTgtHHuwpKLuLtnQtNBg/ddvNGiAzdbZKZflSfXo9//6FbzMh39odPN
         |CLKAKDKnVPbYnMw2Iyp7AWZBtLDcxwW7UYq+bXJzE50ir3grsbxhHZOvTZb22lH7
         |atcbRHDq9KTWjhLT03rKYisOO8pxAzyoJtXM4qWJLo23oP9eSRVNLGFxEUdiUd4h
         |G7ctYoqC7JJRIgwHm7iGwT/2T0nSzk7V0VrZrQsnV2nkV54g9gv31Qs+OZgJHsTF
         |2gIZDZLofiXf6FhpjEi0oGsoLH+ii4Iov+Ga9ZQ6ISHdwn7YiCejKZ1/RIGCz51f
         |QRX5EwRUkF+OwlPuTQjErNHg1rFR8vau3uTCmjIhed2/6KKdI5c+Fqy1gY01iNu9
         |Bo4ZCeoNvePHhoIEnQC/wBMCAwEAAQ==
         |-----END PUBLIC KEY-----""".stripMargin

    val rsaPublicKey: PublicKey = TrustProxyUtils.stringToPublicKey(publicKey)
    val ret = TrustProxyUtils.decodeToken(token, rsaPublicKey)

    ret match {
      case Some(claim) =>
        println("sig ok - FAIL")
        false
      case None =>
        println("sig not ok - PASS")
        true
    }
  }
}
