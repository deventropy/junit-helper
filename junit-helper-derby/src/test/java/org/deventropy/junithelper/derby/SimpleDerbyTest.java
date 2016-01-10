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

import static org.junit.Assert.assertTrue;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.junit.AfterClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TemporaryFolder;

/**
 * @author Bindul Bhowmik
 */
public class SimpleDerbyTest {
	
	private TemporaryFolder tempFolder = new TemporaryFolder();
	private EmbeddedDerbyResource embeddedDerbyResource =
		new EmbeddedDerbyResource(DerbyResourceConfig.buildDefault()
			.useDevNullErrorLogging(),
		tempFolder);
	
	@Rule
	public RuleChain derbyRuleChain = RuleChain.outerRule(tempFolder).around(embeddedDerbyResource);
	
	/**
	 * Cleanup stuff.
	 */
	@AfterClass
	public static void cleanupDerbySystem () {
		// Cleanup for next test
		DerbyUtils.shutdownDerbySystemQuitely(true);
	}

	@Test
	public void test () throws SQLException {
		final String jdbcUrl = embeddedDerbyResource.getJdbcUrl();
		Connection connection = null;
		Statement stmt = null;
		ResultSet rs = null;

		try {
			connection = DriverManager.getConnection(jdbcUrl);
	
			// Check a value
			stmt = connection.createStatement();
			rs = stmt.executeQuery("SELECT 1 FROM SYSIBM.SYSDUMMY1");
	
			assertTrue(rs.next());
		} finally {
			DerbyUtils.closeQuietly(rs);
			DerbyUtils.closeQuietly(stmt);
			DerbyUtils.closeQuietly(connection);
		}
	}

}
