package it.unibz.inf.ontop.iq.optimizer.impl;

import it.unibz.inf.ontop.injection.IntermediateQueryFactory;

public abstract class AbstractExtendedIQOptimizer extends AbstractIQOptimizer {

    protected AbstractExtendedIQOptimizer(IntermediateQueryFactory iqFactory) {
        super(iqFactory);
    }

}
