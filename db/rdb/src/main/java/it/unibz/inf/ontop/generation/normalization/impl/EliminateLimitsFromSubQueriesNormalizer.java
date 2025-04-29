package it.unibz.inf.ontop.generation.normalization.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.generation.normalization.DialectExtraNormalizer;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.UnaryIQTree;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.transform.impl.DefaultRecursiveIQTreeExtendedTransformer;
import it.unibz.inf.ontop.model.term.NonGroundTerm;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import javax.inject.Inject;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
Used to get rid of limits in sub-queries that are not necessary, for dialects like Denodo, that don't allow limits in sub-queries.
 */
public class EliminateLimitsFromSubQueriesNormalizer implements DialectExtraNormalizer {

    private final CoreSingletons coreSingletons;

    @Inject
    protected EliminateLimitsFromSubQueriesNormalizer(CoreSingletons coreSingletons) {
        this.coreSingletons = coreSingletons;
    }

    @Override
    public IQTree transform(IQTree tree, VariableGenerator variableGenerator) {
        return tree.acceptTransformer(new DefaultRecursiveIQTreeExtendedTransformer<VariableGenerator>(coreSingletons) {
            @Override
            public IQTree transformSlice(IQTree tree, SliceNode sliceNode, IQTree child, VariableGenerator context) {
                //We only perform this normalization if there is no OFFSET
                if(sliceNode.getOffset() != 0 || sliceNode.getLimit().isEmpty())
                    return super.transformSlice(tree, sliceNode, child, context);

                var subLimitTransformer = new SubLimitTransformer(sliceNode.getLimit().get(), this, coreSingletons);

                return iqFactory.createUnaryIQTree(sliceNode, child.acceptTransformer(subLimitTransformer, context));
            }
        }, variableGenerator);
    }

    /**
     * Calling super.transform[...] continues in this transformer, normal transform calls are redirected to the parent transformer.
     */
    private static class SubLimitTransformer extends DefaultRecursiveIQTreeExtendedTransformer<VariableGenerator> {

        private final DefaultRecursiveIQTreeExtendedTransformer<VariableGenerator> eliminateLimitsFromSubQueriesNormalizer;
        private final long currentBounds;

        protected SubLimitTransformer(long currentBounds, DefaultRecursiveIQTreeExtendedTransformer<VariableGenerator> eliminateLimitsFromSubQueriesNormalizer,
                                      CoreSingletons coreSingletons) {
            super(coreSingletons);
            this.eliminateLimitsFromSubQueriesNormalizer = eliminateLimitsFromSubQueriesNormalizer;
            this.currentBounds = currentBounds;
        }

        /**If the child slice has a lower limit than the parent, we cannot drop it
        *We once again only perform this normalization if there is no OFFSET
         * */
        @Override
        public IQTree transformSlice(IQTree tree, SliceNode sliceNode, IQTree child, VariableGenerator context) {
            if(sliceNode.getOffset() != 0 || sliceNode.getLimit().isEmpty() || sliceNode.getLimit().get() < currentBounds)
                return eliminateLimitsFromSubQueriesNormalizer.transform(tree, context);
            return transform(tree.getChildren().get(0), context);
        }

        /**
         * On left joins, we only apply the transformation to the left child
        */
        @Override
        public IQTree transformLeftJoin(IQTree tree, LeftJoinNode rootNode, IQTree leftChild, IQTree rightChild, VariableGenerator context) {
            var leftSubTree = transform(tree.getChildren().get(0), context);
            var rightSubTree = eliminateLimitsFromSubQueriesNormalizer.transform(tree.getChildren().get(1), context);
            if(leftSubTree.equals(tree.getChildren().get(0)) && rightSubTree.equals(tree.getChildren().get(1)))
                return tree;
            return iqFactory.createBinaryNonCommutativeIQTree((LeftJoinNode)tree.getRootNode(), leftSubTree, rightSubTree);
        }

        /**
         * On inner joins, unions and constructions, we keep going inside this normalizer.
        */
        @Override
        public IQTree transformInnerJoin(IQTree tree, InnerJoinNode rootNode, ImmutableList<IQTree> children, VariableGenerator context) {
            return super.transformNaryCommutativeNode(tree, rootNode, children, context);
        }

        @Override
        public IQTree transformUnion(IQTree tree, UnionNode rootNode, ImmutableList<IQTree> children, VariableGenerator context) {
            return super.transformNaryCommutativeNode(tree, rootNode, children, context);
        }

        @Override
        public IQTree transformConstruction(IQTree tree, ConstructionNode rootNode, IQTree child, VariableGenerator context) {
            return super.transformUnaryNode(tree, rootNode, child, context);
        }

        /**
         * All other nodes are not modified and passed back to the original normalizer to continue.
         * This includes ORDER BY, DISTINCT, FILTER and more
        */
        @Override
        protected IQTree transformUnaryNode(IQTree tree, UnaryOperatorNode rootNode, IQTree child, VariableGenerator context) {
            return eliminateLimitsFromSubQueriesNormalizer.transform(tree, context);
        }

        @Override
        protected IQTree transformBinaryNonCommutativeNode(IQTree tree, BinaryNonCommutativeOperatorNode rootNode, IQTree leftChild, IQTree rightChild, VariableGenerator context) {
            return eliminateLimitsFromSubQueriesNormalizer.transform(tree, context);
        }

        @Override
        protected IQTree transformNaryCommutativeNode(IQTree tree, NaryOperatorNode rootNode, ImmutableList<IQTree> children, VariableGenerator context) {
            return eliminateLimitsFromSubQueriesNormalizer.transform(tree, context);
        }
    }
}
