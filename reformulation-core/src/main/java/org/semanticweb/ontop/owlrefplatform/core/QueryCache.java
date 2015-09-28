package org.semanticweb.ontop.owlrefplatform.core;

/**
 * Cache of queries.
 *
 * Mutable class.
 */
public interface QueryCache {
    ExecutableQuery getTargetQuery(String sparqlQuery);

    void cacheTargetQuery(String sparqlQuery, ExecutableQuery executableQuery);

    void clear();
}
