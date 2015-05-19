package it.unibz.krdb.sql;

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

import it.unibz.krdb.obda.model.OBDADataSource;
import it.unibz.krdb.obda.model.OBDAException;
import it.unibz.krdb.obda.model.impl.RDBMSourceParameterConstants;
import it.unibz.krdb.sql.api.Attribute;
import it.unibz.krdb.sql.api.RelationJSQL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.*;

public class JDBCConnectionManager {

	public static final String JDBC_AUTOCOMMIT = "autocommit";
	public static final String JDBC_FETCHSIZE = "fetchsize";
	public static final String JDBC_RESULTSETTYPE = "resultsettype";
	public static final String JDBC_RESULTSETCONCUR = "resultsetconcur";

	// These are used by getOtherMetadata to signal whether the
	// unquoted table names should be put in lower (Postgres), upper (db2) or no change(mysql)
	private static final int JDBC_ORIGINALCASE = 0;
	private static final int JDBC_LOWERCASE = 1;
	private static final int JDBC_UPPERCASE = 2;
	
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
	 *  Retrieves the database meta data about the table schema given a
	 * particular data source id.
	 * This method is used when the table names are taken from the mappings.
	 * 
	 * @param sourceId
	 *            The database id.
	 * @return The database meta data object.
	 */
	public DBMetadata getMetaData(OBDADataSource sourceId, List<RelationJSQL> tables) throws SQLException {
		Connection conn = getConnection(sourceId);
		return getMetaData(conn, tables);
	}
	
	public static DBMetadata getMetaData(Connection conn, List<RelationJSQL> tables) throws SQLException {
		if (tables == null || tables.isEmpty())
			return getMetaData(conn);
		DBMetadata metadata = null;
		final DatabaseMetaData md = conn.getMetaData();
		if (md.getDatabaseProductName().contains("Oracle")) {
			// If the database engine is Oracle
			metadata = getOracleMetaData(md, conn, tables);
		} else if (md.getDatabaseProductName().contains("DB2")|| md.getDatabaseProductName().contains("H2")) {
			// If the database engine is IBM DB2
			metadata = getOtherMetaData(md, conn, tables, JDBC_UPPERCASE);
		}  else if (md.getDatabaseProductName().contains("PostgreSQL")) {
			// Postgres treats unquoted identifiers as lowercase
			metadata = getOtherMetaData(md, conn, tables, JDBC_LOWERCASE);
		} else {
			// For other database engines, i.e. mysql
			metadata = getOtherMetaData(md, conn, tables, JDBC_ORIGINALCASE);
		}
		return metadata;
	}

	/**
	 * Retrieve metadata for most of the database engine, e.g., MySQL and PostgreSQL
	 */
	private static DBMetadata getOtherMetaData(DatabaseMetaData md) throws SQLException {
		DBMetadata metadata = new DBMetadata(md);

		try (ResultSet rsTables = md.getTables(null, null, null, new String[] { "TABLE", "VIEW" })) {
			while (rsTables.next()) {
				Set<String> tableColumns = new HashSet<String>();

				final String tblCatalog = rsTables.getString("TABLE_CAT");
				final String tblName = rsTables.getString("TABLE_NAME");
				final String tblSchema = rsTables.getString("TABLE_SCHEM");
				final ArrayList<String> primaryKeys = getPrimaryKey(md, tblCatalog, tblSchema, tblName);
				final Map<String, Reference> foreignKeys = getForeignKey(md, null, null, tblName);

				TableDefinition td = new TableDefinition(tblName);

				try (ResultSet rsColumns = md.getColumns(tblCatalog, tblSchema, tblName, null)) {
					if (rsColumns == null) {
						continue;
					}
					while (rsColumns.next()) {
						final String columnName = rsColumns.getString("COLUMN_NAME");
						int dataType = rsColumns.getInt("DATA_TYPE");
						final boolean isPrimaryKey = primaryKeys.contains(columnName);
						final Reference reference = foreignKeys.get(columnName);
						final int isNullable = rsColumns.getInt("NULLABLE");
						
						final String typeName = rsColumns.getString("TYPE_NAME");
						
						if (dataType == 91 && typeName.equals("YEAR")) {
							dataType = -10000;
						}
						
						td.addAttribute(new Attribute(columnName, dataType, isPrimaryKey, reference, isNullable));

						// Check if the columns are unique regardless their letter cases
						if (!tableColumns.add(columnName.toLowerCase())) {
							// if exist
							throw new RuntimeException("The system cannot process duplicate table columns!");
						}
					}
					// Add this information to the DBMetadata
					metadata.add(td);
				} 
			}
		} 
		return metadata;
	}

