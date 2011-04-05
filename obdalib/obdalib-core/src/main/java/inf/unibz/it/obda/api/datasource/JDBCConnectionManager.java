package inf.unibz.it.obda.api.datasource;

import inf.unibz.it.obda.domain.DataSource;
import inf.unibz.it.obda.gui.swing.datasource.panels.ColumnInspectorTableModel;
import inf.unibz.it.obda.gui.swing.exception.NoDatasourceSelectedException;
import inf.unibz.it.obda.rdbmsgav.domain.RDBMSsourceParameterConstants;

import java.net.URI;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	
	Logger				log			= LoggerFactory.getLogger(JDBCConnectionManager.class);
	
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
			
			try {
				Class d = Class.forName(driver);
			} catch (Exception e) {
				log.warn("Driver class not found!");
			}
			con = DriverManager.getConnection(url+dbname, username, password);
			Boolean b = (Boolean) properties.get(JDBC_AUTOCOMMIT);
			con.setAutoCommit(b.booleanValue());
			connectionPool.put(connID, con);
		}else{
			con.close();
			try {
				Class d = Class.forName(driver);
			} catch (Exception e) {
				log.warn("Driver class not found!");
			}			con = DriverManager.getConnection(url+dbname, username, password);
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
	
	public Statement getStatement(URI connID, DataSource ds) throws Exception{
		Connection con = connectionPool.get(connID);
		if(con == null || con.isClosed()){
			createConnection(ds);
			con = connectionPool.get(connID);
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
		return st;
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
	
	public Connection getConnection(DataSource source) throws NoDatasourceSelectedException, ClassNotFoundException, SQLException {
		if(source == null){
			throw new SQLException("No data source selected.");
		}
		Connection con = connectionPool.get(source.getSourceID());
		if(con == null){
			createConnection(source);
		}
		return connectionPool.get(source.getSourceID());

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
