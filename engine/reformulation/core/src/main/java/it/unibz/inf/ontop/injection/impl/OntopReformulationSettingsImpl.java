package it.unibz.inf.ontop.injection.impl;

import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.injection.OntopOBDASettings;
import it.unibz.inf.ontop.injection.OntopOptimizationSettings;
import it.unibz.inf.ontop.injection.OntopReformulationSettings;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Properties;

public class OntopReformulationSettingsImpl extends OntopOBDASettingsImpl implements OntopReformulationSettings {

    private static final String DEFAULT_FILE = "reformulation-default.properties";
    private final OntopOptimizationSettings optimizationSettings;

    // LAZY
    @Nullable
    private ImmutableSet<String> httpHeaderNamesToLog;

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
    public boolean isPostProcessingAvoided() {
        return getRequiredBoolean(AVOID_POST_PROCESSING);
    }

    @Override
    public boolean areInvalidTriplesExcludedFromResultSet() {
        return getRequiredBoolean(EXCLUDE_INVALID_TRIPLES_FROM_RESULT_SET);
    }

    @Override
    public boolean isQueryLoggingEnabled() {
        return getRequiredBoolean(QUERY_LOGGING);
    }

    @Override
    public boolean isQueryTemplateExtractionEnabled() {
        return getRequiredBoolean(QUERY_TEMPLATE_EXTRACTION);
    }

    @Override
    public boolean isSparqlQueryIncludedIntoQueryLog() {
        return getRequiredBoolean(SPARQL_INCLUDED_QUERY_LOGGING);
    }

    @Override
    public boolean isReformulatedQueryIncludedIntoQueryLog() {
        return getRequiredBoolean(REFORMULATED_INCLUDED_QUERY_LOGGING);
    }

    @Override
    public boolean areClassesAndPropertiesIncludedIntoQueryLog() {
        return getRequiredBoolean(CLASSES_INCLUDED_QUERY_LOGGING);
    }

    @Override
    public boolean areTablesIncludedIntoQueryLog() {
        return getRequiredBoolean(TABLES_INCLUDED_QUERY_LOGGING);
    }

    @Override
    public boolean isQueryLoggingDecompositionEnabled() {
        return getRequiredBoolean(QUERY_LOGGING_DECOMPOSITION);
    }

    @Override
    public boolean areQueryLoggingDecompositionAndMergingMutuallyExclusive() {
        return getRequiredBoolean(QUERY_LOGGING_DECOMPOSITION_AND_MERGING_EXCLUSIVE);
    }

    @Override
    public boolean isFixedObjectIncludedInDescribe() {
        return getRequiredBoolean(INCLUDE_FIXED_OBJECT_POSITION_IN_DESCRIBE);
    }

    @Override
    public long getQueryCacheMaxSize() {
        return getRequiredLong(QUERY_CACHE_MAX_SIZE);
    }

    @Override
    public String getApplicationName() {
        return getRequiredProperty(APPLICATION_NAME);
    }

    @Override
    public synchronized ImmutableSet<String> getHttpHeaderNamesToLog() {
        if (httpHeaderNamesToLog == null) {
            httpHeaderNamesToLog = Collections.list(getPropertyKeys()).stream()
                    .filter(k -> k instanceof String)
                    .map(k -> (String) k)
                    .filter(k -> k.startsWith(HTTP_HEADER_INCLUDED_QUERY_LOGGING_PREFIX))
                    .filter(k -> getBoolean(k)
                            .filter(b -> b)
                            .isPresent())
                    .map(k -> k.substring(HTTP_HEADER_INCLUDED_QUERY_LOGGING_PREFIX.length()))
                    //Normalization
                    .map(String::toLowerCase)
                    .collect(ImmutableCollectors.toSet());
        }
        return httpHeaderNamesToLog;
    }
}
