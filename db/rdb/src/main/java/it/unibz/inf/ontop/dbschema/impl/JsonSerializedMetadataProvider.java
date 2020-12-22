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
import it.unibz.inf.ontop.model.type.DBTypeFactory;
import it.unibz.inf.ontop.model.type.TypeFactory;

import it.unibz.inf.ontop.dbschema.impl.json.*;

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
        relationMap = extractRelationDefinitions(jsonMetadata, quotedIDFactory, typeFactory);
        dbParameters = extractDBParameters(jsonMetadata, quotedIDFactory, typeFactory);
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

    /**
     * Extract relation definitions with the exception of FKs
     */
    protected static ImmutableMap<RelationID, DatabaseTableDefinition> extractRelationDefinitions(JsonMetadata jsonMetadata,
                                                                                                     QuotedIDFactory quotedIDFactory,
                                                                                                     TypeFactory typeFactory) throws MetadataExtractionException {
        ImmutableMap.Builder<RelationID, DatabaseTableDefinition> builder = ImmutableMap.builder();

        for (JsonDatabaseTable r : jsonMetadata.relations) {
            DatabaseTableDefinition relation = extractRelationDefinition(r, quotedIDFactory, typeFactory);
            builder.put(relation.getID(), relation);
        }
        ImmutableMap<RelationID, DatabaseTableDefinition> relationMap = builder.build();

        for (JsonDatabaseTable r : jsonMetadata.relations) {
            DatabaseTableDefinition relation = relationMap.get(quotedIDFactory.createRelationID(r.name));
            try {
                insertUniqueConstraints(r, relation, quotedIDFactory);
                insertForeignKeys(r, relationMap, quotedIDFactory);
            }
            catch (AttributeNotFoundException e) {
                throw new MetadataExtractionException(e);
            }
        }
        return relationMap;
    }


    /**
     * For each relation add individual attributes
     */
    protected static DatabaseTableDefinition extractRelationDefinition(JsonDatabaseTable parsedRelation, QuotedIDFactory quotedIDFactory, TypeFactory typeFactory) {

        DBTypeFactory dbTypeFactory = typeFactory.getDBTypeFactory();
        RelationDefinition.AttributeListBuilder attributeListBuilder = AbstractRelationDefinition.attributeListBuilder();
        for (JsonDatabaseTable.Column attribute: parsedRelation.columns)
            attributeListBuilder.addAttribute(
                    quotedIDFactory.createAttributeID(attribute.name),
                    dbTypeFactory.getDBTermType(attribute.datatype),
                    attribute.isNullable);

        RelationID id = quotedIDFactory.createRelationID(parsedRelation.name);
        return new DatabaseTableDefinition(ImmutableList.of(id), attributeListBuilder);
    }


    /**
     * For each relation add unique constraints
     */
    protected static void insertUniqueConstraints(JsonDatabaseTable parsedRelation, DatabaseTableDefinition relationDefinition, QuotedIDFactory quotedIDFactory) throws AttributeNotFoundException {
        for (JsonUniqueConstraint uc: parsedRelation.uniqueConstraints) {
            FunctionalDependency.Builder builder = uc.isPrimaryKey
                ? UniqueConstraint.primaryKeyBuilder(relationDefinition, uc.name)
                : UniqueConstraint.builder(relationDefinition, uc.name);
            for (String column : uc.determinants) {
                builder.addDeterminant(quotedIDFactory.createAttributeID(column));
            }
            builder.build();
        }
    }

    /**
     * Insert the FKs
     */
    private static void insertForeignKeys(JsonDatabaseTable relation, ImmutableMap<RelationID, DatabaseTableDefinition> relationMap, QuotedIDFactory quotedIDFactory) throws MetadataExtractionException, AttributeNotFoundException {
        for (JsonForeignKey fk : relation.foreignKeys) {
            ForeignKeyConstraint.Builder builder = ForeignKeyConstraint.builder(fk.name,
                    relationMap.get(quotedIDFactory.createRelationID(fk.from.relation)),
                    relationMap.get(quotedIDFactory.createRelationID(fk.to.relation)));

            for (int i = 0; i < fk.from.columns.size(); i++) {
                builder.add(quotedIDFactory.createAttributeID(fk.from.columns.get(i)),
                        quotedIDFactory.createAttributeID(fk.to.columns.get(i)));
            }
            builder.build();
        }
    }

    private static DBParameters extractDBParameters(JsonMetadata jsonRelation, QuotedIDFactory quotedIDFactory, TypeFactory typeFactory) {
        return new BasicDBParametersImpl(jsonRelation.metadata.driverName,
                jsonRelation.metadata.driverVersion,
                jsonRelation.metadata.dbmsProductName,
                jsonRelation.metadata.dbmsVersion,
                quotedIDFactory,
                typeFactory.getDBTypeFactory());
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
