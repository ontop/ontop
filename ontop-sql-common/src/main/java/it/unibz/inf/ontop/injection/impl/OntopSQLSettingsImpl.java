package it.unibz.inf.ontop.injection.impl;


import it.unibz.inf.ontop.injection.OntopSQLSettings;
import it.unibz.inf.ontop.utils.IDGenerator;

import java.util.Properties;

public class OntopSQLSettingsImpl extends OntopOBDASettingsImpl implements OntopSQLSettings {

    private static final String DB_PREFIX = "DB-";
    private final String jdbcUrl;
    private final String dbUser;
    private final String dbPassword;
    private final String jdbcDriver;
    private final String dbName;

    /**
     * Beware: immutable class!
     * <p>
     * Recommended constructor.
     * <p>
     * Changing the Properties object afterwards will not have any effect
     * on this OntopModelProperties object.
     *
     * @param userProperties
     */
    protected OntopSQLSettingsImpl(Properties userProperties) {
        super(userProperties);

        jdbcUrl = getRequiredProperty(OntopSQLSettings.JDBC_URL);
        dbUser = getRequiredProperty(OntopSQLSettings.DB_USER);
        dbPassword = getRequiredProperty(OntopSQLSettings.DB_PASSWORD);
        jdbcDriver = getRequiredProperty(OntopSQLSettings.JDBC_DRIVER);

        dbName = getProperty(OntopSQLSettings.DB_NAME)
                .orElseGet(() -> IDGenerator.getNextUniqueID(DB_PREFIX));
    }

    @Override
    public String getJdbcUrl() {
        return jdbcUrl;
    }

    @Override
    public String getDBName() {
        return dbName;
    }

    @Override
    public String getDBUser() {
        return dbUser;
    }

    @Override
    public String getDbPassword() {
        return dbPassword;
    }

    @Override
    public String getJdbcDriver() {
        return jdbcDriver;
    }
}
