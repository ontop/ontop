package it.unibz.inf.ontop.iq.optimizer.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.optimizer.IQOptimizer;

public abstract class CompositeIQOptimizer implements IQOptimizer {

    private final ImmutableList<? extends IQOptimizer> optimizers;

    protected CompositeIQOptimizer(IQOptimizer... optimizers) {
        this.optimizers = ImmutableList.copyOf(optimizers);
    }

    @Override
    public IQ optimize(IQ query) {
        //non-final
        IQ current = query;
        for (IQOptimizer optimizer : optimizers) {
            current = optimizer.optimize(current);
        }
        return current;
    }
}
