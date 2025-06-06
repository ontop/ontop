package it.unibz.inf.ontop.iq.optimizer.impl.lj;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import it.unibz.inf.ontop.iq.optimizer.LeftJoinIQOptimizer;
import it.unibz.inf.ontop.iq.optimizer.impl.CompositeIQOptimizer;

public class DefaultCompositeLeftJoinIQOptimizer extends CompositeIQOptimizer implements LeftJoinIQOptimizer {

    @Inject
    private DefaultCompositeLeftJoinIQOptimizer(
            CardinalitySensitiveJoinTransferLJOptimizer cardinalitySensitiveJoinTransferLJOptimizer,
            CardinalityInsensitiveJoinTransferLJOptimizer cardinalityInsensitiveJoinTransferLJOptimizer,
            LJWithNestingOnRightToInnerJoinOptimizer ljWithNestingOnRightToInnerJoinOptimizer,
            MergeLJOptimizer mergeLJOptimizer,
            CardinalityInsensitiveLJPruningOptimizer cardinalityInsensitiveLJPruningOptimizer,
            NullableFDSelfLJOptimizer nullableFDOptimizer) {

        super(ImmutableList.of(
                cardinalitySensitiveJoinTransferLJOptimizer,
                cardinalityInsensitiveJoinTransferLJOptimizer,
                ljWithNestingOnRightToInnerJoinOptimizer,
                mergeLJOptimizer,
                cardinalityInsensitiveLJPruningOptimizer,
                nullableFDOptimizer));
    }
}
