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

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.deventropy.shared.utils.DirectoryArchiverUtil;
import org.junit.AfterClass;
import org.junit.rules.TemporaryFolder;

/**
 * Base class with shared methods for some checks.
 * 
 * @author Bindul Bhowmik
 */
public abstract class AbstractEmbeddedDerbyResourceTest {

	/**
	 * Runs simple checks on the contents of the database; based on SimpeDb01 setup.
	 * @param embeddedDerbyResource The derby resource
	 * @throws SQLException SQL exception running the checks
	 */
	protected void simpleDb01Check01 (final EmbeddedDerbyResource embeddedDerbyResource) throws SQLException {
		Connection connection = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			connection = embeddedDerbyResource.createConnection();
			assertNotNull(connection);
	
			// Check a value
			stmt = connection.prepareStatement("SELECT * FROM PEOPLE WHERE PERSON = ?");
			stmt.setString(1, "John Doe");
			rs = stmt.executeQuery();

			if (rs.next()) {
				assertEquals("john.doe@example.com", rs.getString("EMAIL"));
			} else {
				fail("Should have had at least one row");
			}

		} finally {
			DerbyUtils.closeQuietly(rs);
			DerbyUtils.closeQuietly(stmt);
			DerbyUtils.closeQuietly(connection);
		}
	}
	
	/**
	 * Create a database in a jar. Essentially, create a database in a directory, set up tables and data, shut down
	 * Derby and create a jar file in the desired path.
	 * 
	 * @param tempFolder Parent temporary folder under which other files and folders are created.
	 * @param dirDatabaseName The path/name of the database directory
	 * @param jarDatabasePath The path in the jar where the archive is created
	 * @return File reference to the jar.
	 * @throws IOException Error with a file operation
	 * @throws SQLException Error with a SQL operation
	 */
	protected File createAndShutdownDbInJarSimple01 (final TemporaryFolder tempFolder, final String dirDatabaseName,
			final String jarDatabasePath) throws IOException, SQLException {

		final DirDerbyHomeDbInfo dirDerbyHomeDbInfo = createAndShutdownDbInDirSimple01(tempFolder, dirDatabaseName,
				false);

		// Create a Jar file nesting it down a folder.
		final File dbArchiveFile = tempFolder.newFile(dirDatabaseName + ".jar");
		DirectoryArchiverUtil.createJarArchiveOfDirectory(dbArchiveFile.getAbsolutePath(),
				dirDerbyHomeDbInfo.getDbDirectory(), jarDatabasePath);
		return dbArchiveFile;
	}

	/**
	 * Create a test database, with scripts for SIMPLE01. And shut down Derby, but do not delete the directory.
	 * 
	 * @param tempFolder Parent temporary folder under which other files and folders are created.
	 * @param dirDatabaseName The path/name of the database directory
	 * @param createDbInDerbyHome If the DB should be created as a child of the Derby system home
	 * @return Directory Derby Info structure
	 * @throws IOException Error with a file operation
	 * @throws SQLException Error with a SQL operation
	 */
	protected DirDerbyHomeDbInfo createAndShutdownDbInDirSimple01 (final TemporaryFolder tempFolder,
			final String dirDatabaseName, final boolean createDbInDerbyHome) throws IOException, SQLException {

		final DirDerbyHomeDbInfo dirDerbyHomeDbInfo = createAndStartEmbeddedDerbyResourceSimple01(tempFolder,
				dirDatabaseName, createDbInDerbyHome);
		closeEmbeddedDerbyResource(dirDerbyHomeDbInfo.getEmbeddedDerbyResource());

		dirDerbyHomeDbInfo.setEmbeddedDerbyResource(null);

		// Shutdown Derby
		DerbyUtils.shutdownDerbySystemQuitely(true);
		return dirDerbyHomeDbInfo;
	}
	
	/**
	 * Create and start an Embedded Derby Resource with the Simple 01 scripts.
	 * @param tempFolder The temporary folder provider
	 * @param dirDatabaseName The directory name
	 * @param createDbInDerbyHome Should the DB be created in the Derby Home directory or a random directory
	 * @return Directory Derby Info structure
	 * @throws IOException Error with a file operation
	 * @throws SQLException Error with a SQL operation
	 */
	protected DirDerbyHomeDbInfo createAndStartEmbeddedDerbyResourceSimple01 (final TemporaryFolder tempFolder,
			final String dirDatabaseName, final boolean createDbInDerbyHome) throws IOException, SQLException {
		final File derbySystemHomeDir = tempFolder.newFolder();

		File dbHomeDir = null;
		if (createDbInDerbyHome) {
			dbHomeDir = new File(derbySystemHomeDir, dirDatabaseName);
		} else {
			dbHomeDir = tempFolder.newFolder(dirDatabaseName);
			dbHomeDir.delete();
		}

		final DerbyResourceConfig derbyResourceConfig = DerbyResourceConfig.buildDefault()
				.useDatabaseInDirectory(createDbInDerbyHome ? dirDatabaseName : dbHomeDir.getAbsolutePath())
				.addPostInitScript("classpath:/org/deventropy/junithelper/derby/simple01/ddl.sql")
				.addPostInitScript("classpath:/org/deventropy/junithelper/derby/simple01/dml.sql");
		final EmbeddedDerbyResource embeddedDerbyResource = new EmbeddedDerbyResource(derbyResourceConfig,
			derbySystemHomeDir);
		embeddedDerbyResource.start();

		final DirDerbyHomeDbInfo dirDerbyHomeDbInfo = new DirDerbyHomeDbInfo(derbySystemHomeDir, dbHomeDir,
				dirDatabaseName);
		dirDerbyHomeDbInfo.setEmbeddedDerbyResource(embeddedDerbyResource);

		return dirDerbyHomeDbInfo;
	}

	/**
	 * Closes an embedded derby resource if it is not null. Should not be called if using the resource as a JUnit Rule.
	 * @param embeddedDerbyResource The resource to close
	 * @throws IOException Exception closing the resource
	 */
	protected void closeEmbeddedDerbyResource (final EmbeddedDerbyResource embeddedDerbyResource) throws IOException {
		if (null != embeddedDerbyResource) {
			embeddedDerbyResource.close();
		}
	}
	
	/**
	 * Cleanup stuff.
	 */
	@AfterClass
	public static void cleanupDerbySystem () {
		// Cleanup for next test
		DerbyUtils.shutdownDerbySystemQuitely(true);
	}

	/**
	 * An internal structure to share multiple folders and database info.
	 * 
	 * @author Bindul Bhowmik
	 */
	protected class DirDerbyHomeDbInfo {
		private final File derbySystemHome;
		private final File dbDirectory;
		private final String dbName;
		private EmbeddedDerbyResource embeddedDerbyResource;

		/**
		 * A new DirDerbyHomeDbInfo instance.
		 * 
		 * @param derbySystemHome The derby system home
		 * @param dbDirectory The directory where the DB exists
		 * @param dbName The DB name.
		 */
		protected DirDerbyHomeDbInfo (final File derbySystemHome, final File dbDirectory, final String dbName) {
			this.derbySystemHome = derbySystemHome;
			this.dbDirectory = dbDirectory;
			this.dbName = dbName;
		}

		/**
		 * @return the derbySystemHome
		 */
		public File getDerbySystemHome () {
			return derbySystemHome;
		}

		/**
		 * @return the dbDirectory
		 */
		public File getDbDirectory () {
			return dbDirectory;
		}

		/**
		 * @return the dbName
		 */
		public String getDbName () {
			return dbName;
		}

		/**
		 * @return the embeddedDerbyResource
		 */
		public EmbeddedDerbyResource getEmbeddedDerbyResource () {
			return embeddedDerbyResource;
		}

		/**
		 * @param embeddedDerbyResource the embeddedDerbyResource to set
		 */
		public void setEmbeddedDerbyResource (final EmbeddedDerbyResource embeddedDerbyResource) {
			this.embeddedDerbyResource = embeddedDerbyResource;
		}
	}
}
