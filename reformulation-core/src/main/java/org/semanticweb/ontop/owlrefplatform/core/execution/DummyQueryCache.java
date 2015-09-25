package org.semanticweb.ontop.owlrefplatform.core.execution;

import org.semanticweb.ontop.owlrefplatform.core.QueryCache;
import org.semanticweb.ontop.owlrefplatform.core.NativeQuery;

/**
 * Does not cache anything.
 */
public class DummyQueryCache implements QueryCache {

    @Override
    public NativeQuery getTargetQuery(String sparqlQuery) {
        return null;
    }

    @Override
    public void cacheTargetQuery(String sparqlQuery, NativeQuery nativeQuery) {
    }

    @Override
    public void clear() {
    }
}
