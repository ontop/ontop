package it.unibz.inf.ontop.dbschema.impl;

import com.google.common.collect.ImmutableList;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.exception.MetadataExtractionException;
import it.unibz.inf.ontop.model.type.TypeFactory;

import java.io.Reader;

public class SerializedMetadataProviderImpl implements DBMetadataProvider {

    private final Reader dbMetadataReader; //
    private final QuotedIDFactory quotedIDFactory;
    private final TypeFactory typeFactory;

    @AssistedInject
    protected SerializedMetadataProviderImpl(@Assisted Reader dbMetadataReader,
                                             @Assisted QuotedIDFactory quotedIDFactory,
                                             TypeFactory typeFactory) {
        this.dbMetadataReader = dbMetadataReader;
        this.quotedIDFactory = quotedIDFactory;
        this.typeFactory = typeFactory;
        // add the arguments with the methods at the beginning
    }

    @Override
    public DatabaseRelationDefinition getRelation(RelationID id) throws MetadataExtractionException {
        return null;
    }

    @Override
    public QuotedIDFactory getQuotedIDFactory() {
        return quotedIDFactory;
    }

    @Override
    public ImmutableList<RelationID> getRelationIDs() throws MetadataExtractionException {
        return null;
    }

    @Override
    public void insertIntegrityConstraints(DatabaseRelationDefinition relation, MetadataLookup metadataLookup) throws MetadataExtractionException {

    }

    @Override
    public DBParameters getDBParameters() {
        return null;
    }
}
