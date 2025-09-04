package it.unibz.inf.ontop.iq.optimizer.impl;

import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.transform.IQTreeVariableGeneratorTransformer;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.function.Function;

public abstract class AbstractExtendedIQOptimizer extends AbstractIQOptimizer {
    protected AbstractExtendedIQOptimizer(IntermediateQueryFactory iqFactory, Function<IQ, IQ> postTransformerAction) {
        super(iqFactory, postTransformerAction);
    }

    @Override
    protected IQTree transformTree(IQTree tree, VariableGenerator variableGenerator) {
        IQTreeVariableGeneratorTransformer transformer = getTransformer();
        return transformer.transform(tree, variableGenerator);
    }

    abstract protected IQTreeVariableGeneratorTransformer getTransformer();
}
