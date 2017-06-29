package it.unibz.inf.ontop.owlrefplatform.core.execution;

import it.unibz.inf.ontop.answering.input.InputQuery;
import it.unibz.inf.ontop.owlrefplatform.core.ExecutableQuery;
import it.unibz.inf.ontop.owlrefplatform.core.QueryCache;

/**
 * Does not cache anything.
 */
public class DummyQueryCache implements QueryCache {

    @Override
    public ExecutableQuery get(InputQuery inputQuery) {
        return null;
    }

    @Override
    public void put(InputQuery inputQuery, ExecutableQuery executableQuery) {
    }

    @Override
    public void clear() {
    }
}
