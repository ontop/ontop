package it.unibz.inf.ontop.owlrefplatform.core;

import it.unibz.inf.ontop.model.*;

/**
 * High-level component in charge of abstracting the interaction with the DB.
 * This interface is agnostic regarding the native query language.
 *
 * Guice-enabled interface (see the QuestComponentFactory).
 *
 */
@Deprecated
public interface DBConnector {

    /**
     * TODO: keep them public?
     */
    boolean connect() throws OBDAException;
    void disconnect() throws OBDAException;
    void dispose();


    void close();

    /**
     * Gets a direct QuestConnection.
     */
    IQuestConnection getNonPoolConnection() throws OBDAException;

    /**
     * Gets a QuestConnection usually coming from a connection pool.
     */
    IQuestConnection getConnection() throws OBDAException;

}
