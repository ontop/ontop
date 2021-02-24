package it.unibz.inf.ontop.owlapi.connection;

import it.unibz.inf.ontop.owlapi.resultset.BooleanOWLResultSet;
import it.unibz.inf.ontop.owlapi.resultset.GraphOWLResultSet;
import it.unibz.inf.ontop.owlapi.resultset.TupleOWLResultSet;
import it.unibz.inf.ontop.owlapi.connection.impl.DefaultOntopOWLConnection;
import org.semanticweb.owlapi.model.OWLException;

/***
 * A Statement to execute queries over a OntopOWLConnection.
 *
 * RESTRICTION: By contrast with JDBC statements, an OWLStatement accepts AT MOST ONE query execution.
 * It cannot be reused.
 *
 * <p>
 * <strong>Performance</strong> Note that you should not create multiple
 * statements over the same connection to execute parallel queries (see
 * {@link DefaultOntopOWLConnection}). Multiple statements over the same connection are
 * not going to be very useful until we support updates (then statements will
 * allow to implement transactions in the same way as JDBC Statements).
 *
 * Initial @author Mariano Rodriguez Muro <mariano.muro@gmail.com>
 *
 * Used by the OWLAPI.
 *
 *
 */
public interface OWLStatement extends AutoCloseable {

	void cancel() throws OWLException;

	void close() throws OWLException;

	TupleOWLResultSet executeSelectQuery(String query) throws OWLException;
	BooleanOWLResultSet executeAskQuery(String query) throws OWLException;

	GraphOWLResultSet executeConstructQuery(String query) throws OWLException;
	GraphOWLResultSet executeDescribeQuery(String query) throws OWLException;
	GraphOWLResultSet executeGraphQuery(String query) throws OWLException;

	boolean isClosed() throws OWLException;

	void setQueryTimeout(int seconds) throws OWLException;

	long getTupleCount(String query) throws OWLException;

}
