package org.hatdex.hat.contract.models

import org.hatdex.hat.api.models.ErrorMessage

object Errors {
  def richDataDuplicate(error: Throwable) =
    ErrorMessage("Bad Request", s"Duplicate data - ${error.getMessage}")
  def richDataError(error: Throwable) =
    ErrorMessage("Bad Request", s"Could not insert data - ${error.getMessage}")
}
