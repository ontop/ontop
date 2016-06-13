package it.unibz.inf.ontop.owlrefplatform.core;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.CQIE;
import it.unibz.inf.ontop.model.OBDAException;
import it.unibz.inf.ontop.model.OBDAModel;
import it.unibz.inf.ontop.nativeql.DBMetadataException;
import it.unibz.inf.ontop.owlrefplatform.core.basicoperations.LinearInclusionDependencies;
import it.unibz.inf.ontop.model.DataSourceMetadata;

import java.net.URI;

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
    void close();

    /**
     * Gets a direct QuestConnection.
     */
    IQuestConnection getNonPoolConnection() throws OBDAException;

    /**
     * Gets a QuestConnection usually coming from a connection pool.
     */
    IQuestConnection getConnection() throws OBDAException;

    LinearInclusionDependencies generateFKRules(DataSourceMetadata metadata);

    DBMetadataAndMappings extractDBMetadataAndMappings(OBDAModel obdaModel, URI sourceId)
            throws DBMetadataException, OBDAException;

    ImmutableList<CQIE> extractMappings(OBDAModel obdaModel, URI sourceId, DataSourceMetadata metadata) throws OBDAException;

    void completePredefinedMetadata(DataSourceMetadata metadata);
}
