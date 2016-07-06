package it.unibz.inf.ontop.injection.impl;

import it.unibz.inf.ontop.injection.InvalidOBDAConfigurationException;
import it.unibz.inf.ontop.injection.OBDAProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.Properties;

public class OBDAPropertiesImpl implements OBDAProperties {

    private static final String DEFAULT_OBDA_PROPERTIES_FILE = "default_obda.properties";
    private static final String DEFAULT_R2RML_PROPERTIES_FILE = "default_r2rml.properties";

    private static Logger LOG = LoggerFactory.getLogger(OBDAProperties.class);
    private final Properties properties;

    /**
     * Beware: immutable class!
     *
     * Recommended constructor.
     *
     * Changing the Properties object afterwards will not have any effect
     * on this OBDAProperties object.
     */
    protected OBDAPropertiesImpl(Properties userProperties, boolean isR2rml) throws InvalidOBDAConfigurationException {

        String defaultFilePath = isR2rml
                ? DEFAULT_R2RML_PROPERTIES_FILE
                : DEFAULT_OBDA_PROPERTIES_FILE;

        /**
         * Loads default properties
         */
        properties = loadDefaultPropertiesFromFile(OBDAProperties.class, defaultFilePath);
        /**
         * Overloads the default properties.
         */
        properties.putAll(userProperties);
    }

    protected static Properties loadDefaultPropertiesFromFile(Class localClass, String fileName) {
        Properties properties = new Properties();
        InputStream in = localClass.getResourceAsStream(fileName);
        if (in == null)
            throw new RuntimeException("Configuration " + fileName + " not found.");

        try {

            properties.load(in);
        } catch (IOException e1) {
            LOG.error("Error reading default OBDA properties.");
            LOG.debug(e1.getMessage(), e1);
            throw new RuntimeException("Impossible to extract configuration from " + fileName);
        }
        return properties;
    }

    @Override
    public Optional<String> getMappingFilePath() {
        return Optional.ofNullable(getProperty(OBDAProperties.MAPPING_FILE_PATH));
    }

    @Override
    public boolean obtainFullMetadata() {
        return Optional.ofNullable(getBoolean(OBDAProperties.OBTAIN_FULL_METADATA))
                .orElseThrow(() -> new IllegalStateException("Predefined value for OBTAIN_FULL_METADATA is missing"));
    }

    @Override
    public Optional<String> getJdbcUrl() {
        return Optional.ofNullable(getProperty(OBDAProperties.JDBC_URL));
    }

    /**
     * Returns the value of the given key.
     *
     * Returns null if not available.
     */
    public Object get(Object key) {
        return properties.get(key);
    }

    /**
     * Returns the boolean value of the given key.
     *
     * Returns null if not available.
     */
    @Override
    public boolean getBoolean(String key) {
        String value = (String) get(key);
        return Boolean.parseBoolean(value);
    }

    /**
     * Returns the integer value of the given key.
     *
     * Returns null if not available.
     */
    @Override
    public int getInteger(String key) {
        String value = (String) get(key);
        return Integer.parseInt(value);
    }

    /**
     * Returns the string value of the given key.
     *
     * Returns null if not available.
     */
    @Override
    public String getProperty(String key) {
        return (String) get(key);
    }

    @Override
    public boolean contains(Object key) {
        return properties.containsKey(key);
    }

    protected Properties copyProperties() {
        Properties p = new Properties();
        p.putAll(properties);
        return p;
    }


}
