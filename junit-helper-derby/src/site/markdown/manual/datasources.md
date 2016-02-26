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
# User Guide :: Using DataSources

Derby JDBC classes include support for `javax.sql.DataSource` and related APIs (see [DataSource classes](http://db.apache.org/derby/docs/10.12/ref/rrefapi1003363.html)
for more information). When writing JUnit tests involving Derby classes, if the tests require classes that would access
the database through a `DataSource`, you are free to write code to initialize the DataSources, connection pools, etc.
independent of any JUnit Helper code. However, JUnit Helper includes some convenience mechanisms of accessing such
resources without additional code. This page discusses using `DataSrouces` from the `EmbeddedDerbyResource`.

The utility classes provided in this library support all three different kinds of `DataSource` interfaces in JDBC and
supported by Derby.

## <a name="EmbeddedDerbyDataSourceFactory"></a>EmbeddedDerbyDataSourceFactory

[EmbeddedDerbyDataSourceFactory](../apidocs/org/deventropy/junithelper/derby/EmbeddedDerbyDataSourceFactory.html)
interface defines a factory that is implemented by the `EmbeddedDerbyResource` which can create the different kinds of
data sources. The factory instance can be accessed from
[EmbeddedDerbyResource#getDataSourceFactory()](../apidocs/org/deventropy/junithelper/derby/EmbeddedDerbyResource.html#getDataSourceFactory--).

The factory will return data source instances only while the `EmbeddedDerbyResource` is active, other times an
`IllegalStateException` is returned.

The following table shows the `DataSource` instances returned by the different factory methods:

| Factory method | JDBC Interface | Derby implementation returned |
|----------------|----------------|-------------------------------|
| `#getDataSource(boolean)` | `javax.sql.DataSource` | `org.apache.derby.jdbc.EmbeddedDataSource` |
| `#getConnectionPoolDataSource(boolean)` | `javax.sql.ConnectionPoolDataSource` | `org.apache.derby.jdbc.EmbeddedConnectionPoolDataSource` |
| `#getXADataSource(boolean)` | `javax.sql.XADataSource` | `org.apache.derby.jdbc.EmbeddedXADataSource` |

### Caching DataSources

The factory provides a basic single instance lazy cache for each kind of `DataSource` returned. If the `cachedInstance`
parameter of the factory methods is set to `true` the internal cached instance is returned (the instance is created on
the first call for a cached `DataSource` instance).

### JavaSE Compact Profiles

Since Java SE version 8, Oracle JRE has provided [compact profiles](https://docs.oracle.com/javase/8/docs/technotes/guides/compactprofiles/compactprofiles.html).
`compact2` profile has JDBC support, but leaves out certain classes required for full `DataSource` support. Derby
provides support for `DataSource`s for use with the `compact2` profile (using `org.apache.derby.jdbc.BasicEmbedded*`
classes); however at this time the Derby JUnit Helper library does not provide support for those data sources  in the
provided factory. 
