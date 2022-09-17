package it.unibz.inf.ontop.iq.optimizer.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.BinaryNonCommutativeIQTree;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.NaryIQTree;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.optimizer.FilterLifter;
import it.unibz.inf.ontop.iq.transform.impl.DefaultRecursiveIQTreeVisitingTransformer;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Optional;
import java.util.stream.Stream;

public class FilterLifterImpl implements FilterLifter {

    private final IntermediateQueryFactory iqFactory;
    private final TermFactory termFactory;

    @Inject
    private FilterLifterImpl(IntermediateQueryFactory iqFactory, TermFactory termFactory) {
        this.iqFactory = iqFactory;
        this.termFactory = termFactory;
    }

    @Override
    public IQ optimize(IQ query) {
        TreeTransformer treeTransformer = new TreeTransformer(iqFactory);
        return iqFactory.createIQ(
                query.getProjectionAtom(),
                query.getTree().acceptTransformer(treeTransformer)
        );
    }

    private class TreeTransformer extends DefaultRecursiveIQTreeVisitingTransformer {

        TreeTransformer(IntermediateQueryFactory iqFactory) {
            super(iqFactory);
        }

        @Override
        public IQTree transformConstruction(IQTree tree, ConstructionNode cn, IQTree child) {

            child = child.acceptTransformer(this);

            if (tree.getRootNode().equals(cn)) {
                return iqFactory.createUnaryIQTree(cn, child);
            }

            Optional<FilterNode> rootFilter = getOptionalRootFilter(child);
            if (rootFilter.isPresent()) {
                FilterNode filter = rootFilter.get();
                ConstructionNode updatedCn = updateConstructionNode(
                        cn,
                        filter.getFilterCondition()
                );
                return iqFactory.createUnaryIQTree(
                        filter,
                        iqFactory.createUnaryIQTree(
                                updatedCn,
                                discardOptionalRootFilter(child)
                        ));
            }
            return iqFactory.createUnaryIQTree(
                    cn,
                    child
            );
        }

        @Override
        public IQTree transformFilter(IQTree tree, FilterNode filter, IQTree child) {

            child = child.acceptTransformer(this);
            Optional<FilterNode> rootFilter = getOptionalRootFilter(child);

            if (rootFilter.isPresent()) {
                filter = iqFactory.createFilterNode(termFactory.getConjunction(
                        filter.getFilterCondition(),
                        rootFilter.get().getFilterCondition()
                ));
            }
            return iqFactory.createUnaryIQTree(
                    filter,
                    discardOptionalRootFilter(child)
            );
        }

        @Override
        public IQTree transformFlatten(IQTree tree, FlattenNode fn, IQTree child) {

            child = child.acceptTransformer(this);

            Optional<FilterNode> rootFilter = getOptionalRootFilter(child);
            if (rootFilter.isPresent()) {
                FilterNode filter = rootFilter.get();
                return iqFactory.createUnaryIQTree(
                        filter,
                        iqFactory.createUnaryIQTree(
                                fn,
                                discardOptionalRootFilter(child)
                        ));
            }
            return iqFactory.createUnaryIQTree(
                    fn,
                    child
            );
        }

        @Override
        public IQTree transformUnion(IQTree tree, UnionNode rootNode, ImmutableList<IQTree> children) {
            children = children.stream()
                    .map(c -> c.acceptTransformer(this))
                    .collect(ImmutableCollectors.toList());

            Optional<ImmutableExpression> childrenExpression =
                    termFactory.getConjunction(
                            children.stream()
                                    .map(t -> getOptionalRootFilter(t))
                                    .filter(o -> o.isPresent())
                                    .map(o -> o.get().getFilterCondition())
                    );

            if (childrenExpression.isPresent()) {
                children = children.stream()
                        .map(t -> discardOptionalRootFilter(t))
                        .collect(ImmutableCollectors.toList());
            }

            NaryIQTree unionSubtree = iqFactory.createNaryIQTree(
                    iqFactory.createUnionNode(children.get(0).getVariables()),
                    children
            );

            return childrenExpression.isPresent() ?
                    iqFactory.createUnaryIQTree(
                            iqFactory.createFilterNode(childrenExpression.get()),
                            unionSubtree
                    ) :
                    unionSubtree;
        }

