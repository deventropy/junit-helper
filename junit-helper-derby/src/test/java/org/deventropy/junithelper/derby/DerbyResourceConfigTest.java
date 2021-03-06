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

import java.util.UUID;

import org.junit.Test;

/**
 * Tests for {@link org.deventropy.junithelper.derby.DerbyResourceConfig}.
 * 
 * @author Bindul Bhowmik
 */
public class DerbyResourceConfigTest {
	
	@Test
	public void testDefaultMethodsDirect () {
		assertNotNull("Default database name should be a UUID",
				UUID.fromString(DerbyResourceConfig.getDefaultDatabasePathName()));
		assertEquals("Default sub-sub protocol should be In Memory", JdbcDerbySubSubProtocol.Memory,
				DerbyResourceConfig.getDefaultSubSubProtocol());
		assertEquals("Default error logging mode should be dev null", ErrorLoggingMode.Default,
				DerbyResourceConfig.getDefaultErrorLoggingMode());
	}

	@Test
	public void testDatabaseName () {
		// Should be a default Database name
		final DerbyResourceConfig resourceConfig = DerbyResourceConfig.buildDefault();
		assertNotNull("Should have a default database name", resourceConfig.getDatabasePath());
		// Default should be a UUID
		assertNotNull("Default database name should be a UUID",
				UUID.fromString(resourceConfig.getDatabasePath()));
	}
	
	public void testInMemoryDatabaseName () {
		final DerbyResourceConfig resourceConfig = DerbyResourceConfig.buildDefault();

		// Should be able to set a database name
		resourceConfig.useInMemoryDatabase("test-database");
		assertEquals("Database name should be the same", "test-database", resourceConfig.getDatabasePath());

		// Should reset the database name
		resourceConfig.useInMemoryDatabase();
		// Default should be a UUID
		assertNotNull("Default database name should be a UUID", UUID.fromString(resourceConfig.getDatabasePath()));
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testDatabaseNameNegative () {
		// Should not be able to set null database name
		DerbyResourceConfig.buildDefault().useInMemoryDatabase(null);
	}
	
	@Test
	public void testInMemoryProtocol () {
		// Default should be in memory, as should setting inMemory
		final DerbyResourceConfig resourceConfig = DerbyResourceConfig.buildDefault();
		assertEquals("Default sub-sub protocol should be In Memory", JdbcDerbySubSubProtocol.Memory,
				resourceConfig.getSubSubProtocol());

		// Switch to in-memory
		resourceConfig.useInMemoryDatabase();
		assertEquals("Should be using in-memory sub sub protocol now", JdbcDerbySubSubProtocol.Memory,
				resourceConfig.getSubSubProtocol());
	}
	
	@Test
	public void testDirectoryProtocol () {
		final DerbyResourceConfig resourceConfig = DerbyResourceConfig.buildDefault().useDatabaseInDirectory();
		assertEquals("Sub-sub protocol should be Directory", JdbcDerbySubSubProtocol.Directory,
				resourceConfig.getSubSubProtocol());
		assertNotNull("Database path should be a random UUID", UUID.fromString(resourceConfig.getDatabasePath()));

		final String oldUuid = resourceConfig.getDatabasePath();
		// Reset the config
		resourceConfig.useDatabaseInDirectory();
		assertEquals("Sub-sub protocol should be Directory", JdbcDerbySubSubProtocol.Directory,
				resourceConfig.getSubSubProtocol());
		assertNotNull("Database path should be a random UUID", UUID.fromString(resourceConfig.getDatabasePath()));
		assertNotEquals("Database path should be reset", oldUuid, resourceConfig.getDatabasePath());

		final String dbPath = "/tmp/testdir";
		resourceConfig.useDatabaseInDirectory(dbPath);
		assertEquals("Sub-sub protocol should be Directory", JdbcDerbySubSubProtocol.Directory,
				resourceConfig.getSubSubProtocol());
		assertEquals("Database path should be test directory", dbPath, resourceConfig.getDatabasePath());
	}
	
	@Test
	public void testErrorLoggingModes () {
		// Default should be in memory, as should setting inMemory
		final DerbyResourceConfig resourceConfig = DerbyResourceConfig.buildDefault();
		assertEquals("Default error logging mode should be dev null", ErrorLoggingMode.Default,
				resourceConfig.getErrorLoggingMode());

		// TODO Save values and check resetting them.
		resourceConfig.useDevNullErrorLogging();
		assertEquals("Error logging mode should be dev null", ErrorLoggingMode.Null,
				resourceConfig.getErrorLoggingMode());

		// TODO Save values and check resetting them.
		resourceConfig.useDefaultErrorLogging();
		assertEquals("Error logging mode should be dev null", ErrorLoggingMode.Default,
				resourceConfig.getErrorLoggingMode());
	}

}
