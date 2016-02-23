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

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.deventropy.junithelper.derby.AbstractEmbeddedDerbyResourceTest;
import org.deventropy.junithelper.derby.DerbyScriptRunner;
import org.deventropy.junithelper.derby.DerbyUtils;
import org.deventropy.junithelper.derby.EmbeddedDerbyResource;

/**
 * @author Bindul Bhowmik
 */
public abstract class AbstractEmbeddedDerbyBackupTest extends AbstractEmbeddedDerbyResourceTest {

	/**
	 * Apply the post backup changes script to the Derby Resource, then verify the changes.
	 * 
	 * @param derbyResource The derby resource to apply the changes to
	 * @throws SQLException SQL exception running script
	 * @throws IOException IO exception reading script
	 */
	protected void applyPostBackupChangesAndVerify (final EmbeddedDerbyResource derbyResource)
			throws SQLException, IOException {
		Connection connection = null;
		try {
			connection = derbyResource.createConnection();
			final DerbyScriptRunner originDerbyScriptRunner = new DerbyScriptRunner(connection);
			originDerbyScriptRunner
					.executeScript("classpath:/org/deventropy/junithelper/derby/simple01/dml-post-bkp.sql");
			assertTrue("Post backup change should exist here", hasPostBackupDbChanges(connection));
		} finally {
			DerbyUtils.closeQuietly(connection);
		}
	}
	
	/**
	 * Check if the post backup script content exists.
	 * @param derbyResource The derby resource to check
	 * @return <code>true</code> if the data exists
	 * @throws SQLException Error running SQL
	 */
	protected boolean hasPostBackupDbChanges (final EmbeddedDerbyResource derbyResource) throws SQLException {
		Connection connection = null;
		try {
			connection = derbyResource.createConnection();
			return hasPostBackupDbChanges(connection);
		} finally {
			DerbyUtils.closeQuietly(connection);
		}
	}
	
	/**
	 * Check if the post backup script content exists.
	 * @param connection The connection to check
	 * @return <code>true</code> if the data exists
	 * @throws SQLException Error running SQL
	 */
	protected boolean hasPostBackupDbChanges (final Connection connection) throws SQLException {
		boolean hasPostBackupChanges = false;

		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			// Check a value
			stmt = connection.prepareStatement("SELECT * FROM PEOPLE WHERE PERSON = ?");
			stmt.setString(1, "Jane Doe");
			rs = stmt.executeQuery();

			if (rs.next()) {
				hasPostBackupChanges = "jane.doe@example.com".equals(rs.getString("EMAIL"));
			}

		} finally {
			DerbyUtils.closeQuietly(rs);
			DerbyUtils.closeQuietly(stmt);
		}

		return hasPostBackupChanges;
	}
}
