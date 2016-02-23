<!--
Copyright 2016 Development Entropy (deventropy.org) Contributors

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

## <a name="db-path"></a>Database Path

*This configuration is optional for some sub-sub protocols (see specific sub-sub protocol for details).*

The sub-sub protocol dependent path at which the database is running (database name for in memory database, relative
or absolute database path for directory database, path to the database directory in a jar file for a jar database) is
available from the configuration using the `#getDatabasePath()` method.

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

For the `In Memory` and `Directory` sub sub protocols, the resource also allows starting or restoring a database from a
backup. For more information on those options, please see the [Backing up and Restoring a Database](./backup-restore.html)
documentation.

### <a name="in-memory"></a>In Memory Database

*Method to enable:* `#useInMemoryDatabase() or #useInMemoryDatabase(String)`<br />
*Enumeration Value:* Memory<br />
*Derby JDBC URL prefix:* jdbc:derby:memory:<br />
*Additional Configurations:* Database Name<br />

Databases exist only in main memory and are not written to disk.

#### <a name="in-memory-db-path"></a>Database Path

The `DerbyResourceConfig` lets you set the [Database Path](https://db.apache.org/derby/docs/10.12/ref/rrefattrib17246.html)
using the `#useInMemoryDatabase(String)` method.

If the database name is not set, an automatic database name is generated using a `java.util.UUID` object. The method
`#getDefaultDatabasePathName()` is used internally, which generates a new UUID value on every call.

### <a name="directory"></a>Database in Directory

*Method to enable:* `#useDatabaseInDirectory() or #useDatabaseInDirectory(String) or #useDatabaseInDirectory(String,
boolean)`<br />
*Enumeration Value:* Directory<br />
*Derby JDBC URL prefix:* jdbc:derby:directory:<br />
*Additional Configurations:* Database Path, Skip create attribute<br />

The database runs in a specified directory, either a relative directory under the `Derby System Directory` or an
absolute directory outside the system directory. See [Connecting to databases](http://db.apache.org/derby/docs/10.12/devguide/cdevdvlp34964.html)
in the Derby Development Reference for more information.

#### <a name="directory-db-path"></a>Database Path

The `DerbyResourceConfig` lets you set the database path using the `#useDatabaseInDirectory(String)` method. If a path
is not set, an automatic database path is generated using a `java.util.UUID` object. The method `#getDefaultDatabasePathName()`
is used internally, which generates a new UUID value on every call. This will be a sub directory under the `Derby System
Directory`.

#### Skip `create` attribute

By default, for the `memory` and `directory` databases, the `EmbeddedDerbyResource` starts the Derby instance with the
[create=true attribute](http://db.apache.org/derby/docs/10.12/ref/rrefattrib26867.html). The configuration allows the
user to skip this attribute using the `#useDatabaseInDirectory(String, boolean)` method with the `skipCreateAttribute`
attribute set to `true`.

#### Existing (non-database) existing directory

When the `EmbeddedDerbyResource` starts the database with the `;create=true` JDBC URL parameter, and it seems like Derby
[likes to create its own directory](http://mail-archives.apache.org/mod_mbox/db-derby-user/200912.mbox/%3C4B3C845A.3050104@gmx.ch%3E).
If the directory (relative or absolute) specified as the `#getDatabasePath()` already exists, the Derby startup will
fail with an error similar to:

```
java.sql.SQLException: Failed to create database 'directory:/path/to/database', see the next exception for details.
	at org.apache.derby.impl.jdbc.SQLExceptionFactory.getSQLException(Unknown Source)
	... NN more
Caused by: ERROR XJ041: Failed to create database 'directory:/path/to/database', see the next exception for details.
	at org.apache.derby.iapi.error.StandardException.newException(Unknown Source)
	at org.apache.derby.impl.jdbc.SQLExceptionFactory.wrapArgsForTransportAcrossDRDA(Unknown Source)
	... 41 more
Caused by: ERROR XBM0J: Directory /derby/system/home//path/to/database already exists.
	at org.apache.derby.iapi.error.StandardException.newException(Unknown Source)
	... NN more
```

So, if a directory path is specified in `#useDatabaseInDirectory(String)`, ensure that database does not exist.

### <a name="jar"></a>Database in a Jar

*Method to enable:* `#useJarSubSubProtocol(String, String)`<br />
*Enumeration Value:* Jar<br />
*Derby JDBC URL prefix:* jdbc:derby:jar:<br />
*Additional Configurations:* Path to Jar file &amp; Database Path<br />

Derby supports creating and using Read Only databases; which may be pre-existing database setup and data that is used
in tests. For instructions on how to create read only database, see the Derby documentation
[Creating Derby databases for read-only use](https://db.apache.org/derby/docs/10.12/devguide/cdevdeploy15325.html).
Though not required, but a read only database in a jar file may be created during a test execution and used in a future
test as well; just make sure to completely shut down the derby system before creating the archive.

The read only database is run from the jar file specified in the `jarFilePath` location. The path may be absolute or
relative. The relative path is resolved by the Derby system relative to the `Derby System Directory`.

The `databasePath` is the path of the database directory inside the `jar` file archive.

For further information on read only databases, see the Derby documentation at [Accessing a read-only database in a
zip/jar file](http://db.apache.org/derby/docs/10.12/devguide/cdevdeploy11201.html) and [Accessing databases from a jar
or zip file](http://db.apache.org/derby/docs/10.12/devguide/cdevdvlp24155.html).

If a `jar` database is configured with values of `jarFilePath` and `databasePath` as `/tmp/test/db.jar` and
`/sample/product` respectively, it will open a read-only database `product` in the directory `sample` in the jar file.
The effective JDBC URL generated with these parameters is `jdbc:derby:jar:(/tmp/test/db.jar)sample/product`.

Due to lack of documentation on how to implement this, the tool currently does not support an `empty` or `null`
`databasePath`; setting such a value will cause a `IllegalArgumentException` from the configuration class.

### <a name="classpath"></a>Database from the classpath

*Method to enable:* `#useClasspathSubSubProtocol(String)`<br />
*Enumeration Value:* Classpath<br />
*Derby JDBC URL prefix:* jdbc:derby:classpath:<br />
*Additional Configurations:* Database Path<br />

This is another type of Read Only database supported by Derby. The database exists on the classpath, either in a Jar
file or directly in the classpath. The _Database Path_ mentioned above  designates the path to the database in the
classpath. All databaseNames must begin with at least a slash, because you specify them _relative_ to the classpath
directory or archive. See also [Syntax of database connection URLs for applications with embedded
databases](http://db.apache.org/derby/docs/10.12/ref/rrefjdbc37352.html) and [Accessing databases within a jar file 
using the classpath](http://db.apache.org/derby/docs/10.12/devguide/tdevdeploy39856.html).

The Derby development guide has more on [Accessing databases from the
classpath](http://db.apache.org/derby/docs/10.12/devguide/cdevdvlp91854.html).

## <a name="post-init-script"></a>Post Init Scripts

The initial configuration supports adding SQL scripts to be executed on database initialization. These can be used to
set up database tables, seed initial or test data, etc. Multiple scripts can be added to the configuration using calls
to the `#addPostInitScript(String)` methods. All scripts added will be executed in the order they were added to this
method.

*Note:* Exceptions when executing these scripts may cause the database to fail to start up.

The script URLs configured in this method should be in formats supported by
[UrlResourceUtil](../../junit-helper-utils/apidocs/index.html?org/deventropy/junithelper/utils/UrlResourceUtil.html).

