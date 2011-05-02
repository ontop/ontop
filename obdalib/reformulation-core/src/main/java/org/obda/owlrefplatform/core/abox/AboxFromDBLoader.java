package org.obda.owlrefplatform.core.abox;

import inf.unibz.it.obda.api.datasource.JDBCConnectionManager;
import inf.unibz.it.obda.domain.DataSource;

import java.net.URI;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;

public class AboxFromDBLoader {

	public HashMap<URIIdentyfier, String> getMapper(DataSource source) throws Exception{
		
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
			throw new Exception("Error while loading the index for the Direct SQL Generator from the given data source.", e);
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
