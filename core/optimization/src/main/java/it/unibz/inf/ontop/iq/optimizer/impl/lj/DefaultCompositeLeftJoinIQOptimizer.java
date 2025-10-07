package it.unibz.inf.ontop.iq.optimizer.impl.lj;

import com.google.inject.Inject;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.optimizer.LeftJoinIQOptimizer;
import it.unibz.inf.ontop.iq.transform.impl.DefaultDelegatingIQTreeVariableGeneratorTransformer;

public class DefaultCompositeLeftJoinIQOptimizer extends DefaultDelegatingIQTreeVariableGeneratorTransformer implements LeftJoinIQOptimizer {

    @Inject
    private DefaultCompositeLeftJoinIQOptimizer(
            CardinalitySensitiveJoinTransferLJOptimizer cardinalitySensitiveJoinTransferLJOptimizer,
            CardinalityInsensitiveJoinTransferLJOptimizer cardinalityInsensitiveJoinTransferLJOptimizer,
            LJWithNestingOnRightToInnerJoinOptimizer ljWithNestingOnRightToInnerJoinOptimizer,
            MergeLJOptimizer mergeLJOptimizer,
            CardinalityInsensitiveLJPruningOptimizer cardinalityInsensitiveLJPruningOptimizer,
            NullableFDSelfLJOptimizer nullableFDOptimizer) {

        super(cardinalitySensitiveJoinTransferLJOptimizer,
                IQTree::normalizeForOptimization,
                cardinalityInsensitiveJoinTransferLJOptimizer,
                IQTree::normalizeForOptimization,
                ljWithNestingOnRightToInnerJoinOptimizer,
                IQTree::normalizeForOptimization,
                mergeLJOptimizer,
                IQTree::normalizeForOptimization,
                cardinalityInsensitiveLJPruningOptimizer,
                IQTree::normalizeForOptimization,
                nullableFDOptimizer,
                IQTree::normalizeForOptimization);
    }
}
