package it.unibz.inf.ontop.owlrefplatform.core.execution;

import it.unibz.inf.ontop.owlrefplatform.core.ExecutableQuery;
import it.unibz.inf.ontop.owlrefplatform.core.QueryCache;

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
