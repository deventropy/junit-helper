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
package org.deventropy.junithelper.derby.datasource;

import javax.sql.ConnectionPoolDataSource;
import javax.sql.DataSource;
import javax.sql.XADataSource;

/**
 * Factory for creating {@link DataSource} instances to connect to a Derby resource. The specific resources provide
 * implementations to this factory. Typically all three kinds of <code>DataSource</code>s ({@link DataSource},
 * {@link ConnectionPoolDataSource} and {@link XADataSource}) are supported by the factory instances.
 * 
 * <p>Factory implementations can support caching of the data sources created; and cached instances of the datasource
 * are retrieved by setting the <code>cachedInstance</code> parameter to <code>true</code>. If an implementation does
 * not support caching, this parameter is simply ignored and a new instance is returned every time.
 * 
 * <p>Derby resources may optionally check if the derby instance is active, and if so may throw
 * {@link IllegalStateException} if the <code>DataSource</code> factory methods are invoked while the underlying
 * instance is not active.
 * 
 * @see <a href="https://db.apache.org/derby/docs/10.12/ref/rrefapi1003363.html">Derby DataSource classes</a>
 * 
 * @author Bindul Bhowmik
 */
public interface EmbeddedDerbyDataSourceFactory {

	/**
	 * Returns a new or cached {@link DataSource} instance with parameters set up to create connections to connect to
	 * the underlying Derby instance.
	 * 
	 * <p>For the embedded Derby instance, tha actual instance returned is usually
	 * {@link org.apache.derby.jdbc.EmbeddedDataSource}.
	 * 
	 * @param cachedInstance Should a cached instance be returned if available and caching is enabled.
	 * @return A DataSource instance
	 * @throws IllegalStateException If the underlying implementation does state checking and the resource is not active
	 */
	DataSource getDataSource (boolean cachedInstance);
	
	/**
	 * Returns a new or cached {@link ConnectionPoolDataSource} instance with parameters set up to create connections to
	 * connect to the underlying Derby instance.
	 * 
	 * <p>For the embedded Derby instance, tha actual instance returned is usually
	 * {@link org.apache.derby.jdbc.EmbeddedConnectionPoolDataSource}.
	 * 
	 * @param cachedInstance Should a cached instance be returned if available and caching is enabled.
	 * @return A ConnectionPoolDataSource instance
	 * @throws IllegalStateException If the underlying implementation does state checking and the resource is not active
	 */
	ConnectionPoolDataSource getConnectionPoolDataSource (boolean cachedInstance);
	
	/**
	 * Returns a new or cached {@link XADataSource} instance with parameters set up to create connections to
	 * connect to the underlying Derby instance.
	 * 
	 * <p>For the embedded Derby instance, tha actual instance returned is usually
	 * {@link org.apache.derby.jdbc.EmbeddedXADataSource}.
	 * 
	 * @param cachedInstance Should a cached instance be returned if available and caching is enabled.
	 * @return A XADataSource instance
	 * @throws IllegalStateException If the underlying implementation does state checking and the resource is not active
	 */
	XADataSource getXADataSource (boolean cachedInstance);
}
