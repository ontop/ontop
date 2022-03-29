package it.unibz.inf.ontop.iq.optimizer.impl;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.optimizer.*;

public class CompositeFlattenLifter implements FlattenLifter {

    private final ImmutableList<IQOptimizer> optimizers;

    @Inject
    private CompositeFlattenLifter(FilterLifter filterLifter,
                                   BasicFlattenLifter flattenLifter,
                                   BooleanExpressionPushDownOptimizer pushDownOptimizer) {
        this.optimizers = ImmutableList.of(filterLifter, flattenLifter, pushDownOptimizer);
    }

    @Override
    public IQ optimize(IQ query) {
        return optimizers.stream()
                .reduce(query,
                        (q, o) -> o.optimize(q),
                        (q1, q2) -> {
                            throw new MinorOntopInternalBugException("parallel query optimization not applicable");
                        });
    }
}
