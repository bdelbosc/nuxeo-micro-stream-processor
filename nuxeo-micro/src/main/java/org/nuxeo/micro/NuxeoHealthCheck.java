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

/**
 * Endpoint for the liveness HTTP request.
 */
@Liveness
public class NuxeoHealthCheck implements HealthCheck {

    @Override
    public HealthCheckResponse call() {
        // TODO: check access to redis and stream services
        return HealthCheckResponse.named("Health check nuxeo container").up().build();
    }
}
