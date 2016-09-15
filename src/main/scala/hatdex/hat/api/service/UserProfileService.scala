/*
 * Copyright (C) 2016 Andrius Aucinas <andrius.aucinas@hatdex.org>
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
 */

package hatdex.hat.api.service

import com.typesafe.config.ConfigFactory
import hatdex.hat.api.models.ProfileField
import org.joda.time.LocalDateTime

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

trait UserProfileService extends BundleService {
  val configuration = ConfigFactory.load()

  def getPublicProfile: Future[(Boolean, Iterable[ProfileField])] = {
    val eventualMaybeProfileTable = sourceDatasetTables(Seq(("rumpel", "profile")), None).map(_.headOption)
    val eventualMaybeFacebookTable = sourceDatasetTables(Seq(("facebook", "profile_picture")), None).map(_.headOption)
    val eventualProfileRecord = eventualMaybeProfileTable flatMap { maybeTable =>
      maybeTable map { table =>
        val fieldset = getStructureFields(table)

        val startTime = LocalDateTime.now().minusDays(365)
        val endTime = LocalDateTime.now()
        val eventualValues = fieldsetValues(fieldset, startTime, endTime)

        eventualValues.map(values => getValueRecords(values, Seq(table)))
          .map { records => records.headOption }
          .map(_.flatMap(_.tables.flatMap(_.headOption)))
      } getOrElse {
        Future.successful(None)
      }
    }

    val eventualProfilePicture = eventualMaybeFacebookTable flatMap { maybeTable =>
      maybeTable map { table =>
        val fieldset = getStructureFields(table)
        val startTime = LocalDateTime.now().minusDays(365)
        val endTime = LocalDateTime.now()
        val eventualValues = fieldsetValues(fieldset, startTime, endTime)
        eventualValues.map(values => getValueRecords(values, Seq(table)))
          .map { records => records.headOption }
          .map { record => record.flatMap(_.tables.flatMap(_.headOption)) }
      } getOrElse {
        Future.successful(None)
      }
    }

    val eventualProfilePictureField = eventualProfilePicture map { maybeValueTable =>
      maybeValueTable map { valueTable =>
        val flattenedValues = flattenTableValues(valueTable)
        ProfileField("fb_profile_picture", Map("url" -> flattenedValues.getOrElse("url", "").toString), true)
      }
    }

    val profile = for {
      profilePictureField <- eventualProfilePictureField
      valueTable <- eventualProfileRecord.map(_.get)
    } yield {
      val flattenedValues = flattenTableValues(valueTable)
      val publicProfile = flattenedValues.get("private").contains("false")
      val profileFields = flattenedValues.collect {
        case ("fb_profile_photo", m: Map[String, String]) if profilePictureField.isDefined =>
          val publicField = m.get("private").contains("false")
          profilePictureField.get.copy(fieldPublic = publicField)
        case (fieldName, m: Map[String, String]) =>
          val publicField = m.get("private").contains("false")
          ProfileField(fieldName, m - "private", publicField)
      }

      (publicProfile, profileFields.filter(_.fieldPublic))
    }

    profile recover {
      case e =>
        (false, Iterable())
    }
  }

  def getHatConfiguration: Map[String, String] = {
    val hatParameters: Map[String, String] = Map(
      "hatName" -> configuration.getString("hat.name"),
      "hatDomain" -> configuration.getString("hat.domain"),
      "hatAddress" -> s"${configuration.getString("hat.name")}.${configuration.getString("hat.domain")}")
    hatParameters
  }

  def formatProfile(profileFields: Seq[ProfileField]): Map[String, Map[String, String]] = {
    val hatParameters: Map[String, String] = getHatConfiguration

    val links = Map(profileFields collect {
      // links
      case ProfileField("facebook", values, true) => "Facebook" -> values.getOrElse("link", "")
      case ProfileField("website", values, true)  => "Web" -> values.getOrElse("link", "")
      case ProfileField("youtube", values, true)  => "Youtube" -> values.getOrElse("link", "")
      case ProfileField("linkedin", values, true) => "LinkedIn" -> values.getOrElse("link", "")
      case ProfileField("google", values, true)   => "Google" -> values.getOrElse("link", "")
      case ProfileField("blog", values, true)     => "Blog" -> values.getOrElse("link", "")
      case ProfileField("twitter", values, true)  => "Twitter" -> values.getOrElse("link", "")
    }: _*).filterNot(_._2 == "").map {
      case (k, v) =>
        k -> (if (v.startsWith("http")) {
          v
        }
        else {
          s"http://$v"
        })
    }

    val contact = Map(profileFields collect {
      // contact
      case ProfileField("primary_email", values, true)     => "primary_email" -> values.getOrElse("value", "")
      case ProfileField("alternative_email", values, true) => "alternative_email" -> values.getOrElse("value", "")
      case ProfileField("mobile", values, true)            => "mobile" -> values.getOrElse("no", "")
      case ProfileField("home_phone", values, true)        => "home_phone" -> values.getOrElse("no", "")
    }: _*).filterNot(_._2 == "")

    val personal = Map(profileFields collect {
      case ProfileField("fb_profile_picture", values, true) => "profile_picture" -> values.getOrElse("url", "")
      // address
      case ProfileField("address_global", values, true) => "address_global" -> {
        values.getOrElse("city", "")+" "+
          values.getOrElse("county", "")+" "+
          values.getOrElse("country", "")
      }
      case ProfileField("address_details", values, true) => "address_details" -> {
        values.getOrElse("address_details", "")
      }

      case ProfileField("personal", values, true) =>
        "personal" -> {
          val title = values.get("title").map(_+" ").getOrElse("")
          val preferredName = values.get("preferred_name").map(_+" ").getOrElse("")
          val firstName = values.get("first_name").map { n =>
            if (preferredName.nonEmpty && !preferredName.startsWith(n)) {
              s"($n) "
            }
            else if (preferredName.isEmpty) {
              s"$n "
            }
            else {
              ""
            }
          }.getOrElse("")
          val middleName = values.get("middle_name").map(_+" ").getOrElse("")
          val lastName = values.getOrElse("last_name", "")
          s"$title$preferredName$firstName$middleName$lastName"
        }

      case ProfileField("emergency_contact", values, true) =>
        "emergency_contact" -> {
          values.getOrElse("first_name", "")+" "+
            values.getOrElse("last_name", "")+" "+
            values.getOrElse("relationship", "")+" "+
            ": "+values.getOrElse("mobile", "")+" "
        }
      case ProfileField("gender", values, true) => "gender" -> values.getOrElse("type", "")

      case ProfileField("nick", values, true)   => "nick" -> values.getOrElse("type", "")
      case ProfileField("age", values, true)    => "age" -> values.getOrElse("group", "")
      case ProfileField("birth", values, true)  => "brithDate" -> values.getOrElse("date", "")
    }: _*).filterNot(_._2 == "")

    val about = Map[String, String](
      "title" -> profileFields.find(_.name == "about").map(_.values.getOrElse("title", "")).getOrElse(""),
      "body" -> profileFields.find(_.name == "about").map(_.values.getOrElse("body", "")).getOrElse(""))

    //    val profile = hatParameters ++ profileParameters.filterNot(_._2 == "")

    val profile = Map(
      "hat" -> hatParameters,
      "links" -> links,
      "contact" -> contact,
      "profile" -> personal,
      "about" -> about).filterNot(_._2.isEmpty)

    profile
  }
}
