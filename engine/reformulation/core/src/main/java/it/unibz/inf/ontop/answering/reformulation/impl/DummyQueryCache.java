package it.unibz.inf.ontop.answering.reformulation.impl;

import it.unibz.inf.ontop.query.KGQuery;
import it.unibz.inf.ontop.answering.reformulation.QueryCache;
import it.unibz.inf.ontop.iq.IQ;

/**
 * Does not cache anything.
 */
public class DummyQueryCache implements QueryCache {

    @Override
    public IQ get(KGQuery inputQuery) {
        return null;
    }

    @Override
    public void put(KGQuery inputQuery, IQ executableQuery) {
    }

    @Override
    public void clear() {
    }
}
