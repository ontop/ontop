package org.obda.owlrefplatform.core.queryevaluation;

import inf.unibz.it.obda.model.DataSource;
import inf.unibz.it.obda.model.impl.RDBMSsourceParameterConstants;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

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

	private DataSource	datasource		= null;
	private Connection	connection		= null;
	private Statement	statement		= null;
	private boolean		actionCanceled	= false;

	Logger				log				= LoggerFactory.getLogger(EvaluationEngine.class);

	public JDBCEngine(DataSource ds) throws SQLException {
		datasource = ds;
		connect();

	}

	public JDBCEngine(Connection con) {
		connection = con;
	}

	private void connect() throws SQLException {

		String driver = datasource.getParameter(RDBMSsourceParameterConstants.DATABASE_DRIVER);
		String url = datasource.getParameter(RDBMSsourceParameterConstants.DATABASE_URL);

		log.debug("Connecting to JDBC source: {}", url);

		String username = datasource.getParameter(RDBMSsourceParameterConstants.DATABASE_USERNAME);
		String password = datasource.getParameter(RDBMSsourceParameterConstants.DATABASE_PASSWORD);
		try {
			Class d = Class.forName(driver);
		} catch (Exception e) {
			log.warn("WARNING: JDBC Driver not found exception or may have been already loaded by the system.");
			// log.debug(e.getMessage(), e);
		}
		connection = DriverManager.getConnection(url, username, password);

	}

	/**
	 * Closes the current JDBC connection
	 * 
	 * @throws SQLException
	 */
	public void disconnect() throws SQLException {
		if (connection != null) {
			connection.close();
		}
	}

	public ResultSet execute(String sql) throws Exception {
		log.debug("Executing SQL query: \n{}", sql);
		if (!actionCanceled) {
			if (connection == null)
				throw new SQLException("No connection has been stablished yet");
			statement = connection.createStatement();
			return statement.executeQuery(sql);
		} else {
			throw new Exception("Action canceled");
		}
	}

	@Override
	public void update(DataSource ds) {
		if (!ds.getParameter(RDBMSsourceParameterConstants.DATABASE_DRIVER).equals("org.h2.Driver")) {
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
			statement.cancel();
			statement.close();
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
}
