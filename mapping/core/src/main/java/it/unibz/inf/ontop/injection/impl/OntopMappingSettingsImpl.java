package it.unibz.inf.ontop.injection.impl;

import it.unibz.inf.ontop.injection.OntopMappingSettings;
import it.unibz.inf.ontop.injection.OntopOptimizationSettings;

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
        return properties;
    }

    static Properties loadDefaultMappingProperties() {
        return loadDefaultPropertiesFromFile(OntopMappingSettings.class, DEFAULT_FILE);
    }

    @Override
    public boolean isFullMetadataExtractionEnabled() {
        return getRequiredBoolean(OBTAIN_FULL_METADATA);
    }

    @Override
    public boolean isProvidedDBMetadataCompletionEnabled() {
        return getRequiredBoolean(COMPLETE_PROVIDED_METADATA);
    }

    @Override
    public boolean isDefaultDatatypeInferred() {
        return getRequiredBoolean(INFER_DEFAULT_DATATYPE);
    }

    @Override
    public boolean isOntologyAnnotationQueryingEnabled() {
        return getRequiredBoolean(QUERY_ONTOLOGY_ANNOTATIONS);
    }

    @Override
    public boolean isCanIRIComplete() {
        return getRequiredBoolean(IS_CANONICAL_IRI_COMPLETE);
    }
}
