package it.unibz.inf.ontop.injection.impl;

import it.unibz.inf.ontop.exception.InvalidOntopConfigurationException;
import it.unibz.inf.ontop.injection.OntopMappingSettings;
import it.unibz.inf.ontop.injection.OntopOptimizationSettings;
import it.unibz.inf.ontop.spec.mapping.transformer.ABoxFactIntoMappingConverter;

import java.util.Properties;


class OntopMappingSettingsImpl extends OntopOBDASettingsImpl implements OntopMappingSettings {

    private static final String DEFAULT_FILE = "mapping-default.properties";
    private final OntopOptimizationSettings optimizationSettings;

    OntopMappingSettingsImpl(Properties properties) {
        super(loadProperties(properties));
        optimizationSettings = new OntopOptimizationSettingsImpl(copyProperties());
    }

    private static Properties loadProperties(Properties userProperties) {
        Properties properties = OntopOptimizationSettingsImpl.loadDefaultOptimizationProperties();
        properties.putAll(loadDefaultMappingProperties());
        properties.putAll(userProperties);

        String factConverterKey = ABoxFactIntoMappingConverter.class.getCanonicalName();
        if (!userProperties.containsKey(factConverterKey)) {
            Boolean withValuesNode = getBoolean(properties, ENABLE_VALUES_NODE)
                    .orElseThrow(() -> new InvalidOntopConfigurationException
                            (ENABLE_VALUES_NODE + "is required but missing " + "(must have a default value)"));

            String factConverterValue = withValuesNode
                    ? properties.getProperty("fact-converter-with-values-nodes")
                    : properties.getProperty("fact-converter-without-values-nodes");

            if (factConverterValue == null) {
                throw new InvalidOntopConfigurationException("Missing a default value for constructing the fact converter");
            }
            properties.put(factConverterKey, factConverterValue);
        }

        return properties;
    }

    static Properties loadDefaultMappingProperties() {
        return loadDefaultPropertiesFromFile(OntopMappingSettings.class, DEFAULT_FILE);
    }

    @Override
    public boolean isDefaultDatatypeInferred() {
        return getRequiredBoolean(INFER_DEFAULT_DATATYPE);
    }

    @Override
    public boolean areAbstractDatatypesToleratedInMapping() {
        return getRequiredBoolean(TOLERATE_ABSTRACT_DATATYPE);
    }

    @Override
    public boolean isOntologyAnnotationQueryingEnabled() {
        return getRequiredBoolean(QUERY_ONTOLOGY_ANNOTATIONS);
    }

    @Override
    public boolean isCanIRIComplete() {
        return getRequiredBoolean(IS_CANONICAL_IRI_COMPLETE);
    }

    @Override
    public boolean isValuesNodeEnabled() { return getRequiredBoolean(OntopMappingSettings.ENABLE_VALUES_NODE);}
}
