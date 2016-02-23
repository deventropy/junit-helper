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
package org.deventropy.junithelper.derby.classpath.simple01;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.SQLException;

import org.apache.commons.io.FileUtils;
import org.deventropy.junithelper.derby.EmbeddedDerbyResource;
import org.deventropy.shared.utils.ClassUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * @author Bindul Bhowmik
 *
 */
public class ClasspathDbFromDirTest extends AbstractClasspathEmbeddedDerbyResourceTest {
	
	private static final String DB_NAME = "test-database-classpath-test02-customdirectory";
	
	private static final String CLASSPATH_DB_NAME = "test/database/classpath-test02";
	
	@Rule
	public TemporaryFolder tempFolder = new TemporaryFolder();
	
	/**
	 * This is currently stored as a field variable as there is only a single test method, if there were more we
	 * probably need to move them to a ThreadLocal storage.
	 */
	private ClassLoader parentLoader;
	
	/**
	 * Setup the database.
	 * @throws Exception error setting up database
	 */
	@Before
	public void createDbAndSetupClasspath () throws Exception {

		// Create the directory with the DB
		final DirDerbyHomeDbInfo dirDerbyHomeDbInfo = createAndShutdownDbInDirSimple01(tempFolder, DB_NAME, false);
		final File cpDirectory = tempFolder.newFolder();
		final File dbDirectory = new File(cpDirectory.getAbsolutePath() + File.separator + CLASSPATH_DB_NAME
				+ File.separator);
		FileUtils.copyDirectory(dirDerbyHomeDbInfo.getDbDirectory(), dbDirectory);

		parentLoader = ClassUtil.getApplicableClassloader(this);
		final URLClassLoader contextClassLoader = new URLClassLoader(new URL[]{cpDirectory.toURI().toURL()},
				parentLoader);
		Thread.currentThread().setContextClassLoader(contextClassLoader);
	}

	@Test
	public void testSimpleClasspathDatabaseFromDir () throws IOException, SQLException {

		EmbeddedDerbyResource embeddedDerbyResource = null;

		try {
			embeddedDerbyResource = setupClasspathDb(tempFolder, CLASSPATH_DB_NAME);

			// Make sure derby loads up
			final String jdbcUrl = embeddedDerbyResource.getJdbcUrl();
			assertNotNull(jdbcUrl);
			assertTrue(jdbcUrl.contains(CLASSPATH_DB_NAME));

			simpleDb01Check01(embeddedDerbyResource);
		} finally {
			closeEmbeddedDerbyResource(embeddedDerbyResource);
		}
	}
	
	/**
	 * Clean up.
	 * @throws Exception is not thrown.
	 */
	@After
	public void cleanupAfterClasspathDbTest () throws Exception {
		Thread.currentThread().setContextClassLoader(parentLoader);
	}
}
