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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;

public class JDBCConnectionManager {

	private static final Logger LOGGER = LoggerFactory.getLogger(JDBCConnectionManager.class);

	private Connection connection = null;

	JDBCConnectionManager() {
		/*  TODO: default parameters for creating statements (to be enforced?)
			JDBC_AUTOCOMMIT, false
			JDBC_FETCHSIZE, 100
			JDBC_RESULTSETCONCUR, ResultSet.CONCUR_READ_ONLY
			JDBC_RESULTSETTYPE, ResultSet.TYPE_FORWARD_ONLY
		 */
	}

	/**
	 * Retrieves the connection object.
	 * If the connection doesn't exist or is dead, it will create a new connection.
	 */
	public Connection getConnection(String url, String username, String password) throws SQLException {
		if (connection == null || connection.isClosed())
			connection = DriverManager.getConnection(url, username, password);

		return connection;
	}

	/**
	 * Closes the connection quietly
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
