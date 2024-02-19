package fi.johannes.loadbalancer.errors

object config:
  type InvalidConfig = InvalidConfig.type

  case object InvalidConfig extends Throwable {
    override def getMessage: String =
      "invalid config"
  }
