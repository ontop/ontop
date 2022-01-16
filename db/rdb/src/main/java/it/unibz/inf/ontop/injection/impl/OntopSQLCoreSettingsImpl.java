package it.unibz.inf.ontop.injection.impl;


import it.unibz.inf.ontop.dbschema.DBMetadataProvider;
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
    private static final String DB_MP_FACTORY_SUFFIX = "-metadataProvider";

    private static final String DEFAULT_FILE = "sql-default.properties";
    private final String jdbcUrl;
    private final String jdbcDriver;

    /**
     * Beware: immutable class!
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
    }

    static Properties loadSQLCoreProperties(Properties userProperties) {

        String jdbcDriver = extractJdbcDriver(userProperties);

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

        /*
         * DB metadata provider
         */
        String dbMPFactoryKey = jdbcDriver + DB_MP_FACTORY_SUFFIX;
        String dbMPFactoryName = DBMetadataProvider.class.getCanonicalName();
        Optional.ofNullable(properties.getProperty(dbMPFactoryKey))
                // Must NOT override user properties
                .filter(v -> !userProperties.containsKey(dbMPFactoryName))
                .ifPresent(v -> properties.setProperty(dbMPFactoryName, v));

        return properties;
    }


    public static String extractJdbcUrl(Properties userProperties) {
        return Optional.ofNullable(userProperties.getProperty(OntopSQLCoreSettings.JDBC_URL))
                .orElseThrow(() -> new InvalidOntopConfigurationException(OntopSQLCoreSettings.JDBC_URL + " is required"));
    }

    public static String extractJdbcDriver(Properties userProperties) {
        return Optional.ofNullable(userProperties.getProperty(OntopSQLCoreSettings.JDBC_DRIVER))
                .orElseGet(() -> {
                    try {
                        return DriverManager.getDriver(extractJdbcUrl(userProperties)).getClass().getCanonicalName();
                    }
                    catch (SQLException e) {
                        throw new InvalidOntopConfigurationException("Impossible to get the JDBC driver. Reason: "
                                + e.getMessage());
                    }
                });
    }


    @Override
    public String getJdbcUrl() {
        return jdbcUrl;
    }

    @Override
    public String getJdbcDriver() {
        return jdbcDriver;
    }
}
