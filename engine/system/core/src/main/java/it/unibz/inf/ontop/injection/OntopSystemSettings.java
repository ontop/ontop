package it.unibz.inf.ontop.injection;


import java.util.Optional;

public interface OntopSystemSettings extends OntopReformulationSettings {

    /**
     * Query EVALUATION timeout (executed on the DB engine)
     *
     * Has no effect if negative or equal to 0.
     */
    Optional<Integer> getDefaultQueryTimeout();

    /**
     * Needed by some in-memory DBs
     *  (e.g. an H2 DB storing a semantic index)
     */
    boolean isPermanentDBConnectionEnabled();

    // HTTP Caching
    Optional<String> getHttpCacheControl();

    //--------------------------
    // Keys
    //--------------------------

    String DEFAULT_QUERY_TIMEOUT = "ontop.query.defaultTimeout";
    String PERMANENT_DB_CONNECTION = "ontop.permanentConnection";

    // HTTP caching
    String HTTP_CACHE_CONTROL = "ontop.http.cacheControl";
    @Deprecated
    String HTTP_CACHE_MAX_AGE = "ontop.http.cache.maxAge";
    @Deprecated
    String HTTP_CACHE_STALE_WHILE_REVALIDATE = "ontop.http.cache.staleWhileRevalidate";
    @Deprecated
    String HTTP_CACHE_STALE_IF_ERROR = "ontop.http.cache.staleIfError";
}
