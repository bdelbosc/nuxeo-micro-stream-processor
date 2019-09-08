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

import org.nuxeo.common.Environment;
import org.nuxeo.common.codec.CryptoProperties;
import org.nuxeo.micro.NuxeoApplication;
import org.nuxeo.runtime.util.SimpleRuntime;
import org.osgi.framework.Bundle;

public class HelidonRuntime extends SimpleRuntime {

    private final NuxeoApplication app;

    public HelidonRuntime(NuxeoApplication app) {
        super();
        this.properties = (CryptoProperties) Environment.getDefault().getProperties();
        this.app = app;
    }

    @Override
    public Bundle getBundle(String bundleName) {
        // some module need this to access their resources
        try {
            return app.getBundle(bundleName);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
