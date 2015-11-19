package org.semanticweb.ontop.owlrefplatform.core.execution;

import com.google.inject.Inject;
import org.semanticweb.ontop.owlrefplatform.core.QueryCache;
import org.semanticweb.ontop.owlrefplatform.core.ExecutableQuery;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Basic implementation. No timeout.
 *
 */
public class BasicQueryCache implements QueryCache {

    private final Map<String, ExecutableQuery> targetQueryCache;

    @Inject
    private BasicQueryCache() {
        targetQueryCache = new ConcurrentHashMap<>();
    }

    @Override
    public ExecutableQuery getTargetQuery(String sparqlQuery) {
        return targetQueryCache.get(sparqlQuery);
    }

    @Override
    public void cacheTargetQuery(String sparqlQuery, ExecutableQuery executableQuery) {
        targetQueryCache.put(sparqlQuery, executableQuery);
    }

    @Override
    public void clear() {
        targetQueryCache.clear();
    }
}
