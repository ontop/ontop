package it.unibz.inf.ontop.dbschema.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.exception.MetadataExtractionException;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.function.Function;

public class ImmutableMetadataLookup implements MetadataLookup {

    protected final QuotedIDFactory idFactory;
    protected final ImmutableMap<RelationID, DatabaseRelationDefinition> map;
    protected final String filepath;

    ImmutableMetadataLookup(QuotedIDFactory idFactory, ImmutableMap<RelationID, DatabaseRelationDefinition> map) {
        this.idFactory = idFactory;
        this.map = map;
        filepath=null;
    }

    ImmutableMetadataLookup(String filepath) {
        idFactory = null;
        map = null;
        this.filepath=filepath;
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

    protected List<ImmutableMetadata> loadRelations() throws MetadataExtractionException, IOException {
        try {
            File viewsFile = new File(filepath);

            ObjectMapper mapper = new ObjectMapper();
            //List<ImmutableMetadata> metadata = mapper.readValue(viewsFile, ImmutableMetadata.class);
            List<ImmutableMetadata> metadata = mapper.readValue(viewsFile,mapper.getTypeFactory().constructCollectionType(List.class, ImmutableMetadata.class));
            return metadata;
        }
        catch (
            JsonProcessingException e) {
            throw new MetadataExtractionException("problem with JSON processing.\n" + e);
    }
    }

}
