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
 * Junit Helper Derby extensions to support working with {@link javax.sql.DataSource}s over a
 * {@link org.deventropy.junithelper.derby.EmbeddedDerbyResource} using the JUnit 4.x resource
 * {@link org.deventropy.junithelper.derby.datasource.EmbeddedDerbyDataSourceResource}.
 * 
 * <p>All the configuration and features available in <code>EmbeddedDerbyResource</code> are available in the extended
 * resource.
 * 
 * <p>All three <code>DataSource</code> types supported by derby; {@link javax.sql.DataSource},
 * {@link javax.sql.ConnectionPoolDataSource} and {@link javax.sql.XADataSource} are supported using the factory
 * interface {@link org.deventropy.junithelper.derby.datasource.EmbeddedDerbyDataSourceFactory}. An instance of the
 * datasource factory tied to the underlying resource (and database) can be obtained using the
 * {@link org.deventropy.junithelper.derby.datasource.EmbeddedDerbyDataSourceResource#getDataSourceFactory()} method to
 * enable creating datasources.
 * 
 * <p>More details are in the <a href="http://www.deventropy.org/junit-helper/junit-helper-derby/">Project Website</a>.
 * 
 * @author Bindul Bhowmik
 */
package org.deventropy.junithelper.derby.datasource;
