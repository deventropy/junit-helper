/* 
 * Copyright 2016 Development Entropy (deventropy.org) Contributors
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *  http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.deventropy.junithelper.derby.backup.simple01;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

import org.apache.commons.io.FileUtils;
import org.deventropy.junithelper.derby.DerbyResourceConfig;
import org.deventropy.junithelper.derby.DerbyUtils;
import org.deventropy.junithelper.derby.EmbeddedDerbyResource;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * @author Bindul Bhowmik
 *
 */
public class RollForwardRecoveryTest extends AbstractEmbeddedDerbyBackupTest {
	
	private static final String DB_NAME_FIRST = "test-database-backup-test02-first";
	private static final String DB_NAME_SECOND = "test-database-backup-test02-second";
	
	@Rule
	public TemporaryFolder tempFolder = new TemporaryFolder();
	
	@Test
	public void testRollForwardRecoverySequence () throws Exception {
		// Create the original DB
		final DirDerbyHomeDbInfo originDbInfo = createAndStartEmbeddedDerbyResourceSimple01(tempFolder,
				DB_NAME_FIRST, true);
		final EmbeddedDerbyResource originDbResource = originDbInfo.getEmbeddedDerbyResource();
		final File backupBase = tempFolder.newFolder();

		// Back up the database, make changes and shut it down
		final File originDbBackupDir = backupDbWithArchiveLogsMakeChangesAndShutdown(originDbResource, backupBase,
				DB_NAME_FIRST, false);
		final File backupHistoryFile = new File (originDbBackupDir, "BACKUP.HISTORY");
		assertTrue("Backup directory should exist", originDbBackupDir.exists() && originDbBackupDir.isDirectory());
		assertTrue("Backup history file exists", backupHistoryFile.exists());

		// Prepare for backup (copy the directory and delete the original)
		final File originDbCopyForLogs = tempFolder.newFolder();
		FileUtils.copyDirectory(originDbInfo.getDbDirectory(), originDbCopyForLogs);
		FileUtils.deleteDirectory(originDbInfo.getDbDirectory());

		// Start a database from the backup and back that up too
		restoreDbFromBackupCheckAndShutdown(originDbInfo, backupBase, originDbBackupDir, originDbCopyForLogs,
				DB_NAME_FIRST);

		// Repeat backup, deleting old logs
		final DirDerbyHomeDbInfo secondDbInfo = createAndStartEmbeddedDerbyResourceSimple01(tempFolder,
				DB_NAME_SECOND, true);
		final EmbeddedDerbyResource secondDbResource = secondDbInfo.getEmbeddedDerbyResource();
		final File secondBackupBase = tempFolder.newFolder();

		// Back up the database, make changes and shut it down
		final File secondDbBackupDir = backupDbWithArchiveLogsMakeChangesAndShutdown(secondDbResource, secondBackupBase,
				DB_NAME_SECOND, true);
		final File secondBackupHistoryFile = new File (secondDbBackupDir, "BACKUP.HISTORY");
		assertTrue("Backup directory should exist", secondDbBackupDir.exists() && secondDbBackupDir.isDirectory());
		assertTrue("Backup history file exists", secondBackupHistoryFile.exists());

		// Prepare for backup (copy the directory and delete the original)
		final File secondDbCopyForLogs = tempFolder.newFolder();
		FileUtils.copyDirectory(secondDbInfo.getDbDirectory(), secondDbCopyForLogs);
		FileUtils.deleteDirectory(secondDbInfo.getDbDirectory());

		// Start a database from the backup and back that up too
		restoreDbFromBackupCheckAndShutdown(secondDbInfo, secondBackupBase, secondDbBackupDir, secondDbCopyForLogs,
				DB_NAME_SECOND);
	}
	
	private File backupDbWithArchiveLogsMakeChangesAndShutdown (final EmbeddedDerbyResource originDbResource,
			final File backupBase, final String dbName, final boolean deleteOldLogs) throws SQLException, IOException {
		// Online backup the DB (misusing deleteOldLogs as wait to complete coverage)
		originDbResource.backupLiveDatabase(backupBase, deleteOldLogs, true, deleteOldLogs);
		final File originDbBackupDir = new File(backupBase, dbName);

		// Change the original DB
		applyPostBackupChangesAndVerify(originDbResource);

		// Shut down the DB
		closeEmbeddedDerbyResource(originDbResource);
		DerbyUtils.shutdownDerbySystemQuitely(true);
		return originDbBackupDir;
	}
	
	private void restoreDbFromBackupCheckAndShutdown (final DirDerbyHomeDbInfo originDbInfo, final File backupBase,
			final File originDbBackupDir, final File originDbCopyForLogs, final String dbName)
					throws IOException, SQLException {
		// Start a new DB with create From...
		final DerbyResourceConfig derbyResourceConfigCreateFrom = DerbyResourceConfig.buildDefault()
				.useDatabaseInDirectory(dbName)
				.recoverDatabaseFrom(originDbBackupDir, originDbCopyForLogs);
		final EmbeddedDerbyResource dbCopyEmbeddedDerbyResource = new EmbeddedDerbyResource(
				derbyResourceConfigCreateFrom, originDbInfo.getDerbySystemHome());
		dbCopyEmbeddedDerbyResource.start();
		final File dbCopyDirDbDir = new File (originDbInfo.getDerbySystemHome(), dbName);
		assertTrue("Database directory should exist", dbCopyDirDbDir.exists() && dbCopyDirDbDir.isDirectory());

		// Make sure it started up and does not have the new data
		simpleDb01Check01(dbCopyEmbeddedDerbyResource);
		assertTrue("Post backup change should or should not exist here",
				hasPostBackupDbChanges(dbCopyEmbeddedDerbyResource));

		// Shut down the DB
		closeEmbeddedDerbyResource(dbCopyEmbeddedDerbyResource);
		DerbyUtils.shutdownDerbySystemQuitely(true);
	}
}
