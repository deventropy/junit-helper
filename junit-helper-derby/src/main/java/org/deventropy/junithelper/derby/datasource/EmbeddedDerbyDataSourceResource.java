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

import java.io.File;

import javax.sql.ConnectionPoolDataSource;
import javax.sql.DataSource;
import javax.sql.XADataSource;

import org.apache.derby.jdbc.EmbeddedConnectionPoolDataSource;
import org.apache.derby.jdbc.EmbeddedDataSource;
import org.apache.derby.jdbc.EmbeddedDataSourceInterface;
import org.apache.derby.jdbc.EmbeddedXADataSource;
import org.deventropy.junithelper.derby.DerbyResourceConfig;
import org.deventropy.junithelper.derby.EmbeddedDerbyResource;
import org.junit.rules.TemporaryFolder;

/**
 * Provides an in-memory Derby resource capable of providing different kinds of {@link DataSource}s supported by Derby.
 * An instance of this class is initialized with the {@link DerbyResourceConfig configuration} and a
 * {@link #getDerbySystemHome() Derby System Home}.
 * 
 * <p>This resource extends {@link EmbeddedDerbyResource} and all functionality in that class are available here, please
 * consult the documentation of that class on initialization, control of the resource and the underlying Derby
 * database.
 * 
 * <p>Example of usage:
 * <pre>
 * public class SimpleDerbyTest {
 * 
 *  private static final String DB_NAME = "test-database";
 * 
 *  private TemporaryFolder tempFolder = new TemporaryFolder();
 *  private EmbeddedDerbyDataSourceResource embeddedDerbyResource =
 *  	new EmbeddedDerbyDataSourceResource(DerbyResourceConfig.buildDefault().useInMemoryDatabase(DB_NAME),
 *  	tempFolder);
 * 
 *  &#064;Rule
 *  public RuleChain derbyRuleChain = RuleChain.outerRule(tempFolder).around(embeddedDerbyResource);
 * 
 * 	&#064;Test
 * 	public void test () throws SQLException {
 * 		final String jdbcUrl = embeddedDerbyResource.getJdbcUrl();
 * 		DataSource dataSource = null;
 * 		Connection connection = null;
 * 		Statement stmt = null;
 * 		ResultSet rs = null;
 * 
 * 		try {
 * 			final EmbeddedDerbyDataSourceFactory dsFactory = embeddedDerbyDataSourceResource.getDataSourceFactory();
 * 			assertNotNull(dsFactory);
 * 
 * 			dataSource = dsFactory.getDataSource(true);
 * 			connection = dataSource.getConnection();
 * 
 * 			// Check a value
 * 			stmt = connection.createStatement();
 * 			rs = stmt.executeQuery("SELECT 1 FROM SYSIBM.SYSDUMMY1");
 * 
 * 			assertTrue(rs.next());
 * 		} finally {
 * 			// Close resources
 * 		}
 * 	}
 * }
 * </pre>
 * 
 * <p>For further information and examples, see
 * <a href="http://www.deventropy.org/junit-helper/junit-helper-derby/manual/">User Manual on the Project Website</a>.
 * 
 * @see EmbeddedDerbyResource
 * @see DerbyResourceConfig
 * 
 * @author Bindul Bhowmik
 */
public class EmbeddedDerbyDataSourceResource extends EmbeddedDerbyResource {
	
	private final EmbeddedDerbyDataSourceFactory dataSourceFactory = new EmbeddedDerbyDataSourceFactoryImpl();

	/**
	 * Creates a new Derby resource.
	 * 
	 * @see EmbeddedDerbyResource#EmbeddedDerbyResource(DerbyResourceConfig, File)
	 * 
	 * @param dbResourceConfig Configurations to setup this resource
	 * @param derbySystemHomeDir A folder to use as the derby system home
	 */
	public EmbeddedDerbyDataSourceResource (final DerbyResourceConfig dbResourceConfig, final File derbySystemHomeDir) {
		super (dbResourceConfig, derbySystemHomeDir);
	}

