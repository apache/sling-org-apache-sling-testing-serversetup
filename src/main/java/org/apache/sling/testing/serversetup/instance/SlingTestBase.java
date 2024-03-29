/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.sling.testing.serversetup.instance;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.TreeSet;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.apache.sling.testing.clients.ClientException;
import org.apache.sling.testing.clients.SlingClient;
import org.apache.sling.testing.clients.SlingHttpResponse;
import org.apache.sling.testing.clients.osgi.BundlesInstaller;
import org.apache.sling.testing.clients.osgi.OsgiConsoleClient;
import org.apache.sling.testing.clients.util.TimeoutsProvider;
import org.apache.sling.testing.serversetup.jarexec.JarExecutor;
import org.junit.After;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Base class for running tests against a Sling instance,
 *  takes care of starting Sling and waiting for it to be ready.
 */
public class SlingTestBase implements SlingInstance {
    // TODO: unify these
    public static final String TEST_SERVER_URL_PROP = "test.server.url";
    public static final String TEST_SERVER_USERNAME = "test.server.username";
    public static final String TEST_SERVER_PASSWORD = "test.server.password";
    public static final String SERVER_READY_TIMEOUT_PROP = "server.ready.timeout.seconds";
    public static final String SERVER_READY_TIMEOUT_INITIAL_DELAY_PROP = "server.ready.timeout.initial.delay.seconds";
    public static final String SERVER_READY_TIMEOUT_DELAY_PROP = "server.ready.timeout.delay.seconds";
    public static final String SERVER_READY_QUIET_PERIOD_PROP = "server.ready.quiet.period.seconds";
    public static final String SERVER_READY_PROP_PREFIX = "server.ready.path";
    public static final String KEEP_JAR_RUNNING_PROP = "keepJarRunning";
    public static final String SERVER_HOSTNAME_PROP = "test.server.hostname";
    public static final String ADDITONAL_BUNDLES_PATH = "additional.bundles.path";
    public static final String ADDITONAL_BUNDLES_UNINSTALL = "additional.bundles.uninstall";
    public static final String BUNDLE_TO_INSTALL_PREFIX = "sling.additional.bundle";
    public static final String START_BUNDLES_TIMEOUT_SECONDS = "start.bundles.timeout.seconds";
    public static final String BUNDLE_INSTALL_TIMEOUT_SECONDS = "bundle.install.timeout.seconds";
    public static final String ADMIN = "admin";

    private final boolean keepJarRunning;
    private final boolean uninstallAdditionalBundles;
    private final String serverUsername;
    private final String serverPassword;
    private final SlingInstanceState slingTestState;
    private final Properties systemProperties;
    private OsgiConsoleClient osgiConsoleClient;
    private BundlesInstaller bundlesInstaller;
    private boolean serverStartedByThisClass;


    private final Logger log = LoggerFactory.getLogger(getClass());


    public SlingTestBase() {
        this(SlingInstanceState.getInstance(SlingInstanceState.DEFAULT_INSTANCE_NAME), System.getProperties());
    }

    /** Get configuration but do not start server yet, that's done on demand */
    public SlingTestBase(SlingInstanceState slingTestState, Properties systemProperties) {
        this.slingTestState = slingTestState;
        this.systemProperties = systemProperties;
        this.keepJarRunning = "true".equals(systemProperties.getProperty(KEEP_JAR_RUNNING_PROP));

        final String configuredUrl = systemProperties.getProperty(TEST_SERVER_URL_PROP,
                systemProperties.getProperty("launchpad.http.server.url"));
        if(configuredUrl != null && configuredUrl.trim().length() > 0) {
            slingTestState.setServerBaseUrl(configuredUrl);
            slingTestState.setServerStarted(true);
            uninstallAdditionalBundles = "true".equals(systemProperties.getProperty(ADDITONAL_BUNDLES_UNINSTALL));
        } else {
            synchronized(this.slingTestState) {
                try {
                    if(slingTestState.getJarExecutor() == null) {
                        slingTestState.setJarExecutor(new JarExecutor(systemProperties));
                    }
                } catch(Exception e) {
                    log.error("JarExecutor setup failed", e);
                    fail("JarExecutor setup failed: " + e);
                }
            }
            String serverHost = systemProperties.getProperty(SERVER_HOSTNAME_PROP);
            if(serverHost == null || serverHost.trim().length() == 0) {
                serverHost = "localhost";
            }
            slingTestState.setServerBaseUrl("http://" + serverHost + ":" + slingTestState.getJarExecutor().getServerPort());
            uninstallAdditionalBundles = false; // never undeploy additional bundles in case the server is provisioned here!
        }

        // Set configured username using "admin" as default credential
        final String configuredUsername = systemProperties.getProperty(TEST_SERVER_USERNAME);
        if (configuredUsername != null && configuredUsername.trim().length() > 0) {
            serverUsername = configuredUsername;
        } else {
            serverUsername = ADMIN;
        }

        // Set configured password using "admin" as default credential
        final String configuredPassword = systemProperties.getProperty(TEST_SERVER_PASSWORD);
        if (configuredPassword != null && configuredPassword.trim().length() > 0) {
            serverPassword = configuredPassword;
        } else {
            serverPassword = ADMIN;
        }

        // create client
        try {
            osgiConsoleClient = new OsgiConsoleClient(URI.create(slingTestState.getServerBaseUrl()), serverUsername, serverPassword);
        } catch (ClientException e) {
            throw new RuntimeException("Cannot instantiate client", e);
        }

        bundlesInstaller = new BundlesInstaller(osgiConsoleClient);

        if(!slingTestState.isServerInfoLogged()) {
            log.info("Server base URL={}", slingTestState.getServerBaseUrl());
            slingTestState.setServerInfoLogged(true);
        }
    }

