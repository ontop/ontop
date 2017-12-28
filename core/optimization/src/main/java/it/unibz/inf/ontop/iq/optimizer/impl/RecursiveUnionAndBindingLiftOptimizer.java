package it.unibz.inf.ontop.iq.optimizer.impl;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.optimizer.UnionAndBindingLiftOptimizer;

@Singleton
public class RecursiveUnionAndBindingLiftOptimizer implements UnionAndBindingLiftOptimizer {

    private final IntermediateQueryFactory iqFactory;

    @Inject
    private RecursiveUnionAndBindingLiftOptimizer(IntermediateQueryFactory iqFactory) {
        this.iqFactory = iqFactory;
    }

    @Override
    public IQ optimize(IQ query) {
        return liftSomeUnions(query.liftBinding());
    }

    private IQ liftSomeUnions(IQ query) {
        IQTree liftedTree = liftSomeUnions(query.getTree());
        return liftedTree == query.getTree()
                ? query
                : iqFactory.createIQ(query.getProjectionAtom(), liftedTree);
    }

    /**
     * TODO: implement it
     */
    private IQTree liftSomeUnions(IQTree queryTree) {
        return queryTree;
    }
}
