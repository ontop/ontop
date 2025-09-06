package it.unibz.inf.ontop.iq.optimizer.impl;

import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.optimizer.IQOptimizer;
import it.unibz.inf.ontop.utils.VariableGenerator;

public abstract class AbstractIQOptimizer implements IQOptimizer {
    protected final IntermediateQueryFactory iqFactory;

    public AbstractIQOptimizer(IntermediateQueryFactory iqFactory) {
        this.iqFactory = iqFactory;
    }


    @Override
    public IQ optimize(IQ query) {
        IQTree initialTree = query.getTree();
        IQTree newTree = transformTree(initialTree, query.getVariableGenerator());

        return newTree.equals(initialTree)
                ? query
                : iqFactory.createIQ(query.getProjectionAtom(), newTree);
    }

    protected abstract IQTree transformTree(IQTree tree, VariableGenerator variableGenerator);
}
