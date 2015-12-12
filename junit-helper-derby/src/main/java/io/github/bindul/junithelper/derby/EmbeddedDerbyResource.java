/* 
 * Copyright 2015 JUnit Helper Contributors
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
package io.github.bindul.junithelper.derby;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.derby.tools.ij;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.rules.ExternalResource;
import org.junit.rules.TemporaryFolder;

import io.github.bindul.junithelper.utils.ArgumentCheck;
import io.github.bindul.junithelper.utils.UrlResourceUtil;

/**
 * Provides an in-memory Derby resource
 * 
 * TODO Complete docs and examples
 * 
 * @author Bindul Bhowmik
 */
public class EmbeddedDerbyResource extends ExternalResource implements Closeable {

	/**
	 * Stream used for DEV_NULL logging
	 */
	public static OutputStream DEV_NULL = new OutputStream() {
		@Override
		public void write (int b) throws IOException {
			// Derby log > /dev/null
		}
	};
	
	private static final String PROP_FILE_DERBY_PROPERTIES = "derby.properties";
	private static final String PROP_DERBY_SYSTEM_HOME = "derby.system.home";
	private static final String PROP_DERBY_STREAM_ERROR_FIELD = "derby.stream.error.field";
	
	private static final String URLPROP_DERBY_CREATE = ";create=true";
	private static final String URLPROP_DERBY_SHUTDOWN = ";shutdown=true";
	
	private static final String DERBY_EMBEDDED_DRIVER_CLASS = "org.apache.derby.jdbc.EmbeddedDriver";
	
	private final Logger log = LogManager.getLogger(getClass());
	
	private final DerbyResourceConfig config;
	private File derbySystemHome;
	private TemporaryFolder derbySystemHomeParent;
	
	private final String jdbcUrl;
	
	private String oldDerbySystemHomeValue;
	
	/**
	 * Creates a new Derby resource. All configurable parameters for this resource come from the config object passed
	 * in.
	 * 
	 * @param config Configurations to setup this resource
	 * @param derbySystemHome A folder to use as the derby system home
	 */
	public EmbeddedDerbyResource (DerbyResourceConfig config, File derbySystemHome) {
		ArgumentCheck.notNull(config, "Embedded derby config");
		this.config = config;
		ArgumentCheck.notNull(derbySystemHome, "Derby System Home Directory");
		this.derbySystemHome = derbySystemHome;
		
		this.jdbcUrl = buildJdbcUrl();
	}
	
	/**
	 * Creates a new Derby resource. All configurable parameters for this resource come from the config object passed
	 * in.
	 * 
	 * @param config Configurations to setup this resource
	 * @param derbySystemHomeParent A temporary folder to use as the derby system home
	 */
	public EmbeddedDerbyResource (DerbyResourceConfig config, TemporaryFolder derbySystemHomeParent) {
		ArgumentCheck.notNull(config, "Embedded derby config");
		this.config = config;
		ArgumentCheck.notNull(derbySystemHomeParent, "Derby System Home Parent Directory");
		this.derbySystemHomeParent = derbySystemHomeParent; // This can be a TemporaryFolder, so make sure it is not touched before #before()
		
		this.jdbcUrl = buildJdbcUrl();
	}
	
	private String buildJdbcUrl() {
		// TODO Check if this needs to be different for certain sub-sub protocols
		return new StringBuilder().append(config.getSubSubProtocol().jdbcConnectionPrefix()).append(config.getDatabaseName()).toString();
	}

