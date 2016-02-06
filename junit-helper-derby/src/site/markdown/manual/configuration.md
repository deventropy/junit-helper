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

[&#x21ab; User Guide Home](./index.html)
# User Guide :: Configuration Options

Unless otherwise noted, the Derby JUnit Helper instances can be configured using the `DerbyResourceConfig` class,
which provides a Fluent API to configure the `EmbeddedDerbyResource`
(see [Javadoc](../apidocs/org/deventropy/junithelper/derby/DerbyResourceConfig.html)).

The configuration instance of the class tries to keep option compatibility; so setting a particular option value may
reset another option. Read the documentation on this page or the Javadoc of the specific configuration method on possible
side effects of a call.

## <a name="db-dir"></a>Database Directory

**Note:** This property cannot be configured using the `DerbyResourceConfig`, but needs to be set up using one of the
constructors of `EmbeddedDerbyResource`.

The Derby System Home (**[derby.system.home](https://db.apache.org/derby/docs/10.12/ref/rrefproper32066.html)**) is the
directory used by Derby to read the derby properties file from, store database files (for non in-memory databases) and is
the default location of the derby log file.

After the Derby instance is started the home directory in use can be viewed using the `EmbeddedDerbyResource#getDerbySystemHome()`
method.

The Derby JUnit Helper allows two different options to set the directory in the `EmbeddedDerbyResource`:

### TemporaryFolder

The `EmbeddedDerbyResource` supports using a JUnit [TemporaryFolder Rule](https://github.com/junit-team/junit/wiki/Rules#temporaryfolder-rule)
as a parent directory for the database directory. When using a temporary folder:

* The `EmbeddedDerbyResource` will not call the `#before()` or `#after()` methods on the `TemporaryFolder`. If the 
	temporary folder is configured using a JUnit `@Rule` or `@ClassRule`, the JUnit test runner will set that up,
	otherwise the calling class will have to ensure that it is initialized and de-initialized.
* The actual directory used is a sub directory in the `TemporaryFolder` created using the `#newFolder()` method.
* The `EmbeddedDerbyResource` will not try to use the `TemporaryFolder` until the `#start()` (or `#before()`) method
	is invoked on it, to allow both to be configured as JUnit `@Rule`s or `@ClassRule`s.

**Important:** If both `TemporaryFolder` and `EmbeddedDerbyResource` are used as JUnit `@Rule`s or `@ClassRule`, it is
imperative that both are configured using a `RuleChain` and the `@Rule` or `@ClassRule` annotation be present on the
`RuleChain` with the `TemporaryFolder` being initialized before the `EmbeddedDerbyResource`.

```java
private TemporaryFolder tempFolder = new TemporaryFolder();
private EmbeddedDerbyResource embeddedDerbyResource =
	new EmbeddedDerbyResource(DerbyResourceConfig.buildDefault(), tempFolder);

@Rule
public RuleChain derbyRuleChain = RuleChain.outerRule(tempFolder).around(embeddedDerbyResource);
```

### java.io.File Directory

`EmbeddedDerbyResource` also supports accepting a `java.io.File` directory to use as a database directory. The `File`
object for the database directory need not exist before the test execution, but should be a path that the user the test
is running under should have enough privileges to create the directory (if it does not exist) and be able to write in
it.

The `EmbeddedDerbyResource` does not clean up any content of the directory after the execution.

The following code snippet shows a sample to create a `EmbeddedDerbyResource` using a Java temporary file, faked as a
folder:

```java
File tempFile = File.createTempFile("junit-helper-derby", Long.toString(System.nanoTime()));
tempFile.delete();
tempFile.mkdir();

final EmbeddedDerbyResource embeddedDerbyResource = new EmbeddedDerbyResource(DerbyResourceConfig.buildDefault(), tempFile);
```

### System Property Handling / Limitations

Apache Derby only allows the `derby.system.home` to be set up as a System property, and if the property is not set it
uses the `user.dir` directory as the the database directory.

To set the selected database directory on Derby, the `EmbeddedDerbyResource` does the following:

* In the `#start()` (or `#before()`) method:
	* If a `derby.system.home` property is set, save it off
	* Set the value of the selected database directory as the `derby.system.home` property
* In the `#close()` (or `#after()`) method:
	* Unset the `derby.system.home` property
	* If an old value was saved in the `#start()` method, set that back to the `derby.system.home`

The additional consequence of this behavior of Derby is that only a single instance of Derby (and `EmbeddedDerbyResource`)
can be running in a single JVM. See [Managing Concurrency](./concurrency.html) for options of running multiple instances.

## <a name="db-name"></a>Database Name

*This configuration is optional*

The `DerbyResourceConfig` lets you set the [Database Name](https://db.apache.org/derby/docs/10.12/ref/rrefattrib17246.html)
using the `#setDatabaseName(String)` method.

If the database name is not set, an automatic database name is generated using a `java.util.UUID` object. The method
`#getDefaultDatabaseName()` is used internally, which generates a new UUID value on every call.

## <a name="db-logging"></a>Database Error Logging

*This configuration defaults to* `default` *logging mode*

The resource supports configuring one of the Derby Error Logging modes from the enumeration
[ErrorLoggingMode](../apidocs/org/deventropy/junithelper/derby/ErrorLoggingMode.html):

### Default

*Method to enable:* `#useDefaultErrorLogging()`
*Enumeration Value:* Default

This is the default setting; derby will write to `derby.log` file in the `derby.system.home`.

### No Logging

*Method to enable:* `#useDevNullErrorLogging()`
*Enumeration Value:* Null

All error log entries will be eaten up using an `OutputStream` which simply ignores all writes (see [DerbyUtils#DEV_NULL](../apidocs/org/deventropy/junithelper/derby/DerbyUtils.html#DEV_NULL)
stream), equivalent to redirecting logs to `/dev/null` on a *nix system.

## <a name="sub-sub-protocols"></a>Derby Sub-Sub Protocols

*This configuration defaults to* `Memory` *sub sub protocol*

In Derby speak [subsubprotocol](http://db.apache.org/derby/docs/10.12/ref/rrefjdbc37352.html) specifies where Derby looks
for a database: in a directory, in memory, in a classpath, or in a jar file. The Derby JUnit Helper currently supports
the sub sub protocols listed in the following sub sections.

The enumeration [JdbcDerbySubSubProtocol](../apidocs/org/deventropy/junithelper/derby/JdbcDerbySubSubProtocol.html) is
used to list the supported sub sub protocols.

Some of the sub-sub protocols require additional configuration parameters, some required others optional. See the sub
section below. Calling the `#useXXXDatabase()` methods on select any sub sub protocol will reset additional configurations
set for any other sub-sub protocol. 

### <a name="in-memory"></a>In Memory Database

*Method to enable:* `#useInMemoryDatabase()`
*Enumeration Value:* Memory
*Derby JDBC URL prefix:* jdbc:derby:memory:
*Additional Configurations:* None

Databases exist only in main memory and are not written to disk.

## <a name="post-init-script"></a>Post Init Scripts

The initial configuration supports adding SQL scripts to be executed on database initialization. These can be used to
set up database tables, seed initial or test data, etc. Multiple scripts can be added to the configuration using calls
to the `#addPostInitScript(String)` methods. All scripts added will be executed in the order they were added to this
method.

*Note:* Exceptions when executing these scripts may cause the database to fail to start up.

The script URLs configured in this method should be in formats supported by
[UrlResourceUtil](../../../shared-utils/shared-utils-java/apidocs/org/deventropy/junithelper/utils?UrlResourceUtil.html).
