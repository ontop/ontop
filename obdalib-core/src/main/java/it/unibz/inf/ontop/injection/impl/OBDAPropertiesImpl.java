package it.unibz.inf.ontop.injection.impl;

import it.unibz.inf.ontop.injection.InvalidOntopConfigurationException;
import it.unibz.inf.ontop.injection.OBDAProperties;
import it.unibz.inf.ontop.injection.OntopModelProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.Properties;

public class OBDAPropertiesImpl extends OntopModelPropertiesImpl implements OBDAProperties {

    private static final String DEFAULT_OBDA_PROPERTIES_FILE = "default_obda.properties";
    private static final String DEFAULT_R2RML_PROPERTIES_FILE = "default_r2rml.properties";

    protected OBDAPropertiesImpl(Properties userProperties, boolean isR2rml) throws InvalidOntopConfigurationException {
        super(loadProperties(userProperties, isR2rml));
    }

    private static Properties loadProperties(Properties userPreferences,  boolean isR2rml) {
        String defaultFile = isR2rml
                ? DEFAULT_R2RML_PROPERTIES_FILE
                : DEFAULT_OBDA_PROPERTIES_FILE;
        Properties properties = loadDefaultPropertiesFromFile(OBDAProperties.class, defaultFile);
        properties.putAll(userPreferences);
        return properties;
    }

    @Override
    public Optional<String> getMappingFilePath() {
        return getProperty(OBDAProperties.MAPPING_FILE_PATH);
    }

    @Override
    public boolean isFullMetadataExtractionEnabled() {
        return getRequiredBoolean(OBDAProperties.OBTAIN_FULL_METADATA);
    }

    @Override
    public Optional<String> getJdbcUrl() {
        return getProperty(OBDAProperties.JDBC_URL);
    }

}
