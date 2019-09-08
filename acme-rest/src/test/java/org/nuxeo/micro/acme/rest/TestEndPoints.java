package org.nuxeo.micro.acme.rest;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.nuxeo.lib.stream.log.LogManager;
import org.nuxeo.micro.helidon.AbstractWebServerTest;
import org.nuxeo.micro.helidon.junit.Deploy;
import org.nuxeo.micro.helidon.junit.NuxeoHelidonTest;
import org.nuxeo.runtime.api.Framework;
import org.nuxeo.runtime.stream.StreamService;

import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;

@NuxeoHelidonTest
@Deploy("OSGI-INF/stream-config.xml")
public class TestEndPoints extends AbstractWebServerTest {

    @Override
    public Class<?>[] getResources() {
        return new Class[] { BatchEndPoint.class };
    }

    @Test
    public void iCanUseBatchEndPoint() {
        given().when().get("/batches/foo").then().statusCode(404);

        JsonPath path = given().contentType(ContentType.JSON)
                               .when()
                               .post("/batches")
                               .then()
                               .statusCode(200)
                               .body(containsString("created"))
                               .extract()
                               .body()
                               .jsonPath();
        String batchId = path.get("id");

        given().when().get("/batches/" + batchId).then().statusCode(200)
                .body(containsString(batchId));

        given().contentType(ContentType.JSON)
               .body("{\"key\": \"1234\"}")
               .queryParam("debug", "true")
               .when()
               .post("/batches/" + batchId + "/append")
               .then()
               .statusCode(200)
               .body(containsString("source"));
    }

    @Test
    public void testSwaggerEndpoint() {
        given().when().get("/openapi.yaml").then().statusCode(200).body(containsString("ACME Batch endpoint"));
    }

    /**
     * Added to clean streams (when hitting input endpoint).
     */
    @AfterAll
    public static void tearDown() {
        StreamService service = Framework.getService(StreamService.class);
        LogManager manager = service.getLogManager("default");
        manager.delete("source");
    }
}
