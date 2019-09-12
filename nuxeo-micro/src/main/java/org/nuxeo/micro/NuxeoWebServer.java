package org.nuxeo.micro;

import java.util.Arrays;
import java.util.concurrent.CompletionStage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.nuxeo.micro.helidon.Properties;

import io.helidon.config.Config;
import io.helidon.health.HealthSupport;
import io.helidon.health.checks.HealthChecks;
import io.helidon.media.jsonp.server.JsonSupport;
import io.helidon.metrics.MetricsSupport;
import io.helidon.webserver.Routing;
import io.helidon.webserver.ServerConfiguration;
import io.helidon.webserver.WebServer;
import io.helidon.webserver.jersey.JerseySupport;
import io.swagger.v3.jaxrs2.integration.resources.AcceptHeaderOpenApiResource;
import io.swagger.v3.jaxrs2.integration.resources.OpenApiResource;

public interface NuxeoWebServer extends WebServer {

    static Builder withBundle(String bundle) {
        return new Builder().withBundle(bundle);
    }

    static Builder withComponents(String... components) {
        return new Builder().withComponents(components);
    }

    static Builder empty() {
        return new Builder();
    }

    static Builder fromApplication(NuxeoApplication app) {
        return new Builder(app);
    }

    final class Builder {
        private static final Logger log = LogManager.getLogger(NuxeoWebServer.Builder.class);

        protected final NuxeoApplication app;

        public Builder() {
            this(new NuxeoApplication());
        }

        public Builder(NuxeoApplication app) {
            this.app = app;
            app.onStart();
        }

        public Builder withBundle(String bundle) {
            try {
                app.installBundle(bundle);
                return this;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        public Builder withComponents(String... components) {
            try {
                app.installComponents(components);
                return this;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        protected ServerConfiguration getServerConfiguration() {
            Config config = ((Properties) app.getRuntime().getProperties()).getObject("server");
            return ServerConfiguration.create(config);
        }

        public WebServer create(Class<?>... resources) {
            return create(getServerConfiguration(), resources);
        }

        public WebServer create(ServerConfiguration configuration, Class<?>... resources) {
            return WebServer.create(configuration, createRouting(app, resources));
        }

        public CompletionStage<Void> start(Class<?>... resources) {
            WebServer webServer = create(getServerConfiguration(), resources);
            return start(webServer);
        }

        public CompletionStage<Void> start(WebServer ws) {
            ws.whenShutdown().thenRun(app::onStop);

            return ws.start().thenAccept(w -> {
                log.info("Nuxeo server is up! http://localhost:" + w.port());
                w.whenShutdown().thenRun(() -> log.info("WEB server is DOWN. Good bye!"));
            }).exceptionally(t -> {
                log.fatal("Startup failed: " + t.getMessage(), t);
                return null;
            });
        }

        private static Routing createRouting(NuxeoApplication app, Class<?>... resources) {
            MetricsSupport metrics = MetricsSupport.create();
            HealthSupport health = HealthSupport.builder()
                                                .addLiveness(HealthChecks.healthChecks()) // Adds a convenient set of
                                                                                          // checks
                                                .addReadiness(new NuxeoHealthCheck(app))
                                                .build();

            JerseySupport.Builder jBuilder = JerseySupport.builder();
            Arrays.stream(resources).forEach(jBuilder::register);

            // Register OpenApi resources last, to follow class registration order required.
            jBuilder.register(OpenApiResource.class) //
                    .register(AcceptHeaderOpenApiResource.class);

            return Routing.builder()
                          .register(JsonSupport.create())
                          .register(health) // Health at "/health"
                          .register(metrics) // Metrics at "/metrics"
                          .register(jBuilder.build())
                          .build();
        }
    }
}
