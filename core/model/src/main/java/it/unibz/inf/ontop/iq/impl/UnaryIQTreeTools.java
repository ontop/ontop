package it.unibz.inf.ontop.iq.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.UnaryIQTree;
import it.unibz.inf.ontop.iq.node.UnaryOperatorNode;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

public class UnaryIQTreeTools {
    /**
     * Decomposition of a UnaryIQTree into a possibly empty node, child, tree
     * and non-empty tail: the tree is the node together with the child, while
     * the tail is the remaining part of the UnaryIQTree.
     * In other words, either the tail is the whole UnaryIQTree
     * (and then isPresent returns false and getNode, getChild and getTree fail),
     * or the tail is the child of the UnaryIQTree.
     *
     * @param <T>
     */

    public static class UnaryIQTreeDecomposition<T extends UnaryOperatorNode> extends IQTreeTools.IQTreeDecomposition<T, UnaryIQTree> {
        private final IQTree tail;
        private final IQTree child;

        private UnaryIQTreeDecomposition(T node, UnaryIQTree tree) {
            super(node, tree);
            this.child = tree.getChild();
            this.tail = child;
        }

        private UnaryIQTreeDecomposition(IQTree tree) {
            super(null, null);
            this.child = null;
            this.tail = Objects.requireNonNull(tree);
        }

        @Nonnull
        public IQTree getChild() {
            return Objects.requireNonNull(child);
        }

        @Nonnull
        public IQTree getTail() {
            return tail;
        }

        public static <T extends UnaryOperatorNode> UnaryIQTreeDecomposition<T> of(IQTree tree, Class<T> nodeClass) {
            return nodeClass.isInstance(tree.getRootNode())
                    ? new UnaryIQTreeDecomposition<>(nodeClass.cast(tree.getRootNode()), ((UnaryIQTree)tree))
                    : new UnaryIQTreeDecomposition<>(tree);
        }

        public static <T extends UnaryOperatorNode> UnaryIQTreeDecomposition<T> of(UnaryIQTreeDecomposition<?> parent, Class<T> nodeClass) {
            IQTree tree = parent.getTail();
            return nodeClass.isInstance(tree.getRootNode())
                    ? new UnaryIQTreeDecomposition<>(nodeClass.cast(tree.getRootNode()), ((UnaryIQTree)tree))
                    : new UnaryIQTreeDecomposition<>(tree);
        }

        public static <T extends UnaryOperatorNode> ImmutableList<UnaryIQTreeDecomposition<T>> of(ImmutableList<IQTree> list, Class<T> nodeClass) {
            return list.stream()
                    .map(c -> UnaryIQTreeDecomposition.of(c, nodeClass))
                    .collect(ImmutableCollectors.toList());
        }

        public static <T extends UnaryOperatorNode> ImmutableList<IQTree> getTails(ImmutableList<UnaryIQTreeDecomposition<T>> list) {
            return list.stream()
                    .map(UnaryIQTreeDecomposition::getTail)
                    .collect(ImmutableCollectors.toList());
        }

        public static <T extends UnaryOperatorNode> Stream<T> getNodeStream(ImmutableList<UnaryIQTreeDecomposition<T>> list) {
            return list.stream()
                    .map(UnaryIQTreeDecomposition::getOptionalNode)
                    .flatMap(Optional::stream);
        }

    }


    public static class UnaryOperatorSequence<T extends UnaryOperatorNode> {
        private final ImmutableList<T> list;
        private UnaryOperatorSequence(ImmutableList<T> list) {
            this.list = list;
        }

        public boolean isEmpty() {
            return list.isEmpty();
        }

        public T getLast() {
            return list.get(list.size() - 1);
        }

        public Stream<T> stream() {
            return list.stream();
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof UnaryOperatorSequence) {
                UnaryOperatorSequence<?> other = (UnaryOperatorSequence<?>) o;
                return list.equals(other.list);
            }
            return false;
        }

        public UnaryOperatorSequence<T> append(T node) {
            return new UnaryOperatorSequence<>(
                    Stream.concat(list.stream(), Stream.of(node))
                            .collect(ImmutableList.toImmutableList()));
        }

        public UnaryOperatorSequence<T> append(Optional<? extends T> optionalNode) {
            return optionalNode.map(this::append)
                    .orElse(this);
        }

        public UnaryOperatorSequence<T> append(Stream<? extends T> stream) {
            return new UnaryOperatorSequence<>(
                    Stream.concat(list.stream(), stream)
                            .collect(ImmutableList.toImmutableList()));
        }

        public static <T extends UnaryOperatorNode> UnaryOperatorSequence<T> of() {
            return new UnaryOperatorSequence<>(ImmutableList.of());
        }
    }
}
