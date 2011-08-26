package it.unibz.krdb.obda.model;

public interface OBDAStatement {

	public String getUnfolding(String query) throws Exception;
	
	public String getUnfolding(String query, boolean noreformulation) throws Exception;

	public String getRewriting(String query) throws Exception;

	public OBDAResultSet executeQuery(String query) throws Exception;

	public int getTupleCount(String query) throws Exception;

	public void close() throws Exception;

}
