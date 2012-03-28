package it.unibz.krdb.obda.owlrefplatform.core.resultset;

import it.unibz.krdb.obda.model.BNode;
import it.unibz.krdb.obda.model.Constant;
import it.unibz.krdb.obda.model.OBDAException;
import it.unibz.krdb.obda.model.OBDAResultSet;
import it.unibz.krdb.obda.model.OBDAStatement;
import it.unibz.krdb.obda.model.ValueConstant;

import java.net.URI;
import java.util.List;

public class EmptyQueryResultSet implements OBDAResultSet {

	List<String> head = null;
	private OBDAStatement st;

	public EmptyQueryResultSet(List<String> headvariables, OBDAStatement st) {
		this.head = headvariables;
		this.st = st;
	}

	@Override
	public void close() throws OBDAException {
	}

	@Override
	public double getDouble(int column) throws OBDAException {
		return 0;
	}

	@Override
	public int getInt(int column) throws OBDAException {
		return 0;
	}

	@Override
	public Object getObject(int column) throws OBDAException {
		return null;
	}

	@Override
	public String getString(int column) throws OBDAException {
		return null;
	}

	@Override
	public URI getURI(int column) throws OBDAException {
		return null;
	}

	@Override
	public int getColumCount() throws OBDAException {
		return head.size();
	}

	@Override
	public int getFetchSize() throws OBDAException {
		return 0;
	}

	@Override
	public List<String> getSignature() throws OBDAException {
		return head;
	}

	@Override
	public boolean nextRow() throws OBDAException {
		return false;
	}

	@Override
	public OBDAStatement getStatement() {
		return st;
	}

	@Override
	public Constant getConstant(int column) throws OBDAException {
		return null;
	}

	@Override
	public ValueConstant getLiteral(int column) throws OBDAException {
		return null;
	}

	@Override
	public BNode getBNode(int column) throws OBDAException {
		return null;
	}

	@Override
	public Constant getConstant(String name) throws OBDAException {
		return null;
	}

	@Override
	public URI getURI(String name) throws OBDAException {
		return null;
	}

	@Override
	public ValueConstant getLiteral(String name) throws OBDAException {
		return null;
	}

	@Override
	public BNode getBNode(String name) throws OBDAException {
		return null;
	}

}
