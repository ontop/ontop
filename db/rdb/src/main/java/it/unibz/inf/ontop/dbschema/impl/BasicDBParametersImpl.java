package it.unibz.inf.ontop.dbschema.impl;

import it.unibz.inf.ontop.dbschema.DBParameters;
import it.unibz.inf.ontop.dbschema.QuotedIDFactory;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.model.type.DBTypeFactory;

public class BasicDBParametersImpl implements DBParameters {

    private final QuotedIDFactory idFactory;
    private final DBTypeFactory dbTypeFactory;

    private final String driverName;
    private final String driverVersion;
    private final String databaseProductName;
    private final String databaseVersion;
    private final CoreSingletons coreSingletons;

    BasicDBParametersImpl(String driverName,
                          String driverVersion,
                          String databaseProductName,
                          String databaseVersion,
                          QuotedIDFactory idFactory,
                          CoreSingletons coreSingletons) {
        this.idFactory = idFactory;
        this.dbTypeFactory = coreSingletons.getTypeFactory().getDBTypeFactory();

        this.driverName = driverName;
        this.driverVersion = driverVersion;
        this.databaseProductName = databaseProductName;
        this.databaseVersion = databaseVersion;
        this.coreSingletons = coreSingletons;
    }

    @Override
    public QuotedIDFactory getQuotedIDFactory() {
        return idFactory;
    }

    @Override
    public DBTypeFactory getDBTypeFactory() { return dbTypeFactory; }

    @Override
    public CoreSingletons getCoreSingletons() {
        return coreSingletons;
    }

    @Override
    public String getDriverName() {
        return driverName;
    }

    @Override
    public String getDriverVersion() {
        return driverVersion;
    }

    @Override
    public String getDbmsProductName() {
        return databaseProductName;
    }

    @Override
    public String getDbmsVersion() {
        return databaseVersion;
    }
}
