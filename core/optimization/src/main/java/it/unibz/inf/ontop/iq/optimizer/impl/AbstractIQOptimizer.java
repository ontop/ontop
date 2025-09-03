package it.unibz.inf.ontop.iq.optimizer.impl;

import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.optimizer.IQOptimizer;
import it.unibz.inf.ontop.iq.transform.IQTreeTransformer;
import it.unibz.inf.ontop.iq.visit.IQVisitor;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.function.Function;

public abstract class AbstractIQOptimizer implements IQOptimizer {
    protected final IntermediateQueryFactory iqFactory;
    private final Function<IQ, IQ> postTransformerAction;

    protected static final Function<IQ, IQ> NO_ACTION = q -> q;
    protected static final Function<IQ, IQ> NORMALIZE_FOR_OPTIMIZATION = IQ::normalizeForOptimization;

    public AbstractIQOptimizer(IntermediateQueryFactory iqFactory, Function<IQ, IQ> postTransformerAction) {
        this.iqFactory = iqFactory;
        this.postTransformerAction = postTransformerAction;
    }


    @Override
    public IQ optimize(IQ query) {
        IQTree initialTree = query.getTree();
        IQTree newTree = transformTree(initialTree, query.getVariableGenerator());

        return newTree.equals(initialTree)
                ? query
                : postTransformerAction.apply(iqFactory.createIQ(query.getProjectionAtom(), newTree));
    }

    protected static IQTreeTransformer transformerOf(IQVisitor<IQTree> visitor) {
        return t -> t.acceptVisitor(visitor);
    }

    protected abstract IQTree transformTree(IQTree tree, VariableGenerator variableGenerator);
}
