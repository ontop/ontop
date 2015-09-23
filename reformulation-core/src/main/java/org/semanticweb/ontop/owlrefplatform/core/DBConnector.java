package org.semanticweb.ontop.owlrefplatform.core;

import org.semanticweb.ontop.model.OBDAException;
import org.semanticweb.ontop.model.OBDAModel;
import org.semanticweb.ontop.nativeql.DBMetadataException;
import org.semanticweb.ontop.owlrefplatform.core.basicoperations.LinearInclusionDependencies;
import org.semanticweb.ontop.model.DataSourceMetadata;
import org.semanticweb.ontop.sql.ImplicitDBConstraints;

import javax.annotation.Nullable;
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
     * Extracts the schema from the DB and/or the mappings.
     */
    DataSourceMetadata extractDBMetadata(OBDAModel obdaModel, @Nullable ImplicitDBConstraints userConstraints)
            throws DBMetadataException;

    /**
     * Gets a direct QuestConnection.
     */
    IQuestConnection getNonPoolConnection() throws OBDAException;

    /**
     * Gets a QuestConnection usually coming from a connection pool.
     */
    IQuestConnection getConnection() throws OBDAException;

    LinearInclusionDependencies generateFKRules(DataSourceMetadata metadata);

    /**
     * TODO: explain
     */
    OBDAModel normalizeMappings(OBDAModel obdaModel, URI sourceId, DataSourceMetadata metadata) throws OBDAException;
}
