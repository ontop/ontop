package it.unibz.inf.ontop.injection.impl;

import it.unibz.inf.ontop.injection.OntopOBDASettings;
import it.unibz.inf.ontop.injection.OntopOptimizationSettings;
import it.unibz.inf.ontop.injection.OntopReformulationSettings;

import java.util.Properties;

public class OntopReformulationSettingsImpl extends OntopOBDASettingsImpl implements OntopReformulationSettings {

    private static final String DEFAULT_FILE = "reformulation-default.properties";
    private final OntopOptimizationSettings optimizationSettings;

    OntopReformulationSettingsImpl(Properties userProperties) {
        super(loadProperties(userProperties));
        optimizationSettings = new OntopOptimizationSettingsImpl(copyProperties());
    }

    private static Properties loadProperties(Properties userProperties) {
        Properties properties = OntopOptimizationSettingsImpl.loadDefaultOptimizationProperties();
        properties.putAll(loadDefaultRuntimeProperties());
        properties.putAll(userProperties);
        return properties;
    }

    static Properties loadDefaultRuntimeProperties() {
        return loadDefaultPropertiesFromFile(OntopOBDASettings.class, DEFAULT_FILE);
    }

    @Override
    public boolean isExistentialReasoningEnabled() {
        return getRequiredBoolean(EXISTENTIAL_REASONING);
    }

    @Override
    public boolean isIRISafeEncodingEnabled() {
        return getRequiredBoolean(SQL_GENERATE_REPLACE);
    }

    @Override
    public boolean isDistinctPostProcessingEnabled() {
        return getRequiredBoolean(DISTINCT_RESULTSET);
    }
}
