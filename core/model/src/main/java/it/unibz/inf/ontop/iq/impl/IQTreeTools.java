package it.unibz.inf.ontop.iq.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.*;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.request.FunctionalDependencies;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.substitution.Substitution;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
@Singleton
public class IQTreeTools {

    private final IntermediateQueryFactory iqFactory;
    private final TermFactory termFactory;

    @Inject
    private IQTreeTools(IntermediateQueryFactory iqFactory, TermFactory termFactory) {
        this.iqFactory = iqFactory;
        this.termFactory = termFactory;
    }

    public static ImmutableSet<Variable> computeStrictDependentsFromFunctionalDependencies(IQTree tree) {
        FunctionalDependencies functionalDependencies = tree.inferFunctionalDependencies();
        ImmutableSet<Variable> dependents = functionalDependencies.stream()
                .flatMap(e -> e.getValue().stream())
                .collect(ImmutableCollectors.toSet());
        ImmutableSet<Variable> determinants = functionalDependencies.stream()
                .flatMap(e -> e.getKey().stream())
                .collect(ImmutableCollectors.toSet());
        return Sets.difference(dependents, determinants).immutableCopy();
    }


    public EmptyNode createEmptyNode(DownPropagation ds) {
        return iqFactory.createEmptyNode(ds.computeProjectedVariables());
    }

    public IQ createMappingIQ(DistinctVariableOnlyDataAtom atom, Substitution<?> substitution, IQTree child) {
        return iqFactory.createIQ(atom,
                iqFactory.createUnaryIQTree(
                        iqFactory.createConstructionNode(atom.getVariables(), substitution),
                        child));
    }

    public Optional<FilterNode> createOptionalFilterNode(Optional<ImmutableExpression> expression) {
        return expression.map(iqFactory::createFilterNode);
    }

    public Optional<OrderByNode> createOptionalOrderByNode(Optional<ImmutableList<OrderByNode.OrderComparator>> comparators) {
        return comparators
                .filter(cs -> !cs.isEmpty())
                .map(iqFactory::createOrderByNode);
    }

    public ImmutableList<OrderByNode.OrderComparator> transformComparators(ImmutableList<OrderByNode.OrderComparator> comparators, Function<? super NonGroundTerm, ImmutableTerm> transformer) {
        return comparators.stream()
                .flatMap(c -> Stream.of(c.getTerm())
                        .map(transformer)
                        .filter(t -> t instanceof NonGroundTerm)
                        .map(t -> (NonGroundTerm) t)
                        .map(t -> iqFactory.createOrderComparator(t, c.isAscending())))
                .collect(ImmutableCollectors.toList());
    }

    public ConstructionNode createExtendingConstructionNode(Set<Variable> subTreeVariables, Substitution<?> extendingSubstitution) {
        return iqFactory.createConstructionNode(
                Sets.union(subTreeVariables, extendingSubstitution.getDomain()).immutableCopy(),
                extendingSubstitution);
    }

    public <T extends ImmutableTerm> ConstructionNode replaceSubstitution(ConstructionNode cn, Function<Substitution<ImmutableTerm>, Substitution<T>> substitutionTransformer) {
        return iqFactory.createConstructionNode(
                cn.getVariables(),
                substitutionTransformer.apply(cn.getSubstitution()));
    }

    public Optional<ConstructionNode> createOptionalConstructionNode(Supplier<ImmutableSet<Variable>> projectedVariables, Substitution<?> substitution) {
        return substitution.isEmpty()
            ? Optional.empty()
            : Optional.of(iqFactory.createConstructionNode(projectedVariables.get(), substitution));
    }

    public Optional<ConstructionNode> createOptionalConstructionNode(ImmutableSet<Variable> signature, Substitution<?> substitution, IQTree newTree) {
        return substitution.isEmpty() && signature.equals(newTree.getVariables())
                ? Optional.empty()
                : Optional.of(iqFactory.createConstructionNode(signature, substitution));
    }

    public Optional<DistinctNode> createOptionalDistinctNode(boolean f) {
        return f ? Optional.of(iqFactory.createDistinctNode()) : Optional.empty();
    }

