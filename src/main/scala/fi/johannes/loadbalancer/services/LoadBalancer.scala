package fi.johannes.loadbalancer.services

import fi.johannes.loadbalancer.domain.*
import fi.johannes.loadbalancer.domain.UrlsRef.*
import fi.johannes.loadbalancer.services.RoundRobin.BackendsRoundRobin
import org.http4s.Uri.Path.Segment
import org.http4s.dsl.Http4sDsl
import org.http4s.{HttpRoutes, Request}
import cats.effect.IO

object LoadBalancer {
  def from(
    backends: Backends,
    sendAndExpectResponse: Request[IO] => SendAndExpect[String],
    uriParser: ParseUri,
    requestPathToBackend: AddRequestPathToBackendUrl,
    backendsRoundRobin: BackendsRoundRobin,
  ): HttpRoutes[IO] = {
    val dsl = new Http4sDsl[IO] {}
    import dsl._
    HttpRoutes.of[IO] { request =>
      backendsRoundRobin(backends).flatMap {
        _.fold(Ok("All backends are inactive")) { backendUrl =>
          val url = requestPathToBackend(backendUrl.value, request)
          for {
            uri      <- IO.fromEither(uriParser(url))
            response <- sendAndExpectResponse(request)(uri)
            result   <- Ok(response)
          } yield result
        }
      }
    }
  }
}