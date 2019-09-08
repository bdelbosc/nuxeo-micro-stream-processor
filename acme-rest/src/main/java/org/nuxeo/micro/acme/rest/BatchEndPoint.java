package org.nuxeo.micro.acme.rest;

import java.io.IOException;
import java.util.UUID;

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.nuxeo.lib.stream.codec.Codec;
import org.nuxeo.lib.stream.computation.Record;
import org.nuxeo.lib.stream.computation.StreamManager;
import org.nuxeo.lib.stream.log.LogOffset;
import org.nuxeo.micro.acme.Batch;
import org.nuxeo.micro.acme.Message;
import org.nuxeo.runtime.api.Framework;
import org.nuxeo.runtime.codec.CodecService;
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
@OpenAPIDefinition(info = @Info(title = "Batch End Point", version = "1.0", description = "ACME Batch endpoint", contact = @Contact(url = "https://nuxeo.com")))
@Path("/batches")
public class BatchEndPoint {
    protected static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private Codec<Message> codec;

    @GET
    @Path("{id: [a-zA-Z][a-zA-Z_0-9]+}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getBatchById(@PathParam("id") String id) {
        String ret = getBatchFromKv(id);
        if (ret == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok().entity(ret).build();
    }

    private String getBatchFromKv(String id) {
        KeyValueStore kv = getKeyValueStore();
        byte[] val = kv.get(id);
        if (val != null && val.length > 0) {
            return new String(val);
        }
        return null;
    }

    private KeyValueStore getKeyValueStore() {
        KeyValueService service = Framework.getService(KeyValueService.class);
        return service.getKeyValueStore("default");
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createBatch(@QueryParam("total") int total) {
        Batch ret = new Batch();
        ret.setId(UUID.randomUUID().toString());
        ret.setTotal(total);
        ret.setStatus("created");
        storeBatchToKv(ret);
        return Response.ok().entity(ret.toString()).build();
    }

    private void storeBatchToKv(Batch batch) {
        KeyValueStore kv = getKeyValueStore();
        kv.put(batch.getId(), batch.toString());
    }

    @Operation(summary = "Append to a batch", requestBody = @RequestBody(description = "The Url payload", required = true, content = {
            @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = BatchAppendRequest.class)) }), responses = {
                    @ApiResponse(responseCode = "200", description = "Return the offset", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = BatchAppendResponse.class))),
                    @ApiResponse(responseCode = "500", description = "Oops") })
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{id: [a-zA-Z][a-zA-Z_0-9\\-]+}/append")
    public Response appendToBatch(String json, @PathParam("id") String id,
            @QueryParam("debug") @DefaultValue("false") String debugStr) throws IOException {
        boolean debug = "true".equals(debugStr);
        BatchAppendRequest request = getRequest(json, debug);
        Record record = getRecord(request, id, debug);
        BatchAppendResponse response = appendToStream("source", record, debug);
        return Response.ok().entity(response.toString()).build();
    }

    protected Record getRecord(BatchAppendRequest request, String batchId, boolean debug) {
        Message message = getMessageFromRequest(request, batchId);
        Record record = Record.of(request.getKey(), getMessageCodec().encode(message));
        if (debug) {
            System.out.println("Record: " + record);
        }
        return record;
    }

    private Message getMessageFromRequest(BatchAppendRequest request, String batchId) {
        Message message = new Message();
        message.setBatchId(batchId);
        message.setKey(request.getKey());
        message.setDuration(request.getDuration());
        message.setFailAfter(request.getFailAfter());
        message.setPayload(request.getPayload());
        return message;
    }

    private Codec<Message> getMessageCodec() {
        if (codec == null) {
            codec = Framework.getService(CodecService.class).getCodec("avro", Message.class);
        }
        return codec;
    }

    protected BatchAppendRequest getRequest(String json, boolean debug) throws IOException {
        BatchAppendRequest request = OBJECT_MAPPER.readValue(json, BatchAppendRequest.class);
        if (debug) {
            System.out.println("Request: " + request);
        }
        return request;
    }

    protected BatchAppendResponse appendToStream(String stream, Record record, boolean debug) {
        StreamService streamService = Framework.getService(StreamService.class);
        StreamManager manager = streamService.getStreamManager("default");
        LogOffset offset = manager.append(stream, record);
        BatchAppendResponse response = new BatchAppendResponse(offset);
        if (debug) {
            System.out.println("Response: " + response);
        }
        return response;
    }
}
