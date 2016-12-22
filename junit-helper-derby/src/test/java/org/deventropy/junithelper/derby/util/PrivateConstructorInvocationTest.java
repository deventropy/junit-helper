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
package org.deventropy.junithelper.derby.util;

import static org.junit.Assert.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

import org.deventropy.junithelper.derby.DerbyConstants;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * A stupid test to ensure JaCoCo is happy. See <a href="https://github.com/jacoco/jacoco/issues/15">Filtering options
 * for coverage analysis</a>, specifically on private constructors.
 * 
 * <p>Also makes sure private constructors stay private :-)
 * 
 * @author Bindul Bhowmik
 */
@RunWith(Parameterized.class)
public class PrivateConstructorInvocationTest {
	
	private final Class<?> typeToCheck;

	/**
	 * @param typeToCheck The type to check.
	 */
	public PrivateConstructorInvocationTest (final Class<?> typeToCheck) {
		this.typeToCheck = typeToCheck;
	}
	
	/**
	 * Creates data for the parameterized test.
	 * @return Data for the parameterized test
	 */
	@Parameters (name = "{index} - testing {0}")
	public static Object[] typesToCheck () {
		return new Object[] {
			DerbyUtils.class,
			DerbyConstants.class
		};
	}
	
	@Test(expected = IllegalAccessException.class)
	public void testConstructorIsPrivate () throws Exception {
		final Constructor<?> cons = typeToCheck.getDeclaredConstructor();
		assertTrue(Modifier.isPrivate(cons.getModifiers()));
		cons.newInstance();
	}
	
	@Test
	public void testAccessibleConstructor () throws Exception {
		final Constructor<?> cons = typeToCheck.getDeclaredConstructor();
		cons.setAccessible(true);
		assertNotNull(cons.newInstance());
	}
}
