package it.unibz.inf.ontop.iq.optimizer.impl.lj;

import com.google.inject.Inject;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.optimizer.LeftJoinIQOptimizer;
import it.unibz.inf.ontop.iq.optimizer.impl.AbstractExtendedIQOptimizer;
import it.unibz.inf.ontop.iq.transform.IQTreeVariableGeneratorTransformer;

public class DefaultCompositeLeftJoinIQOptimizer extends AbstractExtendedIQOptimizer implements LeftJoinIQOptimizer {

    private final IQTreeVariableGeneratorTransformer transformer;

    @Inject
    private DefaultCompositeLeftJoinIQOptimizer(
            CardinalitySensitiveJoinTransferLJOptimizer cardinalitySensitiveJoinTransferLJOptimizer,
            CardinalityInsensitiveJoinTransferLJOptimizer cardinalityInsensitiveJoinTransferLJOptimizer,
            LJWithNestingOnRightToInnerJoinOptimizer ljWithNestingOnRightToInnerJoinOptimizer,
            MergeLJOptimizer mergeLJOptimizer,
            CardinalityInsensitiveLJPruningOptimizer cardinalityInsensitiveLJPruningOptimizer,
            NullableFDSelfLJOptimizer nullableFDOptimizer,
            IntermediateQueryFactory iqFactory) {
        super(iqFactory, NO_ACTION);

        this.transformer = IQTreeVariableGeneratorTransformer.of(cardinalitySensitiveJoinTransferLJOptimizer,
                cardinalityInsensitiveJoinTransferLJOptimizer,
                ljWithNestingOnRightToInnerJoinOptimizer,
                mergeLJOptimizer,
                cardinalityInsensitiveLJPruningOptimizer,
                nullableFDOptimizer);
    }

    @Override
    protected IQTreeVariableGeneratorTransformer getTransformer() {
        return transformer;
    }
}
