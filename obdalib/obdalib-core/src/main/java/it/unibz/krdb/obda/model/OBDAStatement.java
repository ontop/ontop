package it.unibz.krdb.obda.model;

public interface OBDAStatement {

	public void cancel() throws OBDAException;

	public void close() throws OBDAException;

	public OBDAResultSet execute(String query) throws OBDAException;

	public GraphResultSet executeConstruct(String query) throws OBDAException;

	public GraphResultSet executeDescribe(String query) throws OBDAException;

	public int executeUpdate(String query) throws OBDAException;

	public OBDAConnection getConnection() throws OBDAException;

	public int getFetchSize() throws OBDAException;

	public int getMaxRows() throws OBDAException;

	public void getMoreResults() throws OBDAException;

	public OBDAResultSet getResultSet() throws OBDAException;

	public int getQueryTimeout() throws OBDAException;

	public void setFetchSize(int rows) throws OBDAException;

	public void setMaxRows(int max) throws OBDAException;

	public boolean isClosed() throws OBDAException;

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
}
