package it.unibz.inf.ontop.injection.impl;

import it.unibz.inf.ontop.injection.OntopMappingSQLAllSettings;
import it.unibz.inf.ontop.injection.OntopMappingSQLTemporalSettings;

import java.util.Properties;

public class OntopMappingSQLTemporalSettingsImpl extends OntopMappingSQLSettingsImpl implements OntopMappingSQLTemporalSettings {

    OntopMappingSQLTemporalSettingsImpl(Properties properties, boolean isR2rml, boolean isTemporal) {
        super(loadMappingSQLAllProperties(properties, isR2rml, isTemporal));
    }

    private static final String DEFAULT_TOBDA_PROPERTIES_FILE = "default_tobda.properties";
    private static final String DEFAULT_OBDA_PROPERTIES_FILE = "default_obda.properties";
    private static final String DEFAULT_R2RML_PROPERTIES_FILE = "default_r2rml.properties";

    static Properties loadMappingSQLAllProperties(Properties userPreferences,  boolean isR2rml, boolean isTemporal) {
        String defaultFile = DEFAULT_OBDA_PROPERTIES_FILE;
        if(isR2rml)
            defaultFile = DEFAULT_R2RML_PROPERTIES_FILE;
        if(isTemporal)
            defaultFile = DEFAULT_TOBDA_PROPERTIES_FILE;

        Properties properties = loadDefaultPropertiesFromFile(OntopMappingSQLAllSettings.class, defaultFile);
        properties.putAll(userPreferences);
        return properties;
    }

}
