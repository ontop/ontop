package it.unibz.inf.ontop.injection.impl;

import it.unibz.inf.ontop.injection.InvalidOntopConfigurationException;
import it.unibz.inf.ontop.injection.OBDASettings;
import it.unibz.inf.ontop.injection.OntopSQLSettings;

import java.util.Optional;
import java.util.Properties;

public class OBDASettingsImpl extends OntopSQLSettingsImpl implements OBDASettings {

    private static final String DEFAULT_OBDA_PROPERTIES_FILE = "default_obda.properties";
    private static final String DEFAULT_R2RML_PROPERTIES_FILE = "default_r2rml.properties";

    protected OBDASettingsImpl(Properties userProperties, boolean isR2rml) throws InvalidOntopConfigurationException {
        super(loadProperties(userProperties, isR2rml));
    }

    private static Properties loadProperties(Properties userPreferences,  boolean isR2rml) {
        String defaultFile = isR2rml
                ? DEFAULT_R2RML_PROPERTIES_FILE
                : DEFAULT_OBDA_PROPERTIES_FILE;
        Properties properties = loadDefaultPropertiesFromFile(OBDASettings.class, defaultFile);
        properties.putAll(userPreferences);
        return properties;
    }

    @Override
    public Optional<String> getMappingFilePath() {
        return getProperty(OBDASettings.MAPPING_FILE_PATH);
    }

    @Override
    public boolean isFullMetadataExtractionEnabled() {
        return getRequiredBoolean(OBDASettings.OBTAIN_FULL_METADATA);
    }

}
