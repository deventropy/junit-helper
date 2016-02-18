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

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

import org.deventropy.junithelper.derby.AbstractEmbeddedDerbyResourceTest;
import org.deventropy.junithelper.derby.DerbyResourceConfig;
import org.deventropy.junithelper.derby.EmbeddedDerbyResource;
import org.junit.rules.TemporaryFolder;

/**
 * @author Bindul Bhowmik
 */
public abstract class AbstractJarEmbeddedDerbyResourceTest extends AbstractEmbeddedDerbyResourceTest {

	/**
	 * Creates an EmbeddedDerbyResource with Jar sub-sub protocol and starts it.
	 * 
	 * @param tempFolder The temporary folder to use as a parent for Derby Home
	 * @param dbArchiveFile The Jar file with the database
	 * @param jarDatabasePath The database path in the jar
	 * @return The created embedded derby resource
	 * @throws IOException Error with a file operation
	 * @throws SQLException Error with a SQL operation
	 */
	protected EmbeddedDerbyResource createAndstartJarDerbyResource (final TemporaryFolder tempFolder,
			final File dbArchiveFile, final String jarDatabasePath) throws IOException, SQLException {

		final File derbySystemHomeDir = tempFolder.newFolder();
		final DerbyResourceConfig derbyResourceConfig = DerbyResourceConfig.buildDefault()
				.useJarSubSubProtocol(dbArchiveFile.getAbsolutePath(), jarDatabasePath);
		final EmbeddedDerbyResource embeddedDerbyResource = new EmbeddedDerbyResource(derbyResourceConfig,
				derbySystemHomeDir);
		embeddedDerbyResource.start();
		return embeddedDerbyResource;
	}
}
