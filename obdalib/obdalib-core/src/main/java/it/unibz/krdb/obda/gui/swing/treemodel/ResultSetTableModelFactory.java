/*******************************************************************************
 * Copyright (c) 2008, Mariano Rodriguez-Muro. All rights reserved.
 * 
 * The OBDA-API is licensed under the terms of the Lesser General Public License
 * v.3 (see OBDAAPI_LICENSE.txt for details). The components of this work
 * include:
 * 
 * a) The OBDA-API developed by the author and licensed under the LGPL; and, b)
 * third-party components licensed under terms that may be different from those
 * of the LGPL. Information about such licenses can be found in the file named
 * OBDAAPI_3DPARTY-LICENSES.txt.
 */
package it.unibz.krdb.obda.gui.swing.treemodel;

import it.unibz.krdb.obda.exception.NoConnectionException;
import it.unibz.krdb.obda.gui.swing.exception.NoDatasourceSelectedException;
import it.unibz.krdb.obda.model.OBDADataSource;
import it.unibz.krdb.obda.model.impl.RDBMSourceParameterConstants;

import java.net.URI;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;

/**
 * This class encapsulates a JDBC database connection and, given a SQL query as
 * a string, returns a ResultSetTableModel object suitable for display in a
 * JTable Swing component
 */
public class ResultSetTableModelFactory {
	private Connection											connection;																// Holds
	// the
	// connection
	// to
	// the
	// database
	public String												driverClassName		= null;
	public String												dbname				= null;
	public String												username			= null;
	public String												password			= null;

	HashMap<String, String>										rowcounts			= new HashMap<String, String>();

	private static HashMap<URI, ResultSetTableModelFactory>	resultsetfactories	= new HashMap<URI, ResultSetTableModelFactory>();
	
	/** The statement that is currently being executing to create a model **/
	private Statement currentStatement = null;

	/** The constructor method uses the arguments to create db Connection */
	private ResultSetTableModelFactory(String driverClassName, String dbname, String username, String password) {
		// Look up the JDBC driver by class name. When the class loads, it
		// automatically registers itself with the DriverManager used in
		// the next step.

		this.driverClassName = driverClassName;
		this.dbname = dbname;
		this.username = username;
		this.password = password;

	}

	public static ResultSetTableModelFactory getInstance(OBDADataSource current_datasource) throws NoDatasourceSelectedException, SQLException, ClassNotFoundException,
			NoConnectionException {

		 
		if (current_datasource == null) {
			throw new NoDatasourceSelectedException("No source selected");
		}
		String driver = current_datasource.getParameter(RDBMSourceParameterConstants.DATABASE_DRIVER);
		String url = current_datasource.getParameter(RDBMSourceParameterConstants.DATABASE_URL);
		String username = current_datasource.getParameter(RDBMSourceParameterConstants.DATABASE_USERNAME);
		String password = current_datasource.getParameter(RDBMSourceParameterConstants.DATABASE_PASSWORD);

		ResultSetTableModelFactory rstmfactory = resultsetfactories.get(current_datasource.getSourceID());
		if (rstmfactory == null) {
			/*******************************************************************
			 * No facotry has been created yet. Creating on
			 * 
			 */
			rstmfactory = new ResultSetTableModelFactory(driver, url, username, password);
			resultsetfactories.put(current_datasource.getSourceID(), rstmfactory);
		} else {
			// If the datasource information changed since last connectino was
			// stablished.
			boolean driverissame = driver.equals(rstmfactory.driverClassName);
			boolean userissame = username.equals(rstmfactory.username);
			boolean passwordissame = password.equals(rstmfactory.password);
			boolean dbissame = rstmfactory.dbname.equals(url);

			if (!((driverissame) && (userissame) && (passwordissame) && (dbissame))) {
				rstmfactory.close();
				rstmfactory = new ResultSetTableModelFactory(driver, url, username, password);
				resultsetfactories.put(current_datasource.getSourceID(), rstmfactory);
			}
		}
		/***********************************************************************
		 * The resultset factory is ready, checking if it is connected.
		 */

		boolean connected = false;

		if (!rstmfactory.isConnected()) {
			connected = rstmfactory.connect();
		} else {
			connected = true;
		}

		if (!connected) {
			throw new NoConnectionException();
		}
		return rstmfactory;
	}

	/***************************************************************************
	 * Connects to the database, it should be called only if the previous
	 * connection was closed first.
	 * 
	 * @return true if a new connetion was successfull. False if there is
	 *         already a connection;
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	public boolean connect() throws ClassNotFoundException, SQLException {

		if (connection != null && !connection.isClosed())
			return false;

		try {
			Class d = Class.forName(driverClassName);
		} catch (Exception e) {
			e.printStackTrace();
		}		
		connection = DriverManager.getConnection(dbname, username, password);
		connection.setAutoCommit(true);
		return true;
	}

	public boolean isConnected() throws SQLException {
		if (connection == null)
			return false;
		return !connection.isClosed();
	}

	/**
	 * This method takes a SQL query, passes it to the database, obtains the
	 * results as a ResultSet, and returns a ResultSetTableModel object that
	 * holds the results in a form that the Swing JTable component can use.
	 */
	public IncrementalResultSetTableModel getResultSetTableModel(String query, String s) throws SQLException {
		// If we've called close(), then we can't call this method
		if (connection == null)
			throw new IllegalStateException("Connection already closed.");

		// Create a Statement object that will be used to excecute the query.
		// The arguments specify that the returned ResultSet will be
		// scrollable, read-only, and insensitive to changes in the db.
//		Statement currentStatement = null;
		ResultSet r = null;
		connection.setAutoCommit(false);
		currentStatement = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
		currentStatement.setFetchSize(100);
		// Run the query, creating a ResultSet
		r = currentStatement.executeQuery(query);
		
		r.setFetchSize(100);
		
		// Create and return a TableModel for the ResultSet
		return new IncrementalResultSetTableModel(r);
	}
	
