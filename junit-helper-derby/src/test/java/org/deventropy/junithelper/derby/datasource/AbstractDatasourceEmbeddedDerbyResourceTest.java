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
package org.deventropy.junithelper.derby.datasource;

import static org.junit.Assert.*;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.ConnectionPoolDataSource;
import javax.sql.DataSource;
import javax.sql.PooledConnection;
import javax.sql.XADataSource;

import org.deventropy.junithelper.derby.AbstractEmbeddedDerbyResourceTest;
import org.deventropy.junithelper.derby.util.DerbyUtils;

/**
 * @author Bindul Bhowmik
 *
 */
public abstract class AbstractDatasourceEmbeddedDerbyResourceTest extends AbstractEmbeddedDerbyResourceTest {
	/**
	 * Run various tests on the datasources in the resource.
	 * @param embeddedDerbyDataSourceResource Derby resource to test
	 * @throws SQLException SQL exception
	 */
	protected void testDifferentDataSources (final EmbeddedDerbyDataSourceResource embeddedDerbyDataSourceResource)
			throws SQLException {
		final EmbeddedDerbyDataSourceFactory dataSourceFactory = embeddedDerbyDataSourceResource.getDataSourceFactory();
		assertNotNull(dataSourceFactory);
		testRegularDataSource(dataSourceFactory);
		testConnectionPoolDataSource(dataSourceFactory);
		testXADataSource(dataSourceFactory);
	}
	
	private void testRegularDataSource (final EmbeddedDerbyDataSourceFactory dataSourceFactory) throws SQLException {
		final DataSource dataSource1 = dataSourceFactory.getDataSource(true);
		final DataSource dataSource2 = dataSourceFactory.getDataSource(true);
		final DataSource dataSource3 = dataSourceFactory.getDataSource(false);

		assertTrue("Cached instances should be the same", dataSource1 == dataSource2);
		assertFalse("Non cached datasources should not be the same", dataSource1 == dataSource3);

		// Check data
		Connection connection = null;
		try {
			connection = dataSource1.getConnection();
			assertNotNull(connection);
			simpleDb01Check01(connection);
		} finally {
			DerbyUtils.closeQuietly(connection);
		}
	}
	
	private void testConnectionPoolDataSource (final EmbeddedDerbyDataSourceFactory dataSourceFactory)
			throws SQLException {
		final ConnectionPoolDataSource dataSource1 = dataSourceFactory.getConnectionPoolDataSource(true);
		final ConnectionPoolDataSource dataSource2 = dataSourceFactory.getConnectionPoolDataSource(true);
		final ConnectionPoolDataSource dataSource3 = dataSourceFactory.getConnectionPoolDataSource(false);

		assertTrue("Cached instances should be the same", dataSource1 == dataSource2);
		assertFalse("Non cached datasources should not be the same", dataSource1 == dataSource3);

		// Check data
		testDataOnAndClosePooledConnection(dataSource1.getPooledConnection());
	}
	
	private void testXADataSource (final EmbeddedDerbyDataSourceFactory dataSourceFactory) throws SQLException {
		final XADataSource dataSource1 = dataSourceFactory.getXADataSource(true);
		final XADataSource dataSource2 = dataSourceFactory.getXADataSource(true);
		final XADataSource dataSource3 = dataSourceFactory.getXADataSource(false);

		assertTrue("Cached instances should be the same", dataSource1 == dataSource2);
		assertFalse("Non cached datasources should not be the same", dataSource1 == dataSource3);

		// Check data
		testDataOnAndClosePooledConnection(dataSource1.getXAConnection());
	}

	private void testDataOnAndClosePooledConnection (final PooledConnection pooledConn) throws SQLException {
		Connection connection = null;
		try {
			assertNotNull(pooledConn);
			connection = pooledConn.getConnection();
			assertNotNull(connection);
			simpleDb01Check01(connection);
		} finally {
			DerbyUtils.closeQuietly(connection);
			try {
				if (null != pooledConn) {
					pooledConn.close();
				}
			} catch (SQLException e) {
				// Ignore.
			}
		}
	}
}
