package it.unibz.inf.ontop.iq.optimizer.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.BinaryNonCommutativeIQTree;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.NaryIQTree;
import it.unibz.inf.ontop.iq.UnaryIQTree;
import it.unibz.inf.ontop.iq.impl.NaryIQTreeTools;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.transform.IQTreeTransformer;
import it.unibz.inf.ontop.iq.visit.impl.AbstractCompositeIQTreeVisitingTransformer;

import java.util.Optional;

/**
 * Transformer where the cardinality does matter for the current tree
 *
 */
public abstract class AbstractBelowDistinctTransformer extends AbstractCompositeIQTreeVisitingTransformer {

    private final IQTreeTransformer lookForDistinctOrLimit1Transformer;

    protected AbstractBelowDistinctTransformer(IQTreeTransformer lookForDistinctOrLimit1Transformer,
                                               IntermediateQueryFactory iqFactory) {
        super(iqFactory, t -> t);
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
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    protected abstract Optional<IQTree> furtherTransformInnerJoin(
            InnerJoinNode rootNode, ImmutableList<IQTree> children);

    @Override
    public IQTree transformConstruction(UnaryIQTree tree, ConstructionNode rootNode, IQTree child) {
        return transformUnaryNode(tree, rootNode, child, this::transformChild);
    }

    public IQTree transformDistinct(UnaryIQTree tree, DistinctNode node, IQTree child) {
        return transformUnaryNode(tree, node, child, this::transformChild);
    }

    @Override
    public IQTree transformSlice(UnaryIQTree tree, SliceNode sliceNode, IQTree child) {
        return transformUnaryNode(tree, sliceNode, child, this::transformChild);
    }

    @Override
    public IQTree transformOrderBy(UnaryIQTree tree, OrderByNode rootNode, IQTree child) {
        return transformUnaryNode(tree, rootNode, child, this::transformChild);
    }

    @Override
    public IQTree transformFilter(UnaryIQTree tree, FilterNode rootNode, IQTree child) {
        return transformUnaryNode(tree, rootNode, child, this::transformChild);
    }

    @Override
    public IQTree transformFlatten(UnaryIQTree tree, FlattenNode node, IQTree child) {
        return transformUnaryNode(tree, node, child, this::transformChild);
    }

    @Override
    public IQTree transformAggregation(UnaryIQTree tree, AggregationNode node, IQTree child) {
        return transformUnaryNode(tree, node, child, this::transformBySearchingFromScratch);
    }

    @Override
    public IQTree transformUnion(NaryIQTree tree, UnionNode rootNode, ImmutableList<IQTree> children) {
        return transformNaryCommutativeNode(tree, rootNode, children, this::transformChild);
    }

    @Override
    public IQTree transformLeftJoin(BinaryNonCommutativeIQTree tree, LeftJoinNode rootNode, IQTree leftChild, IQTree rightChild) {
        return transformBinaryNonCommutativeNode(tree, rootNode, leftChild, rightChild, this::transformChild);
    }

    protected IQTree transformBySearchingFromScratch(IQTree tree) {
        return lookForDistinctOrLimit1Transformer.transform(tree);
    }
}
