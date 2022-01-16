package it.unibz.inf.ontop.dbschema.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.google.common.collect.Maps;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.exception.InvalidQueryException;
import it.unibz.inf.ontop.exception.MetadataExtractionException;
import it.unibz.inf.ontop.injection.CoreSingletons;

import it.unibz.inf.ontop.dbschema.impl.json.*;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.Reader;
import java.util.List;
import java.util.Optional;

public class JsonSerializedMetadataProvider implements SerializedMetadataProvider {

    private final DBParameters dbParameters;
    private final ImmutableMap<RelationID, JsonDatabaseTable> relationMap;
    private final ImmutableMap<RelationID, RelationID> otherNames;

    // Lazy
    @Nullable
    private MetadataLookup parentProvider;
    @Nullable
    private final MetadataLookupSupplier parentProviderSupplier;


    @AssistedInject
    protected JsonSerializedMetadataProvider(@Assisted Reader dbMetadataReader,
                                             CoreSingletons coreSingletons) throws MetadataExtractionException, IOException {
        this(dbMetadataReader, null, coreSingletons);
    }

    @AssistedInject
    protected JsonSerializedMetadataProvider(@Assisted Reader dbMetadataReader,
                                             @Nullable @Assisted MetadataLookupSupplier parentProviderSupplier,
                                             CoreSingletons coreSingletons) throws MetadataExtractionException, IOException {
        JsonMetadata jsonMetadata = loadAndDeserialize(dbMetadataReader);

        QuotedIDFactory idFactory = jsonMetadata.metadata.createQuotedIDFactory();

        dbParameters = new BasicDBParametersImpl(jsonMetadata.metadata.driverName,
                jsonMetadata.metadata.driverVersion,
                jsonMetadata.metadata.dbmsProductName,
                jsonMetadata.metadata.dbmsVersion,
                idFactory,
                coreSingletons);

        relationMap = jsonMetadata.relations.stream()
                .collect(ImmutableCollectors.toMap(t -> JsonMetadata.deserializeRelationID(idFactory, t.name), t -> t));

        otherNames = jsonMetadata.relations.stream()
                .flatMap(t -> {
                    RelationID mainId = JsonMetadata.deserializeRelationID(idFactory, t.name);
                    return t.otherNames.stream()
                            .map(n -> JsonMetadata.deserializeRelationID(idFactory, n))
                            .filter(n -> !n.equals(mainId))
                            .map(id -> Maps.immutableEntry(id, mainId));
                })
                .collect(ImmutableCollectors.toMap());

        this.parentProviderSupplier = parentProviderSupplier;
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
        JsonDatabaseTable jsonTable = relationMap.get(id);
        if (jsonTable == null) {
            RelationID mainId = otherNames.get(id);

            if (mainId == null)
                throw new IllegalArgumentException("The relation " + id.getSQLRendering()
                        + " is unknown to the JsonSerializedMetadataProvider");

            return getRelation(mainId);
        }

        return jsonTable.createDatabaseTableDefinition(dbParameters);
    }

    @Override
    public RelationDefinition getBlackBoxView(String query) throws MetadataExtractionException, InvalidQueryException {
        Optional<MetadataLookup> optionalParentProvider = getParentProvider();

        if (optionalParentProvider.isPresent())
            return optionalParentProvider.get().getBlackBoxView(query);

        throw new UnsupportedOperationException("Has no parent provider. Should not have been called");
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
    public void insertIntegrityConstraints(NamedRelationDefinition relation, MetadataLookup metadataLookupForFk) throws MetadataExtractionException {
        JsonDatabaseTable jsonTable = relationMap.get(relation.getID());
        if (jsonTable == null)
            throw new IllegalArgumentException("The relation " + relation.getID().getSQLRendering()
                    + " is unknown to the JsonSerializedMetadataProvider");

        jsonTable.insertIntegrityConstraints(relation, metadataLookupForFk);
    }

    @Override
    public DBParameters getDBParameters() {
        return dbParameters;
    }

    @Override
    public void normalizeAndOptimizeRelations(List<NamedRelationDefinition> relationDefinitions) {
        // Does nothing
    }

    protected synchronized Optional<MetadataLookup> getParentProvider() throws MetadataExtractionException {
        if (parentProvider == null && parentProviderSupplier != null)
            parentProvider = parentProviderSupplier.get();

        return Optional.ofNullable(parentProvider);
    }
}
