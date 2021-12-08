package it.unibz.inf.ontop.injection.impl;

import it.unibz.inf.ontop.answering.connection.JDBCStatementInitializer;
import it.unibz.inf.ontop.injection.OntopSQLCredentialSettings;
import it.unibz.inf.ontop.injection.OntopSystemSQLSettings;
import it.unibz.inf.ontop.injection.OntopSystemSettings;

import java.util.Optional;
import java.util.Properties;

import static it.unibz.inf.ontop.injection.impl.OntopSQLCoreSettingsImpl.extractJdbcDriver;


public class OntopSystemSQLSettingsImpl extends OntopReformulationSQLSettingsImpl implements OntopSystemSQLSettings {

    private static final String DEFAULT_FILE = "system-sql-default.properties";
    private static final String STATEMENT_INITIALIZER_SUFFIX = "-statementInitializer";
    private final OntopSystemSettings systemSettings;
    private final OntopSQLCredentialSettings sqlCredentialSettings;

    OntopSystemSQLSettingsImpl(Properties userProperties) {
        super(loadProperties(userProperties));
        systemSettings = new OntopSystemSettingsImpl(copyProperties());
        sqlCredentialSettings = new OntopSQLCredentialSettingsImpl(copyProperties());
    }

    private static Properties loadProperties(Properties userProperties) {
        Properties properties = loadDefaultSystemSQLProperties();
        properties.putAll(userProperties);

        String jdbcDriver = extractJdbcDriver(userProperties);

        /*
         * Statement initializer
         */
        String initializerKey = jdbcDriver + STATEMENT_INITIALIZER_SUFFIX;
        String initializerName = JDBCStatementInitializer.class.getCanonicalName();
        Optional.ofNullable(properties.getProperty(initializerKey))
                // Must NOT override user properties
                .filter(v -> !userProperties.containsKey(initializerName))
                .ifPresent(v -> properties.setProperty(initializerName, v));

        return properties;
    }

    static Properties loadDefaultSystemSQLProperties() {
        Properties properties = OntopSystemSettingsImpl.loadDefaultSystemProperties();
        properties.putAll(loadDefaultPropertiesFromFile(OntopSystemSQLSettings.class, DEFAULT_FILE));
        return properties;
    }

    @Override
    public boolean isKeepAliveEnabled() {
        return getRequiredBoolean(KEEP_ALIVE);
    }

    @Override
    public boolean isRemoveAbandonedEnabled() {
        return getRequiredBoolean(REMOVE_ABANDONED);
    }

    @Override
    public int getConnectionTimeout() {
        return getRequiredInteger(CONNECTION_TIMEOUT);
    }

    @Override
    public int getConnectionPoolInitialSize() {
        return getRequiredInteger(INIT_POOL_SIZE);
    }

    @Override
    public int getConnectionPoolMaxSize() {
        return getRequiredInteger(MAX_POOL_SIZE);
    }

    @Override
    public int getFetchSize() {
        return getRequiredInteger(FETCH_SIZE);
    }

    @Override
    public Optional<Integer> getDefaultQueryTimeout() {
        return getInteger(DEFAULT_QUERY_TIMEOUT);
    }

    @Override
    public boolean isPermanentDBConnectionEnabled() {
        return systemSettings.isPermanentDBConnectionEnabled();
    }

    @Override
    public Optional<String> getHttpCacheControl() {
        return systemSettings.getHttpCacheControl();
    }

    @Override
    public Optional<String> getJdbcUser() {
        return sqlCredentialSettings.getJdbcUser();
    }

    @Override
    public Optional<String> getJdbcPassword() {
        return sqlCredentialSettings.getJdbcPassword();
    }
}
