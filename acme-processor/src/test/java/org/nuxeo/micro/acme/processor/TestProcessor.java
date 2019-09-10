package org.nuxeo.micro.acme.processor;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

import org.junit.jupiter.api.Test;
import org.nuxeo.micro.NuxeoHealthCheck;
import org.nuxeo.micro.helidon.AbstractWebServerTest;
import org.nuxeo.micro.helidon.junit.Deploy;
import org.nuxeo.micro.helidon.junit.NuxeoHelidonTest;

@NuxeoHelidonTest
@Deploy("OSGI-INF/stream-config.xml")
public class TestProcessor extends AbstractWebServerTest {

    @Test
    public void testHealthCheck() {
        given().when().get("/health").then().statusCode(200).body("outcome", is("UP"));
    }

    @Override
    public Class<?>[] getResources() {
        return new Class[] { NuxeoHealthCheck.class };
    }
}
