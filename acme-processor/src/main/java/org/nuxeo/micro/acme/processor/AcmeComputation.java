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

import org.nuxeo.lib.stream.codec.Codec;
import org.nuxeo.lib.stream.computation.AbstractComputation;
import org.nuxeo.lib.stream.computation.ComputationContext;
import org.nuxeo.lib.stream.computation.Record;
import org.nuxeo.micro.acme.message.AcmeMessage;
import org.nuxeo.micro.acme.message.Status;
import org.nuxeo.runtime.api.Framework;
import org.nuxeo.runtime.codec.CodecService;

class AcmeComputation extends AbstractComputation {
    private Codec<AcmeMessage> codec;

    private Codec<Status> codecStatus;

    public AcmeComputation(String name) {
        super(name, 1, 1);
    }

    private Codec<AcmeMessage> getMessageCodec() {
        if (codec == null) {
            codec = Framework.getService(CodecService.class).getCodec("avro", AcmeMessage.class);
        }
        return codec;
    }

    private Codec<Status> getStatusCodec() {
        if (codecStatus == null) {
            codecStatus = Framework.getService(CodecService.class).getCodec("avro", Status.class);
        }
        return codecStatus;
    }

    @Override
    public void processRecord(ComputationContext context, String streamName, Record record) {
        AcmeMessage message = getMessageCodec().decode(record.getData());
        if (message.getDuration() != null && message.getDuration() > 0) {
            simulateWorkDuration(message.getDuration());
        }

        Status status = new Status(message.getBatchId(), 1);
        context.produceRecord(OUTPUT_1, Record.of(message.getBatchId(), getStatusCodec().encode(status)));
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
