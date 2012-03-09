package it.unibz.krdb.sql;

import it.unibz.krdb.obda.model.OBDADataSource;
import it.unibz.krdb.obda.model.OBDAException;
import it.unibz.krdb.obda.model.impl.RDBMSourceParameterConstants;
import it.unibz.krdb.sql.api.Attribute;

import java.net.URI;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
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

	// /**
	// * Creates all the database connections that are defined in the OBDA
	// model.
	// * Call this method to start filling the connection pool.
	// *
	// * @param model
	// * The OBDA model.
	// */
	// public void setupConnection(OBDAModel model) {
	// List<OBDADataSource> sources = model.getSources();
	// for (OBDADataSource ds : sources) {
	// try {
	// setConnection(ds);
	// } catch (SQLException e) {
	// String message =
	// String.format("Fail to create a connection.\nReason: %s for data source %s",
	// e.getMessage(),
	// ds.getSourceID());
	// log.error(message);
	// }
	// }
	// }

	// /**
	// * Constructs a new database connection object and then registers it to
	// the
	// * connection pool.
	// *
	// * @param dataSource
	// * The data source object.
	// * @throws SQLException
	// */
	// public void setConnection(OBDADataSource dataSource) throws SQLException
	// {
	// Connection conn = createConnection(dataSource);
	// registerConnection(dataSource.getSourceID(), conn);
	// }

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

		if (connectionPool.get(dataSource) != null && connectionPool.get(dataSource).isClosed())
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
			// Does nothing because the SQLException handles this problem also.
		}

		Connection conn = DriverManager.getConnection(url, username, password);

//		boolean bAutoCommit = ((Boolean) properties.get(JDBC_AUTOCOMMIT)).booleanValue();
//		conn.setAutoCommit(bAutoCommit);

		connectionPool.put(dataSource, conn);

		return conn;
	}

	// /*
	// * Store the connection object to the connection pool. Any existing
	// * connection with the same ID will be replaced by the given connection.
	// */
	// private void registerConnection(URI sourceId, Connection conn) {
	// boolean bRemoved = removeConnection(sourceId);
	// if (bRemoved) {
	// connectionPool.put(sourceId, conn);
	// } else {
	// log.error("Registration failed: Can't remove the existing connection.");
	// }
	// }

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
		if (!alive)
			createConnection(source);

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
		if (existing.isClosed())
			throw new OBDAException("Connection is already close");
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
		if (conn == null) {
			return false;
		}
		return !conn.isClosed();
	}

	// /**
	// * Executes the query string using the given connection ID. If it is
	// * successful, the method will return the result set.
	// *
	// * @param sourceId
	// * The connection ID (usually the same as the data source URI).
	// * @param query
	// * The SQL query string.
	// * @return The Result Set object.
	// * @throws SQLException
	// */
	// public ResultSet executeQuery(URI sourceId, String query) throws
	// SQLException {
	// ResultSet rs = null;
	// Statement st = null;
	// try {
	// Connection conn = getConnection(sourceId);
	//
	// int type = (Integer) properties.get(JDBC_RESULTSETTYPE);
	// int concur = (Integer) properties.get(JDBC_RESULTSETCONCUR);
	// int fetchsize = (Integer) properties.get(JDBC_FETCHSIZE);
	//
	// st = conn.createStatement(type, concur);
	// st.setFetchSize(fetchsize);
	// rs = st.executeQuery(query);
	// } catch (SQLException e) {
	// log.error(e.getMessage());
	// } finally {
	// // TODO: For the purpose of displaying only the query result, it is
	// // better
	// // not to return the ResultSet object. Instead, store all the result
	// // into
	// // another object and return that object. In this way, we can close
	// // both
	// // the Statement and ResultSet in advanced.
	// //
	// // To reduce this memory leak, currently the solution is that we
	// // close the
	// // ResultSet manually in the caller side.
	// // st.close();
	// }
	// return rs;
	// }

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
	public static DBMetadata getMetaData(Connection conn) {
		try {

			DatabaseMetaData md = conn.getMetaData();

			ResultSet rsTables = md.getTables(null, null, null, null);

			DBMetadata metadata = new DBMetadata();

			metadata.setDriverName(md.getDriverName());
			metadata.setDatabaseProductName(md.getDatabaseProductName());

			metadata.setStoresLowerCaseIdentifier(md.storesLowerCaseIdentifiers());
			metadata.setStoresLowerCaseQuotedIdentifiers(md.storesLowerCaseQuotedIdentifiers());
			metadata.setStoresMixedCaseIdentifiers(md.storesMixedCaseIdentifiers());
			metadata.setStoresMixedCaseQuotedIdentifiers(md.storesMixedCaseQuotedIdentifiers());
			metadata.setStoresUpperCaseIdentifiers(md.storesUpperCaseIdentifiers());
			metadata.setStoresUpperCaseQuotedIdentifiers(md.storesUpperCaseQuotedIdentifiers());

			while (rsTables.next()) {
				String tblCatalog = rsTables.getString("TABLE_CAT");
				String tblName = rsTables.getString("TABLE_NAME");
				String tblSchema = rsTables.getString("TABLE_SCHEM");

				ResultSet rsColumns = md.getColumns(tblCatalog, tblSchema, tblName, null);
				ArrayList<String> pk = getPrimaryKey(md, tblCatalog, tblSchema, tblName);

				TableDefinition td = new TableDefinition();
				td.setName(tblName);
				for (int pos = 1; rsColumns.next(); pos++) {
					td.setAttribute(
							pos,
							new Attribute(rsColumns.getString("COLUMN_NAME"), rsColumns.getInt("DATA_TYPE"), pk.contains(rsColumns
									.getString("COLUMN_NAME")), rsColumns.getInt("NULLABLE")));
				}
				// Add this information to the DBMetadata
				metadata.add(td);
			}
			return metadata;
		} catch (Exception e) {
			throw new RuntimeException("Impossible to get DB metadata", e);
		}
	}

	/* Retrives the primary key(s) from a table */
	private static ArrayList<String> getPrimaryKey(DatabaseMetaData md, String tblCatalog, String schema, String table) throws SQLException {
		ArrayList<String> pk = new ArrayList<String>();
		ResultSet rsPrimaryKeys = md.getPrimaryKeys(tblCatalog, schema, table);
		while (rsPrimaryKeys.next()) {
			String colName = rsPrimaryKeys.getString("COLUMN_NAME");
			String pkName = rsPrimaryKeys.getString("PK_NAME");
			if (pkName != null) {
				pk.add(colName);
			}
		}
		return pk;
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
