package fi.johannes.loadbalancer.services

import fi.johannes.loadbalancer.domain.Urls
import fi.johannes.loadbalancer.domain.Url
import fi.johannes.loadbalancer.domain.UrlsRef.Backends
import fi.johannes.loadbalancer.http.HttpClient
import org.http4s.{Request, Uri}
import munit.{CatsEffectSuite, FunSuite}
import cats.effect.IO
import cats.effect.unsafe.implicits.global

class LoadBalancerTest extends CatsEffectSuite {

  test("All backends are inactive because Urls is empty") {
    val obtained = (
      for {
          backends <- IO.ref(Urls.empty)
          loadBalancer = LoadBalancer.from(
            Backends(backends),
            _ => SendAndExpect.BackendSuccessTest,
            ParseUri.Impl,
            AddRequestPathToBackendUrl.Impl,
            RoundRobin.forBackends
          )
          result <- loadBalancer.orNotFound.run(Request[IO]())
        } yield result.body.compile.toVector.map(bytes => String(bytes.toArray))
      ).flatten

    assertIO(obtained, "All backends are inactive")
  }

  test("Success case") {
    val obtained = (
      for {
          backends <- IO.ref(Urls(Vector("localhost:8081", "localhost:8082").map(Url.apply)))
          loadBalancer = LoadBalancer.from(
            Backends(backends),
            _ => SendAndExpect.BackendSuccessTest,
            ParseUri.Impl,
            AddRequestPathToBackendUrl.Impl,
            RoundRobin.LocalHost8081
          )
          result <- loadBalancer.orNotFound.run(Request[IO](uri = Uri.unsafeFromString("localhost:8080/items/1")))
        } yield result.body.compile.toVector.map(bytes => String(bytes.toArray))
      ).flatten

    assertIO(obtained, "Success")
  }

  test("Resource not found (404) case") {
    val obtained = (
      for {
          backends <- IO.ref(Urls(Vector("localhost:8081", "localhost:8082").map(Url.apply)))
          emptyRequest = Request[IO]()
          loadBalancer = LoadBalancer.from(
            Backends(backends),
            _ => SendAndExpect.toBackend(HttpClient.BackendResourceNotFound, Request[IO]()),
            ParseUri.Impl,
            AddRequestPathToBackendUrl.Impl,
            RoundRobin.forBackends
          )
          result <- loadBalancer.orNotFound.run(Request[IO](uri = Uri.unsafeFromString("localhost:8080/items/1")))
        } yield result.body.compile.toVector.map(bytes => String(bytes.toArray))
      ).flatten

    assertIO(obtained, s"resource was not found")
  }
}