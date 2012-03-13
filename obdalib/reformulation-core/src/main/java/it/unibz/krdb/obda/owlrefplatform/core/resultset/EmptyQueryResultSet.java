package it.unibz.krdb.obda.owlrefplatform.core.resultset;

import it.unibz.krdb.obda.model.BNode;
import it.unibz.krdb.obda.model.Constant;
import it.unibz.krdb.obda.model.OBDAResultSet;
import it.unibz.krdb.obda.model.OBDAStatement;
import it.unibz.krdb.obda.model.ValueConstant;

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
	public double getDouble(int column) throws SQLException {
		return 0;
	}

	@Override
	public int getInt(int column) throws SQLException {
		return 0;
	}

	@Override
	public Object getObject(int column) throws SQLException {
		return null;
	}

	@Override
	public String getString(int column) throws SQLException {
		return null;
	}

	@Override
	public URI getURI(int column) throws SQLException {
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

	@Override
	public Constant getConstant(int column) throws SQLException {
		return null;
	}

	@Override
	public ValueConstant getLiteral(int column) throws SQLException {
		return null;
	}

	@Override
	public BNode getBNode(int column) throws SQLException {
		return null;
	}

	@Override
	public Constant getConstant(String name) throws SQLException {
		return null;
	}

	@Override
	public URI getURI(String name) throws SQLException {
		return null;
	}

	@Override
	public ValueConstant getLiteral(String name) throws SQLException {
		return null;
	}

	@Override
	public BNode getBNode(String name) throws SQLException {
		return null;
	}

}
