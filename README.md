[![Apache Sling](https://sling.apache.org/res/logos/sling.png)](https://sling.apache.org)

&#32;[![Build Status](https://ci-builds.apache.org/job/Sling/job/modules/job/sling-org-apache-sling-testing-serversetup/job/master/badge/icon)](https://ci-builds.apache.org/job/Sling/job/modules/job/sling-org-apache-sling-testing-serversetup/job/master/)&#32;[![Test Status](https://img.shields.io/jenkins/tests.svg?jobUrl=https://ci-builds.apache.org/job/Sling/job/modules/job/sling-org-apache-sling-testing-serversetup/job/master/)](https://ci-builds.apache.org/job/Sling/job/modules/job/sling-org-apache-sling-testing-serversetup/job/master/test/?width=800&height=600)&#32;[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=apache_sling-org-apache-sling-testing-serversetup&metric=coverage)](https://sonarcloud.io/dashboard?id=apache_sling-org-apache-sling-testing-serversetup)&#32;[![Sonarcloud Status](https://sonarcloud.io/api/project_badges/measure?project=apache_sling-org-apache-sling-testing-serversetup&metric=alert_status)](https://sonarcloud.io/dashboard?id=apache_sling-org-apache-sling-testing-serversetup)&#32;[![JavaDoc](https://www.javadoc.io/badge/org.apache.sling/org.apache.sling.testing.serversetup.svg)](https://www.javadoc.io/doc/org.apache.sling/org-apache-sling-testing-serversetup)&#32;[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.apache.sling/org.apache.sling.testing.serversetup/badge.svg)](https://search.maven.org/#search%7Cga%7C1%7Cg%3A%22org.apache.sling%22%20a%3A%22org.apache.sling.testing.serversetup%22)&#32;[![testing](https://sling.apache.org/badges/group-testing.svg)](https://github.com/apache/sling-aggregator/blob/master/docs/group/testing.md) [![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://www.apache.org/licenses/LICENSE-2.0)

# Apache Sling Server Setup Tools

This module is part of the [Apache Sling](https://sling.apache.org) project.

Sling Server Setup utilities.


### Configuration System Properties

* __*test.server.url*__  - If the server is already running, this is the full address of your target server. No default value.
* __*test.server.hostname*__ - The hostname for the sling server that will be started.  Default value is "localhost".
* __*test.server.username*__ - The username to use to login for interacting with the OSGi web console.  Default value is "admin".
* __*test.server.password*__  - The password of the user to use for interacting with the OSGi web console.  Default value is "admin".

* __*server.ready.timeout.seconds*__ - The maximum amount of time in seconds to wait for the server to become ready.  Default value is 60. 
* __*server.ready.timeout.initial.delay.seconds*__ - The duration to wait in seconds before checking if the server is ready.  Default value is 0. 
* __*server.ready.timeout.delay.seconds*__ - The duration of the delay in seconds between attempts to check if the server is ready.  Defalt value is 1. 
* __*server.ready.quiet.period.seconds*__ - The duration of a quiet period in seconds after the server has become ready and the tests begin running.  Default value is 0. 
* __*server.ready.path*__ - Prefix for one or more properties whose value describe how to check if the server is ready. The syntax of each value is one of:
     | Pattern | Description |
     | ------- | ----------- |
     | [relative_url]:[response_content_contains] | Load the page and check if the response content contains the string | 
     | [relative_url]:[response_content_pattern]:regexp | Load the page and check if the response content contains the regex pattern | 
* __*keepJarRunning*__ - Specify if you want the server to remain running - you can then run tests against it from another VM.  Default value is false.

* __*additional.bundles.path*__ - The value is a comma-separated list of additional bundles to install (or uninstall).
* __*additional.bundles.uninstall*__ - If true, treat the additional bundles items as bundles to uninstall.  Otherwise, treat those items as bundles to install.  Default value is false.
* __*sling.additional.bundle*__ - Prefix for zero or more properties whose value describes additional bundles to install.
* __*start.bundles.timeout.seconds*__ - The maximum amount of time to wait for the additional bundles to start.  Default value is 30. 
* __*bundle.install.timeout.seconds*__ - The maximum amount of time to wait for the additional bundles to install.  Default value is 10. 

* __*jar.executor.server.port*__ - The port number for the sling server that will be started.  Default value is 8765.
* __*jar.executor.jar.folder*__ - The folder that contains the executable jar. 
* __*jar.executor.jar.name.regexp*__ - The name of the executable jar file. 
* __*jar.executor.vm.options*__ - Additional options to pass along to the spawned jvm. 
* __*jar.executor.work.folder*__ - The working directory for the executable jar. 
* __*jar.executor.exit.timeout.seconds*__ - The maximum time in seconds to wait for for the executor process to exit normally.  Default value is 30. 
* __*jar.executor.wait.on.shutdown*__ - True to wait for the executor process to exit normally, false to not wait.  Default value is false. 
* __*jar.executor.java.executable.path*__ - The path to the java executable. 
* __*jar.executor.synchronous.exec*__ - True to execute synchronous, false otherwise.  Default value is false. 
* __*jar.executor.synchronous.exec.expected.result*__ - The exit code expected from the executor.  Default value is 0. 
