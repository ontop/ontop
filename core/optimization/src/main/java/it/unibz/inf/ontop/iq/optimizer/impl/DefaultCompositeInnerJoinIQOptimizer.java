package it.unibz.inf.ontop.iq.optimizer.impl;

import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.optimizer.*;
import it.unibz.inf.ontop.iq.transform.IQTreeVariableGeneratorTransformer;

import javax.inject.Inject;

public class DefaultCompositeInnerJoinIQOptimizer extends AbstractExtendedIQOptimizer implements InnerJoinIQOptimizer {

    private final IQTreeVariableGeneratorTransformer transformer;

    @Inject
    private DefaultCompositeInnerJoinIQOptimizer(
            SelfJoinUCIQOptimizer selfJoinUCIQOptimizer,
            SelfJoinSameTermIQOptimizer selfJoinSameTermIQOptimizer,
            ArgumentTransferInnerJoinFDIQOptimizer fdIQOptimizer,
            RedundantJoinFKOptimizer fkOptimizer,
            BelowDistinctJoinWithClassUnionOptimizer belowDistinctClassUnionOptimizer,
            IntermediateQueryFactory iqFactory) {
        super(iqFactory, NO_ACTION);

        this.transformer = IQTreeVariableGeneratorTransformer.of(
                selfJoinUCIQOptimizer,
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

    @Override
    protected IQTreeVariableGeneratorTransformer getTransformer() {
        return transformer;
    }
}
