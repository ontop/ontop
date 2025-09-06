package it.unibz.inf.ontop.iq.optimizer.impl;

import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.optimizer.*;
import it.unibz.inf.ontop.iq.transform.impl.CompositeIQTreeVariableGeneratorTransformer;

import javax.inject.Inject;

public class DefaultCompositeInnerJoinIQOptimizer extends CompositeIQTreeVariableGeneratorTransformer implements InnerJoinIQOptimizer {

    @Inject
    private DefaultCompositeInnerJoinIQOptimizer(
            SelfJoinUCIQOptimizer selfJoinUCIQOptimizer,
            SelfJoinSameTermIQOptimizer selfJoinSameTermIQOptimizer,
            ArgumentTransferInnerJoinFDIQOptimizer fdIQOptimizer,
            RedundantJoinFKOptimizer fkOptimizer,
            BelowDistinctJoinWithClassUnionOptimizer belowDistinctClassUnionOptimizer) {

        super(selfJoinUCIQOptimizer,
                IQTree::normalizeForOptimization,
                fdIQOptimizer,
                IQTree::normalizeForOptimization,
                selfJoinSameTermIQOptimizer,
                IQTree::normalizeForOptimization,
                fkOptimizer,
                IQTree::normalizeForOptimization,
                belowDistinctClassUnionOptimizer,
                IQTree::normalizeForOptimization);
    }
}
