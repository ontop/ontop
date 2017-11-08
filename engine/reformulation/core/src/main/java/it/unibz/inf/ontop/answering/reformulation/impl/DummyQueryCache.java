package it.unibz.inf.ontop.answering.reformulation.impl;

import it.unibz.inf.ontop.answering.reformulation.input.InputQuery;
import it.unibz.inf.ontop.answering.reformulation.ExecutableQuery;
import it.unibz.inf.ontop.answering.reformulation.QueryCache;

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
