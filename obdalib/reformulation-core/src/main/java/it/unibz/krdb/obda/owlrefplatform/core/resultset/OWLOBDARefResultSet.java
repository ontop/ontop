package it.unibz.krdb.obda.owlrefplatform.core.resultset;

import it.unibz.krdb.obda.model.QueryResultSet;

import java.net.URI;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Vector;


public class OWLOBDARefResultSet implements QueryResultSet{

	private ResultSet set = null;
	
	public OWLOBDARefResultSet(ResultSet set){
		this.set=set;
	}
	
	public double getAsDouble(int column) throws SQLException {
		
		return set.getDouble(column);
	}

	public int getAsInteger(int column) throws SQLException {
		return set.getInt(column);
	}

	public Object getAsObject(int column) throws SQLException {
		return set.getObject(column);
	}

	public String getAsString(int column) throws SQLException {
		return set.getString(column);
	}

	public URI getAsURI(int column) throws SQLException {
		return URI.create(set.getString(column));
	}

	public int getColumCount() throws SQLException {
		return set.getMetaData().getColumnCount();
	}

	public int getFetchSize() throws SQLException {
		return set.getFetchSize();
	}

	public List<String> getSignature() throws SQLException {
		int i = getColumCount();
		Vector<String> signature = new Vector<String>();
		for(int j=1;j<=i;j++){
			signature.add(set.getMetaData().getColumnLabel(j));
		}
		return signature;
	}

	public boolean nextRow() throws SQLException {
		return set.next();
	}

	public void close() throws SQLException {
		set.close();
	}
}
