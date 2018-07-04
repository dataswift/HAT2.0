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

package org.hatdex.hat.api.service.richData

class RichDataServiceException(message: String = "", cause: Throwable = None.orNull) extends Exception(message, cause)
case class RichDataPermissionsException(message: String = "", cause: Throwable = None.orNull) extends RichDataServiceException(message, cause)
case class RichDataDuplicateException(message: String = "", cause: Throwable = None.orNull) extends RichDataServiceException(message, cause)
case class RichDataMissingException(message: String = "", cause: Throwable = None.orNull) extends RichDataServiceException(message, cause)
case class RichDataDuplicateBundleException(message: String = "", cause: Throwable = None.orNull) extends RichDataServiceException(message, cause)
case class RichDataDuplicateDebitException(message: String = "", cause: Throwable = None.orNull) extends RichDataServiceException(message, cause)
case class RichDataDebitException(message: String = "", cause: Throwable = None.orNull) extends RichDataServiceException(message, cause)
case class RichDataBundleFormatException(message: String = "", cause: Throwable = None.orNull) extends RichDataServiceException(message, cause)
