package org.obda.owlrefplatform.core.resultset;

import it.unibz.krdb.obda.model.QueryResultSet;

import java.net.URI;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Vector;

/**
 * The boolean result set returned by an OBDA statement.
 * @author Manfred Gerstgrasser
 *
 */

public class BooleanOWLOBDARefResultSet implements QueryResultSet{

	private ResultSet set = null;
	private boolean isTrue = false;
	private int counter = 0;
	
	public BooleanOWLOBDARefResultSet(ResultSet set){
		this.set=set;
		try {
			isTrue = set.next();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	
	@Override
	public void close() throws SQLException {
		set.close();		
	}

	/**
	 * return 1 if true 0 otherwise
	 */
	@Override
	public double getAsDouble(int column) throws SQLException {
		if(isTrue){
			return 1;
		}else{
			return 0;
		}
	}

	/**
	 * return 1 if true 0 otherwise
	 */
	@Override
	public int getAsInteger(int column) throws SQLException {
		if(isTrue){
			return 1;
		}else{
			return 0;
		}
	}

	/**
	 * returns the true value as object
	 */
	@Override
	public Object getAsObject(int column) throws SQLException {
		if(isTrue){
			return "true";
		}else{
			return "false";
		}
	}

	/**
	 * returns the true value as string
	 */
	@Override
	public String getAsString(int column) throws SQLException {
		if(isTrue){
			return "true";
		}else{
			return "false";
		}
	}

	/**
	 * returns the true value as URI
	 */
	@Override
	public URI getAsURI(int column) throws SQLException {
		if(isTrue){
			return URI.create("true");
		}else{
			return URI.create("false");
		}
	}

	/**
	 * returns always 1
	 */
	@Override
	public int getColumCount() throws SQLException {
		return 1;
	}

	/**
	 * returns the current fetch size. the default value is 100
	 */
	@Override
	public int getFetchSize() throws SQLException {
		return 100;
	}

	@Override
	public List<String> getSignature() throws SQLException {
		int i = getColumCount();
		Vector<String> signature = new Vector<String>();
		for(int j=1;j<=i;j++){
			signature.add(set.getMetaData().getColumnLabel(j));
		}
		return signature;
	}

	/**
	 * Note: the boolean result set has only 1 row
	 */
	@Override
	public boolean nextRow() throws SQLException {
		if(counter > 0){
			return false;
		}else{
			counter++;
			return true;
		}
	}

}
