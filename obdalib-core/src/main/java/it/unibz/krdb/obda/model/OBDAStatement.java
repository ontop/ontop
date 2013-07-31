/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package it.unibz.krdb.obda.model;

public interface OBDAStatement {

	public void cancel() throws OBDAException;

	public void close() throws OBDAException;

	public ResultSet execute(String query) throws OBDAException;

	public int executeUpdate(String query) throws OBDAException;

	public OBDAConnection getConnection() throws OBDAException;

	public int getFetchSize() throws OBDAException;

	public int getMaxRows() throws OBDAException;

	public void getMoreResults() throws OBDAException;

	public ResultSet getResultSet() throws OBDAException;

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
