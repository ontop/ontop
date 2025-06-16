package it.unibz.inf.ontop.iq.optimizer.impl;

import it.unibz.inf.ontop.iq.optimizer.*;

import javax.inject.Inject;

public class DefaultCompositeInnerJoinIQOptimizer extends CompositeIQOptimizer implements InnerJoinIQOptimizer {

    @Inject
    private DefaultCompositeInnerJoinIQOptimizer(
            SelfJoinUCIQOptimizer selfJoinUCIQOptimizer,
            SelfJoinSameTermIQOptimizer selfJoinSameTermIQOptimizer,
            ArgumentTransferInnerJoinFDIQOptimizer fdIQOptimizer,
            RedundantJoinFKOptimizer fkOptimizer,
            BelowDistinctJoinWithClassUnionOptimizer belowDistinctClassUnionOptimizer) {

        super(selfJoinUCIQOptimizer,
                fdIQOptimizer,
                selfJoinSameTermIQOptimizer,
                fkOptimizer,
                belowDistinctClassUnionOptimizer);
    }
}
