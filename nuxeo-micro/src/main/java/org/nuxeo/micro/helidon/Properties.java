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

import java.util.Objects;

import org.nuxeo.common.codec.Crypto;
import org.nuxeo.common.codec.CryptoProperties;

import io.helidon.config.Config;
import io.helidon.config.ConfigSources;

/**
 * Helidon Config based Properties class that relies as well on Nuxeo Crypto based Property. Resolution order is:
 * <ul>
 * <li>Nuxeo's CryptoProperties</li>
 * </ul>
 */
public class Properties extends CryptoProperties {

    protected Config config;

    @Override
    public String getProperty(String key, boolean raw) {
        Config configValue = getConfig().get(key);
        if (configValue.exists()) {
            String rawValue = configValue.asString().orElse(null);
            if (Crypto.isEncrypted(rawValue) && !raw) {
                return new String(getCrypto().decrypt(rawValue));
            } else {
                return rawValue;
            }
        }
        return super.getProperty(key, raw);
    }

    private Config getConfig() {
        if (config == null) {
            config = Config.create(ConfigSources.classpath("application.yaml"));
        }
        return config;
    }

    public Config getObject(String key) {
        return getConfig().get(key);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        if (!super.equals(o))
            return false;
        Properties that = (Properties) o;
        return Objects.equals(config, that.config);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), config);
    }
}
