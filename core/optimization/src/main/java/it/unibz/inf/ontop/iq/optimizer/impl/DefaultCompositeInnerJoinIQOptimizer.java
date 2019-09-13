package it.unibz.inf.ontop.iq.optimizer.impl;

import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.optimizer.InnerJoinIQOptimizer;

public class DefaultCompositeInnerJoinIQOptimizer implements InnerJoinIQOptimizer {

    /**
     * TODO: enrich
     */
    @Override
    public IQ optimize(IQ query) {
        return query;
    }
}