	/**
	 * Creates a new Derby resource.
	 * 
	 * @see EmbeddedDerbyResource#EmbeddedDerbyResource(DerbyResourceConfig, TemporaryFolder)
	 * 
	 * @param dbResourceConfig Configurations to setup this resource
	 * @param derbySystemHomeParentTmpFolder A temporary folder to use as the derby system home
	 */
	public EmbeddedDerbyDataSourceResource (final DerbyResourceConfig dbResourceConfig,
			final TemporaryFolder derbySystemHomeParentTmpFolder) {
		super (dbResourceConfig, derbySystemHomeParentTmpFolder);
	}
	
	/**
	 * Returns the {@link EmbeddedDerbyDataSourceFactory} instance for this resource from which data sources can be
	 * created / cached. The factory returned supports caching <code>ataSource</code>s created. It also checks the
	 * state of <code>this</code> instance and will throw {@link IllegalStateException} if the resource is not
	 * {@link #isActive()}.
	 * 
	 * @return Factory to create data sources for this instance.
	 */
	public EmbeddedDerbyDataSourceFactory getDataSourceFactory () {
		return dataSourceFactory;
	}
	
	private class EmbeddedDerbyDataSourceFactoryImpl implements EmbeddedDerbyDataSourceFactory {

		private EmbeddedDataSource embeddedDataSource;
		private EmbeddedConnectionPoolDataSource embeddedConnectionPoolDataSource;
		private EmbeddedXADataSource embeddedXADataSource;

		/* (non-Javadoc)
		 * @see org.deventropy.junithelper.derby.EmbeddedDerbyDataSourceFactory#getDataSource(boolean)
		 */
		@Override
		public DataSource getDataSource (final boolean cachedInstance) {
			ensureActive();
			if (cachedInstance) {
				if (null == embeddedDataSource) {
					embeddedDataSource = createEmbeddedDataSource();
				}
				return embeddedDataSource;
			}
			return createEmbeddedDataSource();
		}

		private EmbeddedDataSource createEmbeddedDataSource () {
			final EmbeddedDataSource embeddedDs = new EmbeddedDataSource();
			setupDataSource(embeddedDs);
			return embeddedDs;
		}

		/* (non-Javadoc)
		 * @see org.deventropy.junithelper.derby.EmbeddedDerbyDataSourceFactory#getConnectionPoolDataSource(boolean)
		 */
		@Override
		public ConnectionPoolDataSource getConnectionPoolDataSource (final boolean cachedInstance) {
			ensureActive();
			if (cachedInstance) {
				if (null == embeddedConnectionPoolDataSource) {
					embeddedConnectionPoolDataSource = createEmbeddedConnectionPoolDataSource();
				}
				return embeddedConnectionPoolDataSource;
			}
			return createEmbeddedConnectionPoolDataSource();
		}

		private EmbeddedConnectionPoolDataSource createEmbeddedConnectionPoolDataSource () {
			final EmbeddedConnectionPoolDataSource connectionPoolDataSource = new EmbeddedConnectionPoolDataSource();
			setupDataSource(connectionPoolDataSource);
			return connectionPoolDataSource;
		}

		/* (non-Javadoc)
		 * @see org.deventropy.junithelper.derby.EmbeddedDerbyDataSourceFactory#getXADataSource(boolean)
		 */
		@Override
		public XADataSource getXADataSource (final boolean cachedInstance) {
			ensureActive();
			if (cachedInstance) {
				if (null == embeddedXADataSource) {
					embeddedXADataSource = createEmbeddedXADataSource();
				}
				return embeddedXADataSource;
			}
			return createEmbeddedXADataSource();
		}

		private EmbeddedXADataSource createEmbeddedXADataSource () {
			final EmbeddedXADataSource xaDataSource = new EmbeddedXADataSource();
			setupDataSource(xaDataSource);
			return xaDataSource;
		}

		private void setupDataSource (final EmbeddedDataSourceInterface dataSource) {
			final StringBuilder dsDatabaseName = new StringBuilder()
					.append(getConfig().getSubSubProtocol().datasourceDatabaseNamePrefix());
			appendDbLocNameToUrl(dsDatabaseName);
			dataSource.setDatabaseName(dsDatabaseName.toString());
		}
	}

}
