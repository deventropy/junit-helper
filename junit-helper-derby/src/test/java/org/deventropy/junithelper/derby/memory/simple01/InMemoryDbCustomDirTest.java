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
import java.io.IOException;
import java.sql.SQLException;

import org.deventropy.junithelper.derby.AbstractEmbeddedDerbyResourceTest;
import org.deventropy.junithelper.derby.DerbyResourceConfig;
import org.deventropy.junithelper.derby.EmbeddedDerbyResource;
import org.deventropy.junithelper.derby.util.DerbyUtils;
import org.junit.AfterClass;
import org.junit.Test;

import net.jcip.annotations.NotThreadSafe;

/**
 * @author Bindul Bhowmik
 */
@NotThreadSafe
public class InMemoryDbCustomDirTest extends AbstractEmbeddedDerbyResourceTest {
	
	private static final String DB_NAME = "my-test-database-simple01-customdirectory";
	private static final String PROP_DERBY_SYSTEM_HOME = "derby.system.home";
	
	/**
	 * Cleanup stuff.
	 */
	@AfterClass
	public static void cleanupDerbySystem () {
		// Cleanup for next test
		DerbyUtils.shutdownDerbySystemQuitely(true);
	}
	
	@Test
	public void testCustomDirectoryAndInitScript () throws IOException, SQLException {

		File tempFile = null;
		EmbeddedDerbyResource embeddedDerbyResource = null;
		final String dummyDerbySystemHome = File.createTempFile("junit-helper-derby-dummyhome",
				Long.toString(System.nanoTime())).getAbsolutePath();

		try {
			tempFile = File.createTempFile("junit-helper-derby", Long.toString(System.nanoTime()));
			tempFile.delete();
			tempFile.mkdir();

			System.setProperty(PROP_DERBY_SYSTEM_HOME, dummyDerbySystemHome);
			embeddedDerbyResource =
					new EmbeddedDerbyResource(DerbyResourceConfig.buildDefault().useInMemoryDatabase(DB_NAME)
						.addPostInitScript("classpath:/org/deventropy/junithelper/derby/simple01/ddl.sql")
						.addPostInitScript("classpath:/org/deventropy/junithelper/derby/simple01/dml.sql")
						.addPostInitScript("classpath:/org/deventropy/junithelper/derby/simple01/dne.sql"),
					tempFile);
			IOException expected = null;
			try {
				embeddedDerbyResource.start();
			} catch (IOException e) {
				expected = e;
			}
			assertNotNull("One script should have failed", expected);
			assertEquals("Exception should be for the script only", expected.getMessage(),
					"Exceptions exist in script. See output for details");

			// Check derby system home
			assertEquals("Derby system home should be as we set it",
					embeddedDerbyResource.getDerbySystemHome().getAbsolutePath(), tempFile.getAbsolutePath());
			assertEquals("Derby system home property should be set", System.getProperty(PROP_DERBY_SYSTEM_HOME),
					tempFile.getAbsolutePath());

			// Make sure derby loads up
			final String jdbcUrl = embeddedDerbyResource.getJdbcUrl();
			assertNotNull(jdbcUrl);
			assertTrue(jdbcUrl.contains(DB_NAME));

			simpleDb01Check01(embeddedDerbyResource);

			final File logFile = new File(embeddedDerbyResource.getDerbySystemHome(),  "derby.log");
			assertTrue(logFile.exists());
		} finally {
			closeEmbeddedDerbyResource(embeddedDerbyResource);
			assertFalse("Resource should not be active", embeddedDerbyResource.isActive());
			// Closing again should have no effect
			embeddedDerbyResource.close();
			if (null != tempFile) {
				tempFile.delete();
			}
		}

		// There should be an illegal state exception
		try {
			embeddedDerbyResource.getJdbcUrl();
			fail("Should not come here");
		} catch (IllegalStateException e) {
			assertEquals("Derby resource is not active", e.getMessage());
		}

		assertEquals("Derby system home property should be reset", System.getProperty(PROP_DERBY_SYSTEM_HOME),
				dummyDerbySystemHome);
	}

}
