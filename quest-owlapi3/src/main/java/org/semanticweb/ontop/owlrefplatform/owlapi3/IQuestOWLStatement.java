package org.semanticweb.ontop.owlrefplatform.owlapi3;

import org.semanticweb.ontop.owlrefplatform.core.NativeQuery;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLException;

import java.util.List;

/***
 * A Statement to execute queries over a QuestOWLConnection. The logic of this
 * statement is equivalent to that of JDBC's Statements.
 *
 * <p>
 * <strong>Performance</strong> Note that you should not create multiple
 * statements over the same connection to execute parallel queries (see
 * {@link QuestOWLConnection}). Multiple statements over the same connection are
 * not going to be very useful until we support updates (then statements will
 * allow to implement transactions in the same way as JDBC Statements).
 *
 * Initial @author Mariano Rodriguez Muro <mariano.muro@gmail.com>
 *
 * Used by the OWLAPI.
 *
 * TODO: rename it (not now) QuestOWLStatement
 *
 */
public interface IQuestOWLStatement {
	void cancel() throws OWLException;

	void close() throws OWLException;

	QuestOWLResultSet executeTuple(String query) throws OWLException;

	List<OWLAxiom> executeGraph(String query) throws OWLException;

	int executeUpdate(String query) throws OWLException;

	QuestOWLConnection getConnection() throws OWLException;

	int getFetchSize() throws OWLException;

	int getMaxRows() throws OWLException;

	void getMoreResults() throws OWLException;

	QuestOWLResultSet getResultSet() throws OWLException;

	int getQueryTimeout() throws OWLException;

	void setFetchSize(int rows) throws OWLException;

	void setMaxRows(int max) throws OWLException;

	boolean isClosed() throws OWLException;

	void setQueryTimeout(int seconds) throws Exception;

	long getTupleCount(String query) throws OWLException;

	String getRewriting(String query) throws OWLException;

	NativeQuery getUnfolding(String query) throws OWLException;

	// Davide> Benchmarking
	long getUnfoldingTime();

	long getRewritingTime();

	int getUCQSizeAfterUnfolding();

	int getUCQSizeAfterRewriting();
}
