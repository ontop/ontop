package it.unibz.inf.ontop.iq.impl;

import com.google.common.collect.ImmutableList;
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
import it.unibz.inf.ontop.substitution.InjectiveSubstitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.BiFunction;
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

    /**
     * Excludes the variables that are not projected by the IQTree
     *
     * If a "null" variable is propagated down, throws an UnsatisfiableDescendingSubstitutionException.
     *
     */
    public Optional<Substitution<? extends VariableOrGroundTerm>> normalizeDescendingSubstitution(
            IQTree tree, Substitution<? extends VariableOrGroundTerm> descendingSubstitution)
            throws UnsatisfiableDescendingSubstitutionException {

        Substitution<? extends VariableOrGroundTerm> reducedSubstitution = descendingSubstitution.restrictDomainTo(tree.getVariables());

        if (reducedSubstitution.isEmpty())
            return Optional.empty();

        if (reducedSubstitution.rangeAnyMatch(ImmutableTerm::isNull)) {
            throw new UnsatisfiableDescendingSubstitutionException();
        }

        return Optional.of(reducedSubstitution);
    }

    public ImmutableSet<Variable> computeNewProjectedVariables(
            Substitution<? extends ImmutableTerm> descendingSubstitution,
            ImmutableSet<Variable> projectedVariables) {

        ImmutableSet<Variable> newVariables = descendingSubstitution.restrictDomainTo(projectedVariables).getRangeVariables();

        return Sets.union(newVariables, Sets.difference(projectedVariables, descendingSubstitution.getDomain())).immutableCopy();
    }

    public IQ createMappingIQ(DistinctVariableOnlyDataAtom atom, Substitution<?> substitution, IQTree child) {
        return iqFactory.createIQ(atom, createMappingIQTree(atom, substitution, child));
    }

    public IQTree createMappingIQTree(DistinctVariableOnlyDataAtom atom, Substitution<?> substitution, IQTree child) {
        return iqFactory.createUnaryIQTree(
                        iqFactory.createConstructionNode(atom.getVariables(), substitution),
                        child);
    }

    public ConstructionNode createProjectingConstructionNode(ImmutableSet<Variable> allVariables, ImmutableSet<Variable> projectedAwayVariables) {
        return iqFactory.createConstructionNode(
                Sets.difference(allVariables, projectedAwayVariables).immutableCopy());
    }

    public ConstructionNode replaceSubstitution(ConstructionNode cn, Substitution<?> substitution) {
        return iqFactory.createConstructionNode(cn.getVariables(), substitution);
    }

    public ConstructionNode extendSubTreeWithSubstitution(Set<Variable> subTreeVariables, Substitution<?> substitution) {
        return iqFactory.createConstructionNode(
                Sets.union(subTreeVariables, substitution.getDomain()).immutableCopy(),
                substitution);
    }

    public IQTree createTrueTree(Substitution<?> substitution) {
        return iqFactory.createUnaryIQTree(
                iqFactory.createConstructionNode(substitution.getDomain(), substitution),
                iqFactory.createTrueNode());
    }

    public Optional<ConstructionNode> createOptionalConstructionNode(Supplier<ImmutableSet<Variable>> projectedVariables, Substitution<?> substitution) {
        if (substitution.isEmpty())
            return Optional.empty();
        return Optional.of(iqFactory.createConstructionNode(projectedVariables.get(), substitution));
    }

    public Optional<FilterNode> createOptionalFilterNode(Optional<ImmutableExpression> expression) {
        return expression.map(iqFactory::createFilterNode);
    }

    public IQTree createFilterTree(Optional<ImmutableExpression> expression, IQTree child) {
        return expression
                .map(iqFactory::createFilterNode)
                .<IQTree>map(f -> iqFactory.createUnaryIQTree(f, child))
                .orElse(child);
    }

    public Optional<ConstructionNode> createOptionalConstructionNode(ImmutableSet<Variable> originalSignature, IQTree newTree) {
        // Makes sure no new variable is projected by the returned tree
        return originalSignature.equals(newTree.getVariables())
                ? Optional.empty()
                : Optional.of(iqFactory.createConstructionNode(originalSignature));
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


    // TODO: merge with above
    public IQTree createIQTreeWithSignature(ImmutableSet<Variable> signature, IQTree child) {
        return iqFactory.createUnaryIQTree(
                iqFactory.createConstructionNode(signature),
                child);
    }

    public ImmutableSet<Variable> getChildrenVariables(ImmutableList<IQTree> children) {
         return children.stream()
                .flatMap(c -> c.getVariables().stream())
                .collect(ImmutableCollectors.toSet());
    }

    public ImmutableSet<Variable> getChildrenVariables(IQTree leftChild, IQTree rightChild) {
        return Sets.union(leftChild.getVariables(), rightChild.getVariables()).immutableCopy();
    }

    public ImmutableSet<Variable> getChildrenVariables(IQTree child, Variable newVariable) {
        return Sets.union(child.getVariables(), ImmutableSet.of(newVariable)).immutableCopy();
    }

    public ImmutableSet<Variable> extractChildVariables(ImmutableSet<Variable> groupingVariables,
                                                               Substitution<ImmutableFunctionalTerm> substitution) {
        return Sets.union(groupingVariables, substitution.getRangeVariables()).immutableCopy();
    }

    public IQTree propagateDownOptionalConstraint(IQTree child, Optional<ImmutableExpression> constraint, VariableGenerator variableGenerator) {
        return constraint.map(c -> child.propagateDownConstraint(c, variableGenerator)).orElse(child);
    }

    public IQTree applyDescendingSubstitution(IQTree child, Substitution<? extends VariableOrGroundTerm> substitution, Optional<ImmutableExpression> downConstraint, VariableGenerator variableGenerator) {
        return Optional.of(substitution)
                .filter(s -> !s.isEmpty())
                .map(s -> child.applyDescendingSubstitution(s, downConstraint, variableGenerator))
                .orElseGet(() -> propagateDownOptionalConstraint(child, downConstraint, variableGenerator));
    }

    public ImmutableList<IQTree> applyDescendingSubstitution(ImmutableList<IQTree> children, Substitution<? extends VariableOrGroundTerm> substitution, Optional<ImmutableExpression> downConstraint, VariableGenerator variableGenerator) {
        return Optional.of(substitution)
                .filter(s -> !s.isEmpty())
                .map(s -> children.stream()
                        .map(c -> c.applyDescendingSubstitution(s, downConstraint, variableGenerator))
                        .collect(ImmutableCollectors.toList()))
                .or(() -> downConstraint
                        .map(cs -> children.stream()
                                .map(c -> c.propagateDownConstraint(cs, variableGenerator))
                                .collect(ImmutableCollectors.toList())))
                .orElse(children);
    }

    public IQTree createOptionalUnaryIQTree(Optional<? extends UnaryOperatorNode> optionalNode, IQTree tree) {
        return optionalNode
                .<IQTree>map(n -> iqFactory.createUnaryIQTree(n, tree))
                .orElse(tree);
    }

    public IQTree createOptionalUnaryIQTree(Optional<? extends UnaryOperatorNode> optionalNode1, Optional<? extends UnaryOperatorNode> optionalNode2, IQTree tree) {
        return createAncestorsUnaryIQTree(UnaryOperatorSequence.of().append(optionalNode1).append(optionalNode2), tree);
    }

    public IQTree createUnaryIQTree(UnaryOperatorNode node1, UnaryOperatorNode node2, IQTree tree) {
        return iqFactory.createUnaryIQTree(node1,
                iqFactory.createUnaryIQTree(node2, tree));
    }

    public IQTree createUnaryIQTree(UnaryOperatorNode node1, UnaryOperatorNode node2, UnaryOperatorNode node3, IQTree tree) {
        return iqFactory.createUnaryIQTree(node1,
                iqFactory.createUnaryIQTree(node2,
                        iqFactory.createUnaryIQTree(node3, tree)));
    }

    public IQTree createUnaryIQTree(UnaryOperatorNode node1, UnaryOperatorNode node2, UnaryOperatorNode node3, UnaryOperatorNode node4, IQTree tree) {
        return iqFactory.createUnaryIQTree(node1,
                iqFactory.createUnaryIQTree(node2,
                        iqFactory.createUnaryIQTree(node3,
                                iqFactory.createUnaryIQTree(node4, tree))));
    }

    public Optional<IQTree> createJoinTree(Optional<ImmutableExpression> filter, ImmutableList<? extends IQTree> list) {
        switch (list.size()) {
            case 0:
                return Optional.empty();
            case 1:
                return Optional.of(createFilterTree(filter, list.get(0)));
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

    public IQTree createAncestorsUnaryIQTree(UnaryOperatorSequence<? extends UnaryOperatorNode> sequence, IQTree tree) {
        return sequence.list.reverse().stream()
                .reduce(tree,
                        (t, a) -> iqFactory.createUnaryIQTree(a, t),
                        (t1, t2) -> { throw new MinorOntopInternalBugException("No merge was expected"); });
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
        protected final T tree;

        protected IQTreeDecomposition() {
            this.node = null;
            this.tree = null;
        }

        protected IQTreeDecomposition(N node, T tree) {
            this.node = Objects.requireNonNull(node);
            this.tree = Objects.requireNonNull(tree);
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

    public static class NaryIQTreeDecomposition<T extends NaryOperatorNode> extends IQTreeDecomposition<T, NaryIQTree> {
        private final ImmutableList<IQTree> children;

        private NaryIQTreeDecomposition(T node, NaryIQTree tree) {
            super(node, tree);
            this.children = tree.getChildren();
        }

        private NaryIQTreeDecomposition() {
            this.children = null;
        }

        @Nonnull
        public ImmutableList<IQTree> getChildren() {
            return Objects.requireNonNull(children);
        }

        public <U> Optional<U> map(BiFunction<? super T, ImmutableList<IQTree>, ? extends U> function) {
            return Optional.ofNullable(node).map(n -> function.apply(n, children));
        }

        public static <T extends NaryOperatorNode> NaryIQTreeDecomposition<T> of(IQTree tree, Class<T> nodeClass) {
            return nodeClass.isInstance(tree.getRootNode())
                    ? new NaryIQTreeDecomposition<>(nodeClass.cast(tree.getRootNode()), ((NaryIQTree)tree))
                    : new NaryIQTreeDecomposition<>();
        }
    }

    public static class BinaryNonCommutativeIQTreeDecomposition<T extends BinaryNonCommutativeOperatorNode> extends IQTreeDecomposition<T, BinaryNonCommutativeIQTree> {
        private final IQTree leftChild;
        private final IQTree rightChild;

        private BinaryNonCommutativeIQTreeDecomposition(T node, BinaryNonCommutativeIQTree tree) {
            super(node, tree);
            this.leftChild = tree.getLeftChild();
            this.rightChild = tree.getRightChild();
        }

        private BinaryNonCommutativeIQTreeDecomposition() {
            this.leftChild = null;
            this.rightChild = null;
        }

        @Nonnull
        public IQTree getLeftChild() {
            return Objects.requireNonNull(leftChild);
        }

        @Nonnull
        public IQTree getRightChild() {
            return Objects.requireNonNull(rightChild);
        }

        public <U> Optional<U> map(TriFunction<? super T, IQTree, IQTree, ? extends U> function) {
            return Optional.ofNullable(node).map(n -> function.apply(n, leftChild, rightChild));
        }

        @FunctionalInterface
        public interface TriFunction<T1, T2, T3, R> {
            R apply(T1 t1, T2 t2, T3 t3);
        }

        public static <T extends BinaryNonCommutativeOperatorNode> BinaryNonCommutativeIQTreeDecomposition<T> of(IQTree tree, Class<T> nodeClass) {
            return nodeClass.isInstance(tree.getRootNode())
                    ? new BinaryNonCommutativeIQTreeDecomposition<>(nodeClass.cast(tree.getRootNode()), ((BinaryNonCommutativeIQTree)tree))
                    : new BinaryNonCommutativeIQTreeDecomposition<>();
        }
    }

    public static <T extends QueryNode> boolean contains(IQTree tree, Class<T> nodeClass) {
        return nodeClass.isInstance(tree.getRootNode()) ||
                tree.getChildren().stream().anyMatch(t -> contains(t, nodeClass));
    }

    public IQTree liftIncompatibleDefinitions(UnaryOperatorNode node, Variable variable, IQTree child, VariableGenerator variableGenerator) {
        IQTree newChild = child.liftIncompatibleDefinitions(variable, variableGenerator);

        // Lift the union above the node
        var union = NaryIQTreeDecomposition.of(newChild, UnionNode.class);
        if (union.isPresent()) {
            if (union.getNode().hasAChildWithLiftableDefinition(variable, newChild.getChildren())) {
                ImmutableList<IQTree> newChildren = createUnaryOperatorChildren(node, newChild);
                return iqFactory.createNaryIQTree(union.getNode(), newChildren);
            }
        }
        return iqFactory.createUnaryIQTree(node, newChild);
    }


    /**
     * If the substitution is a fresh renaming, returns it as an injective substitution
     */
    public Optional<InjectiveSubstitution<Variable>> extractFreshRenaming(Substitution<? extends ImmutableTerm> descendingSubstitution,
                                                                          ImmutableSet<Variable> projectedVariables) {

        Substitution<Variable> var2VarFragment = descendingSubstitution.restrictRangeTo(Variable.class);
        int size = descendingSubstitution.getDomain().size();

        if (var2VarFragment.getDomain().size() != size
                || Sets.difference(var2VarFragment.getRangeSet(), projectedVariables).size() != size)
            return Optional.empty();

        return Optional.of(var2VarFragment.injective());
    }

    public Substitution<NonVariableTerm> getEmptyNonVariableSubstitution() {
        return substitutionFactory.getSubstitution();
    }


    /**
     * Typically thrown when a "null" variable is propagated down
     *
     */
    public static class UnsatisfiableDescendingSubstitutionException extends Exception {
    }

    public static Stream<Variable> getCoOccurringVariables(ImmutableList<IQTree> children) {
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


}
