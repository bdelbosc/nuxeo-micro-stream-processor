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
package org.nuxeo.micro.acme.message;

import java.io.Serializable;

import org.apache.avro.reflect.Nullable;

public class AcmeMessage implements Serializable {

    private static final long serialVersionUID = 20190908L;

    private String batchId;

    private String key;

    public AcmeMessage() {
        // Empty constructor needed
    }

    @Nullable
    private Integer duration;

    @Nullable
    private Integer failAfter;

    @Nullable
    private String payload;

    public String getBatchId() {
        return batchId;
    }

    public void setBatchId(String batchId) {
        this.batchId = batchId;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public Integer getFailAfter() {
        return failAfter;
    }

    public void setFailAfter(Integer failAfter) {
        this.failAfter = failAfter;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    @Override
    public String toString() {
        return "AcmeMessage{" + "batchId='" + batchId + '\'' + ", key='" + key + '\'' + ", duration=" + duration
                + ", failAfter=" + failAfter + ", payload='" + payload + '\'' + '}';
    }
}
