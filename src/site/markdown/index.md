<!--
Copyright 2015 JUnit Helper Contributors

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
-->

# JUnit Helper Tools

This project started as a collection of JUnit 4 tools / resources that I have created / used over the years for testing
various personal and open source projects and scenarios. The modules in this project have been cleaned up a bit, made a
little more configurable and reusable (hopefully).

## Source Code, Issue Tracker, Binary Distribution etc.

[![Build Status](https://travis-ci.org/deventropy/junit-helper.svg?branch=master "JUnit Helper master Build Status")](https://travis-ci.org/deventropy/junit-helper)
[![Coverage Status](https://coveralls.io/repos/deventropy/junit-helper/badge.svg?branch=master&service=github "JUnit Helper master Coverage Report")](https://coveralls.io/github/deventropy/junit-helper?branch=master)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.deventropy.junit-helper/junit-helper/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.deventropy.junit-helper/junit-helper)

The [Development Entropy](http://www.deventropy.org/) [Junit Helper](./) project is hosted on
GitHub. See the [GitHub project](http://github.com/deventropy/junit-helper) for source code,
[issue tracker](https://github.com/deventropy/junit-helper/issues), etc.

The project uses [Travis CI's](http://travis-ci.org/) free service for open source projects for continuous integration.
See the [project's Travis CI page](https://travis-ci.org/deventropy/junit-helper) for build status and details.

The project also uses [Coveralls'](http://coveralls.io/) free service for open source projects for tracking unit test
coverage. See the [project's Coveralls page](https://coveralls.io/github/deventropy/junit-helper) for build status and
details.  

### <a name="repository"></a>Binary Distribution Repository

The project is built using [Apache Maven&reg;](http://maven.apache.org/) and binaries are available through the
[JFrog Open Source Software](https://oss.jfrog.org/webapp/#/home) service.

To get the Snapshot and Release artifacts for this project, you may have to add the JFrog OSS repositories (below) to
your project's POM or local settings or as a proxy in a local repository manager.

* **Snapshot Repository:** https://oss.jfrog.org/artifactory/libs-snapshot
* **Release Repository:** https://oss.jfrog.org/artifactory/libs-release

## Modules

The following modules are currently available in the project:

| Project | Summary | Artifact Id | Issues Component |
|---------|---------|-------------|------------------|
| [Derby](./junit-helper-derby/) | Initialize and run embedded [Apache Derby][derby] instances from Junit tests | `junit-helper-derby` | [component:derby](https://github.com/deventropy/junit-helper/labels/component%3Aderby) |
| [Helper Utils](./junit-helper-utils/) | Shared Utils for other modules. | `junit-helper-utils` | [component:utils](https://github.com/deventropy/junit-helper/labels/component%3Autils) |


## Attributions

JUnit is a simple framework to write repeatable tests. It is an instance of the xUnit architecture for unit testing
frameworks. Copyright owned by [JUnit][junit].

Additional attributions are also present on the individual module homepages.

[derby]: http://db.apache.org/derby/ "Apache Derby"
[junit]: http://junit.org/ "JUnit"
