package it.unibz.inf.ontop.injection;


public interface OntopReformulationSettings extends OntopOBDASettings, OntopOptimizationSettings {

    boolean isExistentialReasoningEnabled();

    boolean isDistinctPostProcessingEnabled();

    boolean isQueryLoggingEnabled();

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
}
