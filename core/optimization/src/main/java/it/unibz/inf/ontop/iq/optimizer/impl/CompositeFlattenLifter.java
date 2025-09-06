package it.unibz.inf.ontop.iq.optimizer.impl;

import com.google.inject.Inject;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.optimizer.*;
import it.unibz.inf.ontop.iq.transform.IQTreeVariableGeneratorTransformer;
import it.unibz.inf.ontop.iq.transformer.BooleanExpressionPushDownTransformer;

public class CompositeFlattenLifter extends AbstractExtendedIQOptimizer implements FlattenLifter {

    private final IQTreeVariableGeneratorTransformer transformer;

    @Inject
    private CompositeFlattenLifter(IntermediateQueryFactory iqFactory,
                                   FilterLifter filterLifter,
                                   BasicFlattenLifter flattenLifter,
                                   BooleanExpressionPushDownTransformer pushDownOptimizer) {
        super(iqFactory);
        this.transformer = IQTreeVariableGeneratorTransformer.of(
                IQTreeVariableGeneratorTransformer.of2(filterLifter),
                IQTreeVariableGeneratorTransformer.of2(flattenLifter),
                IQTreeVariableGeneratorTransformer.of2(pushDownOptimizer));
    }

    @Override
    protected IQTreeVariableGeneratorTransformer getTransformer() {
        return transformer;
    }
}

