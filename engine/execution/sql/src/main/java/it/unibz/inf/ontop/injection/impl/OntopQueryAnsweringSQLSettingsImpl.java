package it.unibz.inf.ontop.injection.impl;

import it.unibz.inf.ontop.injection.OntopQueryAnsweringSQLSettings;
import it.unibz.inf.ontop.injection.OntopSQLCoreSettings;

import java.util.Optional;
import java.util.Properties;

public class OntopQueryAnsweringSQLSettingsImpl extends OntopQueryAnsweringSettingsImpl
        implements OntopQueryAnsweringSQLSettings {

    private static final String DEFAULT_FILE = "query-answering-sql-default.properties";
    private final OntopSQLCoreSettings sqlSettings;

    OntopQueryAnsweringSQLSettingsImpl(Properties userProperties) {
        super(loadProperties(userProperties));
        sqlSettings = new OntopSQLCoreSettingsImpl(copyProperties());
    }

    private static Properties loadProperties(Properties userProperties) {
        Properties properties = OntopSQLCoreSettingsImpl.loadDefaultOBDAProperties();
        properties.putAll(loadDefaultQASQLProperties());
        properties.putAll(userProperties);
        return properties;
    }

    static Properties loadDefaultQASQLProperties() {
        return loadDefaultPropertiesFromFile(OntopQueryAnsweringSQLSettings.class, DEFAULT_FILE);
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
    public String getJdbcUser() {
        return sqlSettings.getJdbcUser();
    }

    @Override
    public String getJdbcPassword() {
        return sqlSettings.getJdbcPassword();
    }

    @Override
    public Optional<String> getJdbcDriver() {
        return sqlSettings.getJdbcDriver();
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
    public int getAbandonedTimeout() {
        return getRequiredInteger(ABANDONED_TIMEOUT);
    }

    @Override
    public int getConnectionPoolInitialSize() {
        return getRequiredInteger(INIT_POOL_SIZE);
    }

    @Override
    public int getConnectionPoolMaxSize() {
        return getRequiredInteger(MAX_POOL_SIZE);
    }
}
