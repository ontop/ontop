package it.unibz.inf.ontop.owlrefplatform.owlapi;

import it.unibz.inf.ontop.owlapi.OntopOWLException;
import it.unibz.inf.ontop.owlrefplatform.core.ExecutableQuery;
import org.semanticweb.owlapi.model.OWLAxiom;

import java.util.List;

/***
 * A Statement to execute queries over a OntopOWLConnection. The logic of this
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
 *
 */
public interface OntopOWLStatement extends AutoCloseable {
	void cancel() throws OntopOWLException;

	void close() throws OntopOWLException;

	QuestOWLResultSet executeSelectQuery(String query) throws OntopOWLException;
	QuestOWLResultSet executeAskQuery(String query) throws OntopOWLException;
	QuestOWLResultSet executeTuple(String query) throws OntopOWLException;

	List<OWLAxiom> executeConstructQuery(String query) throws OntopOWLException;
	List<OWLAxiom> executeDescribeQuery(String query) throws OntopOWLException;
	List<OWLAxiom> executeGraph(String query) throws OntopOWLException;

	/**
	 * TODO: remove it
	 */
	@Deprecated
	OntopOWLConnection getConnection() throws OntopOWLException;

	boolean isClosed() throws OntopOWLException;

	void setQueryTimeout(int seconds) throws OntopOWLException;

	long getTupleCount(String query) throws OntopOWLException;

	String getRewritingRendering(String query) throws OntopOWLException;

	ExecutableQuery getExecutableQuery(String query) throws OntopOWLException;

}
