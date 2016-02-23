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
package org.deventropy.junithelper.derby;

import java.io.File;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;

import org.deventropy.shared.utils.ArgumentCheck;

/**
 * Helper class used by the {@link EmbeddedDerbyResource} to implement online backup operations on the instance.
 * 
 * @author Bindul Bhowmik
 */
public class DerbyBackupOperationsHelper {
	
	private final EmbeddedDerbyResource embeddedDerbyResource;
	
	/**
	 * Initializes the backup operations helper with the derby resource.
	 * @param embeddedDerbyResource The derby resource on which backup operations will be invoked.
	 */
	public DerbyBackupOperationsHelper (final EmbeddedDerbyResource embeddedDerbyResource) {
		this.embeddedDerbyResource = embeddedDerbyResource;
	}

	/**
	 * Perform an online backup of the running instance. The online backup uses either the
	 * <code>SYSCS_UTIL.SYSCS_BACKUP_DATABASE</code> if <code>enableArchiveLogging</code> is set to <code>false</code>
	 * or <code>SYSCS_UTIL.SYSCS_BACKUP_DATABASE_AND_ENABLE_LOG_ARCHIVE_MODE</code> otherwise. If the
	 * <code>waitForTransactions</code> parameter is set to <code>false</code> the <code>_NOWAIT</code> versions of the
	 * procedures are used.
	 * 
	 * <p>For more information on backing up Derby database, see
	 * <a href="http://db.apache.org/derby/docs/10.12/adminguide/cadminhubbkup01.html">Using the backup procedures to
	 * perform an online backup</a> in the Derby Administrators guide.
	 * 
	 * @param backupDir The directory to which the database should be backed up.
	 * @param waitForTransactions Wait for running transactions to complete.
	 * @param enableArchiveLogging If archive logging should be enabled for the database.
	 * @param deleteArchivedLogs Ask Derby to delete the old archive logs after the backup is successful.
	 * @throws SQLException Exception from Derby when the backup fails.
	 */
	public void backupLiveDatabase (final File backupDir, final boolean waitForTransactions,
			final boolean enableArchiveLogging, final boolean deleteArchivedLogs) throws SQLException {
		ArgumentCheck.notNull(backupDir, "Backup directory");
		if (enableArchiveLogging) {
			backupLiveDatabaseArchiveLog(backupDir, waitForTransactions, deleteArchivedLogs);
		} else {
			backupLiveDatabaseNoArchiveLog(backupDir, waitForTransactions);
		}
	}
	
	private void backupLiveDatabaseNoArchiveLog (final File backupDir, final boolean waitForTransactions)
			throws SQLException {
		Connection connection = null;
		CallableStatement callableStatement = null;

		try {
			connection = embeddedDerbyResource.createConnection();
			callableStatement = connection.prepareCall(
					waitForTransactions ? DerbyConstants.SYSPROC_BACKUP_DB : DerbyConstants.SYSPROC_BACKUP_DB_NOWAIT);
			callableStatement.setString(1, backupDir.getAbsolutePath());
			callableStatement.execute();
		} finally {
			DerbyUtils.closeQuietly(callableStatement);
			DerbyUtils.closeQuietly(connection);
		}
	}
	
	private void backupLiveDatabaseArchiveLog (final File backupDir, final boolean waitForTransactions,
			final boolean deleteArchivedLogs) throws SQLException {
		Connection connection = null;
		CallableStatement callableStatement = null;

		try {
			connection = embeddedDerbyResource.createConnection();
			callableStatement = connection
					.prepareCall(waitForTransactions ? DerbyConstants.SYSPROC_BACKUP_DB_ENABLE_LOG_ARCHIVE
							: DerbyConstants.SYSPROC_BACKUP_DB_ENABLE_LOG_ARCHIVE_NOWAIT);
			callableStatement.setString(1, backupDir.getAbsolutePath());
			callableStatement.setInt(2, deleteArchivedLogs ? 1 : 0);
			callableStatement.execute();
		} finally {
			DerbyUtils.closeQuietly(callableStatement);
			DerbyUtils.closeQuietly(connection);
		}
	}
}