    public IQTree createUnionTree(ImmutableSet<Variable> variables, ImmutableList<IQTree> children) {
        return iqFactory.createNaryIQTree(iqFactory.createUnionNode(variables), children);
    }

    public Optional<IQTree> createOptionalInnerJoinTree(Optional<ImmutableExpression> filter, ImmutableList<? extends IQTree> list) {
        switch (list.size()) {
            case 0:
                return Optional.empty();
            case 1:
                return Optional.of(unaryIQTreeBuilder()
                        .append(createOptionalFilterNode(filter))
                        .build(list.get(0)));
            default:
                return Optional.of(createInnerJoinTree(filter, (ImmutableList)list));
        }
    }

    public NaryIQTree createInnerJoinTree(Optional<ImmutableExpression> filter, ImmutableList<IQTree> children) {
        return iqFactory.createNaryIQTree(iqFactory.createInnerJoinNode(filter), children);
    }

    public NaryIQTree createInnerJoinTree(ImmutableList<IQTree> children) {
        return iqFactory.createNaryIQTree(iqFactory.createInnerJoinNode(), children);
    }

    public BinaryNonCommutativeIQTree createLeftJoinTree(Optional<ImmutableExpression> filter, IQTree leftChild, IQTree rightChild) {
        return iqFactory.createBinaryNonCommutativeIQTree(iqFactory.createLeftJoinNode(filter), leftChild, rightChild);
    }

    public <T extends UnaryOperatorNode> UnaryIQTreeBuilder<T> unaryIQTreeBuilder() {
        return new UnaryIQTreeBuilder<>(iqFactory, ImmutableList.of(), ImmutableMap.of(), Optional.empty());
    }

    public <T extends UnaryOperatorNode> UnaryIQTreeBuilder<T> unaryIQTreeBuilder(ImmutableSet<Variable> signature) {
        return new UnaryIQTreeBuilder<>(iqFactory, ImmutableList.of(), ImmutableMap.of(), Optional.of(signature));
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

        public static <T extends UnaryOperatorNode> UnaryOperatorSequence<T> of(Stream<? extends T> stream) {
            return new UnaryOperatorSequence<>(stream.collect(ImmutableList.toImmutableList()));
        }
    }

    public ImmutableExpression getConjunction(ImmutableExpression expression1, ImmutableExpression expression2) {
        return termFactory.getConjunction(expression1, expression2);
    }

    public ImmutableExpression getConjunction(Optional<ImmutableExpression> optionalExpression, ImmutableExpression expression) {
        return optionalExpression
                .map(c -> termFactory.getConjunction(c, expression))
                .orElse(expression);
    }

    public Optional<ImmutableExpression> getConjunction(Optional<ImmutableExpression> optionalExpression1, Optional<ImmutableExpression> optionalExpression2) {
        return termFactory.getConjunction(Stream.concat(optionalExpression1.stream(), optionalExpression2.stream()));
    }

    // TODO: to be eliminated later, but some tests depend on the order of conjuncts
    public ImmutableExpression getConjunction(ImmutableExpression expression, Optional<ImmutableExpression> optionalExpression) {
        return optionalExpression
                .map(c -> termFactory.getConjunction(expression, c))
                .orElse(expression);
    }

    public static class IQTreeDecomposition<N extends QueryNode, T extends IQTree> {
        protected final N node; // nullable
        protected final T tree; // nullable

        protected IQTreeDecomposition(N node, T tree) {
            this.node = node;
            this.tree = tree;
        }

        @Nonnull
        public Optional<N> getOptionalNode() {
            return Optional.ofNullable(node);
        }

        public boolean isPresent() {
            return node != null;
        }

        @Nonnull
        public N getNode() {
            return Objects.requireNonNull(node);
        }

        @Nonnull
        public T getTree() {
            return Objects.requireNonNull(tree);
        }
    }

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

    public static class UnaryIQTreeDecomposition<T extends UnaryOperatorNode> extends IQTreeDecomposition<T, UnaryIQTree> {
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


    public static <T extends QueryNode> boolean contains(IQTree tree, Class<T> nodeClass) {
        return nodeClass.isInstance(tree.getRootNode()) ||
                tree.getChildren().stream().anyMatch(t -> contains(t, nodeClass));
    }
}
