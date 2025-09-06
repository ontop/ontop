package it.unibz.inf.ontop.iq.optimizer.impl;

import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.transform.IQTreeVariableGeneratorTransformer;
import it.unibz.inf.ontop.utils.VariableGenerator;

public abstract class AbstractExtendedIQOptimizer extends AbstractIQOptimizer {

    protected AbstractExtendedIQOptimizer(IntermediateQueryFactory iqFactory) {
        super(iqFactory);
    }

    @Override
    protected IQTree transformTree(IQTree tree, VariableGenerator variableGenerator) {
        IQTreeVariableGeneratorTransformer transformer = getTransformer();
        return transformer.transform(tree, variableGenerator);
    }

    abstract protected IQTreeVariableGeneratorTransformer getTransformer();
}
