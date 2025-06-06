package it.unibz.inf.ontop.iq.optimizer.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.optimizer.IQOptimizer;

public abstract class CompositeIQOptimizer implements IQOptimizer {

    private final ImmutableList<? extends IQOptimizer> optimizers;

    protected CompositeIQOptimizer(ImmutableList<? extends IQOptimizer> optimizers) {
        this.optimizers = optimizers;
    }

    @Override
    public IQ optimize(IQ query) {
        return optimizers.stream()
                .reduce(query,
                        (q, o) -> o.optimize(q),
                        (q1, q2) -> {
                            throw  new MinorOntopInternalBugException("Merge is not supported");
                        });
    }
}
