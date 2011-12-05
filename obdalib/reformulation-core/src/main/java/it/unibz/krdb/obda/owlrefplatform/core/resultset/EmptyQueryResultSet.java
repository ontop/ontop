package it.unibz.krdb.obda.owlrefplatform.core.resultset;

import it.unibz.krdb.obda.model.OBDAResultSet;
import it.unibz.krdb.obda.model.OBDAStatement;

import java.net.URI;
import java.sql.SQLException;
import java.util.List;

public class EmptyQueryResultSet implements OBDAResultSet {

	List<String> head = null;
	private OBDAStatement st;
	
	public EmptyQueryResultSet(List<String> headvariables, OBDAStatement st) {
		this.head = headvariables;
		this.st = st;
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

	@Override
	public OBDAStatement getStatement() {
		return st;
	}

}
