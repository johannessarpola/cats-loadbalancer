package fi.johannes.loadbalancer.services

import cats.syntax.either._
import org.http4s.Uri
import fi.johannes.loadbalancer.services.parsing.InvalidUri

trait ParseUri {
  def apply(uri: String): Either[Throwable, Uri]
}

object ParseUri {
  
  object Impl extends ParseUri {
    /**
     * Either returns proper Uri or InvalidUri
     */
    override def apply(uri: String): Either[Throwable, Uri] =
      Uri
        .fromString(uri)
        .leftMap(_ => InvalidUri(uri))
  }
}

object parsing {
  final case class InvalidUri(uri: String) extends Throwable {
    override def getMessage: String =
      s"Could not construct proper URI from $uri"
  }
}