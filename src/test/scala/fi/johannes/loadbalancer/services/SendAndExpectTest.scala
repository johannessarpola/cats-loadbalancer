package fi.johannes.loadbalancer.services

import cats.effect.IO
import org.http4s.{Request, Uri}
import munit.{CatsEffectSuite, FunSuite}
import fi.johannes.loadbalancer.http.HttpClient
import fi.johannes.loadbalancer.domain.ServerHealthStatus

class SendAndExpectTest extends CatsEffectSuite {

  val localhost8080 = "localhost:8080"
  val backend       = Uri.fromString(localhost8080).toOption.get
  val emptyRequest  = Request[IO]()

  test("toBackend [Success]") {
    val sendAndExpect = SendAndExpect.toBackend(HttpClientTesting.Hello, emptyRequest)
    val obtained      = sendAndExpect(backend)

    assertIO(obtained, "Hello")
  }

  test("toBackend [Failure]") {
    val sendAndExpect = SendAndExpect.toBackend(HttpClientTesting.RuntimeException, emptyRequest)
    val obtained      = sendAndExpect(backend)

    assertIO(obtained, s"server with uri: $localhost8080 is dead")
  }

  test("toHealthCheck [Alive]") {
    val sendAndExpect = SendAndExpect.toHealthCheck(HttpClientTesting.Hello)
    val obtained      = sendAndExpect(backend)

    assertIO(obtained, ServerHealthStatus.Alive)
  }

  test("toHealthCheck [Dead due to timeout]") {
    val sendAndExpect = SendAndExpect.toHealthCheck(HttpClientTesting.TestTimeoutFailure)
    val obtained      = sendAndExpect(backend)

    assertIO(obtained, ServerHealthStatus.Dead)
  }

  test("toHealthCheck [Dead due to exception]") {
    val sendAndExpect = SendAndExpect.toHealthCheck(HttpClientTesting.RuntimeException)
    val obtained      = sendAndExpect(backend)

    assertIO(obtained, ServerHealthStatus.Dead)
  }
}