package it.unibz.krdb.obda.model;

import java.net.URI;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.List;

public interface OBDAResultSet {

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// ResultSet management functions
	//
	// //////////////////////////////////////////////////////////////////////////////////////

	public int getColumCount() throws SQLException;

	public List<String> getSignature() throws SQLException;

	public int getFetchSize() throws SQLException;

	public void close() throws SQLException;

	public OBDAStatement getStatement();

	public boolean nextRow() throws SQLException;

	// ////////////////////////////////////////////////////////////////////////////////////
	//
	// Main data fetching functions
	//
	// //////////////////////////////////////////////////////////////////////////////////////

	public Constant getConstant(int column) throws SQLException, URISyntaxException;

	public URI getURI(int column) throws SQLException, URISyntaxException;

	public ValueConstant getLiteral(int column) throws SQLException;

	public BNode getBNode(int column) throws SQLException;

	public Constant getConstant(String name) throws SQLException, URISyntaxException;

	public URI getURI(String name) throws SQLException;

	public ValueConstant getLiteral(String name) throws SQLException;

	public BNode getBNode(String name) throws SQLException;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Convinience data fetching functions
	//
	// //////////////////////////////////////////////////////////////////////////////////////

	public String getString(int column) throws SQLException;

	public int getInt(int column) throws SQLException;

	public double getDouble(int column) throws SQLException;

	public Object getObject(int column) throws SQLException;

}
