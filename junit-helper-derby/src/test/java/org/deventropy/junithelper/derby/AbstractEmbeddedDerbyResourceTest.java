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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

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

		try {
			connection = DriverManager.getConnection(jdbcUrl);
			assertNotNull(connection);
	
			// Check a value
			stmt = connection.prepareStatement("SELECT * FROM PEOPLE WHERE PERSON = ?");
			stmt.setString(1, "John Doe");
			final ResultSet rs = stmt.executeQuery();
	
			assertTrue(rs.next());
			assertEquals("john.doe@example.com", rs.getString("EMAIL"));
			DerbyUtils.closeQuietly(rs);
		} finally {
			DerbyUtils.closeQuietly(stmt);
			DerbyUtils.closeQuietly(connection);
		}
	}
}
