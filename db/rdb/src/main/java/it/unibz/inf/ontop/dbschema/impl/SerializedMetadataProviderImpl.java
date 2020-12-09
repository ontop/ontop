package it.unibz.inf.ontop.dbschema.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.google.common.collect.ImmutableList;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.exception.MetadataExtractionException;
import it.unibz.inf.ontop.model.type.TypeFactory;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.List;

public class SerializedMetadataProviderImpl implements DBMetadataProvider {//DBMetadataProvider

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

    //@Override
    /*public List<ImmutableMetadataImpl> loadanddeserialize(Reader dbMetadataReader) throws MetadataExtractionException, IOException {

        try {
            ObjectMapper objectMapper = new ObjectMapper()
                .registerModule(new GuavaModule())
                .configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true)
                .configure(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT, true);
            CollectionType javatype = objectMapper.getTypeFactory().constructCollectionType(List.class,ImmutableMetadataImpl.class);
            List<ImmutableMetadataImpl> metadata = objectMapper.readValue(dbMetadataReader, javatype);
            return metadata;

        }
        catch (JsonProcessingException e) {
            throw new MetadataExtractionException("problem with JSON processing.\n" + e);
        }
    }*/

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

    public ImmutableMetadata loadRelations() {
        return null;
    }
}
