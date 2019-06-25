package it.unibz.inf.ontop.spec.mapping.transformer.impl;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.LeafIQTree;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.transform.impl.DefaultRecursiveIQTreeVisitingTransformer;
import it.unibz.inf.ontop.spec.mapping.transformer.MappingCQCOptimizer;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

public class MappingCQCOptimizerImpl implements MappingCQCOptimizer {

    private final IntermediateQueryFactory iqFactory;

    @Inject
    public MappingCQCOptimizerImpl(IntermediateQueryFactory iqFactory) {

        this.iqFactory = iqFactory;
    }

    @Override
    public IQ optimize(IQ query) {
        query.getTree().acceptTransformer(new DefaultRecursiveIQTreeVisitingTransformer(iqFactory) {
            @Override
            public IQTree transformInnerJoin(IQTree tree, InnerJoinNode rootNode, ImmutableList<IQTree> children) {
                System.out.println("CQC Q:" + query + "\nCQC T: " + tree + "\nCQC N:" + rootNode);

                return iqFactory.createNaryIQTree(
                        rootNode,
                        children.stream()
                                .map(t -> t.acceptTransformer(this))
                                .collect(ImmutableCollectors.toList()));
            }
        });
        return query;
    }
}
