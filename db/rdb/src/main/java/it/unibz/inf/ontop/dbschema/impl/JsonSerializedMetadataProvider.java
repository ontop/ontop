package it.unibz.inf.ontop.dbschema.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
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

    private final DBParameters dbParameters;
    private final ImmutableMap<RelationID, JsonDatabaseTable> relationMap;


    @AssistedInject
    protected JsonSerializedMetadataProvider(@Assisted Reader dbMetadataReader,
                                             TypeFactory typeFactory) throws MetadataExtractionException, IOException {
        JsonMetadata jsonMetadata = loadAndDeserialize(dbMetadataReader);

        QuotedIDFactory idFactory = jsonMetadata.metadata.createQuotedIDFactory();

        dbParameters = new BasicDBParametersImpl(jsonMetadata.metadata.driverName,
                jsonMetadata.metadata.driverVersion,
                jsonMetadata.metadata.dbmsProductName,
                jsonMetadata.metadata.dbmsVersion,
                idFactory,
                typeFactory.getDBTypeFactory());

        // TODO: add to all
        relationMap = jsonMetadata.relations.stream()
                .collect(ImmutableCollectors.toMap(t -> JsonMetadata.deserializeRelationID(idFactory, t.name), t -> t));
    }


    /**
     * Deserializes a JSON file into a POJO.
     * @param dbMetadataReader JSON file reader
     * @return JSON metadata
     */
    protected static JsonMetadata loadAndDeserialize(Reader dbMetadataReader) throws MetadataExtractionException, IOException {

        try {
            ObjectMapper objectMapper = new ObjectMapper()
                    .registerModule(new GuavaModule())
                    .enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
                    .enable(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT);

            // Create POJO object from JSON
            return objectMapper.readValue(dbMetadataReader, JsonMetadata.class);
        }
        catch (JsonProcessingException e) {
            throw new MetadataExtractionException("problem with JSON processing.\n" + e);
        }
    }

    @Override
    public NamedRelationDefinition getRelation(RelationID id) throws MetadataExtractionException {
        return relationMap.get(id).createDatabaseTableDefinition(dbParameters);
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
    public void insertIntegrityConstraints(NamedRelationDefinition relation, MetadataLookup metadataLookup) throws MetadataExtractionException {
        relationMap.get(relation.getID()).insertIntegrityConstraints(metadataLookup);
    }

    @Override
    public DBParameters getDBParameters() {
        return dbParameters;
    }
}
