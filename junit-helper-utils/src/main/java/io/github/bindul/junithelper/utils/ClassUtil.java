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
package io.github.bindul.junithelper.utils;

/**
 * Utility methods around classes and class loaders.
 * 
 * @author Bindul Bhowmik
 */
public class ClassUtil {

	private ClassUtil () {
		// Util class
	}

	/**
	 * Searches for and finds an appropriate class loader to use. Tries the following in order:
	 * 
	 * <ol>
	 * 	<li>The context class loader</li>
	 * 	<li>The caller's class loader</li>
	 * 	<li>This classes class loader</li>
	 * 	<li>The system class loader</li>
	 * </ol>
	 * 
	 * @return Find the classloader
	 */
	public static ClassLoader getApplicableClassloader (Object caller) {
		ClassLoader cl = null;
		
		try {
			cl = Thread.currentThread().getContextClassLoader();
		} catch (SecurityException e) {
			// log.catching(e); // Cannot get to the context
		}
		
		if (null == cl) {
			if (null != caller) {
				cl = caller.getClass().getClassLoader();
			}
			if (null == cl) {
				cl = ClassUtil.class.getClassLoader();
				if (null == cl) {
					cl = ClassLoader.getSystemClassLoader();
				}
			}
		}
		
		return cl;
	}
}
