package it.unibz.inf.ontop.injection.impl;

import it.unibz.inf.ontop.exception.InvalidOntopConfigurationException;
import it.unibz.inf.ontop.injection.OntopModelSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Optional;
import java.util.Properties;

public class OntopModelSettingsImpl implements OntopModelSettings {

    private static final String DEFAULT_PROPERTIES_FILE = "default.properties";
    private static final Logger LOG = LoggerFactory.getLogger(OntopModelSettings.class);
    private final Properties properties;
    private final CardinalityPreservationMode cardinalityMode;
    private final boolean testMode;

    /**
     * Beware: immutable class!
     *
     * Recommended constructor.
     *
     * Changing the Properties object afterwards will not have any effect
     * on this OntopModelProperties object.
     */
    protected OntopModelSettingsImpl(Properties userProperties) {
        /**
         * Loads default properties
         */
        properties = loadDefaultPropertiesFromFile(OntopModelSettings.class, DEFAULT_PROPERTIES_FILE);
        /**
         * Overloads the default properties.
         */
        properties.putAll(userProperties);

        cardinalityMode = extractCardinalityMode(properties);
        testMode = extractBoolean(properties, OntopModelSettings.TEST_MODE);
    }

    private static CardinalityPreservationMode extractCardinalityMode(Properties properties)
            throws InvalidOntopConfigurationException {
        Object cardinalityModeObject = Optional.ofNullable(properties.get(OntopModelSettings.CARDINALITY_MODE))
                .orElseThrow(() -> new InvalidOntopConfigurationException(CARDINALITY_MODE + " key is missing"));

        if (cardinalityModeObject instanceof String) {
            try {
                return CardinalityPreservationMode.valueOf((String)cardinalityModeObject);
            } catch (IllegalArgumentException e) {
            }
        }
        else if (cardinalityModeObject instanceof CardinalityPreservationMode)
            return (CardinalityPreservationMode) cardinalityModeObject;

        throw new InvalidOntopConfigurationException("Invalid value for " + CARDINALITY_MODE
                + ": is " + cardinalityModeObject);
    }

    private static boolean extractBoolean(Properties properties, String key) {
        Object value = Optional.ofNullable(properties.get(key))
                .orElseThrow(() -> new InvalidOntopConfigurationException(key + " key is missing"));
        if (value instanceof Boolean)
            return (Boolean) value;
        else if (value instanceof String)
            return Boolean.valueOf((String) value);
        else
            throw new InvalidOntopConfigurationException("Invalid value for " + key + ": is " + value);
    }

    protected static Properties loadDefaultPropertiesFromFile(Class localClass, String fileName) {
        try (InputStream in = localClass.getResourceAsStream(fileName)) {
            if (in == null)
                throw new RuntimeException("Configuration " + fileName + " not found.");
            Properties properties = new Properties();
            properties.load(in);
            return properties;
        }
        catch (IOException e1) {
            LOG.error("Error reading default OBDA properties.");
            LOG.debug(e1.getMessage(), e1);
            throw new RuntimeException("Impossible to extract configuration from " + fileName);
        }
    }

    /**
     * Returns the value of the given key.
     */
    public Object get(Object key) {
        return properties.get(key);
    }

    /**
     * TODO: make it configurable
     */
    @Override
    public CardinalityPreservationMode getCardinalityPreservationMode() {
        return cardinalityMode;
    }

    @Override
    public boolean isTestModeEnabled() {
        return testMode;
    }

    /**
     * Returns the boolean value of the given key.
     */
    Optional<Boolean> getBoolean(String key) {
        return getBoolean(properties, key);
    }

    static Optional<Boolean> getBoolean(Properties properties, String key) {
        Object value = properties.get(key);

        if (value == null) {
            return Optional.empty();
        }

        if (value instanceof Boolean) {
            return Optional.of((Boolean) value);
        }
        else if (value instanceof String) {
            return Optional.of(Boolean.parseBoolean((String)value));
        }
        else {
            throw new InvalidOntopConfigurationException("A boolean was expected: " + value);
        }
    }

    boolean getRequiredBoolean(String key) {
        return getBoolean(key)
                .orElseThrow(() -> new InvalidOntopConfigurationException(key + " is required but missing " +
                        "(must have a default value)"));
    }

    /**
     * Returns the integer value of the given key.
     */
    Optional<Integer> getInteger(String key) {
        String value = (String) get(key);
        return Optional.ofNullable((value == null) ? null : Integer.parseInt(value));
    }

    /**
     * Returns the long value of the given key.
     */
    Optional<Long> getLong(String key) {
        String value = (String) get(key);
        return Optional.ofNullable((value == null) ? null : Long.parseLong(value));
    }

    int getRequiredInteger(String key) {
        return getInteger(key)
                .orElseThrow(() -> new InvalidOntopConfigurationException(key + " is required but missing " +
                        "(must have a default value)"));
    }

    long getRequiredLong(String key) {
        return getLong(key)
                .orElseThrow(() -> new InvalidOntopConfigurationException(key + " is required but missing " +
                        "(must have a default value)"));
    }

    /**
     * Returns the string value of the given key.
     */
    @Override
    public Optional<String> getProperty(String key) {
        return Optional.ofNullable((String) get(key));
    }

    String getRequiredProperty(String key) {
        return getProperty(key)
                .orElseThrow(() -> new InvalidOntopConfigurationException(key + " is required but missing " +
                        "(must have a default value)"));
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

    protected Enumeration<Object> getPropertyKeys() {
        return properties.keys();
    }


}
