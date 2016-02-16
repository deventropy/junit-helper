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

[![License](https://img.shields.io/github/license/deventropy/junit-helper.svg)](./license.html)
[![Build Status](https://travis-ci.org/deventropy/junit-helper.svg?branch=master)](https://travis-ci.org/deventropy/junit-helper)
[![Coverage Status](https://coveralls.io/repos/github/deventropy/junit-helper/badge.svg?branch=master)](https://coveralls.io/github/deventropy/junit-helper)
[![Codacy Badge](https://api.codacy.com/project/badge/grade/8f5779cce1c9479fa87316349321e9dd)](https://www.codacy.com/app/deventropy/junit-helper)
[![Coverity Scan Status](https://scan.coverity.com/projects/7581/badge.svg)](https://scan.coverity.com/projects/deventropy-junit-helper)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.deventropy.junit-helper/junit-helper/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.deventropy.junit-helper/junit-helper)
[![Bintray](https://img.shields.io/bintray/v/deventropy/repository/junit-helper.svg)](https://dl.bintray.com/deventropy/repository/)
[![Dependency Status](https://www.versioneye.com/user/projects/56b160c93d82b9003761e470/badge.svg?style=flat)](https://www.versioneye.com/user/projects/56b160c93d82b9003761e470)

The [Development Entropy](http://www.deventropy.org/) [Junit Helper](./) project is hosted on
GitHub. See the [GitHub project](http://github.com/deventropy/junit-helper) for source code,
[issue tracker](https://github.com/deventropy/junit-helper/issues), etc.

The project uses multiple online tools for Continuous Integration ([Travis CI](http://travis-ci.org/)), Unit Test
Coverage ([Coveralls](http://coveralls.io/)), Static Code analysis ([Coverity](http://www.coverity.com/),
[Codacy](https://www.codacy.com/)), Dependency and Build Plug-in version tracking ([VersionEye](https://www.versioneye.com/)).
The services used are provided free of charge to open source projects by the owners of the services. **The badges above
link to the project's pages on these tools.**

### Binary Distribution Repository

See Deventropy's [Binary Distribution Repository](../index.html#repository) for current Maven repository information from
where the project is available.

## Modules

The following modules are currently available in the project:

| Project | Summary | Artifact Id | Issues Component |
|---------|---------|-------------|------------------|
| [Derby](./junit-helper-derby/) | Initialize and run embedded [Apache Derby][derby] instances from Junit tests | `junit-helper-derby` | [component:derby](https://github.com/deventropy/junit-helper/labels/component%3Aderby) |

## Attributions

JUnit is a simple framework to write repeatable tests. It is an instance of the xUnit architecture for unit testing
frameworks. Copyright owned by [JUnit][junit].

Additional attributions are also present on the individual module homepages.

[derby]: http://db.apache.org/derby/ "Apache Derby"
[junit]: http://junit.org/ "JUnit"
