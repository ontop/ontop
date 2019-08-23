package it.unibz.inf.ontop.injection.impl;


import it.unibz.inf.ontop.injection.OntopSQLCoreSettings;
import it.unibz.inf.ontop.utils.IDGenerator;

import java.util.Optional;
import java.util.Properties;

public class OntopSQLCoreSettingsImpl extends OntopOBDASettingsImpl implements OntopSQLCoreSettings {

    private static final String DB_PREFIX = "DB-";
    private final String jdbcUrl;
    private final Optional<String> jdbcDriver;
    private final String jdbcName;

    /**
     * Beware: immutable class!
     * <p>
     * Recommended constructor.
     * <p>
     * Changing the Properties object afterwards will not have any effect
     * on this OntopSQLCoreSettings object.
     *
     * @param userProperties
     */
    protected OntopSQLCoreSettingsImpl(Properties userProperties) {
        super(userProperties);

        jdbcUrl = getRequiredProperty(OntopSQLCoreSettings.JDBC_URL);
        jdbcDriver = getProperty(OntopSQLCoreSettings.JDBC_DRIVER);
        jdbcName = getProperty(OntopSQLCoreSettings.JDBC_NAME)
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
    public Optional<String> getJdbcDriver() {
        return jdbcDriver;
    }
}
