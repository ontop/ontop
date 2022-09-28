package it.unibz.inf.ontop.injection.impl;

import it.unibz.inf.ontop.injection.OntopInputQuerySettings;
import it.unibz.inf.ontop.injection.OntopOptimizationSettings;

import java.util.Properties;

public class OntopInputQuerySettingsImpl extends OntopOBDASettingsImpl implements OntopInputQuerySettings {

    private static final String DEFAULT_FILE = "input-query-default.properties";
    private final OntopOptimizationSettings optimizationSettings;

    OntopInputQuerySettingsImpl(Properties userProperties) {
        super(loadProperties(userProperties));
        optimizationSettings = new OntopOptimizationSettingsImpl(copyProperties());
    }

    private static Properties loadProperties(Properties userProperties) {
        Properties properties = OntopOptimizationSettingsImpl.loadDefaultOptimizationProperties();
        properties.putAll(loadDefaultInputQueryProperties());
        properties.putAll(userProperties);
        return properties;
    }

    static Properties loadDefaultInputQueryProperties() {
        return loadDefaultPropertiesFromFile(OntopInputQuerySettings.class, DEFAULT_FILE);
    }

    @Override
    public boolean isFixedObjectIncludedInDescribe() {
        return getRequiredBoolean(INCLUDE_FIXED_OBJECT_POSITION_IN_DESCRIBE);
    }
}
