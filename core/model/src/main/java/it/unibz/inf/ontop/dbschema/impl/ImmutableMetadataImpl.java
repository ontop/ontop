package it.unibz.inf.ontop.dbschema.impl;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.exception.MetadataExtractionException;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Map;
import java.util.function.Function;

public class ImmutableMetadataImpl implements MetadataProvider {

    private final DBParameters dbParameters;

    private final ImmutableMap<RelationID, RelationDefinition> map;
    private final ImmutableList<RelationDefinition> relations;

    public ImmutableMetadataImpl(DBParameters dbParameters, ImmutableMap<RelationID, RelationDefinition> map) {
        this.dbParameters = dbParameters;
        this.map = map;
        this.relations = map.values().stream()
                .collect(ImmutableCollectors.toMultimap(RelationDefinition::getID, Function.identity())).asMap().values().stream()
                .map(s -> s.iterator().next())
                .collect(ImmutableCollectors.toList());
    }


    @Override
    public RelationDefinition getRelation(RelationID id) throws MetadataExtractionException {
        RelationDefinition relation = map.get(id);
        if (relation == null)
            throw new MetadataExtractionException("Relation " + id + " not found");

        return relation;
    }

    @JsonProperty("relations")
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

    @JsonIgnore
    @Override
    public ImmutableList<RelationID> getRelationIDs()  {
        return relations.stream().map(RelationDefinition::getID).collect(ImmutableCollectors.toList());
    }

    @Override
    public void insertIntegrityConstraints(RelationDefinition relation, MetadataLookup metadataLookup) {
        throw new IllegalStateException("immutable metadata");
    }

}
