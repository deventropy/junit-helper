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

import java.io.Closeable;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import javax.sql.ConnectionPoolDataSource;
import javax.sql.DataSource;
import javax.sql.XADataSource;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.derby.jdbc.EmbeddedConnectionPoolDataSource;
import org.apache.derby.jdbc.EmbeddedDataSource;
import org.apache.derby.jdbc.EmbeddedDataSourceInterface;
import org.apache.derby.jdbc.EmbeddedXADataSource;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.deventropy.shared.utils.ArgumentCheck;
import org.junit.rules.ExternalResource;
import org.junit.rules.TemporaryFolder;

/**
 * Provides an in-memory Derby resource. An instance of this class is initialized with the
 * {@link DerbyResourceConfig configuration} and a {@link #getDerbySystemHome() Derby System Home}.
 * 
 * <p>The class can be used either as a JUnit {@link org.junit.Rule Rule} / {@link org.junit.ClassRule ClassRule} OR
 * directly by the user. Initialization and de-initialization of an instance of this class (and the embedded Derby
 * instance) is handed by the {@link #start()} and {@link #close()} methods respectively. When used as a
 * <code>Rule</code> or <code>ClassRule</code>, the {@link #before()} and {@link #after()} mdthods from those interfaces
 * handle the initialization and de-initialization for the user (internally using the <code>#start()</code> and
 * <code>#close()</code> methods.
 * 
 * <p>Derby does not allow running multiple instances in the same JVM, so external protection should be provided to
 * protect against that.
 * 
 * <p>Example of usage:
 * <pre>
 * public class SimpleDerbyTest {
 * 
 * 	private TemporaryFolder tempFolder = new TemporaryFolder();
 * 	private EmbeddedDerbyResource embeddedDerbyResource =
 * 		new EmbeddedDerbyResource(DerbyResourceConfig.buildDefault().useDevNullErrorLogging(),
 * 		tempFolder);
 * 
 * 	&#064;Rule
 * 	public RuleChain derbyRuleChain = RuleChain.outerRule(tempFolder).around(embeddedDerbyResource);
 * 
 * 	&#064;Test
 * 	public void test () throws SQLException {
 * 		final String jdbcUrl = embeddedDerbyResource.getJdbcUrl();
 * 		Connection connection = null;
 * 		Statement stmt = null;
 * 		ResultSet rs = null;
 * 
 * 		try {
 * 			connection = DriverManager.getConnection(jdbcUrl);
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
 * @see DerbyResourceConfig
 * 
 * @author Bindul Bhowmik
 */
public class EmbeddedDerbyResource extends ExternalResource implements Closeable {

	private final Logger log = LogManager.getLogger();
	
	private final DerbyResourceConfig config;
	private File derbySystemHome;
	private TemporaryFolder derbySystemHomeParent;
	
	private final String jdbcUrl;
	private boolean isActive = false;
	
	private String oldDerbySystemHomeValue;
	
	private final DerbyBackupOperationsHelper backupOperationsHelper = new DerbyBackupOperationsHelper(this);
	private final EmbeddedDerbyDataSourceFactory dataSourceFactory = new EmbeddedDerbyDataSourceFactoryImpl();
	
	/**
	 * Creates a new Derby resource. All configurable parameters for this resource come from the config object
	 * passed in.
	 * 
	 * @param dbResourceConfig Configurations to setup this resource
	 * @param derbySystemHomeDir A folder to use as the derby system home
	 */
	public EmbeddedDerbyResource (final DerbyResourceConfig dbResourceConfig, final File derbySystemHomeDir) {
		ArgumentCheck.notNull(dbResourceConfig, "Embedded derby config");
		this.config = dbResourceConfig;
		ArgumentCheck.notNull(derbySystemHomeDir, "Derby System Home Directory");
		this.derbySystemHome = derbySystemHomeDir;

		this.jdbcUrl = buildJdbcUrl();
	}
	
