package it.unibz.inf.ontop.iq.optimizer.impl;

import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.UnaryIQTree;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.transform.IQTreeTransformer;

/**
 * Transformer where the cardinality does matter for the current tree
 *
 */
public class BelowDistinctTransformer extends DefaultRecursiveIQTreeVisitingInnerJoinTransformer {

    private final IQTreeTransformer lookForDistinctOrLimit1Transformer;

    protected BelowDistinctTransformer(IQTreeTransformer lookForDistinctOrLimit1Transformer,
                                       IntermediateQueryFactory iqFactory,
                                       InnerJoinTransformer innerJoinTransformer) {
        super(iqFactory, innerJoinTransformer);
        this.lookForDistinctOrLimit1Transformer = lookForDistinctOrLimit1Transformer;
    }

    @Override
    public IQTree transformAggregation(UnaryIQTree tree, AggregationNode node, IQTree child) {
        return transformUnaryNode(tree, node, child, lookForDistinctOrLimit1Transformer::transform);
    }
}
