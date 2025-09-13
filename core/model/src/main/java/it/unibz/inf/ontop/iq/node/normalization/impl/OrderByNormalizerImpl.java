package it.unibz.inf.ontop.iq.node.normalization.impl;

import com.google.inject.Inject;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.IQTreeCache;
import it.unibz.inf.ontop.iq.UnaryIQTree;
import it.unibz.inf.ontop.iq.impl.IQTreeTools;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.node.normalization.OrderByNormalizer;
import it.unibz.inf.ontop.iq.visit.impl.IQStateOptionalTransformer;
import it.unibz.inf.ontop.model.term.NonGroundTerm;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Optional;
import java.util.stream.Stream;

public class OrderByNormalizerImpl implements OrderByNormalizer {

    private final IntermediateQueryFactory iqFactory;
    private final IQTreeTools iqTreeTools;

    private static final int MAX_NORMALIZATION_ITERATIONS = 10000;

    @Inject
    private OrderByNormalizerImpl(IntermediateQueryFactory iqFactory, IQTreeTools iqTreeTools) {
        this.iqFactory = iqFactory;
        this.iqTreeTools = iqTreeTools;
    }

    @Override
    public IQTree normalizeForOptimization(OrderByNode orderByNode, IQTree child, VariableGenerator variableGenerator, IQTreeCache treeCache) {
        Context context = new Context(orderByNode, child, variableGenerator, treeCache);
        return context.normalize();
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private static class OptionalOrderBySubTree {
        private final Optional<OrderByNode> optionalOrderByNode;
        private final IQTree child;

        OptionalOrderBySubTree(Optional<OrderByNode> optionalOrderByNode, IQTree child) {
            this.optionalOrderByNode = optionalOrderByNode;
            this.child = child;
        }

        IQTree getChild() {
            return child;
        }

        Optional<OrderByNode> getOptionalNode() {
            return optionalOrderByNode;
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof OptionalOrderBySubTree) {
                OptionalOrderBySubTree other = (OptionalOrderBySubTree) o;
                return optionalOrderByNode.equals(other.optionalOrderByNode)
                        && child.equals(other.child);
            }
            return false;
        }
    }

    private class Context extends NormalizationContext {
        private final OrderByNode initialOrderByNode;
        private final IQTree initialChild;
        private final IQTreeCache treeCache;

        Context(OrderByNode initialOrderByNode, IQTree initialChild, VariableGenerator variableGenerator, IQTreeCache treeCache) {
            super(variableGenerator);
            this.initialOrderByNode = initialOrderByNode;
            this.initialChild = initialChild;
            this.treeCache = treeCache;
        }

        OptionalOrderBySubTree simplify(OptionalOrderBySubTree tree) {
            var variableNullability = tree.getChild().getVariableNullability();
            var optionalNewComparators = tree.getOptionalNode()
                    .map(o -> o.getComparators().stream()
                            .flatMap(c -> Stream.of(c.getTerm())
                                    .map(t -> t.simplify(variableNullability))
                                    .filter(t -> t instanceof NonGroundTerm)
                                    .map(t -> (NonGroundTerm) t)
                                    .map(t -> iqFactory.createOrderComparator(t, c.isAscending())))
                            .collect(ImmutableCollectors.toList()));

            return new OptionalOrderBySubTree(
                    optionalNewComparators
                            .filter(cs -> !cs.isEmpty())
                            .map(iqFactory::createOrderByNode),
                    tree.getChild());
        }


        /**
         * A sequence of ConstructionNode and DistinctNode,
         * followed by an optional OrderByNode, followed by a child tree.
         */

        IQTree normalize() {
            State<UnaryOperatorNode, OptionalOrderBySubTree> initial = new State<>(
                    simplify(new OptionalOrderBySubTree(Optional.of(initialOrderByNode), initialChild.normalizeForOptimization(variableGenerator))));

            // NB: the loop is due to the lifting of both distinct and construction nodes
            State<UnaryOperatorNode, OptionalOrderBySubTree> state = initial.reachFixedPoint(
                    s -> s.reachFinalState(this::liftThroughOrderBy),
                    MAX_NORMALIZATION_ITERATIONS);

            return asIQTree(state);
        }

        Optional<State<UnaryOperatorNode, OptionalOrderBySubTree>> liftThroughOrderBy(State<UnaryOperatorNode, OptionalOrderBySubTree> state) {
            OptionalOrderBySubTree subTree = state.getSubTree();
            if (subTree.getOptionalNode().isEmpty())
                return Optional.empty();

            OrderByNode orderByNode = subTree.getOptionalNode().get();
            return state.getSubTree().getChild().acceptVisitor(new IQStateOptionalTransformer<>() {

                @Override
                public Optional<State<UnaryOperatorNode, OptionalOrderBySubTree>> transformConstruction(UnaryIQTree tree, ConstructionNode node, IQTree newChild) {
                    return Optional.of(state.of(node,
                            simplify(new OptionalOrderBySubTree(
                                    orderByNode.applySubstitution(node.getSubstitution()),
                                    newChild.normalizeForOptimization(variableGenerator)))));
                }

                @Override
                public Optional<State<UnaryOperatorNode, OptionalOrderBySubTree>> transformDistinct(UnaryIQTree tree, DistinctNode node, IQTree newChild) {
                    return Optional.of(state.of(node,
                            new OptionalOrderBySubTree(
                                    subTree.getOptionalNode(),
                                    newChild.normalizeForOptimization(variableGenerator))));
                }

                @Override
                public Optional<State<UnaryOperatorNode, OptionalOrderBySubTree>> transformEmpty(EmptyNode tree) {
                    return Optional.of(state.of(
                            new OptionalOrderBySubTree(Optional.empty(), tree)));
                }
            });
        }

        IQTree asIQTree(State<UnaryOperatorNode, OptionalOrderBySubTree> state) {
            IQTree orderByLevelTree = iqTreeTools.unaryIQTreeBuilder()
                    .append(state.getSubTree().getOptionalNode(), treeCache::declareAsNormalizedForOptimizationWithEffect)
                    .build(state.getSubTree().getChild());

            return asIQTree(state.getAncestors(), orderByLevelTree, iqTreeTools);
        }
    }
}
