/* 
 * Copyright 2015 JUnit Helper Contributors
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
package io.github.bindul.junithelper.derby.memory.simple01;

import static org.junit.Assert.*;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TemporaryFolder;

import io.github.bindul.junithelper.derby.DerbyResourceConfig;
import io.github.bindul.junithelper.derby.EmbeddedDerbyResource;

/**
 * @author Bindul Bhowmik
 *
 */
public class SimpleInMemoryDatabaseTest {
	
	private static final String DB_NAME = "my-test-database-simple01";
	
	public TemporaryFolder tempFolder = new TemporaryFolder();
	public EmbeddedDerbyResource embeddedDerbyResource = new EmbeddedDerbyResource(DerbyResourceConfig.buildDefault()
				.setDatabaseName(DB_NAME)
				.addPostInitScript("classpath:/io/github/bindul/junithelper/derby/memory/simple01/ddl.sql")
				.addPostInitScript("classpath:/io/github/bindul/junithelper/derby/memory/simple01/dml.sql"), 
			tempFolder);
	
	@Rule
	public RuleChain derbyRuleChain = RuleChain.outerRule(tempFolder).around(embeddedDerbyResource);

	@Test
	public void testSimpleInMemoryDatabase() throws SQLException {
		// Make sure derby loads up
		String jdbcUrl = embeddedDerbyResource.getJdbcUrl();
		assertNotNull(jdbcUrl);
		assertTrue(jdbcUrl.contains(DB_NAME));
		
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
			
			assertTrue(rs.next());
			assertEquals("john.doe@example.com", rs.getString("EMAIL"));
		
		} finally {
			closeQuitely(rs);
			closeQuitely(stmt);
			closeQuitely(connection);
		}
		
		File logFile = new File(embeddedDerbyResource.getDerbySystemHome(),  "derby.log");
		assertTrue(logFile.exists());
	}

	private void closeQuitely (AutoCloseable resource) {
		try {
			if (null != resource) {
				resource.close();
			}
		} catch (Exception e) {
			// Ignore
		}
	}
}
