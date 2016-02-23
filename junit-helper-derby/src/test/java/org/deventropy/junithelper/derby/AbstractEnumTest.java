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

import static org.junit.Assert.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.junit.Test;

/**
 * @param <E> The enum under teat
 * 
 * @author Bindul Bhowmik
 */
public abstract class AbstractEnumTest<E extends Enum<E>> {
	
	private final Class<E> enumType;
	private final String[] values;
	private final Method valueOfMethod;
	private final Method valuesMethod;
	
	/**
	 * Base enumeration test.
	 * @param enumType Enum type
	 * @param values values
	 * @throws Exception Reflection errors
	 */
	public AbstractEnumTest (final Class<E> enumType, final String[] values) throws Exception {
		this.enumType = enumType;
		this.values = values;
		this.valueOfMethod = enumType.getDeclaredMethod("valueOf", String.class);
		this.valuesMethod = enumType.getDeclaredMethod("values");
	}

	@Test
	public void testValues () throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		for (String value : values) {
			assertNotNull(Enum.valueOf(enumType, value));
			@SuppressWarnings("unchecked")
			final E enumValue = (E) valueOfMethod.invoke(null, value);
			assertNotNull(enumValue);
		}
	}
	
	@Test
	public void testAllValues () throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		@SuppressWarnings("unchecked")
		final E[] allValues = (E[]) valuesMethod.invoke(null);
		assertEquals(values.length, allValues.length);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testIllegalArgumentException () {
		Enum.valueOf(enumType, "");
		fail("Should not reach here");
	}
	
	@Test(expected = NullPointerException.class)
	public void testNullPointerException () {
		Enum.valueOf(enumType, null);
		fail("Should not reach here");
	}
}
