package it.unibz.krdb.sql;

import it.unibz.krdb.obda.gui.swing.exception.NoDatasourceSelectedException;
import it.unibz.krdb.obda.model.OBDADataSource;
import it.unibz.krdb.obda.model.impl.RDBMSourceParameterConstants;

import java.net.URI;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JDBCConnectionManager {

	public static final String				JDBC_AUTOCOMMIT			= "autocommit";
	public static final String				JDBC_FETCHSIZE			= "fetchsize";
	public static final String				JDBC_RESULTSETTYPE		= "resultsettype";
	public static final String				JDBC_RESULTSETCONCUR	= "resultsetconcur";

	private static JDBCConnectionManager	instance				= null;

	private HashMap<String, Object>			properties				= null;
	private HashMap<URI, Connection>		connectionPool			= null;
	private HashMap<URI, DBMetadata>	    metadataCache			= null;
	
	private Vector<Statement>				statementList			= null;
	private Statement						currentStatement		= null;

	Logger									log						= LoggerFactory.getLogger(JDBCConnectionManager.class);

	private JDBCConnectionManager() {
		properties = new HashMap<String, Object>();
		properties.put(JDBC_AUTOCOMMIT, false);
		properties.put(JDBC_FETCHSIZE, 100);
		properties.put(JDBC_RESULTSETCONCUR, ResultSet.CONCUR_READ_ONLY);
		properties.put(JDBC_RESULTSETTYPE, ResultSet.TYPE_FORWARD_ONLY);
		connectionPool = new HashMap<URI, Connection>();
		metadataCache = new HashMap<URI, DBMetadata>();
		statementList = new Vector<Statement>();
	}

	public void createConnection(OBDADataSource ds) throws ClassNotFoundException, SQLException {
		if (ds == null) {
			RuntimeException ex = new RuntimeException("Invalid datasource: null");
			ex.fillInStackTrace();
			throw ex;
		}
		
		Connection con = connectionPool.get(ds.getSourceID());
		if (con == null) {
			testConnection(ds);
			collectMetadata(ds);
		}
	}
	
	public void testConnection(OBDADataSource ds) throws SQLException {
		String url = ds.getParameter(RDBMSourceParameterConstants.DATABASE_URL);
		String username = ds.getParameter(RDBMSourceParameterConstants.DATABASE_USERNAME);
		String password = ds.getParameter(RDBMSourceParameterConstants.DATABASE_PASSWORD);
		String driver = ds.getParameter(RDBMSourceParameterConstants.DATABASE_DRIVER);
		
		try {
			Class.forName(driver);
		}
		catch (ClassNotFoundException e1) {
			// Does nothing because the SQLException handles this problem also.
		}		
		
		Connection conn = DriverManager.getConnection(url, username, password);		
		
		boolean bAutoCommit = ((Boolean)properties.get(JDBC_AUTOCOMMIT)).booleanValue();
		conn.setAutoCommit(bAutoCommit);

		replaceConnectionFromPool(ds.getSourceID(), conn);	
	}
	
	private void replaceConnectionFromPool(URI connID, Connection newConn) throws SQLException {
		Connection oldConn = connectionPool.remove(connID);
		if (oldConn != null) {
			oldConn.close();
		}
		connectionPool.put(connID, newConn);
	}
	
	public boolean isConnectionAlive(URI connID) throws SQLException {
		Connection con = connectionPool.get(connID);
		if (con == null) {
			return false;
		} else {
			return !con.isClosed();
		}
	}

	public Statement getStatement(URI connID, OBDADataSource ds) throws Exception {
		Connection con = connectionPool.get(connID);
		if (con == null || con.isClosed()) {
			createConnection(ds);
			con = connectionPool.get(connID);
		}
		if (currentStatement != null) {
			currentStatement.close();
			currentStatement = null;
		}
		int type = (Integer) properties.get(JDBC_RESULTSETTYPE);
		int concur = (Integer) properties.get(JDBC_RESULTSETCONCUR);
		Statement st = con.createStatement(type, concur);
		int fetchsize = (Integer) properties.get(JDBC_FETCHSIZE);
		st.setFetchSize(fetchsize);
		return st;
	}

	public ResultSet executeQuery(URI connID, String query, OBDADataSource ds) throws SQLException {
		ResultSet result = null;
		Connection con = connectionPool.get(connID);
		if (con == null || con.isClosed()) {
			try {
				createConnection(ds);
			} catch (ClassNotFoundException e) {
				SQLException ex = new SQLException(e);
				ex.fillInStackTrace();
				throw ex;
			}
			con = connectionPool.get(ds.getSourceID());
			throw new SQLException("No connection established for the given id: " + connID);
		} else {
			if (currentStatement != null) {
				currentStatement.close();
				currentStatement = null;
			}
			int type = (Integer) properties.get(JDBC_RESULTSETTYPE);
			int concur = (Integer) properties.get(JDBC_RESULTSETCONCUR);
			int fetchsize = (Integer) properties.get(JDBC_FETCHSIZE);
			try {
				Statement st = con.createStatement(type, concur);
				st.setFetchSize(fetchsize);
				result = st.executeQuery(query);
				statementList.add(st);
				currentStatement = st;
			} catch (SQLException e) {
				con.rollback();
				throw e;
			}
		}
		return result;
	}

	public ResultSet executeQuery(OBDADataSource ds, String query) throws NoDatasourceSelectedException, ClassNotFoundException, SQLException {
		Connection con = connectionPool.get(ds.getSourceID());
		if (con == null) {
			createConnection(ds);
			con = connectionPool.get(ds.getSourceID());
		}
		if (currentStatement != null) {
			currentStatement.close();
			currentStatement = null;
		}
		int type = (Integer) properties.get(JDBC_RESULTSETTYPE);
		int concur = (Integer) properties.get(JDBC_RESULTSETCONCUR);
		Statement st = con.createStatement(type, concur);
		int fetchsize = (Integer) properties.get(JDBC_FETCHSIZE);
		st.setFetchSize(fetchsize);
		statementList.add(st);
		currentStatement = st;
		return st.executeQuery(query);
	}

	public static JDBCConnectionManager getJDBCConnectionManager() {
		return new JDBCConnectionManager();
	}

	public void closeConnections() throws SQLException {

		Iterator<Statement> sit = statementList.iterator();
		while (sit.hasNext()) {
			Statement s = sit.next();
			if (!s.isClosed()) {
				s.close();
			}
		}

		Iterator<URI> it = connectionPool.keySet().iterator();

		while (it.hasNext()) {
			Connection con = connectionPool.get(it.next());
			con.close();
		}
	}

	public void setProperty(String key, Object value) throws SQLException {

		if (currentStatement != null) {
			try {
				currentStatement.close();
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		}

		properties.put(key, value);
		Iterator<URI> it = connectionPool.keySet().iterator();
		while (it.hasNext()) {
			Connection c = connectionPool.get(it.next());
			if (c != null) {
				if (c.getAutoCommit()) {
					try {
						c.commit();
					} catch (Exception e) {
						log.error(e.getMessage(), e);
					}

				}
				try {
					c.close();
				} catch (Exception e) {
					log.warn(e.getMessage(), e);
				}
			}
		}
		connectionPool.clear();
	}

	public String getApprimateRowCount(String name, OBDADataSource ds) throws NoDatasourceSelectedException, ClassNotFoundException,
			SQLException {
		Connection con = connectionPool.get(ds.getSourceID());
		if (con == null) {
			createConnection(ds);
		}
		con = connectionPool.get(ds.getSourceID());

		if (ds.getParameter(RDBMSourceParameterConstants.DATABASE_DRIVER).equals("org.postgresql.Driver")) {
			Statement statement = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			String query = "select reltuples from pg_class where relname='" + name + "'";
			ResultSet r = statement.executeQuery(query);
			r.next();
			if (r.first()) {
				Object temp = r.getObject(1);
				String count = temp.toString();
				return count;
			}
		}
		return "Count error";
	}

	public Connection getConnection(OBDADataSource source) throws ClassNotFoundException, SQLException {
		if (source == null) {
			throw new SQLException("No data source selected.");
		}
		Connection con = connectionPool.get(source.getSourceID());
		if ((con == null) || (con.isClosed())) {
			createConnection(source);
		}
		return connectionPool.get(source.getSourceID());

	}

	public void setPorperties(Properties p) {
		Object auto = p.get(JDBC_AUTOCOMMIT);
		if (auto != null) {
			properties.put(JDBC_AUTOCOMMIT, auto);
		}

		Object size = p.get(JDBC_FETCHSIZE);
		if (size != null) {
			properties.put(JDBC_FETCHSIZE, size);
		}

		Object type = p.get(JDBC_RESULTSETTYPE);
		if (type != null) {
			properties.put(JDBC_RESULTSETTYPE, type);
		}

		Object concur = p.get(JDBC_RESULTSETCONCUR);
		if (concur != null) {
			properties.put(JDBC_RESULTSETCONCUR, concur);
		}
	}
	
	public void collectMetadata(OBDADataSource source) throws SQLException, ClassNotFoundException {
		
		if (source == null) {
			throw new SQLException("No data source found!");
		}

		URI sourceUri = source.getSourceID();
		Connection conn = connectionPool.get(sourceUri);
		if (conn == null) {
			createConnection(source);
			conn = connectionPool.get(sourceUri);
		}
		
		DatabaseMetaData md = conn.getMetaData();
		ResultSet rsTables = md.getTables("metadata", null, null, null);
		
		DBMetadata metadata = new DBMetadata(sourceUri.toString()); // TODO: This should be the database name. The OBDADataSource must implement the database name setting.
		
		while(rsTables.next()) {
			String tblName = rsTables.getString("TABLE_NAME");
			String tblSchema = rsTables.getString("TABLE_SCHEM");
			ResultSet rsColumns = md.getColumns("metadata", tblSchema, tblName, null);
			ArrayList<String> pk = getPrimaryKey(md, tblSchema, tblName);
            while(rsColumns.next()) {
                String colName = rsColumns.getString("COLUMN_NAME");
                String colType = rsColumns.getString("TYPE_NAME");
                boolean bPrimaryKey = pk.contains(colName);
                int canNull = rsColumns.getInt("NULLABLE");
                
                // Add this information to the DBMetadata
                metadata.add(tblSchema, tblName, colName, colType, bPrimaryKey, canNull);
            }                
		}		
		metadataCache.put(sourceUri, metadata);
	}
	
	public DBMetadata getMetadata(URI sourceUri) {
		return metadataCache.get(sourceUri);
	}
	
	private ArrayList<String> getPrimaryKey(DatabaseMetaData md, String schema, String table) throws SQLException {
		ArrayList<String> pk = new ArrayList<String>();
		ResultSet rsPrimaryKeys = md.getPrimaryKeys("metadata", schema, table);
		while(rsPrimaryKeys.next()) {
			String colName = rsPrimaryKeys.getString("COLUMN_NAME");
			String pkName = rsPrimaryKeys.getString("PK_NAME");
			if (pkName != null) {
				pk.add(colName);
			}
		}
		return pk;
	}
}
