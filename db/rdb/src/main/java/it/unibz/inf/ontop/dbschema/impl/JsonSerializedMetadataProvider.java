package it.unibz.inf.ontop.dbschema.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.exception.MetadataExtractionException;
import it.unibz.inf.ontop.model.type.TypeFactory;

import it.unibz.inf.ontop.dbschema.impl.json.*;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.io.IOException;
import java.io.Reader;

public class JsonSerializedMetadataProvider implements SerializedMetadataProvider {

    private final ImmutableMap<RelationID, DatabaseTableDefinition> relationMap;
    private final DBParameters dbParameters;

    @AssistedInject
    protected JsonSerializedMetadataProvider(@Assisted Reader dbMetadataReader,
                                             @Assisted QuotedIDFactory quotedIDFactory,
                                             TypeFactory typeFactory) throws MetadataExtractionException, IOException {
        JsonMetadata jsonMetadata = loadAndDeserialize(dbMetadataReader);

        dbParameters = new BasicDBParametersImpl(jsonMetadata.metadata.driverName,
                jsonMetadata.metadata.driverVersion,
                jsonMetadata.metadata.dbmsProductName,
                jsonMetadata.metadata.dbmsVersion,
                quotedIDFactory,
                typeFactory.getDBTypeFactory());

        relationMap = jsonMetadata.relations.stream()
                .map(r -> r.createDatabaseTableDefinition(dbParameters))
                .collect(ImmutableCollectors.toMap(AbstractDatabaseRelationDefinition::getID, r -> r));

        for (JsonDatabaseTable r : jsonMetadata.relations)
            r.insertIntegrityConstraints(this);
    }


    /**
     * Deserializes a JSON file into a POJO.
     * @param dbMetadataReader JSON file reader
     * @return JSON metadata
     */
    protected static JsonMetadata loadAndDeserialize(Reader dbMetadataReader) throws MetadataExtractionException, IOException {

        try {
            SimpleModule simpleModule = new SimpleModule().addKeyDeserializer(RelationID.class, new RelationIDKeyDeserializer());
            ObjectMapper objectMapper = new ObjectMapper()
                    .registerModule(new GuavaModule())
                    // Handle non-string or int key
                    .registerModule(simpleModule)
                    // Accept arrays with single value in JSON
                    .configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true)
                    // Accept empty arrays in JSON
                    .configure(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT, true);

            // Create POJO object from JSON
            return objectMapper.readValue(dbMetadataReader, JsonMetadata.class);
        }
        catch (JsonProcessingException e) {
            throw new MetadataExtractionException("problem with JSON processing.\n" + e);
        }
    }


    @Override
    public DatabaseRelationDefinition getRelation(RelationID id) throws MetadataExtractionException {
        return relationMap.get(id);
    }

    @Override
    public QuotedIDFactory getQuotedIDFactory() {
        return dbParameters.getQuotedIDFactory();
    }

    @Override
    public ImmutableList<RelationID> getRelationIDs() throws MetadataExtractionException {
        return ImmutableList.copyOf(relationMap.keySet());
    }

    @Override
    public void insertIntegrityConstraints(DatabaseRelationDefinition relation, MetadataLookup metadataLookup) throws MetadataExtractionException {
        throw new RuntimeException("To be implemented ?");
    }

    @Override
    public DBParameters getDBParameters() {
        return dbParameters;
    }
}
