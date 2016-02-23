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
package org.deventropy.junithelper.derby;

/**
 * Holds Derby related strings as constants.
 * 
 * @author Bindul Bhowmik
 */
public final class DerbyConstants {
	
	/**
	 * Default Derby properties file.
	 */
	public static final String PROP_FILE_DERBY_PROPERTIES = "derby.properties";
	
	/**
	 * Derby embedded JDBC driver class.
	 */
	public static final String DERBY_EMBEDDED_DRIVER_CLASS = "org.apache.derby.jdbc.EmbeddedDriver";
	
	/**
	 * Derby JDBC URL prefix.
	 */
	public static final String DERBY_JDBC_URL_PREFIX = "jdbc:derby:";
	
	// ------------------------------------------------------------------------------------------------ Derby properties
	
	/**
	 * Derby System Home location.
	 */
	public static final String PROP_DERBY_SYSTEM_HOME = "derby.system.home";
	
	/**
	 * Error log file location.
	 */
	public static final String PROP_DERBY_STREAM_ERROR_FILE = "derby.stream.error.file";
	
	/**
	 * Static fully qualified field of type OutputStream for error logging.
	 */
	public static final String PROP_DERBY_STREAM_ERROR_FIELD = "derby.stream.error.field";
	
	// ---------------------------------------------------------------------------- Derby JDBC Connection URL Attributes
	
	/**
	 * Separator between two properties.
	 */
	public static final String URLPROP_DERBY_SEPARATOR = ";";
	
	/**
	 * Key / value separator.
	 */
	public static final String URLPROP_DERBY_EQUAL = "=";
	
	/**
	 * Create a database.
	 */
	public static final String URLPROP_DERBY_CREATE = URLPROP_DERBY_SEPARATOR + "create" + URLPROP_DERBY_EQUAL
			+ "true";
	
	/**
	 * Shutdown a database.
	 */
	public static final String URLPROP_DERBY_SHUTDOWN = URLPROP_DERBY_SEPARATOR + "shutdown" + URLPROP_DERBY_EQUAL
			+ "true";
	
	/**
	 * Drop a database.
	 */
	public static final String URLPROP_DERBY_DROP = URLPROP_DERBY_SEPARATOR + "drop" + URLPROP_DERBY_EQUAL + "true";
	
	// ----------------------------------------------------------------------------------------------- Stored Procedures
	
	/**
	 * Online backup a database.
	 */
	public static final String SYSPROC_BACKUP_DB = "CALL SYSCS_UTIL.SYSCS_BACKUP_DATABASE(?)";
	
	/**
	 * Online backup a database, don't wait for transactions.
	 */
	public static final String SYSPROC_BACKUP_DB_NOWAIT = "CALL SYSCS_UTIL.SYSCS_BACKUP_DATABASE_NOWAIT(?)";
	
	/**
	 * Online backup a database, with archive logging.
	 */
	public static final String SYSPROC_BACKUP_DB_ENABLE_LOG_ARCHIVE =
			"CALL SYSCS_UTIL.SYSCS_BACKUP_DATABASE_AND_ENABLE_LOG_ARCHIVE_MODE(?, ?)";
	
	/**
	 * Online backup a database with archive logging, don't wait for transactions.
	 */
	public static final String SYSPROC_BACKUP_DB_ENABLE_LOG_ARCHIVE_NOWAIT =
			"CALL SYSCS_UTIL.SYSCS_BACKUP_DATABASE_AND_ENABLE_LOG_ARCHIVE_MODE_NOWAIT(?, ?)";
	
	private DerbyConstants () {
		// Constants file
	}
}
