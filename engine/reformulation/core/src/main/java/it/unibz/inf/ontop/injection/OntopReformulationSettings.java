package it.unibz.inf.ontop.injection;


import com.google.common.collect.ImmutableSet;

public interface OntopReformulationSettings extends OntopOBDASettings, OntopOptimizationSettings {

    boolean isExistentialReasoningEnabled();

    boolean isDistinctPostProcessingEnabled();

    boolean isQueryLoggingEnabled();
    boolean isQueryTemplateExtractionEnabled();
    boolean isSparqlQueryIncludedIntoQueryLog();
    boolean isReformulatedQueryIncludedIntoQueryLog();
    boolean areClassesAndPropertiesIncludedIntoQueryLog();
    boolean areTablesIncludedIntoQueryLog();

    ImmutableSet<String> getHttpHeaderNamesToLog();

    long getQueryCacheMaxSize();

    String getApplicationName();


    //--------------------------
    // Keys
    //--------------------------

    String EXISTENTIAL_REASONING = "ontop.existentialReasoning";
    String DISTINCT_RESULTSET = "ontop.distinctResultSet";
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
}
