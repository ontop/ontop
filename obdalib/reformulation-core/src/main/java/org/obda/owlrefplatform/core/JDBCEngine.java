package org.obda.owlrefplatform.core;

import inf.unibz.it.obda.domain.DataSource;
import inf.unibz.it.obda.rdbmsgav.domain.RDBMSsourceParameterConstants;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

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

	private DataSource	datasource	= null;
	private Connection	connection	= null;

	Logger				log			= LoggerFactory.getLogger(EvaluationEngine.class);

	public JDBCEngine(DataSource ds) {
		datasource = ds;
		try {
			connect();
		} catch (ClassNotFoundException e) {
			log.error(e.getMessage(), e);
		} catch (SQLException e) {
			log.error(e.getMessage(), e);
		}
	}

	public JDBCEngine(Connection con) {
		connection = con;
	}

	private void connect() throws ClassNotFoundException, SQLException {

		String driver = datasource.getParameter(RDBMSsourceParameterConstants.DATABASE_DRIVER);
		String url = datasource.getParameter(RDBMSsourceParameterConstants.DATABASE_URL);
		
		log.debug("Connecting to JDBC source: {}", url);
		
		String dbname = datasource.getParameter(RDBMSsourceParameterConstants.DATABASE_NAME);
		String username = datasource.getParameter(RDBMSsourceParameterConstants.DATABASE_USERNAME);
		String password = datasource.getParameter(RDBMSsourceParameterConstants.DATABASE_PASSWORD);
		Class d = Class.forName(driver);
		connection = DriverManager.getConnection(url + dbname, username, password);

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
		return connection.createStatement().executeQuery(sql);
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
}
