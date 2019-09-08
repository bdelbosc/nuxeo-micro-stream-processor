package org.nuxeo.micro.acme.processor;

import org.nuxeo.micro.NuxeoWebServer;

public class Main {
    /**
     * Application main entry point.
     *
     * @param args command line arguments.
     */
    public static void main(final String[] args) {
        NuxeoWebServer.withComponents("OSGI-INF/stream-config.xml").start();
    }
}
