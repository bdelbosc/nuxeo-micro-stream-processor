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
import org.nuxeo.runtime.util.SimpleRuntime;

public class HelidonRuntime extends SimpleRuntime {

    public HelidonRuntime() {
        super();
        this.properties = (CryptoProperties) Environment.getDefault().getProperties();
    }
}
