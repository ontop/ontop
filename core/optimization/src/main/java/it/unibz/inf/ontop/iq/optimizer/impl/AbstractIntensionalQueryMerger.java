package it.unibz.inf.ontop.iq.optimizer.impl;

import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.optimizer.IQOptimizer;
import it.unibz.inf.ontop.model.term.Variable;

public abstract class AbstractIntensionalQueryMerger implements IQOptimizer {

    protected final IntermediateQueryFactory iqFactory;

    protected AbstractIntensionalQueryMerger(IntermediateQueryFactory iqFactory) {
        this.iqFactory = iqFactory;
    }

    @Override
    public IQ optimize(IQ query) {
        IQTree newTree = optimize(query.getTree());
        return iqFactory.createIQ(query.getProjectionAtom(), newTree);
    }

    protected IQTree optimize(IQTree tree) {
        AbstractQueryMergingTransformer transformer = createTransformer(tree.getKnownVariables());
        return transformer.transform(tree);
    }

    protected abstract AbstractQueryMergingTransformer createTransformer(ImmutableSet<Variable> knownVariables);

}
