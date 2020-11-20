package it.unibz.inf.ontop.dbschema.impl;

import com.fasterxml.jackson.annotation.*;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import it.unibz.inf.ontop.dbschema.*;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ImmutableMetadataImpl implements ImmutableMetadata {
    private final DBParameters dbParameters;
    private final ImmutableList<DatabaseRelationDefinition> relations;
    private final File viewFile;

    ImmutableMetadataImpl(DBParameters dbParameters, ImmutableList<DatabaseRelationDefinition> relations) {
        this.dbParameters = dbParameters;
        this.relations = relations;
        viewFile = null;
    }

    ImmutableMetadataImpl(File viewFile) {
        this.viewFile = viewFile;
        dbParameters = null;
        relations = null;
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

    @JsonIgnore
    @Override
    public File getFile() {
        return viewFile;
    }

    @SuppressWarnings("unused")
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

//    @JsonProperty("relationss")
//    private List<Relations> relations2;

    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

//    @JsonProperty("relations")
//    public void setRelations(ImmutableList<DatabaseRelationDefinition> relations) {
//        this.relations = relations;
//    }

//    @JsonProperty("relationss")
//    public List<Relations> getRelations() {
//        return relations2;
//    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonPropertyOrder({ "uniqueConstraints", "otherFunctionalDependencies", "foreignKeys", "columns", "name" })
    private class Relations {

            @JsonProperty("uniqueConstraints")
            private List<UniqueConstraints> uniqueConstraints;

            @JsonProperty("otherFunctionalDependencies")
            private List<String> otherFunctionalDependencies;

            @JsonProperty("foreignKeys")
            private List<ForeignKeys> foreignKeys;

            @JsonProperty("columns")
            private List<Columns> columns;

            @JsonProperty("name")
            private String name;

            @JsonIgnore
            private Map<String, Object> additionalProperties = new HashMap<String, Object>();

            @JsonProperty("uniqueConstraints")
            public List<UniqueConstraints> getUniqueConstraints() {
                return uniqueConstraints;
            }

            @JsonProperty("uniqueConstraints")
            public void setUniqueConstraints(List<UniqueConstraints> uniqueConstraints) {
                this.uniqueConstraints = uniqueConstraints;
            }

            @JsonProperty("otherFunctionalDependencies")
            public List<String> getOtherFunctionalDependencies() {
                return otherFunctionalDependencies;
            }

            @JsonProperty("otherFunctionalDependencies")
            public void setOtherFunctionalDependencies(List<String> otherFunctionalDependencies) {
                this.otherFunctionalDependencies = otherFunctionalDependencies;
            }

            @JsonProperty("foreignKeys")
            public List<ForeignKeys> getForeignKeys() {
                return foreignKeys;
            }

            @JsonProperty("foreignKeys")
            public void setForeignKeys(List<ForeignKeys> foreignKeys) {
                this.foreignKeys = foreignKeys;
            }

            @JsonProperty("columns")
            public List<Columns> getColumns() {
                return columns;
            }

            @JsonProperty("columns")
            public void setColumns(List<Columns> columns) {
                this.columns = columns;
            }

            @JsonProperty("name")
            public String getName() {
                return name;
            }

            @JsonProperty("name")
            public void setName(String name) {
                this.name = name;
            }

            @JsonAnySetter
            public void setAdditionalProperty(String name, Object value) {
                this.additionalProperties.put(name, value);
        }

        @JsonInclude(JsonInclude.Include.NON_NULL)
        @JsonPropertyOrder({ "name", "determinants", "isPrimaryKey" })
        private class UniqueConstraints {

                @JsonProperty("name")
                private String name;

                @JsonProperty("determinants")
                private List<String> determinants;

                @JsonProperty("isPrimaryKey")
                private boolean isPrimaryKey;

    /*@JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();*/

                @JsonProperty("name")
                public void setName(String name) {
                    this.name = name;
                }

                @JsonProperty("determinants")
                public void setDeterminants(List<String> determinants) {
                    this.determinants = determinants;
                }

                @JsonProperty("isPrimaryKey")
                public boolean getIsPrimaryKey() {
                    return isPrimaryKey;
                }

                @JsonProperty("isPrimaryKey")
                public void setIsPrimaryKey(boolean isPrimaryKey) {
                    this.isPrimaryKey = isPrimaryKey;
                }

    /*@JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }*/
        }

        @JsonInclude(JsonInclude.Include.NON_NULL)
        @JsonPropertyOrder({ "name", "from", "to" })
        private class ForeignKeys {

                @JsonProperty("name")
                private String name;
                @JsonProperty("from")
                private List<FromToObject> from;
                @JsonProperty("to")
                private List<FromToObject> to;

    /*@JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();*/

                @JsonProperty("name")
                public String getName() {
                    return name;
                }

                @JsonProperty("name")
                public void setName(String name) {
                    this.name = name;
                }

                @JsonProperty("from")
                public List<FromToObject> getFrom() { return from; }

                @JsonProperty("from")
                public void setFrom(List<FromToObject> from) { this.from = from; }

                @JsonProperty("to")
                public List<FromToObject> getTo() {
                    return to;
                }

                @JsonProperty("to")
                public void setTo(List<FromToObject> to) {
                    this.to = to;
                }

            @JsonInclude(JsonInclude.Include.NON_NULL)
            @JsonPropertyOrder({ "relation", "columns" })
            private class FromToObject {

                    @JsonProperty("relation")
                    private String relation;
                    @JsonProperty("columns")
                    private List<String> columns;

    /*@JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();*/

                    @JsonProperty("relation")
                    public String getRelation() { return relation; }

                    @JsonProperty("relation")
                    public void setRelation(String relation) {
                        this.relation = relation;
                    }

                    @JsonProperty("columns")
                    public List<String> getColumns() {
                        return columns;
                    }

                    @JsonProperty("columns")
                    public void setColumns(List<String> columns) {
                        this.columns = columns;
                    }

    /*@JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }*/
            }

    /*@JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }*/
        }

        @JsonInclude(JsonInclude.Include.NON_NULL)
        @JsonPropertyOrder({ "name", "isNullable", "datatype" })
        private class Columns {

            @JsonProperty("name")
            private String name;

            @JsonProperty("isNullable")
            private boolean isNullable;

            @JsonProperty("datatype")
            private String datatype;

    /*@JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();*/

            @JsonProperty("name")
            public void setName(String name) {
                this.name = name;
            }

            @JsonProperty("isNullable")
            public void setIsNullable(boolean isNullable) {
                this.isNullable = isNullable;
            }

            @JsonProperty("datatype")
            public void setDatatype(String datatype) {
                this.datatype = datatype;
            }

    /*@JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
        }*/
        }
    }
}
