[![Apache Sling](https://sling.apache.org/res/logos/sling.png)](https://sling.apache.org)

&#32;[![Build Status](https://ci-builds.apache.org/job/Sling/job/modules/job/sling-org-apache-sling-testing-serversetup/job/master/badge/icon)](https://ci-builds.apache.org/job/Sling/job/modules/job/sling-org-apache-sling-testing-serversetup/job/master/)&#32;[![Test Status](https://img.shields.io/jenkins/tests.svg?jobUrl=https://ci-builds.apache.org/job/Sling/job/modules/job/sling-org-apache-sling-testing-serversetup/job/master/)](https://ci-builds.apache.org/job/Sling/job/modules/job/sling-org-apache-sling-testing-serversetup/job/master/test/?width=800&height=600)&#32;[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=apache_sling-org-apache-sling-testing-serversetup&metric=coverage)](https://sonarcloud.io/dashboard?id=apache_sling-org-apache-sling-testing-serversetup)&#32;[![Sonarcloud Status](https://sonarcloud.io/api/project_badges/measure?project=apache_sling-org-apache-sling-testing-serversetup&metric=alert_status)](https://sonarcloud.io/dashboard?id=apache_sling-org-apache-sling-testing-serversetup)&#32;[![JavaDoc](https://www.javadoc.io/badge/org.apache.sling/org.apache.sling.testing.serversetup.svg)](https://www.javadoc.io/doc/org.apache.sling/org-apache-sling-testing-serversetup)&#32;[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.apache.sling/org.apache.sling.testing.serversetup/badge.svg)](https://search.maven.org/#search%7Cga%7C1%7Cg%3A%22org.apache.sling%22%20a%3A%22org.apache.sling.testing.serversetup%22)&#32;[![testing](https://sling.apache.org/badges/group-testing.svg)](https://github.com/apache/sling-aggregator/blob/master/docs/group/testing.md) [![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://www.apache.org/licenses/LICENSE-2.0)

# Apache Sling Server Setup Tools

This module is part of the [Apache Sling](https://sling.apache.org) project.

Sling Server Setup utilities.


### Related System Properties

| Property Name  | Default  | Purpose |
|---|:---:|---|
| test.server.url |   | If the server is already running, this is the full address of your target server.  |
| test.server.hostname | localhost | The hostname for the sling server that will be started  |
| test.server.username  | admin  | The username to use to login for interacting with the OSGi Web Console  |
| test.server.password  | admin  | The password of the user to use for interacting with the OSGi Web Console  |
|  |  |  |
| server.ready.timeout.seconds | 60 | The maximum amount of time in seconds to wait for the server to become ready  |
| server.ready.timeout.initial.delay.seconds | 0 | The duration to wait in seconds before checking if the server is ready  |
| server.ready.timeout.delay.seconds | 1 | The duration of the delay in seconds between attempts to check if the server is ready |
| server.ready.quiet.period.seconds | 0 | The duration of a quiet period in seconds after the server has become ready and the tests begin running |
| server.ready.path.* |   | Prefix for properties whose value describe how to check if the server is ready. <br/>The syntax of each value is one of: <table><thead><tr><th>Pattern</th><th>Description</th></tr></thead><tbody><tr><td>[relative_url]:[response_content_contains]</td><td>Load the page and check if the response contains the content string</td></tr><tr><td>[relative_url]:[response_content_pattern]:regexp</td><td>Load the page and check if the response contains the regex pattern</td></tr></tbody></table>   |
| keepJarRunning | false  | Set to true if you want the server to remain running - you can then run tests against it from another VM. |
|  |  |  |
| additional.bundles.path |   | The value is a comma-separated list of additional bundles to install (or uninstall) |
| additional.bundles.uninstall | false | <table><thead><tr><th>Value</th><th>Description</th></tr></thead><tbody><tr><td>true</td><td>uninstall the additional bundles</td></tr><tr><td>false</td><td>install the additional bundles</td></tr></tbody></table> |
| sling.additional.bundle.* |   | Prefix for properties whose value describes additional bundles to install |
| start.bundles.timeout.seconds | 30 | The maximum amount of tiem to wait for the additional bundles to start |
| bundle.install.timeout.seconds | 10 | The maximum amount of tiem to wait for the additional bundles to install  |
|  |  |  |
| jar.executor.server.port | 8765 | The port number for the sling server that will be started |
| jar.executor.jar.folder |  | The folder that contains the executable jar |
| jar.executor.jar.name.regexp |  | The name of the executable jar file |
| jar.executor.vm.options |  | Additional options to pass along to the jvm |
| jar.executor.work.folder |  | The working directory for the executable jar |
| jar.executor.jar.options |  | Additional options for the jar execute command line |
| jar.executor.exit.timeout.seconds | 30 | The maximum time in seconds to wait for for the executor process to exit |
| jar.executor.wait.on.shutdown | false | True to wait for the executor process to exit normally, false to not wait |
| jar.executor.java.executable.path |   | The path to the java executable |
| jar.executor.synchronous.exec |  | True to execute synchronous, false otherwise |
| jar.executor.synchronous.exec.expected.result | 0 | The exit code expected from the executor |
