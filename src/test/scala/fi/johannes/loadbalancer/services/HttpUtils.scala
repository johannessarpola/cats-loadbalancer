package fi.johannes.loadbalancer.services

import cats.Id
import cats.effect.IO
import org.http4s.client.{Client, UnexpectedStatus}
import org.http4s.{Request, Uri}

import scala.concurrent.duration.DurationInt
import fi.johannes.loadbalancer.http.HttpClient
import fi.johannes.loadbalancer.domain.Url

object HttpClientTesting:
  lazy val Hello: HttpClient                   = (_, _) => IO.pure("Hello")
  lazy val RuntimeException: HttpClient        = (_, _) => IO.raiseError(new RuntimeException("Server is dead"))
  lazy val TestTimeoutFailure: HttpClient      = (_, _) => IO.sleep(6.seconds).as("")
  lazy val BackendResourceNotFound: HttpClient = (_, _) =>
    IO.raiseError {
      UnexpectedStatus(
        org.http4s.Status.NotFound,
        org.http4s.Method.GET,
        Uri.unsafeFromString("localhost:8081"),
      )
    }

object SendAndExpectTesting:
    val BackendSuccessTest: SendAndExpect[String] = _ => IO("Success")

object RoundRobinTesting:
    val TestId: RoundRobin[Id]            = _ => IO.pure(Url("localhost:8081"))
    val LocalHost8081: RoundRobin[Option] = _ => IO.pure(Some(Url("localhost:8081")))