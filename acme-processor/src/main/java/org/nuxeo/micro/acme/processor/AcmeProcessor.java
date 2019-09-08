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

import java.util.Collections;
import java.util.Map;

import org.nuxeo.lib.stream.computation.AbstractComputation;
import org.nuxeo.lib.stream.computation.ComputationContext;
import org.nuxeo.lib.stream.computation.Record;
import org.nuxeo.lib.stream.computation.Topology;
import org.nuxeo.runtime.stream.StreamProcessorTopology;

public class AcmeProcessor implements StreamProcessorTopology {
    @Override
    public Topology getTopology(Map<String, String> map) {
        return Topology.builder()
                       .addComputation(() -> new MyComputation("myComputation"), Collections.singletonList("i1:source"))
                       .build();

    }

    private class MyComputation extends AbstractComputation {
        public MyComputation(String name) {
            super(name, 1, 0);
        }

        @Override
        public void processRecord(ComputationContext context, String streamName, Record record) {
            System.out.println("Received " + record);
            context.askForCheckpoint();
        }
    }
}
