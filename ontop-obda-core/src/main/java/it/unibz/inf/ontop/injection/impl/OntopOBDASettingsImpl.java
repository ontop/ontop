package it.unibz.inf.ontop.injection.impl;

import it.unibz.inf.ontop.injection.OntopOBDASettings;

import java.util.Properties;


public class OntopOBDASettingsImpl extends OntopModelSettingsImpl implements OntopOBDASettings {

    private static final String DEFAULT_FILE = "obda-default.properties";
    private final boolean isSameAs;
    private final boolean optEquivalences;

    protected OntopOBDASettingsImpl(Properties userProperties) {
        super(loadProperties(userProperties));
        isSameAs = getRequiredBoolean(SAME_AS);
        optEquivalences = getRequiredBoolean(OPTIMIZE_EQUIVALENCES);
    }

    private static Properties loadProperties(Properties userProperties) {
        Properties properties = loadDefaultOBDAProperties();
        properties.putAll(userProperties);
        return properties;
    }

    static Properties loadDefaultOBDAProperties() {
        return loadDefaultPropertiesFromFile(OntopOBDASettings.class, DEFAULT_FILE);
    }

    @Override
    public boolean isEquivalenceOptimizationEnabled() {
        return optEquivalences;
    }

    @Override
    public boolean isSameAsInMappingsEnabled() {
        return isSameAs;
    }
}
