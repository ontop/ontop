/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package it.unibz.krdb.sql;

import it.unibz.krdb.obda.model.OBDADataSource;
import it.unibz.krdb.obda.model.OBDAException;
import it.unibz.krdb.obda.model.impl.RDBMSourceParameterConstants;
import it.unibz.krdb.sql.api.Attribute;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JDBCConnectionManager {

	public static final String JDBC_AUTOCOMMIT = "autocommit";
	public static final String JDBC_FETCHSIZE = "fetchsize";
	public static final String JDBC_RESULTSETTYPE = "resultsettype";
	public static final String JDBC_RESULTSETCONCUR = "resultsetconcur";

	private static JDBCConnectionManager instance = null;

	private HashMap<String, Object> properties = null;
	private HashMap<OBDADataSource, Connection> connectionPool = null;

	private static Logger log = LoggerFactory.getLogger(JDBCConnectionManager.class);

	/**
	 * Private constructor.
	 */
	private JDBCConnectionManager() {
		connectionPool = new HashMap<OBDADataSource, Connection>();

		properties = new HashMap<String, Object>();
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
	 * Constructs a new database connection object from a data source and
	 * retrieves the object.
	 * 
	 * @param dataSource
	 *            The data source object.
	 * @return The connection object.
	 * @throws SQLException
	 */
	public Connection createConnection(OBDADataSource dataSource) throws SQLException {

		if (connectionPool.get(dataSource) != null && !connectionPool.get(dataSource).isClosed())
			return connectionPool.get(dataSource);

		String url = dataSource.getParameter(RDBMSourceParameterConstants.DATABASE_URL);
		String username = dataSource.getParameter(RDBMSourceParameterConstants.DATABASE_USERNAME);
		String password = dataSource.getParameter(RDBMSourceParameterConstants.DATABASE_PASSWORD);
		String driver = dataSource.getParameter(RDBMSourceParameterConstants.DATABASE_DRIVER);

		if (driver == null || driver.trim().equals(""))
			throw new SQLException("Invalid driver");
		try {
			Class.forName(driver);
		} catch (ClassNotFoundException e1) {
			// NO-OP
		}

		Connection conn = DriverManager.getConnection(url, username, password);
		connectionPool.put(dataSource, conn);
		return conn;
	}
	
	/**
	 * Retrieves the connection object from the connection pool. If the
	 * connection doesnt exist or is dead, it will attempt to create a new
	 * connection.
	 * 
	 * @param sourceId
	 *            The connection ID (usually the same as the data source URI).
	 */
	public Connection getConnection(OBDADataSource source) throws SQLException {
		boolean alive = isConnectionAlive(source);
		if (!alive) {
			createConnection(source);
		}
		Connection conn = connectionPool.get(source);
		return conn;
	}

	/**
	 * Removes a connection object form the pool. The system will put the
	 * connection back to the pool if an exception occurs.
	 * 
	 * @param sourceId
	 *            The connection ID that wants to be removed.
	 * @return Returns true if the removal is successful, or false otherwise.
	 */
	public boolean closeConnection(OBDADataSource source) throws OBDAException, SQLException {
		boolean bStatus = true; // the status flag.
		Connection existing = connectionPool.get(source);
		if (existing == null)
			throw new OBDAException("There is connection for such source");
		if (existing.isClosed()) {
			connectionPool.remove(source);
			throw new OBDAException("Connection is already close");
		}
		try {
			connectionPool.remove(source);
			existing.close();
		} catch (SQLException e) {
			log.error(e.getMessage());
		}
		return bStatus;
	}

	/**
	 * Checks whether the connection is still alive.
	 * 
	 * @param sourceId
	 *            The connection ID (usually the same as the data source URI).
	 * @return Returns true if the connection exists and is still open.
	 * 
	 * @throws SQLException
	 */
	public boolean isConnectionAlive(OBDADataSource sourceId) throws SQLException {
		Connection conn = connectionPool.get(sourceId);
		if (conn == null || conn.isClosed()) {
			return false;
		}
		return !conn.isClosed();
	}

	/**
	 * Retrieves the database meta data about the table schema given a
	 * particular data source id.
	 * 
	 * @param sourceId
	 *            The database id.
	 * @return The database meta data object.
	 */
	public DBMetadata getMetaData(OBDADataSource sourceId) throws SQLException {
		Connection conn = getConnection(sourceId);
		return getMetaData(conn);
	}

	/**
	 * Retrieves the database meta data about the table schema given a
	 * particular data source id.
	 * 
	 * @param sourceId
	 *            The database id.
	 * @return The database meta data object.
	 */
	public static DBMetadata getMetaData(Connection conn) throws SQLException {
		DBMetadata metadata = null;
		final DatabaseMetaData md = conn.getMetaData();
		if (md.getDatabaseProductName().contains("Oracle")) {
			// If the database engine is Oracle
			metadata = getOracleMetaData(md, conn);
		} else if (md.getDatabaseProductName().contains("DB2")) {
			// If the database engine is IBM DB2
			metadata = getDB2MetaData(md, conn);
		} else if (md.getDatabaseProductName().contains("SQL Server")) {
			// If the database engine is SQL Server
			metadata = getSqlServerMetaData(md, conn);
		} else {
			// For other database engines
			metadata = getOtherMetaData(md);
		}
		return metadata;
	}

	/**
	 * Retrieve metadata for most of the database engine, e.g., MySQL and PostgreSQL
	 */
	private static DBMetadata getOtherMetaData(DatabaseMetaData md) throws SQLException {
		DBMetadata metadata = new DBMetadata(md);

		ResultSet rsTables = null;
		try {
			rsTables = md.getTables(null, null, null, new String[] { "TABLE", "VIEW" });
			while (rsTables.next()) {
				Set<String> tableColumns = new HashSet<String>();

				final String tblCatalog = rsTables.getString("TABLE_CAT");
				final String tblName = rsTables.getString("TABLE_NAME");
				final String tblSchema = rsTables.getString("TABLE_SCHEM");
				final ArrayList<String> primaryKeys = getPrimaryKey(md, tblCatalog, tblSchema, tblName);
				final Map<String, Reference> foreignKeys = getForeignKey(md, null, null, tblName);

				TableDefinition td = new TableDefinition(tblName);

				ResultSet rsColumns = null;
				try {
					rsColumns = md.getColumns(tblCatalog, tblSchema, tblName, null);
					if (rsColumns == null) {
						continue;
					}
					for (int pos = 1; rsColumns.next(); pos++) {
						final String columnName = rsColumns.getString("COLUMN_NAME");
						final int dataType = rsColumns.getInt("DATA_TYPE");
						final boolean isPrimaryKey = primaryKeys.contains(columnName);
						final Reference reference = foreignKeys.get(columnName);
						final int isNullable = rsColumns.getInt("NULLABLE");
						td.setAttribute(pos, new Attribute(columnName, dataType, isPrimaryKey, reference, isNullable));

						// Check if the columns are unique regardless their letter cases
						if (!tableColumns.add(columnName.toLowerCase())) {
							// if exist
							throw new RuntimeException("The system cannot process duplicate table columns!");
						}
					}
					// Add this information to the DBMetadata
					metadata.add(td);
				} finally {
					if (rsColumns != null) {
						rsColumns.close(); // close existing open cursor
					}
				}
			}
		} finally {
			if (rsTables != null) {
				rsTables.close();
			}
		}
		return metadata;
	}

	/**
	 * Retrieve metadata for SQL Server database engine
	 */
	private static DBMetadata getSqlServerMetaData(DatabaseMetaData md, Connection conn) throws SQLException {
		DBMetadata metadata = new DBMetadata(md);
		Statement stmt = null;
		ResultSet resultSet = null;
		try {
			/* Obtain the statement object for query execution */
			stmt = conn.createStatement();
			
			/* Obtain the relational objects (i.e., tables and views) */
			final String tableSelectQuery = "SELECT TABLE_CATALOG, TABLE_SCHEMA, TABLE_NAME " +
					"FROM INFORMATION_SCHEMA.TABLES " +
					"WHERE TABLE_TYPE='BASE TABLE' OR TABLE_TYPE='VIEW'";
			resultSet = stmt.executeQuery(tableSelectQuery);
			
			/* Obtain the column information for each relational object */
			while (resultSet.next()) {
				ResultSet rsColumns = null;
				try {
					final String tblCatalog = resultSet.getString("TABLE_CATALOG");
					final String tblSchema = resultSet.getString("TABLE_SCHEMA");
					final String tblName = resultSet.getString("TABLE_NAME");
					final ArrayList<String> primaryKeys = getPrimaryKey(md, tblCatalog, tblSchema, tblName);
					final Map<String, Reference> foreignKeys = getForeignKey(md, tblCatalog, tblSchema, tblName);
					
					TableDefinition td = new TableDefinition(tblName);
					rsColumns = md.getColumns(tblCatalog, tblSchema, tblName, null);
					
					for (int pos = 1; rsColumns.next(); pos++) {
						final String columnName = rsColumns.getString("COLUMN_NAME");
						final int dataType = rsColumns.getInt("DATA_TYPE");
						final boolean isPrimaryKey = primaryKeys.contains(columnName);
						final Reference reference = foreignKeys.get(columnName);
						final int isNullable = rsColumns.getInt("NULLABLE");
						td.setAttribute(pos, new Attribute(columnName, dataType, isPrimaryKey, reference, isNullable));
					}
					// Add this information to the DBMetadata
					metadata.add(td);
				} finally {
					if (rsColumns != null) {
						rsColumns.close(); // close existing open cursor
					}
				}
			}
		} finally {
			if (resultSet != null) {
				resultSet.close();
			}
			if (stmt != null) {
				stmt.close();
			}
		}
		return metadata;
	}
	
	/**
	 * Retrieve metadata for DB2 database engine
	 */
	private static DBMetadata getDB2MetaData(DatabaseMetaData md, Connection conn) throws SQLException {
		DBMetadata metadata = new DBMetadata(md);
		Statement stmt = null;
		ResultSet resultSet = null;
		try {
			/* Obtain the statement object for query execution */
			stmt = conn.createStatement();
			
			/* Obtain the relational objects (i.e., tables and views) */
			final String tableSelectQuery = "SELECT TABSCHEMA, TABNAME " +
					"FROM SYSCAT.TABLES " +
					"WHERE OWNERTYPE='U' " +
					"	AND (TYPE='T' OR TYPE='V') " +
					"	AND TBSPACEID IN (SELECT TBSPACEID FROM SYSCAT.TABLESPACES WHERE TBSPACE LIKE 'USERSPACE%')";
			resultSet = stmt.executeQuery(tableSelectQuery);
			
			/* Obtain the column information for each relational object */
			while (resultSet.next()) {
				ResultSet rsColumns = null;
				try {
					final String tblSchema = resultSet.getString("TABSCHEMA");
					final String tblName = resultSet.getString("TABNAME");
					final ArrayList<String> primaryKeys = getPrimaryKey(md, null, tblSchema, tblName);
					final Map<String, Reference> foreignKeys = getForeignKey(md, null, tblSchema, tblName);
					
					TableDefinition td = new TableDefinition(tblName);
					rsColumns = md.getColumns(null, tblSchema, tblName, null);
					
					for (int pos = 1; rsColumns.next(); pos++) {
						final String columnName = rsColumns.getString("COLUMN_NAME");
						final int dataType = rsColumns.getInt("DATA_TYPE");
						final boolean isPrimaryKey = primaryKeys.contains(columnName);
						final Reference reference = foreignKeys.get(columnName);
						final int isNullable = rsColumns.getInt("NULLABLE");
						td.setAttribute(pos, new Attribute(columnName, dataType, isPrimaryKey, reference, isNullable));
					}
					// Add this information to the DBMetadata
					metadata.add(td);
				} finally {
					if (rsColumns != null) {
						rsColumns.close(); // close existing open cursor
					}
				}
			}
		} finally {
			if (resultSet != null) {
				resultSet.close();
			}
			if (stmt != null) {
				stmt.close();
			}
		}
		return metadata;
	}
	
	/**
	 * Retrieve metadata for Oracle database engine
	 */
	private static DBMetadata getOracleMetaData(DatabaseMetaData md, Connection conn) throws SQLException {
		DBMetadata metadata = new DBMetadata(md);
		Statement stmt = null;
		ResultSet resultSet = null;
		try {
			/* Obtain the statement object for query execution */
			stmt = conn.createStatement();
			
			/* Obtain the table owner (i.e., schema name) */
			String tableOwner = "SYSTEM"; // by default
			resultSet = stmt.executeQuery("SELECT user FROM dual");
			if (resultSet.next()) {
				tableOwner = resultSet.getString("user");
			}
			
			/* Obtain the relational objects (i.e., tables and views) */
			final String tableSelectQuery = "SELECT object_name FROM ( " +
					"SELECT table_name as object_name FROM user_tables WHERE " +
					"NOT table_name LIKE 'MVIEW$_%' AND " +
					"NOT table_name LIKE 'LOGMNR_%' AND " +
					"NOT table_name LIKE 'AQ$_%' AND " +
					"NOT table_name LIKE 'DEF$_%' AND " +
					"NOT table_name LIKE 'REPCAT$_%' AND " +
					"NOT table_name LIKE 'LOGSTDBY$%' AND " +
					"NOT table_name LIKE 'OL$%' " +
					"UNION " +
					"SELECT view_name as object_name FROM user_views WHERE " +
					"NOT view_name LIKE 'MVIEW_%' AND " +
					"NOT view_name LIKE 'LOGMNR_%' AND " +
					"NOT view_name LIKE 'AQ$_%')";
			resultSet = stmt.executeQuery(tableSelectQuery);
			
			/* Obtain the column information for each relational object */
			while (resultSet.next()) {
				ResultSet rsColumns = null;
				try {
					final String tblName = resultSet.getString("object_name");
					final ArrayList<String> primaryKeys = getPrimaryKey(md, null, tableOwner, tblName);
					final Map<String, Reference> foreignKeys = getForeignKey(md, null, tableOwner, tblName);
					
					TableDefinition td = new TableDefinition(tblName);
					rsColumns = md.getColumns(null, tableOwner, tblName, null);
					
					for (int pos = 1; rsColumns.next(); pos++) {
						final String columnName = rsColumns.getString("COLUMN_NAME");
						final int dataType = rsColumns.getInt("DATA_TYPE");
						final boolean isPrimaryKey = primaryKeys.contains(columnName);
						final Reference reference = foreignKeys.get(columnName);
						final int isNullable = rsColumns.getInt("NULLABLE");
						td.setAttribute(pos, new Attribute(columnName, dataType, isPrimaryKey, reference, isNullable));
					}
					// Add this information to the DBMetadata
					metadata.add(td);
				} finally {
					if (rsColumns != null) {
						rsColumns.close(); // close existing open cursor
					}
				}
			}
		} finally {
			if (resultSet != null) {
				resultSet.close();
			}
			if (stmt != null) {
				stmt.close();
			}
		}
		return metadata;
	}

	/* Retrives the primary key(s) from a table */
	private static ArrayList<String> getPrimaryKey(DatabaseMetaData md, String tblCatalog, String schema, String table) throws SQLException {
		ArrayList<String> pk = new ArrayList<String>();
		ResultSet rsPrimaryKeys = null;
		try {
			rsPrimaryKeys = md.getPrimaryKeys(tblCatalog, schema, table);
			while (rsPrimaryKeys.next()) {
				String colName = rsPrimaryKeys.getString("COLUMN_NAME");
				String pkName = rsPrimaryKeys.getString("PK_NAME");
				if (pkName != null) {
					pk.add(colName);
				}
			}
		} finally {
			if (rsPrimaryKeys != null) {
				rsPrimaryKeys.close();
			}
		}
		return pk;
	}
	
	/* Retrives the foreign key(s) from a table */
	private static Map<String, Reference> getForeignKey(DatabaseMetaData md, String tblCatalog, String schema, String table) throws SQLException {
		Map<String, Reference> fk = new HashMap<String, Reference>();
		ResultSet rsForeignKeys = null;
		try {
			rsForeignKeys = md.getImportedKeys(tblCatalog, schema, table);
			while (rsForeignKeys.next()) {
				String fkName = rsForeignKeys.getString("FK_NAME");
				String colName = rsForeignKeys.getString("FKCOLUMN_NAME");
				String pkTableName = rsForeignKeys.getString("PKTABLE_NAME");
				String pkColumnName = rsForeignKeys.getString("PKCOLUMN_NAME");
				fk.put(colName, new Reference(fkName, pkTableName, pkColumnName));
			}
		} finally {
			if (rsForeignKeys != null) {
				rsForeignKeys.close();
			}
		}
		return fk;
	}

	/**
	 * Removes all the connections in the connection pool.
	 * 
	 * @throws SQLException
	 */
	public void dispose() throws SQLException {
		Set<OBDADataSource> keys = connectionPool.keySet();
		for (OBDADataSource sourceId : keys) {
			try {
				closeConnection(sourceId);
			} catch (Exception e) {
				log.error(e.getMessage());
			}
		}
	}
}
