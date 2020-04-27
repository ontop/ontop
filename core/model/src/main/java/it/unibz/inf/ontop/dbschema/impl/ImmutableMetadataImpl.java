package it.unibz.inf.ontop.dbschema.impl;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import it.unibz.inf.ontop.dbschema.*;

import java.util.Map;

public class ImmutableMetadataImpl implements ImmutableMetadata {
    private final DBParameters dbParameters;
    private final ImmutableList<DatabaseRelationDefinition> relations;

    ImmutableMetadataImpl(DBParameters dbParameters, ImmutableList<DatabaseRelationDefinition> relations) {
        this.dbParameters = dbParameters;
        this.relations = relations;
    }

    @JsonProperty("relations")
    @Override
    public ImmutableList<DatabaseRelationDefinition> getAllRelations() {
        return relations;
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

    @Override
    public String toString() {
        StringBuilder bf = new StringBuilder();
        for (DatabaseRelationDefinition r : relations)
            bf.append(r.getAllIDs()).append("=").append(r).append("\n");

        // Prints all primary keys
        bf.append("\n====== constraints ==========\n");
        for (DatabaseRelationDefinition r : relations) {
            for (UniqueConstraint uc : r.getUniqueConstraints())
                bf.append(uc).append(";\n");
            bf.append("\n");
            for (ForeignKeyConstraint fk : r.getForeignKeys())
                bf.append(fk).append(";\n");
            bf.append("\n");
        }
        return bf.toString();
    }
}
