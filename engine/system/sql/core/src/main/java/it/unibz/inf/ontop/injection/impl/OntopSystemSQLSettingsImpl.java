package it.unibz.inf.ontop.injection.impl;

import it.unibz.inf.ontop.injection.OntopSystemSQLSettings;
import it.unibz.inf.ontop.injection.OntopSystemSettings;

import java.util.Properties;


public class OntopSystemSQLSettingsImpl extends OntopTranslationSQLSettingsImpl implements OntopSystemSQLSettings {

    private static final String DEFAULT_FILE = "system-sql-default.properties";
    private final OntopSystemSettings systemSettings;

    OntopSystemSQLSettingsImpl(Properties userProperties) {
        super(loadProperties(userProperties));
        systemSettings = new OntopSystemSettingsImpl(copyProperties());
    }

    private static Properties loadProperties(Properties userProperties) {
        Properties properties = loadDefaultSystemSQLProperties();
        properties.putAll(userProperties);
        return properties;
    }

    static Properties loadDefaultSystemSQLProperties() {
        Properties properties = OntopSystemSettingsImpl.loadDefaultSystemProperties();
        properties.putAll(loadDefaultPropertiesFromFile(OntopSystemSQLSettings.class, DEFAULT_FILE));
        return properties;
    }

    @Override
    public boolean isDistinctPostProcessingEnabled() {
        return systemSettings.isDistinctPostProcessingEnabled();
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

    @Override
    public boolean isPermanentDBConnectionEnabled() {
        return systemSettings.isPermanentDBConnectionEnabled();
    }
}
