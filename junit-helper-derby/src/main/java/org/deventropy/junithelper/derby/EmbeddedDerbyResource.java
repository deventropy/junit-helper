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

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.deventropy.junithelper.utils.ArgumentCheck;
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
 * @author Bindul Bhowmik
 */
public class EmbeddedDerbyResource extends ExternalResource implements Closeable {

	private static final String PROP_FILE_DERBY_PROPERTIES = "derby.properties";
	private static final String PROP_DERBY_SYSTEM_HOME = "derby.system.home";
	private static final String PROP_DERBY_STREAM_ERROR_FILE = "derby.stream.error.file";
	private static final String PROP_DERBY_STREAM_ERROR_FIELD = "derby.stream.error.field";
	
	private static final String URLPROP_DERBY_CREATE = ";create=true";
	private static final String URLPROP_DERBY_SHUTDOWN = ";shutdown=true";
	private static final String URLPROP_DERBY_DROP = ";drop=true";
	
	private static final String DERBY_EMBEDDED_DRIVER_CLASS = "org.apache.derby.jdbc.EmbeddedDriver";
	
	private final Logger log = LogManager.getLogger();
	
	private final DerbyResourceConfig config;
	private File derbySystemHome;
	private TemporaryFolder derbySystemHomeParent;
	
	private final String jdbcUrl;
	
	private String oldDerbySystemHomeValue;
	
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
		// TODO Check if this needs to be different for certain sub-sub protocols
		return new StringBuilder().append(config.getSubSubProtocol().jdbcConnectionPrefix())
				.append(config.getDatabaseName()).toString();
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
	 * @throws IOException IO exception creating or setting derby home
	 * @throws SQLException SQL exception starting derby or running the init scripts
	 */
	public void start () throws IOException, SQLException {
		// Validate and setup
		if (null != derbySystemHomeParent) {
			this.derbySystemHome = derbySystemHomeParent.newFolder();
		}
		FileUtils.forceMkdir(derbySystemHome);
		oldDerbySystemHomeValue = System.getProperty(PROP_DERBY_SYSTEM_HOME); // Saving it to reset it later
		System.setProperty(PROP_DERBY_SYSTEM_HOME, derbySystemHome.getAbsolutePath());
		setupDerbyProperties();

		// Start the database
		// Recommended Derby startup process,
		// see https://db.apache.org/derby/docs/10.12/publishedapi/org/apache/derby/jdbc/EmbeddedDriver.html
		try {
			Class.forName(DERBY_EMBEDDED_DRIVER_CLASS).newInstance();
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
			// Reset the Derby System Home property
			resetDerbyHome();
			throw new SQLException("Unable to initialize Derby driver class: " + DERBY_EMBEDDED_DRIVER_CLASS, e);
		}
		// Create / Connect to the database
		final Connection conn = DriverManager.getConnection(buildCreateJDBCUrl());
		try {
			// Post init scripts
			executePostInitScripts(conn);
		} finally {
			DerbyUtils.closeQuietly(conn);
		}
	}

	private void executePostInitScripts (final Connection conn) throws IOException {
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
	}
	
	private String buildCreateJDBCUrl () {
		// TODO Will handle things here to restore from a backup, etc.
		return new StringBuilder().append(jdbcUrl).append(URLPROP_DERBY_CREATE).toString();
	}

	private void setupDerbyProperties () throws IOException {
		final Properties derbyProps = new Properties();

		// Logging
		switch (config.getErrorLoggingMode()) {
			case Null:
				derbyProps.setProperty(PROP_DERBY_STREAM_ERROR_FIELD, DerbyUtils.DEV_NULL_FIELD_ID);
				break;
			case Default:
			default:
				derbyProps.setProperty(PROP_DERBY_STREAM_ERROR_FILE, "derby.log");
				break;
		}

		// Write it
		final File derbyPropertyFile = new File(derbySystemHome, PROP_FILE_DERBY_PROPERTIES);
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

	/* (non-Javadoc)
	 * @see java.io.Closeable#close()
	 */
	@Override
	public void close () throws IOException {
		Connection conn = null;
		try {
			final StringBuilder shutdownUrl = new StringBuilder(jdbcUrl);
			if (JdbcDerbySubSubProtocol.Memory == config.getSubSubProtocol()) {
				shutdownUrl.append(URLPROP_DERBY_DROP);
			} else {
				shutdownUrl.append(URLPROP_DERBY_SHUTDOWN);
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
	}

	private void resetDerbyHome () {
		// Reset the Derby System Home property
		if (null != oldDerbySystemHomeValue && !oldDerbySystemHomeValue.isEmpty()) {
			System.setProperty(PROP_DERBY_SYSTEM_HOME, oldDerbySystemHomeValue);
			oldDerbySystemHomeValue = null;
		} else {
			System.clearProperty(PROP_DERBY_SYSTEM_HOME);
		}
	}

	/**
	 * @return the derbySystemHome
	 */
	public File getDerbySystemHome () {
		return derbySystemHome;
	}

	/**
	 * @return the jdbcUrl
	 */
	public String getJdbcUrl () {
		return jdbcUrl;
	}

}
