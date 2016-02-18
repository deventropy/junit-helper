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

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

import org.deventropy.junithelper.derby.AbstractEmbeddedDerbyResourceTest;
import org.deventropy.junithelper.derby.DerbyResourceConfig;
import org.deventropy.junithelper.derby.EmbeddedDerbyResource;
import org.junit.rules.TemporaryFolder;

/**
 * @author Bindul Bhowmik
 *
 */
public abstract class AbstractClasspathEmbeddedDerbyResourceTest extends AbstractEmbeddedDerbyResourceTest {

	/**
	 * Creates and starts an embedded classpath DB at the given DB path.
	 * 
	 * @param tempFolder The temporary folder to use as a parent for Derby Home
	 * @param dbPath The database path in the classpath
	 * @return The created embedded derby resource
	 * @throws IOException Error with a file operation
	 * @throws SQLException Error with a SQL operation
	 */
	protected EmbeddedDerbyResource setupClasspathDb (final TemporaryFolder tempFolder, final String dbPath)
			throws IOException, SQLException {
		final File derbySystemHomeDir = tempFolder.newFolder();
		final DerbyResourceConfig derbyResourceConfig = DerbyResourceConfig.buildDefault()
				.useClasspathSubSubProtocol(dbPath);
		final EmbeddedDerbyResource embeddedDerbyResource = new EmbeddedDerbyResource(derbyResourceConfig,
				derbySystemHomeDir);
		embeddedDerbyResource.start();
		return embeddedDerbyResource;
	}
	
}
