package org.obda.owlrefplatform.core.abox;

import inf.unibz.it.obda.api.datasource.JDBCConnectionManager;
import inf.unibz.it.obda.domain.DataSource;
import inf.unibz.it.obda.rdbmsgav.domain.RDBMSsourceParameterConstants;

import java.net.URI;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

public class AboxFromDBLoader {
	
	public void destroyDump(DataSource source) throws AboxLoaderException{
		Statement statement= null;
		try {	
			Connection conn = JDBCConnectionManager.getJDBCConnectionManager().getConnection(source);
			Vector<String> tables = new Vector<String>();
			if (source.getParameter(RDBMSsourceParameterConstants.DATABASE_DRIVER).equals("com.ibm.db2.jcc.DB2Driver")) {

				statement = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
				String dbname = source.getParameter(RDBMSsourceParameterConstants.DATABASE_NAME);
				// jdbc:db2://5.90.168.104:50000/MINIST:currentSchema=PROP;
				String[] sp1 = dbname.split("/");
				String catalog = sp1[sp1.length - 1].split(":")[0];
				String t2 = dbname.split("=")[1];
				String schema = t2.substring(0, t2.length() - 1);
				ResultSet r = statement.executeQuery("SELECT TABLE_NAME FROM SYSIBM.TABLES WHERE TABLE_CATALOG = '" + catalog
							+ "' AND TABLE_SCHEMA = '" + schema + "'");
				while(r.next()){
					tables.add(r.getString(1));
				}
				r.close();
				statement.close();
			} if (source.getParameter(RDBMSsourceParameterConstants.DATABASE_DRIVER).equals("oracle.jdbc.driver.OracleDriver")) {
					// select table_name from user_tables
				statement = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
				ResultSet r = statement.executeQuery("select table_name from user_tables");
				while(r.next()){
					tables.add(r.getString(1));
				}
				r.close();
				statement.close();
			} else {
					//postgres and mysql
				DatabaseMetaData metdata = conn.getMetaData();
				String catalog = null;
				String schemaPattern = "%";
				String tableNamePattern = "%";
				String types[] = { "TABLE" };
				conn.setAutoCommit(true);
				ResultSet r = metdata.getTables(catalog, schemaPattern, tableNamePattern, types);
				conn.setAutoCommit(false);
				while(r.next()){
					tables.add(r.getString("TABLE_NAME"));
				}
				r.close();
			}
			Iterator<String> it = tables.iterator();
			statement = conn.createStatement();
			while(it.hasNext()){
				statement.executeUpdate("DROP TABLE " + it.next());
			}
			statement.close();
		} catch (Exception e) {
			throw new AboxLoaderException("Error while loading the index for the Direct SQL Generator from the given data source.", e);
		}
		finally{
			try {
				statement.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public HashMap<URIIdentyfier, String> getMapper(DataSource source) throws AboxLoaderException{
		
		Statement statement= null;
		ResultSet res = null;
		try {
			Connection conn = JDBCConnectionManager.getJDBCConnectionManager().getConnection(source);
			HashMap<URIIdentyfier, String> mapper = new HashMap<URIIdentyfier, String>();
			String sql = "Select uri, type, tablename from mapper";
			statement = conn.createStatement();
			res = statement.executeQuery(sql);
			while(res.next()){
				URI uri = URI.create(res.getString("uri"));
				URIType type = URIType.getURIType(res.getString("type"));
				String tablename = res.getString("tablename");
				
				URIIdentyfier id = new URIIdentyfier(uri, type);
				mapper.put(id, tablename);
			}
			return mapper;
		} catch (Exception e) {
			throw new AboxLoaderException("Error while loading the index for the Direct SQL Generator from the given data source.", e);
		}
		finally{
			try {
				res.close();
				statement.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	} 
	
}
