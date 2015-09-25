package org.semanticweb.ontop.owlrefplatform.core;

/**
 * Cache of queries.
 *
 * Mutable class.
 */
public interface QueryCache {
    NativeQuery getTargetQuery(String sparqlQuery);

    void cacheTargetQuery(String sparqlQuery, NativeQuery nativeQuery);

    void clear();
}
