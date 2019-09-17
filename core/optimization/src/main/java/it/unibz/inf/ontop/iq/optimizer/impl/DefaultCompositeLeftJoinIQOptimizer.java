package it.unibz.inf.ontop.iq.optimizer.impl;

import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.optimizer.LeftJoinIQOptimizer;

public class DefaultCompositeLeftJoinIQOptimizer implements LeftJoinIQOptimizer {

    /**
     * TODO: enrich
     */
    @Override
    public IQ optimize(IQ query) {
        return query;
    }
}
