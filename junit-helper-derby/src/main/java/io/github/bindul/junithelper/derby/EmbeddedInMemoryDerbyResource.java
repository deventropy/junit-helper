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
package io.github.bindul.junithelper.derby;

import java.io.Closeable;
import java.io.IOException;

import org.junit.rules.ExternalResource;

/**
 * Provides an in-memory Derby resource
 * 
 * TODO Complete docs and examples
 * 
 * @author Bindul Bhowmik
 */
public class EmbeddedInMemoryDerbyResource extends ExternalResource implements Closeable {
	
	public EmbeddedInMemoryDerbyResource (EmbeddedInMemoryDerbyConfig config) {
		
	}

	/* (non-Javadoc)
	 * @see java.io.Closeable#close()
	 */
	@Override
	public void close() throws IOException {
		// TODO Auto-generated method stub
		
	}

}
