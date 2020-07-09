package it.unibz.inf.ontop.injection;


public interface OntopReformulationSettings extends OntopOBDASettings, OntopOptimizationSettings {

    boolean isExistentialReasoningEnabled();

    boolean isDistinctPostProcessingEnabled();

    boolean isQueryLoggingEnabled();
    boolean isSparqlQueryIncludedIntoQueryLog();
    boolean isReformulatedQueryIncludedIntoQueryLog();
    boolean arePropertiesIncludedIntoQueryLog();
    boolean areClassesIncludedIntoQueryLog();
    boolean areTablesIncludedIntoQueryLog();

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
    // Includes properties into the query log
    String PROPERTIES_INCLUDED_QUERY_LOGGING = "ontop.queryLogging.includeProperties";
    // Includes classes into the query log
    String CLASSES_INCLUDED_QUERY_LOGGING = "ontop.queryLogging.includeClasses";
    // Includes DB tables/views into the query log
    String TABLES_INCLUDED_QUERY_LOGGING = "ontop.queryLogging.includeTables";

}
