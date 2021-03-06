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
import java.sql.SQLException;

import org.deventropy.junithelper.derby.DerbyResourceConfig;
import org.deventropy.junithelper.derby.DerbyUtils;
import org.deventropy.junithelper.derby.EmbeddedDerbyResource;
import org.junit.AfterClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TemporaryFolder;

/**
 * @author Bindul Bhowmik
 *
 */
public class DatabaseDbTempDirDirTest {
	
	private static final String DB_NAME = "test-db-dir-simple01-tmpfolder-dirtest-nulllog";
	
	private TemporaryFolder tempFolder = new TemporaryFolder();
	private EmbeddedDerbyResource embeddedDerbyResource =
		new EmbeddedDerbyResource(DerbyResourceConfig.buildDefault().useDatabaseInDirectory(DB_NAME)
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
	public void testSimpleDirectory () throws SQLException {
		final String databasePath = embeddedDerbyResource.getDatabasePath();
		final File derbySystemHome = embeddedDerbyResource.getDerbySystemHome();
		final File databaseDirectory = new File(derbySystemHome, databasePath);

		assertTrue("Database directory should exist", databaseDirectory.exists());
		assertTrue("Database directory should be a directory", databaseDirectory.isDirectory());

		// Database segment directory
		final File seg0Directory = new File(databaseDirectory, "seg0");
		assertTrue("Database segment directory should exist and a directory",
				seg0Directory.exists() && seg0Directory.isDirectory());

		// Database lock file exists
		final File dbLckFile = new File(databaseDirectory, "db.lck");
		assertTrue("Database lock file exists", dbLckFile.exists() && !dbLckFile.isDirectory());
	}

}
