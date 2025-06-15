package it.unibz.inf.ontop.iq.optimizer.impl;

import com.google.inject.Inject;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.optimizer.BooleanExpressionPushDownOptimizer;
import it.unibz.inf.ontop.iq.transformer.BooleanExpressionPushDownTransformer;

public class BooleanExpressionPushDownOptimizerImpl extends AbstractIQOptimizer implements BooleanExpressionPushDownOptimizer {

    private final BooleanExpressionPushDownTransformer transformer;

    @Inject
    private BooleanExpressionPushDownOptimizerImpl(BooleanExpressionPushDownTransformer transformer, IntermediateQueryFactory iqFactory) {
        super(iqFactory, NO_ACTION);
        // no equality check
        this.transformer = transformer;
    }

    @Override
    protected IQTree transformTree(IQ query) {
        return transformer.transform(query.getTree());
    }
}
