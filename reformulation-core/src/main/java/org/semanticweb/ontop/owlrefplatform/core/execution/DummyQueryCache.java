package org.semanticweb.ontop.owlrefplatform.core.execution;

import org.semanticweb.ontop.owlrefplatform.core.QueryCache;
import org.semanticweb.ontop.owlrefplatform.core.ExecutableQuery;

/**
 * Does not cache anything.
 */
public class DummyQueryCache implements QueryCache {

    @Override
    public ExecutableQuery getTargetQuery(String sparqlQuery) {
        return null;
    }

    @Override
    public void cacheTargetQuery(String sparqlQuery, ExecutableQuery executableQuery) {
    }

    @Override
    public void clear() {
    }
}
