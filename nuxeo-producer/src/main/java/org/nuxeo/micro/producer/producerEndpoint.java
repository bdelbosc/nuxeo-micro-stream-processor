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

import java.io.IOException;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.javasimon.Manager;
import org.nuxeo.lib.stream.computation.Record;
import org.nuxeo.lib.stream.computation.StreamManager;
import org.nuxeo.lib.stream.log.LogAppender;
import org.nuxeo.lib.stream.log.LogManager;
import org.nuxeo.lib.stream.log.LogOffset;
import org.nuxeo.runtime.api.Framework;
import org.nuxeo.runtime.codec.AvroCodecFactory;
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
public class producerEndpoint {

    protected ObjectMapper mapper = new ObjectMapper();

    @Operation(summary = "Produce something", requestBody = @RequestBody(description = "The Url payload", required = true, content = {
            @Content(mediaType = MediaType.APPLICATION_JSON) }), responses = {
                    @ApiResponse(responseCode = "200", description = "Return the Conversion Id", content = @Content(mediaType = MediaType.TEXT_PLAIN, schema = @Schema(implementation = String.class))),
                    @ApiResponse(responseCode = "404", description = "Not found"),
                    @ApiResponse(responseCode = "500", description = "Oops") })
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public Response source(String someJson) throws IOException {
        Record record = Record.of("foo", someJson.getBytes("UTF8"));
        LogOffset offset = appendToStreamUsingALog("source", record);
        return Response.ok().entity(String.format(offset.toString())).build();
    }

    private LogOffset appendToStreamUsingALog(String stream, Record record) {
        StreamService streamService = Framework.getService(StreamService.class);
        LogManager logManager = streamService.getLogManager("default");
        LogAppender<Record> appender = logManager.getAppender(stream);
        return appender.append(record.getKey(), record);
    }

    private LogOffset appendToStream(String stream, Record record) {
        // this doesn't work atm because we cannot init a stream without a processor class
        StreamService streamService = Framework.getService(StreamService.class);
        StreamManager manager = streamService.getStreamManager("default");
        return manager.append(stream, record);
    }

}
