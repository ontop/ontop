package it.unibz.inf.ontop.iq.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.NaryIQTree;
import it.unibz.inf.ontop.iq.node.InnerJoinNode;
import it.unibz.inf.ontop.iq.node.NaryOperatorNode;
import it.unibz.inf.ontop.iq.node.UnionNode;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class NaryIQTreeTools {

    protected static class Decomposition<T extends NaryOperatorNode> extends IQTreeTools.IQTreeDecomposition<T, NaryIQTree> {
        private final ImmutableList<IQTree> children;

        private Decomposition(T node, NaryIQTree tree) {
            super(node, tree);
            this.children = tree.getChildren();
        }

        private Decomposition() {
            super(null, null);
            this.children = null;
        }

        @Nonnull
        public ImmutableList<IQTree> getChildren() {
            return Objects.requireNonNull(children);
        }

        public ImmutableSet<Variable> projectedVariables() {
            return NaryIQTreeTools.projectedVariables(children);
        }

        public ImmutableList<IQTree> transformChildren(UnaryOperator<IQTree> transformer) {
            return NaryIQTreeTools.transformChildren(children, transformer);
        }
    }

    public static class InnerJoinDecomposition extends Decomposition<InnerJoinNode> {

        private InnerJoinDecomposition(InnerJoinNode rootNode, NaryIQTree tree) {
            super(rootNode, tree);
        }

        private InnerJoinDecomposition() {
        }

        public static InnerJoinDecomposition of(IQTree tree) {
            return tree.getRootNode() instanceof InnerJoinNode
                    ? new InnerJoinDecomposition((InnerJoinNode)tree.getRootNode(), ((NaryIQTree)tree))
                    : new InnerJoinDecomposition();
        }

        public Optional<ImmutableExpression> joinCondition() {
            return node.getOptionalFilterCondition();
        }
    }

    public static class UnionDecomposition extends Decomposition<UnionNode> {

        private UnionDecomposition(UnionNode rootNode, NaryIQTree tree) {
            super(rootNode, tree);
        }

        private UnionDecomposition() {
        }

        public static UnionDecomposition of(IQTree tree) {
            return tree.getRootNode() instanceof UnionNode
                    ? new UnionDecomposition((UnionNode)tree.getRootNode(), ((NaryIQTree)tree))
                    : new UnionDecomposition();
        }

        public static UnionDecomposition of(IQTreeTools.UnaryIQTreeDecomposition<?> parent) {
            IQTree tree = parent.getTail();
            return tree.getRootNode() instanceof UnionNode
                    ? new UnionDecomposition((UnionNode)tree.getRootNode(), ((NaryIQTree)tree))
                    : new UnionDecomposition();
        }

        public UnionDecomposition filter(java.util.function.Predicate<UnionDecomposition> predicate) {
            return isPresent() && predicate.test(this)
                    ? this
                    : new UnionDecomposition();
        }
    }

    public static ImmutableSet<Variable> projectedVariables(ImmutableList<IQTree> children) {
        return children.stream()
                .flatMap(c -> c.getVariables().stream())
                .collect(ImmutableCollectors.toSet());
    }

    public static Stream<Variable> coOccurringVariablesStream(Collection<? extends IQTree> children) {
        /* quadratic time,
         return IntStream.range(0, children.size() - 1)
                .boxed()
                .flatMap(i -> children.get(i).getVariables().stream()
                        .filter(v1 -> IntStream.range(i + 1, children.size())
                                .anyMatch(j -> children.get(j).getVariables().stream().
                                        anyMatch(v1::equals))));
         */

        // grouping should be more efficient - n log n
        Map<Variable, Long> variableOccurrencesCount = children.stream()
                .map(IQTree::getVariables)
                .flatMap(Collection::stream)
                .collect(Collectors.groupingBy(v -> v, Collectors.counting()));

        return variableOccurrencesCount.entrySet().stream()
                .filter(e -> e.getValue() > 1)
                .map(Map.Entry::getKey);
    }

    public static <T> ImmutableList<T> transformChildren(ImmutableList<IQTree> children, Function<IQTree, T> transformer) {
        return children.stream().map(transformer).collect(ImmutableCollectors.toList());
    }

    public static ImmutableList<IQTree> replaceChild(ImmutableList<IQTree> children, int index, IQTree newChild) {
        return IntStream.range(0, children.size())
                .mapToObj(i -> i == index
                        ? newChild
                        : children.get(i))
                .collect(ImmutableCollectors.toList());
    }
}