	/**
	 * Retrieve metadata for most of the database engine, e.g., MySQL and PostgreSQL
	 * 
	 * Only retrieves metadata for the tables listed
	 * 
	 * Future plan to retrieve all tables when this list is empty?
	 * 
	 * @param tables 
	 * @param lowerCaseId: Decides whether casing of unquoted object identifiers should be changed
	 */
	private static DBMetadata getOtherMetaData(DatabaseMetaData md, Connection conn, List<RelationJSQL> tables, int caseIds) throws SQLException {
		DBMetadata metadata = new DBMetadata(md);
		
		/**
		 *  The sql to extract table names is now removed, since we instead use the
		 *  table names from the source sql of the mappings, given as the parameter tables
		 */

		Iterator<RelationJSQL> table_iter = tables.iterator();
		/* Obtain the column information for each relational object */
		while (table_iter.hasNext()) {
			
			RelationJSQL table = table_iter.next();
			Set<String> tableColumns = new HashSet<String>();
			String tblName = table.getTableName(); 
			
			log.debug("get metadata for " + tblName);
			
			/**
			 * tableGivenName is exactly the name the user provided, including schema prefix if that was
			 * provided, otherwise without.
			 */
			String tableGivenName = table.getGivenName();
			String tableSchema;
			if( table.getSchema()!=null)
				tableSchema = table.getSchema();
			else
				tableSchema = null;

			switch(caseIds){
			case JDBC_LOWERCASE:
				if(!table.isTableQuoted())
				tblName = tblName.toLowerCase();
				if(tableSchema != null && !table.isSchemaQuoted())
					tableSchema = tableSchema.toLowerCase();
				break;
			case JDBC_UPPERCASE: 
				if(!table.isTableQuoted())
				tblName = tblName.toUpperCase();
				if(tableSchema != null && !table.isSchemaQuoted())
					tableSchema = tableSchema.toUpperCase();
				break;
			}
			
			final ArrayList<String> primaryKeys = getPrimaryKey(md, null, tableSchema, tblName);
			final Map<String, Reference> foreignKeys = getForeignKey(md, null, tableSchema, tblName);

			TableDefinition td = new TableDefinition(tableGivenName);

			try (ResultSet rsColumns = md.getColumns(null, tableSchema, tblName, null)) {
				if (rsColumns == null) {
					continue;
				}
				while (rsColumns.next()) {
		
					/**
					 * Print JDBC metadata returned by the driver, enabled in debug mode
					 */
//					displayColumnNames(md, conn, rsColumns, tableSchema, tblName);
					
					
					final String columnName = rsColumns.getString("COLUMN_NAME");
					int dataType = rsColumns.getInt("DATA_TYPE");
					final boolean isPrimaryKey = primaryKeys.contains(columnName);
					final Reference reference = foreignKeys.get(columnName);
					final int isNullable = rsColumns.getInt("NULLABLE");
					
					/***
					 * Fix for MySQL YEAR
					 */
					final String typeName = rsColumns.getString("TYPE_NAME");
					
					if (dataType == 91 && typeName.equals("YEAR")) {
						dataType = -10000;
					}
					
					
					td.addAttribute(new Attribute(columnName, dataType, isPrimaryKey, reference, isNullable, typeName));
				
					// Check if the columns are unique regardless their letter cases
					if (!tableColumns.add(columnName.toLowerCase())) {
						// if exist
						throw new RuntimeException("The system cannot process duplicate table columns!");
					}
				}
				// Add this information to the DBMetadata
				metadata.add(td);
			} 
		}
		return metadata;
	}

