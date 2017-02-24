package it.unibz.inf.ontop.owlrefplatform.core;

import it.unibz.inf.ontop.exception.OntopConnectionException;

/**
 * High-level component in charge of abstracting the interaction with the DB.
 * This interface is agnostic regarding the native query language.
 *
 * Guice-enabled interface (see the QuestComponentFactory).
 *
 */
public interface DBConnector {

    boolean connect() throws OntopConnectionException;

    /**
     * TODO: remove it
     */
    @Deprecated
    void disconnect() throws OntopConnectionException;

    void close() throws OntopConnectionException;

    /**
     * Gets a direct OntopConnection.
     */
    OntopConnection getNonPoolConnection() throws OntopConnectionException;

    /**
     * Gets a OntopConnection usually coming from a connection pool.
     */
    OntopConnection getConnection() throws OntopConnectionException;

}
