/*
 * (C) Copyright 2019 Nuxeo (http://nuxeo.com/).
 * This is unpublished proprietary source code of Nuxeo SA. All rights reserved.
 * Notice of copyright on this source code does not indicate publication.
 *
 * Contributors:
 *     Nuxeo
 *
 */
package org.nuxeo.micro.helidon;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.nuxeo.micro.helidon.junit.NuxeoTestExtension;

import io.helidon.webserver.ServerConfiguration;
import io.helidon.webserver.WebServer;
import io.restassured.RestAssured;

public abstract class AbstractWebServerTest {
    protected WebServer ws;

    protected static final int RETRIES = 1000;

    private static final Log log = LogFactory.getLog(AbstractWebServerTest.class);

    @BeforeEach
    public void beforeEach() throws ExecutionException, InterruptedException, TimeoutException {
        int port = findFreePort();
        ServerConfiguration configuration = ServerConfiguration.builder().port(port).build();
        ws = NuxeoWebServer.fromApplication(NuxeoTestExtension.APP)
                           .create(configuration, getResources())
                           .start()
                           .toCompletableFuture()
                           .get(5, TimeUnit.SECONDS);
        RestAssured.port = port;
    }

    @AfterEach
    public void afterEach() throws InterruptedException, ExecutionException, TimeoutException {
        ws.shutdown().toCompletableFuture().get(5, TimeUnit.SECONDS);
    }

    public abstract Class<?>[] getResources();

    protected int findFreePort() {
        for (int i = 0; i < RETRIES; i++) {
            try (ServerSocket socket = new ServerSocket(0)) {
                socket.setReuseAddress(true);
                return socket.getLocalPort();
            } catch (IOException e) {
                log.trace("Failed to allocate port", e);
            }
        }
        throw new RuntimeException("Unable to find free port after " + RETRIES + " retries");
    }
}
