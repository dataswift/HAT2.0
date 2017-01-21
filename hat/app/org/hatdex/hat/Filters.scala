package org.hatdex.hat

/*
 * Copyright (C) HAT Data Exchange Ltd - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Andrius Aucinas <andrius.aucinas@hatdex.org>, 10 2016
 */

import javax.inject.Inject

import play.api.http.HttpFilters
import play.api.mvc.EssentialFilter
import play.filters.cors.CORSFilter
import play.filters.csrf.CSRFFilter
import play.filters.gzip.GzipFilter

class Filters @Inject() (
  gzipFilter: GzipFilter,
  cSRFFilter: CSRFFilter,
  corsFilter: CORSFilter)
    extends HttpFilters {

  override def filters: Seq[EssentialFilter] = Seq(corsFilter, gzipFilter)
}