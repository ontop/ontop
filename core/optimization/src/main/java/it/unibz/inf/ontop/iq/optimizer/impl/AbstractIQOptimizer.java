package it.unibz.inf.ontop.iq.optimizer.impl;

import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.optimizer.IQOptimizer;
import it.unibz.inf.ontop.iq.transform.IQTreeTransformer;
import it.unibz.inf.ontop.iq.transform.impl.IQTreeTransformerAdapter;
import it.unibz.inf.ontop.iq.visit.IQVisitor;

import java.util.function.Function;

public abstract class AbstractIQOptimizer implements IQOptimizer {
    protected final IntermediateQueryFactory iqFactory;
    private final Function<IQ, IQ> postTransformerAction;
    private final Function<IQ, IQ> preTransformerAction;

    protected static final Function<IQ, IQ> NO_ACTION = q -> q;
    protected static final Function<IQ, IQ> NORMALIZE_FOR_OPTIMIZATION = IQ::normalizeForOptimization;

    public AbstractIQOptimizer(IntermediateQueryFactory iqFactory, Function<IQ, IQ> postTransformerAction) {
        this(iqFactory, NO_ACTION, postTransformerAction);
    }

    public AbstractIQOptimizer(IntermediateQueryFactory iqFactory, Function<IQ, IQ> preTransformerAction, Function<IQ, IQ> postTransformerAction) {
        this.iqFactory = iqFactory;
        this.preTransformerAction = preTransformerAction;
        this.postTransformerAction = postTransformerAction;
    }

    @Override
    public IQ optimize(IQ query) {
        IQ before = preTransformerAction.apply(query);
        IQTree newTree = transformTree(before);

        return newTree.equals(query.getTree())
                ? query
                : postTransformerAction.apply(iqFactory.createIQ(query.getProjectionAtom(), newTree));
    }

    protected static IQTreeTransformer transformerOf(IQVisitor<IQTree> visitor) {
        return new IQTreeTransformerAdapter(visitor);
    }

    protected abstract IQTree transformTree(IQ query);
}
