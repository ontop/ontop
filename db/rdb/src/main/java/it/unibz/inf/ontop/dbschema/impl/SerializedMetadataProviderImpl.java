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
import it.unibz.inf.ontop.dbschema.impl.JSONRelation.Column;
import it.unibz.inf.ontop.dbschema.impl.JSONRelation.ForeignKey;
import it.unibz.inf.ontop.dbschema.impl.JSONRelation.JSONRelation;
import it.unibz.inf.ontop.dbschema.impl.JSONRelation.Relation;
import it.unibz.inf.ontop.exception.MetadataExtractionException;
import it.unibz.inf.ontop.model.type.DBTypeFactory;
import it.unibz.inf.ontop.model.type.TypeFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.io.IOException;
import java.io.Reader;
import java.util.*;

public class SerializedMetadataProviderImpl implements SerializedMetadataProvider {

    private final QuotedIDFactory quotedIDFactory;
    private final Map<RelationID, DatabaseRelationDefinition> relationMap;

    @AssistedInject
    protected SerializedMetadataProviderImpl(@Assisted Reader dbMetadataReader,
                                             @Assisted QuotedIDFactory quotedIDFactory,
                                             TypeFactory typeFactory) throws MetadataExtractionException, IOException {
        this.quotedIDFactory = quotedIDFactory;
        relationMap = extractRelationDefinitions(loadAndDeserialize(dbMetadataReader), quotedIDFactory, typeFactory);
    }

    /**
     * Deserializes a JSON file into a POJO.
     * @param dbMetadataReader JSON file reader
     * @return JSON metadata
     */
    protected static JSONRelation loadAndDeserialize(Reader dbMetadataReader) throws MetadataExtractionException, IOException {

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
            JSONRelation JSONMetadata = objectMapper.readValue(dbMetadataReader, JSONRelation.class);

            return JSONMetadata;

        } catch (JsonProcessingException e) {
            throw new MetadataExtractionException("problem with JSON processing.\n" + e);
        }
    }

    /**
     * Extract relation definitions with the exception of FKs
     */
    protected static ImmutableMap<RelationID, DatabaseRelationDefinition> extractRelationDefinitions(JSONRelation jsonMetadata,
                                                                                                     QuotedIDFactory quotedIDFactory,
                                                                                                     TypeFactory typeFactory) throws MetadataExtractionException {

        ImmutableMap<RelationID, DatabaseRelationDefinition> relationMap = jsonMetadata.getRelations().stream()
            .map(r -> extractRelationDefinition(r, quotedIDFactory, typeFactory))
            .collect(ImmutableCollectors.toMap(
                DatabaseRelationDefinition::getID,
                d -> d
                ));

        insertForeignKeys(jsonMetadata, relationMap, quotedIDFactory);
        return relationMap;
    }


    /**
     * For each relation add individual attributes
     */
    protected static DatabaseRelationDefinition extractRelationDefinition(Relation parsedRelation, QuotedIDFactory quotedIDFactory, TypeFactory typeFactory) {

        // Initialize database relation builder object
        OfflineMetadataProviderBuilder builder = new OfflineMetadataProviderBuilder(typeFactory);
        DBTypeFactory dbTypeFactory = typeFactory.getDBTypeFactory();

        // Initialize attribute builder object
        RelationDefinition.AttributeListBuilder attributeListBuilder = AbstractRelationDefinition.attributeListBuilder();

        // Add all attributes
        for (Column attributeName: parsedRelation.getColumns()) {
            attributeListBuilder.addAttribute(
                quotedIDFactory.createAttributeID(attributeName.getName()),
                dbTypeFactory.getDBTermType(attributeName.getDatatype()),
                attributeName.getIsNullable());
        }

        // Create "key" i.e. Relation ID
        RelationID id = quotedIDFactory.createRelationID(parsedRelation.getName());

        DatabaseRelationDefinition relationDefinition = builder.createDatabaseRelation(ImmutableList.of(id), attributeListBuilder);

        insertUniqueConstraints(parsedRelation, relationDefinition, quotedIDFactory);

        return relationDefinition;
    }


    /**
     * For each relation add unique constraints i.e. primary keys
     */
    protected static void insertUniqueConstraints(Relation parsedRelation, DatabaseRelationDefinition relationDefinition, QuotedIDFactory quotedIDFactory) {

        // Add primary key
        RelationID id = quotedIDFactory.createRelationID(parsedRelation.getName());

        for (it.unibz.inf.ontop.dbschema.impl.JSONRelation.UniqueConstraint uc: parsedRelation.getUniqueConstraints()) {

            if (uc.getIsPrimaryKey()) {
                UniqueConstraint.primaryKeyBuilder(relationDefinition, uc.getName());
            }

            else {
                UniqueConstraint.builder(relationDefinition, uc.getName());
            }
        }
    }

    /**
     * Insert the FKs
     */
    private static void insertForeignKeys(JSONRelation jsonMetadata, ImmutableMap<RelationID, DatabaseRelationDefinition> relationMap, QuotedIDFactory quotedIDFactory) throws MetadataExtractionException {


        for (Relation relation : jsonMetadata.getRelations()) {
            for (ForeignKey fk : relation.getForeignKeys()) {
                ForeignKeyConstraint.Builder builder = ForeignKeyConstraint.builder(fk.getName(),
                    relationMap.get(quotedIDFactory.createRelationID(fk.getFrom().getRelation())),
                    relationMap.get(quotedIDFactory.createRelationID(fk.getTo().getRelation())));
                for (int i = 0; i < fk.getFrom().getColumns().size(); i++) {

                    try {
                        builder.add(quotedIDFactory.createAttributeID(fk.getFrom().getColumns().get(i)),
                            quotedIDFactory.createAttributeID(fk.getTo().getColumns().get(i)));
                    } catch (AttributeNotFoundException e) {
                        throw new MetadataExtractionException(e);
                    }
                }
                builder.build();
            }
        }
    }


    @Override
    public DatabaseRelationDefinition getRelation(RelationID id) throws MetadataExtractionException {
        return relationMap.get(id);
    }

    @Override
    public QuotedIDFactory getQuotedIDFactory() {
        return quotedIDFactory;
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
        throw new RuntimeException("To be implemented");
    }

}
