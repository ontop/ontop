package org.semanticweb.ontop.owlrefplatform.core.execution;

import org.semanticweb.ontop.owlrefplatform.core.QueryCache;
import org.semanticweb.ontop.owlrefplatform.core.TargetQuery;

/**
 * Does not cache anything.
 */
public class DummyQueryCache implements QueryCache {

    @Override
    public TargetQuery getTargetQuery(String sparqlQuery) {
        return null;
    }

    @Override
    public void cacheTargetQuery(String sparqlQuery, TargetQuery targetQuery) {
    }

    @Override
    public void clear() {
    }
}
