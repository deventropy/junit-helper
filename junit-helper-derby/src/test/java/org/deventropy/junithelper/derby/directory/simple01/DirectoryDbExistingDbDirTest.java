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

import org.deventropy.junithelper.derby.AbstractEmbeddedDerbyResourceTest;
import org.deventropy.junithelper.derby.DerbyResourceConfig;
import org.deventropy.junithelper.derby.EmbeddedDerbyResource;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * @author Bindul Bhowmik
 *
 */
public class DirectoryDbExistingDbDirTest extends AbstractEmbeddedDerbyResourceTest {
	
	private static final String DB_NAME = "test-database-dir-test04-custom-existing";
	
	@Rule
	public TemporaryFolder tempFolder = new TemporaryFolder();

	@Test
	public void testStartingDbFromExistingDbDir () throws IOException, SQLException {
		final File dbPathFile = createAndShutdownDbInDirSimple01(tempFolder, DB_NAME);
		final File derbyHome = tempFolder.newFolder();
		final DerbyResourceConfig config = DerbyResourceConfig.buildDefault()
				.useDatabaseInDirectory(dbPathFile.getAbsolutePath(), true);
		final EmbeddedDerbyResource embeddedDerbyResource = new EmbeddedDerbyResource(config, derbyHome);
		embeddedDerbyResource.start();

		// Make sure derby loads up
		final String jdbcUrl = embeddedDerbyResource.getJdbcUrl();
		assertNotNull(jdbcUrl);

		simpleDb01Check01(jdbcUrl);

		// Done, shut it down
		closeEmbeddedDerbyResource(embeddedDerbyResource);
	}
}
