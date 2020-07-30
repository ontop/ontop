package it.unibz.inf.ontop.injection.impl;

import it.unibz.inf.ontop.injection.OntopOBDASettings;
import it.unibz.inf.ontop.injection.OntopSystemSettings;

import java.util.Optional;
import java.util.Properties;

public class OntopSystemSettingsImpl extends OntopReformulationSettingsImpl implements OntopSystemSettings {

    private static final String DEFAULT_FILE = "system-default.properties";

    OntopSystemSettingsImpl(Properties userProperties) {
        super(loadProperties(userProperties));
    }

    private static Properties loadProperties(Properties userProperties) {
        Properties properties = loadDefaultSystemProperties();
        properties.putAll(userProperties);
        return properties;
    }

    static Properties loadDefaultSystemProperties() {
        return loadDefaultPropertiesFromFile(OntopOBDASettings.class, DEFAULT_FILE);
    }

    @Override
    public Optional<Integer> getDefaultQueryTimeout() {
        return getInteger(DEFAULT_QUERY_TIMEOUT);
    }

    @Override
    public boolean isPermanentDBConnectionEnabled() {
        return getRequiredBoolean(PERMANENT_DB_CONNECTION);
    }

    @Override
    public Optional<Integer> getHttpMaxAge() {
        return getInteger(HTTP_CACHE_MAX_AGE);
    }

    @Override
    public Optional<Integer> getHttpStaleWhileRevalidate() {
        return getInteger(HTTP_CACHE_STALE_WHILE_REVALIDATE);
    }

    @Override
    public Optional<Integer> getHttpStaleIfError() {
        return getInteger(HTTP_CACHE_STALE_IF_ERROR);
    }
}
