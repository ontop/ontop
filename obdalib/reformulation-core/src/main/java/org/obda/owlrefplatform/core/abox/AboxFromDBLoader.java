package org.obda.owlrefplatform.core.abox;

import inf.unibz.it.obda.api.datasource.JDBCConnectionManager;
import inf.unibz.it.obda.domain.DataSource;

import java.net.URI;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Iterator;

public class AboxFromDBLoader {
	
	public void destroyDump(DataSource source) throws AboxLoaderException{
		Statement statement= null;
		try {	
			Connection conn = JDBCConnectionManager.getJDBCConnectionManager().getConnection(source);
			HashMap<URIIdentyfier, String> mapper = getMapper(source);
			Iterator<String> it = mapper.values().iterator();
			statement = conn.createStatement();
			while(it.hasNext()){
				statement.executeUpdate("DROP TABLE " + it.next());
			}
			statement.executeUpdate("DROP TABLE mapper");
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
			conn.commit();
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
