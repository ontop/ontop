package it.unibz.inf.ontop.injection;


import com.google.common.collect.ImmutableSet;

public interface OntopReformulationSettings extends OntopOBDASettings, OntopOptimizationSettings {

    boolean isExistentialReasoningEnabled();

    boolean isPostProcessingAvoided();

    /**
     * If false, makes the query fail when an invalid triple is detected.
     */
    boolean areInvalidTriplesExcludedFromResultSet();

    boolean isQueryLoggingEnabled();
    boolean isQueryTemplateExtractionEnabled();
    boolean isSparqlQueryIncludedIntoQueryLog();
    boolean isReformulatedQueryIncludedIntoQueryLog();
    boolean areClassesAndPropertiesIncludedIntoQueryLog();
    boolean areTablesIncludedIntoQueryLog();
    boolean isQueryLoggingDecompositionEnabled();
    boolean areQueryLoggingDecompositionAndMergingMutuallyExclusive();

    /**
     * Returns true if the pattern "?s ?p <describedIRI>" should also be
     * considered while answering a DESCRIBE query.
     *
     */
    boolean isFixedObjectIncludedInDescribe();

    ImmutableSet<String> getHttpHeaderNamesToLog();

    long getQueryCacheMaxSize();

    String getApplicationName();


    //--------------------------
    // Keys
    //--------------------------

    String EXISTENTIAL_REASONING = "ontop.existentialReasoning";
    String AVOID_POST_PROCESSING = "ontop.avoidPostProcessing";
    String EXCLUDE_INVALID_TRIPLES_FROM_RESULT_SET = "ontop.excludeInvalidTriplesFromResultSet";
    String QUERY_CACHE_MAX_SIZE = "ontop.cache.query.size";
    String QUERY_LOGGING = "ontop.queryLogging";
    // Needed for logging
    String APPLICATION_NAME = "ontop.applicationName";
    // Includes the SPARQL query string into the query log
    String SPARQL_INCLUDED_QUERY_LOGGING = "ontop.queryLogging.includeSparqlQuery";
    // Includes the reformulated query into the query log
    String REFORMULATED_INCLUDED_QUERY_LOGGING = "ontop.queryLogging.includeReformulatedQuery";
    // Includes classes and properties into the query log
    String CLASSES_INCLUDED_QUERY_LOGGING = "ontop.queryLogging.includeClassesAndProperties";
    // Includes DB tables/views into the query log
    String TABLES_INCLUDED_QUERY_LOGGING = "ontop.queryLogging.includeTables";
    String HTTP_HEADER_INCLUDED_QUERY_LOGGING_PREFIX = "ontop.queryLogging.includeHttpHeader.";
    String QUERY_TEMPLATE_EXTRACTION = "ontop.queryLogging.extractQueryTemplate";
    String QUERY_LOGGING_DECOMPOSITION = "ontop.queryLogging.decomposition";
    String QUERY_LOGGING_DECOMPOSITION_AND_MERGING_EXCLUSIVE = "ontop.queryLogging.decompositionAndMergingMutuallyExclusive";

    String INCLUDE_FIXED_OBJECT_POSITION_IN_DESCRIBE = "ontop.includeFixedObjectPositionInDescribe";
}
