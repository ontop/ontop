package org.obda.owlrefplatform.core.resultset;

import it.unibz.krdb.obda.model.QueryResultSet;

import java.net.URI;
import java.sql.SQLException;
import java.util.List;

public class EmptyQueryResultSet implements QueryResultSet {

	List<String> head = null;
	
	public EmptyQueryResultSet(List<String> headvariables) {
		this.head = headvariables;
	}
	
	@Override
	public void close() throws SQLException {
	}

	@Override
	public double getAsDouble(int column) throws SQLException {
		return 0;
	}

	@Override
	public int getAsInteger(int column) throws SQLException {
		return 0;
	}

	@Override
	public Object getAsObject(int column) throws SQLException {
		return null;
	}

	@Override
	public String getAsString(int column) throws SQLException {
		return null;
	}

	@Override
	public URI getAsURI(int column) throws SQLException {
		return null;
	}

	@Override
	public int getColumCount() throws SQLException {
		return head.size();
	}

	@Override
	public int getFetchSize() throws SQLException {
		return 0;
	}

	@Override
	public List<String> getSignature() throws SQLException {
		return head;
	}

	@Override
	public boolean nextRow() throws SQLException {
		return false;
	}

}
