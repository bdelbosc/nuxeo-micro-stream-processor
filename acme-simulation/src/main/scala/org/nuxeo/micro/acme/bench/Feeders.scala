package org.nuxeo.micro.acme.bench

import java.util.UUID
import scala.util.Random

object Feeders {

 val feeder = Iterator.continually(Map("key" -> UUID.randomUUID.toString(),
      "payload" -> Random.alphanumeric.take(64).mkString,
      // "duration" -> Random.nextInt(200))
      "duration" -> Parameters.getWorkDuration()))

  def messages() = {
     feeder
  }

}