        @Override
        public IQTree transformInnerJoin(IQTree tree, InnerJoinNode joinNode, ImmutableList<IQTree> children) {

            children = children.stream()
                    .map(c -> c.acceptTransformer(this))
                    .collect(ImmutableCollectors.toList());

            Optional<ImmutableExpression> childrenExpression =
                    termFactory.getConjunction(
                            children.stream()
                                    .map(t -> getOptionalRootFilter(t))
                                    .filter(o -> o.isPresent())
                                    .map(o -> o.get().getFilterCondition())
                    );

            Optional<ImmutableExpression> explicitJoinCondition = joinNode.getOptionalFilterCondition();

            if (childrenExpression.isPresent()) {
                children = children.stream()
                        .map(t -> discardOptionalRootFilter(t))
                        .collect(ImmutableCollectors.toList());
            }

            NaryIQTree joinSubtree = iqFactory.createNaryIQTree(
                    iqFactory.createInnerJoinNode(),
                    children
            );

            ImmutableList.Builder<ImmutableExpression> builder = ImmutableList.builder();
            childrenExpression.ifPresent(e -> builder.add(e));
            explicitJoinCondition.ifPresent(e -> builder.add(e));

            ImmutableList<ImmutableExpression> conjuncts = builder.build();

            return conjuncts.isEmpty() ?
                    joinSubtree :
                    iqFactory.createUnaryIQTree(
                            iqFactory.createFilterNode(termFactory.getConjunction(conjuncts)),
                            joinSubtree
                    );
        }

        @Override
        public IQTree transformLeftJoin(IQTree tree, LeftJoinNode rootNode, IQTree leftChild, IQTree rightChild) {
            leftChild = leftChild.acceptTransformer(this);
            rightChild = rightChild.acceptTransformer(this);

            Optional<FilterNode> optionalLeftFilter = getOptionalRootFilter(leftChild);
            Optional<FilterNode> optionalRightFilter = getOptionalRootFilter(rightChild);

            rightChild = discardOptionalRootFilter(rightChild);
            leftChild = discardOptionalRootFilter(leftChild);

            LeftJoinNode updatedLJ = (optionalRightFilter.isPresent()) ?
                    iqFactory.createLeftJoinNode(termFactory.getConjunction(
                            rootNode.getOptionalFilterCondition(),
                            Stream.of(optionalRightFilter.get().getFilterCondition()))
                    ) :
                    rootNode;
            BinaryNonCommutativeIQTree lJSubtree = iqFactory.createBinaryNonCommutativeIQTree(
                    updatedLJ,
                    leftChild,
                    rightChild
            );
            return (optionalLeftFilter.isPresent()) ?
                    iqFactory.createUnaryIQTree(
                            optionalLeftFilter.get(),
                            lJSubtree
                    ) :
                    lJSubtree;
        }
    }

    private Optional<FilterNode> getOptionalRootFilter(IQTree tree) {
        return Optional.of(tree.getRootNode())
                .filter(n -> n instanceof FilterNode)
                .map(n -> (FilterNode) n);
    }

    private IQTree discardOptionalRootFilter(IQTree tree) {
        return tree.getRootNode() instanceof FilterNode ?
                tree.getChildren().get(0) :
                tree;
    }

    private ConstructionNode updateConstructionNode(ConstructionNode cn, ImmutableExpression filteringCondition) {

        // add the variable that appear in the expression to ones projected by the cn
        ImmutableSet<Variable> projectedVars = ImmutableSet.<Variable>builder()
                .addAll(filteringCondition.getVariables())
                .addAll(cn.getVariables())
                .build();

        return iqFactory.createConstructionNode(projectedVars, cn.getSubstitution());
    }
}

