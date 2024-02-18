package fi.johannes.loadbalancer.services


import fi.johannes.loadbalancer.domain.*
import fi.johannes.loadbalancer.domain.UrlsRef.*
import fi.johannes.loadbalancer.http.HttpClient
import munit.{CatsEffectSuite, FunSuite}
import cats.effect.IO

class HealthCheckBackendsTest extends CatsEffectSuite {

  test("add backend url to the Backends as soon as health check returns success") {
    val healthChecks = Urls(Vector("localhost:8081", "localhost:8082").map(Url.apply))
    val obtained = for
      backends     <- IO.ref(Urls(Vector(Url("localhost:8082"))))
      healthChecks <- IO.ref(healthChecks)
      result       <- HealthCheckBackends.checkHealthAndUpdateBackends(
        HealthChecks(healthChecks),
        Backends(backends),
        ParseUri.Impl,
        UpdateBackendsAndGet.Impl,
        RoundRobin.forHealthChecks,
        SendAndExpect.toHealthCheck(HttpClient.Hello)
      )
    yield result

    assertIO(obtained, Urls(Vector("localhost:8082", "localhost:8081").map(Url.apply)))
  }

  test("remove backend url from the Backends as soon as health check returns failure") {
    val urls = Urls(Vector("localhost:8081", "localhost:8082").map(Url.apply))
    val obtained = for
      backends     <- IO.ref(urls)
      healthChecks <- IO.ref(urls)
      result       <- HealthCheckBackends.checkHealthAndUpdateBackends(
        HealthChecks(healthChecks),
        Backends(backends),
        ParseUri.Impl,
        UpdateBackendsAndGet.Impl,
        RoundRobin.forHealthChecks,
        SendAndExpect.toHealthCheck(HttpClient.TestTimeoutFailure)
      )
    yield result

    assertIO(obtained, Urls(Vector("localhost:8082").map(Url.apply)))
  }
}
