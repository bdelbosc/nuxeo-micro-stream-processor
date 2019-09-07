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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import java.util.Properties;

import org.junit.jupiter.api.Test;
import org.nuxeo.common.Environment;
import org.nuxeo.common.codec.CryptoProperties;
import org.nuxeo.micro.helidon.junit.ConfigProperty;
import org.nuxeo.micro.helidon.junit.Deploy;
import org.nuxeo.micro.helidon.junit.NuxeoHelidonTest;
import org.nuxeo.micro.helidon.junit.NuxeoTestExtension;
import org.nuxeo.runtime.RuntimeService;
import org.nuxeo.runtime.api.Framework;
import org.nuxeo.runtime.kafka.KafkaConfigService;

@NuxeoHelidonTest
public class EmbeddedNuxeoRuntimeTest {

    @Test
    public void shouldResolveEnvVariableAsProperties() {
        Environment defEnv = Environment.getDefault();
        assertThat(defEnv).isInstanceOf(HelidonEnvironment.class);

        String envVar = "java.home";
        CryptoProperties properties = (CryptoProperties) defEnv.getProperties();
        assertThat(properties.getProperty(envVar)).isNotNull();
        assertThat(properties.getProperty(envVar, true)).isNotNull();
    }

    @Test
    @Deploy("test-contrib.xml")
    public void shouldDeployBundleWithAnnotation() {
        KafkaConfigService service = Framework.getService(KafkaConfigService.class);

        Properties defaultConfig = service.getConsumerProperties("default");
        assertThat(defaultConfig.getProperty("max.poll.records")).isEqualTo("1337");
        assertThat(defaultConfig.getProperty("session.timeout.ms")).isEqualTo("${nuxeo.test.kafka.session.timeout.ms}");
    }

    @Test
    @Deploy("test-contrib.xml")
    @ConfigProperty(key = "nuxeo.test.kafka.servers", value = "localhost")
    public void shouldExpandConfig() {
        KafkaConfigService service = Framework.getService(KafkaConfigService.class);
        assertThat(service).isNotNull();

        assertThat(Framework.getProperty("nuxeo.test.kafka.servers")).isEqualTo("localhost");

        assertThat(Framework.getProperties()).isInstanceOf(org.nuxeo.micro.helidon.Properties.class);

        Properties defaultConfig = service.getConsumerProperties("default");
        assertThat(defaultConfig.getProperty("bootstrap.servers")).isEqualTo("localhost-titi");
    }

    @Test
    public void testNuxeoDirs() {
        assertThat(Environment.getDefault().getLog()).hasName("log");
    }

    @Test
    public void testInstallComponent() throws Exception {
        RuntimeService runtime = NuxeoTestExtension.APP.getRuntime();
        assertThat(runtime.getComponent("org.nuxeo.runtime.stream.kafka.service.test")).isNull();

        NuxeoTestExtension.APP.installComponents("test-contrib.xml");
        assertThat(runtime.getComponent("org.nuxeo.runtime.stream.kafka.service.test")).isNotNull();
    }

    @Test
    public void testMissingComponent() throws Exception {
        try {
            NuxeoTestExtension.APP.installComponents("missing-test-contrib.xml");
            fail("Should have raised an exception for missing contrib!");
        } catch (IllegalArgumentException iea) {
            assertThat(iea.getMessage()).contains("missing-test-contrib.xml");
        }
    }
}
