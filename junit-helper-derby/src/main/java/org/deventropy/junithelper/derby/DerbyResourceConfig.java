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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.deventropy.shared.utils.ArgumentCheck;

/**
 * Configurations to control the {@link EmbeddedDerbyResource}.
 * 
 * <p>This class provides a fluid interface to build the config object to pass to the constructor of the resource.
 * Example usage:
 * 
 * <pre>
 * EmbeddedDerbyResource derbyResource = new EmbeddedDerbyResource(
 * 		DerbyResourceConfig.buildDefault().);
 * </pre>
 * 
 * <p>
 * Some methods in this class, for example the methods setting the subSubProtocol, can be mutually exclusive and unset /
 * alter values set by other methods. Certain configuration values have valid values as <code>null</code> and do not
 * have corresponding <code>getDefaultXXX</code> methods.
 * </p>
 * 
 * @author Bindul Bhowmik
 */
public class DerbyResourceConfig {
	
	private String databaseName;
	
	// TODO have combined setters for the sub protocols (with other required values)
	private JdbcDerbySubSubProtocol subSubProtocol;
	
	// TODO Complete configuring logging
	private ErrorLoggingMode errorLoggingMode;
	
	private List<String> postInitScripts;
	
	/**
	 * Sets up a default config that can be used as is to start a database. See the appropriate
	 * <code>getDefaultXXX</code> methods to see the default values.
	 * 
	 * @return A config setup with defaults.
	 */
	public static DerbyResourceConfig buildDefault () {
		final DerbyResourceConfig config = new DerbyResourceConfig();
		config.databaseName = getDefaultDatabaseName();
		config.subSubProtocol = getDefaultSubSubProtocol();
		config.errorLoggingMode = getDefaultErrorLoggingMode();
		// TODO Complete setting defaults
		return config;
	}
	
	/**
	 * Sets the database name to use.
	 * @param dbName Database name
	 * @return current instance
	 * @throws IllegalArgumentException if the database name is null or empty
	 */
	public DerbyResourceConfig setDatabaseName (final String dbName) {
		ArgumentCheck.notNullOrEmpty(dbName, "databaseName");
		this.databaseName = dbName;
		return this;
	}
	
	/**
	 * Returns the name of the database to use.
	 * 
	 * <p>Consult the documentation for Derby Sub Sub Protocols on database name formats and use.
	 * 
	 * @return The database name.
	 * @see #getDefaultDatabaseName()
	 * @see <a href="http://db.apache.org/derby/docs/10.11/ref/rrefjdbc37352.html">Syntax of db connection URLs</a>
	 */
	public String getDatabaseName () {
		return databaseName;
	}
	
	/**
	 * Returns the default database name value, which is a UUID string.
	 * @return The default database name.
	 */
	public static String getDefaultDatabaseName () {
		return UUID.randomUUID().toString();
	}
	
	/**
	 * Will have the database start up as an in-memory database.
	 * 
	 * @return This instance
	 */
	public DerbyResourceConfig useInMemoryDatabase () {
		this.subSubProtocol = JdbcDerbySubSubProtocol.Memory;
		// TODO null out other values?
		return this;
	}
	
	/**
	 * The JDBC sub-sub protocol to use for the embedded database.
	 * 
	 * @return A valid Derby sub-sub protocol
	 */
	public JdbcDerbySubSubProtocol getSubSubProtocol () {
		return this.subSubProtocol;
	}
	
	/**
	 * The default sub-sub protocol value.
	 * @return {@link JdbcDerbySubSubProtocol#Memory}
	 */
	public static JdbcDerbySubSubProtocol getDefaultSubSubProtocol () {
		return JdbcDerbySubSubProtocol.Memory;
	}
	
	/**
	 * Sets the {@link #getErrorLoggingMode()} value to {@link ErrorLoggingMode#Null}; and clears other logging
	 * properties.
	 * 
	 * @return This instance
	 */
	public DerbyResourceConfig useDevNullErrorLogging () {
		this.errorLoggingMode = ErrorLoggingMode.Null;
		//TODO clean out other values?
		return this;
	}
	
	/**
	 * Sets the {@link #getErrorLoggingMode()} value to {@link ErrorLoggingMode#Default}; and clears other logging
	 * properties.
	 * 
	 * @return This instance
	 */
	public DerbyResourceConfig useDefaultErrorLogging () {
		this.errorLoggingMode = ErrorLoggingMode.Default;
		//TODO clean out other values?
		return this;
	}
	
	/**
	 * The configured error logging mode.
	 * @return the configured error logging mode
	 */
	public ErrorLoggingMode getErrorLoggingMode () {
		return errorLoggingMode;
	}
	
	/**
	 * The default logging setup.
	 * @return {@link ErrorLoggingMode#Default}
	 */
	public static ErrorLoggingMode getDefaultErrorLoggingMode () {
		return ErrorLoggingMode.Default;
	}
	
	/**
	 * Gets the configured post init scripts in the config; or an empty list.
	 * @return Post init scripts to execute
	 */
	public List<String> getPostInitScripts () {
		if (null == postInitScripts) {
			return Collections.emptyList();
		}
		return postInitScripts;
	}
	
	/**
	 * Adds a post init script to the config.
	 * 
	 * <p>A post init script should refer to a file containing SQL DDL or DML statements that will be executed
	 * against the new derby instance. Script locations can be on the classpath or file system or on a HTTP(s)
	 * location, for format, see {@linkplain org.deventropy.junithelper.utils.UrlResourceUtil}.
	 * 
	 * @param postInitScript A post init script to add
	 * @return this object
	 */
	public DerbyResourceConfig addPostInitScript (final String postInitScript) {
		ArgumentCheck.notNullOrEmpty(postInitScript, "Post Init Script");
		if (null == postInitScripts) {
			postInitScripts = new ArrayList<>();
		}
		postInitScripts.add(postInitScript);
		return this;
	}
}
