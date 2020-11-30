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
    private final File dbMetadataFile;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    ImmutableMetadataImpl(@JsonProperty("dbParameters") DBParameters dbParameters,
                          @JsonProperty("relations") ImmutableList<DatabaseRelationDefinition> relations,
                          @JsonProperty("dbMetadataFile") File dbMetadataFile){
        //super();
        this.dbParameters = dbParameters;
        this.relations = relations;
        this.dbMetadataFile = dbMetadataFile;
        //this(dbParameters, relations, dbMetadataFile);
    }

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



    //@JsonProperty("relations")
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

    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /*@JsonProperty("relations")
    public void setRelations(ImmutableList<DatabaseRelationDefinition> relations) {
        this.relations = relations;
    }*/


    @JsonProperty("relations")
    private void unpackNested(Map<String,Object> metadata) {
        this.uniqueConstraints = (Map<String,Object>) metadata.get("uniqueConstraints");
        this.otherFunctionalDependencies = (String) metadata.get("otherFunctionalDependencies");
        this.foreignKeys = (Map<String,Object>) metadata.get("foreignKeys");
        this.columns = (Map<String,Object>) metadata.get("columns");
        this.name = (String) metadata.get("name");
        this.constraintname = (String) uniqueConstraints.get("name");
        this.determinants = (List<String>) uniqueConstraints.get("determinants");
        this.isPrimaryKey = (Boolean) uniqueConstraints.get("isPrimaryKey");
        this.foreignkeyname = (String) foreignKeys.get("name");
        this.from = (Map<String,Object>) foreignKeys.get("from");
        this.to = (Map<String,Object>) foreignKeys.get("to");
        this.fromrelation = (String) from.get("relation");
        this.torelation = (String) to.get("relation");
        this.fromcolumns = (List<String>) from.get("columns");
        this.tocolumns = (List<String>) to.get("columns");
        this.columnname = (String) columns.get("name");
        this.isNullable = (Boolean) columns.get("isNullable");
        this.datatype = (String) columns.get("datatype");
    }

    /*Relations*/
    @JsonProperty("uniqueConstraints")
    private Map<String,Object> uniqueConstraints;

    @JsonProperty("otherFunctionalDependencies")
    private String otherFunctionalDependencies;

    @JsonProperty("foreignKeys")
    private Map<String,Object> foreignKeys;

    @JsonProperty("columns")
    private Map<String,Object> columns;

    @JsonProperty("name")
    private String name;

    @JsonProperty("otherFunctionalDependencies")
    public String getOtherFunctionalDependencies() {
        return otherFunctionalDependencies;
    }

    @JsonProperty("otherFunctionalDependencies")
    public void setOtherFunctionalDependencies(String otherFunctionalDependencies) {
        this.otherFunctionalDependencies = otherFunctionalDependencies;
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
