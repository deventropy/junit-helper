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

/**
 * Derby error logging <code>(derby.log)</code> modes supported by the JUnit Derby helper.
 * 
 * @author Bindul Bhowmik
 */
public enum ErrorLoggingMode {

	/**
	 * No special config; derby will write to <code>derby.log</code> file in the
	 * <code>derby.system.home</code>.
	 */
	Default,
	
	/**
	 * No logging; equivalent to redirecting logs to <code>/dev/null</code>.
	 */
	Null;
	
	//TODO See properties around http://db.apache.org/derby/docs/10.12/ref/rrefproper13217.html
	
}
