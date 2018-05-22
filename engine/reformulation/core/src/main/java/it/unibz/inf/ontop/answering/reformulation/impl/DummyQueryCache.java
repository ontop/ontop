package it.unibz.inf.ontop.answering.reformulation.impl;

import it.unibz.inf.ontop.answering.reformulation.input.InputQuery;
import it.unibz.inf.ontop.answering.reformulation.QueryCache;
import it.unibz.inf.ontop.iq.IQ;

/**
 * Does not cache anything.
 */
public class DummyQueryCache implements QueryCache {

    @Override
    public IQ get(InputQuery inputQuery) {
        return null;
    }

    @Override
    public void put(InputQuery inputQuery, IQ executableQuery) {
    }

    @Override
    public void clear() {
    }
}
