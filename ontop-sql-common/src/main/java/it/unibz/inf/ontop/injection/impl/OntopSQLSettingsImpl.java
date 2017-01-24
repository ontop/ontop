package it.unibz.inf.ontop.injection.impl;


import it.unibz.inf.ontop.injection.OntopSQLSettings;
import it.unibz.inf.ontop.utils.IDGenerator;

import java.util.Properties;

public class OntopSQLSettingsImpl extends OntopOBDASettingsImpl implements OntopSQLSettings {

    private static final String DB_PREFIX = "DB-";
    private final String jdbcUrl;
    private final String jdbcUser;
    private final String jdbcPassword;
    private final String jdbcDriver;
    private final String jdbcName;

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
        jdbcUser = getRequiredProperty(OntopSQLSettings.JDBC_USER);
        jdbcPassword = getRequiredProperty(OntopSQLSettings.JDBC_PASSWORD);
        jdbcDriver = getRequiredProperty(OntopSQLSettings.JDBC_DRIVER);

        jdbcName = getProperty(OntopSQLSettings.JDBC_NAME)
                .orElseGet(() -> IDGenerator.getNextUniqueID(DB_PREFIX));
    }

    @Override
    public String getJdbcUrl() {
        return jdbcUrl;
    }

    @Override
    public String getJdbcName() {
        return jdbcName;
    }

    @Override
    public String getJdbcUser() {
        return jdbcUser;
    }

    public String getJdbcPassword() {
        return jdbcPassword;
    }

    @Override
    public String getJdbcDriver() {
        return jdbcDriver;
    }
}
