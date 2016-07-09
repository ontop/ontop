package it.unibz.inf.ontop.owlrefplatform.core;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;
import it.unibz.inf.ontop.model.*;
import it.unibz.inf.ontop.nativeql.DBMetadataException;
import it.unibz.inf.ontop.owlrefplatform.core.basicoperations.LinearInclusionDependencies;

import java.net.URI;
import java.util.Collection;
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

    DataSourceMetadata extractDBMetadata(OBDAModel obdaModel) throws DBMetadataException;

    DataSourceMetadata extractDBMetadata(OBDAModel obdaModel, DataSourceMetadata partiallyDefinedMetadata)
            throws DBMetadataException;

    Multimap<Predicate, List<Integer>> extractUniqueConstraints(DataSourceMetadata metadata);

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


    Collection<OBDAMappingAxiom> applyDBSpecificNormalization(Collection<OBDAMappingAxiom> mappingAxioms,
                                                              DataSourceMetadata metadata) throws OBDAException;

    void completePredefinedMetadata(DataSourceMetadata metadata);

}
