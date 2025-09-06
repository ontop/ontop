package it.unibz.inf.ontop.iq.optimizer.impl;

import it.unibz.inf.ontop.injection.IntermediateQueryFactory;

public abstract class AbstractIntensionalQueryMerger extends AbstractIQOptimizer {

    protected AbstractIntensionalQueryMerger(IntermediateQueryFactory iqFactory) {
        super(iqFactory);
    }
}
