package it.unibz.inf.ontop.injection.impl;

import it.unibz.inf.ontop.injection.OntopOBDASettings;

import java.util.Properties;


public class OntopOBDASettingsImpl extends OntopModelSettingsImpl implements OntopOBDASettings {

    private static final String DEFAULT_FILE = "obda-default.properties";
    private final boolean isSameAs;

    protected OntopOBDASettingsImpl(Properties userProperties) {
        super(loadProperties(userProperties));
        isSameAs = getRequiredBoolean(SAME_AS);
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
    public boolean isSameAsInMappingsEnabled() {
        return isSameAs;
    }
}
