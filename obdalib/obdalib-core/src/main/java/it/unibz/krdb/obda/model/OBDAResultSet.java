package it.unibz.krdb.obda.model;

import java.net.URI;
import java.sql.SQLException;
import java.util.List;

public interface OBDAResultSet {

	public int getColumCount() throws SQLException;

	public List<String> getSignature() throws SQLException;

	public boolean nextRow() throws SQLException;

	public String getAsString(int column) throws SQLException;

	public int getAsInteger(int column) throws SQLException;

	public double getAsDouble(int column) throws SQLException;

	public Object getAsObject(int column) throws SQLException;

	public URI getAsURI(int column) throws SQLException;

	public int getFetchSize() throws SQLException;

	public void close() throws SQLException;
	
	public OBDAStatement getStatement();

}
