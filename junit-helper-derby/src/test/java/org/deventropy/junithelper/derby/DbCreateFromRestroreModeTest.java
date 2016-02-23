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
 * @author Bindul Bhowmik
 *
 */
public class DbCreateFromRestroreModeTest extends AbstractEnumTest<DbCreateFromRestroreMode> {

	/**
	 * DbCreateFromRestroreMode enum test.
	 * @throws Exception reflection errors
	 */
	public DbCreateFromRestroreModeTest () throws Exception {
		super (DbCreateFromRestroreMode.class, new String[] {"RestoreFrom", "CreateFrom", "RollForwardRecoveryFrom"});
	}
}
