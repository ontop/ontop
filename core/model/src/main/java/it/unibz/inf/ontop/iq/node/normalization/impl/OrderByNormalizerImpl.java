package it.unibz.inf.ontop.iq.node.normalization.impl;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.IQTreeCache;
import it.unibz.inf.ontop.iq.UnaryIQTree;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.node.normalization.OrderByNormalizer;
import it.unibz.inf.ontop.model.term.NonGroundTerm;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Optional;
import java.util.stream.Stream;

public class OrderByNormalizerImpl implements OrderByNormalizer {

    private static final int MAX_ITERATIONS = 1000;
    private final IntermediateQueryFactory iqFactory;

    @Inject
    private OrderByNormalizerImpl(IntermediateQueryFactory iqFactory) {
        this.iqFactory = iqFactory;
    }

    /**
     * NB: the loop is due to the lifting of both distinct and construction nodes
     */
    @Override
    public IQTree normalizeForOptimization(OrderByNode orderByNode, IQTree child, VariableGenerator variableGenerator, IQTreeCache treeCache) {

        Optional<OrderByNode> simplifiedOrderByNode = simplifyOrderByNode(orderByNode, child.getVariableNullability());
        if (!simplifiedOrderByNode.isPresent())
            return child.normalizeForOptimization(variableGenerator);

        // Non-final
        State state = new State(simplifiedOrderByNode.get(), child, variableGenerator);
        for (int i=0; i < MAX_ITERATIONS; i++) {
            State newState = state.liftChild();
            if (newState.hasConverged(state))
                return newState.createNormalizedTree(variableGenerator, treeCache);
            state = newState;
        }
        throw new MinorOntopInternalBugException("OrderByNormalizerImpl.normalizeForOptimization has not converged after "
                 + MAX_ITERATIONS + " iterations");
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

    private class State {
        // Parent first
        private final ImmutableList<UnaryOperatorNode> ancestors;
        private final Optional<OrderByNode> orderByNode;
        private final IQTree child;
        private final VariableGenerator variableGenerator;

        private State(ImmutableList<UnaryOperatorNode> ancestors, Optional<OrderByNode> orderByNode, IQTree child,
                      VariableGenerator variableGenerator) {
            this.ancestors = ancestors;
            this.orderByNode = orderByNode;
            this.child = child;
            this.variableGenerator = variableGenerator;
        }

        /**
         * Initial constructor
         */
        public State(OrderByNode orderByNode, IQTree child, VariableGenerator variableGenerator) {
            this(ImmutableList.of(), Optional.of(orderByNode), child, variableGenerator);
        }

        private State updateParentOrderByAndChild(UnaryOperatorNode newParent, Optional<OrderByNode> newOrderByNode, IQTree newChild) {
            ImmutableList<UnaryOperatorNode> newAncestors = ImmutableList.<UnaryOperatorNode>builder()
                    .add(newParent)
                    .addAll(ancestors)
                    .build();

            return new State(newAncestors, newOrderByNode, newChild, variableGenerator);
        }

        private State updateChild(IQTree newChild) {
            if (newChild.equals(child))
                return this;
            else
                return new State(ancestors, orderByNode, newChild, variableGenerator);
        }

        private State declareAsEmpty(IQTree newChild) {
            return new State(ancestors, Optional.empty(), newChild, variableGenerator);
        }

        public boolean hasConverged(State previousState) {
            return child.equals(previousState.child);
        }

        /**
         * TODO: refactor
         */
        public State liftChild() {

            // No orderByNode -> already converged (empty)
            if (!orderByNode.isPresent())
                return this;

            OrderByNode orderBy = orderByNode.get();

            IQTree newChild = child.normalizeForOptimization(variableGenerator);
            QueryNode newChildRoot = newChild.getRootNode();

            if (newChildRoot instanceof ConstructionNode)
                return liftChildConstructionNode((ConstructionNode) newChildRoot, (UnaryIQTree) newChild, orderBy);
            else if (newChildRoot instanceof EmptyNode)
                return declareAsEmpty(newChild);
            else if (newChildRoot instanceof DistinctNode) {
                return updateParentOrderByAndChild((DistinctNode) newChildRoot, Optional.of(orderBy),
                        ((UnaryIQTree)newChild).getChild());
            }
            else
                return updateChild(newChild);
        }


        /**
         * Lifts the construction node above and updates the order comparators
         */
        private State liftChildConstructionNode(ConstructionNode childRoot, UnaryIQTree child, OrderByNode orderBy) {
            return updateParentOrderByAndChild(childRoot,
                    orderBy.applySubstitution(childRoot.getSubstitution())
                            .flatMap(o -> simplifyOrderByNode(o, child.getChild().getVariableNullability())),
                    child.getChild());
        }

        public IQTree createNormalizedTree(VariableGenerator variableGenerator, IQTreeCache treeCache) {
            IQTree orderByLevelTree = orderByNode
                    .map(n -> (IQTree) iqFactory.createUnaryIQTree(n, child, treeCache.declareAsNormalizedForOptimizationWithEffect()))
                    .orElse(child);

            if (ancestors.isEmpty())
                return orderByLevelTree;

            return ancestors.stream()
                    .reduce(orderByLevelTree, (t, n) -> iqFactory.createUnaryIQTree(n, t),
                            // Should not be called
                            (t1, t2) -> { throw new MinorOntopInternalBugException("The order must be respected"); })
                    // Normalizes the ancestors (recursive)
                    .normalizeForOptimization(variableGenerator);
        }
    }

}
