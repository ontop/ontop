package it.unibz.inf.ontop.model;

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

public interface OBDAStatement extends AutoCloseable {

	void cancel() throws OBDAException;

	@Override
    void close() throws OBDAException;

	OBDAResultSet execute(String query) throws OBDAException;

	int executeUpdate(String query) throws OBDAException;

	int getFetchSize() throws OBDAException;

	int getMaxRows() throws OBDAException;

	void getMoreResults() throws OBDAException;

	int getQueryTimeout() throws OBDAException;

	void setFetchSize(int rows) throws OBDAException;

	void setMaxRows(int max) throws OBDAException;

	boolean isClosed() throws OBDAException;

	/**
	 * Sets the number of seconds the driver will wait for a Statement object to
	 * execute to the given number of seconds. If the limit is exceeded, an
	 * SQLException is thrown.
	 * 
	 * @param seconds
	 *            the new query timeout limit in seconds; zero means no limit.
	 * @throws Exception
	 */
    void setQueryTimeout(int seconds) throws OBDAException;

	//int getTupleCount(String query) throws OBDAException;
}
