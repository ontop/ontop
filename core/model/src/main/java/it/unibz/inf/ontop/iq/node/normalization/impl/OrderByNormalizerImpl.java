package it.unibz.inf.ontop.iq.node.normalization.impl;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.IQTreeCache;
import it.unibz.inf.ontop.iq.UnaryIQTree;
import it.unibz.inf.ontop.iq.impl.IQTreeTools;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.node.normalization.OrderByNormalizer;
import it.unibz.inf.ontop.iq.visit.impl.StateIQVisitor;
import it.unibz.inf.ontop.model.term.NonGroundTerm;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Optional;
import java.util.stream.Stream;

import static it.unibz.inf.ontop.iq.impl.IQTreeTools.UnaryOperatorSequence;

public class OrderByNormalizerImpl implements OrderByNormalizer {

    private final IntermediateQueryFactory iqFactory;
    private final IQTreeTools iqTreeTools;

    private static final int MAX_NORMALIZATION_ITERATIONS = 10000;

    @Inject
    private OrderByNormalizerImpl(IntermediateQueryFactory iqFactory, IQTreeTools iqTreeTools) {
        this.iqFactory = iqFactory;
        this.iqTreeTools = iqTreeTools;
    }

    /**
     * NB: the loop is due to the lifting of both distinct and construction nodes
     */
    @Override
    public IQTree normalizeForOptimization(OrderByNode orderByNode, IQTree child, VariableGenerator variableGenerator, IQTreeCache treeCache) {

        Optional<OrderByNode> simplifiedOrderByNode = simplifyOrderByNode(orderByNode, child.getVariableNullability());
        if (simplifiedOrderByNode.isEmpty())
            return child.normalizeForOptimization(variableGenerator);

        Context context = new Context(simplifiedOrderByNode.get(), child, variableGenerator, treeCache);
        return context.normalize();
    }

    private Optional<OrderByNode> simplifyOrderByNode(OrderByNode orderByNode, VariableNullability variableNullability) {
        ImmutableList<OrderByNode.OrderComparator> newComparators = orderByNode.getComparators().stream()
                .flatMap(c -> Stream.of(c.getTerm())
                        .map(t -> t.simplify(variableNullability))
                        .filter(t -> t instanceof NonGroundTerm)
                        .map(t -> (NonGroundTerm) t)
                        .map(t -> iqFactory.createOrderComparator(t, c.isAscending())))
                .collect(ImmutableCollectors.toList());

        return Optional.of(newComparators)
                .filter(cs -> !cs.isEmpty())
                .map(iqFactory::createOrderByNode);
    }

    protected class Context {
        private final OrderByNode orderByNode;
        private final IQTree child;
        private final VariableGenerator variableGenerator;
        private final IQTreeCache treeCache;

        protected Context(OrderByNode orderByNode, IQTree child, VariableGenerator variableGenerator, IQTreeCache treeCache) {
            this.orderByNode = orderByNode;
            this.child = child;
            this.variableGenerator = variableGenerator;
            this.treeCache = treeCache;
        }

        IQTree normalize() {
            return StateIQVisitor.reachFixedPoint(
                            new State(
                                    UnaryOperatorSequence.of(),
                                    Optional.of(orderByNode),
                                    child),
                            MAX_NORMALIZATION_ITERATIONS)
                    .toIQTree();
        }

        /**
         * A sequence of ConstructionNode and DistinctNode,
         * followed by an optional OrderByNode, followed by a child tree.
         */
        @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
        protected class State extends StateIQVisitor<State> {
            private final UnaryOperatorSequence<UnaryOperatorNode> ancestors;
            private final Optional<OrderByNode> orderByNode;
            private final IQTree child;

            private State(UnaryOperatorSequence<UnaryOperatorNode> ancestors, Optional<OrderByNode> orderByNode, IQTree child) {
                this.ancestors = ancestors;
                this.orderByNode = orderByNode;
                this.child = child;
            }

            @Override
            public State transformConstruction(UnaryIQTree tree, ConstructionNode node, IQTree newChild) {
                var newOrderByNode = orderByNode.get().applySubstitution(node.getSubstitution())
                        .flatMap(o -> simplifyOrderByNode(o, newChild.getVariableNullability()));
                State newState = new State(ancestors.append(node), newOrderByNode, newChild);

                return newOrderByNode.isEmpty()
                        ? newState.done()
                        : newState.continueTo(newChild);
            }

            @Override
            public State transformDistinct(UnaryIQTree tree, DistinctNode node, IQTree newChild) {
                return new State(ancestors.append(node), orderByNode, newChild)
                        .continueTo(newChild);
            }

            @Override
            public State transformEmpty(EmptyNode tree) {
                return new State(ancestors, Optional.empty(), child)
                        .done();
            }

            @Override
            public State reduce() {
                return continueTo(child)
                        .normalizeChild();
            }

            private State normalizeChild() {
                return new State(ancestors, orderByNode, child.normalizeForOptimization(variableGenerator));
            }

            @Override
            public IQTree toIQTree() {
                IQTree orderByLevelTree = orderByNode
                        .<IQTree>map(n -> iqFactory.createUnaryIQTree(n, child, treeCache.declareAsNormalizedForOptimizationWithEffect()))
                        .orElse(child);

                if (ancestors.isEmpty())
                    return orderByLevelTree;

                return iqTreeTools.createAncestorsUnaryIQTree(ancestors, orderByLevelTree)
                        // Normalizes the ancestors (recursive)
                        .normalizeForOptimization(variableGenerator);
            }

            @Override
            public boolean equals(Object o) {
                if (o instanceof State) {
                    State other = (State) o;
                    return child.equals(other.child);
                }
                return false;
            }
        }
    }
}
