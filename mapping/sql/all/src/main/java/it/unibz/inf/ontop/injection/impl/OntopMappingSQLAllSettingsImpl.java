package it.unibz.inf.ontop.injection.impl;

import it.unibz.inf.ontop.injection.OntopMappingSQLAllSettings;

import java.util.Properties;


public class OntopMappingSQLAllSettingsImpl extends OntopMappingSQLSettingsImpl implements OntopMappingSQLAllSettings {

    OntopMappingSQLAllSettingsImpl(Properties properties, boolean isR2rml) {
        super(loadMappingSQLAllProperties(properties, isR2rml));
    }

    private static final String DEFAULT_OBDA_PROPERTIES_FILE = "default_obda.properties";
    private static final String DEFAULT_R2RML_PROPERTIES_FILE = "default_r2rml.properties";

    static Properties loadMappingSQLAllProperties(Properties userPreferences,  boolean isR2rml) {
        String defaultFile = isR2rml
                ? DEFAULT_R2RML_PROPERTIES_FILE
                : DEFAULT_OBDA_PROPERTIES_FILE;
        Properties properties = loadDefaultPropertiesFromFile(OntopMappingSQLAllSettings.class, defaultFile);
        properties.putAll(userPreferences);
        return properties;
    }
}
