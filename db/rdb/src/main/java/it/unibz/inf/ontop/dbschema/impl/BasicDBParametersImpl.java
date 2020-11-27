package it.unibz.inf.ontop.dbschema.impl;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import it.unibz.inf.ontop.dbschema.DBParameters;
import it.unibz.inf.ontop.dbschema.QuotedIDFactory;
import it.unibz.inf.ontop.model.type.DBTypeFactory;

import java.util.List;
import java.util.Map;

public class BasicDBParametersImpl implements DBParameters {

    @JsonProperty("metadata")
    private String metadata;
    @JsonProperty("idFactory")
    private QuotedIDFactory idFactory;
    @JsonProperty("dbTypeFactory")
    private DBTypeFactory dbTypeFactory;

    @JsonProperty("driverName")
    private String driverName;
    @JsonProperty("driverVersion")
    private final String driverVersion;
    @JsonProperty("databaseProductName")
    private final String databaseProductName;
    @JsonProperty("databaseVersion")
    private final String databaseVersion;

    public BasicDBParametersImpl(String driverName,
                                 String driverVersion,
                                 String databaseProductName,
                                 String databaseVersion,
                                 QuotedIDFactory idFactory,
                                 DBTypeFactory dbTypeFactory) {
        this.idFactory = idFactory;
        this.dbTypeFactory = dbTypeFactory;

        this.driverName = driverName;
        this.driverVersion = driverVersion;
        this.databaseProductName = databaseProductName;
        this.databaseVersion = databaseVersion;
    }

    /*@JsonProperty("metadata")
    @Override
    public QuotedIDFactory getMetadata() {
        return metadata;
    }*/

    @JsonProperty("idFactory")
    @Override
    public QuotedIDFactory getQuotedIDFactory() {
        return idFactory;
    }

    @JsonProperty("idFactory")
    public void setQuotedIDFactory(QuotedIDFactory idFactory) {
        this.idFactory = idFactory;
    }

    @JsonProperty("dbTypeFactory")
    @Override
    public DBTypeFactory getDBTypeFactory() { return dbTypeFactory; }

    @JsonProperty("dbTypeFactory")
    public void setDbTypeFactory(DBTypeFactory dbTypeFactory) {
        this.dbTypeFactory = dbTypeFactory;
    }

    @SuppressWarnings("unchecked")
    @JsonProperty("driverName")
    private void unpackNested(Map<String,Object> metadataN) {
        this.metadata = (String)metadataN.get("metadata");
        Map<String,String> dName = (Map<String,String>)metadataN.get("driverName");
        this.driverName = dName.get("driverName");
    }

    @JsonProperty("driverName")
    @Override
    public String getDriverName() {
        return driverName;
    }

    @JsonProperty("driverName")
    public void setDriverName(String driverName) {
        this.driverName = driverName;
    }

    @JsonProperty("driverVersion")
    @Override
    public String getDriverVersion() {
        return driverVersion;
    }

    @JsonProperty("databaseProductName")
    @Override
    public String getDbmsProductName() {
        return databaseProductName;
    }

    @JsonProperty("databaseVersion")
    @Override
    public String getDbmsVersion() {
        return databaseVersion;
    }
}
