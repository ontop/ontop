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
import it.unibz.inf.ontop.dbschema.impl.JSONRelation.JSONRelation;
import it.unibz.inf.ontop.dbschema.impl.JSONRelation.Relation;
import it.unibz.inf.ontop.exception.MetadataExtractionException;
import it.unibz.inf.ontop.model.type.TypeFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.io.IOException;
import java.io.Reader;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
        map = extractRelationDefinitions(loadAndDeserialize(this.dbMetadataReader));
    }

    /**
     * Deserializes a JSON file into a POJO.
     * @param dbMetadataReader JSON file reader
     * @return JSON metadata
     */
    protected JSONRelation loadAndDeserialize(Reader dbMetadataReader) throws MetadataExtractionException, IOException {

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
    protected ImmutableMap<RelationID, DatabaseRelationDefinition> extractRelationDefinitions(JSONRelation JSONMetadata) {

        // Initialize map of relation id-s and DatabaseRelationDefinition
        List<DatabaseRelationDefinition> myTables = new ArrayList<DatabaseRelationDefinition>();
        Map<RelationID, DatabaseRelationDefinition> map1 = new HashMap<>();

        // Retrieve metadata relations
        // Create local variable which stores relation object
        List<Relation> allRelations = JSONMetadata.getRelations();

        // Return immutable list of relation IDs
        ImmutableList<RelationID> myRelationIDs = allRelations.stream()
            .map(x -> quotedIDFactory.createRelationID(x.getName()))
            .collect(ImmutableCollectors.toList());

        // Initialize metadata builder object
        RelationDefinition.AttributeListBuilder attributeListBuilder = AbstractRelationDefinition.attributeListBuilder();
        OfflineMetadataProviderBuilder builder = new OfflineMetadataProviderBuilder(typeFactory);

        // Return immutable map of relation id-s and database relationd definitions
        ImmutableMap<RelationID, DatabaseRelationDefinition> relationMap = myRelationIDs.stream()
            .collect(ImmutableCollectors.toMap(x -> x, x -> builder.createDatabaseRelation(myRelationIDs, attributeListBuilder)));

        // Unique Constraints - Name
        // flag only one PK!!! --> Not currently done
        relationMap.entrySet().stream()
            .map(x -> UniqueConstraint.primaryKeyBuilder(x.getValue(), x.getValue().getUniqueConstraints().get(0).getName()))
            .collect(ImmutableCollectors.toList());

        // Columns --> Add Attributes to respective relation ID, needs to be changed
        ImmutableList<RelationDefinition.AttributeListBuilder> columnAttributes = allRelations.stream()
            .map(x -> x.getColumns())
            .flatMap(Collection::stream)
            .map(x -> attributeListBuilder.addAttribute(
                new QuotedIDImpl(x.getName(), quotedIDFactory.getIDQuotationString()),
                typeFactory.getDBTypeFactory().getDBTermType(x.getDatatype()),
                x.getIsNullable()))
            .collect(ImmutableCollectors.toList());

        return relationMap;

    }


    /**
     * Insert the FKs
     */
    protected void insertForeignKeys(ImmutableMap<RelationID, DatabaseRelationDefinition> relationMap, JSONRelation JSONMetadata) {

        // Create local variable which stores relations
        List<Relation> allRelations = JSONMetadata.getRelations();

        // Add foreign keys
        allRelations.stream()
            .map(x -> x.getForeignKeys())
            .flatMap(Collection::stream)
            /*.map(x -> new Object() {
                ForeignKeyConstraint.Builder fk01 = ForeignKeyConstraint.builder("", relationMap.get(x.getFrom().getRelation()), relationMap.get(x.getTo().getRelation())) ;
                List<String> fk02 = x.getFrom().getColumns().stream().flatMap(Collection::stream);
                Stream<List<String>> fk03 = Stream.of(x.getTo().getColumns()); })
            .map(x -> x.fk01.add(quotedIDFactory.createAttributeID(x.fk02), quotedIDFactory.createAttributeID(x.fk03.flatMap(Collection::stream))))*/
            .map(x -> ForeignKeyConstraint.builder("", relationMap.get(x.getFrom().getRelation()), relationMap.get(x.getTo().getRelation())))
            //.map(builder.add(quotedIDFactory.createAttributeID(x.getFrom().getColumns().get(0)), quotedIDFactory.createAttributeID(x.getTo().getColumns().get(0))))
                .collect(ImmutableCollectors.toList());

        List<QuotedID> source = allRelations.stream()
            .map(x -> x.getForeignKeys())
            .flatMap(Collection::stream)
            .map(x -> x.getFrom().getColumns())
            .flatMap(Collection::stream)
            .map(x -> quotedIDFactory.createAttributeID(x))
            .collect(ImmutableCollectors.toList());

        List<QuotedID> destination = allRelations.stream()
            .map(x -> x.getForeignKeys())
            .flatMap(Collection::stream)
            .map(x -> x.getTo().getColumns())
            .flatMap(Collection::stream)
            .map(x -> quotedIDFactory.createAttributeID(x))
            .collect(ImmutableCollectors.toList());


        //for (int k = 0; k <= source.size()-1; k++) {
        //for (ForeignKey fk: fkList)
            //builderfk.add(source.get(k), destination.get(k));


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
}
