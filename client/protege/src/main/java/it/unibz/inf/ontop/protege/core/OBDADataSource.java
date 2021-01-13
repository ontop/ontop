package it.unibz.inf.ontop.protege.core;

/*
 * #%L
 * ontop-obdalib-core
 * %%
 * Copyright (C) 2009 - 2014 Free University of Bozen-Bolzano
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


import it.unibz.inf.ontop.injection.OntopSQLCoreSettings;
import it.unibz.inf.ontop.injection.OntopSQLCredentialSettings;

import java.net.URI;
import java.util.Enumeration;
import java.util.Properties;
import java.util.UUID;

public class OBDADataSource {

	private final URI id;
	private final Properties parameters;

	/**
	 * Creates a new DataSource object
	 */
	public OBDADataSource() {
		this.id = URI.create(UUID.randomUUID().toString());
		parameters = new Properties();
		parameters.setProperty(OntopSQLCoreSettings.JDBC_DRIVER, "");
		parameters.setProperty(OntopSQLCoreSettings.JDBC_URL, "");
		parameters.setProperty(OntopSQLCredentialSettings.JDBC_PASSWORD, "");
		parameters.setProperty(OntopSQLCredentialSettings.JDBC_USER, "");
	}

	public String getDriver() {
		return parameters.getProperty(OntopSQLCoreSettings.JDBC_DRIVER);
	}

	public String getUsername() {
		return parameters.getProperty(OntopSQLCredentialSettings.JDBC_USER);
	}

	public String getPassword() {
		return parameters.getProperty(OntopSQLCredentialSettings.JDBC_PASSWORD);
	}

	public String getURL() {
		return parameters.getProperty(OntopSQLCoreSettings.JDBC_URL);
	}

	public void setUsername(String username) {
		parameters.setProperty(OntopSQLCredentialSettings.JDBC_USER, username);
	}

	public void setPassword(String password) {
		parameters.setProperty(OntopSQLCredentialSettings.JDBC_PASSWORD, password);
	}

	public void setDriver(String driver) {
		parameters.setProperty(OntopSQLCoreSettings.JDBC_DRIVER, driver);
	}

	public void setURL(String url) {
		parameters.setProperty(OntopSQLCoreSettings.JDBC_URL, url);
	}

	/**
	 * These properties are compatible with OBDAProperties' keys.
	 */
	public Properties asProperties() {
		Properties p = new Properties();
		p.put(OntopSQLCoreSettings.JDBC_NAME, getSourceID().toString());
		p.put(OntopSQLCoreSettings.JDBC_URL, getURL());
		p.put(OntopSQLCredentialSettings.JDBC_USER, getUsername());
		p.put(OntopSQLCredentialSettings.JDBC_PASSWORD, getPassword());
		p.put(OntopSQLCoreSettings.JDBC_DRIVER, getDriver());
		return p;
	}


	public URI getSourceID() {
		return id;
	}

	@Override
	public String toString() {
		StringBuilder buff = new StringBuilder();
		buff.append("DatasourceURI=").append(id.toString()).append("\n");
		Enumeration<Object> keys = parameters.keys();
		while (keys.hasMoreElements()) {
			String key = (String) keys.nextElement();
			buff.append("\n").append(key).append("=").append(parameters.getProperty(key));
		}
		return buff.toString();
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof OBDADataSource)) {
			return false;
		}
		OBDADataSource d2 = (OBDADataSource) o;
		return d2.id.equals(this.id);
	}
}