	/**
	 * Retrieve metadata for SQL Server database engine
	 */
	private static DBMetadata getSqlServerMetaData(DatabaseMetaData md, Connection conn) throws SQLException {
		DBMetadata metadata = new DBMetadata(md);
		try (Statement stmt = conn.createStatement()) {
			/* Obtain the statement object for query execution */

			/* Obtain the relational objects (i.e., tables and views) */
			final String tableSelectQuery = "SELECT TABLE_CATALOG, TABLE_SCHEMA, TABLE_NAME " +
					"FROM INFORMATION_SCHEMA.TABLES " +
					"WHERE TABLE_TYPE='BASE TABLE' OR TABLE_TYPE='VIEW'";
			try (ResultSet resultSet = stmt.executeQuery(tableSelectQuery)) {

				/* Obtain the column information for each relational object */
				while (resultSet.next()) {
					final String tblCatalog = resultSet.getString("TABLE_CATALOG");
					final String tblSchema = resultSet.getString("TABLE_SCHEMA");
					final String tblName = resultSet.getString("TABLE_NAME");
					final ArrayList<String> primaryKeys = getPrimaryKey(md, tblCatalog, tblSchema, tblName);
					final Map<String, Reference> foreignKeys = getForeignKey(md, tblCatalog, tblSchema, tblName);

					TableDefinition td = new TableDefinition(tblName);
					try (ResultSet rsColumns = md.getColumns(tblCatalog, tblSchema, tblName, null)) {
						while (rsColumns.next()) {
							final String columnName = rsColumns.getString("COLUMN_NAME");
							final int dataType = rsColumns.getInt("DATA_TYPE");
							final boolean isPrimaryKey = primaryKeys.contains(columnName);
							final Reference reference = foreignKeys.get(columnName);
							final int isNullable = rsColumns.getInt("NULLABLE");
							td.addAttribute(new Attribute(columnName, dataType, isPrimaryKey, reference, isNullable));
						}
						// Add this information to the DBMetadata
						metadata.add(td);
					} 
				}
			}
		} 
		return metadata;
	}

	/**
	 * Retrieve metadata for DB2 database engine
	 */
	private static DBMetadata getDB2MetaData(DatabaseMetaData md, Connection conn) throws SQLException {
		DBMetadata metadata = new DBMetadata(md);
		/* Obtain the statement object for query execution */
		try (Statement stmt = conn.createStatement()) {
			
			/* Obtain the relational objects (i.e., tables and views) */
			final String tableSelectQuery = "SELECT TABSCHEMA, TABNAME " +
					"FROM SYSCAT.TABLES " +
					"WHERE OWNERTYPE='U' " +
					"	AND (TYPE='T' OR TYPE='V') " +
					"	AND TBSPACEID IN (SELECT TBSPACEID FROM SYSCAT.TABLESPACES WHERE TBSPACE LIKE 'USERSPACE%')";
			try (ResultSet resultSet = stmt.executeQuery(tableSelectQuery)) {
			
				/* Obtain the column information for each relational object */
				while (resultSet.next()) {
					final String tblSchema = resultSet.getString("TABSCHEMA");
					final String tblName = resultSet.getString("TABNAME");
					final ArrayList<String> primaryKeys = getPrimaryKey(md, null, tblSchema, tblName);
					final Map<String, Reference> foreignKeys = getForeignKey(md, null, tblSchema, tblName);
					
					TableDefinition td = new TableDefinition(tblName);
					try (ResultSet rsColumns = md.getColumns(null, tblSchema, tblName, null)) {
						
						while (rsColumns.next()) {
							final String columnName = rsColumns.getString("COLUMN_NAME");
							final int dataType = rsColumns.getInt("DATA_TYPE");
							final boolean isPrimaryKey = primaryKeys.contains(columnName);
							final Reference reference = foreignKeys.get(columnName);
							final int isNullable = rsColumns.getInt("NULLABLE");
							td.addAttribute(new Attribute(columnName, dataType, isPrimaryKey, reference, isNullable));
						}
						// Add this information to the DBMetadata
						metadata.add(td);
					} 
				}
			}
		} 
		return metadata;
	}
	
