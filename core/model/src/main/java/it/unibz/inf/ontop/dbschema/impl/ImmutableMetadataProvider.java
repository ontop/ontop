package it.unibz.inf.ontop.dbschema.impl;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Map;
import java.util.function.Function;

public class ImmutableMetadataProvider extends ImmutableMetadataLookup implements MetadataProvider {

    private final DBParameters dbParameters;
    private final ImmutableList<DatabaseRelationDefinition> relations;

    public ImmutableMetadataProvider(DBParameters dbParameters, ImmutableMap<RelationID, DatabaseRelationDefinition> map) {
        super(map, dbParameters.getQuotedIDFactory());
        this.dbParameters = dbParameters;
        // the list contains no repetitions (based on full relation ids)
        this.relations = map.values().stream()
                .collect(ImmutableCollectors.toMultimap(DatabaseRelationDefinition::getID, Function.identity())).asMap().values().stream()
                .map(s -> s.iterator().next())
                .collect(ImmutableCollectors.toList());
    }


    @JsonProperty("relations")
    public ImmutableList<DatabaseRelationDefinition> getAllRelations() {
        return relations;
    }

    @Override
    public String toString() {
        StringBuilder bf = new StringBuilder();
        for (Map.Entry<RelationID, DatabaseRelationDefinition> e : map.entrySet()) {
            bf.append(e.getKey()).append("=").append(e.getValue()).append("\n");
        }
        // Prints all primary keys
        bf.append("\n====== constraints ==========\n");
        for (Map.Entry<RelationID, DatabaseRelationDefinition> e : map.entrySet()) {
            for (UniqueConstraint uc : e.getValue().getUniqueConstraints())
                bf.append(uc).append(";\n");
            bf.append("\n");
            for (ForeignKeyConstraint fk : e.getValue().getForeignKeys())
                bf.append(fk).append(";\n");
            bf.append("\n");
        }
        return bf.toString();
    }

    @JsonIgnore
    @Override
    public DBParameters getDBParameters() {
        return dbParameters;
    }

    @JsonProperty("metadata")
    Map<String, String> getMedadataForJsonExport() {
        return ImmutableMap.of(
                "dbmsProductName", getDBParameters().getDbmsProductName(),
                "dbmsVersion", getDBParameters().getDbmsVersion(),
                "driverName", getDBParameters().getDriverName(),
                "driverVersion", getDBParameters().getDriverVersion(),
                "quotationString", getDBParameters().getQuotedIDFactory().getIDQuotationString()
        );
    }

    @JsonIgnore
    @Override
    public ImmutableList<RelationID> getRelationIDs()  {
        return relations.stream().map(DatabaseRelationDefinition::getID).collect(ImmutableCollectors.toList());
    }

    @Override
    public void insertIntegrityConstraints(DatabaseRelationDefinition relation, MetadataLookup metadataLookup) {
        throw new IllegalStateException("immutable metadata");
    }

}
