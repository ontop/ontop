package it.unibz.inf.ontop.injection.impl;

import it.unibz.inf.ontop.injection.OntopOptimizationSettings;

import java.util.Properties;

public class OntopOptimizationSettingsImpl extends OntopModelSettingsImpl
        implements OntopOptimizationSettings {

    private static final String DEFAULT_FILE = "optimization-default.properties";

    /**
     * Beware: immutable class!
     * <p>
     * Recommended constructor.
     * <p>
     * Changing the Properties object afterwards will not have any effect
     * on this OntopModelProperties object.
     *
     * @param userProperties
     */
    protected OntopOptimizationSettingsImpl(Properties userProperties) {
        super(loadProperties(userProperties));
    }

    private static Properties loadProperties(Properties userProperties) {
        Properties properties = loadDefaultOptimizationProperties();
        properties.putAll(userProperties);
        return properties;
    }

    public static Properties loadDefaultOptimizationProperties() {
        return loadDefaultPropertiesFromFile(OntopOptimizationSettings.class, DEFAULT_FILE);
    }
}
