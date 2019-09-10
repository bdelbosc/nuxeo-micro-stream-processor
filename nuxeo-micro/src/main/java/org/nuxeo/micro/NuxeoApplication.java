/*
 * (C) Copyright 2019 Nuxeo (http://nuxeo.com/).
 * This is unpublished proprietary source code of Nuxeo SA. All rights reserved.
 * Notice of copyright on this source code does not indicate publication.
 *
 * Contributors:
 *     Nuxeo
 *
 */

package org.nuxeo.micro;

import static java.util.stream.Collectors.joining;
import static org.nuxeo.runtime.osgi.OSGiRuntimeService.getComponentsList;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.common.Environment;
import org.nuxeo.micro.helidon.HelidonEnvironment;
import org.nuxeo.micro.helidon.HelidonRuntime;
import org.nuxeo.osgi.BundleFile;
import org.nuxeo.osgi.DirectoryBundleFile;
import org.nuxeo.osgi.JarBundleFile;
import org.nuxeo.osgi.OSGiAdapter;
import org.nuxeo.osgi.application.StandaloneBundleLoader;
import org.nuxeo.runtime.RuntimeService;
import org.nuxeo.runtime.RuntimeServiceException;
import org.nuxeo.runtime.api.Framework;
import org.nuxeo.runtime.util.SimpleRuntime;
import org.osgi.framework.Bundle;

import io.github.lukehutch.fastclasspathscanner.FastClasspathScanner;

public class NuxeoApplication {
    private static final Log log = LogFactory.getLog(NuxeoApplication.class);

    protected Set<URI> readUris;

    protected URL[] urls; // classpath urls, used for bundles lookup

    protected Map<String, BundleFile> bundles;

    protected final List<String> delayedBundles = new ArrayList<>();

    protected final List<URL> delayedComponents = new ArrayList<>();

    private StandaloneBundleLoader bundleLoader;

    private SimpleRuntime runtime;

    private boolean isUp;

    protected static URL[] introspectClasspath() {
        return new FastClasspathScanner().getUniqueClasspathElements().stream().map(file -> {
            try {
                return file.toURI().toURL();
            } catch (MalformedURLException cause) {
                throw new RuntimeServiceException("Could not get URL from " + file, cause);
            }
        }).toArray(URL[]::new);
    }

    public NuxeoApplication() {
        initialize();
    }

