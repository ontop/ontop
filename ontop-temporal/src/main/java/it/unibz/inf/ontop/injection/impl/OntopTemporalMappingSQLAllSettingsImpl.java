package it.unibz.inf.ontop.injection.impl;

import it.unibz.inf.ontop.injection.OntopMappingSQLAllSettings;
import it.unibz.inf.ontop.injection.OntopTemporalMappingSQLAllSettings;

import java.util.Properties;

public class OntopTemporalMappingSQLAllSettingsImpl extends OntopMappingSQLAllSettingsImpl
        implements OntopTemporalMappingSQLAllSettings
{

    OntopTemporalMappingSQLAllSettingsImpl(Properties properties, boolean isR2rml, boolean isTemporal) {
        super(loadTemporalMappingProperties(properties, isTemporal), isR2rml);
    }

    private static final String DEFAULT_TOBDA_PROPERTIES_FILE = "default_tobda.properties";

    static Properties loadTemporalMappingProperties(Properties properties, boolean isTemporal) {
        if(isTemporal) {
            properties.putAll(loadDefaultPropertiesFromFile(OntopMappingSQLAllSettings.class, DEFAULT_TOBDA_PROPERTIES_FILE));
        }
        return properties;
    }

}
