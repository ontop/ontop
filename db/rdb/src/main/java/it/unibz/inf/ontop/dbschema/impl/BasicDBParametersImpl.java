package it.unibz.inf.ontop.dbschema.impl;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import it.unibz.inf.ontop.dbschema.DBParameters;
import it.unibz.inf.ontop.dbschema.QuotedIDFactory;
import it.unibz.inf.ontop.model.type.DBTypeFactory;

import java.util.List;

public class BasicDBParametersImpl implements DBParameters {

    @JsonProperty("idFactory")
    private final QuotedIDFactory idFactory;
    @JsonProperty("dbTypeFactory")
    private final DBTypeFactory dbTypeFactory;

    @JsonProperty("driverName")
    private final String driverName;
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

    @JsonProperty("idFactory")
    @Override
    public QuotedIDFactory getQuotedIDFactory() {
        return idFactory;
    }

    @JsonProperty("dbTypeFactory")
    @Override
    public DBTypeFactory getDBTypeFactory() { return dbTypeFactory; }

    @JsonProperty("driverName")
    @Override
    public String getDriverName() {
        return driverName;
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
