/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package it.unibz.krdb.obda.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class VersionInfo {
	
	private static VersionInfo instance;

	private String version;

	private VersionInfo() {
		Properties prop = new Properties();
    	try {
            // Load the properties file
    		InputStream inputStream = VersionInfo.class.getResourceAsStream("version.properties");
    		prop.load(inputStream);
 
            // Get the property value
            version = prop.getProperty("pluginVersion"); 
    	} catch (IOException ex) {
    		ex.printStackTrace();
        }
	}

	public synchronized static VersionInfo getVersionInfo() {
		if (instance == null) {
			instance = new VersionInfo();
		}
		return instance;
	}

	/**
	 * Gets a string that contains the version of this build. This is generated
	 * from the manifest of the jar that this class is packaged in.
	 * 
	 * @return The version info string (if available).
	 */
	public String getVersion() {
		return version;
	}
	
	@Override
	public String toString() {
		return "Using -ontopCore- API v" + version;
	}
}
