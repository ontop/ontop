package it.unibz.inf.ontop.answering.connection;

import com.google.common.collect.ImmutableMultimap;
import it.unibz.inf.ontop.answering.reformulation.input.InputQuery;
import it.unibz.inf.ontop.answering.resultset.OBDAResultSet;
import it.unibz.inf.ontop.exception.*;

/**
 * An OBDAStatement can execute at most one high-level InputQuery.
 *
 * By high-level InputQuery, we mean a query issued by the user,
 * not the SELECT and CONSTRUCT queries generated internally for answering a high-level DESCRIBE query.
 *
 * This restriction has been introduced as binding libraries like RDF4J do not use the OBDAStatement in a standard manner.
 * It allows to carefully close the underlying DB statement.
 *
 */
public interface OBDAStatement extends AutoCloseable {

	void cancel() throws OntopConnectionException;

	@Override
    void close() throws OntopConnectionException;

	<R extends OBDAResultSet> R execute(InputQuery<R> inputQuery) throws OntopReformulationException, OntopQueryEvaluationException,
	OntopConnectionException, OntopResultConversionException;

	<R extends OBDAResultSet> R execute(InputQuery<R> inputQuery, ImmutableMultimap<String, String> httpHeaders)
			throws OntopReformulationException, OntopQueryEvaluationException, OntopConnectionException, OntopResultConversionException;

	int getMaxRows() throws OntopConnectionException;

	void getMoreResults() throws OntopConnectionException;

	int getQueryTimeout() throws OntopConnectionException;

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
