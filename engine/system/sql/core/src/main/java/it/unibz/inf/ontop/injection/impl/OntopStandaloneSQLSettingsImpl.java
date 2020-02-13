package it.unibz.inf.ontop.injection.impl;

import it.unibz.inf.ontop.injection.OntopStandaloneSQLSettings;
import it.unibz.inf.ontop.injection.OntopSystemSQLSettings;

import java.util.Optional;
import java.util.Properties;


public class OntopStandaloneSQLSettingsImpl extends OntopMappingSQLAllSettingsImpl implements OntopStandaloneSQLSettings {

    private final OntopSystemSQLSettings systemSettings;

    OntopStandaloneSQLSettingsImpl(Properties userProperties, boolean isR2rml) {
        super(loadProperties(userProperties), isR2rml);
        systemSettings = new OntopSystemSQLSettingsImpl(copyProperties());
    }

    private static Properties loadProperties(Properties userProperties) {
        Properties properties = new OntopSystemSQLSettingsImpl(userProperties).copyProperties();
        properties.putAll(userProperties);
        return properties;
    }

    @Override
    public boolean isExistentialReasoningEnabled() {
        return systemSettings.isExistentialReasoningEnabled();
    }

    @Override
    public boolean isDistinctPostProcessingEnabled() {
        return getRequiredBoolean(DISTINCT_RESULTSET);
    }

    @Override
    public long getQueryCacheMaxSize() {
        return getRequiredLong(QUERY_CACHE_MAX_SIZE);
    }

    @Override
    public boolean isKeepAliveEnabled() {
        return systemSettings.isKeepAliveEnabled();
    }

    @Override
    public boolean isRemoveAbandonedEnabled() {
        return systemSettings.isRemoveAbandonedEnabled();
    }

    @Override
    public int getConnectionTimeout() {
        return systemSettings.getConnectionTimeout();
    }

    @Override
    public int getConnectionPoolInitialSize() {
        return systemSettings.getConnectionPoolInitialSize();
    }

    @Override
    public int getConnectionPoolMaxSize() {
        return systemSettings.getConnectionPoolMaxSize();
    }

    @Override
    public int getFetchSize() {
        return systemSettings.getFetchSize();
    }

    @Override
    public Optional<Integer> getDefaultQueryTimeout() {
        return getInteger(DEFAULT_QUERY_TIMEOUT);
    }

    @Override
    public boolean isPermanentDBConnectionEnabled() {
        return systemSettings.isPermanentDBConnectionEnabled();
    }
}
