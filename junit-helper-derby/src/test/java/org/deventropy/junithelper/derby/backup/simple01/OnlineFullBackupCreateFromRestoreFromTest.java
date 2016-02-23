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

import org.deventropy.junithelper.derby.DerbyResourceConfig;
import org.deventropy.junithelper.derby.DerbyUtils;
import org.deventropy.junithelper.derby.EmbeddedDerbyResource;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * @author Bindul Bhowmik
 */
public class OnlineFullBackupCreateFromRestoreFromTest extends AbstractEmbeddedDerbyBackupTest {
	
	private static final String DB_NAME_ORIGIN = "test-database-backup-test01-origin";
	
	private static final String DB_NAME_COPY = "test-database-backup-test01-copy";
	
	@Rule
	public TemporaryFolder tempFolder = new TemporaryFolder();

	@Test
	public void testSequenceForOnlineFullBackupCreateAndRestore () throws Exception {
		// Create the original DB
		final DirDerbyHomeDbInfo originDbInfo = createAndStartEmbeddedDerbyResourceSimple01(tempFolder,
				DB_NAME_ORIGIN, true);
		final EmbeddedDerbyResource originDbResource = originDbInfo.getEmbeddedDerbyResource();
		final File backupBase = tempFolder.newFolder();

		// Back up the database, make changes and shut it down
		final File originDbBackupDir = backupDbMakeChangesAndShutdown(originDbResource, backupBase);
		final File backupHistoryFile = new File (originDbBackupDir, "BACKUP.HISTORY");
		assertTrue("Backup directory should exist", originDbBackupDir.exists() && originDbBackupDir.isDirectory());
		assertTrue("Backup history file exists", backupHistoryFile.exists());

		// Start a database from the backup and back that up too
		final File dbBackupDir2 = createDbFromBackupAndBackup(originDbInfo, backupBase, originDbBackupDir);
		final File backupHistoryFile2 = new File (dbBackupDir2, "BACKUP.HISTORY");
		assertTrue("Backup directory should exist", dbBackupDir2.exists() && dbBackupDir2.isDirectory());
		assertTrue("Backup history file exists", backupHistoryFile2.exists());

		// // Start a new DB with memory protocol from the second backup
		inMemoryDbCreateFromSecondBackup(originDbInfo, dbBackupDir2);

		// Start a new DB with restore From in the original DBs location
		restoreOriginalDatabase(originDbInfo, originDbBackupDir);
	}

	private File backupDbMakeChangesAndShutdown (final EmbeddedDerbyResource originDbResource, final File backupBase)
			throws SQLException, IOException {
		// Online backup the DB
		originDbResource.backupLiveDatabase(backupBase, true, false, false);
		final File originDbBackupDir = new File(backupBase, DB_NAME_ORIGIN);

		// Change the original DB
		applyPostBackupChangesAndVerify(originDbResource);

		// Shut down the DB
		closeEmbeddedDerbyResource(originDbResource);
		DerbyUtils.shutdownDerbySystemQuitely(true);
		return originDbBackupDir;
	}

	private File createDbFromBackupAndBackup (final DirDerbyHomeDbInfo originDbInfo, final File backupBase,
			final File originDbBackupDir) throws IOException, SQLException {
		// Start a new DB with create From...
		final DerbyResourceConfig derbyResourceConfigCreateFrom = DerbyResourceConfig.buildDefault()
				.useDatabaseInDirectory(DB_NAME_COPY).createDatabaseFrom(originDbBackupDir);
		final EmbeddedDerbyResource dbCopyEmbeddedDerbyResource = new EmbeddedDerbyResource(
				derbyResourceConfigCreateFrom, originDbInfo.getDerbySystemHome());
		dbCopyEmbeddedDerbyResource.start();
		final File dbCopyDirDbDir = new File (originDbInfo.getDerbySystemHome(), DB_NAME_COPY);
		assertTrue("Database directory should exist", dbCopyDirDbDir.exists() && dbCopyDirDbDir.isDirectory());

		// Make sure it started up and does not have the new data
		simpleDb01Check01(dbCopyEmbeddedDerbyResource);
		assertFalse("Post backup change should not exist here", hasPostBackupDbChanges(dbCopyEmbeddedDerbyResource));

		// Online backup again not waiting for transactions the DB
		dbCopyEmbeddedDerbyResource.backupLiveDatabase(backupBase, false, false, false);
		final File dbBackupDir2 = new File(backupBase, DB_NAME_COPY);

		// Change the copy DB
		applyPostBackupChangesAndVerify(dbCopyEmbeddedDerbyResource);

		// Shut down the DB
		closeEmbeddedDerbyResource(dbCopyEmbeddedDerbyResource);
		DerbyUtils.shutdownDerbySystemQuitely(true);
		return dbBackupDir2;
	}

	private void inMemoryDbCreateFromSecondBackup (final DirDerbyHomeDbInfo originDbInfo, final File dbBackupDir2)
			throws IOException, SQLException {
		// Start a new DB with memory protocol
		final DerbyResourceConfig derbyResourceConfigMemoryCreateFrom = DerbyResourceConfig.buildDefault()
				.useInMemoryDatabase(DB_NAME_COPY).createDatabaseFrom(dbBackupDir2);
		final EmbeddedDerbyResource dbCopyEmbeddedDerbyMemoryResource = new EmbeddedDerbyResource(
				derbyResourceConfigMemoryCreateFrom, originDbInfo.getDerbySystemHome());
		dbCopyEmbeddedDerbyMemoryResource.start();

		// Make sure it started up and does not have the new data
		simpleDb01Check01(dbCopyEmbeddedDerbyMemoryResource);
		assertFalse("Post backup change should not exist here",
				hasPostBackupDbChanges(dbCopyEmbeddedDerbyMemoryResource));

		// Shut down the DB
		closeEmbeddedDerbyResource(dbCopyEmbeddedDerbyMemoryResource);
		DerbyUtils.shutdownDerbySystemQuitely(true);
	}

	private void restoreOriginalDatabase (final DirDerbyHomeDbInfo originDbInfo, final File originDbBackupDir)
			throws IOException, SQLException {
		// Start a new DB with restore From...
		final DerbyResourceConfig derbyResourceConfigRestoreFrom = DerbyResourceConfig.buildDefault()
				.useDatabaseInDirectory(DB_NAME_ORIGIN).restoreDatabaseFrom(originDbBackupDir);
		final EmbeddedDerbyResource dbRestoreEmbeddedDerbyResource = new EmbeddedDerbyResource(
				derbyResourceConfigRestoreFrom, originDbInfo.getDerbySystemHome());
		dbRestoreEmbeddedDerbyResource.start();

		// Make sure it started up and does not have the new data
		simpleDb01Check01(dbRestoreEmbeddedDerbyResource);
		assertFalse("Post backup change should not exist here", hasPostBackupDbChanges(dbRestoreEmbeddedDerbyResource));

		// Shut down the DB
		closeEmbeddedDerbyResource(dbRestoreEmbeddedDerbyResource);
		DerbyUtils.shutdownDerbySystemQuitely(true);
	}
}
