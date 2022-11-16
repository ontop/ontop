package it.unibz.inf.ontop.iq.optimizer.impl;

import com.google.inject.Inject;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.optimizer.BooleanExpressionPushDownOptimizer;
import it.unibz.inf.ontop.iq.transformer.BooleanExpressionPushDownTransformer;

public class BooleanExpressionPushDownOptimizerImpl implements BooleanExpressionPushDownOptimizer {


    private BooleanExpressionPushDownTransformer transformer;
    private IntermediateQueryFactory iqFactory;

    @Inject
    private BooleanExpressionPushDownOptimizerImpl(BooleanExpressionPushDownTransformer transformer, IntermediateQueryFactory iqFactory) {
        this.transformer = transformer;
        this.iqFactory = iqFactory;
    }

    @Override
    public IQ optimize(IQ query) {
        return iqFactory.createIQ(
                query.getProjectionAtom(),
                transformer.transform(query.getTree())
        );
    }
}
