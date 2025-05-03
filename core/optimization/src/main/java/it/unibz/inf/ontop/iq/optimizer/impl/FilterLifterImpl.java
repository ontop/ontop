package it.unibz.inf.ontop.iq.optimizer.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.*;
import it.unibz.inf.ontop.iq.impl.IQTreeTools;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.optimizer.FilterLifter;
import it.unibz.inf.ontop.iq.transform.impl.DefaultRecursiveIQTreeVisitingTransformer;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.Variable;

import java.util.Optional;
import java.util.stream.Stream;

import static it.unibz.inf.ontop.iq.impl.IQTreeTools.UnaryIQTreeDecomposition;

public class FilterLifterImpl implements FilterLifter {

    private final IntermediateQueryFactory iqFactory;
    private final TermFactory termFactory;
    private final IQTreeTools iqTreeTools;

    @Inject
    private FilterLifterImpl(IntermediateQueryFactory iqFactory, TermFactory termFactory, IQTreeTools iqTreeTools) {
        this.iqFactory = iqFactory;
        this.termFactory = termFactory;
        this.iqTreeTools = iqTreeTools;
    }

    @Override
    public IQ optimize(IQ query) {
        TreeTransformer treeTransformer = new TreeTransformer();
        return iqFactory.createIQ(
                query.getProjectionAtom(),
                query.getTree().acceptTransformer(treeTransformer));
    }

    private class TreeTransformer extends DefaultRecursiveIQTreeVisitingTransformer {

        TreeTransformer() {
            super(FilterLifterImpl.this.iqFactory);
        }

        @Override
        public IQTree transformConstruction(IQTree tree, ConstructionNode cn, IQTree child) {

            child = transformChild(child);

            if (tree.getRootNode().equals(cn)) {
                return iqFactory.createUnaryIQTree(cn, child);
            }

            UnaryIQTreeDecomposition<FilterNode> decomposition = UnaryIQTreeDecomposition.of(child, FilterNode.class);

            if (decomposition.isPresent()) {
                FilterNode filter = decomposition.get();
                ImmutableSet<Variable> projectedVars = Sets.union(filter.getFilterCondition().getVariables(), cn.getVariables()).immutableCopy();

                ConstructionNode updatedCn = iqFactory.createConstructionNode(projectedVars, cn.getSubstitution());
                return iqFactory.createUnaryIQTree(
                        filter,
                        iqFactory.createUnaryIQTree(updatedCn, decomposition.getChild()));
            }
            return iqFactory.createUnaryIQTree(cn, child);
        }

        @Override
        public IQTree transformFilter(IQTree tree, FilterNode filter, IQTree child) {

            child = transformChild(child);
            UnaryIQTreeDecomposition<FilterNode> decomposition = UnaryIQTreeDecomposition.of(child, FilterNode.class);

            return iqFactory.createUnaryIQTree(
                    decomposition.map(f -> termFactory.getConjunction(
                                            filter.getFilterCondition(),
                                            f.getFilterCondition()))
                            .map(iqFactory::createFilterNode)
                            .orElse(filter),
                    decomposition.getChild());
        }

        @Override
        public IQTree transformFlatten(IQTree tree, FlattenNode fn, IQTree child) {

            child = transformChild(child);
            UnaryIQTreeDecomposition<FilterNode> decomposition = UnaryIQTreeDecomposition.of(child, FilterNode.class);

            if (decomposition.isPresent()) {
                FilterNode filter = decomposition.get();
                return iqFactory.createUnaryIQTree(
                        filter,
                        iqFactory.createUnaryIQTree(fn, decomposition.getChild()));
            }
            return iqFactory.createUnaryIQTree(fn, child);
        }

        @Override
        public IQTree transformUnion(IQTree tree, UnionNode rootNode, ImmutableList<IQTree> children) {

            children = transformChildren(children);
            var childrenDecomposition = UnaryIQTreeDecomposition.of(children, FilterNode.class);

            NaryIQTree unionSubtree = iqFactory.createNaryIQTree(
                    iqFactory.createUnionNode(childrenDecomposition.get(0).getChild().getVariables()),
                    UnaryIQTreeDecomposition.getChildren(childrenDecomposition));

            Optional<ImmutableExpression> childrenExpression = termFactory.getConjunction(
                    getChildrenExpression(childrenDecomposition));

            return iqTreeTools.createOptionalUnaryIQTree(
                    childrenExpression.map(iqFactory::createFilterNode),
                    unionSubtree);
        }

        @Override
        public IQTree transformInnerJoin(IQTree tree, InnerJoinNode joinNode, ImmutableList<IQTree> children) {

            children = transformChildren(children);
            var childrenDecomposition = UnaryIQTreeDecomposition.of(children, FilterNode.class);

            NaryIQTree joinSubtree = iqFactory.createNaryIQTree(
                    iqFactory.createInnerJoinNode(),
                    UnaryIQTreeDecomposition.getChildren(childrenDecomposition));

            Stream<ImmutableExpression> conjuncts = Stream.concat(
                            getChildrenExpression(childrenDecomposition),
                            joinNode.getOptionalFilterCondition().stream());

            return iqTreeTools.createOptionalUnaryIQTree(
                    termFactory.getConjunction(conjuncts)
                            .map(iqFactory::createFilterNode),
                    joinSubtree);
        }

        @Override
        public IQTree transformLeftJoin(IQTree tree, LeftJoinNode rootNode, IQTree leftChild, IQTree rightChild) {
            leftChild = transformChild(leftChild);
            rightChild = transformChild(rightChild);

            UnaryIQTreeDecomposition<FilterNode> leftChildDecomposition = UnaryIQTreeDecomposition.of(leftChild, FilterNode.class);
            UnaryIQTreeDecomposition<FilterNode> rightChildDecomposition = UnaryIQTreeDecomposition.of(rightChild, FilterNode.class);

            LeftJoinNode updatedLJ = rightChildDecomposition
                    .map(f -> termFactory.getConjunction(rootNode.getOptionalFilterCondition(), Stream.of(f.getFilterCondition())))
                    .map(iqFactory::createLeftJoinNode)
                    .orElse(rootNode);

            BinaryNonCommutativeIQTree lJSubtree = iqFactory.createBinaryNonCommutativeIQTree(
                    updatedLJ,
                    leftChildDecomposition.getChild(),
                    rightChildDecomposition.getChild());

            return iqTreeTools.createOptionalUnaryIQTree(leftChildDecomposition.getOptionalNode(), lJSubtree);
        }
    }

    private Stream<ImmutableExpression> getChildrenExpression(ImmutableList<UnaryIQTreeDecomposition<FilterNode>> childrenDecomposition) {
        return childrenDecomposition.stream()
                .filter(UnaryIQTreeDecomposition::isPresent)
                .map(UnaryIQTreeDecomposition::get)
                .map(FilterNode::getFilterCondition);
    }
}

