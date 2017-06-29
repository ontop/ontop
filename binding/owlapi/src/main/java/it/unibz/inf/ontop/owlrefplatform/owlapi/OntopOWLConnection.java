package it.unibz.inf.ontop.owlrefplatform.owlapi;

import it.unibz.inf.ontop.owlrefplatform.core.OntopStatement;
import org.semanticweb.owlapi.model.OWLException;


/***
 * Handler for a connection. Note that as with JDBC, executing queries in
 * parallels over a single connection is inefficient, since JDBC drivers will
 * serialize each query execution and you get a bottle neck (even if using
 * multiple {@link OntopOWLStatement}) . Having explicit QuestOWLConnections
 * allows to initialize a single QuestOWLReasoner and make multiple queries in
 * parallel with good performance (as with JDBC).
 *
 * <p>
 * Besides parallel connections, at the moment, the class is not very useful,
 * it will be when transactions and updates are implemented. Or when we allow
 * the user to setup the kind of statement that he wants to use.
 *
 * <p>
 * Internally, this class is mostly an OWLAPI specific wrapper for the API
 * agnostic {@link OntopStatement}.
 *
 * @author Mariano Rodriguez Muro <mariano.muro@gmail.com>
 *
 * @see OntopOWLStatement
 * @see QuestOWL
 * @see OntopStatement
 */
public interface OntopOWLConnection extends AutoCloseable {

    OntopOWLStatement createStatement() throws OWLException;

    void close() throws OWLException;

    boolean isClosed() throws OWLException;

    //---------------------------
    // Transaction (read-only)
    //---------------------------

    void commit() throws OWLException;

    void setAutoCommit(boolean autocommit) throws OWLException;

    boolean getAutoCommit() throws OWLException;

    void rollBack() throws OWLException;
}
