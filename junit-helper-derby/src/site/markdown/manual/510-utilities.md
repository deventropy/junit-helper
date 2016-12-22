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
# User Guide :: Utilities

The library comes with some utilities which may be used in your test code in the `util` subpackage. However, these
classes are *not* considered part of the public API of the library and they may change without notice even between minor
releases.

## <a name="script-runner"></a>Script Runner

`org.deventropy.junithelper.derby.util.DerbyScriptRunner` may be used to execute a SQL script against a Derby database (the
database need not be created or managed by this library). The utility internally uses the Derby 
[ij](http://db.apache.org/derby/docs/10.13/tools/ctoolsij34525.html) tool to execute the scripts.

An instance of this utility is expected to be used against a single connection; and the connection is a required parameter
when creating a new instance.

The following snippet shows a use of this utility to execute a script:

```java
final DerbyScriptRunner scriptRunner = new DerbyScriptRunner(connection);

// Streams to be used for logging
final StringWriter logData1 = new StringWriter();
final WriterOutputStream wos1 = new WriterOutputStream(logData1, Charset.defaultCharset());
final StringWriter logData2 = new StringWriter();
final WriterOutputStream wos2 = new WriterOutputStream(logData2, Charset.defaultCharset());

// Set the default log stream
scriptRunner.setDefaultScriptLogStream(wos1);

// The result of the following will go to logData2
scriptRunner.executeScript("classpath:/org/deventropy/junithelper/derby/memory/simple01/ddl.sql",
	wos2, false);

// The result of the following will go to logData1 - the default
scriptRunner.executeScript("classpath:/org/deventropy/junithelper/derby/memory/simple01/dml.sql");
```

### Character Set

At this time the utility only supports a single character set for reading the `SQL` scripts as well as for writing the
log file. This may be set in the constructor of the class. If not specified, the system default character set is used.

### Logging

The utility supports logging the results of the script execution either at a script level (using `#executeScript(String, File)`
or `#executeScript(String, OutputStream, boolean)` method) either to a file or to an `OutputStream`.

The utility does have a default log stream, which is used when scripts are invoked using the `#executeScript(String)`
method. The default log stream may be set using the `#setDefaultScriptLogStream(OutputStream)` method, and until set,
is initialized with `DerbyUtils#DEV_NULL` stream, which simply discards all log entries.

### Scripts

Scripts sent to this utility should be identified as resources in a format supported by
[UrlResourceUtil](../../../shared-utils/shared-utils-java/resource-location-formats.html).

## <a name="derby-utils"></a>Derby Utils

This `org.deventropy.junithelper.derby.util.DerbyUtils` class provides some random utilities:

### DEV_NULL

This is an `OutputStream` that can discard anything written to it. It is the equivalent of writing anything to the
`/dev/null` device on a *nix system.

### Close Quitely

The `#closeQuietly(java.lang.AutoCloseable)` tries to close any non-null objects like `Statement`, `Connection`,
`ResultSet`, etc. ignoring any exceptions generated on the way.

### Shut down Derby System Quitely

Shuts down the derby system so it can be reloaded; per Derby developer guide. For more information see
[Derby System](https://db.apache.org/derby/docs/10.13/devguide/tdevdvlp20349.html). This method also ignores any
exception generated (which always is).

