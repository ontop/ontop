package it.unibz.inf.ontop.iq.node.normalization.impl;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.IQTreeCache;
import it.unibz.inf.ontop.iq.UnaryIQTree;
import it.unibz.inf.ontop.iq.impl.IQTreeTools;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.node.normalization.OrderByNormalizer;
import it.unibz.inf.ontop.iq.visit.impl.IQStateOptionalTransformer;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Optional;

import static it.unibz.inf.ontop.iq.node.normalization.impl.NormalizationContext.UnarySubTree;

public class OrderByNormalizerImpl implements OrderByNormalizer {

    private final IQTreeTools iqTreeTools;

    @Inject
    private OrderByNormalizerImpl(IQTreeTools iqTreeTools) {
        this.iqTreeTools = iqTreeTools;
    }

    @Override
    public IQTree normalizeForOptimization(OrderByNode orderByNode, IQTree child, VariableGenerator variableGenerator, IQTreeCache treeCache) {
        UnarySubTree<OrderByNode> initialSubTree = UnarySubTree.of(orderByNode, child);
        Context context = new Context(initialSubTree.getChild().getVariables(), variableGenerator, treeCache);
        return context.normalize(initialSubTree);
    }

    private class Context extends NormalizationContext {

        Context(ImmutableSet<Variable> projectedVariables, VariableGenerator variableGenerator, IQTreeCache treeCache) {
            super(projectedVariables, variableGenerator, treeCache, OrderByNormalizerImpl.this.iqTreeTools);
        }

        UnarySubTree<OrderByNode> simplify(UnarySubTree<OrderByNode> tree) {
            var variableNullability = tree.getChild().getVariableNullability();
            var optionalNewComparators = tree.getOptionalNode()
                    .map(o -> iqTreeTools.transformComparators(
                            o.getComparators(),
                            t -> t.simplify(variableNullability)));

            return UnarySubTree.of(
                    iqTreeTools.createOptionalOrderByNode(optionalNewComparators),
                    tree.getChild());
        }

        /**
         * S state ia a sequence of CONSTRUCT and DISTINCT,
         * followed by an optional ORDER BY, followed by a child tree.
         */

        IQTree normalize(UnarySubTree<OrderByNode> initialSubTree) {
            var initial = State.initial(simplify(normalizeChild(initialSubTree)));

            var state = initial.reachFinal(this::liftThroughOrderBy);

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

            OrderByNode orderByNode = optionalOrderBy.get();
            return subTree.getChild().acceptVisitor(new IQStateOptionalTransformer<>() {
                @Override
                public Optional<State<UnaryOperatorNode, UnarySubTree<OrderByNode>>> transformConstruction(UnaryIQTree tree, ConstructionNode node, IQTree newChild) {
                    Optional<OrderByNode> newOptionalOrderBy = orderByNode.applySubstitution(node.getSubstitution());
                    // will be final on the next iteration if the OrderBy node is absent
                    return Optional.of(state.lift(node, simplify(UnarySubTree.of(newOptionalOrderBy, newChild))));
                }

                @Override
                public Optional<State<UnaryOperatorNode, UnarySubTree<OrderByNode>>> transformDistinct(UnaryIQTree tree, DistinctNode node, IQTree newChild) {
                    return Optional.of(state.lift(node, UnarySubTree.of(orderByNode, newChild)));
                }
            });
        }

        IQTree asIQTree(State<UnaryOperatorNode, UnarySubTree<OrderByNode>> state) {
            UnarySubTree<OrderByNode> subTree = state.getSubTree();
            if (subTree.getChild().isDeclaredAsEmpty())
                return createEmptyNode();

            IQTree orderByLevelTree = iqTreeTools.unaryIQTreeBuilder()
                    .append(subTree.getOptionalNode(), () -> getNormalizedTreeCache(true))
                    .build(subTree.getChild());

            return asIQTree(state.getAncestors(), orderByLevelTree);
        }
    }
}
