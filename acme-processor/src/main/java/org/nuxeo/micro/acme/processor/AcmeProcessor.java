/*
 * (C) Copyright 2019 Nuxeo (http://nuxeo.com/) and others.
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
 *     bdelbosc
 */
package org.nuxeo.micro.acme.processor;

import static org.nuxeo.lib.stream.computation.AbstractComputation.INPUT_1;
import static org.nuxeo.lib.stream.computation.AbstractComputation.OUTPUT_1;

import java.util.Arrays;
import java.util.Map;

import org.nuxeo.lib.stream.computation.Topology;
import org.nuxeo.runtime.stream.StreamProcessorTopology;

public class AcmeProcessor implements StreamProcessorTopology {
    @Override
    public Topology getTopology(Map<String, String> map) {
        return Topology.builder()
                       .addComputation(() -> new AcmeComputation("acmeComputation"),
                               Arrays.asList(INPUT_1 + ":source", OUTPUT_1 + ":status"))
                       .addComputation(() -> new CounterComputation("Counter"), Arrays.asList("i1:status", "o1:done"))
                       .build();
    }

}
