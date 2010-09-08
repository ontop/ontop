package inf.unibz.it.obda.api.datasource;

import inf.unibz.it.obda.domain.DataSource;
import inf.unibz.it.obda.gui.swing.datasource.panels.ColumnInspectorTableModel;
import inf.unibz.it.obda.gui.swing.exception.NoDatasourceSelectedException;
import inf.unibz.it.obda.rdbmsgav.domain.RDBMSsourceParameterConstants;

import java.net.URI;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;
import java.util.Vector;

public class JDBCConnectionManager {

	public static final String JDBC_AUTOCOMMIT = "autocommit";
	public static final String JDBC_FETCHSIZE = "fetchsize";
	public static final String JDBC_RESULTSETTYPE = "resultsettype";
	public static final String JDBC_RESULTSETCONCUR = "resultsetconcur";
	
	private static JDBCConnectionManager instance = null;
	
	private HashMap<String, Object> properties = null;
	private HashMap<URI, Connection> connectionPool = null;
	
	private Vector<Statement> statementList = null;
	private Statement currentStatement = null;
	
	private String currentDriver = null;
	
	private JDBCConnectionManager(){
		properties = new HashMap<String, Object>();
		properties.put(JDBC_AUTOCOMMIT, false);
		properties.put(JDBC_FETCHSIZE, 100);
		properties.put(JDBC_RESULTSETCONCUR, ResultSet.CONCUR_READ_ONLY);
		properties.put(JDBC_RESULTSETTYPE, ResultSet.TYPE_FORWARD_ONLY);
		connectionPool = new HashMap<URI, Connection>();
		statementList = new Vector<Statement>();
	}
	
	public void createConnection(DataSource ds) throws NoDatasourceSelectedException, ClassNotFoundException, SQLException{
		
		if (ds == null) {
			throw new NoDatasourceSelectedException("No source selected");
		}
		String driver = ds.getParameter(RDBMSsourceParameterConstants.DATABASE_DRIVER);
		currentDriver = driver;
		String url = ds.getParameter(RDBMSsourceParameterConstants.DATABASE_URL);
		String dbname = ds.getParameter(RDBMSsourceParameterConstants.DATABASE_NAME);
		String username = ds.getParameter(RDBMSsourceParameterConstants.DATABASE_USERNAME);
		String password = ds.getParameter(RDBMSsourceParameterConstants.DATABASE_PASSWORD);
		URI connID = ds.getSourceID();
		
		Connection con = connectionPool.get(connID);
		if(con == null){
			
			Class d = Class.forName(driver);
			con = DriverManager.getConnection(url+dbname, username, password);
			Boolean b = (Boolean) properties.get(JDBC_AUTOCOMMIT);
			con.setAutoCommit(b.booleanValue());
			connectionPool.put(connID, con);
		}else{
			con.close();
			Class d = Class.forName(driver);
			con = DriverManager.getConnection(url+dbname, username, password);
			Boolean b = (Boolean) properties.get(JDBC_AUTOCOMMIT);
			con.setAutoCommit(b.booleanValue());
			connectionPool.put(connID, con);
		}
	}
	
	public boolean isConnectionAlive(URI connID) throws SQLException{
		Connection con = connectionPool.get(connID);
		if(con == null){
			return false;
		}else{
			return !con.isClosed();
		}
	}
	
