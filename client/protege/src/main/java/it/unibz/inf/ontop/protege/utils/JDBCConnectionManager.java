package it.unibz.inf.ontop.protege.utils;

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

import it.unibz.inf.ontop.injection.OntopSQLCredentialSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class JDBCConnectionManager {

	public static final String JDBC_AUTOCOMMIT = "autocommit";
	public static final String JDBC_FETCHSIZE = "fetchsize";
	public static final String JDBC_RESULTSETTYPE = "resultsettype";
	public static final String JDBC_RESULTSETCONCUR = "resultsetconcur";
	
	private static JDBCConnectionManager instance = null;

	private Map<String, Object> properties = new HashMap<>();
	private Connection connection = null;

	private static final Logger log = LoggerFactory.getLogger(JDBCConnectionManager.class);

	/**
	 * Private constructor.
	 */
	private JDBCConnectionManager() {
		properties.put(JDBC_AUTOCOMMIT, false);
		properties.put(JDBC_FETCHSIZE, 100);
		properties.put(JDBC_RESULTSETCONCUR, ResultSet.CONCUR_READ_ONLY);
		properties.put(JDBC_RESULTSETTYPE, ResultSet.TYPE_FORWARD_ONLY);
	}

	/**
	 * Returns a single connection manager.
	 */
	public static JDBCConnectionManager getJDBCConnectionManager() {
		if (instance == null) {
			instance = new JDBCConnectionManager();
		}
		return instance;
	}

	/**
	 * Constructs a new database connection object and retrieves it.
	 *
	 * @return The connection object.
	 * @throws SQLException
	 */
	public Connection createConnection(String url, String username, String password) throws SQLException {

		if (connection != null && !connection.isClosed())
			return connection;

		connection = DriverManager.getConnection(url, username, password);
		return connection;
	}
	
	/**
	 * Retrieves the connection object. If the
	 * connection doesnt exist or is dead, it will attempt to create a new
	 * connection.
	 *
	 */
	public Connection getConnection(String url, String username, String password) throws SQLException {
		boolean alive = isConnectionAlive();
		if (!alive) {
			createConnection(url, username, password);
		}
		return connection;
	}

	/**
	 * Removes a connection object form the pool. The system will put the
	 * connection back to the pool if an exception occurs.
	 *
	 * @return Returns true if the removal is successful, or false otherwise.
	 */
	public void closeConnection() throws SQLException {
//		boolean bStatus = true; // the status flag.
//		if (connection == null)
//			throw new OBDAException("There is connection for such source");
//		if (connection.isClosed()) {
//			throw new OBDAException("Connection is already close");
//		}
//		try {
//			connection.close();
//		}
//		catch (SQLException e) {
//			log.error(e.getMessage());
//		}
//		return bStatus;
		if (connection != null)
			connection.close();
	}

	/**
	 * Checks whether the connection is still alive.
	 *
	 * @return Returns true if the connection exists and is still open.
	 * 
	 * @throws SQLException
	 */
	public boolean isConnectionAlive() throws SQLException {
		if (connection == null || connection.isClosed()) {
			return false;
		}
		return !connection.isClosed();
	}

	
	/**
	 * Removes all the connections in the connection pool.
	 * 
	 * @throws SQLException
	 */
	public void dispose() throws SQLException {
		try {
			closeConnection();
		}
		catch (Exception e) {
			log.error(e.getMessage());
		}
	}

	public Connection getConnection(OntopSQLCredentialSettings settings) throws SQLException {
		return getConnection(settings.getJdbcUrl(), settings.getJdbcUser(),
				settings.getJdbcPassword());
	}
}
