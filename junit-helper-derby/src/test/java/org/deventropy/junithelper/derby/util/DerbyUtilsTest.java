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
package org.deventropy.junithelper.derby.util;

import static org.junit.Assert.*;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Struct;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;

import org.deventropy.junithelper.derby.DerbyResourceConfig;
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
public class DerbyUtilsTest {
	
	private TemporaryFolder tempFolder = new TemporaryFolder();
	private EmbeddedDerbyResource embeddedDerbyResource = new EmbeddedDerbyResource(
			DerbyResourceConfig.buildDefault().useDevNullErrorLogging(),
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
	
	/**
	 * Test method for {@link org.deventropy.junithelper.derby.util.DerbyUtils#DEV_NULL} and
	 * {@link org.deventropy.junithelper.derby.util.DerbyUtils#DEV_NULL_FIELD_ID}.
	 * @throws Exception test failure
	 */
	@Test
	public void testDevNull () throws Exception {
		assertNotNull(DerbyUtils.DEV_NULL);
		assertNotNull(DerbyUtils.DEV_NULL_FIELD_ID);

		// Check Field ID
		final String fieldIdClass = DerbyUtils.DEV_NULL_FIELD_ID.substring(0,
				DerbyUtils.DEV_NULL_FIELD_ID.lastIndexOf('.'));
		assertEquals("Should be the same class", DerbyUtils.class.getName(), fieldIdClass);
		final String fieldIdField = DerbyUtils.DEV_NULL_FIELD_ID.substring(
				DerbyUtils.DEV_NULL_FIELD_ID.lastIndexOf('.') + 1);
		assertNotNull(fieldIdField);

		@SuppressWarnings("rawtypes")
		final Class cls = Class.forName(fieldIdClass);
		final Field devNullField = cls.getDeclaredField(fieldIdField);
		assertTrue(Modifier.isStatic(devNullField.getModifiers()));
		assertEquals(DerbyUtils.DEV_NULL, devNullField.get(null));
	}

	/**
	 * Test method for {@link org.deventropy.junithelper.derby.util.DerbyUtils#closeQuietly(java.lang.AutoCloseable)}.
	 * @throws SQLException Error getting conn
	 */
	@Test
	public void testCloseQuietly () throws SQLException {

		final Connection conn = embeddedDerbyResource.createConnection();
		// First time - no failures
		DerbyUtils.closeQuietly(conn);
		assertTrue("Connection should be closed", conn.isClosed());

		final Connection conn2 = embeddedDerbyResource.createConnection();
		// Calling connection twice is a No-Op (per documentation), so getting creative here
		DerbyUtils.closeQuietly(new ExceptionOnCloseConnectionWrapper(conn2));
		assertTrue("Connection should be closed", conn2.isClosed());
	}
	
	/**
	 * A test connection wrapper
	 * 
	 * @author Bindul Bhowmik
	 */
	private final class ExceptionOnCloseConnectionWrapper implements Connection {
		private final Connection wrapped;
		private ExceptionOnCloseConnectionWrapper (final Connection wrapped) {
			this.wrapped = wrapped;
		}
		public <T> T unwrap (final Class<T> iface) throws SQLException {
			return wrapped.unwrap(iface);
		}
		public boolean isWrapperFor (final Class<?> iface) throws SQLException {
			return wrapped.isWrapperFor(iface);
		}
		public Statement createStatement () throws SQLException {
			return wrapped.createStatement();
		}
		public PreparedStatement prepareStatement (final String sql) throws SQLException {
			return wrapped.prepareStatement(sql);
		}
		public CallableStatement prepareCall (final String sql) throws SQLException {
			return wrapped.prepareCall(sql);
		}
		public String nativeSQL (final String sql) throws SQLException {
			return wrapped.nativeSQL(sql);
		}
		public void setAutoCommit (final boolean autoCommit) throws SQLException {
			wrapped.setAutoCommit(autoCommit);
		}
		public boolean getAutoCommit () throws SQLException {
			return wrapped.getAutoCommit();
		}
		public void commit () throws SQLException {
			wrapped.commit();
		}
		public void rollback () throws SQLException {
			wrapped.rollback();
		}
		public void close () throws SQLException {
			// Cause exception
			wrapped.close();
			throw new SQLException("Test code");
		}
		public boolean isClosed () throws SQLException {
			return wrapped.isClosed();
		}
		public DatabaseMetaData getMetaData () throws SQLException {
			return wrapped.getMetaData();
		}
		public void setReadOnly (final boolean readOnly) throws SQLException {
			wrapped.setReadOnly(readOnly);
		}
		public boolean isReadOnly () throws SQLException {
			return wrapped.isReadOnly();
		}
		public void setCatalog (final String catalog) throws SQLException {
			wrapped.setCatalog(catalog);
		}
		public String getCatalog () throws SQLException {
			return wrapped.getCatalog();
		}
		public void setTransactionIsolation (final int level) throws SQLException {
			wrapped.setTransactionIsolation(level);
		}
		public int getTransactionIsolation () throws SQLException {
			return wrapped.getTransactionIsolation();
		}
		public SQLWarning getWarnings () throws SQLException {
			return wrapped.getWarnings();
		}
		public void clearWarnings () throws SQLException {
			wrapped.clearWarnings();
		}
		public Statement createStatement (final int resultSetType, final int resultSetConcurrency) throws SQLException {
			return wrapped.createStatement(resultSetType, resultSetConcurrency);
		}
		public PreparedStatement prepareStatement (final String sql, final int resultSetType,
				final int resultSetConcurrency) throws SQLException {
			return wrapped.prepareStatement(sql, resultSetType, resultSetConcurrency);
		}
		public CallableStatement prepareCall (final String sql, final int resultSetType, final int resultSetConcurrency)
				throws SQLException {
			return wrapped.prepareCall(sql, resultSetType, resultSetConcurrency);
		}
		public Map<String, Class<?>> getTypeMap () throws SQLException {
			return wrapped.getTypeMap();
		}
		public void setTypeMap (final Map<String, Class<?>> map) throws SQLException {
			wrapped.setTypeMap(map);
		}
		public void setHoldability (final int holdability) throws SQLException {
			wrapped.setHoldability(holdability);
		}
		public int getHoldability () throws SQLException {
			return wrapped.getHoldability();
		}
		public Savepoint setSavepoint () throws SQLException {
			return wrapped.setSavepoint();
		}
		public Savepoint setSavepoint (final String name) throws SQLException {
			return wrapped.setSavepoint(name);
		}
		public void rollback (final Savepoint savepoint) throws SQLException {
			wrapped.rollback(savepoint);
		}
		public void releaseSavepoint (final Savepoint savepoint) throws SQLException {
			wrapped.releaseSavepoint(savepoint);
		}
		public Statement createStatement (final int resultSetType, final int resultSetConcurrency,
				final int resultSetHoldability) throws SQLException {
			return wrapped.createStatement(resultSetType, resultSetConcurrency, resultSetHoldability);
		}
		public PreparedStatement prepareStatement (final String sql, final int resultSetType,
				final int resultSetConcurrency, final int resultSetHoldability) throws SQLException {
			return wrapped.prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
		}
		public CallableStatement prepareCall (final String sql, final int resultSetType, final int resultSetConcurrency,
				final int resultSetHoldability) throws SQLException {
			return wrapped.prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
		}
		public PreparedStatement prepareStatement (final String sql, final int autoGeneratedKeys) throws SQLException {
			return wrapped.prepareStatement(sql, autoGeneratedKeys);
		}
		public PreparedStatement prepareStatement (final String sql, final int[] columnIndexes) throws SQLException {
			return wrapped.prepareStatement(sql, columnIndexes);
		}
		public PreparedStatement prepareStatement (final String sql, final String[] columnNames) throws SQLException {
			return wrapped.prepareStatement(sql, columnNames);
		}
		public Clob createClob () throws SQLException {
			return wrapped.createClob();
		}
		public Blob createBlob () throws SQLException {
			return wrapped.createBlob();
		}
		public NClob createNClob () throws SQLException {
			return wrapped.createNClob();
		}
		public SQLXML createSQLXML () throws SQLException {
			return wrapped.createSQLXML();
		}
		public boolean isValid (final int timeout) throws SQLException {
			return wrapped.isValid(timeout);
		}
		public void setClientInfo (final String name, final String value) throws SQLClientInfoException {
			wrapped.setClientInfo(name, value);
		}
		public void setClientInfo (final Properties properties) throws SQLClientInfoException {
			wrapped.setClientInfo(properties);
		}
		public String getClientInfo (final String name) throws SQLException {
			return wrapped.getClientInfo(name);
		}
		public Properties getClientInfo () throws SQLException {
			return wrapped.getClientInfo();
		}
		public Array createArrayOf (final String typeName, final Object[] elements) throws SQLException {
			return wrapped.createArrayOf(typeName, elements);
		}
		public Struct createStruct (final String typeName, final Object[] attributes) throws SQLException {
			return wrapped.createStruct(typeName, attributes);
		}
		public void setSchema (final String schema) throws SQLException {
			wrapped.setSchema(schema);
		}
		public String getSchema () throws SQLException {
			return wrapped.getSchema();
		}
		public void abort (final Executor executor) throws SQLException {
			wrapped.abort(executor);
		}
		public void setNetworkTimeout (final Executor executor, final int milliseconds) throws SQLException {
			wrapped.setNetworkTimeout(executor, milliseconds);
		}
		public int getNetworkTimeout () throws SQLException {
			return wrapped.getNetworkTimeout();
		}
	}
}
