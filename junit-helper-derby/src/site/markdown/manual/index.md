<!--
Copyright 2016 JUnit Helper Contributors

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

# User Guide

* [Getting Started](#getting-started)
* [Managing Derby Instances](#instance)
	* [Using JUnit Rules](#junit-rules)
* [Application Logging](#logging)
* [Configuration Options](./configuration.html)
	* [Database Directory](./configuration.html#db-dir)
	* [Database Path](./configuration.html#db-path)
	* [Database Logging](./configuration.html#db-logging)
	* [Sub Sub Protocols](./configuration.html#sub-sub-protocol)
		* [In Memory database](./configuration.html#in-memory)
		* [Database in Directory](./configuration.html#directory)
	* [Post Init Scripts](./configuration.html#post-init-script)
* [Managing Concurrency](./concurrency.html)
* [Utilities](./utilities.html)
	* [Script Runner](./utilities.html#script-runner)
	* [Derby Utils](./utilities.html#derby-utils)

<!-- TODO Document internals? DB URL parameters, Derby Home reset, etc. -->

## <a name="getting-started"></a>Getting Started

JUnit Helper Derby Utils is a set of classes that aid in building and writing unit tests of database code using
[Apache Derby](http://db.apache.org/derby/) embedded database. It has been developed, tested and used with unit tests
built on [JUnit](http://junit.org) only; it might work with other testing frameworks, but no claim to such usability
is implied. It does not aim to be a general purpose database utility as well.

### Dependency Info

See the [Dependency Info](../dependency-info.html) page to add this project as a dependency to your project; if the
use of this library is limited to unit testing only, then add the dependency to the `test` scope only. You may have to add
the required [Distribution Repository](../../index.html#repository) for resolution.

Also see the [Project Dependencies](../dependencies.html) for dependencies that come with this project, you may want to
exclude some of them to avoid version conflicts if your project has some of those dependencies as well. 

### Basic Workflow

When unit testing a piece of JDBC code, the steps would be:

1. Configure and start an in-memory instance of Derby
1. Set up the tables and test data in the instance
1. Run your tests
1. Shut down the database and run cleanup

### Hello World

A small sample test:

```java
public class SimpleDerbyTest {
	
	private TemporaryFolder tempFolder = new TemporaryFolder();
	private EmbeddedDerbyResource embeddedDerbyResource =
		new EmbeddedDerbyResource(DerbyResourceConfig.buildDefault()
			.useDevNullErrorLogging(),
		tempFolder);
	
	@Rule
	public RuleChain derbyRuleChain = RuleChain.outerRule(tempFolder).around(embeddedDerbyResource);

	@Test
	public void test () throws SQLException {
		final String jdbcUrl = embeddedDerbyResource.getJdbcUrl();
		Connection connection = null;
		Statement stmt = null;
		ResultSet rs = null;

		try {
			connection = DriverManager.getConnection(jdbcUrl);
	
			// Check a value
			stmt = connection.createStatement();
			rs = stmt.executeQuery("SELECT 1 FROM SYSIBM.SYSDUMMY1");
	
			assertTrue(rs.next());
		} finally {
			// Close resources
		}
	}
}
```

The Derby instance is wrapped inside the `EmbeddedDerbyResource` instance, and it has convenience methods to access the
runtime values from the instance (see _[EmbeddedDerbyResource Javadocs](../apidocs/org/deventropy/junithelper/derby/EmbeddedDerbyResource.html)_
for more information on available methods). The instance can be configured using the `DerbyResourceConfig` instance,
which provides a [Fluent Interface](https://en.wikipedia.org/wiki/Fluent_interface) to configure runtime parameters
(see _[javadocs](../apidocs/org/deventropy/junithelper/derby/DerbyResourceConfig.html)_ for API information). See the
[Configuration Options](./configuration.html) documentation to see available configurations.

## <a name="instance"></a>Derby Instances

An *Embedded Derby* instance can be started and stopped using the `EmbeddedDerbyResource#start()` and `EmbeddedDerbyResource#close()`
methods. Users are free to initialize and manage instances on their own (also read about [Concurrency](./concurrency.html)
when attempting to run multiple concurrent instances of Derby).

If the user wants to control the instance and possibly share it across multiple test classes or want more control over
the running instance.

### <a name="junit-rules"></a>Using JUnit Rules

The `EmbeddedDerbyResource` does extend JUnit `ExternalResource` so can be used as a [JUnit Rules](https://github.com/junit-team/junit/wiki/Rules).
In that case, JUnit test runners will automatically instantiate the external resource and handle shutting down the
instance after the execution of the test (using the `EmbeddedDerbyResource#before()` and `EmbeddedDerbyResource#after()`
methods which in turn use the `EmbeddedDerbyResource#start()` and `EmbeddedDerbyResource#close()` methods respectively).

Derby needs a directory as the `derby.system.home` which can be either a directory that the user code supplies, a 
Java temporary directory or JUnit [TemporaryFolder Rule](https://github.com/junit-team/junit/wiki/Rules#temporaryfolder-rule)
(more at [Database Directory](./configuration.html#db-dir)). If the `TemporaryFolder` Rule is used as well as the
`EmbeddedDerbyResource` as a JUnit Rule, then the two need to be chained together using a
**[RuleChain](https://github.com/junit-team/junit/wiki/Rules#rulechain)** to tie the `#before()` and `#after()` methods
in order. The following sample shows a sample use:

```java
private TemporaryFolder tempFolder = new TemporaryFolder();
private EmbeddedDerbyResource embeddedDerbyResource =
	new EmbeddedDerbyResource(DerbyResourceConfig.buildDefault(), tempFolder);

@ClassRule
public RuleChain derbyRuleChain = RuleChain.outerRule(tempFolder).around(embeddedDerbyResource);
```

## <a name="logging"></a>Application Logging

Derby JUnit Helper uses [Log4j2 API](http://logging.apache.org/log4j/2.x/manual/api.html) for the little bit of logging
it sends. It does not have a default log configuration with the distribution, neither does it come with a runtime logger.
Users are free to provide their own configuration and runtime logger as a dependency; or live with no logging and a 2
line Log4j warning on each JVM run.
