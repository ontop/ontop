package it.unibz.inf.ontop.dbschema.impl;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Functions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public class ImmutableDBMetadataImpl implements ImmutableDBMetadata {

    private final ImmutableMap<RelationID, RelationDefinition> map;
    private final ImmutableList<RelationDefinition> relations;

    private final DBParameters dbParameters;

    public ImmutableDBMetadataImpl(DBParameters dbParameters, ImmutableList<RelationDefinition> relations) {
        this.dbParameters = dbParameters;
        this.relations = relations;
        this.map = relations.stream()
                .collect(ImmutableCollectors.toMap(RelationDefinition::getID, Function.identity()));
    }

    public ImmutableDBMetadataImpl(DBParameters dbParameters, ImmutableMap<RelationID, RelationDefinition> map) {
        this.dbParameters = dbParameters;
        this.map = map;
        this.relations = map.values().stream()
                .map(r -> Maps.immutableEntry(r.getID(), r))
                .collect(ImmutableCollectors.toMultimap()).asMap().values().stream()
                .map(r -> r.iterator().next())
                .collect(ImmutableCollectors.toList());
    }


    @Override
    public Optional<RelationDefinition> getRelation(RelationID id) {
        return Optional.ofNullable(map.get(id));
    }

    @JsonProperty("relations")
    @Override
    public ImmutableList<RelationDefinition> getAllRelations() {
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
