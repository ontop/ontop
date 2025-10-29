package it.unibz.inf.ontop.iq.optimizer.impl;

import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.optimizer.IQOptimizer;
import it.unibz.inf.ontop.iq.transform.IQTreeVariableGeneratorTransformer;
import it.unibz.inf.ontop.utils.VariableGenerator;

public abstract class AbstractIQOptimizer implements IQOptimizer {
    protected final IntermediateQueryFactory iqFactory;

    public AbstractIQOptimizer(IntermediateQueryFactory iqFactory) {
        this.iqFactory = iqFactory;
    }


    @Override
    public IQ optimize(IQ query) {
        IQTree initialTree = query.getTree();
        VariableGenerator variableGenerator = query.getVariableGenerator();
        IQTreeVariableGeneratorTransformer transformer = getTransformer();
        IQTree newTree = transformer.transform(initialTree, variableGenerator);

        return newTree.equals(initialTree)
                ? query
                : iqFactory.createIQ(query.getProjectionAtom(), newTree);
    }

    abstract protected IQTreeVariableGeneratorTransformer getTransformer();
}
