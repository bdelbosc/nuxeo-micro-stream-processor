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

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import org.nuxeo.common.Environment;

import io.helidon.config.Config;

public class HelidonEnvironment extends Environment {

    public HelidonEnvironment() {
        super(new File(getEnvHome()));
        setServerHome(this.getHome());
        reloadProperties();
    }

    private static String getEnvHome() {
        return Config.create().get(Environment.NUXEO_HOME).asString().orElse(System.getProperty("java.io.tmpdir"));
    }

    public void reloadProperties() {
        // XXX Using reflection to override default properties attribute
        try {
            Field properties = Environment.class.getDeclaredField("properties");
            properties.setAccessible(true);

            Field modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
            modifiersField.setInt(properties, properties.getModifiers() & ~Modifier.FINAL);

            properties.set(this, new Properties());
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Unable to initialize Quarkus Env", e);
        }
    }
}
