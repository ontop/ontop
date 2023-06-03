package it.unibz.inf.ontop.iq.optimizer.impl;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.optimizer.LeftJoinIQOptimizer;
import it.unibz.inf.ontop.iq.optimizer.impl.lj.CardinalityInsensitiveJoinTransferLJOptimizer;
import it.unibz.inf.ontop.iq.optimizer.impl.lj.CardinalitySensitiveJoinTransferLJOptimizer;
import it.unibz.inf.ontop.iq.optimizer.impl.lj.LJWithNestingOnRightToInnerJoinOptimizer;
import it.unibz.inf.ontop.iq.optimizer.impl.lj.MergeLJOptimizer;

public class DefaultCompositeLeftJoinIQOptimizer implements LeftJoinIQOptimizer {

    private final ImmutableList<LeftJoinIQOptimizer> optimizers;

    @Inject
    private DefaultCompositeLeftJoinIQOptimizer(
            CardinalitySensitiveJoinTransferLJOptimizer cardinalitySensitiveJoinTransferLJOptimizer,
            CardinalityInsensitiveJoinTransferLJOptimizer cardinalityInsensitiveJoinTransferLJOptimizer,
            LJWithNestingOnRightToInnerJoinOptimizer ljWithNestingOnRightToInnerJoinOptimizer,
            MergeLJOptimizer mergeLJOptimizer) {
        this.optimizers = ImmutableList.of(
                cardinalitySensitiveJoinTransferLJOptimizer,
                cardinalityInsensitiveJoinTransferLJOptimizer,
                ljWithNestingOnRightToInnerJoinOptimizer,
                mergeLJOptimizer);

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
