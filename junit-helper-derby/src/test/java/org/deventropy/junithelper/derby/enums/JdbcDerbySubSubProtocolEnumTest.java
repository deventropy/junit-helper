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
package org.deventropy.junithelper.derby.enums;

import static org.junit.Assert.assertEquals;

import org.deventropy.junithelper.derby.JdbcDerbySubSubProtocol;
import org.junit.Test;

/**
 * @author Bindul Bhowmik
 *
 */
public class JdbcDerbySubSubProtocolEnumTest extends AbstractEnumTest<JdbcDerbySubSubProtocol> {

	/**
	 * JdbcDerbySubSubProtocol enum test.
	 * @throws Exception reflection errors
	 */
	public JdbcDerbySubSubProtocolEnumTest () throws Exception {
		super (JdbcDerbySubSubProtocol.class, new String[]{"Memory", "Directory", "Jar", "Classpath"});
	}
	
	@Test
	public void testSubSubProtocol () {
		assertEquals("Not expected format of JDBC connection string prefix", "jdbc:derby:memory:",
				JdbcDerbySubSubProtocol.Memory.jdbcConnectionPrefix());

		assertEquals("Not expected format of JDBC connection string prefix", "jdbc:derby:directory:",
				JdbcDerbySubSubProtocol.Directory.jdbcConnectionPrefix());

		assertEquals("Not expected format of JDBC connection string prefix", "jdbc:derby:jar:",
				JdbcDerbySubSubProtocol.Jar.jdbcConnectionPrefix());

		assertEquals("Not expected format of JDBC connection string prefix", "jdbc:derby:classpath:",
				JdbcDerbySubSubProtocol.Classpath.jdbcConnectionPrefix());
	}
}
