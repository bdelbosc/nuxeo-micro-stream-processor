package org.nuxeo.micro.acme.bench;

import scala.concurrent.duration.{Duration, FiniteDuration}

object Parameters {

  def getBaseUrl(default: String = "http://acme.docker.localhost"): String = {
    System.getProperty("url", default)
  }

  def getConcurrentUsers(default: Integer = 8, prefix: String = ""): Integer = {
    Integer.getInteger(prefix + "users", default)
  }

  def getPause(defaultMs: Integer = 0, prefix: String = ""): Duration = {
    val pauseMs: Long = 0L + Integer.getInteger(prefix + "pause_ms", defaultMs)
    Duration(pauseMs, "millisecond")
  }

  def getSimulationDuration(default: Integer = 120): Duration = {
    val duration: Long = 0L + Integer.getInteger("duration", default)
    Duration(duration, "second")
  }

  def getRampDuration(default: Integer = 2, prefix: String = ""): FiniteDuration = {
    val ramp: Long = 0L + Integer.getInteger(prefix + "ramp", default)
    FiniteDuration(ramp, "second")
  }

  def getNbMessages(default: Integer = 1000): Integer = {
    Integer.getInteger("nbMessages", default)
  }


}
