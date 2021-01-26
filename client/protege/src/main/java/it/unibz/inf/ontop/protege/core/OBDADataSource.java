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
import java.util.*;

public class OBDADataSource {

	private final URI id;
	private String driver = "", url = "", username = "", password = "";

	private final List<Listener> listeners = new ArrayList<>();

	public interface Listener {
		void changed();
	}

	public OBDADataSource() {
		this.id = URI.create(UUID.randomUUID().toString());
	}

	public void addListener(Listener listener) {
		listeners.add(listener);
	}

	public void removeListener(Listener listener) {
		listeners.remove(listener);
	}

	public String getDriver() {
		return driver;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	public String getURL() {
		return url;
	}

	public void reset() {
		driver = "";
		url = "";
		username = "";
		password = "";
	}

	public void fireChanged() {
		listeners.forEach(Listener::changed);
	}

	public void setUsername(String username) {
		Objects.requireNonNull(username);
		this.username = username;
	}

	public void setPassword(String password) {
		Objects.requireNonNull(password);
		this.password = password;
	}

	public void setDriver(String driver) {
		Objects.requireNonNull(driver);
		this.driver = driver;
	}

	public void setURL(String url) {
		Objects.requireNonNull(url);
		this.url = url;
	}

	public Properties asProperties() {
		Properties p = new Properties();
		p.put(OntopSQLCoreSettings.JDBC_NAME, id.toString());
		p.put(OntopSQLCoreSettings.JDBC_URL, url);
		p.put(OntopSQLCredentialSettings.JDBC_USER, username);
		p.put(OntopSQLCredentialSettings.JDBC_PASSWORD, password);
		p.put(OntopSQLCoreSettings.JDBC_DRIVER, driver);
		return p;
	}

	@Override
	public String toString() {
		return "DatasourceURI=" + id + "\n" +
				OntopSQLCoreSettings.JDBC_URL + "=" + url + "\n" +
				OntopSQLCoreSettings.JDBC_DRIVER + "=" + driver + "\n" +
				OntopSQLCredentialSettings.JDBC_USER + "=" + username + "\n" +
				OntopSQLCredentialSettings.JDBC_PASSWORD + "=" + password + "\n";
	}
}
