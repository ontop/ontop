package it.unibz.inf.ontop.iq.optimizer.impl;

import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.optimizer.IQOptimizer;
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
        IQVisitor<IQTree> transformer = getTransformer(query);

        IQ before = preTransformerAction.apply(query);
        IQTree newTree = before.getTree().acceptVisitor(transformer);

        return newTree.equals(query.getTree())
                ? query
                : postTransformerAction.apply(iqFactory.createIQ(query.getProjectionAtom(), newTree));
    }

    protected abstract IQVisitor<IQTree> getTransformer(IQ query);
}
