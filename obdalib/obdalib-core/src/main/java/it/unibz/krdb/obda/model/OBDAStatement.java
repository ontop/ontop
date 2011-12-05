package it.unibz.krdb.obda.model;

public interface OBDAStatement {

	public void addBatch(String query) throws Exception;

	public void cancel() throws Exception;

	public void clearBatch() throws Exception;

	public void close() throws Exception;

	public OBDAResultSet executeQuery(String query) throws Exception;

	public int executeUpdate(String query) throws Exception;

	public int getFetchSize() throws Exception;

	public int getMaxRows() throws Exception;

	/***
	 * To implement
	 * 
	 * @throws Exception
	 */
	public void getMoreResults() throws Exception;

	public void setFetchSize(int rows) throws Exception;

	public void setMaxRows(int max) throws Exception;

	/***
	 * Sets the number of seconds the driver will wait for a Statement object to
	 * execute to the given number of seconds. If the limit is exceeded, an
	 * SQLException is thrown.
	 * 
	 * @param seconds
	 *            the new query timeout limit in seconds; zero means no limit.
	 * @throws Exception
	 */
	public void setQueryTimeout(int seconds) throws Exception;

	/*
	 * These should be removed and moved some where else
	 */

	public String getUnfolding(String query) throws Exception;

	public String getUnfolding(String query, boolean noreformulation) throws Exception;

	public String getRewriting(String query) throws Exception;

	public int getTupleCount(String query) throws Exception;

}
