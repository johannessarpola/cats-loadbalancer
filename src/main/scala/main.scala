import fi.johannes.loadbalancer.domain.UrlsRef.*
import fi.johannes.loadbalancer.domain.*
import fi.johannes.loadbalancer.domain.Url
import fi.johannes.loadbalancer.errors.config.InvalidConfig
import fi.johannes.loadbalancer.http.HttpServer
import fi.johannes.loadbalancer.services.{ParseUri, RoundRobin, UpdateBackendsAndGet}
import cats.effect.{IO, IOApp}
import cats.implicits.{catsSyntaxTuple2Parallel, catsSyntaxTuple2Semigroupal}
import com.comcast.ip4s.{Host, Port}
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import org.typelevel.log4cats.syntax.*
import pureconfig.{ConfigReader, ConfigSource}

object Main extends IOApp.Simple {

  implicit def logger: Logger[IO] = Slf4jLogger.getLogger[IO]

  private def hostAndPort(
    host: String,
    port: Int,
  ): Either[InvalidConfig, (Host, Port)] =
    (
      Host.fromString(host),
      Port.fromInt(port),
    ).tupled.toRight(InvalidConfig)

  override def run: IO[Unit] =
    for {
      config <- IO(ConfigSource.default.loadOrThrow[Config])
      backendUrls = config.backends
      backends     <- IO.ref(backendUrls)
      healthChecks <- IO.ref(backendUrls)
      hostAndPort  <- IO.fromEither(hostAndPort(config.host, config.port))
      (host, port) = hostAndPort
      _ <- info"Starting server on $host:$port"
      _ <- HttpServer.start(
          Backends(backends),
          HealthChecks(healthChecks),
          port,
          host,
          config.healthCheckInterval,
          ParseUri.Impl,
          UpdateBackendsAndGet.Impl,
          RoundRobin.forBackends,
          RoundRobin.forHealthChecks,
      )
    } yield ()
}
