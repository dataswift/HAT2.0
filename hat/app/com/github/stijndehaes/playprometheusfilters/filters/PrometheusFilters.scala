package com.github.stijndehaes.playprometheusfilters.filters

import play.api.http.DefaultHttpFilters

import javax.inject.Inject

class PrometheusFilters @Inject() (
    statusCounterFilter: StatusCounterFilter,
    statusAndRouteLatencyFilter: StatusAndRouteLatencyFilter)
    extends DefaultHttpFilters(statusCounterFilter, statusAndRouteLatencyFilter)