	/**
	 * Creates a new Derby resource. All configurable parameters for this resource come from the config object
	 * passed in.
	 * 
	 * @param dbResourceConfig Configurations to setup this resource
	 * @param derbySystemHomeParentTmpFolder A temporary folder to use as the derby system home
	 */
	public EmbeddedDerbyResource (final DerbyResourceConfig dbResourceConfig,
			final TemporaryFolder derbySystemHomeParentTmpFolder) {

		ArgumentCheck.notNull(dbResourceConfig, "Embedded derby config");
		this.config = dbResourceConfig;
		ArgumentCheck.notNull(derbySystemHomeParentTmpFolder, "Derby System Home Parent Directory");
		// This can be a TemporaryFolder, so make sure it is not touched before #before()
		this.derbySystemHomeParent = derbySystemHomeParentTmpFolder;

		this.jdbcUrl = buildJdbcUrl();
	}
	
	private String buildJdbcUrl () {
		final StringBuilder jdbcUrlBldr = new StringBuilder().append(config.getSubSubProtocol().jdbcConnectionPrefix());
		appendDbLocNameToUrl(jdbcUrlBldr);
		return jdbcUrlBldr.toString();
	}

	private void appendDbLocNameToUrl (final StringBuilder jdbcUrlBldr) {
		if (JdbcDerbySubSubProtocol.Jar == config.getSubSubProtocol()) {
			// for :jar: protocol, see http://db.apache.org/derby/docs/10.12/devguide/cdevdeploy11201.html
			jdbcUrlBldr.append('(').append(config.getJarDatabaseJarFile()).append(')');
		}
		jdbcUrlBldr.append(config.getDatabasePath());
	}

	/* (non-Javadoc)
	 * @see org.junit.rules.ExternalResource#before()
	 */
	@Override
	protected void before () throws Throwable {
		super.before();
		this.start();
	}
	
	/**
	 * Starts the Embedded derby instance.
	 * 
	 * <p><em>Note:</em> If using this instance as a JUnit {@linkplain org.junit.Rule}, do not call this method;
	 * initialization is already handled from the {@linkplain org.junit.rules.ExternalResource#before()}.
	 * 
	 * <p>On successful completion (with the exception of failures in
	 * {@linkplain DerbyResourceConfig#getPostInitScripts()}, the {@link #isActive()} state of this resource is set to
	 * <code>true</code>. Further calls to this method while the resource is active has no effect.
	 * 
	 * @throws IOException IO exception creating or setting derby home
	 * @throws SQLException SQL exception starting derby or running the init scripts
	 */
	public void start () throws IOException, SQLException {

		if (isActive) {
			return; // already started
		}

		Connection conn = null;
		try {
			// Validate and setup
			if (null != derbySystemHomeParent) {
				this.derbySystemHome = derbySystemHomeParent.newFolder();
			}
			FileUtils.forceMkdir(derbySystemHome);
			// Save it to reset later
			oldDerbySystemHomeValue = System.getProperty(DerbyConstants.PROP_DERBY_SYSTEM_HOME);
			System.setProperty(DerbyConstants.PROP_DERBY_SYSTEM_HOME, derbySystemHome.getAbsolutePath());
			setupDerbyProperties();
	
			// Start the database
			// Recommended Derby startup process,
			// see https://db.apache.org/derby/docs/10.12/publishedapi/org/apache/derby/jdbc/EmbeddedDriver.html
			try {
				Class.forName(DerbyConstants.DERBY_EMBEDDED_DRIVER_CLASS).newInstance();
			} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
				throw new SQLException(
						"Unable to initialize Derby driver class: " + DerbyConstants.DERBY_EMBEDDED_DRIVER_CLASS, e);
			}
			// Create / Connect to the database
			conn = DriverManager.getConnection(buildCreateJDBCUrl());
			isActive = true;
		} catch (IOException | SQLException e) {
			// Reset the Derby System Home property
			resetDerbyHome();
			throw e;
		} finally {
			DerbyUtils.closeQuietly(conn);
		}

