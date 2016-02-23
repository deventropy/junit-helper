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
 * When creating or restoring a database from a backup copy, Derby supports one of three modes.
 * 
 * @author Bindul Bhowmik
 */
public enum DbCreateFromRestroreMode {

	/**
	 * Supports <a href="http://db.apache.org/derby/docs/10.12/adminguide/tadminhubbkup44.html">Restoring a database
	 * from a backup copy</a>.
	 */
	RestoreFrom ("restoreFrom", false),
	
	/**
	 * Supports <a href="http://db.apache.org/derby/docs/10.12/adminguide/tadmincrtdbbkup.html">Creating a database from
	 * a backup copy</a>.
	 */
	CreateFrom ("createFrom", false),
	
	/**
	 * Supports <a href="http://db.apache.org/derby/docs/10.12/adminguide/cadminrollforward.html">Roll-forward recovery
	 * </a>.
	 */
	RollForwardRecoveryFrom ("rollForwardRecoveryFrom", true);
	
	/**
	 * Log device property.
	 */
	public static final String URLPROP_DERBY_LOGDEVICE = "logDevice";
	
	/**
	 * The attribute attached to the connect JDBC URL
	 */
	private final String urlAttribute;
	
	/**
	 * If the recovery mode supports a log device to get archive logs from
	 */
	private final boolean requiresLogDevice;
	
	DbCreateFromRestroreMode (final String urlAttribute, final boolean requiresLogDevice) {
		this.urlAttribute = urlAttribute;
		this.requiresLogDevice = requiresLogDevice;
	}
	
	/**
	 * The attribute key to add to the JDBC connection URL.
	 * 
	 * @return The URL attribute to use for the backup location.
	 */
	public String urlAttribute () {
		return this.urlAttribute;
	}
	
	/**
	 * If the recovery mode requires the archive log recovery.
	 * 
	 * @return <code>true</code> for Roll-forward recovery.
	 */
	public boolean requiresLogDevice () {
		return requiresLogDevice;
	}
}
