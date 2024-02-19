package fi.johannes.loadbalancer.domain

import fi.johannes.loadbalancer.domain.Url
import pureconfig.ConfigReader
import pureconfig._
import pureconfig.generic.derivation.default._

final case class Config(
  port: Int,
  host: String,
  backends: Urls,
  healthCheckInterval: Interval,
) derives ConfigReader

object Config:

  /*
  First of all the derives ConfigReader annotation instructs PureConfig
  to automatically derive a configuration reader for this case class,
  allowing it to be used to parse configuration files into Config instances.
  This requires that there must be given ConfigReaders for all members (recursively).
  Because extending AnyVal prevents using derives clauses,
  so need to define the given-s ourselves.

  given urlsReader:
    ConfigReader[Urls]: This custom reader is defined
    for the Urls type and instructs PureConfig to read a configuration
    value of type Vector[Url] and map it to an Urls instance using
    the Urls.apply constructor.

  given urlReader: ConfigReader[Url]:
    This custom reader is defined
    for the Url type and instructs PureConfig to read a configuration
    value of type String and map it to a Url instance using the
    Url.apply constructor.

  given healthCheckReader:
    ConfigReader[HealthCheckInterval]:
    This custom reader is defined for the HealthCheckInterval
    type and instructs PureConfig to read a configuration value
    of type Long and map it to a HealthCheckInterval instance
    using the HealthCheckInterval.apply constructor.
   */
  given urlsReader: ConfigReader[Urls] = ConfigReader[Vector[Url]].map(Urls.apply)

  given urlReader: ConfigReader[Url] = ConfigReader[String].map(Url.apply)

  given healthCheckReader: ConfigReader[Interval] =
    ConfigReader[Long].map(Interval.apply)