    /**
     * Automatically by the SlingRemoteTestRunner since package version 1.1.0.
     */
    @After
    public void uninstallAdditionalBundlesIfNecessary() {
        if (uninstallAdditionalBundles) {
            log.info("Uninstalling additional bundles...");
            uninstallAdditionalBundles();
        }
    }

    /** Start the server, if not done yet */
    private void startServerIfNeeded() {
        try {
            if(slingTestState.isServerStarted() && !serverStartedByThisClass && !slingTestState.isStartupInfoProvided()) {
                log.info(TEST_SERVER_URL_PROP + " was set: not starting server jar (" + slingTestState.getServerBaseUrl() + ")");
            }
            if(!slingTestState.isServerStarted()) {
                synchronized (slingTestState) {
                    if(!slingTestState.isServerStarted()) {
                        slingTestState.getJarExecutor().start();
                        serverStartedByThisClass = true;
                        if(!slingTestState.setServerStarted(true)) {
                            fail("A server is already started at " + slingTestState.getServerBaseUrl());
                        }
                    }
                }
            }
            slingTestState.setStartupInfoProvided(true);
            waitForServerReady();
            installAdditionalBundles();
            waitForQuietPeriod();
            blockIfRequested();
        } catch(Exception e) {
            log.error("Exception in maybeStartServer()", e);
            fail("maybeStartServer() failed: " + e);
        }
    }

    /**
     * Wait for the configured duration as a quite period to let the server settle down after
     * doing the startup and install additional bundles work.
     */
    protected void waitForQuietPeriod() throws InterruptedException {
        if (slingTestState.isQuietPeriodComplete()) {
            // already waited, so no need to do that again
            return;
        }
        final String quietPeriodSecProp = systemProperties.getProperty(SERVER_READY_QUIET_PERIOD_PROP, "0");
        final int quietPeriodSec = TimeoutsProvider.getInstance().getTimeout(Integer.valueOf(quietPeriodSecProp));
        final int quietPeriodMs = quietPeriodSec * 1000;
        if (quietPeriodMs > 0) {
            log.info("Waiting {} seconds as a quiet period", quietPeriodSec);
            Thread.sleep(quietPeriodMs);
        }
        slingTestState.setQuietPeriodComplete(true);
    }

    protected void installAdditionalBundles() {
        if (slingTestState.isInstallBundlesFailed()) {
            fail("Bundles could not be installed, cannot run tests");
        } else if(!slingTestState.isExtraBundlesInstalled()) {
            final List<File> toInstall = getBundlesToInstall();
            if (!toInstall.isEmpty()) {
                try {
                    // Install bundles, check that they are installed and start them all
                    bundlesInstaller.installBundles(toInstall, false);
                    final List<String> symbolicNames = new LinkedList<String>();
                    for (File f : toInstall) {
                        symbolicNames.add(OsgiConsoleClient.getBundleSymbolicName(f));
                    }
                    bundlesInstaller.waitForBundlesInstalled(symbolicNames,
                            TimeoutsProvider.getInstance().getTimeout(BUNDLE_INSTALL_TIMEOUT_SECONDS, 10) * 1000);
                    bundlesInstaller.startAllBundles(symbolicNames,
                            TimeoutsProvider.getInstance().getTimeout(START_BUNDLES_TIMEOUT_SECONDS, 30) * 1000);
                } catch(AssertionError ae) {
                    log.info("Exception while installing additional bundles", ae);
                    slingTestState.setInstallBundlesFailed(true);
                } catch(Exception e) {
                    log.info("Exception while installing additional bundles", e);
                    slingTestState.setInstallBundlesFailed(true);
                }
                if(slingTestState.isInstallBundlesFailed()) {
                    fail("Could not start all installed bundles:" + toInstall);
                }
            } else {
                log.info("Not installing additional bundles, probably System property {} not set",
                        ADDITONAL_BUNDLES_PATH);
            }
        }

        slingTestState.setExtraBundlesInstalled(!slingTestState.isInstallBundlesFailed());
    }
    