	/* (non-Javadoc)
	 * @see org.junit.rules.ExternalResource#before()
	 */
	@Override
	protected void before() throws Throwable {
		super.before();
		
		// Validate and setup
		if (null != derbySystemHomeParent) {
			this.derbySystemHome = derbySystemHomeParent.newFolder();
		}
		FileUtils.forceMkdir(derbySystemHome);
		oldDerbySystemHomeValue = System.getProperty(PROP_DERBY_SYSTEM_HOME); // Saving it to reset it later
		System.setProperty(PROP_DERBY_SYSTEM_HOME, derbySystemHome.getAbsolutePath());
		setupDerbyProperties();
		
		// Start the database
		// Recommended Derby startup process, see https://db.apache.org/derby/docs/10.12/publishedapi/org/apache/derby/jdbc/EmbeddedDriver.html
		Class.forName(DERBY_EMBEDDED_DRIVER_CLASS).newInstance();
		// Create / Connect to the database
		Connection conn = DriverManager.getConnection(buildCreateJDBCUrl());
		// Post init scripts
		executePostInitScripts(conn);
		conn.close();
	}

	private void executePostInitScripts(Connection conn) throws IOException {
		for (String postInitScript : config.getPostInitScripts()) {
			runScript(postInitScript, conn);
		}
	}
	
	private void runScript (String script, Connection conn) throws IOException {
		
		InputStream scriptStream = null;
		OutputStream scriptLogStream = null;
		
		try {
		
			String charset = Charset.defaultCharset().name();
			URL scriptUrl = UrlResourceUtil.getUrl(script);
			scriptStream = scriptUrl.openStream();
			
			File scriptLogFile = new File(derbySystemHome, "post-init-" + scriptUrl.getPath().replaceAll("/", "_") + ".log");
			scriptLogStream = new FileOutputStream(scriptLogFile, true);
			
			
			log.debug("Executing script: {}", script);
			int exceptionCount = ij.runScript(conn, scriptStream, charset, scriptLogStream, charset);
			if (exceptionCount > 0) {
				log.warn("Error executing script {}", script);
				log.warn(FileUtils.readFileToString(scriptLogFile));
				throw new IOException("Exceptions exist in script. See output for details");
			}
		
		} finally {
			IOUtils.closeQuietly(scriptLogStream);
			IOUtils.closeQuietly(scriptStream);
		}
	}

	private String buildCreateJDBCUrl() {
		// TODO Will handle things here to restore from a backup, etc.
		return new StringBuilder().append(jdbcUrl).append(URLPROP_DERBY_CREATE).toString();
	}

	private void setupDerbyProperties() throws IOException {
		Properties derbyProps = new Properties();
		
		// Logging
		switch (config.getErrorLoggingMode()) {
			case Null:
				derbyProps.setProperty(PROP_DERBY_STREAM_ERROR_FIELD, getClass().getName() + ".DEV_NULL");
				break;
			case Default:
			default:
				// Do nothing
		}
		
		// Write it
		File derbyPropertyFile = new File(derbySystemHome, PROP_FILE_DERBY_PROPERTIES);
		FileWriter derbyPropertyFileWriter = new FileWriter(derbyPropertyFile);
		derbyProps.store(derbyPropertyFileWriter, null);
		IOUtils.closeQuietly(derbyPropertyFileWriter);
	}

	/* (non-Javadoc)
	 * @see org.junit.rules.ExternalResource#after()
	 */
	@Override
	protected void after() {
		super.after();
		try {
			this.close();
		} catch (IOException e) {
			// Ignore
			log.catching(Level.TRACE, e);
		}
		// Reset the Derby System Home property
		if (null != oldDerbySystemHomeValue && !oldDerbySystemHomeValue.isEmpty()) {
			System.setProperty(PROP_DERBY_SYSTEM_HOME, oldDerbySystemHomeValue);
		} else {
			System.clearProperty(PROP_DERBY_SYSTEM_HOME);
		}
	}

	/* (non-Javadoc)
	 * @see java.io.Closeable#close()
	 */
	@Override
	public void close() throws IOException {
		try {
			Connection conn = DriverManager.getConnection(jdbcUrl + URLPROP_DERBY_SHUTDOWN);
			conn.close();
		} catch (SQLException e) {
			// Ignore
		}
	}

	/**
	 * @return the derbySystemHome
	 */
	public File getDerbySystemHome() {
		return derbySystemHome;
	}

	/**
	 * @return the jdbcUrl
	 */
	public String getJdbcUrl() {
		return jdbcUrl;
	}

}
