/*
 * (C) Copyright 2019 Nuxeo (http://nuxeo.com/).
 * This is unpublished proprietary source code of Nuxeo SA. All rights reserved.
 * Notice of copyright on this source code does not indicate publication.
 *
 * Contributors:
 *     Nuxeo
 *
 */

package org.nuxeo.micro.producer;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.nuxeo.lib.stream.computation.Record;
import org.nuxeo.lib.stream.computation.StreamManager;
import org.nuxeo.lib.stream.log.LogOffset;
import org.nuxeo.runtime.api.Framework;
import org.nuxeo.runtime.kv.KeyValueService;
import org.nuxeo.runtime.kv.KeyValueStore;
import org.nuxeo.runtime.stream.StreamService;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

/**
 * Producer endpoint
 */
@OpenAPIDefinition(info = @Info(title = "Producer End Point", version = "1.0", description = "Nuxeo Producer Endpoint", contact = @Contact(url = "https://nuxeo.com")))
@Path("/producer")
public class ProducerEndPoint {

    protected static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Operation(summary = "Produce something", requestBody = @RequestBody(description = "The Url payload", required = true, content = {
            @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ProducerRequest.class)) }), responses = {
                    @ApiResponse(responseCode = "200", description = "Return the offset Id", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ProducerResponse.class))),
                    @ApiResponse(responseCode = "404", description = "Not found"),
                    @ApiResponse(responseCode = "500", description = "Oops") })
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response produce(String json, @QueryParam("debug") @DefaultValue("false") String debugStr)
            throws IOException {
        boolean debug = "true".equals(debugStr);
        ProducerRequest request = getRequest(json, debug);
        Record record = getRecord(request, debug);
        ProducerResponse response = appendToStream("source", record, debug);
        useKvStore(response);
        return Response.ok().entity(response.toString()).build();
    }

    protected void useKvStore(ProducerResponse response) {
        KeyValueService service = Framework.getService(KeyValueService.class);
        KeyValueStore kv = service.getKeyValueStore("default");
        kv.put(response.getId(), response.toString());
        System.out.println("Save in kv " + response.getId());
    }

    protected Record getRecord(ProducerRequest request, boolean debug) {
        Record record = Record.of(request.getKey(), request.toString().getBytes(UTF_8));
        if (debug) {
            System.out.println("Record: " + record);
        }
        return record;
    }

    protected ProducerRequest getRequest(String json, boolean debug) throws IOException {
        ProducerRequest request = OBJECT_MAPPER.readValue(json, ProducerRequest.class);
        if (debug) {
            System.out.println("Request: " + request);
        }
        return request;
    }

    protected ProducerResponse appendToStream(String stream, Record record, boolean debug) {
        // this doesn't work atm because we cannot init a stream without a processor class
        StreamService streamService = Framework.getService(StreamService.class);
        StreamManager manager = streamService.getStreamManager("default");
        LogOffset offset = manager.append(stream, record);
        ProducerResponse response = new ProducerResponse(offset);
        if (debug) {
            System.out.println("Response: " + response);
        }
        return response;
    }

}
