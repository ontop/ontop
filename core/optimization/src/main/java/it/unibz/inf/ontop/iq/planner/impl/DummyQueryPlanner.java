package it.unibz.inf.ontop.iq.planner.impl;

import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.planner.QueryPlanner;
import it.unibz.inf.ontop.iq.tools.ExecutorRegistry;

import javax.inject.Singleton;

@Singleton
public class DummyQueryPlanner implements QueryPlanner {

    @Override
    public IQ optimize(IQ query, ExecutorRegistry executorRegistry) {
        return query;
    }
}
