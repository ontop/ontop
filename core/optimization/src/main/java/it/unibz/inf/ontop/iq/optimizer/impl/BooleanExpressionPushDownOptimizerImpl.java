package it.unibz.inf.ontop.iq.optimizer.impl;

import com.google.inject.Inject;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.optimizer.BooleanExpressionPushDownOptimizer;
import it.unibz.inf.ontop.iq.transform.IQTreeVariableGeneratorTransformer;
import it.unibz.inf.ontop.iq.transformer.BooleanExpressionPushDownTransformer;

public class BooleanExpressionPushDownOptimizerImpl extends AbstractExtendedIQOptimizer implements BooleanExpressionPushDownOptimizer {

    private final IQTreeVariableGeneratorTransformer transformer;

    @Inject
    private BooleanExpressionPushDownOptimizerImpl(BooleanExpressionPushDownTransformer transformer, IntermediateQueryFactory iqFactory) {
        super(iqFactory, NO_ACTION);
        // no equality check
        this.transformer = IQTreeVariableGeneratorTransformer.of2(transformer);
    }

    @Override
    protected IQTreeVariableGeneratorTransformer getTransformer() {
        return transformer;
    }
}
