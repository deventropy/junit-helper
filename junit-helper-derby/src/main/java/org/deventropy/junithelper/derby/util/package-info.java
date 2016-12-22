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
/**
 * Utility classes as part of the Deventropy Junit Helper Derby package.
 * 
 * <p><b>Note:</b> Utilities are created primarily as helper classes/methods for the library. They may be used by
 * consumers, should they find them useful; however the classes in this package are not part of the standard library
 * API and may change at any time without notice.
 * 
 * <p>Classes in this package include:
 * <ul>
 * <li>Utilities to manage the running instance (see {@link org.deventropy.junithelper.derby.util.DerbyUtils})</li>
 * <li>Running SQL scripts against the instance (or any derby instance for that matter) -
 * {@link org.deventropy.junithelper.derby.util.DerbyScriptRunner}</li>
 * <li>Backing up a running instance (see {@link org.deventropy.junithelper.derby.util.DerbyBackupOperationsHelper})
 * </li>
 * </ul>
 * 
 * <p>More details are in the <a href="http://www.deventropy.org/junit-helper/junit-helper-derby/">Project Website</a>.
 * 
 * @author Bindul Bhowmik
 */
package org.deventropy.junithelper.derby.util;
