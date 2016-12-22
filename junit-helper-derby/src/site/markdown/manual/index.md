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

# User Guide

<!-- 010 getting-started -->
* [Getting Started](./010-getting-started.html)
	* [Hello World](./010-getting-started.html#hello-world)s
	* [Managing Derby Instances](./010-getting-started.html#instance)
		* [Using JUnit Rules](./010-getting-started.html#junit-rules)
	* [EmbeddedDerbyResource state](./010-getting-started.html#resource-state)
	* [Application Logging](./010-getting-started.html#logging)
	* [Common Errors](./010-getting-started.html#common-errors)
<!-- 110 configuration -->
* [Configuration Options](./110-configuration.html)
	* [Database Directory](./110-configuration.html#db-dir)
	* [Database Path](./110-configuration.html#db-path)
	* [Database Logging](./110-configuration.html#db-logging)
	* [Sub Sub Protocols](./110-configuration.html#sub-sub-protocol)
		* [In Memory database](./110-configuration.html#in-memory)
		* [Database in Directory](./110-configuration.html#directory)
		* [Database in a Jar](./110-configuration.html#jar)
		* [Database in the Classpath](./110-configuration.html#classpath)
	* [Post Init Scripts](./110-configuration.html#post-init-script)
<!-- 120 datasources -->
* [Using DataSources](./120-datasources.html)
	* [EmbeddedDerbyDataSourceResource](./120-datasources.html#EmbeddedDerbyDataSourceResource)
	* [EmbeddedDerbyDataSourceFactory](./120-datasources.html#EmbeddedDerbyDataSourceFactory)
<!-- 210 backup-restore -->
* [Backing up and Restoring a Database](./210-backup-restore.html)
	* [Online Backup](./210-backup-restore.html#online-backup)
	* [Using a Backup](./210-backup-restore.html#using-backup)
		* [Create from](./210-backup-restore.html#create-from)
		* [Restore to](./210-backup-restore.html#restore-to)
		* [Roll-forward recovery](./210-backup-restore.html#roll-forward)
<!-- 310 concurrency -->
* [Managing Concurrency](./310-concurrency.html)
<!-- 510 utilities -->
* [Utilities](./510-utilities.html)
	* [Script Runner](./510-utilities.html#script-runner)
	* [Derby Utils](./510-utilities.html#derby-utils)


<!-- TODO Document internals? DB URL parameters, Derby Home reset, etc. -->
