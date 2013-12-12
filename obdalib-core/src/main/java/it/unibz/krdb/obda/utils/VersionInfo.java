package it.unibz.krdb.obda.utils;

/*
 * #%L
 * ontop-obdalib-core
 * %%
 * Copyright (C) 2009 - 2013 Free University of Bozen-Bolzano
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

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
