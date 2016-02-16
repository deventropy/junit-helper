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
package org.deventropy.junithelper.derby.memory.simple01;

import static org.junit.Assert.*;

import java.io.File;
import java.sql.SQLException;

import org.deventropy.junithelper.derby.AbstractEmbeddedDerbyResourceTest;
import org.deventropy.junithelper.derby.DerbyResourceConfig;
import org.deventropy.junithelper.derby.DerbyUtils;
import org.deventropy.junithelper.derby.EmbeddedDerbyResource;
import org.junit.AfterClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TemporaryFolder;

import net.jcip.annotations.NotThreadSafe;

/**
 * @author Bindul Bhowmik
 */
@NotThreadSafe
public class InMemoryDbTempDirNullLoggingTest extends AbstractEmbeddedDerbyResourceTest {
	
	private static final String DB_NAME = "my-test-database-simple01-tmpfolder";
	
	private TemporaryFolder tempFolder = new TemporaryFolder();
	private EmbeddedDerbyResource embeddedDerbyResource =
		new EmbeddedDerbyResource(DerbyResourceConfig.buildDefault().useInMemoryDatabase(DB_NAME)
			.useDevNullErrorLogging()
			.addPostInitScript("classpath:/org/deventropy/junithelper/derby/simple01/ddl.sql")
			.addPostInitScript("classpath:/org/deventropy/junithelper/derby/simple01/dml.sql"),
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
	public void testSimpleInMemoryDatabase () throws SQLException {
		// Make sure derby loads up
		final String jdbcUrl = embeddedDerbyResource.getJdbcUrl();
		assertNotNull(jdbcUrl);
		assertTrue(jdbcUrl.contains(DB_NAME));

		simpleDb01Check01(jdbcUrl);

		// This test will fail in eclipse; see https://bugs.eclipse.org/bugs/show_bug.cgi?id=298061
		final File logFile = new File(embeddedDerbyResource.getDerbySystemHome(),  "derby.log");
		assertFalse(logFile.exists());
	}
}

