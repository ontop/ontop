package it.unibz.inf.ontop.iq.optimizer.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.*;
import it.unibz.inf.ontop.iq.impl.IQTreeTools;
import it.unibz.inf.ontop.iq.impl.NaryIQTreeTools;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.optimizer.FilterLifter;
import it.unibz.inf.ontop.iq.transform.impl.DefaultRecursiveIQTreeVisitingTransformer;
import it.unibz.inf.ontop.model.term.TermFactory;

import java.util.stream.Stream;

import static it.unibz.inf.ontop.iq.impl.IQTreeTools.UnaryIQTreeDecomposition;

public class FilterLifterImpl implements FilterLifter {

    private final IntermediateQueryFactory iqFactory;
    private final TermFactory termFactory;
    private final IQTreeTools iqTreeTools;
    private final Transformer transformer;

    @Inject
    private FilterLifterImpl(IntermediateQueryFactory iqFactory, TermFactory termFactory, IQTreeTools iqTreeTools) {
        // no equality check
        this.iqFactory = iqFactory;
        this.termFactory = termFactory;
        this.iqTreeTools = iqTreeTools;
        this.transformer = new Transformer();
    }

    @Override
    public IQTree transform(IQTree tree) {
        return tree.acceptVisitor(transformer);
    }

    private class Transformer extends DefaultRecursiveIQTreeVisitingTransformer {

        Transformer() {
            super(FilterLifterImpl.this.iqFactory);
        }

        @Override
        public IQTree transformConstruction(UnaryIQTree tree, ConstructionNode cn, IQTree child) {
            IQTree transformedChild = transformChild(child);

            if (tree.getRootNode().equals(cn)) {
                return iqFactory.createUnaryIQTree(cn, transformedChild);
            }

            // TODO: this code is unreachable - check the intention
            var filter = UnaryIQTreeDecomposition.of(transformedChild, FilterNode.class);
            return iqFactory.createUnaryIQTree(
                    filter.getOptionalNode()
                            .map(f -> Sets.union(f.getFilterCondition().getVariables(), cn.getVariables()).immutableCopy())
                            .map(v -> iqFactory.createConstructionNode(v, cn.getSubstitution()))
                            .orElse(cn),
                    filter.getTail());
        }

        @Override
        public IQTree transformFilter(UnaryIQTree tree, FilterNode rootNode, IQTree child) {
            IQTree transformedChild = transformChild(child);
            var filter = UnaryIQTreeDecomposition.of(transformedChild, FilterNode.class);

            return iqFactory.createUnaryIQTree(
                    filter.getOptionalNode()
                            .map(f -> termFactory.getConjunction(rootNode.getFilterCondition(), f.getFilterCondition()))
                            .map(iqFactory::createFilterNode)
                            .orElse(rootNode),
                    filter.getTail());
        }

        @Override
        public IQTree transformFlatten(UnaryIQTree tree, FlattenNode fn, IQTree child) {
            IQTree transformedChild = transformChild(child);
            var filter = UnaryIQTreeDecomposition.of(transformedChild, FilterNode.class);
            // TODO: check why this is sound
            return iqTreeTools.unaryIQTreeBuilder()
                    .append(filter.getOptionalNode())
                    .append(fn)
                    .build(filter.getTail());
        }

        @Override
        public IQTree transformUnion(NaryIQTree tree, UnionNode rootNode, ImmutableList<IQTree> children) {
            ImmutableList<IQTree> transformedChildren = NaryIQTreeTools.transformChildren(children, this::transformChild);
            var filters = UnaryIQTreeDecomposition.of(transformedChildren, FilterNode.class);

            // TODO: check why this is sound
            var optionalFilter = iqTreeTools.createOptionalFilterNode(termFactory.getConjunction(
                    UnaryIQTreeDecomposition.getNodeStream(filters)
                            .map(FilterNode::getFilterCondition)));

            IQTree unionSubtree = iqTreeTools.createUnionTree(
                            filters.get(0).getTail().getVariables(),
                            UnaryIQTreeDecomposition.getTails(filters));

            return iqTreeTools.unaryIQTreeBuilder()
                    .append(optionalFilter)
                    .build(unionSubtree);
        }

        @Override
        public IQTree transformInnerJoin(NaryIQTree tree, InnerJoinNode joinNode, ImmutableList<IQTree> children) {
            ImmutableList<IQTree> transformedChildren = NaryIQTreeTools.transformChildren(children, this::transformChild);
            var filters = UnaryIQTreeDecomposition.of(transformedChildren, FilterNode.class);

            var optionalFilter = iqTreeTools.createOptionalFilterNode(termFactory.getConjunction(Stream.concat(
                    UnaryIQTreeDecomposition.getNodeStream(filters)
                            .map(FilterNode::getFilterCondition),
                    joinNode.getOptionalFilterCondition().stream())));

            NaryIQTree joinSubtree = iqTreeTools.createInnerJoinTree(
                    // no condition!
                    UnaryIQTreeDecomposition.getTails(filters));

            return iqTreeTools.unaryIQTreeBuilder()
                    .append(optionalFilter)
                    .build(joinSubtree);
        }

        @Override
        public IQTree transformLeftJoin(BinaryNonCommutativeIQTree tree, LeftJoinNode rootNode, IQTree leftChild, IQTree rightChild) {
            IQTree transformedLeftChild = transformChild(leftChild);
            IQTree transformedRightChild = transformChild(rightChild);

            var leftFilter = UnaryIQTreeDecomposition.of(transformedLeftChild, FilterNode.class);
            var rightFilter = UnaryIQTreeDecomposition.of(transformedRightChild, FilterNode.class);

            BinaryNonCommutativeIQTree lJSubtree = iqTreeTools.createLeftJoinTree(
                    iqTreeTools.getConjunction(
                            rootNode.getOptionalFilterCondition(),
                            rightFilter.getOptionalNode().map(FilterNode::getFilterCondition)),
                    leftFilter.getTail(),
                    rightFilter.getTail());

            return iqTreeTools.unaryIQTreeBuilder()
                    .append(leftFilter.getOptionalNode())
                    .build(lJSubtree);
        }
    }
}

