package it.unibz.inf.ontop.dbschema.impl;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Map;
import java.util.function.Function;

public class ImmutableDBMetadataImpl implements ImmutableDBMetadata {

    private final ImmutableMap<RelationID, RelationDefinition> map;
    private final ImmutableList<RelationDefinition> relations;

    private final DBParameters dbParameters;


    public ImmutableDBMetadataImpl(DBParameters dbParameters, ImmutableList<RelationDefinition> relations) {
        this.dbParameters = dbParameters;
        this.relations = relations;
        // TODO: this needs FIXING
        this.map = relations.stream()
                .collect(ImmutableCollectors.toMultimap(RelationDefinition::getID, Function.identity())).asMap().entrySet().stream()
        .collect(ImmutableCollectors.toMap(e -> e.getKey(), e -> e.getValue().iterator().next()));
    }

    @Override
    public RelationDefinition getRelation(RelationID id) {
        return map.get(id);
    }

    @JsonProperty("relations")
    @Override
    public ImmutableList<RelationDefinition> getDatabaseRelations() {
        return relations;
    }

    @Override
    public String toString() {
        StringBuilder bf = new StringBuilder();
        for (Map.Entry<RelationID, RelationDefinition> e : map.entrySet()) {
            bf.append(e.getKey()).append("=").append(e.getValue()).append("\n");
        }
        // Prints all primary keys
        bf.append("\n====== constraints ==========\n");
        for (Map.Entry<RelationID, RelationDefinition> e : map.entrySet()) {
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
}
