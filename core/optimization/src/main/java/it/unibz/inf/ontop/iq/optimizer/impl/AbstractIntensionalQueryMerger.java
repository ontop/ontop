package it.unibz.inf.ontop.iq.optimizer.impl;

import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.optimizer.IQOptimizer;

public abstract class AbstractIntensionalQueryMerger extends AbstractIQOptimizer implements IQOptimizer {

    protected AbstractIntensionalQueryMerger(IntermediateQueryFactory iqFactory) {
        super(iqFactory, NO_ACTION);
    }
}
