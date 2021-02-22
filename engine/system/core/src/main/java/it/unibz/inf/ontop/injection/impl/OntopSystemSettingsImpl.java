package it.unibz.inf.ontop.injection.impl;

import it.unibz.inf.ontop.injection.OntopOBDASettings;
import it.unibz.inf.ontop.injection.OntopSystemSettings;

import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    public Optional<String> getHttpCacheControl() {
        String cacheControl = getProperty(HTTP_CACHE_CONTROL)
                // Former configuration (to be removed in a future version)
                .orElseGet(() ->
                        Stream.of(getInteger(HTTP_CACHE_MAX_AGE)
                                        .map(i -> "max-age=" + i),
                                getInteger(HTTP_CACHE_STALE_WHILE_REVALIDATE)
                                        .map(i -> "stale-while-revalidate=" + i),
                                getInteger(HTTP_CACHE_STALE_IF_ERROR)
                                        .map(i -> "stale-if-error=" + i))
                                .flatMap(e -> e.map(Stream::of)
                                        .orElseGet(Stream::empty))
                                .collect(Collectors.joining(", ")));
        return Optional.of(cacheControl)
                .filter(s -> !s.isEmpty());
    }
}
