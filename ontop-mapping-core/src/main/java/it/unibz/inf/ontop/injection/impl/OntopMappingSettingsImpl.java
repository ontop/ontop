package it.unibz.inf.ontop.injection.impl;

import it.unibz.inf.ontop.injection.OntopMappingSettings;
import it.unibz.inf.ontop.injection.OntopOBDASettings;

import java.util.Properties;


class OntopMappingSettingsImpl extends OntopOBDASettingsImpl implements OntopMappingSettings {

    private static final String DEFAULT_FILE = "mapping-default.properties";

    OntopMappingSettingsImpl(Properties properties) {
        super(loadProperties(properties));
    }

    private static Properties loadProperties(Properties userProperties) {
        Properties properties = loadDefaultMappingProperties();
        properties.putAll(userProperties);
        return properties;
    }

    public static Properties loadDefaultMappingProperties() {
        return loadDefaultPropertiesFromFile(OntopOBDASettings.class, DEFAULT_FILE);
    }
}
