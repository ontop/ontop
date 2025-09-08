package it.unibz.inf.ontop.iq.optimizer.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.NaryIQTree;
import it.unibz.inf.ontop.iq.UnaryIQTree;
import it.unibz.inf.ontop.iq.impl.NaryIQTreeTools;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.transform.IQTreeTransformer;
import it.unibz.inf.ontop.iq.transform.impl.DefaultRecursiveIQTreeVisitingTransformer;

import java.util.Optional;

/**
 * Transformer where the cardinality does matter for the current tree
 *
 */
public abstract class AbstractBelowDistinctTransformer extends DefaultRecursiveIQTreeVisitingTransformer {

    private final IQTreeTransformer lookForDistinctOrLimit1Transformer;

    protected AbstractBelowDistinctTransformer(IQTreeTransformer lookForDistinctOrLimit1Transformer,
                                               IntermediateQueryFactory iqFactory) {
        super(iqFactory);
        this.lookForDistinctOrLimit1Transformer = lookForDistinctOrLimit1Transformer;
    }

    @Override
    public IQTree transformInnerJoin(NaryIQTree tree, InnerJoinNode rootNode, ImmutableList<IQTree> children) {
        ImmutableList<IQTree> transformedChildren = NaryIQTreeTools.transformChildren(children, this::transformChild);

        return furtherTransformInnerJoin(rootNode, transformedChildren)
                .orElseGet(() -> withTransformedChildren(tree, transformedChildren));
    }

    /**
     * Takes account of the interaction between children
     *
     * Returns empty() if no further optimization can be applied
     *
     */
    protected abstract Optional<IQTree> furtherTransformInnerJoin(InnerJoinNode rootNode, ImmutableList<IQTree> children);

    @Override
    public IQTree transformAggregation(UnaryIQTree tree, AggregationNode node, IQTree child) {
        return transformUnaryNode(tree, node, child, this::transformBySearchingFromScratch);
    }

    private IQTree transformBySearchingFromScratch(IQTree tree) {
        return lookForDistinctOrLimit1Transformer.transform(tree);
    }
}
