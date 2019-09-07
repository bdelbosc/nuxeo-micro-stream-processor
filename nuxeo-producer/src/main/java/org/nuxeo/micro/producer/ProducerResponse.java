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
package org.nuxeo.micro.producer;

import org.nuxeo.lib.stream.log.LogOffset;

public class ProducerResponse {
    protected String id;

    protected String stream;

    protected Integer partition;

    protected Long offset;

    public ProducerResponse(LogOffset offset) {
        this.id = offset.toString();
        this.stream = offset.partition().name();
        this.partition = offset.partition().partition();
        this.offset = offset.offset();
    }

    public String getId() {
        return id;
    }

    public String getStream() {
        return stream;
    }

    public Integer getPartition() {
        return partition;
    }

    public Long getOffset() {
        return offset;
    }

    @Override
    public String toString() {
        return String.format("{\"id\":\"%s\", \"stream\": \"%s\", \"partition\": %d, \"offset\": %d}", id, stream,
                partition, offset);
    }

}
