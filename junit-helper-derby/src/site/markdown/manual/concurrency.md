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
# User Guide :: Managing Concurrency

While a running instance of Derby is perfectly happy to support multiple concurrent consumers; it does not like running
multiple instances of Derby in a single JVM (see Derby System documentation
[One Derby instance for each Java Virtual Machine (JVM)](https://db.apache.org/derby/docs/10.12/devguide/cdevdvlp96597.html).
This makes sharing a Derby instance across multiple instances a little challenging.

So, first if multiple test classes are required to use this library or Derby instances in general every attempt should
be made to consolidate the test cases in a single class.

## Single Derby Instance per Test Class

The first strategy is to use a single instance of Derby per class. This can be ensured managing the `EmbeddedDerbyResource`
as a `@ClassRule` or using `@BeforeClass` / `@AfterClass` methods.

Further, if using a test runner like Maven Surefire, class and method level parallelism should be disabled, at least for
these classes. See 
[Fork Options and Parallel Test Execution](https://maven.apache.org/surefire/maven-surefire-plugin/examples/fork-options-and-parallel-execution.html)
for information on Surefire parallelism.

Different classes using different Derby database homes can however be executed on multiple JVMs in parallel, so any
test runner supporting **forked JVMs** (like Surefire) may be used to executed individual classes on multiple JVMs.

### Class Cleanup

If multiple test classes executed sequentially in a JVM need to initialize a new instance of Derby (after a previous run
by the same/different class started, then stopped a Derby instance) may run into issues unless the previous instance
was shut down properly (see [Shutting down the system](https://db.apache.org/derby/docs/10.12/devguide/tdevdvlp20349.html)
in the Derby Developer Guide.

It is therefore important to *clean up after oneself*. The `DerbyUtil` class in this library provides a convenient method
to achieve that: `#shutdownDerbySystemQuitely(boolean)`. This may be wired up in a method that is executed at the end of
running all tests in the method. An example:

```java
@AfterClass
public static void cleanupDerbySystem () {
	// Cleanup for next test
	DerbyUtils.shutdownDerbySystemQuitely(true);
}
```

## Multiple Derby Instances per Test Class / Method

In cases where it is absolutely necessary, it is possible to use multiple Derby instances in a single test class or
method. There are a few caveats though:

* Only a single instance of Derby should be active at any time.
* Unless there is a single test concurrency; the initialization and shutdown of Derby instances or `EmbeddedDerbyResource`
	should be controlled by a single test method or thread.

A sample flow case for this behavior is shown below:

```java

	// Set up and start the first instance
	final DerbyResourceConfig derbyResourceConfig1 = DerbyResourceConfig.buildDefault();	// Add other setup
	final EmbeddedDerbyResource embeddedDerbyResource1 = new EmbeddedDerbyResource(derbyResourceConfig1,
			derbySystemHomeDir);
	embeddedDerbyResource1.start();
	
	// Execute test steps with the first database
	
	// Shutdown and clean up the first instance
	embeddedDerbyResource1.close();
	DerbyUtils.shutdownDerbySystemQuitely(true);
	
	// Set up and start the second instance
	final DerbyResourceConfig derbyResourceConfig2 = DerbyResourceConfig.buildDefault();	// Add other setup
	final EmbeddedDerbyResource embeddedDerbyResource2 = new EmbeddedDerbyResource(derbyResourceConfig2,
			derbySystemHomeDir);
	embeddedDerbyResource2.start();
	
	// Execute test steps with the second database
	
	// Shutdown and clean up the second instance
	embeddedDerbyResource2.close();
	DerbyUtils.shutdownDerbySystemQuitely(true);
```
