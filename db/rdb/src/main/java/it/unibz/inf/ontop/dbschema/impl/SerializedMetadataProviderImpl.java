package it.unibz.inf.ontop.dbschema.impl;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.type.MapType;
import com.google.common.collect.ImmutableList;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.dbschema.impl.JSONRelation.JSONRelation;
import it.unibz.inf.ontop.exception.MetadataExtractionException;
import it.unibz.inf.ontop.model.type.TypeFactory;

import java.io.IOException;
import java.io.Reader;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SerializedMetadataProviderImpl implements SerializedMetadataProvider {//DBMetadataProvider

    private final Reader dbMetadataReader; //
    private final QuotedIDFactory quotedIDFactory;
    private final MetadataProvider provider;
    private final TypeFactory typeFactory;
    private final Map<RelationID, DatabaseRelationDefinition> map;

    @AssistedInject
    protected SerializedMetadataProviderImpl(@Assisted Reader dbMetadataReader,
                                             @Assisted QuotedIDFactory quotedIDFactory,
                                             @Assisted MetadataProvider provider,
                                             TypeFactory typeFactory) throws MetadataExtractionException, IOException {
        this.dbMetadataReader = dbMetadataReader;
        this.quotedIDFactory = quotedIDFactory;
        this.provider = provider;
        this.typeFactory = typeFactory;
        map = loadAndDeserialize(this.dbMetadataReader);
    }

    //@Override
    public Map<RelationID, DatabaseRelationDefinition> loadAndDeserialize(Reader dbMetadataReader) throws MetadataExtractionException, IOException {

        try {
            SimpleModule simpleModule = new SimpleModule().addKeyDeserializer(RelationID.class, new RelationIDKeyDeserializer());
            ObjectMapper objectMapper = new ObjectMapper()
                // Handle non-string or int key
                .registerModule(simpleModule)
                // Accept arrays with single value in JSON
                .configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true)
                // Accept empty arrays in JSON
                .configure(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT, true);

            objectMapper.configOverride(DatabaseRelationDefinition.class)
                .setVisibility((JsonAutoDetect.Value.defaultVisibility())
                .withFieldVisibility(JsonAutoDetect.Visibility.NON_PRIVATE));

            MapType mapType = objectMapper.getTypeFactory().constructMapType(HashMap.class, RelationID.class, DatabaseRelationDefinition.class);
            HashMap<RelationID, DatabaseRelationDefinition> map = objectMapper.readValue(dbMetadataReader, mapType);

            return map;

        }
        catch (JsonProcessingException e) {
            throw new MetadataExtractionException("problem with JSON processing.\n" + e);
        }
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

    public MetadataProvider getProvider() { return provider; }

    public Reader getDbMetadataReader() { return  dbMetadataReader; }

    public Map<RelationID, DatabaseRelationDefinition> getMap() {
        return map;
    }

    public TypeFactory getTypeFactory() {
        return typeFactory;
    }
    //public ImmutableMetadata loadRelations() { return null; }
}
