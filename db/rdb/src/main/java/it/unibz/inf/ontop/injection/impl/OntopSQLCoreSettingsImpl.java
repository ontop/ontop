package it.unibz.inf.ontop.injection.impl;


import it.unibz.inf.ontop.exception.InvalidOntopConfigurationException;
import it.unibz.inf.ontop.generation.normalization.DialectExtraNormalizer;
import it.unibz.inf.ontop.generation.serializer.SelectFromWhereSerializer;
import it.unibz.inf.ontop.injection.OntopOBDASettings;
import it.unibz.inf.ontop.injection.OntopSQLCoreSettings;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBFunctionSymbolFactory;
import it.unibz.inf.ontop.model.type.DBTypeFactory;
import it.unibz.inf.ontop.utils.IDGenerator;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Optional;
import java.util.Properties;

public class OntopSQLCoreSettingsImpl extends OntopOBDASettingsImpl implements OntopSQLCoreSettings {

    private static final String DB_PREFIX = "DB-";

    private static final String DB_TYPE_FACTORY_SUFFIX = "-typeFactory";
    private static final String DB_FS_FACTORY_SUFFIX = "-symbolFactory";
    private static final String DIALECT_SERIALIZER_SUFFIX = "-serializer";
    private static final String DIALECT_NORMALIZER_SUFFIX = "-normalizer";

    private static final String DEFAULT_FILE = "sql-default.properties";
    private final String jdbcUrl;
    private final String jdbcDriver;
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
        super(loadSQLCoreProperties(userProperties));

        jdbcUrl = getRequiredProperty(OntopSQLCoreSettings.JDBC_URL);
        jdbcDriver = getRequiredProperty(OntopSQLCoreSettings.JDBC_DRIVER);
        jdbcName = getProperty(OntopSQLCoreSettings.JDBC_NAME)
                .orElseGet(() -> IDGenerator.getNextUniqueID(DB_PREFIX));
    }

    static Properties loadSQLCoreProperties(Properties userProperties) {
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

        Properties properties = loadDefaultPropertiesFromFile(OntopSQLCoreSettings.class, DEFAULT_FILE);
        properties.setProperty(OntopSQLCoreSettings.JDBC_DRIVER, jdbcDriver);
        properties.putAll(userProperties);

        /*
         * DB type factory
         */
        String typeFactoryKey = jdbcDriver + DB_TYPE_FACTORY_SUFFIX;
        String dbTypeFactoryName = DBTypeFactory.class.getCanonicalName();
        Optional.ofNullable(properties.getProperty(typeFactoryKey))
                // Must NOT override user properties
                .filter(v -> !userProperties.containsKey(dbTypeFactoryName))
                .ifPresent(v -> properties.setProperty(dbTypeFactoryName, v));

        /*
         * DB function symbol factory
         */
        String dbFSFactoryKey = jdbcDriver + DB_FS_FACTORY_SUFFIX;
        String dbFSFactoryName = DBFunctionSymbolFactory.class.getCanonicalName();
        Optional.ofNullable(properties.getProperty(dbFSFactoryKey))
                // Must NOT override user properties
                .filter(v -> !userProperties.containsKey(dbFSFactoryName))
                .ifPresent(v -> properties.setProperty(dbFSFactoryName, v));

        /*
         * Dialect serializer
         */
        String serializerKey = jdbcDriver + DIALECT_SERIALIZER_SUFFIX;
        String serializerName = SelectFromWhereSerializer.class.getCanonicalName();
        Optional.ofNullable(properties.getProperty(serializerKey))
                .filter(v -> !userProperties.containsKey(serializerName))
                .ifPresent(v -> properties.setProperty(serializerName, v));

        /*
         * Dialect normalizer
         */
        String normalizerKey = jdbcDriver + DIALECT_NORMALIZER_SUFFIX;
        String normalizerName = DialectExtraNormalizer.class.getCanonicalName();
        Optional.ofNullable(properties.getProperty(normalizerKey))
                .filter(v -> !userProperties.containsKey(normalizerName))
                .ifPresent(v -> properties.setProperty(normalizerName, v));

        return properties;
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
    public String getJdbcDriver() {
        return jdbcDriver;
    }
}
