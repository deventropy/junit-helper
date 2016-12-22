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
package org.deventropy.junithelper.derby.datasource.simple01;

import static org.junit.Assert.*;

import java.sql.SQLException;

import org.deventropy.junithelper.derby.DerbyResourceConfig;
import org.deventropy.junithelper.derby.datasource.AbstractDatasourceEmbeddedDerbyResourceTest;
import org.deventropy.junithelper.derby.datasource.EmbeddedDerbyDataSourceResource;
import org.deventropy.junithelper.derby.util.DerbyUtils;
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
public class InMemoryDbTempDirDatasourceTest extends AbstractDatasourceEmbeddedDerbyResourceTest {
	
	private static final String DB_NAME = "my-test-database-simple05-tmpfolder-ds";
	
	private TemporaryFolder tempFolder = new TemporaryFolder();
	private EmbeddedDerbyDataSourceResource embeddedDerbyResource =
		new EmbeddedDerbyDataSourceResource(DerbyResourceConfig.buildDefault().useInMemoryDatabase(DB_NAME)
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
	public void testDataSourceFromSimpleInMemoryDatabase () throws SQLException {
		// Make sure derby loads up
		simpleDb01Check01(embeddedDerbyResource);
		assertTrue(embeddedDerbyResource.isActive());

		testDifferentDataSources(embeddedDerbyResource);
	}
}
