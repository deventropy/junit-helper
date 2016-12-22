<!--
Copyright 2015 Development Entropy (deventropy.org) Contributors

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

# Home

Derby JUnit Helper is a utility to allow testing JDBC related code against [Apache Derby][derby] embedded instances
using [JUnit][junit] version 4.x.

The library allows the user to configure an embedded Derby instance as an External Resource which can be added to a
test class using the [Rules](https://github.com/junit-team/junit4/wiki/Rules) annotation, with initialization scripts;
and JUnit managing the initialization and de-initialization of the database instance.

The library also comes with some utilities to manage the running instance, running `SQL scripts` against the instance (or
any derby instance for that matter), etc. More details are in the user documents.

## User Guide

See the [User Manual](./manual/index.html) for the user guide.

## Source Code, Issues and Support

This module is part of the [Development Entropy](http://www.deventropy.org/) [JUnit Helper](../) project. See the project's
[homepage](../) for details on source repository, issue tracker, continuous integration service, etc. That page also
has details regarding the project binary (Maven 2) repository.

To add this module as a dependency to your project, see the generated [Dependency Information](./dependency-info.html)
page. **Note:** As this module is meant to aid in unit testing, if your project is using Maven for your builds, you
probably are OK just adding it with `<scope>test</scope>`.

## Attributions

Apache&reg;,
[Apache DB](http://db.apache.org/ "Apache DB")&reg;,
[Apache Derby][derby]&reg;, [Apache Logging](http://logging.apache.org/)&reg;,
[Apache Log4j](http://logging.apache.org/log4j/)&reg;, Log4j&reg;,
the Derby hat logo, the Apache Logging project logo and the Apache feather logo are registered trademarks or trademarks
of the [Apache Software Foundation](http://www.apache.org/ "ASF") in the United States and/or other countries.

JUnit is a simple framework to write repeatable tests. It is an instance of the xUnit architecture for unit testing
frameworks. Copyright owned by [JUnit][junit].

The JNDI DataSource implementation is inspired by Randy Carver's post [Injecting JNDI datasources for JUnit Tests
outside of a container](https://blogs.oracle.com/randystuph/entry/injecting_jndi_datasources_for_junit).

[derby]: http://db.apache.org/derby/ "Apache Derby"
[junit]: http://junit.org/ "JUnit"