    protected void uninstallAdditionalBundles() {
        try {
            // always uninstall independent of installation status
            bundlesInstaller.uninstallBundles(getBundlesToInstall());
        } catch (Exception e) {
             log.info("Exception while uninstalling additional bundles", e);
        }
    }

    /** Start server if needed, and return its base URL */
    public String getServerBaseUrl() {
        startServerIfNeeded();
        return slingTestState.getServerBaseUrl();
    }

    /** Return username configured for execution of HTTP requests */
    public String getServerUsername() {
        return serverUsername;
    }

    /** Return password configured for execution of HTTP requests */
    public String getServerPassword() {
        return serverPassword;
    }

    @Override
    public SlingClient getSlingClient() {
        return osgiConsoleClient;
    }

    public OsgiConsoleClient getOsgiConsoleClient() {
        startServerIfNeeded();
        return osgiConsoleClient;
    }

    /** Optionally block here so that the runnable jar stays up - we can
     *  then run tests against it from another VM.
     */
    protected void blockIfRequested() {
        if (keepJarRunning) {
            log.info(KEEP_JAR_RUNNING_PROP + " set to true - entering infinite loop"
                     + " so that runnable jar stays up. Kill this process to exit.");
            synchronized (slingTestState) {
                try {
                    slingTestState.wait();
                } catch(InterruptedException iex) {
                    log.info("InterruptedException in blockIfRequested");
                }
            }
        }
    }

    /** Check a number of server URLs for readyness */
    protected void waitForServerReady() throws Exception {
        if(slingTestState.isServerReady()) {
            return;
        }
        if(slingTestState.isServerReadyTestFailed()) {
            fail("Server is not ready according to previous tests");
        }

        // Timeout for readiness test
        TimeoutsProvider tp = TimeoutsProvider.getInstance();
        final String sec = systemProperties.getProperty(SERVER_READY_TIMEOUT_PROP, "60");
        final int timeoutSec = tp.getTimeout(Integer.valueOf(sec));

        final String initialDelaySec = systemProperties.getProperty(SERVER_READY_TIMEOUT_INITIAL_DELAY_PROP, "0");
        final int timeoutInitialDelaySec = tp.getTimeout(Integer.valueOf(initialDelaySec));
        final int timeoutInitialDelayMs = timeoutInitialDelaySec * 1000;

        final String delaySec = systemProperties.getProperty(SERVER_READY_TIMEOUT_DELAY_PROP, "1");
        final int timeoutDelaySec = tp.getTimeout(Integer.valueOf(delaySec));
        final int timeoutDelayMs = timeoutDelaySec * 1000;

        log.info("Will wait up to {} seconds for server to become ready with a {} second initial delay and {} seconds between each check",
                new Object[] {timeoutSec, timeoutInitialDelaySec, timeoutDelaySec});
        final long endTime = System.currentTimeMillis() + timeoutSec * 1000L;

        // Get the list of paths to test and expected content regexps
        final List<String> testPaths = new ArrayList<String>();
        final TreeSet<Object> propertyNames = new TreeSet<Object>();
        propertyNames.addAll(systemProperties.keySet());
        for(Object o : propertyNames) {
            final String key = (String)o;
            if(key.startsWith(SERVER_READY_PROP_PREFIX)) {
                testPaths.add(systemProperties.getProperty(key));
            }
        }

        if (timeoutInitialDelayMs > 0) {
            // wait for the initial deal duration
            Thread.sleep(timeoutInitialDelayMs);
        }

        // Consider the server ready if it responds to a GET on each of
        // our configured request paths with a 200 result and content
        // that contains the pattern that's optionally supplied with the
        // path, separated by a colon
        log.info("Checking that GET requests return expected content (timeout={} seconds): {}", timeoutSec, testPaths);
        while (System.currentTimeMillis() < endTime) {
            boolean errors = false;
            for (String p : testPaths) {
                final String [] s = p.split(":");
                final String path = s[0];
                final String pattern = (s.length > 0 ? s[1] : "");
                boolean isRegex = s.length > 1 ? "regexp".equals(s[2]) : false;
                try {
                    URI uri = new URI(path);
                    List<NameValuePair> reqParams = extractParams(uri);
                    SlingHttpResponse get = osgiConsoleClient.doGet(uri.getPath(), reqParams, 200);
                    if (isRegex) {
                        get.checkContentRegexp(pattern);
                    } else {
                        get.checkContentContains(pattern);
                    }
                } catch(ClientException e) {
                    errors = true;
                    log.debug("Request to {}@{} failed, will retry ({})",
                            new Object[] { serverUsername, osgiConsoleClient.getUrl(path), e});
                } catch(Exception e) {
                    errors = true;
                    log.debug("Request to {}@{} failed, will retry ({})",
                            new Object[] { serverUsername, osgiConsoleClient.getUrl(path), pattern, e });
                }
            }

            if (!errors) {
                slingTestState.setServerReady(true);
                log.info("All {} paths return expected content, server ready", testPaths.size());
                break;
            }
            Thread.sleep(timeoutDelayMs);
        }

        if (!slingTestState.isServerReady()) {
            slingTestState.setServerReadyTestFailed(true);
            final String msg = "Server not ready after " + timeoutSec + " seconds, giving up";
            log.info(msg);
            fail(msg);
        }
    }

