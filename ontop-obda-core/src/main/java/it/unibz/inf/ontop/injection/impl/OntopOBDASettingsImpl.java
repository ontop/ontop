package it.unibz.inf.ontop.injection.impl;

import it.unibz.inf.ontop.injection.OntopOBDASettings;

import java.util.Properties;


public class OntopOBDASettingsImpl extends OntopModelSettingsImpl implements OntopOBDASettings {

    private static final String DEFAULT_FILE = "obda-default.properties";

    protected OntopOBDASettingsImpl(Properties userProperties) {
        super(loadProperties(userProperties));
    }

    private static Properties loadProperties(Properties userProperties) {
        Properties properties = loadDefaultOptimizationProperties();
        properties.putAll(userProperties);
        return properties;
    }

    public static Properties loadDefaultOptimizationProperties() {
        return loadDefaultPropertiesFromFile(OntopOBDASettings.class, DEFAULT_FILE);
    }
}
