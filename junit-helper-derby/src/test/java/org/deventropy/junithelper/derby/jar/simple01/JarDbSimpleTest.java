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
package org.deventropy.junithelper.derby.jar.simple01;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

import org.deventropy.junithelper.derby.DerbyUtils;
import org.deventropy.junithelper.derby.EmbeddedDerbyResource;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * Tests a simple Jar database. For our preference not to have jar files in SCM, we create the jar DB from a directory
 * database in the <code>Before</code> method.; hence make sure there is only one test method in the class.
 * 
 * @author Bindul Bhowmik
 */
public class JarDbSimpleTest extends AbstractJarEmbeddedDerbyResourceTest {
	
	private static final String DB_NAME = "test-database-jar-test01-customdirectory";
	
	private static final String JAR_DB_NAME = "test/database/jar-test01";
	
	@Rule
	public TemporaryFolder tempFolder = new TemporaryFolder();
	
	private EmbeddedDerbyResource embeddedDerbyResource;
	
	/**
	 * Setup the database.
	 * @throws Exception error setting up database
	 */
	@Before
	public void createJarDatabase () throws Exception {
		final File dbArchiveFile = createJarDbSimple01(tempFolder, DB_NAME, JAR_DB_NAME);
		embeddedDerbyResource = createAndstartJarDerbyResource(tempFolder, dbArchiveFile, JAR_DB_NAME);
	}

	@Test
	public void testSimpleJarDatabase () throws IOException, SQLException {

		// Make sure derby loads up
		final String jdbcUrl = embeddedDerbyResource.getJdbcUrl();
		assertNotNull(jdbcUrl);
		assertTrue(jdbcUrl.contains(JAR_DB_NAME));

		simpleDb01Check01(jdbcUrl);
	}
	
	/**
	 * Shut down the database.
	 * @throws Exception error shutting down the database
	 */
	@After
	public void shutdownJarDatabase () throws Exception {
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
}
