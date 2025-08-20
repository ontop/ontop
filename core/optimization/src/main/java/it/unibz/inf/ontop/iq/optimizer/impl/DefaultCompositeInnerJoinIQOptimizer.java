package it.unibz.inf.ontop.iq.optimizer.impl;

import it.unibz.inf.ontop.iq.optimizer.*;

import javax.inject.Inject;

public class DefaultCompositeInnerJoinIQOptimizer extends CompositeIQOptimizer implements InnerJoinIQOptimizer {

    @Inject
    private DefaultCompositeInnerJoinIQOptimizer(
            SelfJoinUCIQOptimizerImpl selfJoinUCIQOptimizer,
            SelfJoinSameTermIQOptimizerImpl selfJoinSameTermIQOptimizer,
            ArgumentTransferInnerJoinFDIQOptimizer fdIQOptimizer,
            RedundantJoinFKOptimizerImpl fkOptimizer,
            BelowDistinctJoinWithClassUnionOptimizerImpl belowDistinctClassUnionOptimizer) {

        super(selfJoinUCIQOptimizer,
                fdIQOptimizer,
                selfJoinSameTermIQOptimizer,
                fkOptimizer,
                belowDistinctClassUnionOptimizer);
    }
}
