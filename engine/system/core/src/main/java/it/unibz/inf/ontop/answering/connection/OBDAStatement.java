package it.unibz.inf.ontop.answering.connection;

import it.unibz.inf.ontop.answering.reformulation.input.InputQuery;
import it.unibz.inf.ontop.answering.resultset.OBDAResultSet;
import it.unibz.inf.ontop.exception.*;

public interface OBDAStatement extends AutoCloseable {

	void cancel() throws OntopConnectionException;

	@Override
    void close() throws OntopConnectionException;

	<R extends OBDAResultSet> R execute(InputQuery<R> inputQuery) throws OntopReformulationException, OntopQueryEvaluationException,
	OntopConnectionException, OntopResultConversionException;

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
