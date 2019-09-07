package org.nuxeo.micro.processor;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

import org.junit.jupiter.api.Test;
import org.nuxeo.micro.helidon.AbstractWebServerTest;
import org.nuxeo.micro.helidon.NuxeoHealthCheck;
import org.nuxeo.micro.helidon.junit.NuxeoHelidonTest;

@NuxeoHelidonTest
public class TestProcessor extends AbstractWebServerTest {

    @Test
    public void testHealthCheck() {
        given().when().get("/health").then().statusCode(200).body("outcome", is("UP")).body("checks.size()", is(3));
    }

    @Override
    public Class<?>[] getResources() {
        return new Class[] { NuxeoHealthCheck.class };
    }
}