	/**
	 * Retrieve metadata for Oracle database engine
	 */
	private static DBMetadata getOracleMetaData(DatabaseMetaData md, Connection conn) throws SQLException {
		DBMetadata metadata = new DBMetadata(md);
		
		/* Obtain the statement object for query execution */
		try (Statement stmt = conn.createStatement()) {
			
			/* Obtain the table owner (i.e., schema name) */
			String tableOwner = "SYSTEM"; // by default
			try (ResultSet resultSet = stmt.executeQuery("SELECT user FROM dual")) {
				if (resultSet.next()) {
					tableOwner = resultSet.getString("user");
				}
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
			try (ResultSet resultSet = stmt.executeQuery(tableSelectQuery)) {
			
				/* Obtain the column information for each relational object */
				while (resultSet.next()) {

					final String tblName = resultSet.getString("object_name");
					final ArrayList<String> primaryKeys = getPrimaryKey(md, null, tableOwner, tblName);
					final Map<String, Reference> foreignKeys = getForeignKey(md, null, tableOwner, tblName);
					
					TableDefinition td = new TableDefinition(tblName);
					
					try (ResultSet rsColumns = md.getColumns(null, tableOwner, tblName, null)) {
						
						while (rsColumns.next()) {
							log.debug("=============== COLUMN METADATA ========================");
							// Print JDBC metadata returned by the driver, enable for debugging
							int metadataCount = rsColumns.getMetaData().getColumnCount();
							for (int j = 1; j < metadataCount+1; j++) {
								String columnName = rsColumns.getMetaData().getColumnName(j);
								log.debug("Column={} Value={}", columnName, rsColumns.getString(columnName));
							}
							
							final String columnName = rsColumns.getString("COLUMN_NAME");
							int dataType = rsColumns.getInt("DATA_TYPE");
							
							final boolean isPrimaryKey = primaryKeys.contains(columnName);
							final Reference reference = foreignKeys.get(columnName);
							final int isNullable = rsColumns.getInt("NULLABLE");
							
							/***
							 * To fix bug in Oracle 11 and up driver returning wrong datatype
							 */
							final String typeName = rsColumns.getString("TYPE_NAME");
							
							if (dataType == 93 && typeName.equals("DATE")) {
								dataType = 91;
							}
							
							td.addAttribute(new Attribute(columnName, dataType, isPrimaryKey, reference, isNullable));
						}
						// Add this information to the DBMetadata
						metadata.add(td);
					} 
				}
			}
		} 
		return metadata;
	}
	
	
	
	/**
	 * Retrieve metadata for Oracle database engine
	 * 
	 * Currently only retrieves metadata for the tables listed
	 * 
	 * Future plan to retrieve all tables when this list is empty?
	 * 
	 * @param tables 
	 */
	private static DBMetadata getOracleMetaData(DatabaseMetaData md, Connection conn, List<RelationJSQL> tables) throws SQLException {
		DBMetadata metadata = new DBMetadata(md);
		
		/* Obtain the statement object for query execution */
		try (Statement stmt = conn.createStatement()) {
			
			/* Obtain the table owner (i.e., schema name) */
			String loggedUser = "SYSTEM"; // by default
			try (ResultSet resultSet = stmt.executeQuery("SELECT user FROM dual")) {
				if (resultSet.next()) {
					loggedUser = resultSet.getString("user");
				}
			}
			
			/**
			 * The tables contains all tables which occur in the sql source queries
			 * Note that different spellings (casing, quotation marks, optional schema prefix) 
			 * may lead to the same table occurring several times 
			 */
			Iterator<RelationJSQL> table_iter = tables.iterator();
			/* Obtain the column information for each relational object */
			while (table_iter.hasNext()) {
				RelationJSQL table = table_iter.next();
//				String tblName = resultSet.getString("object_name");
//				tableOwner = resultSet.getString("owner_name");
				String tblName = table.getTableName();
				if(!table.isTableQuoted())
					tblName = tblName.toUpperCase();
				/**
				 * givenTableName is exactly the name the user provided, including schema prefix if that was
				 * provided, otherwise without.
				 */
				String tableGivenName = table.getGivenName();
				/**
				 * If there is a schema prefix, this must be the tableOwner argument to the 
				 * jdbc methods below. Otherwise, we use the logged in user. I guess null would
				 * also have worked in the latter case.
				 */
				String tableOwner;
				if( table.getSchema()!=null){
					tableOwner = table.getSchema();
					if(!table.isSchemaQuoted())
						tableOwner = tableOwner.toUpperCase();
				}
				else
					tableOwner = loggedUser.toUpperCase();
					
				final ArrayList<String> primaryKeys = getPrimaryKey(md, null, tableOwner, tblName);
				final Map<String, Reference> foreignKeys = getForeignKey(md, null, tableOwner, tblName);
				
				TableDefinition td = new TableDefinition(tableGivenName);
//				TableDefinition td = new TableDefinition(tblName);
				try (ResultSet rsColumns = md.getColumns(null, tableOwner, tblName, null)) {
			
					while (rsColumns.next()) {
						
						log.debug("=============== COLUMN METADATA ========================");

						// Print JDBC metadata returned by the driver, enable for debugging
						int metadataCount = rsColumns.getMetaData().getColumnCount();
						for (int j = 1; j < metadataCount+1; j++) {
							String columnName = rsColumns.getMetaData().getColumnName(j);
							log.debug("Column={} Value={}", columnName, rsColumns.getString(columnName));
						}
						
						final String columnName = rsColumns.getString("COLUMN_NAME");
						
						//TODO Oracle bug here - wrong automatic typing - Date vs DATETIME - driver ojdbc16-11.2.0.3
						/* Oracle returns 93 for DATE SQL types, but this corresponds to 
						 * TIMESTAMP. This causes a wrong typing to xsd:dateTime and later
						 * parsing errors. To avoid this bug manually type the column in the
						 * mapping. This may be a problem of the driver, try with other version
						 * I tried oracle thin driver ojdbc16-11.2.0.3
						 */
						int dataType = rsColumns.getInt("DATA_TYPE");
						final boolean isPrimaryKey = primaryKeys.contains(columnName);
						final Reference reference = foreignKeys.get(columnName);
						final int isNullable = rsColumns.getInt("NULLABLE");
						
						/***
						 * To fix bug in Oracle 11 and up driver returning wrong datatype
						 */
						final String typeName = rsColumns.getString("TYPE_NAME");
						
						if (dataType == 93 && typeName.equals("DATE")) {
							dataType = 91;
						}			
						
						td.addAttribute(new Attribute(columnName, dataType, isPrimaryKey, reference, isNullable, typeName));
					}
					// Add this information to the DBMetadata
					metadata.add(td);
					//metadata.add(tblName,tableOwner);
				} 
			}
		} 
		return metadata;
	}
	
	
	
	

	/* Retrieves the primary key(s) from a table */
	private static ArrayList<String> getPrimaryKey(DatabaseMetaData md, String tblCatalog, String schema, String table) throws SQLException {
		ArrayList<String> pk = new ArrayList<String>();
		try (ResultSet rsPrimaryKeys = md.getPrimaryKeys(tblCatalog, schema, table)) {
			while (rsPrimaryKeys.next()) {
				String colName = rsPrimaryKeys.getString("COLUMN_NAME");
				String pkName = rsPrimaryKeys.getString("PK_NAME");
				if (pkName != null) {
					pk.add(colName);
				}
			}
		} 
		return pk;
	}
	
	/* Retrieves the foreign key(s) from a table */
	private static Map<String, Reference> getForeignKey(DatabaseMetaData md, String tblCatalog, String schema, String table) throws SQLException {
		Map<String, Reference> fk = new HashMap<String, Reference>();
		try (ResultSet rsForeignKeys = md.getImportedKeys(tblCatalog, schema, table)) {
			;
			while (rsForeignKeys.next()) {
				String fkName = rsForeignKeys.getString("FK_NAME");
				String colName = rsForeignKeys.getString("FKCOLUMN_NAME");
				String pkTableName = rsForeignKeys.getString("PKTABLE_NAME");
				String pkColumnName = rsForeignKeys.getString("PKCOLUMN_NAME");
				fk.put(colName, new Reference(fkName, pkTableName, pkColumnName));
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
