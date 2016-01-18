/* 
 * Copyright 2015 Development Entropy (deventropy.org) Contributors
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

import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.apache.commons.io.output.WriterOutputStream;
import org.junit.AfterClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TemporaryFolder;

/**
 * @author Bindul Bhowmik
 *
 */
public class SimpleDerbyScriptRunnerTest {
	
private static final String DB_NAME = "my-test-database-simple01-tmpfolder-nulllog";
	
	private TemporaryFolder tempFolder = new TemporaryFolder();
	private EmbeddedDerbyResource embeddedDerbyResource =
		new EmbeddedDerbyResource(DerbyResourceConfig.buildDefault().useInMemoryDatabase(DB_NAME),
		tempFolder);
	
	@Rule
	public RuleChain derbyRuleChain = RuleChain.outerRule(tempFolder).around(embeddedDerbyResource);
	
//	private Logger log = LogManager.getLogger();
	
	/**
	 * Cleanup stuff.
	 */
	@AfterClass
	public static void cleanupDerbySystem () {
		// Cleanup for next test
		DerbyUtils.shutdownDerbySystemQuitely(false);
	}

	@Test
	public void testLoggingChanges () throws SQLException, IOException {

		final StringWriter logData1 = new StringWriter();
		final WriterOutputStream wos1 = new WriterOutputStream(logData1, Charset.defaultCharset());

		final StringWriter logData2 = new StringWriter();
		final WriterOutputStream wos2 = new WriterOutputStream(logData2, Charset.defaultCharset());

		Connection connection = null;

		try {
			connection = DriverManager.getConnection(embeddedDerbyResource.getJdbcUrl());
			final DerbyScriptRunner scriptRunner = new DerbyScriptRunner(connection);

			scriptRunner.setDefaultScriptLogStream(wos1);

			// Run one with a output stream here
			scriptRunner.executeScript("classpath:/org/deventropy/junithelper/derby/simple01/ddl.sql",
					wos2, true);
			logData2.flush();
			assertTrue("Expected log doesnt exist", logData2.toString().contains("rows inserted/updated/deleted"));
			assertTrue("Should have nothing in the default", logData1.toString().isEmpty());

			scriptRunner.executeScript("classpath:/org/deventropy/junithelper/derby/simple01/dml.sql");

			wos1.flush();
			logData1.flush();

			assertTrue("Expected log doesnt exist", logData1.toString().contains("row inserted/updated/deleted"));
		} finally {
			DerbyUtils.closeQuietly(connection);
		}
	}
	
	@Test(expected = UnsupportedEncodingException.class)
	public void testUnsupportedEncryption () throws SQLException, IOException {
		Connection connection = null;

		try {
			connection = DriverManager.getConnection(embeddedDerbyResource.getJdbcUrl());
			final DerbyScriptRunner scriptRunner = new DerbyScriptRunner(connection, "UTF-64");

			scriptRunner.executeScript("classpath:/org/deventropy/junithelper/derby/simple01/dml.sql");

		} finally {
			DerbyUtils.closeQuietly(connection);
		}
	}

}
