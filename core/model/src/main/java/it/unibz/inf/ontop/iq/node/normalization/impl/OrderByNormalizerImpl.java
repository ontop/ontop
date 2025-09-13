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

import static it.unibz.inf.ontop.iq.impl.IQTreeTools.UnaryOperatorSequence;
import static it.unibz.inf.ontop.iq.visit.impl.IQStateOptionalTransformer.*;

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

    protected class Context extends NormalizationContext {
        private final OrderByNode initialOrderByNode;
        private final IQTree initialChild;
        private final IQTreeCache treeCache;

        protected Context(OrderByNode initialOrderByNode, IQTree initialChild, VariableGenerator variableGenerator, IQTreeCache treeCache) {
            super(variableGenerator);
            this.initialOrderByNode = initialOrderByNode;
            this.initialChild = initialChild;
            this.treeCache = treeCache;
        }

        IQTree normalize() {
            State initial = new State(UnaryOperatorSequence.of(), Optional.of(initialOrderByNode), initialChild);
            State simplified = initial.simplifyOrderByNode().normalizeChild();
            if (simplified.optionalOrderByNode.isEmpty())
                return simplified.asIQTree();

            // NB: the loop is due to the lifting of both distinct and construction nodes
            State state = reachFixedPoint(
                    simplified,
                    s -> IQStateOptionalTransformer.reachFinalState(s, State::liftThroughOrderBy)
                            .normalizeChild(),
                    MAX_NORMALIZATION_ITERATIONS);
            return state.asIQTree();
        }

        /**
         * A sequence of ConstructionNode and DistinctNode,
         * followed by an optional OrderByNode, followed by a child tree.
         */
        @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
        protected class State extends NormalizationState<UnaryOperatorNode> {
            private final Optional<OrderByNode> optionalOrderByNode;
            private final IQTree child;

            private State(UnaryOperatorSequence<UnaryOperatorNode> ancestors, Optional<OrderByNode> optionalOrderByNode, IQTree child) {
                super(ancestors);
                this.optionalOrderByNode = optionalOrderByNode;
                this.child = child;
            }

            protected State simplifyOrderByNode() {
                var variableNullability = child.getVariableNullability();
                var optionalNewComparators = optionalOrderByNode
                        .map(o -> o.getComparators().stream()
                        .flatMap(c -> Stream.of(c.getTerm())
                                .map(t -> t.simplify(variableNullability))
                                .filter(t -> t instanceof NonGroundTerm)
                                .map(t -> (NonGroundTerm) t)
                                .map(t -> iqFactory.createOrderComparator(t, c.isAscending())))
                        .collect(ImmutableCollectors.toList()));

                return new State(getAncestors(),
                        optionalNewComparators
                                .filter(cs -> !cs.isEmpty())
                                .map(iqFactory::createOrderByNode),
                        child);
            }


            protected Optional<State> liftThroughOrderBy() {
                return child.acceptVisitor(new IQStateOptionalTransformer<>() {

                    @Override
                    public Optional<State> transformConstruction(UnaryIQTree tree, ConstructionNode node, IQTree newChild) {
                        return optionalOrderByNode
                                .map(o -> new State(
                                        getAncestors().append(node),
                                        o.applySubstitution(node.getSubstitution()),
                                        newChild)
                                        .simplifyOrderByNode());
                    }

                    @Override
                    public Optional<State> transformDistinct(UnaryIQTree tree, DistinctNode node, IQTree newChild) {
                        return optionalOrderByNode
                                .map(o -> new State(getAncestors().append(node), optionalOrderByNode, newChild));
                    }

                    @Override
                    public Optional<State> transformEmpty(EmptyNode tree) {
                        return optionalOrderByNode
                                .map(o -> new State(getAncestors(), Optional.empty(), child));
                    }
                });
            }

            private State normalizeChild() {
                return new State(getAncestors(), optionalOrderByNode, child.normalizeForOptimization(variableGenerator));
            }

            @Override
            public IQTree asIQTree() {
                IQTree orderByLevelTree = iqTreeTools.unaryIQTreeBuilder()
                        .append(optionalOrderByNode, treeCache::declareAsNormalizedForOptimizationWithEffect)
                        .build(child);

                if (getAncestors().isEmpty())
                    return orderByLevelTree;

                return iqTreeTools.unaryIQTreeBuilder()
                        .append(getAncestors())
                        .build(orderByLevelTree)
                        // Normalizes the ancestors (recursive)
                        .normalizeForOptimization(variableGenerator);
            }

            @Override
            public boolean equals(Object o) {
                if (o instanceof State) {
                    State other = (State) o;
                    return optionalOrderByNode.equals(other.optionalOrderByNode)
                            && child.equals(other.child)
                            && getAncestors().equals(other.getAncestors());
                }
                return false;
            }
        }
    }
}
