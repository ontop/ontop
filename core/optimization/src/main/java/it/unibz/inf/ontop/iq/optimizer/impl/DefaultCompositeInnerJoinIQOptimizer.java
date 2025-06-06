package it.unibz.inf.ontop.iq.optimizer.impl;

import com.google.common.collect.ImmutableList;
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

        super(ImmutableList.of(
                selfJoinUCIQOptimizer,
                fdIQOptimizer,
                selfJoinSameTermIQOptimizer,
                fkOptimizer,
                belowDistinctClassUnionOptimizer));
    }
}
