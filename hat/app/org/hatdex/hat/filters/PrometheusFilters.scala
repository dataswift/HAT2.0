package org.hatdex.hat.filters

import com.github.stijndehaes.playprometheusfilters.filters.{ StatusAndRouteLatencyFilter, StatusCounterFilter }
import play.api.http.DefaultHttpFilters

import javax.inject.Inject

class PrometheusFilters @Inject() (
    statusCounterFilter: StatusCounterFilter,
    statusAndRouteLatencyFilter: StatusAndRouteLatencyFilter)
    extends DefaultHttpFilters(statusCounterFilter, statusAndRouteLatencyFilter)
