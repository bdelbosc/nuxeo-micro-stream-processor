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

import org.nuxeo.lib.stream.codec.Codec;
import org.nuxeo.lib.stream.computation.AbstractComputation;
import org.nuxeo.lib.stream.computation.ComputationContext;
import org.nuxeo.lib.stream.computation.Record;
import org.nuxeo.lib.stream.computation.Topology;
import org.nuxeo.micro.acme.Message;
import org.nuxeo.runtime.api.Framework;
import org.nuxeo.runtime.codec.CodecService;
import org.nuxeo.runtime.stream.StreamProcessorTopology;

public class AcmeProcessor implements StreamProcessorTopology {
    @Override
    public Topology getTopology(Map<String, String> map) {
        return Topology.builder()
                       .addComputation(() -> new MyComputation("myComputation"), Collections.singletonList("i1:source"))
                       .build();

    }

    private class MyComputation extends AbstractComputation {
        private Codec<Message> codec;

        public MyComputation(String name) {
            super(name, 1, 0);
        }

        private Codec<Message> getMessageCodec() {
            if (codec == null) {
                codec = Framework.getService(CodecService.class).getCodec("avro", Message.class);
            }
            return codec;
        }

        @Override
        public void processRecord(ComputationContext context, String streamName, Record record) {
            Message message = getMessageCodec().decode(record.getData());
            System.out.println("Received " + message);
            if (message.getDuration() != null && message.getDuration() > 0) {
                simulateWorkDuration(message.getDuration());
                System.out.println("done" + message);
            }
            context.askForCheckpoint();
        }

        private void simulateWorkDuration(Integer duration) {
            try {
                Thread.sleep(duration);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Interrupted", e);
            }
        }
    }
}
