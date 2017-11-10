package it.unibz.inf.ontop.answering.connection;

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

import it.unibz.inf.ontop.answering.reformulation.input.InputQuery;
import it.unibz.inf.ontop.answering.resultset.OBDAResultSet;
import it.unibz.inf.ontop.exception.*;

public interface OBDAStatement extends AutoCloseable {

	void cancel() throws OntopConnectionException;

	@Override
    void close() throws OntopConnectionException;

//	InputQuery parseInputQuery(String inputQueryString) throws OntopInvalidInputQueryException;

	<R extends OBDAResultSet> R execute(InputQuery<R> inputQuery) throws OntopReformulationException, OntopQueryEvaluationException,
	OntopConnectionException, OntopResultConversionException;
//
//	default OBDAResultSet execute(String inputQueryString) throws OntopReformulationException, OntopQueryEvaluationException,
//			OntopInvalidInputQueryException, OntopConnectionException, OntopResultConversionException {
//		return execute(parseInputQuery(inputQueryString));
//	}

	int getFetchSize() throws OntopConnectionException;

	int getMaxRows() throws OntopConnectionException;

	void getMoreResults() throws OntopConnectionException;

	int getQueryTimeout() throws OntopConnectionException;

	void setFetchSize(int rows) throws OntopConnectionException;

	void setMaxRows(int max) throws OntopConnectionException;

	boolean isClosed() throws OntopConnectionException;

	/**
	 * Sets the number of seconds the driver will wait for a Statement object to
	 * execute to the given number of seconds. If the limit is exceeded, an
	 * exception is thrown.
	 * 
	 * @param seconds
	 *            the new query timeout limit in seconds; zero means no limit.
	 */
    void setQueryTimeout(int seconds) throws OntopConnectionException;
}
