package it.unibz.inf.ontop.protege.connection;

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


import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.injection.OntopSQLCoreSettings;
import it.unibz.inf.ontop.injection.OntopSQLCredentialSettings;
import it.unibz.inf.ontop.protege.utils.DialogUtils;
import it.unibz.inf.ontop.protege.utils.EventListenerList;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.*;
import java.net.URI;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;

import static it.unibz.inf.ontop.injection.OntopSQLCoreSettings.JDBC_DRIVER;
import static it.unibz.inf.ontop.injection.OntopSQLCoreSettings.JDBC_URL;
import static it.unibz.inf.ontop.injection.OntopSQLCredentialSettings.JDBC_PASSWORD;
import static it.unibz.inf.ontop.injection.OntopSQLCredentialSettings.JDBC_USER;

public class DataSource {

	private static final Logger LOGGER = LoggerFactory.getLogger(DataSource.class);
	
	private final Properties properties = new Properties();

	private final URI id;
	private String driver = "", url = "", username = "", password = "";

	private final EventListenerList<DataSourceListener> listeners = new EventListenerList<>();

	private Connection connection;

	/*  TODO: default parameters for creating statements (to be enforced?)
			JDBC_AUTOCOMMIT, false
			JDBC_FETCHSIZE, 100
			JDBC_RESULTSETCONCUR, ResultSet.CONCUR_READ_ONLY
			JDBC_RESULTSETTYPE, ResultSet.TYPE_FORWARD_ONLY
		 */


	public DataSource() {
		this.id = URI.create(UUID.randomUUID().toString());
	}

	/**
	 * Closes the connection quietly
	 */
	public void dispose() {
		try {
			if (connection != null) {
				connection.close();
				connection = null;
			}
		}
		catch (Exception e) {
			LOGGER.error(e.getMessage());
		}
	}


	@Nonnull
	public String getDriver() {
		return driver;
	}

	@Nonnull
	public String getUsername() {
		return username;
	}

	@Nonnull
	public String getPassword() {
		return password;
	}

	@Nonnull
	public String getURL() {
		return url;
	}

	public void set(String url, String username, String password, String driver) {
		Objects.requireNonNull(url);
		Objects.requireNonNull(username);
		Objects.requireNonNull(password);
		Objects.requireNonNull(driver);

		boolean changed = !this.url.equals(url) || !this.username.equals(username)
					|| !this.password.equals(password) || !this.driver.equals(driver);

		this.url = url;
		this.username = username;
		this.password = password;
		this.driver = driver;

		if (changed) {
			dispose();
			listeners.fire(l -> l.changed(this));
		}
	}

	/**
	 * Retrieves the connection object.
	 * If the connection doesn't exist or is dead, it will create a new connection.
	 */
	public Connection getConnection() throws SQLException {
		if (connection == null || connection.isClosed())
			connection = DriverManager.getConnection(url, username, password);

		return connection;
	}

	public Properties asProperties() {
		Properties p = new Properties();
		p.putAll(properties);
		p.put(OntopSQLCoreSettings.JDBC_URL, url);
		p.put(OntopSQLCredentialSettings.JDBC_USER, username);
		p.put(OntopSQLCredentialSettings.JDBC_PASSWORD, password);
		p.put(OntopSQLCoreSettings.JDBC_DRIVER, driver);
		return p;
	}

	public static final ImmutableSet<String> CONNECTION_PARAMETER_NAMES = ImmutableSet.of(
			OntopSQLCoreSettings.JDBC_URL,
			OntopSQLCredentialSettings.JDBC_USER,
			OntopSQLCredentialSettings.JDBC_PASSWORD,
			OntopSQLCoreSettings.JDBC_DRIVER);

	public void clear() {
		properties.clear();
		driver = "";
		url = "";
		username = "";
		password = "";
		connection = null;
	}

	public ImmutableSet<String> getPropertyKeys() {
		return properties.keySet().stream()
				.map(Object::toString)
				.filter(k -> !CONNECTION_PARAMETER_NAMES.contains(k))
				.collect(ImmutableCollectors.toSet());
	}

	public Object getProperty(@Nonnull String key) {
		return properties.getProperty(key);
	}

	public void setProperty(@Nonnull String key, @Nonnull Object value) {
		if (CONNECTION_PARAMETER_NAMES.contains(key))
			throw new IllegalArgumentException("Cannot change reserved " + key);

		Object oldValue = properties.get(key); // can be null
		if (!value.equals(oldValue)) {
			properties.put(key, value);
			listeners.fire(l -> l.changed(this));
		}
	}

	public void removeProperty(@Nonnull String key) {
		if (CONNECTION_PARAMETER_NAMES.contains(key))
			throw new IllegalArgumentException("Cannot remove reserved " + key);

		properties.remove(key);
		listeners.fire(l -> l.changed(this));
	}

	/**
	 * Should not be called twice in a row without clear() in between.
	 *
	 * No need to remove listeners - this is handled by OBDAModelManager.
	 * @param listener
	 */
	public void addListener(DataSourceListener listener) {
		listeners.add(listener);
	}

	public void load(File propertyFile) throws IOException {
		if (propertyFile.exists())
			try (FileReader reader = new FileReader(propertyFile)) {
				properties.load(reader);
				updateDataSourceParametersFromUserSettings();
			}
	}

	/**
	 * possibly called after load() to override settings from the OBDA file
	*/
	public void update(Properties p) {
		properties.putAll(p);
		updateDataSourceParametersFromUserSettings();
	}

	private void updateDataSourceParametersFromUserSettings() {
		set(Optional.ofNullable(properties.getProperty(JDBC_URL))
						.orElseGet(this::getURL),
				Optional.ofNullable(properties.getProperty(JDBC_USER))
						.orElseGet(this::getUsername),
				Optional.ofNullable(properties.getProperty(JDBC_PASSWORD))
						.orElseGet(this::getPassword),
				Optional.ofNullable(properties.getProperty(JDBC_DRIVER))
						.orElseGet(this::getDriver));
	}


	public void store(File propertyFile) throws IOException {
		Properties properties = asProperties();
		boolean nonEmpty = !properties.isEmpty();

		DialogUtils.saveFileOrDeleteEmpty(!nonEmpty, propertyFile, file -> {
			try (FileOutputStream outputStream = new FileOutputStream(propertyFile)) {
				properties.store(outputStream, null);
			}
		}, LOGGER);
	}
}
