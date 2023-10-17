package it.unibz.inf.ontop.injection.impl;

import it.unibz.inf.ontop.exception.InvalidOntopConfigurationException;
import it.unibz.inf.ontop.injection.OntopMappingSettings;
import it.unibz.inf.ontop.spec.fact.FactExtractor;

import java.util.Properties;


class OntopMappingSettingsImpl extends OntopKGQuerySettingsImpl implements OntopMappingSettings {

    private static final String DEFAULT_FILE = "mapping-default.properties";

    OntopMappingSettingsImpl(Properties properties) {
        super(loadProperties(properties));
    }

    private static Properties loadProperties(Properties userProperties) {
        Properties properties = loadDefaultMappingProperties();
        properties.putAll(userProperties);

        String factExtractorKey = FactExtractor.class.getCanonicalName();
        if (!userProperties.containsKey(factExtractorKey)) {
            Boolean withTBoxFactExtractor = getBoolean(properties, ENABLE_FACT_EXTRACTION_WITH_TBOX)
                    .orElseThrow(() -> new InvalidOntopConfigurationException
                            (ENABLE_FACT_EXTRACTION_WITH_TBOX + "is required but missing " + "(must have a default value)"));

            String factExtractorValue = withTBoxFactExtractor
                    ? properties.getProperty("fact-extraction-with-tbox")
                    : properties.getProperty("fact-extraction-without-tbox");

            if (factExtractorValue == null) {
                throw new InvalidOntopConfigurationException("Missing a default value for using the fact extractor");
            }
            properties.put(factExtractorKey, factExtractorValue);
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
    public boolean isFactExtractionWithTBoxEnabled() { return getRequiredBoolean(OntopMappingSettings.ENABLE_FACT_EXTRACTION_WITH_TBOX);}

    @Override
    public boolean areSuperClassesOfDomainRangeInferred() {
        return getRequiredBoolean(OntopMappingSettings.INFER_SUPER_CLASSES_OF_DOMAIN_RANGE);
    }

    @Override
    public boolean isValuesNodesWrapInLensesInMappingEnabled() {
        return getRequiredBoolean(OntopMappingSettings.WRAP_MAPPING_VALUES_NODES_IN_LENSES);
    }
}
