package fi.johannes.loadbalancer.services

import cats.effect.IO
import fi.johannes.loadbalancer.domain.ServerHealthStatus
import fi.johannes.loadbalancer.domain.Urls
import fi.johannes.loadbalancer.domain.Url
import fi.johannes.loadbalancer.domain.UrlsRef
import fi.johannes.loadbalancer.domain.UrlsRef.Backends

trait UpdateBackendsAndGet {
  def apply(backends: UrlsRef.Backends, url: Url, status: ServerHealthStatus): IO[Urls]
}

object UpdateBackendsAndGet {

  object Impl extends UpdateBackendsAndGet {
    override def apply(backends: Backends, url: Url, status: ServerHealthStatus): IO[Urls] = 
      backends.urls.updateAndGet { urls =>
        status match {
          case ServerHealthStatus.Alive => urls.add(url)
          case ServerHealthStatus.Dead  => urls.remove(url)
        }
      }
  }
}
