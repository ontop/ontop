package it.unibz.inf.ontop.iq.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.*;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.request.FunctionalDependencies;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.substitution.Substitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
@Singleton
public class IQTreeTools {

    private final IntermediateQueryFactory iqFactory;
    private final SubstitutionFactory substitutionFactory;
    private final TermFactory termFactory;

    @Inject
    private IQTreeTools(IntermediateQueryFactory iqFactory, SubstitutionFactory substitutionFactory, TermFactory termFactory) {
        this.iqFactory = iqFactory;
        this.substitutionFactory = substitutionFactory;
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

    public ConstructionNode createProjectingConstructionNode(ImmutableSet<Variable> allVariables, ImmutableSet<Variable> projectedAwayVariables) {
        return iqFactory.createConstructionNode(
                Sets.difference(allVariables, projectedAwayVariables).immutableCopy());
    }

    public <T extends ImmutableTerm> ConstructionNode replaceSubstitution(ConstructionNode cn, Function<Substitution<ImmutableTerm>, Substitution<T>> substitutionTransformer) {
        return iqFactory.createConstructionNode(
                cn.getVariables(),
                substitutionTransformer.apply(cn.getSubstitution()));
    }

    public ConstructionNode createExtendingConstructionNode(Set<Variable> subTreeVariables, Substitution<?> extendingSubstitution) {
        return iqFactory.createConstructionNode(
                Sets.union(subTreeVariables, extendingSubstitution.getDomain()).immutableCopy(),
                extendingSubstitution);
    }

    public Optional<ConstructionNode> createOptionalConstructionNode(Supplier<ImmutableSet<Variable>> projectedVariables, Substitution<?> substitution) {
        if (substitution.isEmpty())
            return Optional.empty();
        return Optional.of(iqFactory.createConstructionNode(projectedVariables.get(), substitution));
    }

    public Optional<FilterNode> createOptionalFilterNode(Optional<ImmutableExpression> expression) {
        return expression.map(iqFactory::createFilterNode);
    }

    public Optional<ConstructionNode> createOptionalConstructionNode(ImmutableSet<Variable> originalSignature, IQTree newTree) {
        // Makes sure no new variable is projected by the returned tree
        return originalSignature.equals(newTree.getVariables())
                ? Optional.empty()
                : Optional.of(iqFactory.createConstructionNode(originalSignature));
    }

    public Optional<ConstructionNode> createOptionalConstructionNode(ImmutableSet<Variable> originalSignature, Substitution<?> substitution, IQTree newTree) {
        return createOptionalConstructionNode(() -> originalSignature, substitution)
                .or(() -> createOptionalConstructionNode(originalSignature, newTree));
    }

    public IQTree createUnionTreeWithOptionalConstructionNodes(ImmutableSet<Variable> signature, Stream<IQTree> childrenStream) {
        return createUnionTree(
                signature,
                childrenStream
                        .map(c -> createOptionalUnaryIQTree(
                                createOptionalConstructionNode(signature, c), c))
                        .collect(ImmutableCollectors.toList()));
    }

    public IQTree createDummyConstructionIQTree(IQTree tree) {
        return iqFactory.createUnaryIQTree(
                iqFactory.createConstructionNode(tree.getVariables()),
                tree);
    }


    public ImmutableSet<Variable> extractChildVariables(ImmutableSet<Variable> groupingVariables,
                                                               Substitution<ImmutableFunctionalTerm> substitution) {
        return Sets.union(groupingVariables, substitution.getRangeVariables()).immutableCopy();
    }


    public IQTree createOptionalUnaryIQTree(Optional<? extends UnaryOperatorNode> optionalNode, IQTree tree) {
        return optionalNode
                .<IQTree>map(n -> iqFactory.createUnaryIQTree(n, tree))
                .orElse(tree);
    }

    public Optional<IQTree> createJoinTree(Optional<ImmutableExpression> filter, ImmutableList<? extends IQTree> list) {
        switch (list.size()) {
            case 0:
                return Optional.empty();
            case 1:
                return Optional.of(createOptionalUnaryIQTree(createOptionalFilterNode(filter), list.get(0)));
            default:
                return Optional.of(iqFactory.createNaryIQTree(iqFactory.createInnerJoinNode(filter), (ImmutableList)list));
        }
    }

    public IQTree createUnionTree(ImmutableSet<Variable> variables, ImmutableList<IQTree> children) {
        return iqFactory.createNaryIQTree(iqFactory.createUnionNode(variables), children);
    }

    public NaryIQTree createInnerJoinTree(Optional<ImmutableExpression> filter, ImmutableList<IQTree> children) {
        return iqFactory.createNaryIQTree(iqFactory.createInnerJoinNode(filter), children);
    }

    public NaryIQTree createInnerJoinTree(ImmutableList<IQTree> children) {
        return iqFactory.createNaryIQTree(iqFactory.createInnerJoinNode(), children);
    }

    public <T extends UnaryOperatorNode> UnaryIQTreeBuilder<T> unaryIQTreeBuilder() {
        return new UnaryIQTreeBuilder<>(iqFactory, ImmutableList.of(), ImmutableMap.of());
    }

    public static class UnaryOperatorSequence<T extends UnaryOperatorNode> {
        private final ImmutableList<T> list;
        private UnaryOperatorSequence(ImmutableList<T> list) {
            this.list = list;
        }

        public UnaryOperatorSequence<T> append(T node) {
            return new UnaryOperatorSequence<>(
                    Stream.concat(list.stream(), Stream.of(node))
                            .collect(ImmutableList.toImmutableList()));
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

        public static <T extends UnaryOperatorNode> UnaryOperatorSequence<T> of(T node) {
            return new UnaryOperatorSequence<>(ImmutableList.of(node));
        }

        public static <T extends UnaryOperatorNode> UnaryOperatorSequence<T> of(Stream<? extends T> stream) {
            return new UnaryOperatorSequence<>(stream.collect(ImmutableList.toImmutableList()));
        }
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

    public ImmutableList<IQTree> createUnaryOperatorChildren(UnaryOperatorNode node, IQTree child) {
         return createUnaryOperatorChildren(node, child.getChildren());
    }

    public ImmutableList<IQTree> createUnaryOperatorChildren(UnaryOperatorNode node, ImmutableList<IQTree> children) {
        return children.stream()
                .<IQTree>map(c -> iqFactory.createUnaryIQTree(node, c))
                .collect(ImmutableCollectors.toList());
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

        public <U> Optional<U> map(BiFunction<? super T, IQTree, ? extends U> function) {
            return Optional.ofNullable(node).map(n -> function.apply(n, child));
        }

        public static <T extends UnaryOperatorNode> UnaryIQTreeDecomposition<T> of(IQTree tree, Class<T> nodeClass) {
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

    }

    public static class NestedUnaryIQTreeDecomposition<T extends UnaryOperatorNode> {
        private final UnaryOperatorSequence<T> sequence;
        private final IQTree child;

        private NestedUnaryIQTreeDecomposition(UnaryOperatorSequence<T> sequence, IQTree child) {
            this.sequence = sequence;
            this.child = child;
        }

        public static <T extends UnaryOperatorNode> NestedUnaryIQTreeDecomposition<T> of(UnaryOperatorSequence<T> sequence, IQTree child) {
            return new NestedUnaryIQTreeDecomposition<>(sequence, child);
        }

        public static <T extends UnaryOperatorNode> NestedUnaryIQTreeDecomposition<T> of(IQTree tree, Class<T> nodeClass) {
            var list = Stream.iterate(
                            UnaryIQTreeDecomposition.of(tree, nodeClass),
                            UnaryIQTreeDecomposition::isPresent,
                            d -> UnaryIQTreeDecomposition.of(d.getChild(), nodeClass))
                    .collect(ImmutableCollectors.toList());

            return list.isEmpty()
                    ? new NestedUnaryIQTreeDecomposition<T>(UnaryOperatorSequence.of(), tree)
                    : new NestedUnaryIQTreeDecomposition<T>(
                            new UnaryOperatorSequence<>(
                                    list.stream()
                                            .map(UnaryIQTreeDecomposition::getNode)
                                            .collect(ImmutableCollectors.toList())),
                    list.get(list.size() - 1).getChild());
        }

        public NestedUnaryIQTreeDecomposition<T> append(T node) {
            return new NestedUnaryIQTreeDecomposition<>(sequence.append(node), child);
        }

        public UnaryOperatorSequence<T> getSequence() {
            return sequence;
        }

        public IQTree getChild() {
            return child;
        }
    }



    public static <T extends QueryNode> boolean contains(IQTree tree, Class<T> nodeClass) {
        return nodeClass.isInstance(tree.getRootNode()) ||
                tree.getChildren().stream().anyMatch(t -> contains(t, nodeClass));
    }

    public IQTree liftIncompatibleDefinitions(UnaryOperatorNode node, Variable variable, IQTree child, VariableGenerator variableGenerator) {
        IQTree newChild = child.liftIncompatibleDefinitions(variable, variableGenerator);

        // Lift the union above the node
        var union = NaryIQTreeTools.UnionDecomposition.of(newChild);
        if (union.isPresent()) {
            if (union.getNode().hasAChildWithLiftableDefinition(variable, newChild.getChildren())) {
                ImmutableList<IQTree> newChildren = createUnaryOperatorChildren(node, newChild);
                return iqFactory.createNaryIQTree(union.getNode(), newChildren);
            }
        }
        return iqFactory.createUnaryIQTree(node, newChild);
    }



    public Substitution<NonVariableTerm> getEmptyNonVariableSubstitution() {
        return substitutionFactory.getSubstitution();
    }

}
