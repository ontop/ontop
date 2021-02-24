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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class JDBCConnectionManager {

	public static final String JDBC_AUTOCOMMIT = "autocommit";
	public static final String JDBC_FETCHSIZE = "fetchsize";
	public static final String JDBC_RESULTSETTYPE = "resultsettype";
	public static final String JDBC_RESULTSETCONCUR = "resultsetconcur";
	
	private Map<String, Object> properties = new HashMap<>(); // TODO: never used - remove?
	private Connection connection = null;

	private static final Logger LOGGER = LoggerFactory.getLogger(JDBCConnectionManager.class);

	JDBCConnectionManager() {
		properties.put(JDBC_AUTOCOMMIT, false);
		properties.put(JDBC_FETCHSIZE, 100);
		properties.put(JDBC_RESULTSETCONCUR, ResultSet.CONCUR_READ_ONLY);
		properties.put(JDBC_RESULTSETTYPE, ResultSet.TYPE_FORWARD_ONLY);
	}

	/**
	 * Retrieves the connection object. If the connection doesn't exist or is dead,
	 * it will attempt to create a new connection.
	 */
	public Connection getConnection(String url, String username, String password) throws SQLException {
		if (connection == null || connection.isClosed())
			connection = DriverManager.getConnection(url, username, password);

		return connection;
	}


	/**
	 * Removes all the connections in the connection pool.
	 */
	public void dispose() {
		try {
			if (connection != null)
				connection.close();
		}
		catch (Exception e) {
			LOGGER.error(e.getMessage());
		}
	}
}
