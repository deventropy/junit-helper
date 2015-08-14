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

import java.util.UUID;

import io.github.bindul.junithelper.utils.ArgumentCheck;

/**
 * Configurations to control the {@link EmbeddedInMemoryDerbyResource}.
 * 
 * <p>
 * This class provides a fluid interface to build the config object to pass to the constructor of the resource. Example
 * usage:
 * 
 * <pre>
 * EmbeddedInMemoryDerbyResource derbyResource = new EmbeddedInMemoryDerbyResource(
 * 		EmbeddedInMemoryDerbyConfig.buildDefault().);
 * </pre>
 * </p>
 * 
 * <p>
 * Some methods in this class, for example the methods setting the subSubProtocol, can be mutually exclusive and unset / 
 * alter values set by other methods. Certain configuration values have valid values as <code>null</code> and do not
 * have corresponding <code>getDefaultXXX</code> methods.
 * </p>
 * 
 * @author Bindul Bhowmik
 */
public class EmbeddedInMemoryDerbyConfig {
	
	/**
	 * Derby Sub-Sub Protocols
	 * 
	 * @see http://db.apache.org/derby/docs/10.11/ref/rrefjdbc37352.html
	 * 
	 * @author Bindul Bhowmik
	 */
	public enum SubSubProtocols {
		/**
		 * Database is a directory
		 */
		directory,
		/**
		 * In memory database
		 */
		memory,
		/**
		 * Read only database on the classpath
		 */
		classpath,
		/**
		 * Read only database in a jar file
		 */
		jar;
	}

	private String databaseName;
	
	// TODO have combined setters for the sub protocols (with other required values)
	private SubSubProtocols subSubProtocol;
	
	/**
	 * Sets up a default config that can be used as is to start a database. See the appropriate <code>getDefaultXXX</code>
	 * methods to see the default values.
	 * 
	 * @return A config setup with defaults.
	 */
	public static EmbeddedInMemoryDerbyConfig buildDefault() {
		final EmbeddedInMemoryDerbyConfig config = new EmbeddedInMemoryDerbyConfig();
		config.databaseName = getDefaultDatabaseName();
		config.subSubProtocol = getDefaultSubSubProtocol();
		// TODO Complete setting defaults
		return config;
	}
	
	/**
	 * Sets the database name to use
	 * @param databaseName Database name
	 * @return current instance
	 * @throws IllegalArgumentException if the database name is null or empty
	 */
	public EmbeddedInMemoryDerbyConfig setDatabaseName(String databaseName) {
		ArgumentCheck.notNullOrEmpty(databaseName, "databaseName");
		this.databaseName = databaseName;
		return this;
	}
	
	/**
	 * Returns the name of the database to use.
	 * 
	 * @see #getDefaultDatabaseName()
	 * @return The database name.
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
	public EmbeddedInMemoryDerbyConfig useInMemoryDatabase () {
		this.subSubProtocol = SubSubProtocols.memory;
		// TODO null out other values?
		return this;
	}
	
	/**
	 * The JDBC sub-sub protocol to use for the embedded database.
	 * 
	 * @return A valid Derby sub-sub protocol
	 */
	public SubSubProtocols getSubSubProtocol () {
		return this.subSubProtocol;
	}
	
	/**
	 * The default sub-sub protocol value
	 * @return {@link SubSubProtocols#memory}
	 */
	public static SubSubProtocols getDefaultSubSubProtocol () {
		return SubSubProtocols.memory;
	}
}
