package fi.johannes.loadbalancer.services

import org.http4s.client.UnexpectedStatus
import org.http4s.{Request, Uri}
import cats.syntax.option.*
import cats.effect.IO
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import org.typelevel.log4cats.syntax.*
import cats.syntax.applicative._

import scala.concurrent.duration.DurationInt
import fi.johannes.loadbalancer.http.HttpClient
import fi.johannes.loadbalancer.http.ServerHealthStatus

trait SendAndExpect[A] {
  def apply(uri: Uri): IO[A]
}

object SendAndExpect {

  implicit def logger: Logger[IO] = Slf4jLogger.getLogger[IO]
  val timeOut = 5.seconds;

  def toBackend(httpClient: HttpClient, req: Request[IO]): SendAndExpect[String] =
    new SendAndExpect[String] {
      override def apply(uri: Uri): IO[String] =
        info"[LOAD-BALANCER] sending request to $uri" *> httpClient
          .sendAndReceive(uri, req.some)
          .handleErrorWith {
            case UnexpectedStatus(org.http4s.Status.NotFound, _, _) =>
              s"resource was not found"
                .pure[IO]
                .flatTap(msg => warn"$msg")
            case _                                                  =>
              s"server with uri: $uri is dead"
                .pure[IO]
                .flatTap(msg => warn"$msg")
          }
    }

  def toHealthCheck(httpClient: HttpClient): SendAndExpect[ServerHealthStatus] =
    new SendAndExpect[ServerHealthStatus] {
      override def apply(uri: Uri): IO[ServerHealthStatus] =
        debug"[HEALTH-CHECK] checking $uri health" *>
          httpClient
            .sendAndReceive(uri, none)
            .as(ServerHealthStatus.Alive)
            .flatTap(_ => debug"$uri is alive")
            .timeout(timeOut)
            .handleErrorWith(_ => warn"$uri is dead" *> ServerHealthStatus.Dead.pure[IO])
    }


}
