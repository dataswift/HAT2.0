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
 * 11 / 2017
 */

package org.hatdex.hat.utils

object Slugs {
  def slugify(str: String): String = {
    import java.text.Normalizer
    Normalizer.normalize(str, Normalizer.Form.NFD)
      .replaceAll("[^\\w ]", "")
      .replace(" ", "-")
      .toLowerCase
  }

  def slugifyUnique(str: String, suffix: Option[String], existing: Seq[String]): String =
    generateUniqueSlug(slugify(str), suffix, existing)

  private def generateUniqueSlug(slug: String, suffix: Option[String], existingSlugs: Seq[String]): String = {
    val slugSuffix = suffix.getOrElse("")
    if (!(existingSlugs contains slug + slugSuffix)) {
      s"$slug$slugSuffix"
    }
    else {
      val endsWithNumber = s"(.+-)([0-9]+)$slugSuffix".r
      val suffixes = existingSlugs.map {
        case endsWithNumber(_, number) => number.toInt
        case _                         => 0
      }
      s"$slug-${suffixes.max + 1}$slugSuffix"
    }
  }
}
