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
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.deventropy.shared.utils.DirectoryArchiverUtil;
import org.junit.rules.TemporaryFolder;

/**
 * Base class with shared methods for some checks.
 * 
 * @author Bindul Bhowmik
 */
public abstract class AbstractEmbeddedDerbyResourceTest {

	/**
	 * Runs simple checks on the contents of the database; based on SimpeDb01 setup.
	 * @param jdbcUrl The JDBC url
	 * @throws SQLException SQL exception running the checks
	 */
	protected void simpleDb01Check01 (final String jdbcUrl) throws SQLException {
		Connection connection = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			connection = DriverManager.getConnection(jdbcUrl);
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

		final File dbHomeDir = createAndShutdownDbInDirSimple01(tempFolder, dirDatabaseName);

		// Create a Jar file nesting it down a folder.
		final File dbArchiveFile = tempFolder.newFile(dirDatabaseName + ".jar");
		DirectoryArchiverUtil.createJarArchiveOfDirectory(dbArchiveFile.getAbsolutePath(), dbHomeDir, jarDatabasePath);
		return dbArchiveFile;
	}

	/**
	 * Create a test database, with scripts for SIMPLE01. And shut down Derby, but do not delete the directory.
	 * 
	 * @param tempFolder Parent temporary folder under which other files and folders are created.
	 * @param dirDatabaseName The path/name of the database directory
	 * @return File reference to the directory where the database is.
	 * @throws IOException Error with a file operation
	 * @throws SQLException Error with a SQL operation
	 */
	protected File createAndShutdownDbInDirSimple01 (final TemporaryFolder tempFolder, final String dirDatabaseName)
			throws IOException, SQLException {
		final File derbySystemHomeDir = tempFolder.newFolder();

		final File dbHomeDir = tempFolder.newFolder(dirDatabaseName);
		dbHomeDir.delete();

		final DerbyResourceConfig derbyResourceConfig = DerbyResourceConfig.buildDefault()
				.useDatabaseInDirectory(dbHomeDir.getAbsolutePath())
				.addPostInitScript("classpath:/org/deventropy/junithelper/derby/simple01/ddl.sql")
				.addPostInitScript("classpath:/org/deventropy/junithelper/derby/simple01/dml.sql");
		final EmbeddedDerbyResource embeddedDerbyResource = new EmbeddedDerbyResource(derbyResourceConfig,
			derbySystemHomeDir);
		embeddedDerbyResource.start();
		embeddedDerbyResource.close();

		// Shutdown Derby
		DerbyUtils.shutdownDerbySystemQuitely(true);
		return dbHomeDir;
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

}
