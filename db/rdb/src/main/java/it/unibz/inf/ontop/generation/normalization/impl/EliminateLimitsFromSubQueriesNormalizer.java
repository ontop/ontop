package it.unibz.inf.ontop.generation.normalization.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.generation.normalization.DialectExtraNormalizer;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.BinaryNonCommutativeIQTree;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.NaryIQTree;
import it.unibz.inf.ontop.iq.UnaryIQTree;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.transform.impl.DefaultRecursiveIQTreeVisitingTransformer;
import it.unibz.inf.ontop.utils.VariableGenerator;

import javax.inject.Inject;

/**
Used to get rid of limits in sub-queries that are not necessary, for dialects like Denodo, that don't allow limits in sub-queries.
 */
public class EliminateLimitsFromSubQueriesNormalizer implements DialectExtraNormalizer {

    private final IntermediateQueryFactory iqFactory;
    private final Transformer transformer;

    @Inject
    protected EliminateLimitsFromSubQueriesNormalizer(IntermediateQueryFactory iqFactory) {
        this.iqFactory = iqFactory;
        this.transformer = new Transformer();
    }

    @Override
    public IQTree transform(IQTree tree, VariableGenerator variableGenerator) {
        return tree.acceptVisitor(transformer);
    }

    private class Transformer extends DefaultRecursiveIQTreeVisitingTransformer {
        Transformer() {
            super(EliminateLimitsFromSubQueriesNormalizer.this.iqFactory);
        }

        @Override
        public IQTree transformSlice(UnaryIQTree tree, SliceNode sliceNode, IQTree child) {
            //We only perform this normalization if there is no OFFSET
            if (sliceNode.getOffset() != 0 || sliceNode.getLimit().isEmpty())
                return super.transformSlice(tree, sliceNode, child);

            var subLimitTransformer = new SubLimitTransformer(sliceNode.getLimit().get(), this);

            return iqFactory.createUnaryIQTree(sliceNode, child.acceptVisitor(subLimitTransformer));
        }
    }

    /**
     * Calling super.transform[...] continues in this transformer, normal transform calls are redirected to the parent transformer.
     */
    private class SubLimitTransformer extends DefaultRecursiveIQTreeVisitingTransformer {

        private final DefaultRecursiveIQTreeVisitingTransformer eliminateLimitsFromSubQueriesNormalizer;
        private final long currentBounds;

        protected SubLimitTransformer(long currentBounds, DefaultRecursiveIQTreeVisitingTransformer eliminateLimitsFromSubQueriesNormalizer) {
            super(EliminateLimitsFromSubQueriesNormalizer.this.iqFactory);
            this.eliminateLimitsFromSubQueriesNormalizer = eliminateLimitsFromSubQueriesNormalizer;
            this.currentBounds = currentBounds;
        }

        /**If the child slice has a lower limit than the parent, we cannot drop it
        *We once again only perform this normalization if there is no OFFSET
         * */
        @Override
        public IQTree transformSlice(UnaryIQTree tree, SliceNode sliceNode, IQTree child) {
            if (sliceNode.getOffset() != 0 || sliceNode.getLimit().isEmpty() || sliceNode.getLimit().get() < currentBounds)
                return tree.acceptVisitor(eliminateLimitsFromSubQueriesNormalizer);
            return transformChild(tree.getChildren().get(0));
        }

        /**
         * On left joins, we only apply the transformation to the left child
        */
        @Override
        public IQTree transformLeftJoin(BinaryNonCommutativeIQTree tree, LeftJoinNode rootNode, IQTree leftChild, IQTree rightChild) {
            var leftSubTree = transformChild(tree.getLeftChild());
            var rightSubTree = tree.getRightChild().acceptVisitor(eliminateLimitsFromSubQueriesNormalizer);
            if (leftSubTree.equals(tree.getLeftChild()) && rightSubTree.equals(tree.getRightChild()))
                return tree;
            return iqFactory.createBinaryNonCommutativeIQTree(tree.getRootNode(), leftSubTree, rightSubTree);
        }

        /**
         * On inner joins, unions and constructions, we keep going inside this normalizer.
        */
        @Override
        public IQTree transformInnerJoin(NaryIQTree tree, InnerJoinNode rootNode, ImmutableList<IQTree> children) {
            return super.transformNaryCommutativeNode(tree, rootNode, children);
        }

        @Override
        public IQTree transformUnion(NaryIQTree tree, UnionNode rootNode, ImmutableList<IQTree> children) {
            return super.transformNaryCommutativeNode(tree, rootNode, children);
        }

        @Override
        public IQTree transformConstruction(UnaryIQTree tree, ConstructionNode rootNode, IQTree child) {
            return super.transformUnaryNode(tree, rootNode, child);
        }

        /**
         * All other nodes are not modified and passed back to the original normalizer to continue.
         * This includes ORDER BY, DISTINCT, FILTER and more
        */
        @Override
        protected IQTree transformUnaryNode(UnaryIQTree tree, UnaryOperatorNode rootNode, IQTree child) {
            return tree.acceptVisitor(eliminateLimitsFromSubQueriesNormalizer);
        }

        @Override
        protected IQTree transformBinaryNonCommutativeNode(BinaryNonCommutativeIQTree tree, BinaryNonCommutativeOperatorNode rootNode, IQTree leftChild, IQTree rightChild) {
            return tree.acceptVisitor(eliminateLimitsFromSubQueriesNormalizer);
        }

        @Override
        protected IQTree transformNaryCommutativeNode(NaryIQTree tree, NaryOperatorNode rootNode, ImmutableList<IQTree> children) {
            return tree.acceptVisitor(eliminateLimitsFromSubQueriesNormalizer);
        }
    }
}
