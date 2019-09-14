/*
 * (C) Copyright 2019 Nuxeo SA (http://nuxeo.com/).
 * This is unpublished proprietary source code of Nuxeo SA. All rights reserved.
 * Notice of copyright on this source code does not indicate publication.
 *
 * Contributors:
 *     Gildas Lefevre
 */
package org.nuxeo.micro;

import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Liveness;
import org.nuxeo.runtime.management.api.ProbeStatus;
import org.nuxeo.runtime.stream.StreamProbe;

/**
 * Endpoint for the liveness HTTP request.
 */
@Liveness
public class NuxeoHealthCheck implements HealthCheck {

    private final NuxeoApplication app;

    private final StreamProbe streamProbe;

    public NuxeoHealthCheck() {
        this(null);
    }

    public NuxeoHealthCheck(NuxeoApplication app) {
        this.app = app;
        streamProbe = new StreamProbe();
    }

    @Override
    public HealthCheckResponse call() {
        if (app == null && !app.isUp()) {
            return HealthCheckResponse.named("nuxeoHealth").down().build();
        }
        ProbeStatus status = streamProbe.run();
        if (status.isSuccess()) {
            return HealthCheckResponse.named("nuxeoHealth").up().withData("stream", status.getAsString()).build();
        } else {
            return HealthCheckResponse.named("nuxeoHealth").down().withData("stream", status.getAsString()).build();
        }
    }
}
