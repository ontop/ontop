package it.unibz.inf.ontop.iq.node.normalization.impl;

import com.google.common.collect.ImmutableSet;
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
import it.unibz.inf.ontop.model.term.Variable;
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

    private class Context extends NormalizationContext {
        private final UnarySubTree<OrderByNode> initialSubTree;
        private final IQTreeCache treeCache;

        Context(OrderByNode initialOrderByNode, IQTree initialChild, VariableGenerator variableGenerator, IQTreeCache treeCache) {
            super(variableGenerator);
            this.initialSubTree = UnarySubTree.of(Optional.of(initialOrderByNode), initialChild);
            this.treeCache = treeCache;
        }

        UnarySubTree<OrderByNode> simplify(UnarySubTree<OrderByNode> tree) {
            var variableNullability = tree.getChild().getVariableNullability();
            var optionalNewComparators = tree.getOptionalNode()
                    .map(o -> o.getComparators().stream()
                            .flatMap(c -> Stream.of(c.getTerm())
                                    .map(t -> t.simplify(variableNullability))
                                    .filter(t -> t instanceof NonGroundTerm)
                                    .map(t -> (NonGroundTerm) t)
                                    .map(t -> iqFactory.createOrderComparator(t, c.isAscending())))
                            .collect(ImmutableCollectors.toList()));

            return UnarySubTree.of(
                    optionalNewComparators
                            .filter(cs -> !cs.isEmpty())
                            .map(iqFactory::createOrderByNode),
                    tree.getChild());
        }

        /**
         * S state ia a sequence of CONSTRUCT and DISTINCT,
         * followed by an optional ORDER BY, followed by a child tree.
         */

        IQTree normalize() {
            State<UnaryOperatorNode, UnarySubTree<OrderByNode>> initial =
                    State.initial(simplify(normalizeChild(initialSubTree)));

            // NB: the loop is due to the lifting of both distinct and construction nodes
            State<UnaryOperatorNode, UnarySubTree<OrderByNode>> state =
                    initial.replace(this::normalizeChild)
                                    .reachFinal(this::liftThroughOrderBy);
                    /*initial.reachFixedPoint(
                    s ->
                            s.replace(normalizeChild(s.getSubTree()))
                                    .reachFinal(this::liftThroughOrderBy),
                    MAX_NORMALIZATION_ITERATIONS);*/

            return asIQTree(state);
        }

        /**
         * One-step lifting of CONSTRUCT and DISTINCT through ORDER BY.
         * The child is assumed to be normalized, so repeated applications are possible
         * (without the need to normalize the child again).
         */
        Optional<State<UnaryOperatorNode, UnarySubTree<OrderByNode>>> liftThroughOrderBy(State<UnaryOperatorNode, UnarySubTree<OrderByNode>> state) {
            UnarySubTree<OrderByNode> subTree = state.getSubTree();
            Optional<OrderByNode> optionalOrderBy = subTree.getOptionalNode();
            if (optionalOrderBy.isEmpty())
                return Optional.empty();

            return subTree.getChild().acceptVisitor(new IQStateOptionalTransformer<>() {
                @Override
                public Optional<State<UnaryOperatorNode, UnarySubTree<OrderByNode>>> transformConstruction(UnaryIQTree tree, ConstructionNode node, IQTree newChild) {
                    Optional<OrderByNode> newOptionalOrderBy = optionalOrderBy.get().applySubstitution(node.getSubstitution());
                    // will be final on the next iteration if the OrderBy node is absent
                    return Optional.of(state.lift(node, simplify(UnarySubTree.of(newOptionalOrderBy, newChild))));
                }

                @Override
                public Optional<State<UnaryOperatorNode, UnarySubTree<OrderByNode>>> transformDistinct(UnaryIQTree tree, DistinctNode node, IQTree newChild) {
                    return Optional.of(state.lift(node, UnarySubTree.of(optionalOrderBy, newChild)));
                }
            });
        }

        IQTree asIQTree(State<UnaryOperatorNode, UnarySubTree<OrderByNode>> state) {
            UnarySubTree<OrderByNode> subTree = state.getSubTree();
            if (subTree.getChild().isDeclaredAsEmpty())
                return iqFactory.createEmptyNode(initialSubTree.getChild().getVariables());

            IQTree orderByLevelTree = iqTreeTools.unaryIQTreeBuilder()
                    .append(subTree.getOptionalNode(), treeCache::declareAsNormalizedForOptimizationWithEffect)
                    .build(subTree.getChild());

            return asIQTree(state.getAncestors(), orderByLevelTree, iqTreeTools);
        }
    }
}