	public ResultSet executeQuery(URI connID, String query, DataSource ds) throws SQLException{
		Connection con = connectionPool.get(connID);
		if(con == null || con.isClosed()){
			try {
				createConnection(ds);
			} catch (NoDatasourceSelectedException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
			con = connectionPool.get(ds.getSourceID());
			throw new SQLException("No connection established for the given id: " + connID);
		}else{
			if(currentStatement != null){
				currentStatement.close();
				currentStatement = null;
			}
			int type = (Integer) properties.get(JDBC_RESULTSETTYPE);
			int concur = (Integer) properties.get(JDBC_RESULTSETCONCUR);
			Statement st = con.createStatement(type, concur);
			int fetchsize = (Integer)properties.get(JDBC_FETCHSIZE);
			st.setFetchSize(fetchsize);
			statementList.add(st);
			currentStatement = st;
			return st.executeQuery(query);
		}
	}
	
	public ResultSet executeQuery(DataSource ds, String query) throws NoDatasourceSelectedException, ClassNotFoundException, SQLException{
		Connection con = connectionPool.get(ds.getSourceID());
		if(con == null){
			createConnection(ds);
			con = connectionPool.get(ds.getSourceID());
		}
		if(currentStatement != null){
			currentStatement.close();
			currentStatement = null;
		}
		int type = (Integer) properties.get(JDBC_RESULTSETTYPE);
		int concur = (Integer) properties.get(JDBC_RESULTSETCONCUR);
		Statement st = con.createStatement(type, concur);
		int fetchsize = (Integer)properties.get(JDBC_FETCHSIZE);
		st.setFetchSize(fetchsize);
		statementList.add(st);
		currentStatement = st;
		return st.executeQuery(query);
		
	}
	
	public static JDBCConnectionManager getJDBCConnectionManager(){
		if(instance==null){
			instance = new JDBCConnectionManager();
		}
		return instance;
	}
	
	public void closeConnections() throws SQLException{
		
		Iterator<Statement> sit = statementList.iterator();
		while(sit.hasNext()){
			Statement s = sit.next();
			if(!s.isClosed()){
				s.close();
			}
		}
		
		Iterator<URI> it = connectionPool.keySet().iterator();
		
		while(it.hasNext()){
			Connection con = connectionPool.get(it.next());
			con.close();
		}
	}
	
	public void setProperty(String key, Object value) throws SQLException{
		
			if(currentStatement != null){
				currentStatement.close();
			}
		
		properties.put(key, value);
		Iterator<URI> it = connectionPool.keySet().iterator();
		while(it.hasNext()){
			Connection c = connectionPool.get(it.next());
			if(c != null){
					c.commit();
					c.close();
			}
		}
		connectionPool.clear();
	}
	
	public String getApprimateRowCount(String name, DataSource ds) throws NoDatasourceSelectedException, ClassNotFoundException, SQLException{
		Connection con = connectionPool.get(ds.getSourceID());
		if(con == null){
			createConnection(ds);
		}
		con = connectionPool.get(ds.getSourceID());
		
		if (ds.getParameter(RDBMSsourceParameterConstants.DATABASE_DRIVER).equals("org.postgresql.Driver")) {
			Statement statement = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			String query = "select reltuples from pg_class where relname='" + name+"'";
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
	
	public String getRowCount(String name, DataSource ds) throws NoDatasourceSelectedException, ClassNotFoundException, SQLException {
		
		Connection con = connectionPool.get(ds.getSourceID());
		if(con == null){
			createConnection(ds);
		}
		con = connectionPool.get(ds.getSourceID());
		Statement statement = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
		// Run the query, creating a ResultSet
		ResultSet r = statement.executeQuery("SELECT COUNT(*) FROM " + name.toUpperCase());
		// Create and return a TableModel for the ResultSet
		r.next();
		// r.absolute(1);
		if (r.first()) {
			Object temp = r.getObject(1);
			// count = ((Long) temp).toString();
			String count = temp.toString();
			return count;
		}
		return "Count error";
		
	}
	
	public ResultSet getRelationsResultSet(DataSource source) throws NoDatasourceSelectedException, ClassNotFoundException, SQLException {
		if(source == null){
			throw new SQLException("No data source selected.");
		}
		Connection con = connectionPool.get(source.getSourceID());
		if(con == null){
			createConnection(source);
		}
		con = connectionPool.get(source.getSourceID());
		if(con != null){
			if (source.getParameter(RDBMSsourceParameterConstants.DATABASE_DRIVER).equals("com.ibm.db2.jcc.DB2Driver")) {

				Statement statement = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
				String dbname = source.getParameter(RDBMSsourceParameterConstants.DATABASE_NAME);
				// jdbc:db2://5.90.168.104:50000/MINIST:currentSchema=PROP;
				String[] sp1 = dbname.split("/");
				String catalog = sp1[sp1.length - 1].split(":")[0];
				String t2 = dbname.split("=")[1];
				String schema = t2.substring(0, t2.length() - 1);
				ResultSet r = statement.executeQuery("SELECT TABLE_NAME FROM SYSIBM.TABLES WHERE TABLE_CATALOG = '" + catalog
						+ "' AND TABLE_SCHEMA = '" + schema + "'");
				return r;
			} if (source.getParameter(RDBMSsourceParameterConstants.DATABASE_DRIVER).equals("oracle.jdbc.driver.OracleDriver")) {
				// select table_name from user_tables
				Statement statement = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
				ResultSet r = statement.executeQuery("select table_name from user_tables");
				return r;
			} else {
				//postgres and mysql
				DatabaseMetaData metdata = con.getMetaData();
				String catalog = null;
				String schemaPattern = "%";
				String tableNamePattern = "%";
				String types[] = { "TABLE" };
				con.setAutoCommit(true);
				ResultSet r = metdata.getTables(catalog, schemaPattern, tableNamePattern, types);
				con.setAutoCommit(false);
				return r;
			}
		}else{
			throw new SQLException("No connection established for id: " + source.getSourceID());
		}
	}
	
	public void setPorperties(Properties p){
		Object auto =p.get(JDBC_AUTOCOMMIT);
		if(auto != null){
			properties.put(JDBC_AUTOCOMMIT, auto);
		}
		
		Object size = p.get(JDBC_FETCHSIZE);
		if(size != null){
			properties.put(JDBC_FETCHSIZE, size);
		}
		
		Object type = p.get(JDBC_RESULTSETTYPE);
		if(type != null){
			properties.put(JDBC_RESULTSETTYPE, type);
		}
		
		Object concur = p.get(JDBC_RESULTSETCONCUR);
		if(concur != null){
			properties.put(JDBC_RESULTSETCONCUR, concur);
		}
	}
	
	public ColumnInspectorTableModel getTableDescriptionTableModel(DataSource source, String tablename) throws SQLException, NoDatasourceSelectedException, ClassNotFoundException {
		
		if(source == null){
			throw new SQLException("No data source selected.");
		}
		
		Connection connection = connectionPool.get(source.getSourceID());
		if(connection == null){
			createConnection(source);
		}
		connection = connectionPool.get(source.getSourceID());
		
		String driverClassName = source.getParameter(RDBMSsourceParameterConstants.DATABASE_DRIVER);
		
		if(currentStatement != null){
			currentStatement.close();
		}
		connection.setAutoCommit(true);
		currentStatement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
		String query = "";
		if(driverClassName.equals("oracle.jdbc.driver.OracleDriver")){
			query = "select * from " + tablename + " where rownum =0";
		}else if(driverClassName.equals("com.ibm.db2.jcc.DB2Driver")){
			query = "select * from " + tablename + " fetch first 1 rows only ";
		}else if(driverClassName.equals("org.postgresql.Driver")){
			query = "select * from \"" + tablename + "\" LIMIT 1";
		}else{
			query = "select * from " + tablename + " LIMIT 1";
		}
		ResultSet r = currentStatement.executeQuery(query);
		ResultSetMetaData rmeta = r.getMetaData();
		return new ColumnInspectorTableModel(rmeta);
	}
}
