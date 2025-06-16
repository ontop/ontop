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
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.stream.Stream;

import static it.unibz.inf.ontop.iq.impl.IQTreeTools.UnaryIQTreeDecomposition;

public class FilterLifterImpl extends AbstractIQOptimizer implements FilterLifter {

    private final TermFactory termFactory;
    private final IQTreeTools iqTreeTools;
    private final Transformer transformer;

    @Inject
    private FilterLifterImpl(IntermediateQueryFactory iqFactory, TermFactory termFactory, IQTreeTools iqTreeTools) {
        // no equality check
        super(iqFactory, NO_ACTION);
        this.termFactory = termFactory;
        this.iqTreeTools = iqTreeTools;
        this.transformer = new Transformer();
    }

    @Override
    protected IQTree transformTree(IQTree tree, VariableGenerator variableGenerator) {
        return tree.acceptVisitor(transformer);
    }

    private class Transformer extends DefaultRecursiveIQTreeVisitingTransformer {

        Transformer() {
            super(FilterLifterImpl.this.iqFactory);
        }

        @Override
        public IQTree transformConstruction(UnaryIQTree tree, ConstructionNode cn, IQTree child) {

            child = transformChild(child);

            if (tree.getRootNode().equals(cn)) {
                return iqFactory.createUnaryIQTree(cn, child);
            }

            UnaryIQTreeDecomposition<FilterNode> decomposition = UnaryIQTreeDecomposition.of(child, FilterNode.class);
            return iqFactory.createUnaryIQTree(
                    decomposition.getOptionalNode()
                            .map(f -> Sets.union(f.getFilterCondition().getVariables(), cn.getVariables()).immutableCopy())
                            .map(v -> iqFactory.createConstructionNode(v, cn.getSubstitution()))
                            .orElse(cn),
                    decomposition.getTail());
        }

        @Override
        public IQTree transformFilter(UnaryIQTree tree, FilterNode filter, IQTree child) {
            child = transformChild(child);

            UnaryIQTreeDecomposition<FilterNode> decomposition = UnaryIQTreeDecomposition.of(child, FilterNode.class);
            return iqFactory.createUnaryIQTree(
                    decomposition.getOptionalNode()
                            .map(f -> termFactory.getConjunction(filter.getFilterCondition(), f.getFilterCondition()))
                            .map(iqFactory::createFilterNode)
                            .orElse(filter),
                    decomposition.getTail());
        }

        @Override
        public IQTree transformFlatten(UnaryIQTree tree, FlattenNode fn, IQTree child) {
            child = transformChild(child);

            UnaryIQTreeDecomposition<FilterNode> filter = UnaryIQTreeDecomposition.of(child, FilterNode.class);
            return iqTreeTools.unaryIQTreeBuilder()
                    .append(filter.getOptionalNode())
                    .append(fn)
                    .build(filter.getTail());
        }

        @Override
        public IQTree transformUnion(NaryIQTree tree, UnionNode rootNode, ImmutableList<IQTree> children) {

            children = NaryIQTreeTools.transformChildren(children, this::transformChild);
            var childrenDecomposition = UnaryIQTreeDecomposition.of(children, FilterNode.class);

            IQTree unionSubtree = iqTreeTools.createUnionTree(
                            childrenDecomposition.get(0).getTail().getVariables(),
                            UnaryIQTreeDecomposition.getTails(childrenDecomposition));

            var optionalFilter = iqTreeTools.createOptionalFilterNode(termFactory.getConjunction(
                    UnaryIQTreeDecomposition.getNodeStream(childrenDecomposition)
                            .map(FilterNode::getFilterCondition)));

            return iqTreeTools.unaryIQTreeBuilder()
                    .append(optionalFilter)
                    .build(unionSubtree);
        }

        @Override
        public IQTree transformInnerJoin(NaryIQTree tree, InnerJoinNode joinNode, ImmutableList<IQTree> children) {

            children = NaryIQTreeTools.transformChildren(children, this::transformChild);
            var childrenDecomposition = UnaryIQTreeDecomposition.of(children, FilterNode.class);

            NaryIQTree joinSubtree = iqTreeTools.createInnerJoinTree(
                    UnaryIQTreeDecomposition.getTails(childrenDecomposition));

            var optionalFilter = iqTreeTools.createOptionalFilterNode(termFactory.getConjunction(Stream.concat(
                    UnaryIQTreeDecomposition.getNodeStream(childrenDecomposition)
                            .map(FilterNode::getFilterCondition),
                    joinNode.getOptionalFilterCondition().stream())));

            return iqTreeTools.unaryIQTreeBuilder()
                    .append(optionalFilter)
                    .build(joinSubtree);
        }

        @Override
        public IQTree transformLeftJoin(BinaryNonCommutativeIQTree tree, LeftJoinNode rootNode, IQTree leftChild, IQTree rightChild) {
            leftChild = transformChild(leftChild);
            rightChild = transformChild(rightChild);

            var leftChildFilter = UnaryIQTreeDecomposition.of(leftChild, FilterNode.class);
            var rightChildFilter = UnaryIQTreeDecomposition.of(rightChild, FilterNode.class);

            BinaryNonCommutativeIQTree lJSubtree = iqTreeTools.createLeftJoinTree(
                    iqTreeTools.getConjunction(
                            rootNode.getOptionalFilterCondition(),
                            rightChildFilter.getOptionalNode().map(FilterNode::getFilterCondition)),
                    leftChildFilter.getTail(),
                    rightChildFilter.getTail());

            return iqTreeTools.unaryIQTreeBuilder()
                    .append(leftChildFilter.getOptionalNode())
                    .build(lJSubtree);
        }
    }

}

