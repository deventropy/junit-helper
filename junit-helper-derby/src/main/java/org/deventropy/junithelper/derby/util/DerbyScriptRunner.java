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
package org.deventropy.junithelper.derby.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.nio.charset.Charset;
import java.sql.Connection;

import org.apache.commons.io.IOUtils;
import org.apache.derby.tools.ij;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.deventropy.shared.utils.ArgumentCheck;
import org.deventropy.shared.utils.UrlResourceUtil;

/**
 * Executes a SQL script on the given Derby embedded instance connection.
 * 
 * <p>The script uses a single character set for reading the scripts as well as writing the logs. The class has a
 * {@link #setDefaultScriptLogStream(OutputStream) Default Log Stream} which it uses when no log stream or file is
 * specified along with the script for execution ({@link #executeScript(String)}). A log stream may be specified with
 * the script resource using the {@link #executeScript(String, File)} or
 * {@link #executeScript(String, OutputStream, boolean)} methods.
 * 
 * <p>The script resources provided to this class should be in the format supported by {@link UrlResourceUtil}.
 * 
 * @author Bindul Bhowmik
 */
public class DerbyScriptRunner {
	
	/**
	 * Default system character set to use if none is specified by the user.
	 */
	public static final String DEFAULT_CHARSET = Charset.defaultCharset().name();
	
	private final Logger log = LogManager.getLogger();
	
	/**
	 * DB connection to use
	 */
	private final Connection dbConnection;
	
	/**
	 * Character set to use for this script runner
	 */
	private final String charset;
	
	/**
	 * Default script log stream if none is specified with the script.
	 */
	private OutputStream defaultScriptLogStream;
	
	/**
	 * Initializes a new script runner.
	 * 
	 * @param dbConnection The database connection to run the scripts on.
	 * @param charset Character set to use for reading the script and writing the log
	 */
	public DerbyScriptRunner (final Connection dbConnection, final String charset) {
		this.dbConnection = dbConnection;
		this.charset = charset;
		this.defaultScriptLogStream = DerbyUtils.DEV_NULL;
	}
	
	/**
	 * Initializes a new script runner for the connection. Uses the {@link #DEFAULT_CHARSET} as the character set.
	 * 
	 * @param dbConnection The database connection to run the scripts on.
	 */
	public DerbyScriptRunner (final Connection dbConnection) {
		this(dbConnection, DEFAULT_CHARSET);
	}
	
	/**
	 * Sets the log stream to log results to. This can be overridden on a per script basis, and if not set defaults to
	 * {@link DerbyUtils#DEV_NULL} stream, which simply ignores all output.
	 * 
	 * <p>Data will be written to this stream using the character set set in the constructor.
	 * 
	 * @param defaultScriptLogStream The default script log stream
	 */
	public void setDefaultScriptLogStream (final OutputStream defaultScriptLogStream) {
		ArgumentCheck.notNull(defaultScriptLogStream, "Default Script Log Stream");
		this.defaultScriptLogStream = defaultScriptLogStream;
	}
	
	/**
	 * Executes the given script. The script run logs are written to the
	 * {@link #setDefaultScriptLogStream(OutputStream)}.
	 * 
	 * @param scriptResource The script source; should be in a format compatible with {@linkplain UrlResourceUtil}
	 * @return <code>0</code> if the execution succeeds without any errors; <code>non zero</code> value otherwise.
	 * @throws IOException Error reading script file, or writing to the log file.
	 */
	public int executeScript (final String scriptResource) throws IOException {
		return executeScript(scriptResource, defaultScriptLogStream, false);
	}
	
	/**
	 * Executes the given script. The script run logs are written to the provided <code>scriptLogFile</code>.
	 * 
	 * @param scriptResource The script source; should be in a format compatible with {@linkplain UrlResourceUtil}
	 * @param scriptLogFile The file to which the output logs are written. If the file already exists, it is appended to
	 * @return <code>0</code> if the execution succeeds without any errors; <code>non zero</code> value otherwise.
	 * @throws IOException Error reading script file, or writing to the log file.
	 */
	public int executeScript (final String scriptResource, final File scriptLogFile) throws IOException {
		try {
			final OutputStream scriptLogStream = new FileOutputStream(scriptLogFile, true);
			return executeScript(scriptResource, scriptLogStream, true);
		} catch (FileNotFoundException e) {
			log.error("Error opening script log stream: {}", scriptLogFile);
			throw new IOException("Error opening script log stream", e);
		}
	}
	
	/**
	 * Executes the given script. The script run logs are written to the provided <code>scriptLogStream</code>.
	 * 
	 * @param script The script source; should be in a format compatible with {@linkplain UrlResourceUtil}
	 * @param scriptLogStream The stream to which the output logs are written.
	 * @param closeScriptLogStream If set to <code>true</code>, the log stream is closed at the end of the execution.
	 * @return <code>0</code> if the execution succeeds without any errors; <code>non zero</code> value otherwise.
	 * @throws IOException Error reading script file, or writing to the log file.
	 */
	public int executeScript (final String script, final OutputStream scriptLogStream,
			final boolean closeScriptLogStream) throws IOException {

		InputStream scriptStream = null;

		try {

			final URL scriptUrl = UrlResourceUtil.getUrl(script);
			scriptStream = scriptUrl.openStream();
	
			log.debug("Executing script: {}", script);
			final int exceptionCount = ij.runScript(dbConnection, scriptStream, charset,
					scriptLogStream, charset);
			if (exceptionCount > 0) {
				log.warn("Error executing script {}. See output for details", script);
			}
			return exceptionCount;

		} catch (UnsupportedEncodingException e) {
			log.warn("Incompatible encoding for script file. Current characterset {0}", charset);
			throw e;
		} catch (IOException e) {
			log.warn("Error opening or reading script file: {0}", script);
			throw e;
		} finally {
			IOUtils.closeQuietly(scriptLogStream);
			IOUtils.closeQuietly(scriptStream);
		}
	}
}

