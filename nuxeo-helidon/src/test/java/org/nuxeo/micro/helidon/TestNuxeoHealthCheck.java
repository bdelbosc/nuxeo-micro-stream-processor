/*
 * (C) Copyright 2018 Nuxeo SA (http://nuxeo.com/).
 * This is unpublished proprietary source code of Nuxeo SA. All rights reserved.
 * Notice of copyright on this source code does not indicate publication.
 *
 * Contributors:
 *     Gildas Lefevre
 */
package org.nuxeo.micro.helidon;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

import org.junit.jupiter.api.Test;
import org.nuxeo.micro.helidon.junit.NuxeoHelidonTest;

/**
 * Test class for the health check of the containers.
 */
@NuxeoHelidonTest
public class TestNuxeoHealthCheck extends AbstractWebServerTest {

    @Test
    public void testHealthCheck() {
        given().when().get("/health").then().statusCode(200).body("outcome", is("UP")).body("checks.size()", is(3));
    }

    @Override
    public Class<?>[] getResources() {
        return new Class[] { NuxeoHealthCheck.class };
    }
}
