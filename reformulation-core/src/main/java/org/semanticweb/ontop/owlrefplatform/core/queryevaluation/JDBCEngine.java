package org.semanticweb.ontop.owlrefplatform.core.queryevaluation;

/*
 * #%L
 * ontop-reformulation-core
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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.semanticweb.ontop.model.OBDADataSource;
import org.semanticweb.ontop.model.impl.RDBMSourceParameterConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The JDBC engine is a implementation of the evaluation engine interface which
 * uses JDBC as connector to data sources. It can handle basically every data
 * source which provides a JDBC interface.
 * 
 * @author Manfred Gerstgrasser
 * 
 */

public class JDBCEngine implements EvaluationEngine {

	private OBDADataSource datasource = null;
	private Connection connection = null;
	private Statement statement = null;
	private boolean actionCanceled = false;

	Logger log = LoggerFactory.getLogger(EvaluationEngine.class);

	public JDBCEngine(OBDADataSource ds) throws SQLException {
		datasource = ds;
		connect();

	}

	public JDBCEngine(Connection con) {
		connection = con;
	}

	private void connect() throws SQLException {

		String driver = datasource.getParameter(RDBMSourceParameterConstants.DATABASE_DRIVER);
		String url = datasource.getParameter(RDBMSourceParameterConstants.DATABASE_URL);

		log.debug("Connecting to JDBC source: {}", url);

		String username = datasource.getParameter(RDBMSourceParameterConstants.DATABASE_USERNAME);
		String password = datasource.getParameter(RDBMSourceParameterConstants.DATABASE_PASSWORD);

		connection = DriverManager.getConnection(url, username, password);

	}

	/**
	 * Closes the current JDBC connection
	 * 
	 * @throws SQLException
	 */
	public void disconnect() throws SQLException {
		if (connection != null && !connection.isClosed()) {
			try {
				connection.close();
			} catch (Exception e) {
				log.error(e.getMessage());
			}
		}
	}

	public ResultSet execute(String sql) throws Exception {
//		log.debug("Executing SQL query: \n{}", sql);
		if (!actionCanceled) {
			if (connection == null) {
				throw new SQLException("No connection has been stablished yet");
			}
			statement = connection.createStatement();
			return statement.executeQuery(sql);
		} else {
			throw new Exception("Action canceled");
		}
	}

	@Override
	public void update(OBDADataSource ds) {
		if (!ds.getParameter(RDBMSourceParameterConstants.DATABASE_DRIVER).equals("org.h2.Driver")) {
			if (connection != null) {
				try {
					disconnect();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			datasource = ds;

			try {
				connect();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void closeStatement() throws Exception {
		if (statement != null && !statement.isClosed()) {
			try {
				statement.cancel();
			} catch (Exception e) {

			}
			try {
				statement.close();
			} catch (Exception e) {

			}
		}
	}

	@Override
	public void isCanceled(boolean bool) {
		actionCanceled = bool;
	}

	@Override
	public void dispose() {
		try {
			disconnect();
		} catch (SQLException e) {
		}
	}

	@Override
	public Statement getStatement() throws SQLException {
		return connection.createStatement();
	}
}