	public void cancelCurrentStatement() throws SQLException {
		currentStatement.cancel();
	}

	/**
	 * This method takes a SQL query, passes it to the database, obtains the
	 * results as a ResultSet, and returns a ResultSetTableModel object that
	 * holds the results in a form that the Swing JTable component can use.
	 */
	public ColumnInspectorTableModel getTableDescriptionTableModel(String tablename) throws SQLException {
		// If we've called close(), then we can't call this method
		if (connection == null)
			throw new IllegalStateException("Connection already closed.");

		// Create a Statement object that will be used to excecute the query.
		// The arguments specify that the returned ResultSet will be
		// scrollable, read-only, and insensitive to changes in the db.
		Statement statement = null;
		ResultSet r = null;

		statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
		String query = "";
		if(driverClassName.equals("oracle.jdbc.driver.OracleDriver")){
			query = "select * from " + tablename + " where rownum =0";
		}else if(driverClassName.equals("com.ibm.db2.jcc.DB2Driver")){
			query = "select * from " + tablename + " fetch first 1 rows only ";
		}else{
			query = "select * from " + tablename + " LIMIT 1";
		}
		r = statement.executeQuery(query);
		ResultSetMetaData rmeta = r.getMetaData();
		// rmeta.getC
		// Run the query, creating a ResultSet
		// DatabaseMetaData meta = connection.getMetaData();
		// ResultSet r = meta.getColumns(null, null, tablename, null);

		// r = statement.executeQuery(query);

		// Create and return a TableModel for the ResultSet
		return new ColumnInspectorTableModel(rmeta);
	}

	/**
	 * This method takes a SQL query, passes it to the database, obtains the
	 * results as a ResultSet, and returns a ResultSetTableModel object that
	 * holds the results in a form that the Swing JTable component can use.
	 */

	public RelationsResultSetTableModel getRelationsResultSetTableModel() throws SQLException {
		
		

		// If we've called close(), then we can't call this method
		if (connection == null)
			throw new IllegalStateException("Connection already closed.");

		// Create a Statement object that will be used to excecute the query.
		// The arguments specify that the returned ResultSet will be
		// scrollable, read-only, and insensitive to changes in the db.
		// Statement statement =
		// connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
		// ResultSet.CONCUR_READ_ONLY);
		// Run the query, creating a ResultSet

		if (driverClassName.equals("com.ibm.db2.jcc.DB2Driver")) {

			Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);

			// jdbc:db2://5.90.168.104:50000/MINIST:currentSchema=PROP;
			String[] sp1 = dbname.split("/");
			String catalog = sp1[sp1.length - 1].split(":")[0];
			String t2 = dbname.split("=")[1];
			String schema = t2.substring(0, t2.length() - 1);
			ResultSet r = statement.executeQuery("SELECT TABLE_NAME FROM SYSIBM.TABLES WHERE TABLE_CATALOG = '" + catalog
					+ "' AND TABLE_SCHEMA = '" + schema + "'");
			return new RelationsResultSetTableModel(r,  null);
		} if (driverClassName.equals("oracle.jdbc.driver.OracleDriver")) {
			// select table_name from user_tables
			Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			ResultSet r = statement.executeQuery("select table_name from user_tables");
			return new RelationsResultSetTableModel(r,  null);
		} else {
			//postgres and mysql
			DatabaseMetaData metdata = connection.getMetaData();
			String catalog = null;
			String schemaPattern = "%";
			String tableNamePattern = "%";
			String types[] = { "TABLE" };
			ResultSet r = metdata.getTables(catalog, schemaPattern, tableNamePattern, types);
			return new RelationsResultSetTableModel(r,  null);
		}
	}

	public String getRelationsRowCount(String relation) throws SQLException {

		if (connection == null)
			throw new IllegalStateException("Connection already closed.");

		String count = rowcounts.get(relation);

		if (count != null) {
			return count;
		}

		Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
		// Run the query, creating a ResultSet
		ResultSet r = statement.executeQuery("SELECT COUNT(*) FROM " + relation);
		// Create and return a TableModel for the ResultSet
		r.next();
		// r.absolute(1);
		if (r.first()) {
			Object temp = r.getObject(1);
			// count = ((Long) temp).toString();
			count = temp.toString();
			rowcounts.put(relation, count);
			return count;
		}
		return "Count error";
	}

	/**
	 * Call this method when done with the factory to close the DB connection
	 */
	public void close() {
		try {
			connection.close();
		} // Try to close the connection
		catch (Exception e) {
		} // Do nothing on error. At least we tried.
		connection = null;
	}

	/** Automatically close the connection when we're garbage collected */
	protected void finalize() {
		close();
	}
}
