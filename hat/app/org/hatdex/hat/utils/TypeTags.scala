/*
 * Copyright (C) HAT Data Exchange Ltd - All Rights Reserved
 *  Unauthorized copying of this file, via any medium is strictly prohibited
 *  Proprietary and confidential
 *  Written by Andrius Aucinas <andrius.aucinas@hatdex.org>, 11 2016
 */

package org.hatdex.hat.utils

object TypeTags {
  import scala.reflect.runtime.universe.{ TypeTag, typeOf, typeTag }
  def getTypeTag[T: TypeTag](t: T) = typeTag[T].tpe
  def getTypeOfTag[T: TypeTag] = typeOf[T]
}
