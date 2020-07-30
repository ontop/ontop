package it.unibz.inf.ontop.dbschema.impl;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import it.unibz.inf.ontop.dbschema.*;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
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
    Map<String, String> getMetadataForJsonExport() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        String extractionTime = dateFormat.format(Calendar.getInstance().getTime());

        return ImmutableMap.<String, String>builder()
                .put("dbmsProductName", getDBParameters().getDbmsProductName())
                .put("dbmsVersion", getDBParameters().getDbmsVersion())
                .put("driverName", getDBParameters().getDriverName())
                .put("driverVersion", getDBParameters().getDriverVersion())
                .put("quotationString", getDBParameters().getQuotedIDFactory().getIDQuotationString())
                .put("extractionTime", extractionTime)
                .build();
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
