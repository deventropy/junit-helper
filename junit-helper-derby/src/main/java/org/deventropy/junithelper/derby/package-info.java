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
/**
 * JUnit Helper is a utility to allow testing JDBC related code against Apache Derby embedded instances using JUnit
 * version 4.x.
 * 
 * <p>The library allows the user to configure an embedded Derby instance
 * ({@link org.deventropy.junithelper.derby.EmbeddedDerbyResource}) as an {@link org.junit.rules.ExternalResource}
 * which can be added to a test class using the {@link org.junit.Rule} or {@link org.junit.ClassRule} annotation,
 * with initialization scripts; and JUnit managing the initialization and de-initialization of the database instance.
 * 
 * <p>The runtime instance can be configured using the {@link org.deventropy.junithelper.derby.DerbyResourceConfig}.
 * 
 * <p>The library also comes with some utilities to manage the running instance
 * (see {@link org.deventropy.junithelper.derby.DerbyUtils}), running SQL scripts against the instance (or any derby
 * instance for that matter) - {@link org.deventropy.junithelper.derby.DerbyScriptRunner}, etc.
 * 
 * <p>More details are in the <a href="http://www.deventropy.org/junit-helper/junit-helper-derby/">Project Website</a>.
 * 
 * @author Bindul Bhowmik
 */
package org.deventropy.junithelper.derby;
