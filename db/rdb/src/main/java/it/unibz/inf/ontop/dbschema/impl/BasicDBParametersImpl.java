package it.unibz.inf.ontop.dbschema.impl;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import com.google.inject.assistedinject.Assisted;
import it.unibz.inf.ontop.dbschema.DBParameters;
import it.unibz.inf.ontop.dbschema.DatabaseRelationDefinition;
import it.unibz.inf.ontop.dbschema.QuotedIDFactory;
import it.unibz.inf.ontop.injection.OntopModelConfiguration;
import it.unibz.inf.ontop.injection.OntopSQLCoreConfiguration;
import it.unibz.inf.ontop.model.type.DBTypeFactory;
import it.unibz.inf.ontop.model.type.TermType;
import it.unibz.inf.ontop.model.type.TypeFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.google.inject.Injector;

public class BasicDBParametersImpl implements DBParameters {

    @JsonProperty("relations")
    private List<?> relations;
    @JsonProperty("metadata")
    private String metadata;

    @JsonProperty("idFactory")
    private QuotedIDFactory idFactory;
    @JsonProperty("dbTypeFactory")
    private DBTypeFactory dbTypeFactory;

    @JsonProperty("driverName")
    private String driverName;
    @JsonProperty("driverVersion")
    private String driverVersion;
    @JsonProperty("databaseProductName")
    private String databaseProductName;
    @JsonProperty("databaseVersion")
    private String databaseVersion;

    public BasicDBParametersImpl(){
        super();
    }
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

    @JsonIgnore
    private List<?> getRelations() {return relations;}


    //Class QuotedIDFactory = Class.forName(cname)

    @SuppressWarnings("unchecked")
    @JsonProperty("metadata")
    private void unpackNested(Map<String,Object> metadata) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {

        Class<QuotedIDFactory> cls = (Class<QuotedIDFactory>) Class.forName(metadata.get("quotedIdFactory").toString());
        Constructor<QuotedIDFactory> cons = cls.getConstructor();
        this.idFactory = cons.newInstance();

        OntopSQLCoreConfiguration defaultConfiguration = OntopSQLCoreConfiguration.defaultBuilder()
            //OntopModelConfiguration.defaultBuilder()
            .jdbcDriver("")
            .jdbcUrl("\"jdbc:h2:tcp://localhost/DBName\"")
            .build();
        Injector injector = defaultConfiguration.getInjector();
        TypeFactory TYPE_FACTORY = injector.getInstance(TypeFactory.class);
        this.dbTypeFactory = TYPE_FACTORY.getDBTypeFactory();

        this.driverName = (String) metadata.get("driverName");
        this.driverVersion = (String) metadata.get("driverVersion");
        this.databaseProductName = (String) metadata.get("dbmsProductName");
        this.databaseVersion = (String) metadata.get("dbmsVersion");
    }

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

    @JsonProperty("driverName")
    @Override
    public String getDriverName() { return driverName; }

    @JsonProperty("driverName")
    public void setDriverName(String driverName) { this.driverName = driverName; }

    @JsonProperty("driverVersion")
    @Override
    public String getDriverVersion() {
        return driverVersion;
    }

    @JsonProperty("driverVersion")
    public void setDriverVersion(String driverVersion) { this.driverVersion = driverVersion; }

    @JsonProperty("databaseProductName")
    @Override
    public String getDbmsProductName() {
        return databaseProductName;
    }

    @JsonProperty("databaseProductName")
    public void setDbmsProductName(String databaseProductName) { this.databaseProductName = databaseProductName; }

    @JsonProperty("databaseVersion")
    @Override
    public String getDbmsVersion() {
        return databaseVersion;
    }

    @JsonProperty("databaseVersion")
    public void setDbmsVersion(String databaseVersion) { this.databaseVersion = databaseVersion; }
}
