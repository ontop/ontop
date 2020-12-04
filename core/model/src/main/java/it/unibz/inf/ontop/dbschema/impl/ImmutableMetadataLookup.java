package it.unibz.inf.ontop.dbschema.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.exception.MetadataExtractionException;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import javax.xml.crypto.Data;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.function.Function;
import com.fasterxml.jackson.datatype.guava.GuavaModule;

public class ImmutableMetadataLookup implements MetadataLookup {

    protected final QuotedIDFactory idFactory;
    protected final ImmutableMap<RelationID, DatabaseRelationDefinition> map;
    protected final File dbMetadataFile;

    ImmutableMetadataLookup(QuotedIDFactory idFactory, ImmutableMap<RelationID, DatabaseRelationDefinition> map) {
        this.idFactory = idFactory;
        this.map = map;
        dbMetadataFile=null;
    }

    ImmutableMetadataLookup(File dbMetadataFile) {
        idFactory = null;
        map = null;
        this.dbMetadataFile=dbMetadataFile;
    }

    @Override
    public DatabaseRelationDefinition getRelation(RelationID id) throws MetadataExtractionException {
        DatabaseRelationDefinition relation = map.get(id);
        if (relation == null)
            throw new MetadataExtractionException("Relation " + id + " not found");

        return relation;
    }

    @Override
    public QuotedIDFactory getQuotedIDFactory() {
        return idFactory;
    }


    protected ImmutableList<DatabaseRelationDefinition> getRelations() {
        // the list contains no repetitions (based on full relation ids)
        return map.values().stream()
                .collect(ImmutableCollectors.toMultimap(DatabaseRelationDefinition::getAllIDs, Function.identity())).asMap().values().stream()
                .map(s -> s.iterator().next())
                .collect(ImmutableCollectors.toList());
    }

    protected ImmutableList<DatabaseRelationDefinition> loadRelations() throws MetadataExtractionException, IOException {

        try {
            /*ObjectMapper mapper = new ObjectMapper();
            List<ImmutableMetadata> metadata = mapper.readValue(dbMetadataFile,mapper.getTypeFactory().constructCollectionType(List.class, ImmutableMetadata.class));
            return metadata;*/
            ObjectMapper objectMapper = new ObjectMapper()
                .registerModule(new GuavaModule())
                .configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true)
                .configure(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT, true);
            CollectionType javatype = objectMapper.getTypeFactory().constructCollectionType(ImmutableList.class,ImmutableMetadataImpl.class);
            ImmutableList<DatabaseRelationDefinition> metadata = objectMapper.readValue(dbMetadataFile, javatype);
            return metadata;
        }
        catch (
            JsonProcessingException e) {
            throw new MetadataExtractionException("problem with JSON processing.\n" + e);
    }
    }

}