    /**
     * Convert the query part of the URI to a list of name value pairs that are suitable
     * for the client calls
     */
    protected List<NameValuePair> extractParams(URI url) throws UnsupportedEncodingException {
        final List<NameValuePair> paramsList = new ArrayList<>();
        String query = url.getQuery();
        if (query == null || query.isEmpty()) {
            return null;
        }
        final String[] pairs = query.split("&");
        for (String pair : pairs) {
            final int idx = pair.indexOf("=");
            final String key = idx > 0 ? URLDecoder.decode(pair.substring(0, idx), "UTF-8") : pair;
            final String value = idx > 0 && pair.length() > idx + 1 ? URLDecoder.decode(pair.substring(idx + 1), "UTF-8") : null;
            paramsList.add(new BasicNameValuePair(key, value));
        }
        return paramsList;
    }

    /**
     * Get the list of additional bundles to install, as specified by the system property {@link #ADDITONAL_BUNDLES_PATH} 
     * @return the list of {@link File}s pointing to the Bundle JARs or the empty list in case no additional bundles should be installed (never {@code null}).
     */
    protected List<File> getBundlesToInstall() {
        final String paths = systemProperties.getProperty(ADDITONAL_BUNDLES_PATH);
        if(paths == null) {
            return Collections.emptyList();
        } 
        
        final List<File> toInstall = new ArrayList<File>();
        // Paths can contain a comma-separated list
        final String [] allPaths = paths.split(",");
        for(String path : allPaths) {
            toInstall.addAll(getBundlesToInstall(path.trim()));
        }
        return toInstall;
    }

    /**
     * Get the list of additional bundles to install, as specified by additionalBundlesPath parameter
     */
    protected List<File> getBundlesToInstall(String additionalBundlesPath) {
        final List<File> result = new LinkedList<File>();
        if(additionalBundlesPath == null) {
            return result;
        }

        final File dir = new File(additionalBundlesPath);
        if(!dir.isDirectory() || !dir.canRead()) {
            log.info("Cannot read additional bundles directory {}, ignored", dir.getAbsolutePath());
            return result;
        }

        // Collect all filenames of candidate bundles
        final List<String> bundleNames = new ArrayList<String>();
        final String [] files = dir.list();
        if (files != null) {
            for(String file : files) {
                if(file.endsWith(".jar")) {
                    bundleNames.add(file);
                }
            }
        }

        // We'll install those that are specified by system properties, in order
        final List<String> sortedPropertyKeys = new ArrayList<String>();
        for(Object key : systemProperties.keySet()) {
            final String str = key.toString();
            if(str.startsWith(BUNDLE_TO_INSTALL_PREFIX)) {
                sortedPropertyKeys.add(str);
            }
        }
        Collections.sort(sortedPropertyKeys);
        for (String key : sortedPropertyKeys) {
            final String filenamePrefix = systemProperties.getProperty(key);
            for(String bundleFilename : bundleNames) {
                if(bundleFilename.startsWith(filenamePrefix)) {
                    result.add(new File(dir, bundleFilename));
                }
            }
        }

        return result;
    }

    public boolean isServerStartedByThisClass() {
        return serverStartedByThisClass;
    }

}
