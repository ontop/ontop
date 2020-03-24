package it.unibz.inf.ontop.iq.optimizer.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.optimizer.IQOptimizer;
import it.unibz.inf.ontop.iq.optimizer.InnerJoinIQOptimizer;
import it.unibz.inf.ontop.iq.optimizer.SelfJoinSameTermIQOptimizer;
import it.unibz.inf.ontop.iq.optimizer.SelfJoinUCIQOptimizer;

import javax.inject.Inject;

public class DefaultCompositeInnerJoinIQOptimizer implements InnerJoinIQOptimizer {

    private final ImmutableList<IQOptimizer> optimizers;

    @Inject
    private DefaultCompositeInnerJoinIQOptimizer(
            SelfJoinUCIQOptimizer selfJoinUCIQOptimizer,
            SelfJoinSameTermIQOptimizer selfJoinSameTermIQOptimizer) {
        // TODO: enrich
        this.optimizers = ImmutableList.of(
                selfJoinUCIQOptimizer,
                selfJoinSameTermIQOptimizer);
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
