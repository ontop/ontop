/* 
 * Licensed to Aduna under one or more contributor license agreements.  
 * See the NOTICE.txt file distributed with this work for additional 
 * information regarding copyright ownership. 
 *
 * Aduna licenses this file to you under the terms of the Aduna BSD 
 * License (the "License"); you may not use this file except in compliance 
 * with the License. See the LICENSE.txt file distributed with this work 
 * for the full License.
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
 * implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */
package org.openrdf.workbench.proxy;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;

import javax.servlet.ServletConfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Validates a server
 */
class ServerValidator {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ServerValidator.class);
	private static final String ACCEPTED_SERVER = "accepted-server-prefixes";
	private final String prefixes;

	protected ServerValidator(final ServletConfig config) {
		this.prefixes = config.getInitParameter(ACCEPTED_SERVER);
	}
	
	private boolean isDirectory(final String server) {
		boolean isDir = false;
		try {
			final URL url = new URL(server);
			isDir = asLocalFile(url).isDirectory();
		}
		catch (MalformedURLException e) {
			LOGGER.warn(e.toString(), e);
		}
		catch (IOException e) {
			LOGGER.warn(e.toString(), e);
		}
		return isDir;
	}

	/**
	 * Returns whether the given server can be connected to.
	 * 
	 * @param server
	 *        the server path
	 * @param password
	 *        the optional password
	 * @param user
	 *        the optional username
	 * @return true, if the given server can be connected to
	 */
	protected boolean isValidServer(final String server) {
		boolean isValid = checkServerPrefixes(server);
		if (isValid) {
			if (server.startsWith("http")) {
				isValid = canConnect(server);
			}
			else if (server.startsWith("file:")) {
				isValid = isDirectory(server);
			}
		}
		return isValid;
	}
	
	/**
	 * Returns whether the server prefix is in the list of acceptable prefixes,
	 * as given by the space-separated configuration parameter value for
	 * 'accepted-server-prefixes'.
	 * 
	 * @param server
	 *        the server for which to check the prefix
	 * @return whether the server prefix is in the list of acceptable prefixes
	 */
	private boolean checkServerPrefixes(final String server) {
		boolean accept = false;
		if (prefixes == null) {
			accept = true;
		}
		else {
			for (String prefix : prefixes.split(" ")) {
				if (server.startsWith(prefix)) {
					accept = true;
					break;
				}
			}
		}
		if (!accept) {
			LOGGER.warn("server URL {} does not have a prefix {}", server, prefixes);
		}
		return accept;
	}
	
	/**
	 * Assumption: server won't require credentials to access the
	 * protocol path.
	 */
	private boolean canConnect(final String server) {
		boolean success = false;
		try {
			final URL url = new URL(server + "/protocol");
			final BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
			try {
				Integer.parseInt(reader.readLine());
				success = true;
			}
			finally {
				reader.close();
			}
		}
		catch (MalformedURLException e) {
			LOGGER.warn(e.toString(), e);
		}
		catch (IOException e) {
			LOGGER.warn(e.toString(), e);
		}
		return success;
	}
	
	private File asLocalFile(final URL rdf)
			throws UnsupportedEncodingException
		{
			return new File(URLDecoder.decode(rdf.getFile(), "UTF-8"));
		}
}