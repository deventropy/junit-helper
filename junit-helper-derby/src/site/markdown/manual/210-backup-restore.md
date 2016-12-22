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
# User Guide :: Backing up and Restoring a Database

The Derby JUnit Helper can start a database from a backup or restore a database from a backup with or without archive
logs. Creating or restoring the database from a backup is controlled through the [DerbyResourceConfig](../apidocs/org/deventropy/junithelper/derby/DerbyResourceConfig.html)
instance passed in when initializing the Derby resource.

The library can also perform online backups on a database started with the `EmbeddedDerbyResource`. See the 
[backupLiveDatabase](../apidocs/org/deventropy/junithelper/derby/EmbeddedDerbyResource.html#backupLiveDatabase-java.io.File-boolean-boolean-boolean-) 
Javadocs.

For more information on backup and restore of Derby Instances, see the [Backing up and restoring databases](http://db.apache.org/derby/docs/10.13/adminguide/cadminhubbkup98797.html)
section in the Derby Server and Administration Guide.

## <a name="online-backup"></a>Online Backup

Derby supports two kinds of backups, an offline backup and an online backup. For an offline backup, ensure the Derby
instance is not running and simply perform a file system copy of the Derby directory (can only be supported for a 
`directory` sub-sub protocol, as the `memory` database does not exist after the instance is shut down).

To perform an online backup, Derby provides System Stored Procedures to execute the operations in the `SYSCS_UTIL`
schema. The `EmbeddedDerbyResource` provides convenient method to invoke the system provided procedures through the
'EmbeddedDerbyResource#backupLiveDatabase` method.

The primary parameter to the method is the `backupDir` directory under which backup is performed. Derby creates the
backup in a child directory with the same name as the database under the directory provided.

The `waitForTransactions` parameter controls if the Derby system will wait for active transactions to complete before
running the backup. If the parameter is set to `false` and there are active transactions in the instance, the backup
operation will fail with a `SQLException`. 

### Roll Forward Recovery

To create a backup enabling archive logging for future roll-forward recovery, set the `enableArchiveLogging` parameter
to true. In addition the `deleteArchivedLogs` parameter controls if previous (prior to this backup) archive logs should
be deleted or not. 

## <a name="using-backup"></a>Using a backup

A Derby instance can be started from a backup in three different modes, as noted below. All the recovery modes listed
below are mutually exclusive, and setting one in the configuration will negate all others.

These operations are not supported for Read Only (`jar` and `classpath` sub sub protocols), and though there are no
explicit controls preventing the recovery modes with those sub-sub protocols; the Derby system may ignore the backup
options or throw an error.

### <a name="create-from"></a>Creating from

To create a database from a full backup copy at a specified location, use the `DerbyResourceConfig#createDatabaseFrom`
configuration method with the file parameter pointing to a full backup of a database. If there is already a database
with the same name in the Derby system, an error will occur and the existing database will be left intact. Internally,
the `EmbeddedDerbyResource` uses the `createFrom=path` attribute in the boot-time connection URL. More information on
this mode in the Derby documentation at [Creating a database from a backup copy](http://db.apache.org/derby/docs/10.13/adminguide/tadmincrtdbbkup.html).

### <a name="restore-to"></a>Restoring to

To restore a database by using a full backup from a specified location, use the `DerbyResourceConfig#restoreDatabaseFrom`
onfiguration method with the file parameter pointing to a full backup of a database. If a database with the same name
exists in the Derby system, the system will delete the database, copy it from the backup location, and then restart it.
Internally, the `EmbeddedDerbyResource` uses the `restoreFrom=path` attribute in the boot-time connection URL. More information on
this mode in the Derby documentation at [Restoring a database from a backup copy](http://db.apache.org/derby/docs/10.13/adminguide/tadminhubbkup44.html).

### <a name="roll-forward"></a>Roll-forward Recovery

Derby supports roll-forward recovery to restore a damaged database to the most recent state before a failure occurred.
To perform a roll-forward recovery, use the `DerbyResourceConfig#recoverDatabaseFrom` configuration method. Provide the
location of the last good backup in the `dbBackupDir` parameter, and the log location in the `recoveryLogDevice`
parameter. Note, the Derby system looks for the archive logs in a directory named `logs` under the `recoveryLogDevice`
location. Internally the `EmbeddedDerbyResource` uses the `rollForwardRecoveryFrom=path` and `logDevice=path` attributes
in the boot-time connection URL.

Derby restores a database from full backup and replays all the transactions after the backup. All the log files after a
backup are required to replay the transactions after the backup. In roll-forward recovery, the log archival mode ensures
that all old log files are available. The log files are available only from the time that the log archival mode is
enabled.

Further reading in the Derby documentation at [Roll-forward recovery](http://db.apache.org/derby/docs/10.13/adminguide/cadminrollforward.html).
