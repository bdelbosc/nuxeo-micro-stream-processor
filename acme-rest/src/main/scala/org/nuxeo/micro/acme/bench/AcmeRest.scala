package org.nuxeo.micro.acme.bench;

import io.gatling.core.Predef._
import io.gatling.http.Predef._

import scala.util.Random

object AcmeRest {

  def encodePath = (path: String) => {
    java.net.URLEncoder.encode(path, "UTF-8")
  }

  def createBatch(total: Int = 100) = {
        http("Create a batch id")
          .post("/batches")
          .queryParam("total", total)
          .headers(Headers.base)
          .asJson.check(jsonPath("$.id").saveAs("batchId"))
  }

  def getBatch() = {
    http("Get a batch id")
      .get("/batches/${batchId}")
      .headers(Headers.base)
  }

  def appendMessage(key: String, payload: String, duration: Int = 10) = {
    http("Append")
      .post("/batches/${batchId}/append")
      .headers(Headers.base)
      .header("Content-Type", "application/json")
      .body(StringBody("""{ "key": """" + key + """","duration": """+ duration + """, "payload": """"+ payload + """"}"""))
      .check(status.in(200))
  }

}