		// Post init scripts
		executePostInitScripts();
	}

	private void executePostInitScripts () throws IOException, SQLException {
		Connection conn = null;
		try {
			conn = createConnection();
			final DerbyScriptRunner scriptRunner = new DerbyScriptRunner(conn);
			for (String postInitScript : config.getPostInitScripts()) {
				final File scriptLogFile = new File(derbySystemHome, "post-init-"
						+ postInitScript.replaceAll("/", "_") + ".log");
				try {
					final int result = scriptRunner.executeScript(postInitScript, scriptLogFile);
					if (result != 0) {
						log.warn(FileUtils.readFileToString(scriptLogFile));
						throw new IOException("Exceptions exist in script. See output for details");
					}
				} catch (IOException e) {
					log.warn(FileUtils.readFileToString(scriptLogFile));
					throw new IOException("Exceptions exist in script. See output for details");
				}
			}
		} finally {
			DerbyUtils.closeQuietly(conn);
		}
	}
	
	private String buildCreateJDBCUrl () {
		final StringBuilder createDbJdbcUrl = new StringBuilder(jdbcUrl);
		final JdbcDerbySubSubProtocol subSubProtocol = config.getSubSubProtocol();

		if (null != config.getDbCreateFromRestoreMode()) {
			// Database from a backup
			final DbCreateFromRestroreMode dbCreateFromRestroreMode = config.getDbCreateFromRestoreMode();
			createDbJdbcUrl.append(DerbyConstants.URLPROP_DERBY_SEPARATOR)
					.append(dbCreateFromRestroreMode.urlAttribute()).append(DerbyConstants.URLPROP_DERBY_EQUAL)
					.append(config.getDbCreateFromRestoreFrom().getAbsolutePath());
			if (dbCreateFromRestroreMode.requiresLogDevice()) {
				createDbJdbcUrl.append(DerbyConstants.URLPROP_DERBY_SEPARATOR)
						.append(DbCreateFromRestroreMode.URLPROP_DERBY_LOGDEVICE)
						.append(DerbyConstants.URLPROP_DERBY_EQUAL)
						.append(config.getDbRecoveryLogDevice().getAbsolutePath());
			}
		} else if (JdbcDerbySubSubProtocol.Memory == subSubProtocol
				|| (JdbcDerbySubSubProtocol.Directory == subSubProtocol && !config.isDirectoryDatabaseSkipCreate())) {
			// Only :memory: and :directory: databases need the 'create' flag.
			createDbJdbcUrl.append(DerbyConstants.URLPROP_DERBY_CREATE);
		}

		log.debug("Will use JDBC URL {} to start Derby", createDbJdbcUrl);
		return createDbJdbcUrl.toString();
	}

	private void setupDerbyProperties () throws IOException {
		final Properties derbyProps = new Properties();

		// Logging
		switch (config.getErrorLoggingMode()) {
			case Null:
				derbyProps.setProperty(DerbyConstants.PROP_DERBY_STREAM_ERROR_FIELD, DerbyUtils.DEV_NULL_FIELD_ID);
				break;
			case Default:
			default:
				derbyProps.setProperty(DerbyConstants.PROP_DERBY_STREAM_ERROR_FILE, "derby.log");
				break;
		}

		// Write it
		final File derbyPropertyFile = new File(derbySystemHome, DerbyConstants.PROP_FILE_DERBY_PROPERTIES);
		final FileWriter derbyPropertyFileWriter = new FileWriter(derbyPropertyFile);
		derbyProps.store(derbyPropertyFileWriter, null);
		IOUtils.closeQuietly(derbyPropertyFileWriter);
	}

	/* (non-Javadoc)
	 * @see org.junit.rules.ExternalResource#after()
	 */
	@Override
	protected void after () {
		super.after();
		try {
			this.close();
		} catch (IOException e) {
			// Ignore
			log.catching(Level.TRACE, e);
		}
	}

	/**
	 * Shuts down and closes the Derby Instance. It also restores any previously set <code>derby.system.home</code>
	 * system property. However, should another derby instance need to be created in the same JVM after this is shut
	 * down, ensure the Derby system is properly shut down (see {@link DerbyUtils#shutdownDerbySystemQuitely(boolean)}.
	 * 
	 * <p>This method sets the {@link #isActive()} state of the resouce to false, and calls to this method when the
	 * resource is not active are ignored.
	 * 
	 * {@inheritDoc}
	 */
	@Override
	public void close () throws IOException {

		if (!isActive) {
			return;
		}

		Connection conn = null;
		try {
			final StringBuilder shutdownUrl = new StringBuilder(jdbcUrl);
			if (JdbcDerbySubSubProtocol.Memory == config.getSubSubProtocol()) {
				shutdownUrl.append(DerbyConstants.URLPROP_DERBY_DROP);
			} else {
				shutdownUrl.append(DerbyConstants.URLPROP_DERBY_SHUTDOWN);
			}
			conn = DriverManager.getConnection(shutdownUrl.toString());
		} catch (SQLException e) {
			// Ignore - there will always be an exception
			log.catching(Level.TRACE, e);
		} finally {
			DerbyUtils.closeQuietly(conn);
		}
		// Reset the Derby System Home property
		resetDerbyHome();
		isActive = false;
	}

	private void resetDerbyHome () {
		// Reset the Derby System Home property
		if (null != oldDerbySystemHomeValue && !oldDerbySystemHomeValue.isEmpty()) {
			System.setProperty(DerbyConstants.PROP_DERBY_SYSTEM_HOME, oldDerbySystemHomeValue);
			oldDerbySystemHomeValue = null;
		} else {
			System.clearProperty(DerbyConstants.PROP_DERBY_SYSTEM_HOME);
		}
	}

	/**
	 * Returns the file reference to the Derby system home.
	 * 
	 * @return the derbySystemHome
	 * @throws IllegalStateException if the method is invoked when the database is not {@link #isActive()}.
	 */
	public File getDerbySystemHome () {
		ensureActive();
		return derbySystemHome;
	}

	/**
	 * Returns a URL that can be used to create a connection to this database instance.
	 * 
	 * @return the jdbcUrl
	 * @throws IllegalStateException if the method is invoked when the database is not {@link #isActive()}.
	 */
	public String getJdbcUrl () {
		ensureActive();
		return jdbcUrl;
	}

	/**
	 * Returns the database path of the JDBC URL.
	 * @see DerbyResourceConfig#getDatabasePath()
	 * @return The database path
	 */
	public String getDatabasePath () {
		return config.getDatabasePath();
	}
	
	/**
	 * Create and return a new connection for this resource. If the resource is a pooled datasource, it will be a pooled
	 * connection.
	 * 
	 * @return A new basic or pooled connection for this database.
	 * @throws SQLException If there is an error creating the connection
	 * @throws IllegalStateException if the method is invoked when the database is not {@link #isActive()}.
	 */
	public Connection createConnection () throws SQLException {
		ensureActive();
		return DriverManager.getConnection(getJdbcUrl());
	}
	
	/**
	 * Returns true if the resource is started and not closed.
	 * @return The current status of the resource.
	 */
	public boolean isActive () {
		return isActive;
	}
	
	/**
	 * Checks if the Embedded Derby Resource is active, if not throws an {@link IllegalStateException}.
	 */
	protected void ensureActive () {
		if (!isActive) {
			throw new IllegalStateException("Derby resource is not active");
		}
	}

	/**
	 * Perform an online backup of the running instance. The online backup uses either the
	 * <code>SYSCS_UTIL.SYSCS_BACKUP_DATABASE</code> if <code>enableArchiveLogging</code> is set to <code>false</code>
	 * or <code>SYSCS_UTIL.SYSCS_BACKUP_DATABASE_AND_ENABLE_LOG_ARCHIVE_MODE</code> otherwise. If the
	 * <code>waitForTransactions</code> parameter is set to <code>false</code> the <code>_NOWAIT</code> versions of the
	 * procedures are used.
	 * 
	 * <p>For more information on backing up Derby database, see
	 * <a href="http://db.apache.org/derby/docs/10.12/adminguide/cadminhubbkup01.html">Using the backup procedures to
	 * perform an online backup</a> in the Derby Administrators guide.
	 * 
	 * @param backupDir The directory to which the database should be backed up.
	 * @param waitForTransactions Wait for running transactions to complete.
	 * @param enableArchiveLogging If archive logging should be enabled for the database.
	 * @param deleteArchivedLogs Ask Derby to delete the old archive logs after the backup is successful.
	 * @throws SQLException Exception from Derby when the backup fails.
	 * @throws IllegalStateException if the method is invoked when the database is not {@link #isActive()}.
	 */
	public void backupLiveDatabase (final File backupDir, final boolean waitForTransactions,
			final boolean enableArchiveLogging, final boolean deleteArchivedLogs) throws SQLException {
		ensureActive();
		backupOperationsHelper.backupLiveDatabase(backupDir, waitForTransactions, enableArchiveLogging,
				deleteArchivedLogs);
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
					.append(config.getSubSubProtocol().datasourceDatabaseNamePrefix());
			appendDbLocNameToUrl(dsDatabaseName);
			dataSource.setDatabaseName(dsDatabaseName.toString());
		}

	}
}
