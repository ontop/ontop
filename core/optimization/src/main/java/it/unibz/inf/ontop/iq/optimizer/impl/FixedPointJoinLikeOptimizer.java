package it.unibz.inf.ontop.iq.optimizer.impl;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.optimizer.InnerJoinIQOptimizer;
import it.unibz.inf.ontop.iq.optimizer.JoinLikeOptimizer;
import it.unibz.inf.ontop.iq.optimizer.LeftJoinIQOptimizer;
import it.unibz.inf.ontop.iq.transform.IQTreeVariableGeneratorTransformer;


@Singleton
public class FixedPointJoinLikeOptimizer extends AbstractIQOptimizer implements JoinLikeOptimizer {

    private static final int MAX_LOOP = 100;

    private final IQTreeVariableGeneratorTransformer transformer;

    @Inject
    private FixedPointJoinLikeOptimizer(IntermediateQueryFactory iqFactory,
                                        InnerJoinIQOptimizer innerJoinIQOptimizer,
                                        LeftJoinIQOptimizer leftJoinIQOptimizer) {
        super(iqFactory);
        this.transformer = IQTreeVariableGeneratorTransformer.of(
                        innerJoinIQOptimizer,
                        leftJoinIQOptimizer,
                        IQTree::normalizeForOptimization)
                .fixpoint(MAX_LOOP);
    }

    @Override
    protected IQTreeVariableGeneratorTransformer getTransformer() {
        return transformer;
    }
}
