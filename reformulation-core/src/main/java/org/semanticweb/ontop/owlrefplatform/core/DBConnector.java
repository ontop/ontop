package org.semanticweb.ontop.owlrefplatform.core;

import com.google.common.collect.ImmutableList;
import org.semanticweb.ontop.model.CQIE;
import org.semanticweb.ontop.model.OBDAException;
import org.semanticweb.ontop.model.OBDAModel;
import org.semanticweb.ontop.nativeql.DBMetadataException;
import org.semanticweb.ontop.owlrefplatform.core.basicoperations.LinearInclusionDependencies;
import org.semanticweb.ontop.model.DataSourceMetadata;
import org.semanticweb.ontop.sql.ImplicitDBConstraints;

import javax.annotation.Nullable;
import java.net.URI;
import java.util.List;

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

    DBMetadataAndMappings extractDBMetadataAndMappings(OBDAModel obdaModel, URI sourceId,
                                                       @Nullable ImplicitDBConstraints userConstraints)
            throws DBMetadataException, OBDAException;

    ImmutableList<CQIE> extractMappings(OBDAModel obdaModel, URI sourceId, DataSourceMetadata metadata) throws OBDAException;
}
