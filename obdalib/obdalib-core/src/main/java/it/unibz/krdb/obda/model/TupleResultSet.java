package it.unibz.krdb.obda.model;

import java.net.URI;
import java.util.List;

import com.hp.hpl.jena.iri.IRI;

public interface TupleResultSet extends ResultSet{

	/*
	 * ResultSet management functions
	 */

	public int getColumCount() throws OBDAException;

	public List<String> getSignature() throws OBDAException;

	public int getFetchSize() throws OBDAException;

	public void close() throws OBDAException;

	public OBDAStatement getStatement();

	public boolean nextRow() throws OBDAException;

	/*
	 * Main data fetching functions
	 */

	public Constant getConstant(int column) throws OBDAException;

	public URI getURI(int column) throws OBDAException;
	
	public IRI getIRI(int column) throws OBDAException;

	public ValueConstant getLiteral(int column) throws OBDAException;

	public BNode getBNode(int column) throws OBDAException;

	public Constant getConstant(String name) throws OBDAException;

	public URI getURI(String name) throws OBDAException;
	
	public IRI getIRI(String name) throws OBDAException;

	public ValueConstant getLiteral(String name) throws OBDAException;

	public BNode getBNode(String name) throws OBDAException;

	/*
	 * Convenient data fetching functions
	 */

	public String getString(int column) throws OBDAException;

	public int getInt(int column) throws OBDAException;

	public double getDouble(int column) throws OBDAException;

	public Object getObject(int column) throws OBDAException;
}
