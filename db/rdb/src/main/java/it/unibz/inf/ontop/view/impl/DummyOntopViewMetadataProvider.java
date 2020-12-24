package it.unibz.inf.ontop.view.impl;

import com.google.common.collect.ImmutableList;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.exception.MetadataExtractionException;
import it.unibz.inf.ontop.view.OntopViewMetadataProvider;

import java.io.IOException;
import java.io.Reader;

/**
 * Ignores the ontop view reader
 */
public class DummyOntopViewMetadataProvider implements OntopViewMetadataProvider {

    private final MetadataProvider parentMetadataProvider;

    @AssistedInject
    protected DummyOntopViewMetadataProvider(@Assisted MetadataProvider parentMetadataProvider,
                                             @Assisted Reader ontopViewReader) throws MetadataExtractionException {
        this.parentMetadataProvider = parentMetadataProvider;
        // Ignored
        try {
            ontopViewReader.close();
        } catch (IOException e) {
            throw new MetadataExtractionException(e);
        }
    }

    @Override
    public NamedRelationDefinition getRelation(RelationID id) throws MetadataExtractionException {
        return parentMetadataProvider.getRelation(id);
    }

    @Override
    public QuotedIDFactory getQuotedIDFactory() {
        return parentMetadataProvider.getQuotedIDFactory();
    }

    @Override
    public ImmutableList<RelationID> getRelationIDs() throws MetadataExtractionException {
        return parentMetadataProvider.getRelationIDs();
    }

    @Override
    public void insertIntegrityConstraints(NamedRelationDefinition relation, MetadataLookup metadataLookup) throws MetadataExtractionException {
        parentMetadataProvider.insertIntegrityConstraints(relation, metadataLookup);
    }

    @Override
    public DBParameters getDBParameters() {
        return parentMetadataProvider.getDBParameters();
    }
}
