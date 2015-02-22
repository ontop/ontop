package org.semanticweb.ontop.owlrefplatform.core;

/**
 * Cache of queries.
 *
 * Mutable class.
 */
public interface QueryCache {
    TargetQuery getTargetQuery(String sparqlQuery);

    void cacheTargetQuery(String sparqlQuery, TargetQuery targetQuery);

    void clear();
}
