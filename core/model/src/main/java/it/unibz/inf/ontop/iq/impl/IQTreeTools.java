package it.unibz.inf.ontop.iq.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.BinaryNonCommutativeIQTree;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.NaryIQTree;
import it.unibz.inf.ontop.iq.UnaryIQTree;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.request.FunctionalDependencies;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.substitution.Substitution;
import it.unibz.inf.ontop.substitution.InjectiveSubstitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.stream.Stream;

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

    public IQTree createConstructionNodeTreeIfNontrivial(IQTree child, Substitution<?> substitution, Supplier<ImmutableSet<Variable>> projectedVariables) {
        return substitution.isEmpty()
                ? child
                : iqFactory.createUnaryIQTree(iqFactory.createConstructionNode(projectedVariables.get(), substitution), child);
    }

    public IQTree createConstructionNodeTreeIfNontrivial(IQTree child, ImmutableSet<Variable> variables) {
        return child.getVariables().equals(variables)
                ? child
                : iqFactory.createUnaryIQTree(iqFactory.createConstructionNode(variables), child);
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

    public LeftJoinNode updateLeftJoinNodeWithConjunct(LeftJoinNode leftJoinNode, Optional<ImmutableExpression> conjunct) {
        return conjunct
                .map(c -> iqFactory.createLeftJoinNode(termFactory.getConjunction(leftJoinNode.getOptionalFilterCondition(), Stream.of(c))))
                .orElse(leftJoinNode);
    }

    public InnerJoinNode updateInnerJoinNodeWithConjunct(InnerJoinNode innerJoinNode, Optional<ImmutableExpression> conjunct) {
        return conjunct
                .map(c -> iqFactory.createInnerJoinNode(termFactory.getConjunction(innerJoinNode.getOptionalFilterCondition(), Stream.of(c))))
                .orElse(innerJoinNode);
    }

    public IQTree createOptionalUnaryIQTree(Optional<? extends UnaryOperatorNode> optionalNode, IQTree tree) {
        return optionalNode
                .<IQTree>map(n -> iqFactory.createUnaryIQTree(n, tree))
                .orElse(tree);
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

    public IQTree createAncestorsUnaryIQTree(ImmutableList<? extends UnaryOperatorNode> ancestors, IQTree tree) {
        return ancestors.stream()
                .reduce(tree,
                        (t, a) -> iqFactory.createUnaryIQTree(a, t),
                        (t1, t2) -> { throw new MinorOntopInternalBugException("No merge was expected"); });
    }

    public IQTree createOptionalAncestorsUnaryIQTree(ImmutableList<Optional<? extends UnaryOperatorNode>> ancestors, IQTree tree) {
        return ancestors.stream()
                .flatMap(Optional::stream)
                .collect(ImmutableCollectors.toList())
                .reverse().stream()
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

    public static class NaryIQTreeDecomposition<T extends NaryOperatorNode> extends IQTreeDecomposition<T, NaryIQTree> {
        private final ImmutableList<IQTree> children;

        private NaryIQTreeDecomposition(T node, NaryIQTree tree) {
            super(node, tree);
            this.children = tree.getChildren();
        }

        private NaryIQTreeDecomposition(IQTree tree) {
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
                    : new NaryIQTreeDecomposition<>(tree);
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

        private BinaryNonCommutativeIQTreeDecomposition(IQTree tree) {
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
                    : new BinaryNonCommutativeIQTreeDecomposition<>(tree);
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


}
