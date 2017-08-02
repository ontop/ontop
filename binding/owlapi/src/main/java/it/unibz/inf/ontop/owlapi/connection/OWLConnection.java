package it.unibz.inf.ontop.owlapi.connection;

import org.semanticweb.owlapi.model.OWLException;


/***
 * Handler for a connection. Note that as with JDBC, executing queries in
 * parallels over a single connection is inefficient, since JDBC drivers will
 * serialize each query execution and you get a bottle neck (even if using
 * multiple {@link OWLStatement}) . Having explicit OWLConnections
 * allows to initialize a single OWLReasoner and make multiple queries in
 * parallel with good performance (as with JDBC).
 *
 * <p>
 * Besides parallel connections, at the moment, the class is not very useful,
 * it will be when transactions and updates are implemented. Or when we allow
 * the user to setup the kind of statement that he wants to use.
 *
 *
 * @author Mariano Rodriguez Muro <mariano.muro@gmail.com>
 *
 * @see OWLStatement
 */
public interface OWLConnection extends AutoCloseable {

    OWLStatement createStatement() throws OWLException;

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
