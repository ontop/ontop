package it.unibz.inf.ontop.injection.impl;

import it.unibz.inf.ontop.injection.OntopOBDASettings;
import it.unibz.inf.ontop.injection.OntopOptimizationSettings;
import it.unibz.inf.ontop.injection.OntopTranslationSettings;

import java.util.Properties;

public class OntopTranslationSettingsImpl extends OntopOBDASettingsImpl implements OntopTranslationSettings {

    private static final String DEFAULT_FILE = "translation-default.properties";
    private final OntopOptimizationSettings optimizationSettings;

    OntopTranslationSettingsImpl(Properties userProperties) {
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
