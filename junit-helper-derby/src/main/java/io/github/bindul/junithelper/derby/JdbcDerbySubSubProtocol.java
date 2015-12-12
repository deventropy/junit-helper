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

/**
 * Embedded Derby JDBC Sub-Protocols supported by this tool.
 * 
 * @see <a href="http://db.apache.org/derby/docs/10.12/ref/rrefjdbc37352.html">Syntax of database connection URLs</a>
 * 
 * @author Bindul Bhowmik
 */
public enum JdbcDerbySubSubProtocol {

	/**
	 * In Memory database
	 */
	Memory ("memory");
	
	/*
	 * Future support:
	 * /**
		 * Database is a directory
		 * /
		directory,
		/**
		 * Read only database on the classpath
		 * /
		classpath,
		/**
		 * Read only database in a jar file
		 * /
		jar;
	 */
	
	private static final String JDBC_CONN_STR_PREFIX = "jdbc:derby:";
	
	private final String jdbcConnectionPrefix;
	
	private JdbcDerbySubSubProtocol (final String subprotocolcode) {
		jdbcConnectionPrefix = JDBC_CONN_STR_PREFIX + subprotocolcode + ":";
	}
	
	/**
	 * Get the JDBC connection string prefix to construct a JDBC connection string with this sub-protocol.
	 * 
	 * @return A string of the format <code>jdbc:derby:sub-protocol:</code>
	 */
	public String jdbcConnectionPrefix () {
		return jdbcConnectionPrefix;
	}
}
