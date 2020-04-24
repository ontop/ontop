package it.unibz.inf.ontop.injection.impl;

import it.unibz.inf.ontop.answering.reformulation.generation.normalization.DialectExtraNormalizer;
import it.unibz.inf.ontop.answering.reformulation.generation.serializer.SelectFromWhereSerializer;
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
    private static final String DIALECT_NORMALIZER_SUFFIX = "-normalizer";
    private static final String DIALECT_SERIALIZER_SUFFIX = "-serializer";

    private final OntopSQLCoreSettings sqlSettings;

    OntopReformulationSQLSettingsImpl(Properties userProperties) {
        super(loadProperties(userProperties));
        sqlSettings = new OntopSQLCoreSettingsImpl(copyProperties());
    }

    protected static String extractJdbcUrl(Properties userProperties) {
        return Optional.ofNullable(userProperties.getProperty(OntopSQLCoreSettings.JDBC_URL))
                .orElseThrow(() -> new InvalidOntopConfigurationException(OntopSQLCoreSettings.JDBC_URL + " is required"));
    }

    protected static String extractJdbcDriver(Properties userProperties) {
        return Optional.ofNullable(userProperties.getProperty(OntopSQLCoreSettings.JDBC_DRIVER))
                .orElseGet(() -> {
                    try {
                        return DriverManager.getDriver(extractJdbcUrl(userProperties)).getClass().getCanonicalName();
                    } catch (SQLException e) {
                        throw new InvalidOntopConfigurationException("Impossible to get the JDBC driver. Reason: "
                                + e.getMessage());
                    }
                });
    }

    private static Properties loadProperties(Properties userProperties) {
        Properties properties = loadDefaultQASQLProperties();
        properties.putAll(userProperties);

        String jdbcDriver = extractJdbcDriver(userProperties);

        properties.setProperty(OntopSQLCoreSettings.JDBC_DRIVER, jdbcDriver);
        properties.putAll(userProperties);

        /*
         * Dialect normalizer
         */
        String normalizerKey = jdbcDriver + DIALECT_NORMALIZER_SUFFIX;
        String normalizerName = DialectExtraNormalizer.class.getCanonicalName();
        Optional.ofNullable(properties.getProperty(normalizerKey))
                .filter(v -> !userProperties.containsKey(normalizerName))
                .ifPresent(v -> properties.setProperty(normalizerName, v));

        /*
         * Dialect serializer
         */
        String serializerKey = jdbcDriver + DIALECT_SERIALIZER_SUFFIX;
        String serializerName = SelectFromWhereSerializer.class.getCanonicalName();
        Optional.ofNullable(properties.getProperty(serializerKey))
                .filter(v -> !userProperties.containsKey(serializerName))
                .ifPresent(v -> properties.setProperty(serializerName, v));

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
