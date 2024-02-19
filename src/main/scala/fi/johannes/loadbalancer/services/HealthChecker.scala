package fi.johannes.loadbalancer.services

import fi.johannes.loadbalancer.domain.*
import fi.johannes.loadbalancer.domain.UrlsRef.*
import fi.johannes.loadbalancer.domain.ServerHealthStatus
import fi.johannes.loadbalancer.services.RoundRobin.HealthChecksRoundRobin
import cats.effect.IO

import scala.concurrent.duration.DurationLong

object HealthChecker {

  def periodically(
    healthChecks: HealthChecks,
    backends: Backends,
    parseUri: ParseUri,
    updateBackendsAndGet: UpdateBackendsAndGet,
    healthChecksRoundRobin: HealthChecksRoundRobin,
    sendAndExpectStatus: SendAndExpect[ServerHealthStatus],
    healthCheckInterval: Interval,
  ): IO[Unit] =
    checkHealthAndUpdateBackends(
      healthChecks,
      backends,
      parseUri,
      updateBackendsAndGet,
      healthChecksRoundRobin,
      sendAndExpectStatus,
    ).flatMap(_ => IO.sleep(healthCheckInterval.value.seconds)).foreverM

  private[services] def checkHealthAndUpdateBackends(
    healthChecks: HealthChecks,
    backends: Backends,
    parseUri: ParseUri,
    updateBackendsAndGet: UpdateBackendsAndGet,
    healthChecksRoundRobin: HealthChecksRoundRobin,
    sendAndExpectStatus: SendAndExpect[ServerHealthStatus],
  ): IO[Urls] =
    for {
      // gets a healthcheck endpoint from round robin list
      currentUrl <- healthChecksRoundRobin(healthChecks)

      // create uri to call
      uri        <- IO.fromEither(parseUri(currentUrl.value))
      
      //  call the healthcheck endpoint and check status
      status     <- sendAndExpectStatus(uri)
      
      // remove backend if it fails
      updated    <- updateBackendsAndGet(backends, currentUrl, status)
    } yield updated
}
