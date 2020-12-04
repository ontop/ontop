package it.unibz.inf.ontop.dbschema.impl;

import com.fasterxml.jackson.annotation.*;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import it.unibz.inf.ontop.dbschema.*;

import java.io.File;
import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ImmutableMetadataImpl implements ImmutableMetadata {
    private final DBParameters dbParameters;
    private final ImmutableList<DatabaseRelationDefinition> relations;
    private final File dbMetadataFile;

    ImmutableMetadataImpl(DBParameters dbParameters, ImmutableList<DatabaseRelationDefinition> relations) {
        this.dbParameters = dbParameters;
        this.relations = relations;
        dbMetadataFile = null;
    }

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    ImmutableMetadataImpl(@JsonProperty("dbMetadataFile") File dbMetadataFile) {
        this.dbMetadataFile = dbMetadataFile;
        dbParameters = null;
        relations = null;
    }

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
        return dbMetadataFile;
    }

    @SuppressWarnings("unused")
    @JsonProperty("metadata")
    Map<String, String> getMetadataForJsonExport() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        String extractionTime = dateFormat.format(Calendar.getInstance().getTime());
        //Field[] fields = c.getDeclaredFields();

        return ImmutableMap.<String, String>builder()
            .put("dbmsProductName", getDBParameters().getDbmsProductName())
            .put("dbmsVersion", getDBParameters().getDbmsVersion())
            .put("driverName", getDBParameters().getDriverName())
            .put("driverVersion", getDBParameters().getDriverVersion())
            .put("quotationString", getDBParameters().getQuotedIDFactory().getIDQuotationString())
            .put("extractionTime", extractionTime)
            //.put("idFactory1", getDBParameters().getQuotedIDFactory().get)
            .put("idFactory", getDBParameters().getQuotedIDFactory().getClass().toString())
            //.put("dbTypeFactory", getDBParameters().getDBTypeFactory().toString())
            .put("dbTypeFactory", getDBParameters().getDBTypeFactory().getDBStringType().toString())
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

    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    @JsonIgnore
    private Map<String, String> metadata;
    @JsonProperty("metadata")
    private void setMetadata(Map<String, String> metadata) {this.metadata = metadata;}

    @JsonProperty("relations")
    private void unpackNested(List<Map<String,Object>> metadata) {
        for(Map<String,Object> md: metadata) {
            this.uniqueConstraints = (List<Map<String,Object>>) md.get("uniqueConstraints");
            for (Map<String, Object> uc: uniqueConstraints) {
                this.constraintname = (String) uc.get("name");
                this.determinants = (List<String>) uc.get("determinants");
                this.isPrimaryKey = (Boolean) uc.get("isPrimaryKey");
            }
            this.otherFunctionalDependencies = (List<String>) md.get("otherFunctionalDependencies");
            this.foreignKeys = (List<Map<String,Object>>) md.get("foreignKeys");
            for (Map<String, Object> fk: foreignKeys) {
                this.foreignkeyname = (String) fk.get("name");
                this.from = (Map<String,Object>) fk.get("from");
                this.fromrelation = (String) from.get("relation");
                this.fromcolumns = (List<String>) from.get("columns");
                this.to = (Map<String,Object>) fk.get("to");
                this.torelation = (String) to.get("relation");
                this.tocolumns = (List<String>) to.get("columns");
            }
            this.columns = (List<Map<String,Object>>) md.get("columns");
            for (Map<String, Object> cl: columns) {
                this.columnname = (String) cl.get("name");
                this.isNullable = (Boolean) cl.get("isNullable");
                this.datatype = (String) cl.get("datatype");
            }
            this.name = (String) md.get("name");
        }
    }

    /*@JsonProperty("relations")
    public void setRelations(ImmutableList<DatabaseRelationDefinition> relations) {
        this.relations = relations;
    }*/

    /*Relations*/
    @JsonProperty("uniqueConstraints")
    private List<Map<String,Object>> uniqueConstraints;
    //private Map<String,Object> uniqueConstraints;

    @JsonProperty("otherFunctionalDependencies")
    private List<String> otherFunctionalDependencies;

    @JsonProperty("foreignKeys")
    private List<Map<String,Object>> foreignKeys;

    @JsonProperty("columns")
    private List<Map<String,Object>> columns;

    @JsonProperty("name")
    private String name;

    @JsonProperty("uniqueConstraints")
    public List<Map<String,Object>> getUniqueConstraints() {
        return uniqueConstraints;
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
    public List<Map<String,Object>> getForeignKeys() {
        return foreignKeys;
    }

    @JsonProperty("columns")
    public List<Map<String,Object>> getColumns() {
        return columns;
    }

    @JsonProperty("name")
    public String getName() { return name; }

    @JsonProperty("name")
    public void setName(String name) { this.name = name; }

    /*Unique Constraints*/
    //@JsonProperty("name")
    private String constraintname;
    @JsonProperty("determinants")
    private List<String> determinants;
    @JsonProperty("isPrimaryKey")
    private boolean isPrimaryKey;

    public String getConstraintname() {
        return constraintname;
    }
    //@JsonProperty("name")
    public void setConstraintname(String constraintname) { this.constraintname = constraintname; }

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

    /*Foreign Keys*/
    //@JsonProperty("name")
    private String foreignkeyname;
    @JsonProperty("from")
    private Map<String,Object> from;
    @JsonProperty("to")
    private Map<String,Object> to;


    //@JsonProperty("name")
    public String getForeignkeyname() { return name; }

    //@JsonProperty("name")
    public void setForeignkeyname(String name) { this.name = name; }

    //@JsonProperty("from")
    public Map<String,Object> getFrom() { return from; }

    //@JsonProperty("from")
    public void setFrom(Map<String,Object> from) { this.from = from; }

    //@JsonProperty("to")
    public Map<String,Object> getTo() { return to; }

    //@JsonProperty("to")
    public void setTo(Map<String,Object> to) { this.to = to; }

    /* From and To*/
    //@JsonProperty("relation")
    private String fromrelation;
    private String torelation;
    //@JsonProperty("columns")
    private List<String> fromcolumns;
    private List<String> tocolumns;

    /*@JsonProperty("relation")*/
    public String getFromrelation() { return fromrelation; }
    public String getTorelation() { return torelation; }

    /*@JsonProperty("relation")*/
    public void setFromrelation(String fromrelation) { this.fromrelation = fromrelation; }
    public void setTorelation(String torelation) { this.torelation = torelation; }

    //@JsonProperty("columns")
    public List<String> getFromcolumns() { return fromcolumns; }
    public List<String> getTocolumns() { return tocolumns; }

    //@JsonProperty("columns")
    public void setFromcolumns(List<String> fromcolumns) { this.fromcolumns = fromcolumns; }
    public void setTocolumns(List<String> tocolumns) { this.tocolumns = tocolumns; }

    //@JsonProperty("name")
    private String columnname;

    @JsonProperty("isNullable")
    private boolean isNullable;

    @JsonProperty("datatype")
    private String datatype;

    //@JsonProperty("name")
    public void setColumnname(String columnname) {
        this.columnname = columnname;
    }

    @JsonProperty("isNullable")
    public void setIsNullable(boolean isNullable) {
        this.isNullable = isNullable;
    }

    @JsonProperty("datatype")
    public void setDatatype(String datatype) {
        this.datatype = datatype;
    }
}
