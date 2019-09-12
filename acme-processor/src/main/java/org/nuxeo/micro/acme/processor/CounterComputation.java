/*
 * (C) Copyright 2019 Nuxeo SA (http://nuxeo.com/) and others.
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

import static java.nio.charset.StandardCharsets.UTF_8;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.nuxeo.lib.stream.codec.Codec;
import org.nuxeo.lib.stream.computation.AbstractBatchComputation;
import org.nuxeo.lib.stream.computation.ComputationContext;
import org.nuxeo.lib.stream.computation.Record;
import org.nuxeo.micro.acme.Batch;
import org.nuxeo.micro.acme.BatchStorage;
import org.nuxeo.micro.acme.message.Status;
import org.nuxeo.runtime.api.Framework;
import org.nuxeo.runtime.codec.CodecService;

public class CounterComputation extends AbstractBatchComputation {

    private static final Logger log = LogManager.getLogger(CounterComputation.class);

    private Codec<Status> codecStatus;

    private BatchStorage batchStorage;

    private BatchStorage getBatchStorage() {
        if (batchStorage == null) {
            batchStorage = new BatchStorage();
        }
        return batchStorage;
    }

    public CounterComputation(String name) {
        super(name, 1, 1);
    }

    private Codec<Status> getStatusCodec() {
        if (codecStatus == null) {
            codecStatus = Framework.getService(CodecService.class).getCodec("avro", Status.class);
        }
        return codecStatus;
    }

    @Override
    protected void batchProcess(ComputationContext context, String stream, List<Record> records) {
        Map<String, Integer> counters = new HashMap<>();
        for (Record record : records) {
            Status status = getStatusCodec().decode(record.getData());
            String batchId = status.getBatchId();
            if (counters.containsKey(batchId)) {
                counters.put(batchId, counters.get(batchId) + status.getProcessed());
            } else {
                counters.put(batchId, status.getProcessed());
            }
        }
        counters.keySet().forEach(batchId -> updateBatch(context, batchId, counters.get(batchId)));
        context.askForCheckpoint();
    }

    private void updateBatch(ComputationContext context, String batchId, int processed) {
        Batch batch = getBatchStorage().get(batchId);
        if (batch == null) {
            log.warn("Try to update an unkown batch " + batchId);
            return;
        }
        batch.setProcessed(batch.getProcessed() + processed);
        if (batch.getProcessed() >= batch.getTotal()) {
            batch.setStatus("completed");
            context.produceRecord(OUTPUT_1, Record.of(batchId, batch.toString().getBytes(UTF_8)));
        } else {
            batch.setStatus("processing");
        }
        if (log.isDebugEnabled()) {
            log.debug("Update batch: " + batch);
        }
        getBatchStorage().save(batch);
    }

    @Override
    public void batchFailure(ComputationContext context, String stream, List<Record> records) {
        log.error("Failure in processing counters records");
    }

}
