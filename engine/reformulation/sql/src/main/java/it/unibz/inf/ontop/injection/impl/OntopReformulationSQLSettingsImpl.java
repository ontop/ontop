package it.unibz.inf.ontop.injection.impl;

import it.unibz.inf.ontop.answering.reformulation.generation.dialect.SQLDialectAdapter;
import it.unibz.inf.ontop.exception.InvalidOntopConfigurationException;
import it.unibz.inf.ontop.injection.OntopReformulationSQLSettings;
import it.unibz.inf.ontop.injection.OntopSQLCoreSettings;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Optional;
import java.util.Properties;

public class OntopReformulationSQLSettingsImpl extends OntopReformulationSettingsImpl
        implements OntopReformulationSQLSettings {

    private static final String DEFAULT_FILE = "reformulation-sql-default.properties";
    private static final String DIALECT_ADAPTER_SUFFIX = "-adapter";
    private final OntopSQLCoreSettings sqlSettings;

    OntopReformulationSQLSettingsImpl(Properties userProperties) {
        super(loadProperties(userProperties));
        sqlSettings = new OntopSQLCoreSettingsImpl(copyProperties());
    }

    private static Properties loadProperties(Properties userProperties) {
        Properties properties = loadDefaultQASQLProperties();
        properties.putAll(userProperties);

        String jdbcUrl = Optional.ofNullable(userProperties.getProperty(OntopSQLCoreSettings.JDBC_URL))
                .orElseThrow(() -> new InvalidOntopConfigurationException(OntopSQLCoreSettings.JDBC_URL + " is required"));

        String jdbcDriver = Optional.ofNullable(userProperties.getProperty(OntopSQLCoreSettings.JDBC_DRIVER))
                .orElseGet(() -> {
                    try {
                        return DriverManager.getDriver(jdbcUrl).getClass().getCanonicalName();
                    } catch (SQLException e) {
                        throw new InvalidOntopConfigurationException("Impossible to get the JDBC driver. Reason: "
                                + e.getMessage());
                    }
                });

        properties.setProperty(OntopSQLCoreSettings.JDBC_DRIVER, jdbcDriver);
        properties.putAll(userProperties);

        /*
         * Dialect adapter
         */
        String adapterKey = jdbcDriver + DIALECT_ADAPTER_SUFFIX;
        String adapterName = SQLDialectAdapter.class.getCanonicalName();
        Optional.ofNullable(properties.getProperty(adapterKey))
                // Must NOT override user properties
                .filter(v -> !userProperties.containsKey(adapterName))
                .ifPresent(v -> properties.setProperty(adapterName, v));

        return OntopSQLCoreSettingsImpl.loadSQLCoreProperties(properties);
    }

    static Properties loadDefaultQASQLProperties() {
        return loadDefaultPropertiesFromFile(OntopReformulationSQLSettings.class, DEFAULT_FILE);
    }

    @Override
    public String getJdbcUrl() {
        return sqlSettings.getJdbcUrl();
    }

    @Override
    public String getJdbcName() {
        return sqlSettings.getJdbcName();
    }

    @Override
    public String getJdbcDriver() {
        return sqlSettings.getJdbcDriver();
    }
}
