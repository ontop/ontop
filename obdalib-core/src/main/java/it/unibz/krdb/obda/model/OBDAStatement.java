package it.unibz.krdb.obda.model;

/*
 * #%L
 * ontop-obdalib-core
 * %%
 * Copyright (C) 2009 - 2014 Free University of Bozen-Bolzano
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

public interface OBDAStatement {

	public void cancel() throws OBDAException;

	public void close() throws OBDAException;

	public ResultSet execute(String query) throws OBDAException;

	public int executeUpdate(String query) throws OBDAException;

	public int getFetchSize() throws OBDAException;

	public int getMaxRows() throws OBDAException;

	public void getMoreResults() throws OBDAException;

	public ResultSet getResultSet() throws OBDAException;

	public int getQueryTimeout() throws OBDAException;

	public void setFetchSize(int rows) throws OBDAException;

	public void setMaxRows(int max) throws OBDAException;

	public boolean isClosed() throws OBDAException;

	/**
	 * Sets the number of seconds the driver will wait for a Statement object to
	 * execute to the given number of seconds. If the limit is exceeded, an
	 * SQLException is thrown.
	 * 
	 * @param seconds
	 *            the new query timeout limit in seconds; zero means no limit.
	 * @throws Exception
	 */
	public void setQueryTimeout(int seconds) throws Exception;
	
	/**
	 * Produces an expanded SPARQL string given the initial <code>query</code> input.
	 * The expansion utilizes Quest rewriter over an ontology.
	 * 
	 * @param query
	 * 			The initial SPARQL query string.
	 * @return SPARQL query expansion.
	 * @throws OBDAException
	 */
	public String getSPARQLRewriting(String query) throws OBDAException;
}
