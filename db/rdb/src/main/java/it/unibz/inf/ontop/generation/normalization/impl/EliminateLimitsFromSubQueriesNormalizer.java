package it.unibz.inf.ontop.generation.normalization.impl;

import it.unibz.inf.ontop.generation.normalization.DialectExtraNormalizer;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.BinaryNonCommutativeIQTree;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.UnaryIQTree;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.transform.IQTreeTransformer;
import it.unibz.inf.ontop.iq.transform.IQTreeVariableGeneratorTransformer;
import it.unibz.inf.ontop.iq.transform.impl.AbstractDelegatingIQTreeVariableGeneratorTransformer;
import it.unibz.inf.ontop.iq.transform.impl.DefaultRecursiveIQTreeVisitingTransformer;

import javax.inject.Inject;

/**
Used to get rid of limits in sub-queries that are not necessary, for dialects like Denodo, that don't allow limits in sub-queries.
 */
public class EliminateLimitsFromSubQueriesNormalizer extends AbstractDelegatingIQTreeVariableGeneratorTransformer implements DialectExtraNormalizer {

    private final IntermediateQueryFactory iqFactory;
    private final IQTreeTransformer parentTransformer;

    @Inject
    protected EliminateLimitsFromSubQueriesNormalizer(IntermediateQueryFactory iqFactory) {
        this.iqFactory = iqFactory;
        this.parentTransformer = IQTreeTransformer.of(new Transformer());
    }

    @Override
    protected IQTreeVariableGeneratorTransformer getTransformer() {
        return IQTreeVariableGeneratorTransformer.of2(parentTransformer);
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

            var subLimitTransformer = new SubLimitTransformer(sliceNode.getLimit().getAsLong());
            return iqFactory.createUnaryIQTree(sliceNode, subLimitTransformer.transform(child));
        }
    }

    private class SubLimitTransformer extends DefaultRecursiveIQTreeVisitingTransformer {

        private final long currentBounds;

        SubLimitTransformer(long currentBounds) {
            super(EliminateLimitsFromSubQueriesNormalizer.this.iqFactory);
            this.currentBounds = currentBounds;
        }

        /**
         * On left joins, we only apply the transformation to the left child
         */
        @Override
        public IQTree transformLeftJoin(BinaryNonCommutativeIQTree tree, LeftJoinNode rootNode, IQTree leftChild, IQTree rightChild) {
            return withTransformedChildren(tree,
                    transform(leftChild),
                    parentTransformer.transform(rightChild));
        }

        /**
         * If the child slice has a lower limit than the parent, we cannot drop it
         * We once again only perform this normalization if there is no OFFSET
         */
        @Override
        public IQTree transformSlice(UnaryIQTree tree, SliceNode sliceNode, IQTree child) {
            if (sliceNode.getOffset() != 0 || sliceNode.getLimit().isEmpty() || sliceNode.getLimit().getAsLong() < currentBounds)
                return parentTransformer.transform(tree);

            return transform(child);
        }

        @Override
        public IQTree transformOrderBy(UnaryIQTree tree, OrderByNode rootNode, IQTree child) {
            return parentTransformer.transform(tree);
        }

        @Override
        public IQTree transformDistinct(UnaryIQTree tree, DistinctNode rootNode, IQTree child) {
            return parentTransformer.transform(tree);
        }

        @Override
        public IQTree transformFilter(UnaryIQTree tree, FilterNode rootNode, IQTree child) {
            return parentTransformer.transform(tree);
        }

        @Override
        public IQTree transformFlatten(UnaryIQTree tree, FlattenNode rootNode, IQTree child) {
            return parentTransformer.transform(tree);
        }

        @Override
        public IQTree transformAggregation(UnaryIQTree tree, AggregationNode rootNode, IQTree child) {
            return parentTransformer.transform(tree);
        }
    }
}
