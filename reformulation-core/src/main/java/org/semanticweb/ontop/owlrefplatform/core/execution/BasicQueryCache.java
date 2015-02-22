package org.semanticweb.ontop.owlrefplatform.core.execution;

import com.google.inject.Inject;
import org.semanticweb.ontop.owlrefplatform.core.QueryCache;
import org.semanticweb.ontop.owlrefplatform.core.TargetQuery;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Basic implementation. No timeout.
 *
 */
public class BasicQueryCache implements QueryCache {

    private final Map<String, TargetQuery> targetQueryCache;

    @Inject
    private BasicQueryCache() {
        targetQueryCache = new ConcurrentHashMap<>();
    }

    @Override
    public TargetQuery getTargetQuery(String sparqlQuery) {
        return targetQueryCache.get(sparqlQuery);
    }

    @Override
    public void cacheTargetQuery(String sparqlQuery, TargetQuery targetQuery) {
        targetQueryCache.put(sparqlQuery, targetQuery);
    }

    @Override
    public void clear() {
        targetQueryCache.clear();
    }
}
