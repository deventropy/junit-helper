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
package org.deventropy.junithelper.derby.directory.simple01;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.deventropy.junithelper.derby.AbstractEmbeddedDerbyResourceTest;
import org.deventropy.junithelper.derby.DerbyResourceConfig;
import org.deventropy.junithelper.derby.DerbyUtils;
import org.deventropy.junithelper.derby.EmbeddedDerbyResource;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Bindul Bhowmik
 *
 */
public class DirectoryDbCustomDirAllTest extends AbstractEmbeddedDerbyResourceTest {
	
	private static final String DB_NAME = "test-database-directory-test01-customdirectory";
	private Logger log = LogManager.getFormatterLogger();
	private EmbeddedDerbyResource embeddedDerbyResource;
	private File derbySystemHomeDir;
	private File dbHomeDir;
	
	/**
	 * Sets up parameters for the test.
	 * @throws IOException Error with file operations
	 * @throws SQLException Error starting the DB
	 */
	@Before
	public void beforeTest () throws IOException, SQLException {
		derbySystemHomeDir = File.createTempFile("junit-helper-derby", Long.toString(System.nanoTime()));
		derbySystemHomeDir.delete();
		derbySystemHomeDir.mkdir();

		dbHomeDir = File.createTempFile(DB_NAME, Long.toString(System.nanoTime()));
		dbHomeDir.delete();

		final DerbyResourceConfig derbyResourceConfig = DerbyResourceConfig.buildDefault()
				.useDatabaseInDirectory(dbHomeDir.getAbsolutePath())
				.addPostInitScript("classpath:/org/deventropy/junithelper/derby/simple01/ddl.sql")
				.addPostInitScript("classpath:/org/deventropy/junithelper/derby/simple01/dml.sql");
		embeddedDerbyResource = new EmbeddedDerbyResource(derbyResourceConfig, derbySystemHomeDir);
		embeddedDerbyResource.start();
	}

	@Test
	public void testCustomDirectoryDatabase () throws SQLException {
		// Make sure derby loads up
		final String jdbcUrl = embeddedDerbyResource.getJdbcUrl();
		assertNotNull(jdbcUrl);
		assertTrue(jdbcUrl.contains(DB_NAME));

		simpleDb01Check01(embeddedDerbyResource);

		final File logFile = new File(embeddedDerbyResource.getDerbySystemHome(),  "derby.log");
		assertTrue(logFile.exists());
	}

	/**
	 * Cleans up the resource and database.
	 * @throws IOException Exception with file operations
	 */
	@After
	public void afterTest () {
		try {
			closeEmbeddedDerbyResource(embeddedDerbyResource);
		} catch (IOException e) {
			log.catching(Level.TRACE, e);
		}
		try {
			FileUtils.deleteDirectory(dbHomeDir);
		} catch (IOException e) {
			try {
				FileUtils.forceDeleteOnExit(dbHomeDir);
			} catch (IOException e1) {
				log.catching(Level.TRACE, e1);
			}
		}
		try {
			FileUtils.deleteDirectory(derbySystemHomeDir);
		} catch (IOException e) {
			try {
				FileUtils.forceDeleteOnExit(derbySystemHomeDir);
			} catch (IOException e1) {
				log.catching(Level.TRACE, e1);
			}
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
