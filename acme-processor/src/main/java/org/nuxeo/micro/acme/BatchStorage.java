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
package org.nuxeo.micro.acme;

import java.io.IOException;

import org.nuxeo.runtime.api.Framework;
import org.nuxeo.runtime.kv.KeyValueService;
import org.nuxeo.runtime.kv.KeyValueStore;

import com.fasterxml.jackson.databind.ObjectMapper;

public class BatchStorage {
    protected static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private KeyValueStore keyValueStore;

    public void save(Batch batch) {
        getKeyValueStore().put(batch.getId(), batch.toString());
    }

    public Batch get(String batchId) {
        String json = getAsJson(batchId);
        if (json == null) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readValue(json, Batch.class);
        } catch (IOException e) {
            System.out.println("Cannot read json from" + json);
            return null;
        }
    }

    public String getAsJson(String batchId) {
        byte[] val = getKeyValueStore().get(batchId);
        if (val != null && val.length > 0) {
            return new String(val);
        }
        return null;
    }

    private KeyValueStore getKeyValueStore() {
        if (keyValueStore == null) {
            KeyValueService service = Framework.getService(KeyValueService.class);
            keyValueStore = service.getKeyValueStore("default");
        }
        return keyValueStore;
    }

}
