package it.unibz.inf.ontop.owlrefplatform.core;

import it.unibz.inf.ontop.model.*;
import it.unibz.inf.ontop.nativeql.DBMetadataException;

import java.util.Collection;

/**
 * High-level component in charge of abstracting the interaction with the DB.
 * This interface is agnostic regarding the native query language.
 *
 * Guice-enabled interface (see the QuestComponentFactory).
 *
 */
public interface DBConnector {

    /**
     * TODO: keep them public?
     */
    boolean connect() throws OBDAException;
    void disconnect() throws OBDAException;
    void dispose();

    DBMetadata extractDBMetadata(OBDAModel obdaModel) throws DBMetadataException;

    DBMetadata extractDBMetadata(OBDAModel obdaModel, DBMetadata partiallyDefinedMetadata)
            throws DBMetadataException;


    void close();

    /**
     * Gets a direct QuestConnection.
     */
    IQuestConnection getNonPoolConnection() throws OBDAException;

    /**
     * Gets a QuestConnection usually coming from a connection pool.
     */
    IQuestConnection getConnection() throws OBDAException;

    Collection<OBDAMappingAxiom> applyDBSpecificNormalization(Collection<OBDAMappingAxiom> mappingAxioms,
                                                              DBMetadata metadata) throws OBDAException;

    void completePredefinedMetadata(DBMetadata metadata);

}
