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

import java.io.IOException;
import java.io.OutputStream;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Random utilities for this library and also useful when writing JDBC test cases.
 * 
 * @author Bindul Bhowmik
 */
public final class DerbyUtils {
	
	/**
	 * Stream used for DEV_NULL logging.
	 */
	public static final OutputStream DEV_NULL = new OutputStream() {
		@Override
		public void write (final int b) throws IOException {
			// Derby log > /dev/null
		}
	};
	
	/**
	 * A string ID that may be used to refer to {@link #DEV_NULL} from property values (used by Derby).
	 */
	public static final String DEV_NULL_FIELD_ID = DerbyUtils.class.getName() + ".DEV_NULL";
	
	private static Logger log = LogManager.getLogger();
	
	private DerbyUtils () {
		// Utility class
	}
	
	/**
	 * Shuts down the derby system so it can be reloaded; per Derby developer guide. For more information see
	 * <a href="https://db.apache.org/derby/docs/10.12/devguide/tdevdvlp20349.html">Derby System</a>.
	 * 
	 * @param supressLog If the log statement should be ignored.
	 */
	public static void shutdownDerbySystemQuitely (final boolean supressLog) {
		// See https://db.apache.org/derby/docs/10.12/devguide/tdevdvlp20349.html
		try {
			DriverManager.getConnection("jdbc:derby:;shutdown=true");
		} catch (SQLException e) {
			if (!supressLog) {
				log.catching(Level.DEBUG, e);
			}
		}
	}
	
	/**
	 * Quietly close an {@linkplain AutoCloseable} resource, like a SQL Connection, Statement, etc.
	 * 
	 * @param resource The resource to close
	 */
	public static void closeQuietly (final AutoCloseable resource) {
		try {
			if (null != resource) {
				resource.close();
			}
		} catch (Exception e) {
			// Ignore
			log.catching(Level.TRACE, e);
		}
	}
}
