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

        @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
        public class OptionalOrderBySubTree {
            private final Optional<OrderByNode> optionalOrderByNode;
            private final IQTree child;

            private OptionalOrderBySubTree(Optional<OrderByNode> optionalOrderByNode, IQTree child) {
                this.optionalOrderByNode = optionalOrderByNode;
                this.child = child;
            }

            IQTree getChild() {
                return child;
            }

            Optional<OrderByNode> getOptionalOrderByNode() {
                return optionalOrderByNode;
            }

            OptionalOrderBySubTree normalizeChild() {
                return new OptionalOrderBySubTree(optionalOrderByNode, child.normalizeForOptimization(variableGenerator));
            }

            OptionalOrderBySubTree simplify() {
                var variableNullability = child.getVariableNullability();
                var optionalNewComparators = optionalOrderByNode
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
                        child);
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

        /**
         * A sequence of ConstructionNode and DistinctNode,
         * followed by an optional OrderByNode, followed by a child tree.
         */

        IQTree normalize() {
            NormalizationState2<UnaryOperatorNode, OptionalOrderBySubTree> initial = new NormalizationState2<>(
                    new OptionalOrderBySubTree(Optional.of(initialOrderByNode), initialChild)
                            .simplify().normalizeChild());
            if (initial.getSubTree().getOptionalOrderByNode().isEmpty())
                return asIQTree(initial);

            // NB: the loop is due to the lifting of both distinct and construction nodes
            NormalizationState2<UnaryOperatorNode, OptionalOrderBySubTree> state = reachFixedPoint(
                    initial,
                    s -> normalizeChild(IQStateOptionalTransformer.reachFinalState(s, this::liftThroughOrderBy)),
                    MAX_NORMALIZATION_ITERATIONS);
            return asIQTree(state); 
        }

        Optional<NormalizationState2<UnaryOperatorNode, OptionalOrderBySubTree>> liftThroughOrderBy(NormalizationState2<UnaryOperatorNode, OptionalOrderBySubTree> state) {
            if (state.getSubTree().getOptionalOrderByNode().isEmpty())
                return Optional.empty();

            OrderByNode orderByNode = state.getSubTree().getOptionalOrderByNode().get();
            return state.getSubTree().getChild().acceptVisitor(new IQStateOptionalTransformer<>() {

                @Override
                public Optional<NormalizationState2<UnaryOperatorNode, OptionalOrderBySubTree>> transformConstruction(UnaryIQTree tree, ConstructionNode node, IQTree newChild) {
                    return Optional.of(state.of(node,
                            new OptionalOrderBySubTree(orderByNode.applySubstitution(node.getSubstitution()), newChild)
                                    .simplify()));
                }

                @Override
                public Optional<NormalizationState2<UnaryOperatorNode, OptionalOrderBySubTree>> transformDistinct(UnaryIQTree tree, DistinctNode node, IQTree newChild) {
                    return Optional.of(state.of(node,
                            new OptionalOrderBySubTree(state.getSubTree().getOptionalOrderByNode(), newChild)));
                }

                @Override
                public Optional<NormalizationState2<UnaryOperatorNode, OptionalOrderBySubTree>> transformEmpty(EmptyNode tree) {
                    return Optional.of(state.of(
                            new OptionalOrderBySubTree(Optional.empty(), tree)));
                }
            });
        }

        NormalizationState2<UnaryOperatorNode, OptionalOrderBySubTree> normalizeChild(NormalizationState2<UnaryOperatorNode, OptionalOrderBySubTree> state) {
            return state.of(state.getSubTree().normalizeChild());
        }

        IQTree asIQTree(NormalizationState2<UnaryOperatorNode, OptionalOrderBySubTree> state) {
            IQTree orderByLevelTree = iqTreeTools.unaryIQTreeBuilder()
                    .append(state.getSubTree().getOptionalOrderByNode(), treeCache::declareAsNormalizedForOptimizationWithEffect)
                    .build(state.getSubTree().getChild());

            if (state.getAncestors().isEmpty())
                return orderByLevelTree;

            return iqTreeTools.unaryIQTreeBuilder()
                    .append(state.getAncestors())
                    .build(orderByLevelTree)
                    // Normalizes the ancestors (recursive)
                    .normalizeForOptimization(variableGenerator);
        }
    }
}
