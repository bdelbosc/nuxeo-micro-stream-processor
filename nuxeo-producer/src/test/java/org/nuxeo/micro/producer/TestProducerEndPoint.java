package org.nuxeo.micro.producer;

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

@NuxeoHelidonTest
@Deploy("OSGI-INF/stream-config.xml")
public class TestProducerEndPoint extends AbstractWebServerTest {

    @Override
    public Class<?>[] getResources() {
        return new Class[] { producerEndpoint.class };
    }

    @Test
    public void iCanPostToProducerEndPoint() {
        given().contentType(ContentType.JSON)
               .body("{\"I like\": \"json\"}")
               .when()
               .post("/producer")
               .then()
               .statusCode(500);

        given().contentType(ContentType.JSON)
               .body("{\"key\": \"1234\"}")
               .queryParam("debug", "true")
               .when()
               .post("/producer")
               .then()
               .statusCode(200)
               .body(containsString("source"));

    }

    @Test
    public void testSwaggerEndpoint() {
        given().when().get("/openapi.yaml").then().statusCode(200).body(containsString("Produce something"));
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
