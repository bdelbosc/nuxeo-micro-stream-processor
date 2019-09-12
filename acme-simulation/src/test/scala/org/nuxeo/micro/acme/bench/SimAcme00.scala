/*
 * (C) Copyright 2019 Nuxeo (http://nuxeo.com/) and contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 *     Delbosc Benoit
 */
package org.nuxeo.micro.acme.bench

import io.gatling.core.Predef._
import io.gatling.core.config.GatlingFiles
import io.gatling.http.Predef._

import scala.concurrent.duration.Duration
import scala.io.Source

object ScnAcme {

  def get = (nbMessages: Integer, pause: Duration, messages: Iterator[Map[String, Any]]) => {
    scenario("Acme").exec(AcmeRest.createBatch(nbMessages)).exitHereIfFailed
      .repeat(nbMessages.intValue(), "count") {
        feed(messages).exec(AcmeRest.appendMessage()).pause(pause)
      }.exec(AcmeRest.getBatch())
  }
}

class SimAcme00 extends Simulation {
  val httpProtocol = http
    .baseUrl(Parameters.getBaseUrl())
    .disableWarmUp
    .acceptEncodingHeader("gzip, deflate")
    .connectionHeader("keep-alive")
  val scn = ScnAcme.get(Parameters.getNbMessages(), Parameters.getPause(), Feeders.messages())
  setUp(scn.inject(atOnceUsers(Parameters.getConcurrentUsers()))).protocols(httpProtocol).exponentialPauses
    .assertions(global.successfulRequests.percent.is(100))
}