    public void onStart() {
        try {
            log.info("Starting Nuxeo Runtime init...");

            // Manual install bundles
            installBundle("org.nuxeo.runtime.stream");
            installBundle("org.nuxeo.runtime.kv");
            installBundle("org.nuxeo.runtime.redis");
            installBundle("org.nuxeo.ecm.core.redis");

            if (Boolean.parseBoolean(Framework.getProperty("kafka.enabled"))) {
                installComponents("OSGI-INF/default-kafka-config.xml");
            } else {
                installComponents("OSGI-INF/default-chronicle-config.xml");
            }
            installComponents("OSGI-INF/default-redis-config.xml");

            // Install local bundles
            // installComponents("OSGI-INF/default-stream-config.xml");

            // Install delayed bundles
            if (!delayedBundles.isEmpty()) {
                for (String delayedBundle : delayedBundles) {
                    installBundle(delayedBundle);
                }
            }

            // Install delayed components
            if (!delayedComponents.isEmpty()) {
                installComponents(delayedComponents);
            }

            start();
            log.info("Nuxeo Runtime initialized");
            isUp = true;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected void start() {
        runtime.getComponentManager().start();
    }

    public RuntimeService getRuntime() {
        return runtime;
    }

    public void onStop() {
        try {
            isUp = false;
            log.info("The application is stopping...");
            shutdown();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void shutdown() throws InterruptedException {
        Framework.shutdown();
    }

    public void initialize() {
        Environment env = new HelidonEnvironment();
        Environment.setDefault(env);
        env.init();

        initUrls();

        bundleLoader = new StandaloneBundleLoader(new OSGiAdapter(env.getHome()), this.getClass().getClassLoader());
        runtime = new HelidonRuntime(this);

        Framework.initialize(runtime);
    }

    protected void initUrls() {
        log.info("Start classpath scanning");
        urls = introspectClasspath();
        log.info("Stop classpath scanning");
        if (log.isDebugEnabled()) {
            log.debug("URLs on the classpath:\n" + Stream.of(urls).map(URL::toString).collect(joining("\n")));
        }
        readUris = new HashSet<>();
        bundles = new HashMap<>();
    }

    protected BundleFile lookupBundle(String bundleName) throws Exception {
        BundleFile bundleFile = bundles.get(bundleName);
        if (bundleFile != null) {
            return bundleFile;
        }
        for (URL url : urls) {
            URI uri = url.toURI();
            if (readUris.contains(uri)) {
                continue;
            }
            File file = new File(uri);
            readUris.add(uri);
            try {
                if (file.isDirectory()) {
                    bundleFile = new DirectoryBundleFile(file);
                } else {
                    bundleFile = new JarBundleFile(file);
                }
            } catch (IOException e) {
                // no manifest => not a bundle
                continue;
            }
            String symbolicName = readSymbolicName(bundleFile);
            System.out.println(bundleFile + " " + symbolicName);
            if (symbolicName != null) {
                log.debug(String.format("Bundle '%s' has URL %s", symbolicName, url));
                bundles.put(symbolicName, bundleFile);
            }
            if (bundleName.equals(symbolicName)) {
                return bundleFile;
            }
        }
        throw new RuntimeServiceException(String.format("No bundle with symbolic name '%s';", bundleName));
    }

    protected String readSymbolicName(BundleFile bf) {
        Manifest manifest = bf.getManifest();
        if (manifest == null) {
            return null;
        }
        Attributes attrs = manifest.getMainAttributes();
        String name = attrs.getValue("Bundle-SymbolicName");
        if (name == null) {
            return null;
        }
        if (StringUtils.isBlank(attrs.getValue("Nuxeo-Component")) && !name.startsWith("org.nuxeo")) {
            // Ignore non Nuxeo components
            return null;
        }
        String[] sp = name.split(";", 2);
        return sp[0];
    }

    public void installBundle(String name) throws Exception {
        // Order is not atomic, need to handle module init before App
        if (bundleLoader == null) {
            delayedBundles.add(name);
            return;
        }
        Bundle bundle = getBundle(name);
        loadComponents(bundle);
    }

    public Bundle getBundle(String name) throws Exception {
        Bundle bundle = bundleLoader.getOSGi().getRegistry().getBundle(name);
        if (bundle == null) {
            BundleFile bundleFile = lookupBundle(name);
            bundleLoader.loadBundle(bundleFile);
            bundleLoader.installBundle(bundleFile);
            bundle = bundleLoader.getOSGi().getRegistry().getBundle(name);
        } else {
            log.info(String.format("A bundle with name %s has been found. Deploy is ignored.", name));
        }
        return bundle;
    }

    public void installComponents(String... locations) throws Exception {
        ClassLoader ccl = Thread.currentThread().getContextClassLoader();
        List<URL> localUrls = Arrays.stream(locations).map(ccl::getResource).collect(Collectors.toList());

        List<URL> nulls = localUrls.stream().filter(Objects::isNull).collect(Collectors.toList());
        if (!nulls.isEmpty()) {
            throw new IllegalArgumentException("Missing resources found in: " + StringUtils.join(locations, ", "));
        }
        installComponents(localUrls);
    }

    public void installComponents(List<URL> urls) throws IOException {
        installComponents(urls.toArray(new URL[0]));
    }

    public void installComponents(URL... urls) throws IOException {
        if (runtime == null) {
            Collections.addAll(delayedComponents, urls);
            return;
        }

        for (URL url : urls) {
            runtime.getContext().deploy(url);
        }
        runtime.getComponentManager().refresh();
    }

    protected void loadComponents(Bundle bundle) {
        String list = getComponentsList(bundle);
        String name = bundle.getSymbolicName();
        if (list == null) {
            log.debug(String.format("Bundle %s doesn't have components", name));
            return;
        }
        log.trace(String.format("Load Bundle: %s / Components: %s", name, list));
        StringTokenizer tok = new StringTokenizer(list, ", \t\n\r\f");
        while (tok.hasMoreTokens()) {
            String path = tok.nextToken();
            URL url = bundle.getEntry(path);
            if (url != null) {
                log.trace(String.format("Load component %s [%s]", name, url));
                try {
                    installComponents(url);
                } catch (IOException e) {
                    // just log error to know where is the cause of the exception
                    log.error(String.format("Error deploying resource: %s", url));
                    throw new RuntimeServiceException("Cannot deploy: " + url, e);
                }
            } else {
                String message = "Unknown component '" + path + "' referenced by bundle '" + name + "'";
                log.error(message + ". Check the MANIFEST.MF");
            }
        }
    }

    public boolean isUp() {
        return isUp;
    }

}
