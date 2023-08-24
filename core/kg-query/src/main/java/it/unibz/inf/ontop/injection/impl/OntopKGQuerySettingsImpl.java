package it.unibz.inf.ontop.injection.impl;

import it.unibz.inf.ontop.injection.OntopKGQuerySettings;
import it.unibz.inf.ontop.injection.OntopOptimizationSettings;

import java.util.Properties;

public class OntopKGQuerySettingsImpl extends OntopOBDASettingsImpl implements OntopKGQuerySettings {

    private static final String DEFAULT_FILE = "kg-query-default.properties";
    private final OntopOptimizationSettings optimizationSettings;

    OntopKGQuerySettingsImpl(Properties userProperties) {
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
        return loadDefaultPropertiesFromFile(OntopKGQuerySettings.class, DEFAULT_FILE);
    }

    @Override
    public boolean isFixedObjectIncludedInDescribe() {
        return getRequiredBoolean(INCLUDE_FIXED_OBJECT_POSITION_IN_DESCRIBE);
    }

    @Override
    public boolean isCustomSPARQLFunctionRegistrationEnabled() {
        return getRequiredBoolean(REGISTER_CUSTON_SPARQL_AGGREGATE_FUNCTIONS);
    }
}
